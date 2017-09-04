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
    private static final int INVALID_ID = -1;

    private static final int MAX_NODE_COUNT = 2000;
    private static final int STEP = 250;

    private static int nodeCount = 250;

    public static void main(String[] args) {
        generate();
        // generateBA();
        // generateWS();
    }

   public static void generate() {
        // Generate BA graphs
        while(nodeCount <= MAX_NODE_COUNT) {
           for(int id=1; id<=20; id++) {
               generateBA(id);
               generateWS(id);
           }

           nodeCount += STEP;
       }
   }

    private static void generateBA() {
        generateBA(INVALID_ID);
    }

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
        // graph.display();

        exportGraph(graph, "barabasi-albert", "ba", graphID);
    }

    private static void generateWS() {
        generateWS(INVALID_ID);
    }

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

    private static void exportGraph(Graph graph, String folderName, String graphName, int graphID) {
        FileSink fileSink = new FileSinkGraphML();
        String directory = FILE_PATH + folderName;
        try {
            Files.createDirectories(Paths.get(directory));  // Create directory is it doesn't already exist
            String fullPath = directory + "/" + graphName + "_" + nodeCount;

            // Append graph ID to path if it is provided
            if(graphID >= 0) {
                fullPath += "_" + String.format("%02d", graphID);
            }

            fileSink.writeAll(graph, fullPath + ".graphml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
