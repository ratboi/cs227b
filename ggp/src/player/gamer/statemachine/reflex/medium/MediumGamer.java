package player.gamer.statemachine.reflex.medium;

import java.util.List;

import apps.player.detail.DetailPanel;

import player.heuristic.*;
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

public class MediumGamer extends StateMachineGamer {

	public final int MAX_LEVEL = 2;
	public Heuristic heuristic = new MobilityHeuristic();
	
	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		((MobilityHeuristic) heuristic).setMaxMobility(getStateMachine(), getCurrentState(), getRole(), MAX_LEVEL);
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
		double maxScore = -1;
		Move selection = null;
		for (Move move : stateMachine.getLegalMoves(currentState, role)) {
			double score = getMinScore(stateMachine, currentState, role, move, 0);
			if (score >= maxScore) {
				maxScore = score;
				selection = move;
			}
		}
		return selection;
	}
	
	private double getMinScore(StateMachine machine, MachineState currentState, Role role, Move move, int level) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		double minScore = 101;
		for (List<Move> jointMove : machine.getLegalJointMoves(currentState, role, move)) {
			MachineState nextState = machine.getNextState(currentState, jointMove);
			minScore = Math.min(minScore, getMaxScore(machine, nextState, role, level + 1));
		}
		return minScore;
	}
	
	private double getMaxScore(StateMachine machine, MachineState state, Role role, int level) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		System.out.println("level " + level);
		if (machine.isTerminal(state))
			return machine.getGoal(state, role);
		if (level > MAX_LEVEL)
			return heuristic.eval(machine, state, role);
		double maxScore = -1.0;
		for (Move move : machine.getLegalMoves(state, role)) {
			maxScore = Math.max(maxScore, getMinScore(machine, state, role, move, level));
		}
		return maxScore;
	}
	
	@Override
	public StateMachine getInitialStateMachine() {
		return new CachedProverStateMachine();
	}

	@Override
	public String getName() {
		return "Medium";
	}

	@Override
	public DetailPanel getDetailPanel() {
		return new ReflexDetailPanel();
	}

	
}