package util.propnet.architecture.component.proposition;

import util.gdl.grammar.GdlTerm;
import util.propnet.architecture.component.Component;

/**
 * The Proposition class is designed to represent named latches.
 */
public final class Proposition extends Component
{

	/** The name of the Proposition. */
	private final GdlTerm name;
	/** The value of the Proposition. */
	private boolean value;

	/**
	 * Creates a new Proposition with name <tt>name</tt>.
	 * 
	 * @param name
	 *            The name of the Proposition.
	 */
	public Proposition(GdlTerm name)
	{
		this.name = name;
		this.value = false;
	}

	/**
	 * Getter method.
	 * 
	 * @return The name of the Proposition.
	 */
	public GdlTerm getName()
	{
		return name;
	}

	/**
	 * Returns the current value of the Proposition.
	 * 
	 * @see util.propnet.architecture.component.Component#getValue()
	 */
	@Override
	public boolean getValue()
	{
		return value;
	}

	/**
	 * Setter method.
	 * 
	 * @param value
	 *            The new value of the Proposition.
	 */
	public void setValue(boolean value)
	{
		this.value = value;
	}

	/**
	 * @see util.propnet.architecture.component.Component#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("circle", value ? "red" : "white", name.toString());
	}

}
