package strategies;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.EigenvectorCentrality;
import org.gephi.statistics.plugin.GraphDistance;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;

public class BrokerConnect extends Global {
    private Collection<Node> uncovered;

    public BrokerConnect(String graphFilePath, String outputFilePath, int edgeLimit, boolean updateEveryRound, boolean visualise, boolean export) {
        super(graphFilePath, outputFilePath, edgeLimit, updateEveryRound, visualise, export);
    }

    @Override
    public void execute(Node newcomer) {
        ArrayList<Node> targets = new ArrayList<>();

        // Get list of uncovered nodes from input graph
        uncovered = graph.getNodes().toCollection();

        // Depth is the natural log of radius rounded to the nearest integer
        int radius = (int) distance.getRadius();
        int depth = (int) Math.round(Math.log(radius));

        for (int i = 0; i < edgeLimit && uncovered.size() != 0; i++) {
            // Find next node from list of uncovered node
            Node selectedNode = getNextNode();
            targets.add(selectedNode);

            // Establish edge between newcomer and selected node
            Edge edge = graphModel.factory().newEdge(newcomer, selectedNode, 0, 1f, false);
            graph.addEdge(edge);
            targets.add(selectedNode);

            // Compute all immediate neighbours from selected node and remove from uncovered list
            Collection<Node> neighbors = getNeighborhood(selectedNode, depth);
            uncovered.removeAll(neighbors);
            uncovered.remove(selectedNode);

            if (updateEveryRound) updateCentralities();
            if (export) exportCentralities(newcomer);
            if (visualise) {
                for (Node node : graph.getNeighbors(selectedNode).toCollection()){
                    node.setColor(visualizer.getColor(i));
                }
                selectedNode.setColor(visualizer.getColor(i));
                selectedNode.setSize(40);
                newcomer.setColor(Color.BLACK);
                visualizer.updateView();
            }
        }
    }

    private Node getNextNode() {
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

    /**
     * Centrality score is the sum of the normalized betweenness, closeness and eigenvector centrality
     *
     * @param node: the node where the centrality score will be calculated from
     * @return : the centrality score
     */
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
