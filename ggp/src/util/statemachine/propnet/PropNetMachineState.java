package util.statemachine.propnet;

import java.util.List;
import java.util.ArrayList;

import util.gdl.grammar.GdlSentence;
import util.propnet.architecture.component.proposition.Proposition;
import util.statemachine.MachineState;

public final class PropNetMachineState implements MachineState {
	
	private final List<GdlSentence> contents;
	
	public PropNetMachineState(List<GdlSentence> contents) {
		this.contents = contents;
	}

	@Override
	public boolean equals(Object o)
	{
		if ((o != null) && (o instanceof PropNetMachineState))
		{
			PropNetMachineState state = (PropNetMachineState) o;
			return state.contents.equals(contents);
		}

		return false;
	}

	public List<GdlSentence> getContents() {
		return contents;
	}
	
	@Override
	public int hashCode()
	{
		return contents.hashCode();
	}

	@Override
	public String toString()
	{
		return contents.toString();
	}

}
