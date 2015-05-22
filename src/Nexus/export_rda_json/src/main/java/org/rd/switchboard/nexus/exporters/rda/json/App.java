package org.rd.switchboard.nexus.exporters.rda.json;

public class App {
	private static final String SOURCE_NEO4J_FOLDER = "neo4j";        
	private static final String OUTPUT_FOLDER = "rda/json";
	private static final int MAX_LEVEL = 3;
	private static final int MAX_NODES = 500;
	private static final int MAX_SYBLINGS = 25;
	
	
	public static void main(String[] args) {
		String sourceNeo4jFolder = SOURCE_NEO4J_FOLDER;
        if (args.length > 0 && !args[0].isEmpty())
        	sourceNeo4jFolder = args[0];

        String outputFolder = OUTPUT_FOLDER;
        if (args.length > 1 && !args[1].isEmpty())
        	outputFolder = args[1];

        int maxLevel = MAX_LEVEL;
        if (args.length > 2 && !args[2].isEmpty())
        	maxLevel = Integer.parseInt(args[2]);

        int maxNodes = MAX_NODES;
        if (args.length > 3 && !args[3].isEmpty())
        	maxNodes = Integer.parseInt(args[3]);
        
        int maxSyblings = MAX_SYBLINGS;
        if (args.length > 4 && !args[4].isEmpty())
        	maxSyblings = Integer.parseInt(args[4]);

        
       	Exporter expoter = new Exporter(sourceNeo4jFolder, outputFolder, maxLevel, maxNodes, maxSyblings);
        expoter.process();
	}
}
