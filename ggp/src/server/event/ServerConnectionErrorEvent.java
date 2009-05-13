package server.event;

import util.observer.Event;
import util.statemachine.Role;

public final class ServerConnectionErrorEvent extends Event
{

	private final Role role;

	public ServerConnectionErrorEvent(Role role)
	{
		this.role = role;
	}

	public Role getRole()
	{
		return role;
	}

}
