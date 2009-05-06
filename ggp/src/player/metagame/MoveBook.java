package player.metagame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import player.heuristic.Heuristic;
import player.heuristic.LinearCombinedHeuristic;
import player.heuristic.MobilityHeuristic;
import player.heuristic.MonteCarloHeuristic;
import player.heuristic.OpponentFocusHeuristic;

import util.statemachine.MachineState;
import util.statemachine.Move;
import util.statemachine.Role;
import util.statemachine.StateMachine;
import util.statemachine.exceptions.GoalDefinitionException;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;

public class MoveBook {
	private StateMachine machine;
	private Role role;
	private long BUFFER = 500;
	public Map<MachineState, Move> openBook = new HashMap<MachineState, Move>();
	
	public MoveBook(StateMachine machine, Role role) {
		this.machine = machine;
		this.role = role;
	}
	
	private double getMinScore(StateMachine machine, MachineState state, Role role, Heuristic heuristic, Move move, int level, int maxLevel, long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		double minScore = 101;
		for (List<Move> jointMove : machine.getLegalJointMoves(state, role, move)) {
			if (System.currentTimeMillis() > timeout - BUFFER)
				break;
			MachineState nextState = machine.getNextState(state, jointMove);
			double score = getMaxScore(machine, nextState, role, heuristic, level, maxLevel, timeout);
			minScore = Math.min(minScore, score);
		}
		return minScore;
	}
	
	private double getMaxScore(StateMachine machine, MachineState state, Role role, Heuristic heuristic, int level, int maxLevel, long timeout) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		if (machine.isTerminal(state))
			return machine.getGoal(state, role);
		if (System.currentTimeMillis() > timeout - BUFFER)
			return -1.0;
		if (level == maxLevel) {
			double heuristicScore = heuristic.eval(machine, state, role);
			return heuristicScore;
		}
		double maxScore = findBestMove(state, role, heuristic, level + 1, maxLevel, timeout);
		return maxScore;
	}
	
	private double findBestMove(MachineState state, Role role, Heuristic heuristic, int level, int maxLevel, long timeout) {
		double maxScore = -1.0;
		try {
			// find and cache best move for the current state
			Move bestMove = null;
			for (Move move : machine.getLegalMoves(state, role)) {
				if (System.currentTimeMillis() > timeout - BUFFER)
					break;
				double score = getMinScore(machine, state, role, heuristic, move, level, maxLevel, timeout);
				if (score > maxScore) {
					maxScore = score;
					bestMove = move;
				}
			}
			// only cache the state if we're in the safe zone, otherwise 'bestMove' could just be junk
			if (System.currentTimeMillis() < timeout - BUFFER) {
				openBook.put(state, bestMove);
				//System.out.println("put some stuff into open book");
			}
		} catch (Exception e) {
			System.err.println("exception when finding best move in the open book thread");
		}
		return maxScore;
	}
	
	public void startOpenBookThread(MachineState startState, Heuristic heuristic, long timeout) throws MoveDefinitionException, TransitionDefinitionException {
		//OpenBookThread openBook = new OpenBookThread(startState, heuristic, timeout);
		//openBook.start();
		//EndBookThread endBook = new EndBookThread(startState, heuristic, timeout);
		//endBook.start();
	}
	
	public class OpenBookThread extends Thread {
		private MachineState startState;
		private Heuristic heuristic;
		private long timeout;
		
		public OpenBookThread(MachineState startState, long timeout) throws MoveDefinitionException, TransitionDefinitionException {
			this(startState, new MonteCarloHeuristic(5), timeout);
		}
		
		public OpenBookThread(MachineState startState, Heuristic heuristic, long timeout) throws MoveDefinitionException, TransitionDefinitionException {
			this.startState = startState;
			this.heuristic = heuristic;
			this.timeout = timeout;
		}
		
		public void run() {
			int maxLevel = 1;
			while (true) {
				findBestMove(startState, role, heuristic, 1, maxLevel, timeout);
				if (System.currentTimeMillis() > timeout - BUFFER) {
					System.out.println("open book is past time limit, breaking early");
					break;
				}
				maxLevel++;
				System.out.println("max level of open book search is now: " + maxLevel);
			}
		}
	}

	
}
