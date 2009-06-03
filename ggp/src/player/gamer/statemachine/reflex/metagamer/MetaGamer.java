package player.gamer.statemachine.reflex.metagamer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.Timer;

import apps.player.detail.DetailPanel;

import player.heuristic.*;
import player.metagame.*;
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

public class MetaGamer extends StateMachineGamer {

	public final int MAX_LEVEL = 1;
	public static final int BUFFER_TIME = 50;
	private Heuristic heuristic;
	private Map<MachineState, Double> stateValues;
	private boolean foundMove = false;
	private boolean stoppedEarly = false;
	private MoveBook moveBook;
	
	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
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
		moveBook = new MoveBook(getStateMachine(), getRole());
		moveBook.startOpenBookThread(getCurrentState(), heuristic, timeout);
		while (System.currentTimeMillis() < timeout);
	}

	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		//System.out.println("this is the timeout: " + timeout);
		long start = System.currentTimeMillis();
		//System.out.println("this is the start: " + start);

		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stoppedEarly = true;
				//System.out.println("Early Termination!");
			}
		};
		Timer t = new Timer((int) timeout - (int) start - BUFFER_TIME, taskPerformer);
		t.setRepeats(false);
		t.start();
		
		FindMoveThread move = new FindMoveThread();
		move.start();
		while (!foundMove);
		t.stop();
        foundMove = false;
        stoppedEarly = false;
		
		Move selection = move.getSelection();
		List<Move> legalMoves = move.getLegalMoves();
		
		long end = System.currentTimeMillis();
		//System.out.println("this is the end: " + end);
        notifyObservers(new ReflexMoveSelectionEvent(legalMoves, selection, end - start));
        //System.out.println("about to return the selection\n");
        return selection;
	}
	
	public class FindMoveThread extends Thread {
		private Move selection;
		private List<Move> legalMoves;
		private StateMachineGamer gamer;
		private int maxLevel = 1;
		
		public Move getSelection() {
			return selection;
		}
		
		public List<Move> getLegalMoves() {
			return legalMoves;
		}
		
	    public void run() {
	    	try {
				legalMoves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
			} catch (MoveDefinitionException e1) {}
			
			try {
				while (!stoppedEarly) {
					if (moveBook.openBook.containsKey(getCurrentState())) {
						System.out.println("found a move in the open book, skipping search, whee");
						selection = moveBook.openBook.get(getCurrentState());
						break;
					}
					selection = getMinimaxMove(getStateMachine(), getCurrentState(), getRole(), maxLevel);
					maxLevel++;
				}
			} catch (GoalDefinitionException e) {
			} catch (MoveDefinitionException e) {
			} catch (TransitionDefinitionException e) {}
			
			foundMove = true;
	    }
		
		private Move getMinimaxMove(StateMachine stateMachine, MachineState currentState, Role role, int maxLevel) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
			double maxScore = -1;
			Move selection = null;
			for (Move move : stateMachine.getLegalMoves(currentState, role)) {
				if (stoppedEarly) {
					return selection;
				}
				double score = getMinScore(stateMachine, currentState, role, move, 0, maxLevel);
				if (score >= maxScore) {
					maxScore = score;
					selection = move;
				}
			}
			return selection;
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
					score = getMaxScore(machine, nextState, role, level + 1, maxLevel);
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
			if (level > maxLevel) {
				double heuristicScore = heuristic.eval(machine, state, role, gamer);
				//System.out.println("heuristic score " + heuristicScore);
				return heuristicScore;
			}
			double maxScore = -1.0;
			for (Move move : machine.getLegalMoves(state, role)) {
				if (stoppedEarly)
					return maxScore;
				maxScore = Math.max(maxScore, getMinScore(machine, state, role, move, level, maxLevel));
			}
			return maxScore;
		}

	}
	
	@Override
	public StateMachine getInitialStateMachine() {
		return new CachedProverStateMachine();
	}

	@Override
	public String getName() {
		return "MetaGamer";
	}

	@Override
	public DetailPanel getDetailPanel() {
		return new ReflexDetailPanel();
	}

}