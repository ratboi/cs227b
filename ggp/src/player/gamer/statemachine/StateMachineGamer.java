package player.gamer.statemachine;

import java.util.ArrayList;
import java.util.List;

import player.gamer.Gamer;
import player.gamer.exception.MetaGamingException;
import player.gamer.exception.MoveSelectionException;
import util.gdl.grammar.GdlSentence;
import util.statemachine.MachineState;
import util.statemachine.Move;
import util.statemachine.Role;
import util.statemachine.StateMachine;
import util.statemachine.exceptions.GoalDefinitionException;
import util.statemachine.exceptions.MoveDefinitionException;
import util.statemachine.exceptions.TransitionDefinitionException;

public abstract class StateMachineGamer extends Gamer
{

	private MachineState currentState;
	private Role role;
	private StateMachine stateMachine;
	
	public boolean isStopped() {
		return false;
	}

	public MachineState getCurrentState()
	{
		return currentState;
	}

	public Role getRole()
	{
		return role;
	}

	public StateMachine getStateMachine()
	{
		return stateMachine;
	}

	@Override
	public void metaGame(long timeout) throws MetaGamingException
	{
		try
		{
			stateMachine = getInitialStateMachine();
			stateMachine.intialize(getMatch().getDescription());
			currentState = stateMachine.getInitialState();
			role = stateMachine.getRoleFromProp(getRoleName());

			stateMachineMetaGame(timeout);
		}
		catch (Exception e)
		{
			throw new MetaGamingException();
		}
	}

	@Override
	public GdlSentence selectMove(long timeout) throws MoveSelectionException
	{
		try
		{
			stateMachine.doPerMoveWork();

			List<GdlSentence> lastMoves = getMatch().getMostRecentMoves();
			if (lastMoves != null)
			{
				List<Move> moves = new ArrayList<Move>();
				for (GdlSentence sentence : lastMoves)
				{
					moves.add(stateMachine.getMoveFromSentence(sentence));
				}

				currentState = stateMachine.getNextState(currentState, moves);
			}

			return stateMachineSelectMove(timeout).getContents();
		}
		catch (Exception e)
		{
			throw new MoveSelectionException();
		}
	}

	public abstract void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException;

	public abstract Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException;

}
