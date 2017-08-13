package strategies;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.GraphDistance;

import java.util.Collection;

public class Local extends Strategy {
    private Collection<Node> uncovered;

    @Override
    public void execute(Node newcomer) {
        // Get list of uncovered nodes from input graph
//        uncovered = graph.getNodes().toCollection();

        int radius = (int) distance.getRadius();

        // Find node with max centrality from input graph
        Node firstNode = graphModel.factory().newNode();
        Column column = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);

        for (Node n : graph.getNodes().toArray()) {
            Double maxCentrality = (Double) firstNode.getAttribute(column);
            Double centrality = (Double) n.getAttribute(column);

            if (centrality >= maxCentrality) {
                firstNode = n;
            }
        }

        Edge edge = graphModel.factory().newEdge(newcomer, firstNode, 0, 1f, false);
        graph.addEdge(edge);

        Collection<Node> neighbors = graph.getNeighbors(firstNode, radius - 1).toCollection();
        uncovered.addAll(neighbors);

//        for (int i = 0; i < iterations && uncovered.size() != 0; i++) {
//
//        }
    }

    @Override
    public Node getNextNode(String centralityType) {
        return null;
    }

}
