package util.statemachine.prover;

import java.util.List;

import util.gdl.grammar.Gdl;
import util.gdl.grammar.GdlConstant;
import util.gdl.grammar.GdlFunction;
import util.gdl.grammar.GdlProposition;
import util.gdl.grammar.GdlRelation;
import util.gdl.grammar.GdlSentence;
import util.statemachine.MachineState;

public final class ProverMachineState implements MachineState
{

	private final List<GdlSentence> contents;

	public ProverMachineState(List<GdlSentence> contents)
	{
		this.contents = contents;
	}

	@Override
	public boolean equals(Object o)
	{
		if ((o != null) && (o instanceof ProverMachineState))
		{
			ProverMachineState state = (ProverMachineState) o;
			return state.contents.equals(contents);
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see util.statemachine.prover.MachineState#getContents()
	 */
	public List<GdlSentence> getContents()
	{
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
	
	public String toXML()
	{
		String rval = "<match>\n\n<herstory>\n\n\t<state>\n\n";
		for(GdlSentence sentence : contents)
		{
			rval += gdlToXML(sentence);
		}
		rval += "\n\t</state>\n\n</herstory>\n\n</match>\n";
		return rval;
	}
	
	private String gdlToXML(Gdl gdl)
	{
		String rval = "";
		if(gdl instanceof GdlConstant)
		{
			GdlConstant c = (GdlConstant)gdl;
			return c.getValue();
		} else if(gdl instanceof GdlFunction) {
			GdlFunction f = (GdlFunction)gdl;
			if(f.getName().toString().equals("true"))
			{
				return "<fact>\n\n"+gdlToXML(f.get(0))+"</fact>\n\n";
			}
			else
			{
				rval += "\t<relation>"+f.getName()+"</relation>\n\n";
				for(int i=0; i<f.arity(); i++)
					rval += "\t\t<argument>"+gdlToXML(f.get(i))+"</argument>\n\n";
				return rval;
			}
		} else if (gdl instanceof GdlRelation) {
			GdlRelation relation = (GdlRelation) gdl;
			if(relation.getName().toString().equals("true"))
			{
				for(int i=0; i<relation.arity(); i++)
					rval+="<fact>\n\n"+gdlToXML(relation.get(i))+"</fact>\n\n";
				return rval;
			} else {
				rval+="\t<relation>"+relation.getName()+"</relation>\n\n";
				for(int i=0; i<relation.arity(); i++)
					rval+="\t\t<argument>"+gdlToXML(relation.get(i))+"</argument>\n\n";
				return rval;
			}
		} else {
			System.err.println("Oh oh: "+gdl.toString());
			return "";
		}
	}

}
