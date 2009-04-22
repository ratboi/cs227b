package player.gamer.statemachine.reflex.easy;

import java.util.List;
import java.util.ArrayList;
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
		StateMachine stateMachine = getStateMachine();
		MachineState currentState = getCurrentState();
		Role role = getRole();
		Map<Role, Integer> roleIndices = stateMachine.getRoleIndices();

		List<Move> legalMoves = stateMachine.getLegalMoves(currentState, role);
		List<List<Move>> moves = stateMachine.getLegalJointMoves(currentState);
		
		int maxGoal = 0;
		Move selection = null;
		
		for (List<Move> move : moves) {
			MachineState nextState = stateMachine.getNextState(currentState, move);
			int goal = getTerminalGoal(stateMachine, nextState);
			if (goal >= maxGoal) {
				maxGoal = goal;
				selection = move.get(roleIndices.get(role));
			}
		}
		
		long stop = System.currentTimeMillis();

		notifyObservers(new ReflexMoveSelectionEvent(legalMoves, selection, stop - start));
		return selection;
	}
	
	private int getTerminalGoal(StateMachine stateMachine, MachineState currentState) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
		//TODO need to add base case for cycles
		//TODO cache for already-seen states
		if (stateMachine.isTerminal(currentState)) {
			return stateMachine.getGoal(currentState, getRole());
		}
		int maxGoal = 0;
		List<List<Move>> moves = stateMachine.getLegalJointMoves(currentState);		
		for (List<Move> move : moves) {
			MachineState nextState = stateMachine.getNextState(currentState, move);
			maxGoal = Math.max(maxGoal, getTerminalGoal(stateMachine, nextState));
		}
		return maxGoal;
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
