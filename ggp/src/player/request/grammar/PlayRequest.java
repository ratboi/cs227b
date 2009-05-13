package player.request.grammar;

import java.util.List;

import player.event.PlayerTimeEvent;
import player.gamer.Gamer;
import player.gamer.event.GamerUnrecognizedMatchEvent;
import util.gdl.grammar.GdlSentence;

public final class PlayRequest extends Request
{

	private final Gamer gamer;
	private final String matchId;
	private final List<GdlSentence> moves;

	public PlayRequest(Gamer gamer, String matchId, List<GdlSentence> moves)
	{
		this.gamer = gamer;
		this.matchId = matchId;
		this.moves = moves;
	}

	@Override
	public String process()
	{
		if (!gamer.getMatch().getMatchId().equals(matchId))
		{
			gamer.notifyObservers(new GamerUnrecognizedMatchEvent(matchId));
			return "nil";
		}

		if (moves != null)
		{
			gamer.getMatch().appendMoves(moves);
		}

		try
		{
			gamer.notifyObservers(new PlayerTimeEvent(gamer.getMatch().getPlayClock() * 1000));
			return gamer.selectMove(gamer.getMatch().getPlayClock() * 1000 + System.currentTimeMillis()).toString();
		}
		catch (Exception e)
		{
			return "nil";
		}
	}

	@Override
	public String toString()
	{
		return "play";
	}

}
