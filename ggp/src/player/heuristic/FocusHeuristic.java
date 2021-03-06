package player.heuristic;

import util.statemachine.MachineState;
import util.statemachine.Role;
import util.statemachine.StateMachine;
import util.statemachine.exceptions.MoveDefinitionException;

public class FocusHeuristic implements Heuristic {

	public double eval(StateMachine stateMachine, MachineState state, Role role)
			throws MoveDefinitionException {
		return MAX_SCORE/(stateMachine.getLegalMoves(state, role).size() + 1);
	}

}
