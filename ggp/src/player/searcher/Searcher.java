package player.searcher;

import util.statemachine.MachineState;
import util.statemachine.Move;
import util.statemachine.Role;
import util.statemachine.exceptions.GoalDefinitionException;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;

public abstract class Searcher {
	
	public abstract Move findMove(MachineState state, Role role) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException;

}
