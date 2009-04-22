package apps.server.visualization;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import server.event.ServerCompletedMatchEvent;
import server.event.ServerNewGameStateEvent;
import server.event.ServerNewMatchEvent;
import server.event.ServerNewMovesEvent;
import server.event.ServerTimeEvent;
import util.observer.Event;
import util.observer.Observer;
import util.statemachine.prover.ProverMachineState;
import util.xhtml.GameStateRenderPanel;

@SuppressWarnings("serial")
public final class VisualizationPanel extends JPanel implements Observer
{
	//private final JTimerBar timerBar;
	
	private final String gameName;
	
	private List<JPanel> gameStatePanels = new ArrayList<JPanel>();
	private int index = 0;
	private JTabbedPane tabs = new JTabbedPane();

	public VisualizationPanel(String gameName)
	{		
		//super(new GridBagLayout());
		
		this.gameName = gameName;
		
		this.add(tabs);
		//this.add(tabs, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		
		//timerBar = new JTimerBar();
		//this.add(timerBar, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
	}

	public void observe(Event event)
	{
		if (event instanceof ServerNewMatchEvent)
		{
			observe((ServerNewMatchEvent) event);
		}
		else if (event instanceof ServerNewMovesEvent)
		{
			observe((ServerNewMovesEvent) event);
		}
		else if (event instanceof ServerCompletedMatchEvent)
		{
			observe((ServerCompletedMatchEvent) event);
		}
		else if (event instanceof ServerTimeEvent)
		{
			observe((ServerTimeEvent) event);
		}
		else if (event instanceof ServerNewGameStateEvent)
		{
			observe((ServerNewGameStateEvent)event);
		}
	}

	private void observe(ServerCompletedMatchEvent event)
	{
	}

	private void observe(ServerNewMatchEvent event)
	{
	}

	private void observe(ServerNewMovesEvent event)
	{
		
	}
	
	private void observe(ServerNewGameStateEvent event)
	{
		boolean atEnd = index == gameStatePanels.size()-1;		
		ProverMachineState s = event.getState();
		
		try
		{
			String XML = s.toXML();
			String XSL = GameStateRenderPanel.getXSLfromFile(gameName+".xsl", 1); //1 because machinestate XMLs only ever have 1 state
			JPanel newPanel = GameStateRenderPanel.getPanelfromGameXML(XML, XSL);
			
			if(gameStatePanels.size() > 0 && atEnd)
			{
				this.remove(gameStatePanels.get(index));
				index++;			
			}		
			
			if(atEnd)
			{
				tabs.add(new Integer(gameStatePanels.size()).toString(),newPanel);
				tabs.setSelectedIndex(tabs.getComponentCount()-1);
			}
			
			gameStatePanels.add(newPanel);
		} catch(Exception ex) {
			System.err.println("Visualization failed for: "+gameName);
		}
	}

	private void observe(ServerTimeEvent event)
	{
		//timerBar.time(event.getTime(), 500);
	}
}
