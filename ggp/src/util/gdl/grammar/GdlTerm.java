package util.gdl.grammar;

public abstract class GdlTerm extends Gdl
{

	@Override
	public abstract boolean isGround();

	public abstract GdlSentence toSentence();

	@Override
	public abstract String toString();

}
