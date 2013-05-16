package org.lambdaunbound.taskdispatcher;

import java.io.Serializable;

/**
 * An interface for Jobs to run through a Dispatcher. Each job must have a unique ID.
 * Jobs are able to be run by both the Threaded and Cluster Dispatchers.
 *
 * When used by the threaded dispatcher, care must be taken for the synchronisation
 * of objects. Ideally there should be no objects shared between Jobs that are changed.
 *
 * Then used by the clustered dispatcher the Job will be serialised and
 * reconstructed on the remote machine. Here you must be careful not to reference
 * data that will not be needed. Once a Job has reached the end of its run method
 * it would be wise to null all the fields which will not be needed back on the
 * dispatching machine.
 *
 * @author gg32
 */
public interface Job extends Runnable, Serializable{


	/**
	 * Set the ID of this job.
	 * @param ID The ID for this job.
	 */
	public void setID(String ID);

	/**
	 * Get this Job's ID.
	 * @return The ID of this Job
	 */
	public String getID();

	/**
	 * Performs the execution of this job.
	 * @see Runnable#run() 
	 */
	public void run();

}
