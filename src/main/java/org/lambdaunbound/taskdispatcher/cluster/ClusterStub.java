package org.lambdaunbound.taskdispatcher.cluster;

import org.lambdaunbound.taskdispatcher.Job;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Create the process that runs on a remote machine on a cluster and listens and
 * executes incoming jobs.
 * 
 * There are three type of thread. The main thread listens for incoming messages,
 * it then sparks off Job threads as necessary. Job threads run
 * the jobs. The third thread is the sender thread, it is the only thread that
 * sends communications back to the Runner, this is so that no messages are ever
 * mixed in with others.
 * @author gg32
 */
public class ClusterStub {

    
    private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
    private ExecutorService threadpool;
    private ConcurrentLinkedQueue<ArrayList<Object>> outMessages;
    private MessageSender messageSender;
    
    /**
     * Creates a new ClusterStub if the hostname and port number are given as args.
     * @param args 
     */
    public static void main(String[] args){
        if(args.length!=2){
            System.err.println("Incorrect Invocation\nEvoClusterStub requires a hostname and socket number");
            return;
        }
        ClusterStub ecs = new ClusterStub();
        ecs.setSocket(args[0], Integer.parseInt(args[1]));
        ecs.start();
    }
    
    /**
     * Start listening for incoming messages, and spark off jobs on a new thread.
     * @return if this thread exited safely or not.
     */
    public boolean start(){
        Object message;
        boolean safe = false;
        System.out.println("Starting to listen:");
        outer: while((message=getInput())!=null){
            ClusterCommunicationTypes mType = null;
			if(message instanceof ClusterCommunicationTypes){
				mType = (ClusterCommunicationTypes)message;
			}
            switch(mType){
                case START_UP:
                case END_TRANSMISSION:{
                    throw new RuntimeException(message + " should never be sent to the stub");
                }
                case KEEP_ALIVE:{
                    send(mType);
                    break;
                }
				case NEW_JOB:{
					Object o = getInput();
					if(o instanceof Job){
						final Job job = (Job)o;
						Runnable r = new Runnable() {
							@Override
							public void run() {
								job.run();
								finished(job);
							}
						};
						threadpool.submit(r);
					}
					else{
						throw new ClassCastException("Not A Job!");
					}
                    break;
                }
                case FINISHED:{
                    send(ClusterCommunicationTypes.FINISHED);
                    safe = true;
                    break outer;
                }
			case FINISHED_JOB:
			case FINISHED_SEED:
			case JOB_FAILED:
				throw new Error("This message should not have been sent to the runner!\n" + mType);
            }
        }
        try{
            in.close();
            out.flush();
            out.close();
        }
        catch(IOException e){
            System.out.println("Gone wrong");
			System.exit(0);
        }
        finally{
            System.out.println("Closing down");
            threadpool.shutdownNow();
            messageSender.live=false;
            messageSender.thread.interrupt();
        }
		return safe;
    }
    
    /**
     * Adds the given message list to a the queue of messages to be sent.
     * @param message The list of messages to send.
     */
    private void send(ArrayList<Object> message){
        outMessages.add(message);
        messageSender.thread.interrupt();
    }
    
    /**
     * Add a message to the queue of messages to be sent.
     * @param message The message to send
     */
    private void send(Object message){
        ArrayList<Object> arr = new ArrayList<>(1);
        arr.add(message);
        send(arr);
    }
    
    /**
     * Reads in one Object from the socket.
     * @return One Object from the socket.
     */
    private Object getInput(){
        try{
            return in.readObject();
        }
        catch(Exception e){
            return null;
        }
    }
    
    /**
     * Set up this ClusterStub to connect to the given address. Will also setup
     * the MessageSender thread and then report back the number of processing 
     * elements that this machine has.
     * Also sets up the ThreadPool for the Jobs to be run in.
     * 
     * @param hostname The hostname to connect to.
     * @param socketnum The port to connect to on the hostname.
     * @return If everything went okay or not.
     */
    public boolean setSocket(String hostname, int socketnum){
        try{
            //Socket and connections
            socket = new Socket(hostname, socketnum);
			out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject("Need to send an object to make sure it all works");
			in = new ObjectInputStream(socket.getInputStream());
            //Setup the thread to send messages
            //(the main thread will be listening)
            outMessages = new ConcurrentLinkedQueue<>();
            messageSender = new MessageSender();
            messageSender.thread = new Thread(messageSender);
            messageSender.thread.start();
            
            //The number of threads we should use
            int processingElements = Runtime.getRuntime().availableProcessors();
            //if(processingElements>2)processingElements--;
            threadpool = Executors.newFixedThreadPool(processingElements);
            
            //We're good to start, send the startup message to the master
            ArrayList<Object> message = new ArrayList<>(2);
            message.add(ClusterCommunicationTypes.START_UP);
            message.add(Integer.valueOf(processingElements));
            send(message);
            
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            System.err.println("Unable to set up socket: " + hostname +":"+socketnum);
            return false;
        }
    }

    private void finished(Job job){
        ArrayList<Object> outStats = new ArrayList<>(2);
        outStats.add(ClusterCommunicationTypes.FINISHED_JOB);
        outStats.add(job);
        send(outStats);
   }
    
    
    /**
     * This is the only thread that can send messages back to the Runner. Other
     * threads should add messages to the message queue then interrupt this thread.
     */
    private class MessageSender implements Runnable{
        boolean live = true;
        Thread thread;
        
        /**
         * Keep running and sending back messages from the messageQueue until
         * live is set to false.
         */
        @Override
        public synchronized void run(){
			try{
				while(live){
	                if(outMessages.peek()!=null){
	                    for(Object message : outMessages.poll()){
	                        sendMessage(message);
	                    }
	                }
	                try{
	                    wait(5000);
	                }
		            catch(InterruptedException e){}
	            }
			}
			catch(IOException e){
				e.printStackTrace();
			}
        }
        
        /**
         * Send message.
         * @param message Message to send.
         * @return True.
         */
        private boolean sendMessage(Object message)throws IOException{
            out.writeObject(message);
			out.flush();
            return true;
        }
    }
    
}
