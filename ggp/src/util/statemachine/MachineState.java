package util.statemachine;

import java.util.List;

import util.gdl.grammar.GdlSentence;

public interface MachineState {

	public abstract List<GdlSentence> getContents();

}