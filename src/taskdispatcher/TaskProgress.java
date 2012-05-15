package taskdispatcher;

import taskdispatcher.cluster.MachinePanel;

/**
 *An interface through which TaskDispatchers can update a listener of its progress.
 * 
 * @author gg32
 */
public interface TaskProgress {

    /**
     * The number of tasks that this program will tick through.
     * 
     * @param max The number of tasks to tick through.
     */
    void setLimit(int max);

    /**
     * A message to be sent to the listener. Likely to be displayed on a progress bar.
     * @param description The message to be displayed.
     */
    void message(String description);
    
    /**
     * Inform the listener that we have completed another task.
     */
    void tick();
    
    /**
     * Inform the listener that we have completed another task, and display a
     * message.
     * @param message The message to be displayed.
     */
    void tick(String message);
    
    /**
     * Inform the listener that this task has failed and why.
     * @param message A description of why this task has failed.
     */
    void fail(String message);

	/**
	 * Display the port number to be connected to.
	 * @param port The port number used by the dispatcher.
	 */
	void setPort(int port);

	/**
	 * Register that a new host been added to the current cluster,
	 * @param hostname The hostname of the machine which is newly connected.
	 * @return The MachinePanel that represents this host in a GUI, or null.
	 */
	MachinePanel addRemoteHost(String hostname);
   
}
