package player.heuristic;

import java.util.List;

import util.statemachine.MachineState;
import util.statemachine.Move;
import util.statemachine.StateMachine;
import util.statemachine.Role;
import util.statemachine.exceptions.GoalDefinitionException;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;

public class MobilityHeuristic implements Heuristic {

	public double maxMobility = 0.0;
	private static int NUM_CHARGES = 10;
	
	public double eval(StateMachine stateMachine, MachineState state, Role role) throws MoveDefinitionException {
		double numMoves = (double) stateMachine.getLegalMoves(state, role).size();
		maxMobility = Math.max(numMoves, maxMobility);
		return numMoves / maxMobility * MAX_SCORE;
	}
	
	public double setMaxMobility(StateMachine machine, MachineState current, Role role, int maxLevel) throws MoveDefinitionException, TransitionDefinitionException {
		double maxMobility = findMaxMobility(machine, current, role);
		this.maxMobility = maxMobility;
		return maxMobility;
	}
	
	private double findMaxMobility(StateMachine stateMachine, MachineState current, Role role) throws MoveDefinitionException, TransitionDefinitionException {
		MachineState currState = current;
		double maxMobility = 0.0;
		
		for (int i = 0; i < NUM_CHARGES; i++) {
			while (!stateMachine.isTerminal(currState)) {
				maxMobility = Math.max(maxMobility, stateMachine.getLegalMoves(currState, role).size());
				currState = stateMachine.getRandomNextState(currState);
			}
			
		}
		return maxMobility;
	}
	
	
	private double findMaxMobility(StateMachine machine, MachineState current, Role role, int maxLevel, int level) throws MoveDefinitionException, TransitionDefinitionException {
		if (level > maxLevel || machine.isTerminal(current))
			return (double) machine.getLegalMoves(current, role).size();
		double maxMobility = 0.0;
		for (List<Move> jointMove : machine.getLegalJointMoves(current)) {
			MachineState next = machine.getNextState(current, jointMove);
			maxMobility = Math.max(maxMobility, findMaxMobility(machine, next, role, maxLevel, level + 1));
		}
		return maxMobility;
	}
	
}
