package strategies;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

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

            // Compute all immediate neighbours from selected node and remove from uncovered list
            Collection<Node> neighbors = getNeighborhood(selectedNode, 1);
            System.out.println(neighbors.size());
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

    public HashSet<Node> getNeighborhood(Node root, int depth) {
        HashSet<Node> neighbourhood = new HashSet<>();
        LinkedList<Node> queue = new LinkedList<>();
        LinkedList<Node> nextQueue = new LinkedList<>();

        // Use root node as starting point
        neighbourhood.add(root);
        queue.add(root);

        for(int i=0; i<depth; i++) {
            // Iterate through all nodes at current depth to find unvisited neighbours
            while(!queue.isEmpty()) {
                Node currentNode = queue.pop();
                Collection<Node> currentNeighbours = graph.getNeighbors(currentNode).toCollection();

                // Iterate through all immediate neighbours to find new neighbours
                for(Node n : currentNeighbours) {
                    // Only add to neighbourhood and queue if node is unvisited
                    if(!neighbourhood.contains(n)) {
                        neighbourhood.add(n);
                        nextQueue.add(n);   // Unvisited nodes for next depth
                    }
                }
            }

            // Replace current queue with queue for next depth
            queue.addAll(nextQueue);
            nextQueue.clear();
        }

        // Make sure the neighbourhood doesn't contain the root node itself
        neighbourhood.remove(root);

        return neighbourhood;
    }
}