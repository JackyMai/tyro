package strategies;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import java.util.ArrayList;
import java.util.Collection;

public class Global extends Strategy {
    private Collection<Node> uncovered;

    @Override
    public void execute(Node newcomer) {
        ArrayList<Node> targets = new ArrayList<>();

        // Get list of uncovered nodes from input graph
        uncovered = graph.getNodes().toCollection();

        int radius = (int) distance.getRadius();

        for (int i = 0; i < iterations && uncovered.size() != 0; i++) {
            // Establish edge between newcomer and selected node
            Node selectedNode = getNextNode(centralityType);
            targets.add(selectedNode);
            Edge edge = graphModel.factory().newEdge(newcomer, selectedNode, 0, 1f, false);
            graph.addEdge(edge);

            // Compute all nodes with distance < rad(G) from selected node and remove from uncovered list
            Collection<Node> neighbors = graph.getNeighbors(selectedNode, radius - 1).toCollection();
            uncovered.removeAll(neighbors);
            uncovered.remove(selectedNode);
        }
    }

    @Override
    public Node getNextNode(String centralityType) {
        // Find node with max centrality within list of uncovered node
        Node nextNode = graphModel.factory().newNode();
        Column column = graphModel.getNodeTable().getColumn(centralityType);

        for (Node n : uncovered) {
            Double maxCentrality = (Double) nextNode.getAttribute(column);
            Double centrality = (Double) n.getAttribute(column);

            if (centrality >= maxCentrality) {
                System.out.println(maxCentrality);
                nextNode = n;
            }
        }

        return nextNode;
    }
}