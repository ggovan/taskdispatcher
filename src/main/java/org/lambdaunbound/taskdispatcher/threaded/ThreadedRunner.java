package org.lambdaunbound.taskdispatcher.threaded;

import org.lambdaunbound.taskdispatcher.AbstractTaskDispatcher;
import org.lambdaunbound.taskdispatcher.AbstractTaskRunner;
import org.lambdaunbound.taskdispatcher.Job;

/**
 * An implementation of AbstractTaskRunner to be used by the ThreadedDispatcher.
 * @param <J> The type of Job to be run.
 * @author gg32
 */
public class ThreadedRunner<J extends Job> extends AbstractTaskRunner<J> {
    
    private boolean live = true;
    Thread thread;
    
	/**
	 * Create an instance of a ThreadedRunner to perform work for the given dispatcher.
	 * @param atd The dispatcher using this Runner.
	 */
	public ThreadedRunner(AbstractTaskDispatcher<?,J> atd){
        super(atd);
    }
    
    /**
     * Set the max jobs of this Runner to 1.
     */
    public void setUp(){
        //A thread can only do one thing at a time.
        maxJobs = 1;
    }

    @Override
    public void shutdown() {
        live = false;
        thread.interrupt();
    }

    /**
     * Add and start the current job. The ThreadRunner will be interrupted to
     * start this job. It should not currently be running another job at the time.
     * If it is then unexpected behaviour may occur.
     * @param job The job to be added.
     * @see AbstractTaskRunner#addTask(taskdispatcher.Job) 
     */
    @Override
    public void addTask(J job) {
        //System.out.println("Recieved Job, sending it out");
        jobs.put(job.getID(),job);
        thread.interrupt();
    }
    
    /**
     * Keep running and executing jobs in the jobs collection.
     * @see AbstractTaskRunner#run() 
     */
    @Override
    public void run() {
        setAlive(true);
        while(live){
            if(!jobs.isEmpty()){
                String jobID=null;
                for(String jid : jobs.keySet()){
                    jobID = jid;
                    break;
                }
                
				J job = jobs.get(jobID);

                job.run();
                finishedJob(jobID,job);

            }
            synchronized(this){
                if(live&&!thread.isInterrupted()){
                    try{
                        wait(5000);
                    }
                    catch(InterruptedException e){
                    }
                }
            }
        }
        setAlive(false);
    }
    
    /**
     * Read in a finished job and report this back to the dispatcher.
	 * @param jobID The ID of the finished Job,
	 * @param job The finished Job.
     */
    private void finishedJob(String jobID,J job) {
        jobs.remove(jobID);
        finishedJobs.put(jobID, job);
        dispatcher.jobCompleted(jobID);
    }
    
}
