package strategies;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.GraphDistance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class Local extends Strategy {
    private HashSet<Node> covered;
    private ArrayList<Node> targets;

    public Local(String graphFilePath, String outputFilePath, int edgeLimit, boolean updateEveryRound, boolean visualise, boolean export) {
        super(graphFilePath, outputFilePath, edgeLimit, updateEveryRound, visualise, export);
    }

    @Override
    public void execute(Node newcomer) {
        covered = new HashSet<>();
        targets = new ArrayList<>();

        int radius = (int) distance.getRadius();

        // Find first node with max centrality from input graph
        Node selectedNode = graphModel.factory().newNode();
        Column column = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);

        for (Node n : graph.getNodes().toArray()) {
            Double maxCentrality = (Double) selectedNode.getAttribute(column);
            Double centrality = (Double) n.getAttribute(column);

            if (centrality >= maxCentrality) {
                selectedNode = n;
            }
        }

        // Establish an edge between newcomer and node with highest centrality
        Edge edge = graphModel.factory().newEdge(newcomer, selectedNode, 0, 1f, false);
        graph.addEdge(edge);
        targets.add(selectedNode);

        // Add neighbours within distance d to covered list, excluding the initial node
        Collection<Node> neighbors = graph.getNeighbors(selectedNode).toCollection();
        covered.addAll(neighbors);

        for (int i = 0; i < edgeLimit && covered.size() != 0; i++) {
            // Establish edge between newcomer and selected node
            selectedNode = getNextNode();
            edge = graphModel.factory().newEdge(newcomer, selectedNode, 0, 1f, false);
            graph.addEdge(edge);
            targets.add(selectedNode);

            // TODO: fix bug where same node may get chosen multiple times
            // Compute all nodes with distance d from selected node and add it to covered list
            neighbors = graph.getNeighbors(selectedNode, radius - 1).toCollection();
            covered.addAll(neighbors);
            covered.remove(selectedNode);   // Remove selected node from covered list
        }
    }

    public Node getNextNode() {
        // Find node with max centrality within list of covered node
        Node nextNode = graphModel.factory().newNode();
        Column column = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);

        for (Node n : covered) {
            Double maxCentrality = (Double) nextNode.getAttribute(column);
            Double centrality = (Double) n.getAttribute(column);

            if (centrality >= maxCentrality) {
//                System.out.println(maxCentrality);
                nextNode = n;
            }
        }

        return nextNode;
    }
}
