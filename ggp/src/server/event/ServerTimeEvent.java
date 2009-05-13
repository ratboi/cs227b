package server.event;

import util.observer.Event;

public final class ServerTimeEvent extends Event
{

	private final long time;

	public ServerTimeEvent(long time)
	{
		this.time = time;
	}

	public long getTime()
	{
		return time;
	}

}
