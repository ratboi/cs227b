package player.heuristic;

import util.statemachine.MachineState;
import util.statemachine.Role;
import util.statemachine.StateMachine;
import util.statemachine.exceptions.GoalDefinitionException;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;

public class LinearCombinedHeuristic implements Heuristic {
	
	@Override
	public double eval(StateMachine stateMachine, MachineState state, Role role)
			throws MoveDefinitionException, TransitionDefinitionException,
			GoalDefinitionException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void addHeuristic(Heuristic h) {
		
	}

}
