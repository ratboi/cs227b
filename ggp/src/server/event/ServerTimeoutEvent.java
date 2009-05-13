package server.event;

import util.observer.Event;
import util.statemachine.Role;

public final class ServerTimeoutEvent extends Event
{

	private final Role role;

	public ServerTimeoutEvent(Role role)
	{
		this.role = role;
	}

	public Role getRole()
	{
		return role;
	}

}
