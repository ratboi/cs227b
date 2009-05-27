package util.propnet.factory;

import java.util.List;

import util.gdl.grammar.Gdl;
import util.gdl.grammar.GdlRule;
import util.propnet.architecture.PropNet;
import util.propnet.factory.converter.PropNetConverter;
import util.propnet.factory.flattener.PropNetFlattener;

/**
 * The PropNetFactory class defines the creation of PropNets from game
 * descriptions.
 */
public final class PropNetFactory
{

	/** A PropNet Flattener. */
	private final PropNetFlattener flattener;
	/** A PropNet Converter. */
	private final PropNetConverter converter;

	/**
	 * Creates a new PropNetFactory.
	 */
	public PropNetFactory()
	{
		this.flattener = new PropNetFlattener();
		this.converter = new PropNetConverter();
	}

	/**
	 * Creates a PropNet from a game description using the following process:
	 * <ol>
	 * <li>Flattens the game description to remove variables.</li>
	 * <li>Transforms the flattened game description into an equivalent
	 * PropNet.</li>
	 * </ol>
	 * 
	 * @param description
	 *            A game description.
	 * @return An equivalent PropNet.
	 */
	public PropNet create(List<Gdl> description)
	{
		EthansFlattener EF = new EthansFlattener(description);
		List<GdlRule> flatDescription = EF.flatten();
		return converter.convert(flatDescription);
	}

}
