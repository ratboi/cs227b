package apps.dashboard;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import player.gamer.Gamer;
import util.gdl.grammar.Gdl;
import util.reflection.ProjectSearcher;
import util.statemachine.Role;
import util.statemachine.StateMachine;
import util.statemachine.prover.ProverStateMachine;

/**
 * 
 * DashboardPanel manages the GUI for the dashboard application.
 * 
 * @author Sam Schreiber
 * @version 0.100 (Beta)
 *
 */
@SuppressWarnings("serial")
public final class DashboardPanel extends JPanel implements ActionListener {
    /*
     * When you click the "Run Dashboard" button, it queues up the following
     * number of games of a particular event type to be run one after another.
     */
    private static final int NUMBER_OF_REPEATED_GAMES = 100;
    
    private static void createAndShowGUI(DashboardPanel playerPanel) {
        JFrame frame = new JFrame("General Gaming Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setPreferredSize(new Dimension(1024, 768));
        frame.getContentPane().add(playerPanel);

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Unable to set native look and feel.");
            // e.printStackTrace();
        }

        final DashboardPanel dashboardPanel = new DashboardPanel();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(dashboardPanel);
            }
        });
    }

    private final JButton createButton;
    private final DashboardEventsPanel eventsPanel;

    private final JComboBox eventComboBox;

    private List<DashboardEvent> dashboardEvents = loadTourneyEvents();

    private List<Class<?>> gamers = ProjectSearcher.getAllClassesThatAre(Gamer.class);

    private List<JComboBox> playerBoxes;
    private JPanel playerBoxesPanel;

    private JComboBox getPlayerComboBox() {
        JComboBox newBox = new JComboBox();

        for (Class<?> gamer : gamers) {
            Gamer g;
            try {
                g = (Gamer) gamer.newInstance();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            newBox.addItem(g.getName());
        }	

        return newBox;
    }

    public DashboardPanel() {
        super(new GridBagLayout());

        playerBoxes = new ArrayList<JComboBox>();
        playerBoxesPanel = new JPanel(new GridBagLayout());

        eventComboBox = new JComboBox();
        eventComboBox.addActionListener(this);
        for(DashboardEvent de : dashboardEvents) {
            eventComboBox.addItem(de.eventName);
        }

        createButton = new JButton(createButtonMethod());
        eventsPanel = new DashboardEventsPanel();

        JPanel managerPanel = new JPanel(new GridBagLayout());

        managerPanel.setBorder(new TitledBorder("Manager"));

        JPanel buttonPanel = new JPanel(new FlowLayout());

        managerPanel.add(new JLabel("Event:"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
        managerPanel.add(eventComboBox, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
        managerPanel.add(playerBoxesPanel, new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));        
        managerPanel.add(buttonPanel, new GridBagConstraints(1, 3, 1, 1, 1.0, 1.0, GridBagConstraints.SOUTH,
                GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

        buttonPanel.add(createButton);

        JPanel gamesPanel = new JPanel(new GridBagLayout());
        gamesPanel.setBorder(new TitledBorder("Dashboard Games"));

        gamesPanel.add(eventsPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));

        this.add(managerPanel, new GridBagConstraints(0, 0, 1, 2, 0.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
        this.add(gamesPanel, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));

        actionPerformed(null);
    }

    private List<DashboardEvent> loadTourneyEvents() {
        List<DashboardEvent> theList = new ArrayList<DashboardEvent>();

        theList.add(new DashboardEvent("One Step", "onestep", 0, 10, 5));
        theList.add(new DashboardEvent("Pearls", "pearls", 0, 10, 5));
        theList.add(new DashboardEvent("Survival", "survival", 0, 10, 5));
        theList.add(new DashboardEvent("Haystack", "haystack", 0, 10, 5));
        theList.add(new DashboardEvent("Tic Tac Toe", "tictactoe", 0, 15, 10));
        theList.add(new DashboardEvent("Tic Tac Toe (Rapid)", "tictactoe", 0, 5, 2));
        theList.add(new DashboardEvent("Tic Tac Toe (2nd Player)", "tictactoe", 1, 15, 10));
        theList.add(new DashboardEvent("Tic Tac Toe (Large)", "ticTacToeLarge", 1, 15, 10));
        theList.add(new DashboardEvent("Tic Tac Toe (9 Board)", "nineBoardTicTacToe", 0, 15, 10));        
        theList.add(new DashboardEvent("Connect Four", "connectFour", 0, 15, 10));
        theList.add(new DashboardEvent("Connect Four (Rapid)", "connectFour", 0, 5, 1));
        theList.add(new DashboardEvent("Checkers", "checkers", 0, 15, 10));
        theList.add(new DashboardEvent("Checkers (Torus)", "checkersTorus", 0, 15, 10));        
        theList.add(new DashboardEvent("Chess", "chess", 0, 60, 30));
        theList.add(new DashboardEvent("Chess (Mini)", "minichess", 0, 10, 4));
        theList.add(new DashboardEvent("Othello", "othello", 0, 30, 10));
        theList.add(new DashboardEvent("Snake", "snake2p", 0, 15, 10));
        theList.add(new DashboardEvent("Quarto", "quarto", 0, 60, 30));

        return theList;
    }

    private DashboardEvent getEvent(String name) {
        for(DashboardEvent de : dashboardEvents) {
            if(de.eventName == eventComboBox.getSelectedItem()) {
                return de;
            }
        }
        return null;
    }

    private void runTourney() {
        try {
            List<Class<?>> thePlayers = new ArrayList<Class<?>>();
            for(int i = 0; i < playerBoxes.size(); i++) {
                thePlayers.add(gamers.get(playerBoxes.get(i).getSelectedIndex()));
            }

            String type = eventComboBox.getSelectedItem().toString();
            DashboardEvent theEvent = getEvent(type);

            List<DashboardEvent> theList = new ArrayList<DashboardEvent>();
            for(int i = 0; i < NUMBER_OF_REPEATED_GAMES; i++) {
                theList.add(theEvent);
            }

            DashboardManager theManager = new DashboardManager(theList, thePlayers, eventsPanel);
            theManager.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AbstractAction createButtonMethod() {
        return new AbstractAction("Run Dashboard") {
            public void actionPerformed(ActionEvent evt) {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        runTourney();
                    }
                });
            }
        };
    }

    public void actionPerformed(ActionEvent e) {
        if(e == null || e.getSource() == eventComboBox) {	    
            DashboardEvent theEvent = getEvent(eventComboBox.getSelectedItem().toString());
            List<Gdl> gameDescription = theEvent.gameDescription;

            StateMachine stateMachine = new ProverStateMachine();
            stateMachine.intialize(gameDescription);
            List<Role> roles = stateMachine.getRoles();
            int nRoles = roles.size();

            while(playerBoxes.size() > nRoles) {
                playerBoxes.remove(playerBoxes.size()-1);	    
            }

            while(playerBoxes.size() < nRoles) {
                playerBoxes.add(getPlayerComboBox());
            }

            List<Integer> currentSelections = new ArrayList<Integer>();
            for(int i = 0; i < playerBoxes.size(); i++) {
                currentSelections.add(playerBoxes.get(i).getSelectedIndex());
            }

            playerBoxesPanel.removeAll();
            for(int i = 0; i < roles.size(); i++) {
                playerBoxesPanel.add(new JLabel("Player " + i + " Type:"), new GridBagConstraints(0, i, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                        GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
                playerBoxesPanel.add(playerBoxes.get(i), new GridBagConstraints(1, i, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
            }

            for(int i = 0; i < playerBoxes.size(); i++) {
                playerBoxes.get(i).setSelectedIndex(currentSelections.get(i));
            }	    

            playerBoxesPanel.validate();
        }	
    }  
}