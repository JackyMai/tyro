package strategies;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.EigenvectorCentrality;
import org.gephi.statistics.plugin.GraphDistance;

import java.util.ArrayList;
import java.util.Collection;

public class BrokerConnect extends Global {
    private Collection<Node> uncovered;

    public BrokerConnect(String filePath, int iterations, boolean visualise, boolean test, String testFilePath) {
        super(filePath, iterations, visualise, test, testFilePath);
    }

    @Override
    public void execute(Node newcomer) {
        ArrayList<Node> targets = new ArrayList<>();

        // Get list of uncovered nodes from input graph
        uncovered = graph.getNodes().toCollection();

        // Depth is the natural log of radius rounded to the nearest integer
        int radius = (int) distance.getRadius();
        int depth = (int) Math.round(Math.log(radius));

        for (int i = 0; i < iterations && uncovered.size() != 0; i++) {
            // Find next node from list of uncovered node
            Node selectedNode = getNextNode();
            targets.add(selectedNode);

            // Establish edge between newcomer and selected node
            Edge edge = graphModel.factory().newEdge(newcomer, selectedNode, 0, 1f, false);
            graph.addEdge(edge);
            targets.add(selectedNode);

            if (test) exportUpdatedCentralities(newcomer);

            // Compute all immediate neighbours from selected node and remove from uncovered list
            Collection<Node> neighbors = getNeighborhood(selectedNode, depth);
            uncovered.removeAll(neighbors);
            uncovered.remove(selectedNode);

            if(visualise) {
                selectedNode.setColor(visualizer.getColor(i));
                selectedNode.setSize(40);
                visualizer.updateView();
            }
            if (test) exportUpdatedCentralities(newcomer);
        }
    }

    public Node getNextNode() {
        // Find node with max centrality within list of uncovered node
        Node selectedNode = graphModel.factory().newNode();

        for (Node n : uncovered) {
            Double maxCentrality = getCentralityScore(selectedNode);
            Double centrality = getCentralityScore(n);

            if (centrality >= maxCentrality) {
                selectedNode = n;
            }
        }

        return selectedNode;
    }

    private Double getCentralityScore(Node node) {
        Column betweenness = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        Column closeness = graphModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);
        Column eigenvector = graphModel.getNodeTable().getColumn(EigenvectorCentrality.EIGENVECTOR);

        Double betweennessValue = (Double) node.getAttribute(betweenness);
        Double closenessValue = (Double) node.getAttribute(closeness);
        Double eigenvectorValue = (Double) node.getAttribute(eigenvector);

        return betweennessValue + closenessValue + eigenvectorValue;
    }
}
