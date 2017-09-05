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

            // every once in a long while, the selectedNode is the newcomer, which throws an exception
            Node selectedNode = null;
            int id = 0;

            boolean nodeNotFound = true;
            while(nodeNotFound) {
                try {
                    id = rand.nextInt(nodeCount);
                    selectedNode = graph.getNode("" + id);
                    Edge edge = graphModel.factory().newEdge(newcomer, selectedNode, 0, 1f, false);
                    graph.addEdge(edge);
                }catch (NullPointerException e){
                    continue;
                }
                nodeNotFound = false;
            }

            if (test) exportUpdatedCentralities(newcomer);

            if (visualise){
                selectedNode.setSize(40);
                selectedNode.setColor(Color.BLACK);

                visualizer.updateView();
            }
        }
    }
}
