package util.propnet.architecture.component.constant;

import util.propnet.architecture.component.Component;

/**
 * The Constant class is designed to represent nodes with fixed logical values.
 */
public final class Constant extends Component
{

	/** The value of the constant. */
	private final boolean value;

	/**
	 * Creates a new Constant with value <tt>value</tt>.
	 * 
	 * @param value
	 *            The value of the Constant.
	 */
	public Constant(boolean value)
	{
		this.value = value;
	}

	/**
	 * Returns the value that the constant was initialized to.
	 * 
	 * @see util.propnet.architecture.component.Component#getValue()
	 */
	@Override
	public boolean getValue()
	{
		return value;
	}

	/**
	 * @see util.propnet.architecture.component.Component#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("doublecircle", "grey", Boolean.toString(value).toUpperCase());
	}

}
