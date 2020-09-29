package de.tubs.skeditor.synthesis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.util.EcoreUtil;

import SkillGraph.Category;
import SkillGraph.Edge;
import SkillGraph.Graph;
import SkillGraph.Node;
import SkillGraph.SkillGraphFactory;

public class Synthesis {

	private Graph graph;
	private Map<String, SkillProvider> providerMap;
	
	public Synthesis(Graph g) {
		this.graph = g;
		this.providerMap = new HashMap<>();
	}
	
	public void synthesizeGraph(List<Requirement> requirements) {
		return;
		//initialize(requirements);
	}
	
	/*
	 * resolves recursively all dependencies for Node 
	 */
	private boolean resolveDependencies(Node node) {
		List<String> unsatisfiedDependencies = new ArrayList<String>();//remember all satisfied dependencies 
		Collections.copy(unsatisfiedDependencies, node.getRequiredVariables()); //at the beginning, every required var is unsatisfied
		for (String var : node.getRequiredVariables()) {
			if (unsatisfiedDependencies.contains(var)) {
				// at first check Graph, maybe it already contains a skill that satisfies the dependency 
				for(Node n : graph.getNodes()) {
					List<String> provided = providedVariablesOf(n);
					if(provided.contains(var)) { //Node n in graph provides variable required by node
						boolean forbidden = false;
						for (String defined : node.getProvidedVariables()) {
							if(provided.contains(defined)) {
								forbidden = true;
								break;
							}
						}
						if(!forbidden && canCreateEdge(node, n)) { // Node n is fine as dependency for Node node so add an Edge from node to n
							Edge e = SkillGraphFactory.eINSTANCE.createEdge();
							e.setChildNode(n);
							e.setParentNode(node);
							node.getChildEdges().add(e);
							n.getParentNodes().add(node);
							for (String v : providedVariablesOf(node)) {
								if (unsatisfiedDependencies.contains(v)) {
									unsatisfiedDependencies.remove(v);
								}
							}
						}
					}
				}
				// dependency not found in Graph, get it from repo
				if (unsatisfiedDependencies.contains(var)) {
					String[] variable = {var};
					SkillProvider sp = new DependencySkillProvider(variable, (String[])node.getProvidedVariables().toArray());
					Node dep = sp.getNext();
					while(dep != null) {
						
					}
				}
				
			}
		}
		
		// every dependency is satisfied
		if(unsatisfiedDependencies.isEmpty()) {
			return true;
		}
		return false;
	}
	
	private void initialize(List<Requirement> requirements) {
		for(Requirement req : requirements) {
			providerMap.put(req.getFormula(), new RequirementSkillProvider(req));
		}
	}
	
	/*
	 * returns a List of all propagated variables 
	 */
	private List<String> providedVariablesOf(Node node) {
		List<String> providedVars = new ArrayList<String>();
		providedVars.addAll(node.getProvidedVariables());
		for (Edge e : node.getChildEdges()) {
			providedVars.addAll(providedVariablesOf(e.getChildNode()));
		}
		return providedVars;
	}
	
	/*
	 * checks if an edge can be created from parent to child
	 */
	private boolean canCreateEdge(Node parent, Node child) {
		switch(parent.getCategory()) {
		case ACTION: 
			if(child.getCategory() == Category.ACTION || child.getCategory() == Category.ACTUATOR || child.getCategory() == Category.PERCEPTION || child.getCategory() == Category.PLANNING) {
				return true;
			}
			break;
		case OBSERVABLE_EXTERNAL_BEHAVIOR:
			if(child.getCategory() == Category.OBSERVABLE_EXTERNAL_BEHAVIOR || child.getCategory() == Category.ACTION || child.getCategory() == Category.PLANNING || child.getCategory() == Category.PERCEPTION) {
				return true;
			}
			break;
		case PERCEPTION:
			if(child.getCategory() == Category.PERCEPTION || child.getCategory() == Category.SENSOR) {
				return true;
			}
			break;
		case PLANNING:
			if(child.getCategory() == Category.PERCEPTION || child.getCategory() == Category.PLANNING) {
				return true;
			}
			break;
		default:
			break;
		}
		return false;
	}
	
}
