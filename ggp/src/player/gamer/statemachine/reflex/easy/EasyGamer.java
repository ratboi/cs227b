package player.gamer.statemachine.reflex.easy;

import java.util.ArrayList;
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
	
	private int statesExpanded;
	Map<MachineState, Integer> stateTerminalGoals = new HashMap<MachineState, Integer>();

	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		// Do nothing.
	}

	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		statesExpanded = 0;
		long start = System.currentTimeMillis();
		StateMachine stateMachine = getStateMachine();
		MachineState currentState = getCurrentState();
		Role role = getRole();
		Map<Role, Integer> roleIndices = stateMachine.getRoleIndices();

		List<Move> legalMoves = stateMachine.getLegalMoves(currentState, role);
		List<List<Move>> moves = stateMachine.getLegalJointMoves(currentState);
		
		int maxGoal = 0;
		Move selection = null;
		
		statesExpanded++;
		for (List<Move> move : moves) {
			MachineState nextState = stateMachine.getNextState(currentState, move);
			int goal = getTerminalGoal(stateMachine, nextState, stateTerminalGoals);
			if (goal >= maxGoal) {
				maxGoal = goal;
				selection = move.get(roleIndices.get(role));
			}
		}
		
		long stop = System.currentTimeMillis();

		notifyObservers(new ReflexMoveSelectionEvent(legalMoves, selection, stop - start));
		System.out.println(statesExpanded);
		return selection;
	}
	
	private int getTerminalGoal(StateMachine stateMachine, MachineState currentState, Map<MachineState, Integer> stateTerminalGoals) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
		if (stateMachine.isTerminal(currentState)) {
			int goal = stateMachine.getGoal(currentState, getRole());
			stateTerminalGoals.put(currentState, goal);
			return goal;
		}
		if (stateTerminalGoals.containsKey(currentState)) {
			return stateTerminalGoals.get(currentState);
		}
		stateTerminalGoals.put(currentState, -1); // place in map will detect cycles if we see this state again
		statesExpanded++;
		int maxGoal = 0;
		List<List<Move>> moves = stateMachine.getLegalJointMoves(currentState);		
		for (List<Move> move : moves) {
			MachineState nextState = stateMachine.getNextState(currentState, move);
			maxGoal = Math.max(maxGoal, getTerminalGoal(stateMachine, nextState, stateTerminalGoals));
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
