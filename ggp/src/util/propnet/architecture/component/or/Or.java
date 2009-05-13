package util.propnet.architecture.component.or;

import util.propnet.architecture.component.Component;

/**
 * The Or class is designed to represent logical OR gates.
 */
public final class Or extends Component
{

	/**
	 * Returns true if and only if at least one of the inputs to the or is true.
	 * 
	 * @see util.propnet.architecture.component.Component#getValue()
	 */
	@Override
	public boolean getValue()
	{
		for ( Component component : getInputs() )
		{
			if ( component.getValue() )
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @see util.propnet.architecture.component.Component#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("ellipse", "grey", "OR");
	}

}
