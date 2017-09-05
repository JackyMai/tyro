package strategies;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class BrokerExpress extends Strategy {
    private Collection<Node> uncovered;

    public BrokerExpress(String filePath, int iterations, boolean visualise, boolean test, String testFilePath) {
        super(filePath, iterations, visualise, test, testFilePath);
    }

    @Override
    public void execute(Node newcomer) {
        uncovered = graph.getNodes().toCollection();

        for(int i=0; i<iterations && uncovered.size() != 0; i++) {
            // Establish edge between newcomer and selected node
            Node startNode = getStartNode();
            Node endNode = getEndNode(startNode);

            Edge startEdge = graphModel.factory().newEdge(newcomer, startNode, 0, 1f, false);
            Edge endEdge = graphModel.factory().newEdge(newcomer, endNode, 0, 1f, false);
            graph.addEdge(startEdge);
            if (test) exportUpdatedCentralities(newcomer);

            graph.addEdge(endEdge);
            if (test) exportUpdatedCentralities(newcomer);

            Collection<Node> startNeighbors = graph.getNeighbors(startNode).toCollection();
            Collection<Node> endNeighbors = graph.getNeighbors(endNode).toCollection();
            uncovered.removeAll(startNeighbors);
            uncovered.removeAll(endNeighbors);
            uncovered.remove(startNode);
            uncovered.remove(endNode);

            if(visualise) {
                startNode.setColor(visualizer.getColor(i));
                startNode.setSize(40);
                endNode.setColor(visualizer.getColor(i));
                endNode.setSize(40);
                visualizer.updateView();
            }
        }
    }

    private Node getStartNode() {
        // Choose start node with lowest degree
        int minDegree = 0;
        Node startNode = null;

        for(Node n : uncovered) {
            int degree = graph.getDegree(n);
            if(degree > minDegree) {
                startNode = n;
                minDegree = degree;
            }
        }

        if(startNode == null && minDegree == 0) {
            try {
                throw new NullPointerException();
            } catch (NullPointerException e) {
                System.out.println("Start Node is null, graph may contain isolated nodes");
            }
        }

        return startNode;
    }

    // Use BFS to find end node
    private Node getEndNode(Node startNode) {
        ArrayList<Node> set = new ArrayList<>();
        LinkedList<Node> queue = new LinkedList<>();

        set.add(startNode);
        queue.addLast(startNode);

        while(!queue.isEmpty()) {
            Node current = queue.pop();

            for(Node n : graph.getNeighbors(current)) {
                if(!set.contains(n)) {
                    set.add(n);
                    queue.add(n);
                }
            }
        }

        return set.get(set.size()-1);
    }
}
