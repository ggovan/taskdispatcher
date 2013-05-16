/**
 * Provides a lightweight library for distributing a set of jobs over a cluster of
 * multicore computers.
 * <p>
 * The {@link taskdispatcher.cluster.ClusterStub} allows for dynamic adding and removal of machines
 * from the pool of machines connected to it.
 * This allows for reissueing of jobs when a machine fails.
 * {@link taskdispatcher.Job}s are issued to machines on the basis of the number of free processing elements that they have.
 * <p>
 * To use this library:
 * <ul>
 * <li /> You must first create a class that implements the {@link taskdispatcher.Job} interface.
 * <li /> Then create a TaskDispatcher:
 * <ul>
 * <li />The {@link org.lambdaunbound.taskdispatcher.cluster.ClusterDispatcher} is to be used on a cluster of machines.
 * <li />The {@link org.lambdaunbound.taskdispatcher.threaded.ThreadedDispatcher} is an alternate dispatcher to be used on a single multicore machine.
 * </ul>
 * <li />Jobs are then added to the TaskDispatcher using {@link taskdispatcher.AbstractTaskDispatcher#addJob(Job)}.
 * <li />Calling {@link taskdispatcher.AbstractTaskDispatcher#start()} will cause the TaskDispatcher to serialise the jobs and issue them to the remote machines.
 * <li />The jobs will then be executed, and upon completion, serialised and sent back to dispatching machine.
 * <li />Only once all the jobs have been completed will the start method return.
 * </ul>
 * If the task dispatcher is to be used for a second set of jobs, then call the {@link taskdispatcher.AbstractTaskDispatcher#newGeneration()} method to clear the list of jobs.
 * <p>
 * Once all the work has been completed call the {@link taskdispatcher.AbstractTaskDispatcher#end()} to close any open sockets and stop any threads.
 * <p>
 * Other things of note:
 * <ul>
 *  <li /> ssh is used to start the {@link taskdispatcher.cluster.ClusterStub}s on remote machines.
 *  It should therefore be set up to use keypairs that do not require a password to be entered upon login.
 *  <li /> As it uses ssh to start the remote machines, it assumes some sort of linux base. If you are running this on a cluster of Windows machines it is possible to start the {@link taskdispatcher.cluster.ClusterStub}s by hand, or to create another tool to automate it.
 *  <li /> Be careful what you reference from you {@link taskdispatcher.Job}s, as this will all be serialised when sent across the network. Null out any fields that shouldn't be needed.
 * </ul>
 */
package org.lambdaunbound.taskdispatcher;