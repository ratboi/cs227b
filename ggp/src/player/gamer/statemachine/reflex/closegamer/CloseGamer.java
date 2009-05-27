package player.gamer.statemachine.reflex.closegamer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private Map<MachineState, CachedTermination> terminatingStates = new HashMap<MachineState, CachedTermination>();
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
		while (!foundMove) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
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
	
	private class CachedTermination {
		double score;
		int distanceToTerminal;
	}
	
	private class FindMoveThread extends Thread {
		
		private StateMachine stateMachine;
		private MachineState currentState;
		private Role role;
		private List<Move> legalMoves;
		private Move selection;
		private Map<Move, Termination> earliestTerminations;
		
		private class Termination {
			protected boolean winning;
			protected int level;
		}
		
		public FindMoveThread(StateMachine stateMachine, MachineState state, Role role) {
			this.stateMachine = stateMachine;
			this.currentState = state;
			this.role = role;
			earliestTerminations = new HashMap<Move, Termination>();
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
			try {
				selectMove(currentState);
				/*
				int maxLevel = 1;
				Move m = null;
				while (!stoppedEarly) {
					//System.out.println("Now searching at depth " + maxLevel);
					getMoveClosestToTerminal(stateMachine, currentState, role, 1, maxLevel, null);
					System.out.println("------------------------");
					if (selection != null) System.out.println("ITERATION " + (maxLevel+1) + " // " + selection.toString());
					System.out.println("");
					maxLevel++;
				}
				*/
			} catch (GoalDefinitionException e) {
				e.printStackTrace();
			} catch (MoveDefinitionException e) {
				e.printStackTrace();
			} catch (TransitionDefinitionException e) {
				e.printStackTrace();
			}
			foundMove = true;
		}
		
		private void selectMove(MachineState currentState) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
			List<Move> legalMoves = stateMachine.getLegalMoves(currentState, role);
			selection = legalMoves.get(0);
			if (legalMoves.size() == 1)
				return;
			
			int maxLevel = 1;
			boolean foundTie = false;
			
			Map<Move, Double> scores = new HashMap<Move, Double>();
			while (!stoppedEarly) {
				earliestTerminations.clear();

				// debug output
				System.out.println("NOW SEARCHING FOR A MOVE USING A MAX LEVEL OF " + maxLevel);
				System.out.println("----------------------------------------------------");
				System.out.println("TerminatingStates contains " + terminatingStates.size() + " elements.\n\n");
				
				double maxScore = 0;
				
				for (Move move : legalMoves) {
					if (!stoppedEarly) {
						System.out.println("Trying move: " + move.toString());
						double score = getMinScore(move, move, currentState, 1, maxLevel);
						System.out.println("score = " + score + "\n");
						if (score > maxScore) {
							maxScore = score;
							selection = move;
						}
					}
				}
				
				// if there's a winning move, pick the one that wins soonest
				int earliestWinLevel = -1;
				for (Move move : legalMoves) {
					if (earliestTerminations.containsKey(move)) {
						Termination termination = earliestTerminations.get(move);
						if (termination.winning && (termination.level < earliestWinLevel || earliestWinLevel == -1)) {
							earliestWinLevel = termination.level;
							selection = move;
						}
					}
				}
				
				if (selection!=null) System.out.println("MOVE = " + selection.toString());
				
				if (earliestWinLevel!=-1) {
					System.out.println("FOUND WINNING MOVE!");
					System.out.println("size of terminations = " + earliestTerminations.size());
					if (earliestTerminations.containsKey(selection)) { 
						System.out.println("Yes, it terminates somewhere.");
						Termination termination = earliestTerminations.get(selection);
						System.out.println("will win in " + termination.level + " turns");
						if (termination.winning) System.out.println("winning move");
					}
					break; 				
				}
				if (earliestTerminations.size() == legalMoves.size()) {
					System.out.println("ALL MOVES LEAD TO NON-WINNING TERMINAL STATES!");
					break;
				}
				// if there are no winning moves, pick the one that loses the slowest
				// if there are no losing terminations recorded, 'selection' will just be based on scores from minimax
				/*
				if (earliestWinLevel == -1) {
					int latestLoseLevel = -1;
					for (Move move : legalMoves) {
						if (earliestTerminations.containsKey(move)) {
							Termination termination = earliestTerminations.get(move);
							if (termination.level > latestLoseLevel) {
								latestLoseLevel = termination.level;
								selection = move;
							}
						}
					}
				}
				*/
				
				// TODO how to remember which was the winning move if tracking foundWinning and foundLosing?
				maxLevel++;
			}
		}
		
		private double getMinScore(Move initialMove, Move latestMove, MachineState currentState, int curLevel, int maxLevel) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
			double minScore = 101;
			List<List<Move> > jointMoves = stateMachine.getLegalJointMoves(currentState, role, latestMove);
			MachineState chosenNextState = null;
			boolean choseTerminalState = false;
			for (List<Move> jointMove : jointMoves) {
				if (!stoppedEarly) {
					MachineState nextState = stateMachine.getNextState(currentState, jointMove);
					
					// debug output
//					for (int i = 0; i < curLevel; i++)
//						System.out.print("\t");
//					System.out.println("Trying joint move: " + jointMove.toString());
//					for (int i = 0; i < curLevel; i++)
//						System.out.print("\t");
//					System.out.println("Got this state: " + nextState.toString());
					
					// figure out a score for the nextState we're testing
					double score;
					boolean isTerminal = false;
					if (terminatingStates.containsKey(nextState)) {
						score = terminatingStates.get(nextState).score;
						isTerminal = true;
					} else if (stateMachine.isTerminal(nextState)) {
						score = stateMachine.getGoal(nextState, role);
						isTerminal = true;
					} else if (curLevel == maxLevel) {
						score = heuristic.eval(stateMachine, nextState, role) / 2 - 1;
						//score = -score - 1; // TODO how to compare scores if one is always the inverse minus one?
						//System.out.println("Using heuristic for score " + score);
					} else {
						score = getMaxScore(initialMove, nextState, curLevel + 1, maxLevel); // TODO fix this
						//System.out.println("Recursing to level " + (curLevel + 1));
					}
					
					// update minScore if the current score we're testing is better
					if (score <= minScore) {
						minScore = score;
						chosenNextState = nextState;
						choseTerminalState = isTerminal;
					}
				}
			}
			
			if (choseTerminalState) {
				System.out.println("%%%Found Terminal State%%% - score: " + minScore);
				
				// store in cache if it isn't already in there
				if (terminatingStates.containsKey(chosenNextState)) {
					//add current state
					CachedTermination cachedNext = new CachedTermination();
					cachedNext.score = minScore;
					cachedNext.distanceToTerminal = 0;
					terminatingStates.put(chosenNextState, cachedNext);
				}
				
				// update earliest termination for the move that started it all
				if (!earliestTerminations.containsKey(initialMove) || earliestTerminations.get(initialMove).level > curLevel + terminatingStates.get(chosenNextState).distanceToTerminal) {
					Termination termination = new Termination();
					termination.winning = (minScore == 100);
					termination.level = curLevel + terminatingStates.get(chosenNextState).distanceToTerminal;
					earliestTerminations.put(initialMove, termination);
				}
			}
			
			
			// we know chosen next state is terminal, so we should propagate that up to currentState as well
			if (terminatingStates.containsKey(chosenNextState)) {
				CachedTermination cachedTermination = new CachedTermination();
				cachedTermination.score = minScore;
				cachedTermination.distanceToTerminal = terminatingStates.get(chosenNextState).distanceToTerminal + 1;
				terminatingStates.put(currentState, cachedTermination);
			}
			
			return minScore;
		}
		
		private double getMaxScore(Move initialMove, MachineState currentState, int curLevel, int maxLevel) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
			double maxScore = 0;
			List<Move> legalMoves = stateMachine.getLegalMoves(currentState, role);
			for (Move move : legalMoves) {
				double score = getMinScore(initialMove, move, currentState, curLevel, maxLevel);
				maxScore = Math.max(score, maxScore);
			}
			return maxScore;
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
