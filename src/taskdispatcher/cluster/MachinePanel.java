package taskdispatcher.cluster;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A panel that displays the details of a machine that we are connected to on 
 * a cluster. Currently displays the machine name, and the number of jobs assigned
 * and completed. The colour of the panel represents the machine status, green-
 * connected, red- unconnected.
 * @author gg32
 */
public class MachinePanel extends JPanel{
    //TODO Add a button to attempt reconnection is the machine goes down.
    //TODO Add a 'blank' mode to allow the user to enter the details of a machine
    //that we'll then try to connect to.
    
    private static String COMPLETED = "Jobs completed: ";
    private static String ASSIGNED = "Jobs assigned: ";
    
    //View, to be updated from events.
    private JLabel machineNameLabel;
    private JLabel jobsCompletedLabel;
    private JLabel jobsAssignedLabel;
    
    private boolean alive = false;
    private int completed = 0;
    private int assigned = 0;
    
    /**
     * Create and layout a new machine panel.
     * @param machineName The name of the machine that this panel represents 
     * a connection to.
     */
    public MachinePanel(String machineName){
        machineNameLabel = new JLabel(machineName);
        jobsAssignedLabel = new JLabel(ASSIGNED + 0);
        jobsCompletedLabel = new JLabel(COMPLETED + 0);

        setBackground(Color.red);
        
        super.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx=1;
        c.insets= new Insets(3, 3, 3, 3);
        c.ipady = c.ipadx=1;
        c.fill=GridBagConstraints.HORIZONTAL;
        super.add(machineNameLabel,c);
        c.gridy=1;
        super.add(jobsAssignedLabel,c);
        c.gridy=2;
        super.add(jobsCompletedLabel,c);
    }
    
    /**
     * Set whether the connection to the machine is live or not, changes the
     * background colour of the panel to match.
     * @param live 
     */
    public void setAlive(boolean live){
        alive = live;
        if(alive){
            super.setBackground(Color.GREEN);
        }
        else{
            super.setBackground(Color.RED);
        }
        repaint();
    }
    
    /**
     * Increment the number of assigned jobs and redraw.
     */
    public void assignedJob(){
        assigned++;
        jobsAssignedLabel.setText(ASSIGNED + assigned);
        repaint();
    }
    
    /**
     * Increment the number of finshedJobs and redraw.
     */
    public void finishJob(){
        completed++;
        assigned--;
        jobsCompletedLabel.setText(COMPLETED + completed);
        jobsAssignedLabel.setText(ASSIGNED + assigned);
        repaint();
    }
    
   
}
