package util.statemachine.prover;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import util.gdl.grammar.Gdl;
import util.gdl.grammar.GdlConstant;
import util.gdl.grammar.GdlProposition;
import util.gdl.grammar.GdlRelation;
import util.gdl.grammar.GdlSentence;
import util.prover.Prover;
import util.prover.aima.AimaProver;
import util.statemachine.MachineState;
import util.statemachine.Move;
import util.statemachine.Role;
import util.statemachine.StateMachine;
import util.statemachine.exceptions.GoalDefinitionException;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;
import util.statemachine.prover.query.ProverQueryBuilder;
import util.statemachine.prover.result.ProverResultParser;

public class ProverStateMachine extends StateMachine
{

	private MachineState initialState;
	private Prover prover;
	private List<Role> roles;

	/**
	 * Initialize must be called before using the StateMachine
	 */
	public ProverStateMachine()
	{
		
	}
	
	public void intialize(List<Gdl> description)
	{
		prover = new AimaProver(description);
		roles = computeRoles(description);
		initialState = computeInitialState();
	}

	private MachineState computeInitialState()
	{
		List<GdlSentence> results = prover.askAll(ProverQueryBuilder.getInitQuery(), new ArrayList<GdlSentence>());
		return new ProverResultParser().toState(results);
	}	

	private List<Role> computeRoles(List<Gdl> description)
	{
		List<Role> roles = new ArrayList<Role>();
		for (Gdl gdl : description)
		{
			if (gdl instanceof GdlRelation)
			{
				GdlRelation relation = (GdlRelation) gdl;
				if (relation.getName().getValue().equals("role")) //TODO: check if things like ( role ?player) are legal
				{
					roles.add(new ProverRole((GdlProposition) relation.get(0).toSentence()));
				}
			}
		}

		return roles;
	}

	@Override
	public int getGoal(MachineState state, Role role) throws GoalDefinitionException
	{
		List<GdlSentence> results = prover.askAll(ProverQueryBuilder.getGoalQuery(role), ProverQueryBuilder.getContext(state));

		if (results.size() != 1)
		{
			throw new GoalDefinitionException(state, role);
		}

		try
		{
			GdlRelation relation = (GdlRelation) results.get(0);
			GdlConstant constant = (GdlConstant) relation.get(1);

			return Integer.parseInt(constant.toString());
		}
		catch (Exception e)
		{
			throw new GoalDefinitionException(state, role);
		}
	}

	@Override
	public MachineState getInitialState()
	{
		return initialState;
	}
	
	@Override
	public List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException
	{
		List<GdlSentence> results = prover.askAll(ProverQueryBuilder.getLegalQuery(role), ProverQueryBuilder.getContext(state));

		if (results.size() == 0)
		{
			throw new MoveDefinitionException(state, role);
		}

		return new ProverResultParser().toMoves(results);
	}

	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException
	{
		List<GdlSentence> results = prover.askAll(ProverQueryBuilder.getNextQuery(), ProverQueryBuilder.getContext(state, getRoles(), moves));

		for (GdlSentence sentence : results)
		{
			if (!sentence.isGround())
			{
				throw new TransitionDefinitionException(state, moves);
			}
		}

		return new ProverResultParser().toState(results);
	}	

	@Override
	public List<Role> getRoles()
	{
		return roles;
	}

	@Override
	public boolean isTerminal(MachineState state)
	{
		return prover.prove(ProverQueryBuilder.getTerminalQuery(), ProverQueryBuilder.getContext(state));
	}

	@Override
	public MachineState getMachineStateFromSentenceList(List<GdlSentence> sentenceList) {
		return new ProverMachineState(sentenceList);
	}

	@Override
	public Move getMoveFromSentence(GdlSentence sentence) {
		return new ProverMove(sentence);
	}

	@Override
	public Role getRoleFromProp(GdlProposition proposition) {
		return new ProverRole(proposition);
	}

}
