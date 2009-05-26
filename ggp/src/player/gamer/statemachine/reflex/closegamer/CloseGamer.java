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
	private double best = -1;
	
	//heuristic values
	private static final int numCharges = 2;
	
	
	
	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		System.out.println("START - " + System.currentTimeMillis());
		heuristic = new MonteCarloHeuristic();
		stateValues = new HashMap<MachineState, Double>();
	}

	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		long start = System.currentTimeMillis();
		best = -1;
		
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
        System.out.println("Turn time = " + System.currentTimeMillis()); 
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
				while (!stoppedEarly) {
					//System.out.println("Now searching at depth " + maxLevel);
					getMoveClosestToTerminal(stateMachine, currentState, role, 1, maxLevel, null);
					System.out.println("------------------------");
					if (selection != null) System.out.println("ITERATION " + (maxLevel+1) + " // " + selection.toString());
					System.out.println("");
					maxLevel++;
				}
			} catch (GoalDefinitionException e) {
				e.printStackTrace();
			}
			catch (MoveDefinitionException e) {
				e.printStackTrace();
			} catch (TransitionDefinitionException e) {
				e.printStackTrace();
			}
			foundMove = true;
		}
		
		private double getMoveClosestToTerminal(StateMachine stateMachine, MachineState currentState, Role role, int curLevel, int maxLevel, Move nextMove) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
			MachineState state = currentState;
			double maxScore = -1;
			System.out.println(stateMachine.getLegalMoves(state, role).size());
			if (!stoppedEarly) {
				for (Move move : stateMachine.getLegalMoves(state, role)) {
					if (!stoppedEarly) {
						double minScore = 101;
						System.out.println(stateMachine.getLegalJointMoves(state, role, move).size());
						for (List<Move> moveList : stateMachine.getLegalJointMoves(state, role, move)) { 
							if (!stoppedEarly) {
								double myScore = 101;
								System.out.println("CurLevel = " + curLevel);
								if (curLevel==1) {
									nextMove = move;
									System.out.println("NEXT MOVE : " + nextMove.toString());
								}
								MachineState nextState = stateMachine.getNextState(state, moveList);
								if (stateMachine.isTerminal(nextState)) {
									myScore = stateMachine.getGoal(nextState, role);
									System.out.println("!! " + myScore + " !!");
								}
								else {
									if (curLevel < maxLevel) {
										myScore = getMoveClosestToTerminal(stateMachine, nextState, role, curLevel+1, maxLevel, nextMove);
									}
									else {
										myScore = heuristic.eval(stateMachine, state, role)/2;
									}
								}
								if (myScore<minScore || (myScore==minScore && stateMachine.isTerminal(nextState))) {
									System.out.println("%%% " + moveList.toString());
									minScore = myScore;
								}
								
							}
							System.out.println("|||||");
						}
						System.out.println("Move: " + move.toString() + ": expected score: " + minScore);
						if (minScore > maxScore) {
							maxScore = minScore;
						}
						if (minScore > best) {
							System.out.println("UPDATED SELECTION!");
							if (selection!=null) System.out.println("old move: " + selection.toString());
							System.out.println("old score: " + best);
							best = minScore;
							selection = nextMove;
							// TODO note: new selection shouldn't technically ever be null, debug this
							if (selection != null) {
								System.out.println("new move: " + selection.toString());
								System.out.println("new score: " + best);
							}
						}
					}
				} 
			}
			return maxScore;
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
