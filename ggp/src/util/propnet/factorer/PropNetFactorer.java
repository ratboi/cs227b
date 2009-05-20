package util.propnet.factorer;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import util.propnet.architecture.PropNet;
import util.propnet.architecture.component.Component;
import util.propnet.architecture.component.and.And;
import util.propnet.architecture.component.constant.Constant;
import util.propnet.architecture.component.not.Not;
import util.propnet.architecture.component.or.Or;
import util.propnet.architecture.component.proposition.Proposition;
import util.propnet.architecture.component.transition.Transition;
import util.gdl.grammar.GdlPool;

public final class PropNetFactorer {
	
	public List<PropNet> factor(PropNet propnet) {
		List<PropNet> propnets = new ArrayList<PropNet>();
		Proposition terminal = propnet.getTerminalProposition();
		List<Component> queue = new ArrayList<Component>();
		List<Component> newTerminals = new ArrayList<Component>();
		queue.add(terminal);
		
		//go backwards until you find a prop with more than one child
		while (!queue.isEmpty()) {
			Component next = queue.remove(0);
			for (Component parent : next.getInputs()) {
				if (parent.getOutputs().size() == 1) {
					queue.add(parent);
				}
				else {
					//add the proposition - most likely "next" is a non-proposition
					while (!(next.getOutputs().get(0) instanceof Proposition))
						next = next.getOutputs().get(0);
					newTerminals.add(next.getOutputs().get(0));
				}
			}
		}
		
		//build the propnet from each new terminal
		for (Component newTerminal : newTerminals) {
			propnets.add(new PropNet(buildPropnet((Proposition)newTerminal, propnet)));
		}
		
		return propnets;
	}
	
	private List<Component> buildPropnet(Proposition terminal, PropNet propnet) {
		Map<String, List<Proposition>> legalProps = propnet.getLegalPropositions();
		Proposition initProp = propnet.getInitProposition();
		
		// from original component to new component
		HashMap<Component, Component> componentMap = new HashMap<Component, Component>();
		
		List<Component> queue = new ArrayList<Component>();
		List<Component> propnetComps = new ArrayList<Component>();
		
		
		// deal with terminals
		queue.add(terminal);
		Component dup = new Proposition(GdlPool.getConstant("terminal"));
		componentMap.put(terminal, dup);
		
		List<Component> inputProps = new ArrayList<Component>();
		
		// go up
		while (!queue.isEmpty()) {
			Component next = queue.remove(0);
			dup = duplicateComponent(next, componentMap);
			
			//add parents to dup
			for (Component parent : next.getInputs()) {
				Component dupParent = duplicateComponent(parent, componentMap);
				if (!dup.getInputs().contains(dupParent)) 
					dup.addInput(dupParent);
				if (!dupParent.getOutputs().contains(dup))
					dupParent.addOutput(dup);
				if (parent.getInputs().size() != dupParent.getInputs().size() ||
					parent.getInputs().size() == 0)
					queue.add(parent);
			}
			
			//add dup to propnet
			if (!propnetComps.contains(dup)) {
				propnetComps.add(dup);
			}
			
			if (next.getInputs().size() == 0) {
				inputProps.add(next);
			}
		}
		
		queue.addAll(inputProps);
		List<Component> visited = new ArrayList<Component>();
		
		// go down
		while (!queue.isEmpty()) {
			Component next = queue.remove(0);
			dup = duplicateComponent(next, componentMap);
			
			//add children to dup (if not terminal)
			if (!(dup instanceof Proposition && ((Proposition)dup).getName().toString().equals("terminal"))) {
				for (Component child : next.getOutputs()) {
					Component dupChild = duplicateComponent(child, componentMap);
					if (!dup.getOutputs().contains(dupChild)) 
						dup.addOutput(dupChild);
					if (!dupChild.getInputs().contains(dup)) 
						dupChild.addInput(dup);
					if (!visited.contains(next)) 
						queue.add(child);
					
				}
				visited.add(next);
			}
			
			//add dup to propnet
			if (!propnetComps.contains(dup)) {
				propnetComps.add(dup);
			}
		}
		
		// factor the legal props
		for (Component comp : inputProps) {
			Proposition inputProp;
			if (comp instanceof Proposition) {
				inputProp = (Proposition)comp;
				String playerName = inputProp.getName().toSentence().getBody().get(0).toString();
				List<Proposition> legalPropsForPlayer = legalProps.get(playerName);
				for (Proposition legalPropForPlayer : legalPropsForPlayer) {
					if (inputProp.getName().toSentence().getBody().equals(legalPropForPlayer.getName().toSentence().getBody())) {
						queue.add(legalPropForPlayer);
						break;
					}
				}
			}
		}
		
		// deal with init and role props
		for (Proposition prop : propnet.getPropositions()) {
			String propName = prop.getName().toSentence().getName().toString();
			if (propName.equals("init") || propName.equals("role"))
				queue.add(prop);
		}
		
		// go up
		while (!queue.isEmpty()) {
			Component next = queue.remove(0);
			dup = duplicateComponent(next, componentMap);
			
			//add parents to dup
			for (Component parent : next.getInputs()) {
				Component dupParent = duplicateComponent(parent, componentMap);
				if (!dup.getInputs().contains(dupParent)) 
					dup.addInput(dupParent);
				if (!dupParent.getOutputs().contains(dup))
					dupParent.addOutput(dup);
				if (parent.getInputs().size() != dupParent.getInputs().size() ||
					parent.getInputs().size() == 0)
					queue.add(parent);
			}
			
			//add dup to propnet
			if (!propnetComps.contains(dup)) {
				propnetComps.add(dup);
			}
		}
		
		// search starting from init prop
		List<Component> initAncestors = new ArrayList<Component>();
		visited.clear();
		queue.add(initProp);
		while(!queue.isEmpty()) {
			Component next = queue.remove(0);
			dup = duplicateComponent(next, componentMap);
			for (Component child : next.getOutputs()) {
				Component dupChild = duplicateComponent(child, componentMap);
				if (propnetComps.contains(dupChild)) {
					initAncestors.add(child);
				} else {
					if (!visited.contains(next)) 
						queue.add(child);
				}
			}
			visited.add(next);
		}
		
		queue.addAll(initAncestors);
		// go up
		while (!queue.isEmpty()) {
			Component next = queue.remove(0);
			dup = duplicateComponent(next, componentMap);
			
			//add parents to dup
			for (Component parent : next.getInputs()) {
				Component dupParent = duplicateComponent(parent, componentMap);
				if (!dup.getInputs().contains(dupParent)) 
					dup.addInput(dupParent);
				if (!dupParent.getOutputs().contains(dup))
					dupParent.addOutput(dup);
				if (parent.getInputs().size() != dupParent.getInputs().size() ||
					parent.getInputs().size() == 0)
					queue.add(parent);
			}
			
			//add dup to propnet
			if (!propnetComps.contains(dup)) {
				propnetComps.add(dup);
			}
		}
		
		
		return propnetComps;
	}
	
	private Component duplicateComponent(Component comp, HashMap<Component, Component> componentMap) {
		Component dup = null;
		if (!componentMap.containsKey(comp)) {
			if (comp instanceof And) {
				dup = new And();
			} else if (comp instanceof Constant) {
				dup = new Constant(comp.getValue());
			} else if (comp instanceof Not) {
				dup = new Not();
			} else if (comp instanceof Or) {
				dup = new Or();
			} else if (comp instanceof Proposition) {
				dup = new Proposition(((Proposition)comp).getName());
			} else if (comp instanceof Transition) {
				dup = new Transition();
			}
			componentMap.put(comp, dup);
		} else {
			dup = componentMap.get(comp);
		}
		return dup;
	}
	
}
