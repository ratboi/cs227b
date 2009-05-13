package util.statemachine.prover;

import util.gdl.grammar.GdlProposition;
import util.statemachine.Role;

public final class ProverRole implements Role
{

	private final GdlProposition name;

	public ProverRole(GdlProposition name)
	{
		this.name = name;
	}

	@Override
	public boolean equals(Object o)
	{
		if ((o != null) && (o instanceof ProverRole))
		{
			ProverRole role = (ProverRole) o;
			return role.name.equals(name);
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see util.statemachine.prover.Role#getName()
	 */
	public GdlProposition getName()
	{
		return name;
	}

	@Override
	public int hashCode()
	{
		return name.hashCode();
	}

	@Override
	public String toString()
	{
		return name.toString();
	}

}
