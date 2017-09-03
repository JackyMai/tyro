package generator;

import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.WattsStrogatzGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkGraphML;

import java.io.IOException;

public class GraphGenerator {
    private static final int NODE_COUNT = 100;

    // Barabasi-Albert Settings
    private static final int MAX_LINKS_PER_STEP = 3;

    // Watts-Strogatz Settings
    private static final int BASE_DEGREE = 2;
    private static final double REWIRE_PROBABILITY = 0.5;

    // File path for resources
    private static final String FILE_PATH = "src/main/resources/graph/";

    public static void main(String[] args) {
        generateBA();
        generateWS();
    }

    public static void generateBA() {
        Graph graph = new SingleGraph("Barabasi-Albert");

        // Between 1 and "n" new links per node added
        Generator gen = new BarabasiAlbertGenerator(MAX_LINKS_PER_STEP);

        gen.addSink(graph);
        gen.begin();

        for(int i=0; i<NODE_COUNT; i++) {
            gen.nextEvents();
        }

        gen.end();
//        graph.display();

        FileSink fileSink = new FileSinkGraphML();
        try {
            fileSink.writeAll(graph, FILE_PATH + "ba_" + NODE_COUNT + ".graphml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateWS() {
        Graph graph = new SingleGraph("Watts-Strogatz");
        Generator gen = new WattsStrogatzGenerator(NODE_COUNT, BASE_DEGREE, REWIRE_PROBABILITY);

        gen.addSink(graph);
        gen.begin();

        while(gen.nextEvents()) {
            // Generating...
        }

        gen.end();
//        graph.display(false); // Node position is provided

        FileSink fileSink = new FileSinkGraphML();
        try {
            fileSink.writeAll(graph, FILE_PATH + "ws_" + NODE_COUNT + ".graphml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
