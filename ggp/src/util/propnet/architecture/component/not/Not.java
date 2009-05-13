package util.propnet.architecture.component.not;

import util.propnet.architecture.component.Component;

/**
 * The Not class is designed to represent logical NOT gates.
 */
public final class Not extends Component
{

	/**
	 * Returns the inverse of the input to the not.
	 * 
	 * @see util.propnet.architecture.component.Component#getValue()
	 */
	@Override
	public boolean getValue()
	{
		return !getInputs().get(0).getValue();
	}

	/**
	 * @see util.propnet.architecture.component.Component#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("invtriangle", "grey", "NOT");
	}

}
