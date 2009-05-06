package player.heuristic;

import java.util.List;

import util.statemachine.MachineState;
import util.statemachine.Move;
import util.statemachine.Role;
import util.statemachine.StateMachine;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;
import util.statemachine.exceptions.GoalDefinitionException;

public class MonteCarloHeuristic implements Heuristic {

	public int numCharges = 10;
	
	public MonteCarloHeuristic() {}
	
	public MonteCarloHeuristic(int numCharges) {
		this.numCharges = numCharges;
	}
	
	public double eval(StateMachine stateMachine, MachineState state, Role role)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
		MachineState currState = state;
		double runningTotal = 0.0;
		
		for (int i = 0; i < numCharges; i++) {
			while (!stateMachine.isTerminal(currState)) {
				currState = stateMachine.getRandomNextState(currState);
			}
			runningTotal += stateMachine.getGoal(currState, role);
		}
		return runningTotal / numCharges;
	}

}
