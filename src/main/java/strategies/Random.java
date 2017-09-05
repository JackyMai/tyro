package strategies;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

import java.awt.*;

public class Random extends Strategy {

    java.util.Random rand = new java.util.Random();

    public Random (String filePath, int iterations, boolean visualise, boolean test, String testFilePath){
        super(filePath, iterations, visualise, test, testFilePath);
    }

    @Override
    public void execute(Node newcomer) {

        int nodeCount = graph.getNodeCount();

        for (int currentIteration = 1; currentIteration <= iterations; currentIteration++){
            Node selectedNode = graph.getNode("" + rand.nextInt(nodeCount));

            Edge edge = graphModel.factory().newEdge(newcomer, selectedNode, 0, 1f, false);
            graph.addEdge(edge);

            if (visualise){
                selectedNode.setSize(40);
                selectedNode.setColor(Color.BLACK);

                visualizer.updateView();
            }
        }
    }
}
