package player.heuristic;

import util.statemachine.MachineState;
import player.gamer.statemachine.StateMachineGamer;
import util.statemachine.Role;
import util.statemachine.StateMachine;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;

public class OpponentMobilityHeuristic implements Heuristic {

	public double maxMobility = 0.0;
	private static int NUM_CHARGES = 10;
	
	public double eval(StateMachine stateMachine, MachineState state, Role role, StateMachineGamer gamer)
			throws MoveDefinitionException {
		int numTotalJointMoves = stateMachine.getLegalJointMoves(state).size();
		int numYourMoves = stateMachine.getLegalMoves(state, role).size();
		if (numYourMoves == 0) {
			numYourMoves = 1;
		}
		double numMoves = (double)numTotalJointMoves / numYourMoves;
		maxMobility = Math.max(numMoves, maxMobility);
		return numMoves / maxMobility * MAX_SCORE;
	}
	
	public double setMaxMobility(StateMachine machine, MachineState current, Role role, int maxLevel) throws MoveDefinitionException, TransitionDefinitionException {
		double maxMobility = findMaxMobility(machine, current, role);
		this.maxMobility = maxMobility;
		return maxMobility;
	}
	
	private double findMaxMobility(StateMachine stateMachine, MachineState state, Role role) throws MoveDefinitionException, TransitionDefinitionException {
		MachineState currState = state;
		double maxMobility = 0.0;
		
		for (int i = 0; i < NUM_CHARGES; i++) {
			while (!stateMachine.isTerminal(currState)) {
				int numTotalJointMoves = stateMachine.getLegalJointMoves(state).size();
				int numYourMoves = stateMachine.getLegalMoves(state, role).size();
				if (numYourMoves == 0) {
					numYourMoves = 1;
				}
				double opponentMobility = (double)numTotalJointMoves / numYourMoves;
				maxMobility = Math.max(maxMobility, opponentMobility);
				currState = stateMachine.getRandomNextState(currState);
			}
			
		}
		return maxMobility;
	}

}
