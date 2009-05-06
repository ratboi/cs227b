package player.gamer.statemachine.reflex.medium;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
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

public class MediumGamer extends StateMachineGamer {

	public final int MAX_LEVEL = 1;
	public static final int BUFFER_TIME = 50;
	public Heuristic heuristic;
	Map<MachineState, Double> stateValues;
	public long start = -1;
	public long stop = 0;
	public boolean terminated = false;
	
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
	}

	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		start = System.currentTimeMillis();
		FindMoveThread move = new FindMoveThread();
		move.start();
		
		 ActionListener taskPerformer = new ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		          stop = start;
		      }
		  };
		Timer t = new Timer((int)timeout-(int)start - BUFFER_TIME, taskPerformer);
		t.setRepeats(false);
		t.start();
		while (!terminated);
		
		Move selection = move.getSelection();
		List<Move> legalMoves = move.getLegalMoves();
		
		long end = System.currentTimeMillis();
        notifyObservers(new ReflexMoveSelectionEvent(legalMoves, selection, end - start));
        terminated = false;
        return selection;
	}
	
	public class FindMoveThread extends Thread {
		private Move selection;
		private List<Move> legalMoves;
		
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
				selection = getMinimaxMove(getStateMachine(), getCurrentState(), getRole());
			} catch (GoalDefinitionException e) {
			} catch (MoveDefinitionException e) {
			} catch (TransitionDefinitionException e) {}
			
			terminated = true;
	    }

	}
	
	private Move getMinimaxMove(StateMachine stateMachine, MachineState currentState, Role role) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		double maxScore = -1;
		Move selection = null;
		for (Move move : stateMachine.getLegalMoves(currentState, role)) {
			if (start-stop==0) return selection;
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
		long started = System.currentTimeMillis();
		for (List<Move> jointMove : machine.getLegalJointMoves(currentState, role, move)) {
			if (start-stop==0) {
				System.out.println("Early Termination!");
				return minScore;
			}
			MachineState nextState = machine.getNextState(currentState, jointMove);
			double score;
			if (stateValues.containsKey(nextState))
				score = stateValues.get(nextState);
			else {
				score = getMaxScore(machine, nextState, role, level + 1);
				stateValues.put(nextState, score);
			}
			minScore = Math.min(minScore, score);
		}
		return minScore;
	}
	
	private double getMaxScore(StateMachine machine, MachineState state, Role role, int level) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		//System.out.println("level " + level);
		if (machine.isTerminal(state))
			return machine.getGoal(state, role);
		if (level > MAX_LEVEL) {
			double heuristicScore = heuristic.eval(machine, state, role);
			//System.out.println("heuristic score " + heuristicScore);
			return heuristicScore;
		}
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