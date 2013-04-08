package taskdispatcher.cluster;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A panel that will show the port that the server is listening on, and the
 * machines connected with their statuses.
 * @author gg32
 */
public class ClusterMonitorPanel extends JPanel{
    //TODO show machine panels for the machine that are listed in the properties
    //even if they have not connected yet, or ever will.
    
	private static final long serialVersionUID = -1300212134839933113L;
    private JLabel portLabel;
    private JPanel machineContainer;
    private List<MachinePanel> machinePanels = new LinkedList<MachinePanel>();
    
    /**
     * Creates and lays out a new MonitorPanel.
     */
    public ClusterMonitorPanel(){
        portLabel = new JLabel("Listening on port: -----");
        machineContainer = new JPanel(new FlowLayout(FlowLayout.LEADING));
        
        setLayout(new BorderLayout());
        
        add(portLabel,BorderLayout.NORTH);
        add(machineContainer,BorderLayout.CENTER);
        
    }
    
    /**
     * Adds a new machine panel to the layout.
     * @param mp 
     */
    public void addMachine(MachinePanel mp){
        machinePanels.add(mp);
        machineContainer.add(mp);
    }
    
    /**
     * Creates and adds a new machine panel for the given hostname.
     * @param hostname The hostname of the new machine that has connected.
     * @return The newly created MachinePanel.
     */
    public MachinePanel add(String hostname){
        MachinePanel mp = new MachinePanel(hostname);
        addMachine(mp);
        return mp;
    }
    
    /**
     * Set the port number on the label to the argument, or blank if argument is
     * -1
     * @param portNumber The new port number, or -1 if not connected,
     */
    public void setPort(int portNumber){
        if(portNumber==-1){
            portLabel.setText("Listening on port: -----");
        }
        else{
            portLabel.setText("Listening on port: " + portNumber);
        }
    }
    
    /**
     * Remove all the machine panels from display and clear the port number.
     */
    public void clearMachines(){
        machinePanels.clear();
        machineContainer.removeAll();
        setPort(-1);
    }
    
}
