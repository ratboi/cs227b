package util.prover;

import java.util.List;

import util.gdl.grammar.GdlSentence;

public abstract class Prover
{

	public abstract List<GdlSentence> askAll(GdlSentence query, List<GdlSentence> context);

	public abstract GdlSentence askOne(GdlSentence query, List<GdlSentence> context);

	public abstract boolean prove(GdlSentence query, List<GdlSentence> context);

}
