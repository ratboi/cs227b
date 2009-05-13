package util.propnet.factory;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import util.propnet.architecture.PropNet;
import util.propnet.architecture.component.Component;
import util.propnet.architecture.component.and.And;
import util.propnet.architecture.component.not.Not;
import util.propnet.architecture.component.or.Or;
import util.propnet.architecture.component.proposition.Proposition;



public final class PropNetSimplifier
{
/*
	public PropNet convert(PropNet propNet) throws IOException
	{
		int nNodesOld = 0;
		int nNodesNew = propNet.getNodes().size();

		while ( nNodesOld != nNodesNew )
		{
			removeDanglingNodes(propNet);
			removeTrivialGates(propNet);
			propagateConstants(propNet);

			nNodesOld = nNodesNew;
			nNodesNew = propNet.getNodes().size();
		}

		return propNet;
	}

	private void propagateConstants(PropNet propNet)
	{
		LinkedList<Component> zeros = new LinkedList<Component>();
		for ( Component gate : propNet.getAndGates() )
			if ( gate.getInputs().isEmpty() )
				zeros.addLast(gate);

		LinkedList<Component> ones = new LinkedList<Component>();
		for ( Component gate : propNet.getOrGates() )
			if ( gate.getInputs().isEmpty() )
				ones.addLast(gate);
		for ( Component gate : propNet.getNotGates() )
			if ( gate.getInputs().isEmpty() )
				ones.addLast(gate);

		while ( !zeros.isEmpty() || !ones.isEmpty() )
		{
			if ( !zeros.isEmpty() )
			{
				Component component = zeros.removeFirst();
				for ( Component view : component.getOutputs() )
					for ( Component output : view.getOutputs() )
					{
						propNet.removeEdge(view, output);
						if ( output instanceof And )
						{
							for ( Component input : output.getInputs() )
								propNet.removeEdge(input, output);
							zeros.addLast(output);
						}
						else if ( output instanceof Or )
						{
							if ( output.getInputs().isEmpty() )
								zeros.addLast(output);
						}
						else if ( output instanceof Not )
							ones.addLast(output);
					}
			}

			if ( !ones.isEmpty() )
			{
				Component Component = ones.removeFirst();
				for ( Proposition view : propNet.getOutputs(Component) )
					for ( Component output : propNet.getOutputs(view) )
					{
						propNet.removeEdge(view, output);
						if ( output instanceof Or )
						{
							for ( Proposition input : propNet.getInputs(output) )
								propNet.removeEdge(input, output);
							ones.addLast(output);
						}
						else if ( output instanceof And )
						{
							if ( propNet.getInputs(output).isEmpty() )
								ones.addLast(output);
						}
						else if ( output instanceof Not)
							zeros.addLast(output);

					}
			}
		}
	}

	private void removeDanglingNodes(PropNet propNet)
	{
		LinkedList<Component> dangling = new LinkedList<Component>();
		for ( Proposition view : propNet.getViewPropositions() )
			if ( view.getOutputs().isEmpty() )
				dangling.addLast(view);
		for ( Component Component : propNet.getGates() )
			if ( Component.getOutputs().isEmpty() )
				dangling.addLast(Component);

		while ( !dangling.isEmpty() )
		{
			Component Component = dangling.removeFirst();
			for ( Component input : propNet.getInputs(Component) )
				if ( input.getOutputs().size() == 1 )
					dangling.addLast(input);
			propNet.removeNode(Component);
		}
	}

	private void removeTrivialGates(PropNet propNet)
	{
		Set<Component> gates = new HashSet<Component>();
		gates.addAll(propNet.getAndGates());
		gates.addAll(propNet.getOrGates());

		for ( Component component : gates )
			if ( component.getInputs().size() == 1 )
			{
				Component input = component.getInputs().get(0);
				List<Component> outputs = component.getOutputs();

				propNet.removeNode(component);
				for ( Component output : outputs )
				{
					List<Component> successors = propNet.getOutputs(output);

					propNet.removeNode(output);
					for ( Component successor : successors )
						propNet.addEdge(input, successor);
				}
			}
	}
*/
}
