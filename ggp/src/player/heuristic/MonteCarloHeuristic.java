package player.heuristic;

import java.util.List;

import util.statemachine.MachineState;
import util.statemachine.Move;
import util.statemachine.Role;
import util.statemachine.StateMachine;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;
import util.statemachine.exceptions.GoalDefinitionException;
import player.gamer.statemachine.StateMachineGamer;

public class MonteCarloHeuristic implements Heuristic {

	public int numCharges = 1;
	
	public MonteCarloHeuristic() {}
	
	public MonteCarloHeuristic(int numCharges) {
		this.numCharges = numCharges;
	}
	
	public double eval(StateMachine stateMachine, MachineState state, Role role, StateMachineGamer gamer)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
		double runningTotal = 0.0;
		
		for (int i = 0; i < numCharges; i++) {
			if (gamer.isStopped()) break;
			MachineState currState = state;
			int count = 0;
			while (!stateMachine.isTerminal(currState) && !gamer.isStopped()) {
				count++;
				System.out.print("!");
				currState = stateMachine.getRandomNextState(currState);
			}
			runningTotal += stateMachine.getGoal(currState, role);
		}
		return runningTotal / numCharges;
	}

}
