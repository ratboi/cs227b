package util.gdl.grammar;

public final class GdlProposition extends GdlSentence
{

	private final GdlConstant name;

	GdlProposition(GdlConstant name)
	{
		this.name = name;
	}

	@Override
	public int arity()
	{
		return 0;
	}

	@Override
	public GdlTerm get(int index)
	{
		throw new RuntimeException("GdlPropositions have no body!");
	}

	@Override
	public GdlConstant getName()
	{
		return name;
	}

	@Override
	public boolean isGround()
	{
		return name.isGround();
	}

	@Override
	public String toString()
	{
		return name.toString();
	}

	@Override
	public GdlTerm toTerm()
	{
		return name;
	}

}
