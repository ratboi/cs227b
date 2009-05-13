package util.gdl.grammar;

public final class GdlNot extends GdlLiteral
{

	private final GdlLiteral body;
	private Boolean ground;

	GdlNot(GdlLiteral body)
	{
		this.body = body;
		ground = null;
	}

	public GdlLiteral getBody()
	{
		return body;
	}

	@Override
	public boolean isGround()
	{
		if (ground == null)
		{
			ground = body.isGround();
		}

		return ground;
	}

	@Override
	public String toString()
	{
		return "( not " + body + " )";
	}

}
