package player.searcher;

import java.util.List;
import java.util.Map;

import player.heuristic.Heuristic;
import util.statemachine.Move;
import util.statemachine.StateMachine;
import util.statemachine.MachineState;
import util.statemachine.Role;
import util.statemachine.exceptions.GoalDefinitionException;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;

public class MinimaxHeuristicSearcher extends HeuristicSearcher {
	
	public MinimaxHeuristicSearcher(StateMachine machine, Map<MachineState, Double> stateValues, Heuristic heuristic) {
		super(machine, stateValues, heuristic);
	}

	public Move findMove(MachineState state, Role role) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		return getMinimaxMove(state, role);
	}

	private Move getMinimaxMove(MachineState currentState, Role role) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		double maxScore = MIN_SCORE;
		Move selection = null;
		for (Move move : machine.getLegalMoves(currentState, role)) {
			double score = getMinScore(machine, currentState, role, move, 1);
			if (score >= maxScore) {
				maxScore = score;
				selection = move;
			}
		}
		return selection;
	}
	
	private double getMinScore(StateMachine machine, MachineState currentState, Role role, Move move, int level) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		double minScore = MAX_SCORE;
		for (List<Move> jointMove : machine.getLegalJointMoves(currentState, role, move)) {
			MachineState nextState = machine.getNextState(currentState, jointMove);
			double score;
			if (stateValues.containsKey(nextState)) {
				score = stateValues.get(nextState);
				// debug output to see the values stored for states in the cache
				/*System.out.println("found a state in the cache");
				System.out.println(nextState.toString() + ": " + score);
				System.out.println("--------------");*/
			} else {
				score = getMaxScore(machine, nextState, role, level);
				stateValues.put(nextState, score);
			}
			minScore = Math.min(minScore, score);
		}
		return minScore;
	}
	
	private double getMaxScore(StateMachine machine, MachineState state, Role role, int level) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		if (machine.isTerminal(state))
			return machine.getGoal(state, role);
		if (level == maxLevel) {
			return heuristic.eval(machine, state, role);
		}
		double maxScore = MIN_SCORE;
		for (Move move : machine.getLegalMoves(state, role)) {
			double score = getMinScore(machine, state, role, move, level + 1);
			maxScore = Math.max(maxScore, score);
		}
		return maxScore;
	}
	
}
