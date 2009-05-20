package player.searcher;

import java.util.List;

import util.statemachine.Move;
import util.statemachine.StateMachine;
import util.statemachine.MachineState;
import util.statemachine.Role;
import util.statemachine.exceptions.GoalDefinitionException;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;

public class MinimaxSearcher extends Searcher {
	
	private static final int MIN_SCORE = 0;
	private static final int MAX_SCORE = 100;
	
	private StateMachine machine;
	
	public MinimaxSearcher(StateMachine machine) {
		this.machine = machine;
	}

	public Move findMove(MachineState state, Role role) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		return getMinimaxMove(state, role);
	}

	private Move getMinimaxMove(MachineState currentState, Role role) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		int maxScore = MIN_SCORE;
		Move selection = null;
		for (Move move : machine.getLegalMoves(currentState, role)) {
			int score = getMinScore(machine, currentState, role, move);
			if (score >= maxScore) {
				maxScore = score;
				selection = move;
			}
		}
		return selection;
	}
	
	private int getMinScore(StateMachine machine, MachineState currentState, Role role, Move move) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		int minScore = MAX_SCORE;
		for (List<Move> jointMove : machine.getLegalJointMoves(currentState, role, move)) {
			MachineState nextState = machine.getNextState(currentState, jointMove);
			minScore = Math.min(minScore, getMaxScore(machine, nextState, role));
		}
		return minScore;
	}
	
	private int getMaxScore(StateMachine machine, MachineState state, Role role) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		if (machine.isTerminal(state))
			return machine.getGoal(state, role);
		int maxScore = MIN_SCORE;
		for (Move move : machine.getLegalMoves(state, role)) {
			maxScore = Math.max(maxScore, getMinScore(machine, state, role, move));
		}
		return maxScore;
	}
	
}
