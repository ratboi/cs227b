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

	public int numCharges = 3;
	public double delta = 0.000001;
	
	public MonteCarloHeuristic() {}
	
	public MonteCarloHeuristic(int numCharges) {
		this.numCharges = numCharges;
	}
	
	public double eval(StateMachine stateMachine, MachineState state, Role role, StateMachineGamer gamer)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
		double runningTotal = 0.0;
		double depth = 0.0;
		
		for (int i = 0; i < numCharges; i++) {
			if (gamer.isStopped()) break;
			MachineState currState = state;
			while (!stateMachine.isTerminal(currState) && !gamer.isStopped()) {
				depth++;
				currState = stateMachine.getRandomNextState(currState);
			}
			runningTotal += stateMachine.getGoal(currState, role);
		}
		runningTotal /= numCharges;
		depth = 1 / (depth / numCharges);
		if (runningTotal >= 50) runningTotal += depth;
		else runningTotal -= depth;
		if (runningTotal==(int)runningTotal) runningTotal += delta;
		return runningTotal;
	}

}
