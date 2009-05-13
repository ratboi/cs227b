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
import util.propnet.architecture.component.transition.Transition;
import util.propnet.architecture.component.proposition.Proposition;
import util.propnet.architecture.component.Component;
import util.propnet.factory.PropNetFactory;
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
public final class PropNetStateMachine extends StateMachine
{
	private PropNet propnet = null;
	private MachineState initialState;
	private List<Role> roles;
	
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
		propnet = factory.create(description);
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
	
	private MachineState computeInitialState() {
		clearValues();
		propnet.getInitProposition().setValue(true);
		update(false);
		List<GdlSentence> stateTerms = new ArrayList<GdlSentence>();
		for (GdlTerm key : propnet.getBasePropositions().keySet()) {
			if (propnet.getBasePropositions().get(key).getValue()) {
				stateTerms.add(key.toSentence());
			}
		}
		return new PropNetMachineState(stateTerms);
	}
	
	private void clearValues() {
		for (Proposition prop : propnet.getPropositions()) {
			prop.setValue(false);
		}
	}
	
	private void updateOne(Component currComp, List<Component> transitions) {
		for (Component comp : currComp.getOutputs()) {
			if (comp instanceof Proposition) {
				Proposition prop = (Proposition) comp;
				prop.setValue(true);
			}
			if (comp instanceof Transition) {
				transitions.add(comp);
			}
			if (!(comp instanceof Transition) && comp.getValue()) {
				updateOne(comp, transitions);
			}
		}
	}
	
	private void update(boolean clearOld) {
		List<Component> transitions = new ArrayList<Component>();
		// grab all propositions that are true
		for (Component comp : propnet.getComponents()) {
			if (comp.getValue()) {
				updateOne(comp, transitions);
			}
		}
		if (clearOld) {
			clearValues();
			for (Component transition : transitions) {
				for (Component comp : transition.getOutputs()) {
					if (comp instanceof Proposition) {
						((Proposition)comp).setValue(true);
					}
				}
			}
		}
	}

	@Override
	public int getGoal(MachineState state, Role role) throws GoalDefinitionException {
		setState(state);
		update(false);
		String roleName = role.getName().toString();
		List<Proposition> goalProps = propnet.getGoalPropositions().get(roleName);
		Proposition goalProp = null;
		int numTrueGoalProps = 0;
		for (Proposition prop : goalProps) {
			if (prop.getValue()) {
				goalProp = prop;
				numTrueGoalProps++;
			}
		}
		if (numTrueGoalProps != 1) {
			throw new GoalDefinitionException(state, role);
		}
		try {
			GdlTerm value = goalProp.getName().toSentence().get(1);
			return Integer.parseInt(value.toString());
		} catch (Exception e) {
			throw new GoalDefinitionException(state, role);
		}
	}

	@Override
	public MachineState getInitialState() {
		return initialState;
	}

	@Override
	public List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException {
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
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException {
		setState(state);
		setMoves(moves);
		update(true);
		List<GdlSentence> stateTerms = new ArrayList<GdlSentence>();
		for (GdlTerm key : propnet.getBasePropositions().keySet()) {
			if (propnet.getBasePropositions().get(key).getValue()) {
				stateTerms.add(key.toSentence());
			}
		}
		return new PropNetMachineState(stateTerms);
	}

	@Override
	public Role getRoleFromProp(GdlProposition proposition) {
		return new PropNetRole(proposition);
	}

	@Override
	public List<Role> getRoles() {
		return roles;
	}
	
	private void setMoves(List<Move> moves) {
		for (int i = 0; i < moves.size(); i++) {
			GdlTerm doesState = GdlPool.getRelation(GdlPool.getConstant("does"), new GdlTerm[] { roles.get(i).getName().toTerm(), moves.get(i).getContents().toTerm() }).toTerm();
			propnet.getInputPropositions().get(doesState).setValue(true);
		}
	}
	
	private void setState(MachineState state) {
		clearValues();
		List<GdlSentence> contents = state.getContents();
		Map<GdlTerm, Proposition> baseProps = propnet.getBasePropositions();
		for (GdlSentence sentence : contents) {
			Proposition prop = baseProps.get(sentence.toTerm());
			prop.setValue(true);
		}
	}

	@Override
	public boolean isTerminal(MachineState state) {
		setState(state);
		update(false);
		return propnet.getTerminalProposition().getValue();
	}



}
