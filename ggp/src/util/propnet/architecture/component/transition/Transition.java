package util.propnet.architecture.component.transition;

import util.propnet.architecture.component.Component;

/**
 * The Transition class is designed to represent pass-through gates.
 */
public final class Transition extends Component
{

	/**
	 * Returns the value of the input to the transition.
	 * 
	 * @see util.propnet.architecture.component.Component#getValue()
	 */
	@Override
	public boolean getValue()
	{
		return getInputs().get(0).getValue();
	}

	/**
	 * @see util.propnet.architecture.component.Component#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("box", "grey", "TRANSITION");
	}

}
