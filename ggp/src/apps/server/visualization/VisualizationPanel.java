package apps.server.visualization;

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

@SuppressWarnings("serial")
public final class VisualizationPanel extends JPanel implements Observer
{
	//private final JTimerBar timerBar;
	
	private final String gameName;
	
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
	
	private int stepCount = 1;
	private void observe(ServerNewGameStateEvent event)
	{		
		ProverMachineState s = event.getState();
		RenderThread rt = new RenderThread(gameName, s, this, stepCount++);
		rt.start();
	}
	
	public synchronized boolean addVizPanel(JPanel newPanel, Integer stepNum)
	{
		//if(tabs.getTabCount()+1 != stepNum)
			
		boolean atEnd = tabs.getSelectedIndex() == tabs.getTabCount()-1;
		try
		{
			for(int i=tabs.getTabCount(); i<stepNum; i++)
				tabs.add(new Integer(i+1).toString(), new JPanel());
			tabs.setComponentAt(stepNum-1, newPanel);
			tabs.setTitleAt(stepNum-1, stepNum.toString());
			
			if(atEnd)
			{				
				tabs.setSelectedIndex(tabs.getTabCount()-1);
			}
			
		} catch(Exception ex) {
			System.err.println("Adding rendered visualization panel failed for: "+gameName);
		}
		
		return true;
	}

	private void observe(ServerTimeEvent event)
	{
		//timerBar.time(event.getTime(), 500);
	}
}
