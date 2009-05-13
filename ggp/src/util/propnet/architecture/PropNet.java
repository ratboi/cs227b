package util.propnet.architecture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.gdl.grammar.GdlConstant;
import util.gdl.grammar.GdlFunction;
import util.gdl.grammar.GdlTerm;
import util.propnet.architecture.component.Component;
import util.propnet.architecture.component.proposition.Proposition;
import util.propnet.architecture.component.transition.Transition;

/**
 * The PropNet class is designed to represent Propositional Networks.
 */
public final class PropNet
{

	/** References to every component in the PropNet. */
	private final List<Component> components;
	/** References to every Proposition in the PropNet. */
	private final List<Proposition> propositions;
	/** References to every BaseProposition in the PropNet, indexed by name. */
	private final Map<GdlTerm, Proposition> basePropositions;
	/** References to every InputProposition in the PropNet, indexed by name. */
	private final Map<GdlTerm, Proposition> inputPropositions;
	/**
	 * References to every LegalProposition in the PropNet, indexed by player
	 * name.
	 */
	private final Map<String, List<Proposition>> legalPropositions;
	/**
	 * References to every GoalProposition in the PropNet, indexed by player
	 * name.
	 */
	private final Map<String, List<Proposition>> goalPropositions;
	/** A reference to the single, unique, InitProposition. */
	private final Proposition initProposition;
	/** A reference to the single, unique, TerminalProposition. */
	private final Proposition terminalProposition;

	/**
	 * Creates a new PropNet from a list of Components, along with indices over
	 * those components.
	 * 
	 * @param components
	 *            A list of Components.
	 */
	public PropNet(List<Component> components)
	{
		this.components = components;
		this.propositions = recordPropositions();
		this.basePropositions = recordBasePropositions();
		this.inputPropositions = recordInputPropositions();
		this.legalPropositions = recordLegalPropositions();
		this.goalPropositions = recordGoalPropositions();
		this.initProposition = recordInitProposition();
		this.terminalProposition = recordTerminalProposition();
	}

	/**
	 * Getter method.
	 * 
	 * @return References to every BaseProposition in the PropNet, indexed by
	 *         name.
	 */
	public Map<GdlTerm, Proposition> getBasePropositions()
	{
		return basePropositions;
	}

	/**
	 * Getter method.
	 * 
	 * @return References to every Component in the PropNet.
	 */
	public List<Component> getComponents()
	{
		return components;
	}

	/**
	 * Getter method.
	 * 
	 * @return References to every GoalProposition in the PropNet, indexed by
	 *         player name.
	 */
	public Map<String, List<Proposition>> getGoalPropositions()
	{
		return goalPropositions;
	}

	/**
	 * Getter method. A reference to the single, unique, InitProposition.
	 * 
	 * @return
	 */
	public Proposition getInitProposition()
	{
		return initProposition;
	}

	/**
	 * Getter method.
	 * 
	 * @return References to every InputProposition in the PropNet, indexed by
	 *         name.
	 */
	public Map<GdlTerm, Proposition> getInputPropositions()
	{
		return inputPropositions;
	}

	/**
	 * Getter method.
	 * 
	 * @return References to every LegalProposition in the PropNet, indexed by
	 *         player name.
	 */
	public Map<String, List<Proposition>> getLegalPropositions()
	{
		return legalPropositions;
	}

	/**
	 * Getter method.
	 * 
	 * @return References to every Proposition in the PropNet.
	 */
	public List<Proposition> getPropositions()
	{
		return propositions;
	}

	/**
	 * Getter method.
	 * 
	 * @return A reference to the single, unique, TerminalProposition.
	 */
	public Proposition getTerminalProposition()
	{
		return terminalProposition;
	}

	/**
	 * Returns a representation of the PropNet in .dot format.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("digraph propNet\n{\n");
		for ( Component component : components )
		{
			sb.append("\t" + component.toString() + "\n");
		}
		sb.append("}");

		return sb.toString();
	}

	/**
	 * Builds an index over the BasePropositions in the PropNet.
	 * 
	 * @return An index over the BasePropositions in the PropNet.
	 */
	private Map<GdlTerm, Proposition> recordBasePropositions()
	{
		Map<GdlTerm, Proposition> basePropositions = new HashMap<GdlTerm, Proposition>();
		for ( Proposition proposition : propositions )
		{
			if ( proposition.getInputs().size() > 0 )
			{
				Component component = proposition.getInputs().get(0);
				if ( component instanceof Transition )
				{
					basePropositions.put(proposition.getName(), proposition);
				}
			}
		}

		return basePropositions;
	}

	/**
	 * Builds an index over the GoalPropositions in the PropNet.
	 * 
	 * @return An index over the GoalPropositions in the PropNet.
	 */
	private Map<String, List<Proposition>> recordGoalPropositions()
	{
		Map<String, List<Proposition>> goalPropositions = new HashMap<String, List<Proposition>>();
		for ( Proposition proposition : propositions )
		{
			if ( proposition.getName() instanceof GdlFunction )
			{
				GdlFunction function = (GdlFunction) proposition.getName();
				if ( function.getName().getValue().equals("goal") )
				{
					GdlConstant name = (GdlConstant) function.get(0);
					if ( !goalPropositions.containsKey(name.getValue()) )
					{
						goalPropositions.put(name.getValue(), new ArrayList<Proposition>());
					}
					goalPropositions.get(name.getValue()).add(proposition);
				}
			}
		}

		return goalPropositions;
	}

	/**
	 * Returns a reference to the single, unique, InitProposition.
	 * 
	 * @return A reference to the single, unique, InitProposition.
	 */
	private Proposition recordInitProposition()
	{
		for ( Proposition proposition : propositions )
		{
			if ( proposition.getName() instanceof GdlConstant )
			{
				GdlConstant constant = (GdlConstant) proposition.getName();
				if ( constant.getValue().equals("INIT") )
				{
					return proposition;
				}
			}
		}

		return null;
	}

	/**
	 * Builds an index over the InputPropositions in the PropNet.
	 * 
	 * @return An index over the InputPropositions in the PropNet.
	 */
	private Map<GdlTerm, Proposition> recordInputPropositions()
	{
		Map<GdlTerm, Proposition> inputPropositions = new HashMap<GdlTerm, Proposition>();
		for ( Proposition proposition : propositions )
		{
			if ( proposition.getName() instanceof GdlFunction )
			{
				GdlFunction function = (GdlFunction) proposition.getName();
				if ( function.getName().getValue().equals("does") )
				{
					inputPropositions.put(proposition.getName(), proposition);
				}
			}
		}

		return inputPropositions;
	}

	/**
	 * Builds an index over the LegalPropositions in the PropNet.
	 * 
	 * @return An index over the LegalPropositions in the PropNet.
	 */
	private Map<String, List<Proposition>> recordLegalPropositions()
	{
		Map<String, List<Proposition>> legalPropositions = new HashMap<String, List<Proposition>>();
		for ( Proposition proposition : propositions )
		{
			if ( proposition.getName() instanceof GdlFunction )
			{
				GdlFunction function = (GdlFunction) proposition.getName();
				if ( function.getName().getValue().equals("legal") )
				{
					GdlConstant name = (GdlConstant) function.get(0);
					if ( !legalPropositions.containsKey(name.getValue()) )
					{
						legalPropositions.put(name.getValue(), new ArrayList<Proposition>());
					}
					legalPropositions.get(name.getValue()).add(proposition);
				}
			}
		}

		return legalPropositions;
	}

	/**
	 * Builds an index over the Propositions in the PropNet.
	 * 
	 * @return An index over Propositions in the PropNet.
	 */
	private List<Proposition> recordPropositions()
	{
		List<Proposition> propositions = new ArrayList<Proposition>();
		for ( Component component : components )
		{
			if ( component instanceof Proposition )
			{
				propositions.add((Proposition) component);
			}
		}

		return propositions;
	}

	/**
	 * Records a reference to the single, unique, TerminalProposition.
	 * 
	 * @return A reference to the single, unqiue, TerminalProposition.
	 */
	private Proposition recordTerminalProposition()
	{
		for ( Proposition proposition : propositions )
		{
			if ( proposition.getName() instanceof GdlConstant )
			{
				GdlConstant constant = (GdlConstant) proposition.getName();
				if ( constant.getValue().equals("terminal") )
				{
					return proposition;
				}
			}
		}

		return null;
	}

}
