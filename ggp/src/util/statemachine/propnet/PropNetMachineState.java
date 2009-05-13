package util.statemachine.propnet;

import java.util.List;
import java.util.ArrayList;

import util.gdl.grammar.GdlSentence;
import util.propnet.architecture.component.proposition.Proposition;
import util.statemachine.MachineState;

public final class PropNetMachineState implements MachineState {
	
	private final List<Proposition> propositions;

	/**
	 * Constructs a PropNetMachineState given a list of propositions
	 * that are true in the machine state.  All other propositions are
	 * assumed to be false.
	 * 
	 * @param propositions the list of propositions that are true
	 */
	public PropNetMachineState(List<Proposition> propositions)
	{
		this.propositions = propositions;
	}

	@Override
	public boolean equals(Object o)
	{
		if ((o != null) && (o instanceof PropNetMachineState))
		{
			PropNetMachineState state = (PropNetMachineState) o;
			return state.propositions.equals(propositions);
		}

		return false;
	}

	public List<GdlSentence> getContents() {
		List<GdlSentence> contents = new ArrayList<GdlSentence>();
		for (Proposition proposition : propositions) {
			contents.add(proposition.getName().toSentence());
		}
		return contents;
	}
	
	/**
	 * Get the list of propositions that are true in the state.
	 * 
	 * @return the list of true propositions
	 */
	public List<Proposition> getPropositions() {
		return propositions;
	}

}
