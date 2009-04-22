package util.statemachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import util.gdl.grammar.Gdl;
import util.gdl.grammar.GdlProposition;
import util.gdl.grammar.GdlSentence;
import util.statemachine.exceptions.GoalDefinitionException;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;

/**
 * Provides the base class for all state machine implementations.
 *	As of this comment there is only one, the ProverStateMachine.
 *	TODO: Add PropnetStateMachine  TODO: Write  CompiledPropnetStateMachine
 */
public abstract class StateMachine
{
	//Commented out methods are provided for ease of reference and are defined below
	public abstract void intialize(List<Gdl> description);
	
	public abstract int getGoal(MachineState state, Role role) throws GoalDefinitionException;
	public abstract boolean isTerminal(MachineState state);
	
	public abstract List<Role> getRoles();
	public abstract MachineState getInitialState();

	//public List<List<Move>> getLegalJointMoves(MachineState state) throws MoveDefinitionException;
	//public List<List<Move>> getLegalJointMoves(MachineState state, Role role, Move move) throws MoveDefinitionException;

	public abstract List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException;

	public abstract MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException;
	//public List<MachineState> getNextStates(MachineState state) throws MoveDefinitionException, TransitionDefinitionException;
	//public Map<Move, List<MachineState>> getNextStates(MachineState state, Role role) throws MoveDefinitionException, TransitionDefinitionException;

	//public List<Move> getRandomJointMove(MachineState state) throws MoveDefinitionException;
	//public List<Move> getRandomJointMove(MachineState state, Role role, Move move) throws MoveDefinitionException;
	//public Move getRandomMove(MachineState state, Role role) throws MoveDefinitionException;

	//public MachineState getRandomNextState(MachineState state) throws MoveDefinitionException, TransitionDefinitionException;
	//public MachineState getRandomNextState(MachineState state, Role role, Move move) throws MoveDefinitionException, TransitionDefinitionException;

	/**
	 * Optional function.  Some StateMachine implementations may
	 * want to do some work (like trimming a cache) once per move.
	 */
	public void doPerMoveWork() {}
	
	//The following functions are included in the StateMachine interface so
	//implementations which use alternative Role/Move/State representations
	//can look up/compute what some Gdl corresponds to in their representation
	public abstract Role getRoleFromProp(GdlProposition proposition);	
	public abstract Move getMoveFromSentence(GdlSentence sentence);	
	public abstract MachineState getMachineStateFromSentenceList(List<GdlSentence> sentenceList);

	//============================================
	//Implementations of convenience methods
	//============================================
	public List<List<Move>> getLegalJointMoves(MachineState state) throws MoveDefinitionException
	{
		List<List<Move>> legals = new ArrayList<List<Move>>();
		for (Role role : getRoles())
		{
			legals.add(getLegalMoves(state, role));
		}

		List<List<Move>> crossProduct = new ArrayList<List<Move>>();
		crossProductLegalMoves(legals, crossProduct, new LinkedList<Move>());

		return crossProduct;
	}
	
	public List<List<Move>> getLegalJointMoves(MachineState state, Role role, Move move) throws MoveDefinitionException
	{
		List<List<Move>> legals = new ArrayList<List<Move>>();
		for (Role r : getRoles())
		{
			if (r.equals(role))
			{
				List<Move> m = new ArrayList<Move>();
				m.add(move);
				legals.add(m);
			}
			else
			{
				legals.add(getLegalMoves(state, r));
			}
		}

		List<List<Move>> crossProduct = new ArrayList<List<Move>>();
		crossProductLegalMoves(legals, crossProduct, new LinkedList<Move>());

		return crossProduct;
	}
	
	public List<MachineState> getNextStates(MachineState state) throws MoveDefinitionException, TransitionDefinitionException
	{
		List<MachineState> nextStates = new ArrayList<MachineState>();
		for (List<Move> move : getLegalJointMoves(state))
		{
			nextStates.add(getNextState(state, move));
		}

		return nextStates;
	}
	
	public Map<Move, List<MachineState>> getNextStates(MachineState state, Role role) throws MoveDefinitionException, TransitionDefinitionException
	{
		Map<Move, List<MachineState>> nextStates = new HashMap<Move, List<MachineState>>();
		Map<Role, Integer> roleIndices = getRoleIndices();
		for (List<Move> moves : getLegalJointMoves(state))
		{
			Move move = moves.get(roleIndices.get(role));
			if (!nextStates.containsKey(move))
			{
				nextStates.put(move, new ArrayList<MachineState>());
			}
			nextStates.get(move).add(getNextState(state, moves));
		}

		return nextStates;
	}
	
	protected void crossProductLegalMoves(List<List<Move>> legals, List<List<Move>> crossProduct, LinkedList<Move> partial)
	{
		if (partial.size() == legals.size())
		{
			crossProduct.add(new ArrayList<Move>(partial));
		}
		else
		{
			for (Move move : legals.get(partial.size()))
			{
				partial.addLast(move);
				crossProductLegalMoves(legals, crossProduct, partial);
				partial.removeLast();
			}
		}
	}
	
	private Map<Role,Integer> roleIndices = null;
	public Map<Role, Integer> getRoleIndices()
	{
		if(roleIndices == null)
		{
			roleIndices = new HashMap<Role, Integer>();
			List<Role> roles = getRoles();
			for (int i = 0; i < roles.size(); i++)
			{
				roleIndices.put(roles.get(i), i);
			}
		}

		return roleIndices;
	}
	
	public List<Move> getRandomJointMove(MachineState state) throws MoveDefinitionException
	{
		List<Move> random = new ArrayList<Move>();
		for (Role role : getRoles())
		{
			random.add(getRandomMove(state, role));
		}

		return random;
	}

	public List<Move> getRandomJointMove(MachineState state, Role role, Move move) throws MoveDefinitionException
	{
		List<Move> random = new ArrayList<Move>();
		for (Role r : getRoles())
		{
			if (r.equals(role))
			{
				random.add(move);
			}
			else
			{
				random.add(getRandomMove(state, r));
			}
		}

		return random;
	}

	public Move getRandomMove(MachineState state, Role role) throws MoveDefinitionException
	{
		List<Move> legals = getLegalMoves(state, role);
		return legals.get(new Random().nextInt(legals.size()));
	}

	public MachineState getRandomNextState(MachineState state) throws MoveDefinitionException, TransitionDefinitionException
	{
		List<Move> random = getRandomJointMove(state);
		return getNextState(state, random);
	}
	
	public MachineState getRandomNextState(MachineState state, Role role, Move move) throws MoveDefinitionException, TransitionDefinitionException
	{
		List<Move> random = getRandomJointMove(state, role, move);
		return getNextState(state, random);
	}
}
