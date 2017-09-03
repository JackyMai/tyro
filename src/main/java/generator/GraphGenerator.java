package generator;

import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

public class Generator {
    public static void main(String[] args) {
        Graph graph = new SingleGraph("Barabasi-Albert");

        // Between 1 and 3 new links per node added
        Generator gen = new BarabasiAlbertGenerator(3);
    }
}
