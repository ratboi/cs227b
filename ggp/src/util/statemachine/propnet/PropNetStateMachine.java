package util.statemachine.propnet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import util.gdl.grammar.Gdl;
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
		initialState = computeInitialState();
		System.out.println(initialState);
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
		update();
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
	
	private void updateOne(Component currComp) {
		for (Component comp : currComp.getOutputs()) {
			if (comp instanceof Proposition) {
				Proposition prop = (Proposition) comp;
				prop.setValue(true);
			}
			if (!(currComp instanceof Transition) && comp.getValue()) {
				updateOne(comp);
			}
		}
	}
	
	private void update() {
		// grab all propositions that are true
		for (Component comp : propnet.getComponents()) {
			if (comp.getValue()) {
				updateOne(comp);
			}
		}
	}

	@Override
	public int getGoal(MachineState state, Role role) throws GoalDefinitionException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public MachineState getInitialState() {
		return initialState;
	}

	@Override
	public List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Role getRoleFromProp(GdlProposition proposition) {
		return new PropNetRole(proposition);
	}

	@Override
	public List<Role> getRoles() {
		return roles;
	}	

	@Override
	public boolean isTerminal(MachineState state) {
		// TODO Auto-generated method stub
		return false;
	}



}
