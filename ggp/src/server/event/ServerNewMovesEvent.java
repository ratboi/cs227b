package server.event;

import java.util.List;

import util.observer.Event;
import util.statemachine.Move;

public final class ServerNewMovesEvent extends Event
{

	private final List<Move> moves;

	public ServerNewMovesEvent(List<Move> moves)
	{
		this.moves = moves;
	}

	public List<Move> getMoves()
	{
		return moves;
	}

}
