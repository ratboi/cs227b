package player.heuristic;

import util.statemachine.MachineState;
import util.statemachine.StateMachine;
import util.statemachine.Role;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;
import util.statemachine.exceptions.GoalDefinitionException;

public interface Heuristic {

	public static final int MAX_SCORE = 100;
	
	public double eval(StateMachine stateMachine, MachineState state, Role role) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException;
}
