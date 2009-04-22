package validator;

import java.util.ArrayList;
import java.util.List;

import util.gdl.grammar.Gdl;
import util.observer.Event;
import util.observer.Observer;
import util.observer.Subject;
import util.statemachine.MachineState;
import util.statemachine.Role;
import util.statemachine.StateMachine;
import util.statemachine.exceptions.GoalDefinitionException;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;
import util.statemachine.prover.ProverMachineState;
import util.statemachine.prover.ProverStateMachine;
import validator.exception.MaxDepthException;
import validator.exception.MonotonicityException;

public class HandStepper extends Thread implements Subject, Observer {
	
	private final List<Gdl> description;
	private final List<Observer> observers;
	ProverStateMachine stateMachine;
	List<Role> roles;
	List<ProverMachineState> states = new ArrayList<ProverMachineState>();
	
	public HandStepper(List<Gdl> description)
	{
		observers = null;
		this.description = description;
		stateMachine = new ProverStateMachine();
		stateMachine.intialize(description);
		roles = stateMachine.getRoles();
	}
	
	public void Foo()
	{
		

		try {
			MachineState state = stateMachine.getInitialState();
			for (int depth = 0; !stateMachine.isTerminal(state); depth++)
			{			

				for (int i = 0; i < roles.size(); i++)
				{
					int goal = stateMachine.getGoal(state, roles.get(i));
					
					//goals.set(i, goal);
				}

				state = stateMachine.getRandomNextState(state);
			}
		} catch (GoalDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MoveDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransitionDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addObserver(Observer observer) {
		// TODO Auto-generated method stub
		
	}

	public void notifyObservers(Event event) {
		// TODO Auto-generated method stub
		
	}

	public void observe(Event event) {
		// TODO Auto-generated method stub
		
	}
	
	
}
