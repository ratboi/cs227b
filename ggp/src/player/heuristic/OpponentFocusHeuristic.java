package player.heuristic;

import util.statemachine.MachineState;
import util.statemachine.Role;
import util.statemachine.StateMachine;
import util.statemachine.exceptions.MoveDefinitionException;

public class OpponentFocusHeuristic implements Heuristic {

	public double eval(StateMachine stateMachine, MachineState state, Role role)
			throws MoveDefinitionException {
		int numTotalJointMoves = stateMachine.getLegalJointMoves(state).size();
		int numYourMoves = stateMachine.getLegalMoves(state, role).size();
		if (numTotalJointMoves == 0) {
			numTotalJointMoves = 1;
		}
		return (double) numYourMoves / numTotalJointMoves;
	}

}
