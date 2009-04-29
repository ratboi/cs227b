package player.gamer.statemachine.reflex.easy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import apps.player.detail.DetailPanel;

import player.gamer.statemachine.StateMachineGamer;
import player.gamer.statemachine.reflex.event.ReflexMoveSelectionEvent;
import player.gamer.statemachine.reflex.gui.ReflexDetailPanel;
import util.statemachine.Move;
import util.statemachine.StateMachine;
import util.statemachine.MachineState;
import util.statemachine.Role;
import util.statemachine.exceptions.GoalDefinitionException;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;
import util.statemachine.prover.cache.CachedProverStateMachine;

public class EasyGamer extends StateMachineGamer {

	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		// Do nothing.
	}

	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		long start = System.currentTimeMillis();
		List<Move> legalMoves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
		Move selection = getMinimaxMove(getStateMachine(), getCurrentState(), getRole());
		long stop = System.currentTimeMillis();

		notifyObservers(new ReflexMoveSelectionEvent(legalMoves, selection, stop - start));
		return selection;
	}

	private Move getMinimaxMove(StateMachine stateMachine, MachineState currentState, Role role) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		int maxScore = -1;
		Move selection = null;
		for (Move move : stateMachine.getLegalMoves(currentState, role)) {
			int score = getMinScore(stateMachine, currentState, role, move);
			if (score >= maxScore) {
				maxScore = score;
				selection = move;
			}
		}
		return selection;
	}
	
	private int getMinScore(StateMachine machine, MachineState currentState, Role role, Move move) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		int minScore = 101;
		for (List<Move> jointMove : machine.getLegalJointMoves(currentState, role, move)) {
			MachineState nextState = machine.getNextState(currentState, jointMove);
			minScore = Math.min(minScore, getMaxScore(machine, nextState, role));
		}
		return minScore;
	}
	
	private int getMaxScore(StateMachine machine, MachineState state, Role role) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		if (machine.isTerminal(state))
			return machine.getGoal(state, role);
		int maxScore = -1;
		for (Move move : machine.getLegalMoves(state, role)) {
			maxScore = Math.max(maxScore, getMinScore(machine, state, role, move));
		}
		return maxScore;
	}
	
	@Override
	public StateMachine getInitialStateMachine() {
		return new CachedProverStateMachine();
	}

	@Override
	public String getName() {
		return "Easy";
	}

	@Override
	public DetailPanel getDetailPanel() {
		return new ReflexDetailPanel();
	}

	
}
