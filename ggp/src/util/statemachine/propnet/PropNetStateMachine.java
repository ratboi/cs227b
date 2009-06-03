package util.statemachine.propnet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import util.gdl.grammar.Gdl;
import util.gdl.grammar.GdlPool;
import util.gdl.grammar.GdlProposition;
import util.gdl.grammar.GdlRelation;
import util.gdl.grammar.GdlSentence;
import util.gdl.grammar.GdlTerm;
import util.propnet.architecture.PropNet;
import util.propnet.architecture.component.and.And;
import util.propnet.architecture.component.transition.Transition;
import util.propnet.architecture.component.proposition.Proposition;
import util.propnet.architecture.component.constant.Constant;
import util.propnet.architecture.component.Component;
import util.propnet.factory.PropNetFactory;
import util.propnet.factorer.PropNetFactorer;
import util.statemachine.MachineState;
import util.statemachine.Move;
import util.statemachine.Role;
import util.statemachine.StateMachine;
import util.statemachine.exceptions.GoalDefinitionException;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;
import util.statemachine.prover.ProverRole;


/**
 * The PropNetStateMachine class is an implementation of a StateMachine, backed
 * by a Propositional Network.
 */
public class PropNetStateMachine extends StateMachine
{
	private PropNet propnet = null;
	private MachineState initialState;
	private List<Role> roles;
	private List<Component> transitions;
	private List<Component> ands;
	
	/**
	 * Your JavaDoc here.
	 * 
	 * @param propNet
	 */
	public PropNetStateMachine()
	{
	}
	
	@Override
	public void intialize(List<Gdl> description) {
		PropNetFactory factory = new PropNetFactory();
		//PropNetFactorer factorer = new PropNetFactorer();
		propnet = factory.create(description);
		//List<PropNet> propnets = factorer.factor(propnet);
		//propnet = propnets.get(0);
		//System.out.println(propnets.get(0).toString());
		//System.out.println(propnet.toString());
		
		//create transitions and ands list
		transitions = new ArrayList<Component>();
		ands = new ArrayList<Component>();
		for (Component comp : propnet.getComponents()) {
			if (comp instanceof Transition && !transitions.contains(comp)) {
				transitions.add(comp);
			}
			if (comp instanceof And && !ands.contains(comp)) {
				ands.add(comp);
			}
		}
		
		roles = computeRoles(description);
		System.out.println("printing roles");
		for (Role role : roles)
			System.out.println(role.getName().toString());
		System.out.println("done printing roles");
		initialState = computeInitialState();

		// System.out.println(initialState);
		/*try {
			System.out.println(getLegalMoves(initialState, roles.get(0)));
		} catch (Exception e) {
			
		}

		System.out.println(initialState);
		System.out.println("--------------");
		for (Role role : roles) {
			String name = role.getName().toString();
			List<Proposition> goalProps = propnet.getGoalPropositions().get(name);
			for (Proposition prop : goalProps) {
				System.out.println(prop.getName().toSentence().toString());
				System.out.println(prop.getName().toSentence().get(1));
			}
		}*/
	}
	
	private void simplifyAnds() {
		for (Component and : ands) {
			List<Component> inputs = and.getInputs();
			List<Component> allInputInputs = new ArrayList<Component>();
			boolean allInputsAreAnds = true;
			for (Component input : inputs) {
				if (!(input instanceof And)) {
					allInputsAreAnds = false;
					break;
				}
				allInputInputs.addAll(input.getInputs());
			}
			if (allInputsAreAnds) {
				// TODO
				// create a new And component that takes everything in allInputInputs as inputs
				// and has the same outputs as the original And component
				// cut out the old And component and its immediate inputs
					// this also entails modifying 'ands' in this class, and some stuff in 'propnet'...
				// put in the new one
			}
		}
	}

	// mostly copied from ProverStateMachine
	private List<Role> computeRoles(List<Gdl> description)
	{
		List<Role> roles = new ArrayList<Role>();
		for (Gdl gdl : description)
		{
			if (gdl instanceof GdlRelation)
			{
				GdlRelation relation = (GdlRelation) gdl;
				if (relation.getName().getValue().equals("role"))
				{
					roles.add(new PropNetRole((GdlProposition) relation.get(0).toSentence()));
				}
			}
		}

		return roles;
	}
	
	private synchronized MachineState computeInitialState() {
		System.out.println("----COMPUTE INITIAL STATE---");
		clearValues();
		propnet.getInitProposition().setValue(true);
		update(true);
		List<GdlSentence> stateTerms = new ArrayList<GdlSentence>();
		for (GdlTerm key : propnet.getBasePropositions().keySet()) {
			if (propnet.getBasePropositions().get(key).getValue()) {
				stateTerms.add(key.toSentence());
			}
		}
		return new PropNetMachineState(stateTerms);
	}
	
	private synchronized void clearValues() {
		for (Proposition prop : propnet.getPropositions()) {
			prop.setValue(false);
		}
	}
	
	private synchronized void updateOne(Component currComp) {
		if (!currComp.getValue() || currComp.equals(propnet.getTerminalProposition())) {
			return;
		}
		for (Component comp : currComp.getOutputs()) {
			if (comp instanceof Proposition) {
				Proposition prop = (Proposition) comp;
				//System.out.println("UPDATEONE: Truing this prop" + prop);
				prop.setValue(true);
			}
			if (comp instanceof Transition && !transitions.contains(comp)) {
				transitions.add(comp);
			}
			if (!(comp instanceof Transition) && comp.getValue()) {
				updateOne(comp);
			}
		}
	}
	
	private synchronized void update(boolean clearOld) {
		//System.out.println("---------------STARTING UPDATE-------------");
		/*List<Component> trueComps = new ArrayList<Component>();
		// grab all propositions that are true
		for (Component comp : propnet.getComponents()) {
			//if (comp.getValue() && (comp instanceof Proposition || comp instanceof Constant)) {
			if (comp.getValue()) {
				trueComps.add(comp);
			}
		}
		for (Component comp : trueComps) {
			System.out.println("A TRUE COMP: " + comp);
			updateOne(comp);
		}*/
		boolean updated = true;
		//keep looping until no more props need to be updated
		while (updated) {
			updated = false;
			for (Proposition prop : propnet.getPropositions()) {
				//System.out.println(prop);
				if (prop.getInputs().size() > 0) {
					Component parent = prop.getInputs().get(0);
					if (!(parent instanceof Transition) && parent.getValue() != prop.getValue()) {
						//System.out.println("UPDATEONE: Setting this prop " + prop + " to " + parent.getValue());
						prop.setValue(parent.getValue());
						updated = true;
						//break;
					}
				}
			}
		}
		if (clearOld) {
			List<Component> trueTransitions = new ArrayList<Component>();
			for (Component transition : transitions) {
				if (transition.getValue()) {
					trueTransitions.add(transition);
				}
			}
			clearValues();
			for (Component transition : trueTransitions) {
				for (Component comp : transition.getOutputs()) {
					if (comp instanceof Proposition) {
						//System.out.println("CLEAROLD: " + comp + " set to true.");
						((Proposition)comp).setValue(true);
					}
				}
			}
		}
	}

	@Override
	public synchronized int getGoal(MachineState state, Role role) throws GoalDefinitionException {
		setState(state);
		update(false);
		String roleName = role.getName().toString();
		List<Proposition> goalProps = propnet.getGoalPropositions().get(roleName);
		Proposition goalProp = null;
		int numTrueGoalProps = 0;
		for (Proposition prop : goalProps) {
			//System.out.println(prop.getName().toString());
			if (prop.getValue()) {
				//System.out.println("the above proposition is true!");
				goalProp = prop;
				numTrueGoalProps++;
			}
			//System.out.println("-------");
		}
		if (numTrueGoalProps != 1) {
			System.out.println("there are " + numTrueGoalProps + " true goal props instead of just 1");
			//throw new GoalDefinitionException(state, role);
			return 0;
		}
		GdlTerm value = goalProp.getName().toSentence().get(1);
		try {
			return Integer.parseInt(value.toString());
		} catch (Exception e) {
			System.out.println(value.toString() + " couldn't be parsed as an integer");
			throw new GoalDefinitionException(state, role);
		}
	}

	@Override
	public MachineState getInitialState() {
		return initialState;
	}

	@Override
	public synchronized List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException {
		List<Move> moves = new ArrayList<Move>();
		setState(state);
		update(false);
		String roleName = role.getName().toString();
		for (Proposition prop : propnet.getLegalPropositions().get(roleName)) {
			if (prop.getValue()) {
				moves.add(new PropNetMove(prop.getName().toSentence().get(1).toSentence()));
			}
		}
		return moves;
	}

	@Override
	public MachineState getMachineStateFromSentenceList(List<GdlSentence> sentenceList) {
		return new PropNetMachineState(sentenceList);
	}

	@Override
	public Move getMoveFromSentence(GdlSentence sentence) {
		return new PropNetMove(sentence);
	}

	@Override
	public synchronized MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException {
		setState(state);
		List<Proposition> newlyTrueProps = setMoves(moves);
		update(true);
		List<GdlSentence> stateTerms = new ArrayList<GdlSentence>();
		for (GdlTerm key : propnet.getBasePropositions().keySet()) {
			if (propnet.getBasePropositions().get(key).getValue()) {
				stateTerms.add(key.toSentence());
			}
		}
		//return new PropNetMachineState(stateTerms);
		MachineState newState = new PropNetMachineState(stateTerms);
		//System.out.println("OLD STATE: " + state);
		//System.out.println("MOVES: " + moves);
		//System.out.println("NEW STATE: " + newState);
		return newState;
	}

	@Override
	public Role getRoleFromProp(GdlProposition proposition) {
		return new PropNetRole(proposition);
	}

	@Override
	public List<Role> getRoles() {
		return roles;
	}
	
	private synchronized List<Proposition> setMoves(List<Move> moves) {
		List<Proposition> newlyTrueProps = new ArrayList<Proposition>();
		for (int i = 0; i < moves.size(); i++) {
			GdlTerm doesState = GdlPool.getRelation(GdlPool.getConstant("does"), new GdlTerm[] { roles.get(i).getName().toTerm(), moves.get(i).getContents().toTerm() }).toTerm();
			Proposition prop = propnet.getInputPropositions().get(doesState);
			prop.setValue(true);
			newlyTrueProps.add(prop);
		}
		return newlyTrueProps;
	}
	
	private synchronized void setState(MachineState state) {
		clearValues();
		List<GdlSentence> contents = state.getContents();
		Map<GdlTerm, Proposition> baseProps = propnet.getBasePropositions();
		for (GdlSentence sentence : contents) {
			//System.out.println(sentence);
			Proposition prop = baseProps.get(sentence.toTerm());
			prop.setValue(true);
		}
	}

	@Override
	public synchronized boolean isTerminal(MachineState state) {
		setState(state);
		update(false);
		return propnet.getTerminalProposition().getValue();
	}



}
