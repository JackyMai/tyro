package generator;

import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.WattsStrogatzGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkGraphML;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GraphGenerator {
    // Barabasi-Albert Settings
    private static final int MAX_LINKS_PER_STEP = 3;

    // Watts-Strogatz Settings
    private static final int BASE_DEGREE = 6;    // 4 to 8
    private static final double REWIRE_PROBABILITY = 0.1;   // Real-world graph lower than 0.1 on avg

    // File path for resources
    private static final String FILE_PATH = "src/main/resources/graph/";
    private static final int INVALID_ID = -1;   // Used when no variation is required

    private static final int MAX_NODE_COUNT = 2000;
    private static final int STEP = 250;

    private static int nodeCount = 250;

    public static void main(String[] args) {
        generate();
        // generateBA();
        // generateWS();
    }

    /**
     * Generate both Barabasi-Albert and Watts-Strogatz graphs
     * Graph size start from 250, then increase in steps of 250 until a max size of 2000
     * 20 variation is generated for each size
     */
    private static void generate() {
        while(nodeCount <= MAX_NODE_COUNT) {
           for(int id=1; id<=20; id++) {
               generateBA(id);
               generateWS(id);
           }

           nodeCount += STEP;
       }
    }

    /**
     * Generate and export a Barabasi-Albert graph with no ID specified
     * In other words, a single graph with no variation ID in the file name
     */
    private static void generateBA() {
        generateBA(INVALID_ID);
    }

    /**
     * Generate and export a Barabasi-Albert graph with specified ID
     * Graph size is specified by the nodeCount field
     * Maximum number links for each node is specified by the MAX_LINKS_PER_STEP field
     *
     * @param graphID: variation ID of the graph
     */
    private static void generateBA(int graphID) {
        Graph graph = new SingleGraph("Barabasi-Albert");

        // Between 1 and "n" new links per node added
        Generator gen = new BarabasiAlbertGenerator(MAX_LINKS_PER_STEP);

        gen.addSink(graph);
        gen.begin();

        for(int i=0; i<nodeCount; i++) {
            gen.nextEvents();
        }

        gen.end();
        // graph.display(); // Visualize graph structure

        exportGraph(graph, "barabasi-albert", "ba", graphID);
    }

    /**
     * Generate and export a Watts-Strogatz graph with no ID specified
     * In other words, a single graph with no variation ID in the file name
     */
    private static void generateWS() {
        generateWS(INVALID_ID);
    }

    /**
     * Generate and export a Watts-Strogatz graph with ID specified
     * Graph size is specified by the nodeCount field
     * Initial degree of a node is specified by the BASE_DEGREE field
     * Rewiring probability for each node is specified by the REWIRE_PROBABILITY field
     *
     * @param graphID: variation ID of the graph
     */
    private static void generateWS(int graphID) {
        Graph graph = new SingleGraph("Watts-Strogatz");
        Generator gen = new WattsStrogatzGenerator(nodeCount, BASE_DEGREE, REWIRE_PROBABILITY);

        gen.addSink(graph);
        gen.begin();

        while(gen.nextEvents()) {
            // Generating...
        }

        gen.end();
        // graph.display(false); // Node position is provided

        exportGraph(graph, "watts-strogatz", "ws", graphID);
    }

    /**
     * Export a graph to the resource directory in the .graphml format
     *
     * @param graph: the graph that will be exported
     * @param folderName: the folder that the graph will be placed under
     * @param graphName: name of the graph
     * @param graphID: variation ID in the case of multiple graphs with same size
     */
    private static void exportGraph(Graph graph, String folderName, String graphName, int graphID) {
        FileSink fileSink = new FileSinkGraphML();

        String directory = FILE_PATH;
        if(folderName != null && !folderName.equals("")) {  // Append folder name if it's specified
            directory += folderName;
        }

        try {
            Files.createDirectories(Paths.get(directory));  // Create directory is it doesn't already exist
            String fullPath = directory + "/" + graphName + "_" + nodeCount;

            // Append graph ID to path if it is provided
            if(graphID >= 0) {
                fullPath += "_" + String.format("%02d", graphID);
            }

            fileSink.writeAll(graph, fullPath + ".graphml");    // Export graph to the full path specified
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
