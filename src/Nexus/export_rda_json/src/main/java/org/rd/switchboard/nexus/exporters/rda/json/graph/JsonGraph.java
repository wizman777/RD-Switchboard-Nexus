package org.rd.switchboard.nexus.exporters.rda.json.graph;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 1.0.0: Added new json graph class to host nodes and relationships collections
 * 		  The nodes and relationships collections will be stored separately now
 * 
 * @author dima
 *
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonGraph {
	private List<JsonNode> nodes;
	private List<JsonRelationship> relationships;
	
	public List<JsonNode> getNodes() {
		return nodes;
	}
	
	public List<JsonRelationship> getRelationships() {
		return relationships;
	}	
	
	public void addNode(JsonNode node) {
		if (null == nodes)
			nodes = new ArrayList<JsonNode>();
		
		nodes.add(node);
	}
	
	public void addRelationship(JsonRelationship relationship) {
		if (null == relationships)
			relationships = new ArrayList<JsonRelationship>();
		
		relationships.add(relationship);
	}		
}
