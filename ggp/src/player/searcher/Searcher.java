package player.searcher;

import java.util.Map;
import java.util.HashMap;

import util.statemachine.MachineState;
import util.statemachine.Move;
import util.statemachine.Role;
import util.statemachine.StateMachine;
import util.statemachine.exceptions.GoalDefinitionException;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;

public abstract class Searcher {

	protected static final int MIN_SCORE = 0;
	protected static final int MAX_SCORE = 100;
	protected StateMachine machine;
	protected Map<MachineState, Double> stateValues;
	
	/**
	 * Constructs new Searcher.  Assumes StateMachine and Map arguments have
	 * already been constructed.
	 * 
	 * @param machine
	 * @param stateValues
	 */
	public Searcher(StateMachine machine, Map<MachineState, Double> stateValues) {
		this.machine = machine;
		this.stateValues = stateValues;
	}
	
	public abstract Move findMove(MachineState state, Role role) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException;

}
