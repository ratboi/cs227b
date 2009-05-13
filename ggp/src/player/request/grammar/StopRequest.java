package player.request.grammar;

import player.gamer.Gamer;
import player.gamer.event.GamerCompletedMatchEvent;
import player.gamer.event.GamerUnrecognizedMatchEvent;

public final class StopRequest extends Request
{

	private final Gamer gamer;
	private final String matchId;

	public StopRequest(Gamer gamer, String matchId)
	{
		this.gamer = gamer;
		this.matchId = matchId;
	}

	@Override
	public String process()
	{
		if (!gamer.getMatch().getMatchId().equals(matchId))
		{
			gamer.notifyObservers(new GamerUnrecognizedMatchEvent(matchId));
			return "(not done)";
		}

		gamer.notifyObservers(new GamerCompletedMatchEvent());
		return "done";
	}

	@Override
	public String toString()
	{
		return "stop";
	}

}
