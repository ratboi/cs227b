package player.gamer.statemachine.reflex.openbook;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import javax.swing.Timer;

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
import util.statemachine.propnet.PropNetStateMachine;

public class OpenBookGamer extends StateMachineGamer {

	public final int MAX_LEVEL = 2;
	public final int NODES_TO_TERMINAL = 2;
	public static final int BUFFER_TIME = 1000;
	private Heuristic heuristic;
	private Map<MachineState, Double> stateValues;
	private boolean foundMove = false;
	private boolean stoppedEarly = false;
	public double maxGoalValue = -1;
	public double minGoalValue = 101;
	private FindMoveThread move;
	private Map<MachineState, Move> openBook = new HashMap<MachineState, Move>();
	private Map<MachineState, Double> endBook = new HashMap<MachineState, Double>();
	private int roleIndex;
	
	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		roleIndex = getStateMachine().getRoleIndices().get(getRole());
		/*
		MobilityHeuristic mobilityHeuristic = new MobilityHeuristic();
		OpponentFocusHeuristic opponentFocusHeuristic = new OpponentFocusHeuristic();
		mobilityHeuristic.setMaxMobility(getStateMachine(), getCurrentState(), getRole(), MAX_LEVEL);
		ArrayList<Heuristic> heuristics = new ArrayList<Heuristic>();
		heuristics.add(mobilityHeuristic);
		heuristics.add(opponentFocusHeuristic);
		ArrayList<Double> weights = new ArrayList<Double>();
		weights.add(0.5);
		weights.add(0.5);
		
		heuristic = new LinearCombinedHeuristic(heuristics, weights);*/
		heuristic = new MonteCarloHeuristic();
		stateValues = new HashMap<MachineState, Double>();
		
		
		OpenBookThread openBookThread = new OpenBookThread(getStateMachine(), getCurrentState(), getRole(), timeout);
		openBookThread.start();
		EndBookThread endBookThread = new EndBookThread(getStateMachine(), getCurrentState(), getRole(), timeout);
		endBookThread.start();
		while (System.currentTimeMillis() < timeout);
		
		// headstart
		//move = new FindMoveThread(getStateMachine(), getCurrentState(), getRole());
		//move.start();
	}
	
	public class OpenBookThread extends Thread {
		StateMachine machine;
		MachineState startState;
		Role role;
		private long timeout;
		private final long BUFFER = 500;
		private Heuristic heuristic = new MonteCarloHeuristic(5);
		
		public OpenBookThread(StateMachine machine, MachineState startState, Role role, long timeout) throws MoveDefinitionException, TransitionDefinitionException {
			this.machine = machine;
			this.startState = startState;
			this.role = role;
			this.timeout = timeout;
			MobilityHeuristic mobilityHeuristic = new MobilityHeuristic();
			OpponentFocusHeuristic opponentFocusHeuristic = new OpponentFocusHeuristic();
			mobilityHeuristic.setMaxMobility(getStateMachine(), getCurrentState(), getRole(), MAX_LEVEL);
			ArrayList<Heuristic> heuristics = new ArrayList<Heuristic>();
			heuristics.add(mobilityHeuristic);
			heuristics.add(opponentFocusHeuristic);
			ArrayList<Double> weights = new ArrayList<Double>();
			weights.add(0.5);
			weights.add(0.5);
			
			heuristic = new LinearCombinedHeuristic(heuristics, weights);
		}

		private double getMinScore(StateMachine machine, MachineState state, Role role, Move move, int level, int maxLevel) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
			double minScore = 101;
			for (List<Move> jointMove : machine.getLegalJointMoves(state, role, move)) {
				if (System.currentTimeMillis() > timeout - BUFFER)
					break;
				MachineState nextState = machine.getNextState(state, jointMove);
				double score = getMaxScore(machine, nextState, role, level, maxLevel);
				minScore = Math.min(minScore, score);
			}
			return minScore;
		}
		
		private double getMaxScore(StateMachine machine, MachineState state, Role role, int level, int maxLevel) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
			if (machine.isTerminal(state))
				return machine.getGoal(state, role);
			if (System.currentTimeMillis() > timeout - BUFFER)
				return -1.0;
			if (level == maxLevel) {
				double heuristicScore = heuristic.eval(machine, state, role);
				return heuristicScore;
			}
			double maxScore = findBestMove(state, role, level + 1, maxLevel);
			return maxScore;
		}
		
		private double findBestMove(MachineState state, Role role, int level, int maxLevel) {
			double maxScore = -1.0;
			try {
				// find and cache best move for the current state
				Move bestMove = null;
				for (Move move : machine.getLegalMoves(state, role)) {
					if (System.currentTimeMillis() > timeout - BUFFER)
						break;
					double score = getMinScore(machine, state, role, move, level, maxLevel);
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
		
		public void run() {
			int maxLevel = 1;
			while (true) {
				findBestMove(startState, role, 1, maxLevel);
				if (System.currentTimeMillis() > timeout - BUFFER) {
					System.out.println("open book is past time limit, breaking early");
					break;
				}
				maxLevel++;
				System.out.println("max level of open book search is now: " + maxLevel);
			}
		}
	}
	
	
	public class EndBookThread extends Thread {
		StateMachine machine;
		MachineState startState;
		Role role;
		private long timeout;
		private final long BUFFER = 500;

		public EndBookThread(StateMachine machine, MachineState startState, Role role, long timeout) throws MoveDefinitionException, TransitionDefinitionException {
			this.startState = startState;
			this.timeout = timeout;
			this.machine = machine;
			this.role = role;
		}

		public void run() {
			int maxLevel = 1;
			findTerminalNodes(startState, heuristic, 1, maxLevel, timeout);
		}
		public void findTerminalNodes(MachineState startState, Heuristic heuristic, int level, int maxLevel, long timeout) {
			HashSet<MachineState> terminals = new HashSet<MachineState>();
			try {
				while (System.currentTimeMillis() < timeout - BUFFER) {
					MachineState state = startState;
					MachineState lastState = startState;
					MachineState twoStatesEarlier = startState;
					while (!machine.isTerminal(state)) {
						twoStatesEarlier = lastState;
						lastState = state;
						state = machine.getRandomNextState(state);
					}

					if (terminals.add(state)) {
						Thread workBackwards = new ReverseThread(machine, lastState, role, timeout);
						workBackwards.start();

						int value = machine.getGoal(state, role);
						System.out.println("New Terminal state found with value " + value);
						if (value>maxGoalValue) maxGoalValue = value;
						if (value<minGoalValue) minGoalValue = value;
					}
					else {
						System.out.println("already found!");
					}
				}
				System.out.println("closed book is past time limit, breaking early");

			}
			catch (Exception e) {
				System.err.println("Exception in endbook depth charge.");
			}
		}
	}

	public class ReverseThread extends Thread {
		StateMachine machine;
		MachineState startState;
		Role role;
		private long timeout;
		private final long BUFFER = 500;

		public ReverseThread(StateMachine machine, MachineState startState, Role role, long timeout) throws MoveDefinitionException, TransitionDefinitionException {
			this.startState = startState;
			this.timeout = timeout;
			this.machine = machine;
			this.role = role;
		}
		
		private double getMinScore(MachineState state, Role role, Move move, List<MachineState> viewed) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
			double minScore = 101;
			MachineState trueNextState = null;
			for (List<Move> jointMove : machine.getLegalJointMoves(state, role, move)) {
				if (System.currentTimeMillis() > timeout - BUFFER)
					break;
				MachineState nextState = machine.getNextState(state, jointMove);
				double score = getMaxScore(nextState, role, viewed);
				if (score < minScore) {
					minScore = score;
					trueNextState = nextState;
				}
			}
			//viewed.add(trueNextState);
			return minScore;
		}
		
		private double getMaxScore(MachineState state, Role role, List<MachineState> viewed) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
			if (machine.isTerminal(state))
				return machine.getGoal(state, role);
			if (System.currentTimeMillis() > timeout - BUFFER)
				return -1.0;
			double maxScore = findBestMove(state, role, viewed);
			return maxScore;
		}
		
		private double findBestMove(MachineState state, Role role, List<MachineState> viewed) {
			double maxScore = -1.0;
			viewed.add(state);
			try {
				//System.out.println("States found? " + viewed.size());
				// find and cache best move for the current state
				// Move bestMove = null;
				for (Move move : machine.getLegalMoves(state, role)) {
					if (System.currentTimeMillis() > timeout - BUFFER)
						break;
					double score = getMinScore(state, role, move, viewed);
					if (score > maxScore) {
						maxScore = score;
			//			bestMove = move;
					}
				}
			} catch (Exception e) {
				System.err.println("exception when finding best move in the end book thread");
			}
			return maxScore;
		}
		
		
		public void run() {
			//\System.out.println("ReverseThread started!");
			List<MachineState> leadsToTerminal = new ArrayList<MachineState>();
			double terminalValue = findBestMove(startState, role, leadsToTerminal);
			endBook.put(startState, terminalValue);
			//for (MachineState m : leadsToTerminal) {
			//	endBook.put(m, terminalValue);
			//}
			System.out.println("Added new 'terminal' state with value " + terminalValue);
		}
	}

	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		System.out.println("this is the timeout: " + timeout);
		long start = System.currentTimeMillis();
		System.out.println("this is the start: " + start);
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stoppedEarly = true;
				//System.out.println("Early Termination!");
			}
		};
		Timer t = new Timer((int) timeout - (int) start - BUFFER_TIME, taskPerformer);
		t.setRepeats(false);
		t.start();
		
		/*
		if (!move.isAlive()) {
			System.out.println("NOTALIVE");
			move = new FindMoveThread(getStateMachine(), getCurrentState(), getRole());
			move.start();
		}*/
		move = new FindMoveThread(getStateMachine(), getCurrentState(), getRole());
		move.start();

		while (!foundMove);
		t.stop();
		foundMove = false;
		stoppedEarly = false;
		
		
		Move selection = move.getSelection();
		List<Move> legalMoves = move.getLegalMoves();
		
		long end = System.currentTimeMillis();
		System.out.println("this is the end: " + end);
        notifyObservers(new ReflexMoveSelectionEvent(legalMoves, selection, end - start));
        
        //stoppedEarly = false;
        //System.out.println("about to return the selection\n");
        return selection;
	}
	
	public class FindMoveThread extends Thread {
		private Move selection;
		private List<Move> legalMoves;
		private StateMachine stateMachine;
		private MachineState currentState;
		private Role role;
		private int maxLevel = 1;
		//private boolean stoppedEarly = false;
		
		public FindMoveThread(StateMachine stateMachine, MachineState state, Role role) {
			this.stateMachine = stateMachine;
			this.currentState = state;
			this.role = role;
		}
		
		public Move getSelection() {
			return selection;
		}
		
		public List<Move> getLegalMoves() {
			return legalMoves;
		}
		
		public MachineState getCurrentState() {
			return currentState;
		}
		
		public void setCurrentState(MachineState state) {
			currentState = state;
		}
		/*
		public void stopEarly() {
			stoppedEarly = true;
		}
		
		public boolean isStopped() {
			return stoppedEarly;
		}*/
		
	    public void run() {
	    	
	    	try {
				legalMoves = stateMachine.getLegalMoves(currentState, role);
			} catch (MoveDefinitionException e1) {}
			
			try {
				while (!stoppedEarly) {
					if (maxLevel < 10) {
						System.out.println("max level: " + maxLevel);
					}
					if (getStateMachine() == null) {
						System.out.println("state machine is null");
					}
					if (currentState == null) {
						System.out.println("current state is null");
					}
					if (getRole() == null) {
						System.out.println("role is null");
					}
					if (openBook.containsKey(currentState)) {
						System.out.println("found a move in the open book, skipping search, whee");
						selection = openBook.get(currentState);
						break;
					}
					selection = getMinimaxMove(currentState);
					maxLevel++;
				}
			} catch (GoalDefinitionException e) {
			} catch (MoveDefinitionException e) {
			} catch (TransitionDefinitionException e) {}
			
			foundMove = true;
	    }
	    
		private Move getMinimaxMove(MachineState currentState) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
			double maxScore = -1;
			Move selection = null;
			if (stateMachine.getLegalMoves(currentState, role).size() == 0) {
				System.out.println("UH OH NO LEGAL MOVES");
			}
			for (List<Move> jointMove : stateMachine.getLegalJointMoves(currentState)) {
				if (stoppedEarly) {
					return selection;
				}
				MachineState nextState = stateMachine.getNextState(currentState, jointMove);
				double score = getMinScore(nextState, 1);
				if (score >= maxScore) {
					maxScore = score;
					selection = jointMove.get(roleIndex);
				}
			}
			return selection;
		}
		
		private double getMinScore(MachineState state, int level) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
			if (stateMachine.isTerminal(state))
				return stateMachine.getGoal(state, role);
			if (endBook.containsKey(state)) {
				System.out.println("Found a terminal state with end book!");
				return endBook.get(state);
			}
			if (level == maxLevel) {
				double heuristicScore = heuristic.eval(stateMachine, state, role);
				return heuristicScore;
			}
			double minScore = 101;
			for (List<Move> jointMove : stateMachine.getLegalJointMoves(state)) {
				if (stoppedEarly)
					return minScore;
				MachineState nextState = stateMachine.getNextState(state, jointMove);
				double score = getMaxScore(nextState, level + 1);
				minScore = Math.min(minScore, score);
			}
			return minScore;
		}
		
		private double getMaxScore(MachineState state, int level) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
			if (stateMachine.isTerminal(state))
				return stateMachine.getGoal(state, role);
			if (endBook.containsKey(state)) {
				System.out.println("Found a terminal state with end book!");
				return endBook.get(state);
			}
			if (level == maxLevel) {
				double heuristicScore = heuristic.eval(stateMachine, state, role);
				return heuristicScore;
			}
			double maxScore = -1.0;
			for (List<Move> move : stateMachine.getLegalJointMoves(state)) {
				if (stoppedEarly)
					return maxScore;
				MachineState nextState = stateMachine.getNextState(state, move);
				double score = getMinScore(nextState, level + 1);
				maxScore = Math.max(maxScore, score);
			}
			return maxScore;
		}

	}
	

	
	@Override
	public StateMachine getInitialStateMachine() {
		return new PropNetStateMachine();
	}

	@Override
	public String getName() {
		return "Open Book";
	}

	@Override
	public DetailPanel getDetailPanel() {
		return new ReflexDetailPanel();
	}

	
}