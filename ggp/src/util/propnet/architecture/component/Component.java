package util.propnet.architecture.component;

import java.util.ArrayList;
import java.util.List;

/**
 * The root class of the Component hierarchy, which is designed to represent
 * nodes in a PropNet. The general contract of derived classes is to override
 * all methods.
 */
public abstract class Component
{

	/** The inputs to the component. */
	private final List<Component> inputs;
	/** The outputs of the component. */
	private final List<Component> outputs;

	/**
	 * Creates a new Component with no inputs or outputs.
	 */
	public Component()
	{
		this.inputs = new ArrayList<Component>();
		this.outputs = new ArrayList<Component>();
	}

	/**
	 * Adds a new input.
	 * 
	 * @param input
	 *            A new input.
	 */
	public void addInput(Component input)
	{
		inputs.add(input);
	}

	/**
	 * Adds a new output.
	 * 
	 * @param output
	 *            A new output.
	 */
	public void addOutput(Component output)
	{
		outputs.add(output);
	}
	
	/**
	 * Removes an input.
	 * 
	 * @param input
	 *            Input to remove.
	 */
	public void removeInput(Component input)
	{
		inputs.remove(input);
	}

	/**
	 * Removes an output.
	 * 
	 * @param output
	 *            Output to remove.
	 */
	public void removeOutput(Component output)
	{
		outputs.remove(output);
	}

	/**
	 * Getter method.
	 * 
	 * @return The inputs to the component.
	 */
	public List<Component> getInputs()
	{
		return inputs;
	}

	/**
	 * Getter method.
	 * 
	 * @return The outputs of the component.
	 */
	public List<Component> getOutputs()
	{
		return outputs;
	}

	/**
	 * Returns the value of the Component.
	 * 
	 * @return The value of the Component.
	 */
	public abstract boolean getValue();

	/**
	 * Returns a representation of the Component in .dot format.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public abstract String toString();

	/**
	 * Returns a configurable representation of the Component in .dot format.
	 * 
	 * @param shape
	 *            The value to use as the <tt>shape</tt> attribute.
	 * @param fillcolor
	 *            The value to use as the <tt>fillcolor</tt> attribute.
	 * @param label
	 *            The value to use as the <tt>label</tt> attribute.
	 * @return A representation of the Component in .dot format.
	 */
	protected String toDot(String shape, String fillcolor, String label)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("\"@" + Integer.toHexString(hashCode()) + "\"[shape=" + shape + ", style= filled, fillcolor=" + fillcolor + ", label=\"" + label + "\"]; ");
		for ( Component component : getOutputs() )
		{
			sb.append("\"@" + Integer.toHexString(hashCode()) + "\"->" + "\"@" + Integer.toHexString(component.hashCode()) + "\"; ");
		}

		return sb.toString();
	}

}
