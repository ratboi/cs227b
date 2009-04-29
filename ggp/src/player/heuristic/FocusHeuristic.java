package player.heuristic;

import util.statemachine.MachineState;
import util.statemachine.Role;
import util.statemachine.StateMachine;
import util.statemachine.exceptions.MoveDefinitionException;

public class FocusHeuristic implements Heuristic {

	@Override
	public double eval(StateMachine stateMachine, MachineState state, Role role)
			throws MoveDefinitionException {
		return 1.0/(stateMachine.getLegalMoves(state, role).size() + 1);
	}

}
