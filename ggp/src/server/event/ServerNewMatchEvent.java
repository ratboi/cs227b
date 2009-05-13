package server.event;

import java.util.List;

import util.observer.Event;
import util.statemachine.Role;

public final class ServerNewMatchEvent extends Event
{

	private final List<Role> roles;

	public ServerNewMatchEvent(List<Role> roles)
	{
		this.roles = roles;
	}

	public List<Role> getRoles()
	{
		return roles;
	}

}
