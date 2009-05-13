package util.statemachine.propnet;

import util.gdl.grammar.GdlSentence;
import util.statemachine.Move;

public final class PropNetMove implements Move
{

	private final GdlSentence contents;

	public PropNetMove(GdlSentence contents)
	{
		this.contents = contents;
	}

	@Override
	public boolean equals(Object o)
	{
		if ((o != null) && (o instanceof PropNetMove))
		{
			PropNetMove move = (PropNetMove) o;
			return move.contents.equals(contents);
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see util.statemachine.prover.Move#getContents()
	 */
	public GdlSentence getContents()
	{
		return contents;
	}

	@Override
	public int hashCode()
	{
		return contents.hashCode();
	}

	@Override
	public String toString()
	{
		return contents.toString();
	}

}
