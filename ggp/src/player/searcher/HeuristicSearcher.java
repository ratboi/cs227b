package player.searcher;

import java.util.Map;

import player.heuristic.Heuristic;
import util.statemachine.MachineState;
import util.statemachine.Move;
import util.statemachine.Role;
import util.statemachine.StateMachine;
import util.statemachine.exceptions.GoalDefinitionException;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;

public abstract class HeuristicSearcher extends Searcher {
	
	protected Heuristic heuristic;
	protected int maxLevel;
	
	public HeuristicSearcher(StateMachine machine, Map<MachineState, Double> stateValues, Heuristic heuristic) {
		super(machine, stateValues);
		this.heuristic = heuristic;
		maxLevel = 1;
	}
	
	public void setMaxSearchLevel(int level) {
		this.maxLevel = level;
	}

}
