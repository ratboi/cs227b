package server.event;

import util.observer.Event;
import util.statemachine.Move;
import util.statemachine.Role;

public final class ServerIllegalMoveEvent extends Event
{

	private final Move move;
	private final Role role;

	public ServerIllegalMoveEvent(Role role, Move move)
	{
		this.role = role;
		this.move = move;
	}

	public Move getMove()
	{
		return move;
	}

	public Role getRole()
	{
		return role;
	}

}
