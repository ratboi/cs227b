package util.gdl.grammar;

import java.util.List;

public final class GdlFunction extends GdlTerm
{

	private final List<GdlTerm> body;
	private Boolean ground;
	private final GdlConstant name;

	GdlFunction(GdlConstant name, List<GdlTerm> body)
	{
		this.name = name;
		this.body = body;
		ground = null;
	}

	public int arity()
	{
		return body.size();
	}

	private boolean computeGround()
	{
		for (GdlTerm term : body)
		{
			if (!term.isGround())
			{
				return false;
			}
		}

		return true;
	}

	public GdlTerm get(int index)
	{
		return body.get(index);
	}

	public GdlConstant getName()
	{
		return name;
	}

	@Override
	public boolean isGround()
	{
		if (ground == null)
		{
			ground = computeGround();
		}

		return ground;
	}

	@Override
	public GdlSentence toSentence()
	{
		return GdlPool.getRelation(name, body);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("( " + name + " ");
		for (GdlTerm term : body)
		{
			sb.append(term + " ");
		}
		sb.append(")");

		return sb.toString();
	}

}
