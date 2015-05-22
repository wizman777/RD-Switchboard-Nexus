package org.rd.switchboard.nexus.exporters.rda.json;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;
import org.rd.switchboard.nexus.exporters.rda.json.graph.JsonGraph;
import org.rd.switchboard.nexus.exporters.rda.json.graph.JsonNode;
import org.rd.switchboard.nexus.exporters.rda.json.graph.JsonRelationship;
import org.rd.switchboard.utils.aggrigation.AggrigationUtils;
import org.rd.switchboard.utils.neo4j.Neo4jUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Exporter {
	private static final String ANDS_SITE = "researchdata.ands.org.au";
	
	private GraphDatabaseService graphDb;
	private GlobalGraphOperations global;

	private final int maxLevel;
	private final int maxNodes;
	private final int maxSyblings;
	private final File outputFolder;
	
	//private enum NodeType { Grant, Researcher, Publication, Dataset, Institution };
	
	private static final ObjectMapper mapper = new ObjectMapper();   
	
	
	public Exporter(String dbFolder, String outputFolder, 
			int maxLevel, int maxNodes, int maxSyblings) {
		System.out.println("Source Neo4j folder: " + dbFolder);
		System.out.println("Target folder: " + outputFolder);
		System.out.println("Export level: " + maxLevel);
	
		// Open the Graph Database
		graphDb = Neo4jUtils.getReadOnlyGraphDb(dbFolder);
		global = Neo4jUtils.getGlobalOperations(graphDb);
		
		// Set output folder
		this.outputFolder = new File(outputFolder);
		this.outputFolder.mkdirs();
		
		// Store max export level
		this.maxLevel = maxLevel;
		this.maxNodes = maxNodes;
		this.maxSyblings = maxSyblings;
	}
	
	public void process() {
		long beginTime = System.currentTimeMillis();
		long nodeCounter = 0;
		
		try ( Transaction tx = graphDb.beginTx() ) {
			// query all RDA nodes
			ResourceIterable<Node> nodes = global.getAllNodesWithLabel(AggrigationUtils.Labels.RDA);
			for (Node node : nodes) {
				try {		
					String key = (String) node.getProperty(AggrigationUtils.PROPERTY_KEY);
					if (null != key && key.startsWith(ANDS_SITE)) {
						
						System.out.println("=============================");	
						System.out.println("Exporting RDA record with key: " + node.getProperty(AggrigationUtils.PROPERTY_KEY));	
						
						String[] arr = key.split("/");
						if (arr.length >= 3) {
							String jsonName = arr[1] + "-" + arr[2] + ".json";
							
							System.out.println("File: " + jsonName);
							
							long rootId = node.getId();
							
							Map<Long, Node> graphNodes = new HashMap<Long, Node>();
						
							graphNodes.put(node.getId(), node);
							if (!isValidType(node)) {
								List<Node> root = new ArrayList<Node>();
								root.add(node);
								
								exctractNodes(graphNodes, root, maxLevel);
							}
							
							JsonGraph jsonGraph = new JsonGraph();
							for (Node graphNode : graphNodes.values()) {
								JsonNode jsonNode = extractNode(graphNode);
								if (graphNode.getId() == rootId)
									jsonNode.addExtra(JsonNode.EXTRA_ROOT);
								
								jsonGraph.addNode(jsonNode);
								
								Iterable<Relationship> relationships = graphNode.getRelationships();
								if (null != relationships) 
									for (Relationship relationship : relationships) {
										if (relationship.getStartNode().getId() == graphNode.getId()) {
											if (graphNodes.containsKey(relationship.getEndNode().getId()))
												jsonGraph.addRelationship(extractRelationship(relationship));
											else
												jsonNode.addExtra(JsonNode.EXTRA_INCOMPLETE);
										} else if (!graphNodes.containsKey(relationship.getStartNode().getId()))
											jsonNode.addExtra(JsonNode.EXTRA_INCOMPLETE);
									}
							}
							
							String jsonString = mapper.writeValueAsString(jsonGraph);
							File jsonFile = new File(outputFolder, jsonName);
		
							Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(jsonFile), "utf-8"));
								
							writer.write(jsonString);
							writer.close();
							
							++nodeCounter;
						} else
							System.out.println("Error. Invalid RDA key");
					}
					
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println(String.format("Done. Exported %d nodes over %d ms. Average %f ms per node", 
				nodeCounter, endTime - beginTime, (float)(endTime - beginTime) / (float)nodeCounter));
	} 
	
	private void exctractNodes(Map<Long, Node> graph, List<Node> nodes, int level) {
		List<Node> siblings = level > 0 ? new ArrayList<Node>() : null; 
		for (Node node : nodes) {
			Iterable<Relationship> relationships = node.getRelationships();
			if (null != relationships) {
				for (Relationship relationship : relationships) {
					Node other = relationship.getOtherNode(node);
					if (!graph.containsKey(other)) {
						graph.put(other.getId(), other);
						
						// abort the search if nodes cap has been reached
						if (graph.size() >= maxNodes)
							return;

						// add node to the syblings array, if need to check it's siblings as well
						if (level > 0 && !isValidType(node)) 
							siblings.add(other);
					}
				}
			}
		}
		
		// process selected syblings
		if (level > 0)
			exctractNodes(graph, siblings, level - 1);
		
	}
	
	private String getNodeType(Node node) {
		return ((String) node.getProperty(AggrigationUtils.PROPERTY_NODE_TYPE, null));
	}
	
	private boolean isValidType(Node node) {
		String type = getNodeType(node);
		return type == null ? false : !type.equals(AggrigationUtils.LABEL_INSTITUTION_LOWERCASE);
	}
	
	private JsonNode extractNode(Node node) {
		String type = getNodeType(node);
		if (null == type)
			return null;
		
		JsonNode jsonNode = new JsonNode();
		jsonNode.setId(node.getId());
		jsonNode.setType(type);
				
		Iterable<String> keys = node.getPropertyKeys();
		for (String key : keys) 
			jsonNode.addProperty(key, node.getProperty(key));
		
		return jsonNode;
	}
	
	private JsonRelationship extractRelationship(Relationship relationship) {
		JsonRelationship jsonRelationship = new JsonRelationship();
		
		jsonRelationship.setFrom(relationship.getStartNode().getId());
		jsonRelationship.setTo(relationship.getEndNode().getId());
		jsonRelationship.setType(relationship.getType().name());
		
		return jsonRelationship;
	}
	
	
	
}
