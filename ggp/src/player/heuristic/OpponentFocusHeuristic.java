package player.heuristic;

import util.statemachine.MachineState;
import util.statemachine.Role;
import util.statemachine.StateMachine;
import util.statemachine.exceptions.MoveDefinitionException;
import player.gamer.statemachine.StateMachineGamer;

public class OpponentFocusHeuristic implements Heuristic {

	public double eval(StateMachine stateMachine, MachineState state, Role role, StateMachineGamer gamer)
			throws MoveDefinitionException {
		int numTotalJointMoves = stateMachine.getLegalJointMoves(state).size();
		int numYourMoves = stateMachine.getLegalMoves(state, role).size();
		if (numTotalJointMoves == 0) {
			numTotalJointMoves = 1;
		}
		return MAX_SCORE * numYourMoves / numTotalJointMoves;
	}

}
