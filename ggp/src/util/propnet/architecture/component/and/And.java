package util.propnet.architecture.component.and;

import util.propnet.architecture.component.Component;

/**
 * The And class is designed to represent logical AND gates.
 */
public final class And extends Component
{

	/**
	 * Returns true if and only if every input to the and is true.
	 * 
	 * @see util.propnet.architecture.component.Component#getValue()
	 */
	@Override
	public boolean getValue()
	{
		for ( Component component : getInputs() )
		{
			if ( !component.getValue() )
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * @see util.propnet.architecture.component.Component#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("invhouse", "grey", "AND");
	}

}
