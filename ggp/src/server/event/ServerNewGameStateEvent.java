package server.event;

import util.observer.Event;
import util.statemachine.prover.ProverMachineState;

public final class ServerNewGameStateEvent extends Event
{

	private final ProverMachineState state;

	public ServerNewGameStateEvent(ProverMachineState state)
	{
		this.state = state;
	}

	public ProverMachineState getState()
	{
		return state;
	}

}