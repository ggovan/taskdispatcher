package taskdispatcher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An abstract class providing base functionality for other classes that wish to
 * be used as a TaskRunner.
 *
 * @author gg32
 * @param <J> The type of Job to be run.
 */
public abstract class AbstractTaskRunner <J extends Job> implements Runnable {

    /**
     * The max number of jobs that should be issued to this task runner. Usually
     * should be the number of threads that this represents on a local or remote
     * machine.
     */
    protected int maxJobs = 0;
    /**
     * Unfinished jobs that this TaskRunner may or may not be running.
     */
    protected ConcurrentHashMap<String, J> jobs = new ConcurrentHashMap<>();
    /**
     * Jobs ( and their results ) that have been completed.
     */
    protected Map<String, J> finishedJobs = new ConcurrentHashMap<>();
    /**
     * The dispatcher that owns this TaskRunner
     */
    protected AbstractTaskDispatcher<?,?> dispatcher;
    private boolean alive;

    /**
     * Hook this instance to the TaskDispatcher that it will be receiving work
     * from.
     * @param atd The calling AbstractTaskDispatcher.
     */
    public AbstractTaskRunner(AbstractTaskDispatcher<?,?> atd) {
        dispatcher = atd;
    }

    /**
     * Release all resources. There is no more need of this TaskRunner. It is
     * safe to assume that no further methods will be called on it.
     */
    public abstract void shutdown();

    /**
     * Add a task to the workload of this TaskRunner.
     *
     * @param job The job to be added
     */
    public abstract void addTask(J job);

    /**
     * Report the failure of a job. This failure is usually called by an error
     * in one of the property files. Removes the failing job from the collection
     * of jobs to complete and complain to the dispatcher.
     * @param jobID The ID of the job that has failed.
     * @param error The cause of this error.
     */
    protected void jobFailed(String jobID, String error) {
        System.out.println("JobFailed\t" + error);
        if (jobs.contains(jobID)) {
            jobs.remove(jobID);
        }
        dispatcher.jobFailed(jobID, error);
    }

    /**
     * Set the liveness of this TaskRunner.
     * Causes of un-live-ness should include the processing element(s) that this
     * runner represents no longer being active. Or that the dispatcher is
     * closing down. If set to false, then the thread running in this Runner
     * should end.
     * @param _alive The state this thread should be in.
     */
    public void setAlive(boolean _alive) {
        alive = _alive;
    }

    /**
     * Is this thread running and is the processing element(s) it represents
     * avaliable.
     * @return If this runner is usable.
     */
    public boolean isAlive() {
        return alive;
    }
}
