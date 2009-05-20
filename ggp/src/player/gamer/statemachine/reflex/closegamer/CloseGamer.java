package player.gamer.statemachine.reflex.closegamer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Timer;

import player.gamer.statemachine.StateMachineGamer;
import player.gamer.statemachine.reflex.event.ReflexMoveSelectionEvent;
import player.gamer.statemachine.reflex.medium.MediumGamer.FindMoveThread;
import player.heuristic.Heuristic;
import player.heuristic.MonteCarloHeuristic;
import util.statemachine.propnet.PropNetStateMachine;
import util.statemachine.MachineState;
import util.statemachine.Move;
import util.statemachine.Role;
import util.statemachine.StateMachine;
import util.statemachine.exceptions.GoalDefinitionException;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;
public class CloseGamer extends StateMachineGamer {

	// thread-related variables
	public static final int BUFFER_TIME = 500;
	private boolean foundMove = false;
	private boolean stoppedEarly = false;
	
	// search-related variables
	private Heuristic heuristic;
	private Map<MachineState, Double> stateValues;
	
	//search termination;
	private boolean done = false;
	private int turn = 1;
	
	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		System.out.println("START - " + System.currentTimeMillis());
		heuristic = new MonteCarloHeuristic();
		stateValues = new HashMap<MachineState, Double>();
	}

	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		long start = System.currentTimeMillis();
		
		FindMoveThread finder = new FindMoveThread(getStateMachine(), getCurrentState(), getRole());
		finder.start();
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stoppedEarly = true;
				System.out.println("Early Termination!");
			}
		};
		Timer t = new Timer((int) timeout - (int) start - BUFFER_TIME, taskPerformer);
		t.setRepeats(false);
		t.start();
		while (!foundMove);
		t.stop();
		stoppedEarly = false;
		foundMove = false;
		
		List<Move> legalMoves = finder.getLegalMoves();
		Move selection = finder.getSelection();
		
		long end = System.currentTimeMillis();
		
        notifyObservers(new ReflexMoveSelectionEvent(legalMoves, selection, end - start));
        System.out.println("Turn " + turn + " time = " + System.currentTimeMillis()); 
        turn++;
        return selection;
	}
	
	private class FindMoveThread extends Thread {
		
		private StateMachine stateMachine;
		private MachineState currentState;
		private Role role;
		private List<Move> legalMoves;
		private Move selection;
		
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
		
		public void run() {
			try {
				legalMoves = stateMachine.getLegalMoves(currentState, role);
			} catch (MoveDefinitionException e) {
				e.printStackTrace();
			}
			int maxLevel = 1;
			Move m = null;
			try {
				while (m == null && !stoppedEarly) {
					done = false;
					//System.out.println("Now searching at depth " + maxLevel);
					m = getMoveClosestToTerminal(stateMachine, currentState, role, 1, maxLevel, null);
					maxLevel++;
				}
				if (stoppedEarly) selection = stateMachine.getRandomMove(currentState, role);
			} catch (MoveDefinitionException e) {
				e.printStackTrace();
			} catch (TransitionDefinitionException e) {
				e.printStackTrace();
			}
			if (!stoppedEarly) selection = m;
			foundMove = true;
		}
		
		private Move getMoveClosestToTerminal(StateMachine stateMachine, MachineState currentState, Role role, int curLevel, int maxLevel, Move nextMove) throws MoveDefinitionException, TransitionDefinitionException {
			MachineState state = currentState;
			if (!done && !stoppedEarly) {
				for (Move move : stateMachine.getLegalMoves(state, role)) {
					for (List<Move> moveList : stateMachine.getLegalJointMoves(state, role, move)) { 
						if (!done) {
							if (curLevel==1) {
								nextMove = move;
							}
							MachineState nextState = stateMachine.getNextState(state, moveList);
							if (stateMachine.isTerminal(nextState)) {
								//System.out.println("found a terminal state");
								done = true;
							}
							if (curLevel < maxLevel  && !done) {
								getMoveClosestToTerminal(stateMachine, nextState, role, curLevel+1, maxLevel, nextMove);
							}	
						}
					}
				} 
			}
			if (done) {
				//System.out.println("FOUND THE MOVE");
				//System.out.println("curLevel: " + curLevel + "maxLevel: " + maxLevel + "nextMove" + nextMove.toString());
				return nextMove;
			}	
			return null;
		}
		
		private boolean getMinimaxMove(StateMachine stateMachine, MachineState currentState, Role role, int maxLevel) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
			double maxScore = -1;
			selection = null;
			for (Move move : stateMachine.getLegalMoves(currentState, role)) {
				if (stoppedEarly) {
					return false;
				}
				double score = getMinScore(stateMachine, currentState, role, move, 1, maxLevel);
				if (score >= maxScore) {
					maxScore = score;
					selection = move;
				}
			}
			return true;
		}
		
		private double getMinScore(StateMachine machine, MachineState currentState, Role role, Move move, int level, int maxLevel) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
			double minScore = 101;
			for (List<Move> jointMove : machine.getLegalJointMoves(currentState, role, move)) {
				if (stoppedEarly) {
					return minScore;
				}
				MachineState nextState = machine.getNextState(currentState, jointMove);
				double score;
				if (stateValues.containsKey(nextState))
					score = stateValues.get(nextState);
				else {
					score = getMaxScore(machine, nextState, role, level, maxLevel);
					stateValues.put(nextState, score);
				}
				minScore = Math.min(minScore, score);
			}
			return minScore;
		}
		
		private double getMaxScore(StateMachine machine, MachineState state, Role role, int level, int maxLevel) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
			//System.out.println("level " + level);
			if (machine.isTerminal(state))
				return machine.getGoal(state, role);
			if (level == maxLevel) {
				double heuristicScore = heuristic.eval(machine, state, role);
				return heuristicScore;
			}
			double maxScore = -1.0;
			for (Move move : machine.getLegalMoves(state, role)) {
				if (stoppedEarly)
					return maxScore;
				maxScore = Math.max(maxScore, getMinScore(machine, state, role, move, level + 1, maxLevel));
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
		return "CloseGamer";
	}

}
