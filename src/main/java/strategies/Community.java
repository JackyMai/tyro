package strategies;

import javafx.util.Pair;
import org.gephi.appearance.api.*;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.statistics.plugin.Modularity;
import org.openide.util.Lookup;


import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

/**
 * This algorithm uses the Louvain method to find the modularity of the nodes in the graph,
 * this information is used to divide the graph into partitions. A selectedNode with the highest
 * centrality is found for each partition. An edge is created between the newcomer and the selectedNodes
 * (starting with the selectedNodes with the larger partitions).
 */
public class Community extends Strategy {
    private static final String STARTING = "starting";
    private static final String INCREASING = "increasing";
    private static final String DECREASING = "decreasing";

    private Node newcomer;
    private AppearanceController appearanceController;
    private AppearanceModel appearanceModel;
    private Modularity modularity;
    private Function function;
    private Partition partition;

    private String condition;
    private double resolution;
    private double increment;
    private int communitiesNeeded;

    private Pair<Node, Double>[] record;

    public Community (String filePath, int iterations, boolean visualise, boolean test, String testFilePath){
        super(filePath, iterations, visualise, test, testFilePath);
    }

    @Override
    public void execute(Node newcomer) {
        this.newcomer = newcomer;

        appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        appearanceModel = appearanceController.getModel();

        // Modularity is calculated by Louvain method for community detection
        modularity = new Modularity();

        // Number of communities has to equal to the number of iterations
        // The higher the resolution, the fewer communities are found
        resolution = 1.0;
        increment = 0.5;
        condition = STARTING;
        communitiesNeeded = iterations + 1; // The extra community is to hold the Newcomer.

        boolean foundAppropriateResolution = false;

        while(!foundAppropriateResolution) {
            foundAppropriateResolution = refineResolution();
        }

        getTargetNodes();

        createEdges();
    }

    /**
     * This method will increase the resolution if there are too many communities,
     * it will decrease the resolution if there are too few communities.
     *
     * @return if true, then the resolution will produce the right number of communities.
     */
    private boolean refineResolution() {
        // Find modularity with current resolution
        modularity.setResolution(resolution);
        modularity.execute(graphModel);
        Column modColumn = graphModel.getNodeTable().getColumn(Modularity.MODULARITY_CLASS);
        function = appearanceModel.getNodeFunction(graph, modColumn, PartitionElementColorTransformer.class);

        try {
            partition = ((PartitionFunction) function).getPartition();
        } catch (NullPointerException e) {
            // function equals null. resolution equals approximately 0. every node is its own partition.
            resolution += increment;
            increment /= 2;
            return false;
        }

        // Refine resolution so that the correct number of communities are found
        if (partition.size() == communitiesNeeded) {
            return true;
        } else if (partition.size() > communitiesNeeded) {
            if (Objects.equals(condition, DECREASING)) {
                increment /= 2;
            }
            condition = INCREASING;
            resolution += increment;
            return false;
        } else if (partition.size() < communitiesNeeded) {
            if (Objects.equals(condition, INCREASING)) {
                increment /= 2;
            }
            condition = DECREASING;
            resolution -= increment;
            return false;
        }
        return false;
    }

    /**
     * This method finds the node with the highest centrality and places it in an array called record.
     */
    private void getTargetNodes() {
        Column betweenness = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);

        // Find the node with the highest centrality of each partition.
        // Pair[0] is for the partition with the value of 0, the Double is the betweenness of the Node.
        record = new Pair[partition.size()];

        for (Node node : graph.getNodes().toCollection()) {
            Double nodeBetweenness = (Double) node.getAttribute(betweenness);

            int value = (Integer) partition.getValue(node, graph);

            if (record[value] == null) {
                record[value] = new Pair<>(node, nodeBetweenness);
            } else {
                Double maxBetweenness = record[value].getValue();

                if (nodeBetweenness > maxBetweenness) {
                    record[value] = new Pair(node, nodeBetweenness);
                }
            }
        }
    }

    /**
     * This method creates an edge between each selected node and the newcomer.
     * The selected nodes that belong to larger partitions have their edges created earlier.
     * If visualisation is enabled, as each edge is created, the respective partition is colored.
     */
    private void createEdges() {
        // Color the newcomer black and every other node white
        Integer newcomerValue = (Integer) partition.getValue(newcomer, graph);

        if(visualise) {
            for (Object object : partition.getValues()) {
                Integer value = (Integer) object;

                if (Objects.equals(value, newcomerValue)) {
                    partition.setColor(value, Color.BLACK);
                } else {
                    partition.setColor(value, Color.WHITE);
                }
            }
        }

        // Starting with the largest partition, color the partitions, keep the selectedNodes black, create edge.
        int colorIndex = 0;
        ArrayList<Node> selectedNodes = new ArrayList<>();

        for (Object o : partition.getSortedValues()) {
            Integer value = (Integer) o;
            Node selectedNode = record[value].getKey();

            selectedNodes.add(selectedNode);

            if (!Objects.equals(value, newcomerValue)) {
                if (visualise) partition.setColor(value, visualizer.getColor(colorIndex++));

                Edge edge = graphModel.factory().newEdge(newcomer, selectedNode, 0, 1f, false);
                graph.addEdge(edge);

                if (test) exportUpdatedCentralities(newcomer);

                if (visualise) {
                    appearanceController.transform(function);

                    for (Node targetNode : selectedNodes) {
                        targetNode.setColor(Color.BLACK);
                        targetNode.setSize(40);
                    }

                    visualizer.updateView();
                }
            }
        }
    }
}
