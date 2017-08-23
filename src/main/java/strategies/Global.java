package strategies;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

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
            Edge edge = graphModel.factory().newEdge(newcomer, selectedNode, 0, 1f, false);
            graph.addEdge(edge);
            targets.add(selectedNode);

            // Compute all nodes with distance < rad(G) from selected node and remove from uncovered list
            // TODO: get nodes within distance rad(G) instead of immediate neighbours
            // Make a manual graph and test how getNeighbors work
            Collection<Node> neighbors = graph.getNeighbors(selectedNode).toCollection();
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
                nextNode = n;
            }
        }

        return nextNode;
    }

//    public HashSet<Node> getNeighbors(Node node, int depth) {
//        HashSet<Node> neighbors = new HashSet<>();
//        Collection<Node> unvisited = graph.getNeighbors(node).toCollection();
//
//        // Add all immediate neighbors to the set
//        neighbors.addAll(unvisited);
//
//        for(int i=0; i<depth; i++) {
//            for(Node n : unvisited) {
//                if(!neighbors.contains(n)) {
//                    unvisited.add
//                }
//                // Add all neighbours from current node into set
//                neighbors.addAll(graph.getNeighbors(n).toCollection());
//            }
//        }
//
//        return neighbors;
//    }
}