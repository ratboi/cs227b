package util.statemachine.propnet;

import util.gdl.grammar.GdlProposition;
import util.statemachine.Role;

public class PropNetRole implements Role {
	private final GdlProposition name;

	public PropNetRole(GdlProposition name)
	{
		this.name = name;
	}

	@Override
	public boolean equals(Object o)
	{
		if ((o != null) && (o instanceof PropNetRole))
		{
			PropNetRole role = (PropNetRole) o;
			return role.name.equals(name);
		}

		return false;
	}

	public GdlProposition getName() {
		return name;
	}
	
}
