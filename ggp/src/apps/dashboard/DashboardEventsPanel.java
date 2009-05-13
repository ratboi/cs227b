package apps.dashboard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import player.gamer.event.GamerCompletedMatchEvent;
import player.gamer.event.GamerNewMatchEvent;
import util.observer.Event;
import util.observer.Observer;
import apps.common.table.JZebraTable;

/**
 * 
 * DashboardEventsPanel is responsible for displaying information
 * about the matches that dashboard has played (and is currently
 * playing). It observes the currently running match, and renders
 * its information onto a JZebraTable. 
 * 
 * @author Sam Schreiber
 * @version 0.100 (Beta)
 *
 */
@SuppressWarnings("serial")
public final class DashboardEventsPanel extends JPanel implements Observer
{
    private DashboardEvent currentEvent;
    private final JZebraTable matchTable;
    private List<Integer> theScores = new ArrayList<Integer>();

    public void setCurrentEvent(DashboardEvent currentEvent) {
        this.currentEvent = currentEvent;
    }

    public DashboardEventsPanel()
    {
        super(new GridBagLayout());

        DefaultTableModel model = new DefaultTableModel();		
        model.addColumn("Event");
        model.addColumn("Player");
        model.addColumn("Clocks");
        model.addColumn("Status");
        model.addColumn("Goal Summary");
        model.addColumn("PC");
        model.addColumn("TO");
        model.addColumn("IM");
        model.addColumn("CE");

        matchTable = new JZebraTable(model)
        {

            @Override
            public boolean isCellEditable(int rowIndex, int colIndex)
            {
                return false;
            }
        };
        matchTable.setShowHorizontalLines(true);
        matchTable.setShowVerticalLines(true);

        setColumnWidths();
        model.addRow(new String[] { "Wins: ", "Losses: ", "Ties: ", "", "", "", "", "", "" });

        this.add(new JScrollPane(matchTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));		
    }

    public void observe(Event event)
    {
        if (event instanceof GamerCompletedMatchEvent)
        {
            observe((GamerCompletedMatchEvent) event);
        }
        else if (event instanceof GamerNewMatchEvent)
        {
            observe((GamerNewMatchEvent) event);
        }
    }

    private void setColumnWidths() {
        matchTable.getColumnModel().getColumn(5).setPreferredWidth(1);
        matchTable.getColumnModel().getColumn(6).setPreferredWidth(1);
        matchTable.getColumnModel().getColumn(7).setPreferredWidth(1);
        matchTable.getColumnModel().getColumn(8).setPreferredWidth(1);		
    }

    private void observe(GamerCompletedMatchEvent event)
    {
        DefaultTableModel model = (DefaultTableModel) matchTable.getModel();
        model.setValueAt(currentEvent.eventName, model.getRowCount() - 1, 0);
        model.setValueAt(currentEvent.playerName, model.getRowCount() - 1, 1);		
        model.setValueAt("(" + currentEvent.startClock + ", " + currentEvent.playClock + ")", model.getRowCount() - 1, 2);		
        model.setValueAt(currentEvent.theStatus, model.getRowCount() - 1, 3);
        model.setValueAt(currentEvent.getGoalString(), model.getRowCount() - 1, 4);
        model.setValueAt(currentEvent.moveCount, model.getRowCount() - 1, 5);
        model.setValueAt(currentEvent.errorCount_Timeouts, model.getRowCount() - 1, 6);
        model.setValueAt(currentEvent.errorCount_IllegalMoves, model.getRowCount() - 1, 7);
        model.setValueAt(currentEvent.errorCount_ConnectionErrors, model.getRowCount() - 1, 8);

        setColumnWidths();
    }

    private void observe(GamerNewMatchEvent event)
    {
        DefaultTableModel model = (DefaultTableModel) matchTable.getModel();
        model.addRow(new String[] { "", "", "", "", "", "", "", "", "" });

        setColumnWidths();
    }

    public void recordScore(int myScore) {
        theScores.add(myScore);

        int wins = 0;
        int losses = 0;
        int ties = 0;
        for(int i : theScores) {
            if(i >= 75) wins++;
            else if(i <= 25) losses++;
            else ties++;
        }

        // (The distinction here is completely arbitrary, but seems to work for most games)        
        DefaultTableModel model = (DefaultTableModel) matchTable.getModel();	    	    
        model.setValueAt("Wins: " + wins, 0, 0);
        model.setValueAt("Losses: " + losses, 0, 1);		
        model.setValueAt("Ties: " + ties, 0, 2);			    
    }

}
