package taskdispatcher.threaded;

import taskdispatcher.AbstractTaskDispatcher;
import taskdispatcher.Job;

/**
 * An implementation of AbstractTaskDispatcher to run on a machine with multiple
 * processing elements. For now  it should also be used by any machine with only
 * a single PE, as there is no sequential dispatcher.
 * 
 * @param <J> The type of job to be run by this dispatcher.
 * @author gg32
 */
public class ThreadedDispatcher<J extends Job> extends AbstractTaskDispatcher<ThreadedRunner<J>,J> {
    //TODO Write a sequential dispatcher.

    /**
     * Setup ThreadedRunners, one for each processing element on this machine.
     */
    @Override
    public void setUp() {
        int numPE = Runtime.getRuntime().availableProcessors();
        for(int i=0;i<numPE;i++){
            ThreadedRunner<J> tr = new ThreadedRunner<>(this);
            tr.thread = new Thread(tr);
            tr.setUp();
            taskRunners.add(tr);
            tr.thread.start();
        }
    }

    @Override
    public void end() {
        for(ThreadedRunner<J> atr : taskRunners){
            atr.shutdown();
        }
    }
    
    @Override
    public void addJob(J job){
        super.addJob(job);
    }
    
}
