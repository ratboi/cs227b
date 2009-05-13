package player.gamer;

import java.util.ArrayList;
import java.util.List;

import player.gamer.exception.MetaGamingException;
import player.gamer.exception.MoveSelectionException;
import util.gdl.grammar.GdlProposition;
import util.gdl.grammar.GdlSentence;
import util.match.Match;
import util.observer.Event;
import util.observer.Observer;
import util.observer.Subject;
import util.statemachine.StateMachine;
import apps.player.config.ConfigPanel;
import apps.player.config.EmptyConfigPanel;
import apps.player.detail.DetailPanel;
import apps.player.detail.EmptyDetailPanel;

/**
 * The Gamer class defines methods for both meta-gaming and move selection in a
 * pre-specified amount of time. The Gamer class is based on the <i>algorithm</i>
 * design pattern.
 */
public abstract class Gamer implements Subject
{

	private Match match;
	
	private GdlProposition roleName;

	public Gamer()
	{
		observers = new ArrayList<Observer>();
		match = new Match();
	}
	
//====The meat====
	public abstract void metaGame(long timeout) throws MetaGamingException;
	
	public abstract GdlSentence selectMove(long timeout) throws MoveSelectionException;
	
//====Gamer Profile and Configuration====
	public abstract String getName();
	
	public abstract StateMachine getInitialStateMachine();
	
	public ConfigPanel getConfigPanel()
	{
		return new EmptyConfigPanel();
	}
	
	public DetailPanel getDetailPanel()
	{
		return new EmptyDetailPanel();
	}

//====Accessors====
	
	public Match getMatch()
	{
		return match;
	}
	
	public void setMatch(Match match)
	{
		this.match = match;
	}

	public GdlProposition getRoleName()
	{
		return roleName;
	}
	
	public void setRoleName(GdlProposition roleName)
	{
		this.roleName = roleName;
	}
	
//====Observer Stuff====
	private final List<Observer> observers;
	public void addObserver(Observer observer)
	{
		observers.add(observer);
	}
	
	public void notifyObservers(Event event)
	{
		for (Observer observer : observers)
		{
			observer.observe(event);
		}
	}

	

	

}
