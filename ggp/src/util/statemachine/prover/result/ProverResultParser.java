package util.statemachine.prover.result;

import java.util.ArrayList;
import java.util.List;

import util.gdl.grammar.GdlConstant;
import util.gdl.grammar.GdlPool;
import util.gdl.grammar.GdlProposition;
import util.gdl.grammar.GdlSentence;
import util.gdl.grammar.GdlTerm;
import util.statemachine.MachineState;
import util.statemachine.Move;
import util.statemachine.Role;
import util.statemachine.prover.ProverMachineState;
import util.statemachine.prover.ProverMove;
import util.statemachine.prover.ProverRole;

public final class ProverResultParser
{

	private final static GdlConstant TRUE = GdlPool.getConstant("true");

	public List<Move> toMoves(List<GdlSentence> results)
	{
		List<Move> moves = new ArrayList<Move>();
		for (GdlSentence result : results)
		{
			moves.add(new ProverMove(result.get(1).toSentence()));
		}

		return moves;
	}

	public List<Role> toRoles(List<GdlSentence> results)
	{
		List<Role> roles = new ArrayList<Role>();
		for (GdlSentence result : results)
		{
			GdlProposition name = (GdlProposition) result.get(0).toSentence();
			roles.add(new ProverRole(name));
		}

		return roles;
	}

	public MachineState toState(List<GdlSentence> results)
	{
		List<GdlSentence> trues = new ArrayList<GdlSentence>();
		for (GdlSentence result : results)
		{
			trues.add(GdlPool.getRelation(TRUE, new GdlTerm[] { result.get(0) }));
		}

		return new ProverMachineState(trues);
	}

}
