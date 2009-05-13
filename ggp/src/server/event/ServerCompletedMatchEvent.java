package server.event;

import java.util.List;

import util.observer.Event;

public final class ServerCompletedMatchEvent extends Event
{

	private final List<Integer> goals;

	public ServerCompletedMatchEvent(List<Integer> goals)
	{
		this.goals = goals;
	}

	public List<Integer> getGoals()
	{
		return goals;
	}

}
