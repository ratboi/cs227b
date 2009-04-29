package player.heuristic;

import util.statemachine.MachineState;
import util.statemachine.Role;
import util.statemachine.StateMachine;
import util.statemachine.exceptions.GoalDefinitionException;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;

import java.util.List;

public class LinearCombinedHeuristic implements Heuristic {
	
	private List<Heuristic> heuristics;
	private List<Double> weights;
	
	public LinearCombinedHeuristic(List<Heuristic> heuristics, List<Double> weights) {
		this.heuristics = heuristics;
		this.weights = weights;
	}
	
	@Override
	public double eval(StateMachine stateMachine, MachineState state, Role role)
			throws MoveDefinitionException, TransitionDefinitionException,
			GoalDefinitionException {
		double totalScore = 0.0;
		for (int i = 0; i < heuristics.size(); i++) {
			totalScore += weights.get(i) * heuristics.get(i).eval(stateMachine, state, role);
		}
		return totalScore;
	}

}
