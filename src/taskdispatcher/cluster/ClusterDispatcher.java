package taskdispatcher.cluster;

import taskdispatcher.AbstractTaskDispatcher;
import taskdispatcher.Job;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of AbstractTaskDispatcher for dispatching jobs on a cluster
 * of computers.
 * @param <J> The type of Job that this dispatcher will be dispatching.
 * @see AbstractTaskDispatcher
 * @author gg32
 */
public class ClusterDispatcher <J extends Job> extends AbstractTaskDispatcher <ClusterTaskRunner<J>,J> {
    
    private ConnectionHandler ch;

    /**
     * Setup this instance, creating the thread that will listen for incoming
     * connections. After calling this it should then be safe for any other
     * machines to try and connect with this one.
     * @see AbstractTaskDispatcher#setUp() 
     */
    @Override
    public void setUp(){
        ch = new ConnectionHandler(this);
        ch.thread = new Thread(ch);
        ch.thread.start();
        publishMessage("Waiting for clients to connect...");
    }
    
    @Override
    public void end(){
        for(ClusterTaskRunner<J> cm: taskRunners){
            cm.shutdown();
        }
        ch.end = true;
    }
    
    //TODO Some method that would work on Windows.
    /**
     * Tries to start a ClusterStub on each of the remote machines given.
     * Assumes the user name to be used is the name of the current user, and that
     * the classpath and directory to be used are the same as this VM.
     * Creates a command that calls ssh on the local machine, that should then 
     * start the ClusterStub on the remote machine. Unlikely to work on Windows.
     * @param machineNames The machines to try and start ClusterStubs running on.
     */
    public void startRemote(String[] machineNames){
        while(ch.socket==null){
            try{
                Thread.sleep(10);
            }
            catch(InterruptedException e){
            }
        }
        String classPath = System.getProperty("java.class.path");
        String hostname;
        try{
            hostname = InetAddress.getLocalHost().getHostName();
        }
        catch(UnknownHostException e){
            e.printStackTrace();
            return;
        }
        String socket = ""+ch.socket.getLocalPort();
		String launcher = "java -cp " + classPath + " " + taskdispatcher.cluster.ClusterStub.class.getCanonicalName() + " " + hostname + " " + socket;
        String uname = System.getProperty("user.name");
        String pwd = System.getProperty("user.dir");
        
        for(String mn: machineNames){
            List<String> cmdlist = new ArrayList<>(5);
            cmdlist.add("ssh");
            cmdlist.add("-l");
            cmdlist.add(uname);
            cmdlist.add(mn);
            cmdlist.add(" cd " + pwd + " ; " + launcher + " ");
            try{
                ProcessBuilder pb = new ProcessBuilder();
                pb.command(cmdlist);
                /*Process exec =*/ pb.start();
            }
            catch(IOException e){
                System.out.println("Failed to launch on " + mn);
            }
        }
    }
     
    /**
     * A thread that listens for incoming connections and creates ClusterTaskRunners
     * to fulfil match those machines.
     */
    private class ConnectionHandler implements Runnable {
        Thread thread;
        ServerSocket socket;
        boolean end = false;
        int port;
        ClusterDispatcher<?> cc;
        
        public ConnectionHandler(ClusterDispatcher<?> cc){
            this.cc=cc;
        }
        
        /**
         * While end is false this thread will accept incoming connections and
         * either create a new ClusterTaskRunner and add it to the dispatchers
         * list of TaskRunners, or match a TaskRunner that may have died with 
         * the incoming connection, if they belong to the same hostname.
         * If there is a View, then this Handler will also try to create a 
         * representation of the connection on it.
         * @see MachinePanel
         */
        @Override
        public void run() {
            try{
                socket = new ServerSocket(0);
                port = socket.getLocalPort();
                socket.setSoTimeout(2000);
                System.out.println(port);
				if(progress!=null)progress.setPort(port);
                outer: while(!end){
                    try{
                        Socket s = socket.accept();
                        //s.getInetAddress();
                        //See if it exists
                        for(ClusterTaskRunner<J> runner : taskRunners){
                                if(!runner.isAlive()&&runner.getHostName().equals(s.getInetAddress().getHostName())){
                                    runner.setUP(s);
                                    Thread t = new Thread(runner);
                                    t.start();
                                    continue outer;
                                }

                        }
                        //Otherwise, make a new runner
						ClusterTaskRunner<J> cm = new ClusterTaskRunner<>(cc);
                        cm.setUP(s);
                        taskRunners.add(cm);
						if(progress!=null){
							cm.monitor = progress.addRemoteHost(cm.getHostName());
						}
                        new Thread(cm).start();
                        System.out.println("New Machine Connected");
                    }
                    catch(SocketTimeoutException e){
                        //This will be thrown by the connect method
                        //We want this timeout so we can periodically check the
                        //end flag so that we know to terminate this thread.
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                socket.close();
            }
            catch(Exception e){
                
            }
        }
        
    }
    
}
