package player.heuristic;

import util.statemachine.MachineState;
import util.statemachine.StateMachine;
import util.statemachine.Role;
import util.statemachine.exceptions.MoveDefinitionException;

public class MobilityHeuristic implements Heuristic {

	public double eval(StateMachine stateMachine, MachineState state, Role role) throws MoveDefinitionException {
		return (double)stateMachine.getLegalMoves(state, role).size();
	}

}
