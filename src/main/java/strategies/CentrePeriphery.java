package strategies;

import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;
import org.gephi.graph.api.*;
import org.gephi.statistics.plugin.GraphDistance;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CentrePeriphery extends Strategy {
    private Collection<Node> uncovered;
    private Node newcomer;

    private Double overallMaxDistance = null;
    private int currentIteration;

    public CentrePeriphery (String filePath, int iterations, boolean visualise, boolean test, String testFilePath){
        super(filePath, iterations, visualise, test, testFilePath);
    }

    @Override
    public void execute(Node node) {

        this.newcomer = node;
        uncovered = graph.getNodes().toCollection();

        uncovered.remove(newcomer);

        connectToCentre();

        for(currentIteration = 1; currentIteration < iterations && uncovered.size() != 0; currentIteration++) {
            connectToPeriphery();
        }
    }

    /**
     * This method connects the newcomer to the centre of the graph with a new edge
     */
    private void connectToCentre(){
        distance.setNormalized(true);
        distance.execute(graph);
        Column betweenness = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);

        //find the node with the highest centrality.
        Node selectedNode = null;
        Double maxBetweenness = 0.0;

        for (Node node : graph.getNodes().toCollection()) {
            Double nodeBetweenness = (Double) node.getAttribute(betweenness);

            if (selectedNode == null) {
                selectedNode = node;
                maxBetweenness = nodeBetweenness;

            } else {
                if (nodeBetweenness > maxBetweenness) {
                    selectedNode = node;
                    maxBetweenness = nodeBetweenness;
                }
            }
        }

        //connect the newcomer to the centre of the graph
        Edge edge = graphModel.factory().newEdge(newcomer, selectedNode, 0, 1f, false);
        graph.addEdge(edge);

        //remove selected node from the uncovered collection
        uncovered.remove(selectedNode);

        if (visualise){
            selectedNode.setSize(40);
            selectedNode.setColor(Color.BLACK);

            updateView();
        }
    }

    /**
     * This method connects the newcomer to the peripheral node the highest centrality, with a new edge.
     */
    private void connectToPeriphery(){

        DijkstraShortestPathAlgorithm algorithm = new DijkstraShortestPathAlgorithm(graph, newcomer);
        algorithm.compute();

        //distances contains a record of all the shortest-path distances from the newcomer, to any node in the graph.
        HashMap<Node, Double> distances = algorithm.getDistances();

        Double maxDistance = algorithm.getMaxDistance();

        HashSet<Node> furthestNodes = new HashSet<>();

        for (Map.Entry<Node, Double> entry : distances.entrySet()) {

            Node candidateNode = entry.getKey();
            Double candidateDistance = entry.getValue();

            if (!uncovered.contains(candidateNode)) continue;

            if (candidateDistance != Double.POSITIVE_INFINITY && candidateDistance.equals(maxDistance)) {
                furthestNodes.add(candidateNode);
            }
        }

        Node selectedNode = null;

        //find candidate Node with the largest betweenness
        if (furthestNodes.size() == 1){
            Object[] object = furthestNodes.toArray();
            selectedNode = (Node) object[0];

        }else{
            distance.setNormalized(true);
            distance.execute(graph);
            Column betweenness = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);

            //find the node with the highest centrality.
            Double maxBetweenness = 0.0;

            for (Object object : furthestNodes.toArray()){
                Node candidateNode = (Node) object;
                Double nodeBetweenness = (Double) candidateNode.getAttribute(betweenness);

                if (selectedNode == null) {
                    selectedNode = candidateNode;
                    maxBetweenness = nodeBetweenness;

                } else {
                    if (nodeBetweenness > maxBetweenness) {
                        selectedNode = candidateNode;
                        maxBetweenness = nodeBetweenness;
                    }
                }
            }
        }

        //connect the newcomer to the centre of the graph
        Edge edge = graphModel.factory().newEdge(newcomer, selectedNode, 0, 1f, false);
        graph.addEdge(edge);

        uncovered.remove(selectedNode);

        if (visualise){
            selectedNode.setSize(40);
            selectedNode.setColor(Color.BLACK);
            updateView();
        }
    }

    /**
     * This method repaints the nodes of the selectedNode's community
     */
    private void updateView(){

        DijkstraShortestPathAlgorithm algorithm = new DijkstraShortestPathAlgorithm(graph, newcomer);
        algorithm.compute();

        //distances contains a record of all the shortest-path distances from the newcomer, to any node in the graph.
        HashMap<Node, Double> distances = algorithm.getDistances();

        if (overallMaxDistance == null) {
            overallMaxDistance = algorithm.getMaxDistance();
        }

        for (Map.Entry<Node, Double> entry : distances.entrySet()) {

            Node candidateNode = entry.getKey();
            Double candidateDistance = entry.getValue();

            if (!uncovered.contains(candidateNode)) continue;

            // newcomer distance is zero, selected node distance is one, neither will be repainted
            if (visualise) {
                candidateNode.setColor(visualizer.getColor(candidateDistance.floatValue() - 2,
                        overallMaxDistance.floatValue() - 1));
            }
        }

        visualizer.updateView();
    }
}
