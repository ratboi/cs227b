package util.gdl.grammar;

public abstract class GdlSentence extends GdlLiteral
{

	public abstract int arity();

	public abstract GdlTerm get(int index);

	public abstract GdlConstant getName();

	@Override
	public abstract boolean isGround();

	@Override
	public abstract String toString();

	public abstract GdlTerm toTerm();

}
