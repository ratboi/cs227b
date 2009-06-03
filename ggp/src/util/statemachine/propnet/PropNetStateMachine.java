package util.statemachine.propnet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import util.gdl.grammar.Gdl;
import util.gdl.grammar.GdlConstant;
import util.gdl.grammar.GdlPool;
import util.gdl.grammar.GdlProposition;
import util.gdl.grammar.GdlRelation;
import util.gdl.grammar.GdlSentence;
import util.gdl.grammar.GdlTerm;
import util.propnet.architecture.PropNet;
import util.propnet.architecture.component.and.And;
import util.propnet.architecture.component.or.Or;
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
	private List<Component> ors;
	
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
		generateComponentLists();
		
		//simplify the Ands
		boolean modifyingPropnet = true;
		while (modifyingPropnet) {
			modifyingPropnet = simplifyAnds() || simplifyOrs() || simplifySingleAnds() || simplifySingleOrs();
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
	
	private void generateComponentLists() {
		transitions = new ArrayList<Component>();
		ands = new ArrayList<Component>();
		ors = new ArrayList<Component>();
		for (Component comp : propnet.getComponents()) {
			if (comp instanceof Transition && !transitions.contains(comp)) {
				transitions.add(comp);
			}
			if (comp instanceof And && !ands.contains(comp)) {
				ands.add(comp);
			}
			if (comp instanceof Or && !ors.contains(comp)) {
				ors.add(comp);
			}
		}
	}
	
	private List<Component> fakePropnet() {
		Component root0 = new Proposition(new GdlConstant("root0"));
		Component root0b = new Proposition(new GdlConstant("root0b"));
		Component root1 = new Proposition(new GdlConstant("root1"));
		Component root2 = new Proposition(new GdlConstant("root2"));
		Component root3 = new Proposition(new GdlConstant("root3"));
		Component root4 = new Proposition(new GdlConstant("root4"));
		Component or0 = new Or();
		Component or0b = new Or();
		Component or1 = new Or();
		Component or2 = new Or();
		Component or3 = new Or();
		Component end = new Proposition(new GdlConstant("end"));
		
		root0.addOutput(or0);
		root0b.addOutput(or0b);
		root1.addOutput(or1);
		root2.addOutput(or1);
		root3.addOutput(or2);
		root4.addOutput(or2);
		
		or0.addInput(root0);
		or0b.addInput(root0b);
		or1.addInput(root1);
		or1.addInput(root2);
		or2.addInput(root3);
		or2.addInput(root4);
		
		or0.addOutput(or1);
		or0b.addOutput(or1);
		or1.addOutput(or3);
		or2.addOutput(or3);
		
		or1.addInput(or0);
		or1.addInput(or0b);
		or3.addInput(or1);
		or3.addInput(or2);
		
		or3.addOutput(end);
		end.addInput(or3);
		
		List<Component> components = new ArrayList<Component>();
		components.add(root1);
		components.add(root2);
		components.add(root3);
		components.add(root4);
		components.add(root0);
		components.add(root0b);
		components.add(or0);
		components.add(or0b);
		components.add(or1);
		components.add(or2);
		components.add(or3);
		components.add(end);
		
		return components;
		
	}
	
	private boolean simplifySingleAnds() {
		for (Component and : ands) {
			boolean foundOneInput = true;
			if (and.getInputs().size() != 1) {
				foundOneInput = false;
				break;
			}
			if (foundOneInput) {
				List<Component> oldComps = propnet.getComponents();
				System.out.println("------ACTUALLY PRUNING PROPNET!!--------");
				Component input = and.getInputs().get(0);
				Component output = and.getOutputs().get(0);
				
				input.removeOutput(and);
				output.removeInput(and);
				input.addOutput(output);
				output.addInput(input);
				
				if (oldComps.contains(and)) {
					oldComps.remove(and);
				}
				propnet = new PropNet(oldComps);
				generateComponentLists();
				return true;
			}
		}
		return false;
	}
	
	private boolean simplifySingleOrs() {
		for (Component or : ors) {
			boolean foundOneInput = true;
			if (or.getInputs().size() != 1) {
				foundOneInput = false;
				break;
			}
			if (foundOneInput) {
				List<Component> oldComps = propnet.getComponents();
				System.out.println("------ACTUALLY PRUNING PROPNET!!--------");
				Component input = or.getInputs().get(0);
				Component output = or.getOutputs().get(0);
				
				input.removeOutput(or);
				output.removeInput(or);
				input.addOutput(output);
				output.addInput(input);
				
				if (oldComps.contains(or)) {
					oldComps.remove(or);
				}
				propnet = new PropNet(oldComps);
				generateComponentLists();
				return true;
			}
		}
		return false;
	}
	
	private boolean simplifyAnds() {
		for (Component and : ands) {
			List<Component> inputs = and.getInputs();
			Component output = and.getOutputs().get(0);
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
				List<Component> oldComps = propnet.getComponents();
				System.out.println("------ACTUALLY PRUNING PROPNET!!--------");
				Component newAnd = new And();
				for (Component inputInput : allInputInputs) {
					// create a new And component that takes everything in allInputInputs as inputs
					newAnd.addInput(inputInput);
					// cut out the old And component and its immediate inputs
					if (oldComps.contains(inputInput.getOutputs().get(0))) {
						oldComps.remove(inputInput.getOutputs().get(0));
					}
					inputInput.removeOutput(inputInput.getOutputs().get(0));
					// put in the new one
					inputInput.addOutput(newAnd);
				}
				// and has the same outputs as the original And component
				newAnd.addOutput(and.getOutputs().get(0));
				output.removeInput(and);
				if (oldComps.contains(and)) {
					oldComps.remove(and);
				}
				output.addInput(newAnd);
				oldComps.add(newAnd);
				propnet = new PropNet(oldComps);
				generateComponentLists();
				return true;
			}
		}
		return false;
	}
	
	private boolean simplifyOrs() {
		for (Component or : ors) {
			List<Component> inputs = or.getInputs();
			Component output = or.getOutputs().get(0);
			List<Component> allInputInputs = new ArrayList<Component>();
			boolean allInputsAreOrs = true;
			for (Component input : inputs) {
				if (!(input instanceof Or)) {
					allInputsAreOrs = false;
					break;
				}
				allInputInputs.addAll(input.getInputs());
			}
			if (allInputsAreOrs) {
				List<Component> oldComps = propnet.getComponents();
				System.out.println("------ACTUALLY PRUNING PROPNET!!--------");
				Component newOr = new Or();
				for (Component inputInput : allInputInputs) {
					// create a new Or component that takes everything in allInputInputs as inputs
					newOr.addInput(inputInput);
					// cut out the old Or component and its immediate inputs
					if (oldComps.contains(inputInput.getOutputs().get(0))) {
						oldComps.remove(inputInput.getOutputs().get(0));
					}
					inputInput.removeOutput(inputInput.getOutputs().get(0));
					// put in the new one
					inputInput.addOutput(newOr);
				}
				// Or has the same outputs as the original Or component
				newOr.addOutput(or.getOutputs().get(0));
				output.removeInput(or);
				if (oldComps.contains(or)) {
					oldComps.remove(or);
				}
				output.addInput(newOr);
				oldComps.add(newOr);
				propnet = new PropNet(oldComps);
				generateComponentLists();
				return true;
			}
		}
		return false;
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
