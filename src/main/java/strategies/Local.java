package strategies;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.GraphDistance;

import java.util.Collection;

public class Local extends Strategy {
    private Collection<Node> covered;

    @Override
    public void execute(Node newcomer) {
        int radius = (int) distance.getRadius();

        // Find node with max centrality from input graph
        Node firstNode = graphModel.factory().newNode();
        Column column = graphModel.getNodeTable().getColumn(centralityType);

        for (Node n : graph.getNodes().toArray()) {
            Double maxCentrality = (Double) firstNode.getAttribute(column);
            Double centrality = (Double) n.getAttribute(column);

            if (centrality >= maxCentrality) {
                firstNode = n;
            }
        }

        // Establish an edge between newcomer and node with highest centrality
        Edge edge = graphModel.factory().newEdge(newcomer, firstNode, 0, 1f, false);
        graph.addEdge(edge);

        // Add neighbours within distance d to
        Collection<Node> neighbors = graph.getNeighbors(firstNode, radius - 1).toCollection();
        covered.addAll(neighbors);

        for (int i = 0; i < iterations && covered.size() != 0; i++) {
            Node nextNode = getNextNode(centralityType);
            edge = graphModel.factory().newEdge(newcomer, nextNode, 0, 1f, false);
            graph.addEdge(edge);

            // Compute all nodes with distance d from selected node and add it to covered list
            neighbors = graph.getNeighbors(nextNode, radius - 1).toCollection();
            covered.addAll(neighbors);
            covered.remove(nextNode);   // Remove selected node from covered list
        }
    }

    @Override
    public Node getNextNode(String centralityType) {
        // Find node with max centrality within list of covered node
        Node nextNode = graphModel.factory().newNode();
        Column column = graphModel.getNodeTable().getColumn(centralityType);

        for (Node n : covered) {
            Double maxCentrality = (Double) nextNode.getAttribute(column);
            Double centrality = (Double) n.getAttribute(column);

            if (centrality >= maxCentrality) {
                nextNode = n;
            }
        }

        return nextNode;
    }

}
