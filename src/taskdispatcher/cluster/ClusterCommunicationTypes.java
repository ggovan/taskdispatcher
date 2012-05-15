package taskdispatcher.cluster;

/**
 * The type of messages that can be sent between the ClusterTaskRunner and the
 * ClusterStub. The form of each type of message is shown below.
 * @author gg32
 */
public enum ClusterCommunicationTypes {
    /**
     * START_UP is sent from the Stub to the Runner once a channel has been opened.
     * The complete message should be START_UP followed by Integer.valueOf(number_of_processing_elements);
     */
    START_UP,
    /**
     * NEW_JOB is sent from the Runner to a Stub. The message should also 
     * include the Job to be run. The complete message should be NEW_JOB followed
	 * by Job;
     */
    NEW_JOB,
    /**
     * KEEP_ALIVE should be sent by itself (with an EOL). Either the Stub or the
     * Runner should be able to send this and the other should at all times just accept it.
     */
    KEEP_ALIVE,
    /**
     * FINISHED should be sent from the Runner to the Stub once the Stub is no
     * longer required. The Stub should then send the same back to indicate that
     * it will close cleanly.
     */
    FINISHED,
    /**
     * FINISHED_JOB is sent from the Stub to the Runner once a job has been completed.
     * The message should be FINISHED_JOB followed by the Job;
     */
    FINISHED_JOB,
    /**
     * Should only be used in conjunction with FINISHED_JOB.
     * @see ClusterCommunicationTypes#FINISHED_JOB
     */
    FINISHED_SEED,
    /**
     * Should only be used in conjunction with FINISHED_JOB and NEW_JOB.
     * @see ClusterCommunicationTypes#FINISHED_JOB
     * @see ClusterCommunicationTypes#NEW_JOB
     */
    END_TRANSMISSION,
    /**
     * JOB_FAILED should be sent from the Stub to the Runner once a job has failed.
     * This failure will usually be down to an error in the properties.
     * The message should be JOB_FAILED +jobID+error_description;
     */
    JOB_FAILED
}
