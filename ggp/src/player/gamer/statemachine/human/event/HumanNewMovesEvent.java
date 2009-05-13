package player.gamer.statemachine.human.event;

import java.util.List;

import util.observer.Event;
import util.statemachine.Move;

public final class HumanNewMovesEvent extends Event
{

	private final List<Move> moves;
	private final Move selection;

	public HumanNewMovesEvent(List<Move> moves, Move selection)
	{
		this.moves = moves;
		this.selection = selection;
	}

	public List<Move> getMoves()
	{
		return moves;
	}

	public Move getSelection()
	{
		return selection;
	}

}
