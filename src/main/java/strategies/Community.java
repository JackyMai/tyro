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

public class Community extends Strategy {

    private  static final String STARTING = "starting";
    private  static final String INCREASING = "increasing";
    private  static final String DECREASING = "decreasing";

    @Override
    public void execute(Node newcomer) {

        AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        AppearanceModel appearanceModel = appearanceController.getModel();

        //Modularity is calculated by Louvain method for community detection
        Modularity modularity = new Modularity();
        Partition partition;

        // the number of communities has to equal to the number of iterations
        // the higher the resolution, the fewer communities are found
        double resolution = 1.0;
        double increment = 0.5;
        String condition = STARTING;
        int communitiesNeeded = iterations + 1; // the extra community is to hold the Newcomer.

        boolean foundAppropriateResolution = false;

        while(!foundAppropriateResolution) {

            //find modularity with current resolution
            modularity.setResolution(resolution);
            modularity.execute(graphModel);
            Column modColumn = graphModel.getNodeTable().getColumn(Modularity.MODULARITY_CLASS);
            Function function = appearanceModel.getNodeFunction(graph, modColumn, PartitionElementColorTransformer.class);

            try {
                partition = ((PartitionFunction) function).getPartition();
            } catch (NullPointerException e) {
                // function equals null. resolution equals approximately 0. every node is its own partition.
                resolution += increment;
                increment /= 2;
                continue;
            }

            //refine resolution so that the correct number of communities are found
            if (partition.size() == communitiesNeeded){
                foundAppropriateResolution = true;
            }else if (partition.size() > communitiesNeeded){
                if (condition == DECREASING){
                    increment /= 2;
                }
                condition = INCREASING;
                resolution += increment;
                continue;
            } else  if (partition.size() < communitiesNeeded){
                if (condition == INCREASING){
                    increment /= 2;
                }
                condition = DECREASING;
                resolution -= increment;
                continue;
            }

            if(foundAppropriateResolution) {

                distance.setNormalized(true);
                distance.execute(graph);
                Column betweenness = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);

                //find the node with the highest centrality of each partition.
                // Pair[0] is for the partition with the value of 0, the Double is the betweenness of the Node.
                Pair<Node, Double>[] record = new Pair[partition.size()];

                for (Node node : graph.getNodes().toCollection()){
                    Double nodeBetweenness = (Double) node.getAttribute(betweenness);

                    int value = (Integer) partition.getValue(node, graph);

                    if (record[value] == null){
                        record[value] = new Pair<>(node, nodeBetweenness);

                    } else {
                        Double maxBetweenness = record[value].getValue();

                        if (nodeBetweenness > maxBetweenness){
                            record[value] = new Pair(node, nodeBetweenness);
                        }
                    }
                }

                // color the newcomer black and every other node white
                Integer newcomerValue = (Integer) partition.getValue(newcomer, graph);

                for (Object object : partition.getValues()){
                    Integer value = (Integer)object;

                    if (value == newcomerValue){
                        partition.setColor(value, Color.BLACK);
                    }else {
                        partition.setColor(value, Color.WHITE);
                    }
                }

                //starting with the largest partition, color the partitions, keep the selectedNodes black, create edge.
                int colorIndex = 0;
                ArrayList<Node> selectedNodes = new ArrayList<>();

                for (Object o : partition.getSortedValues()) {
                    Integer value = (Integer) o;
                    Node selectedNode = record[value].getKey();

                    selectedNodes.add(selectedNode);

                    if (value != newcomerValue){
                        if (visualise) partition.setColor(value, visualizer.getColor(colorIndex++));

                        Edge edge = graphModel.factory().newEdge(newcomer, selectedNode, 0, 1f, false);
                        graph.addEdge(edge);

                        appearanceController.transform(function);

                        if (visualise) {
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
    }


    @Override
    public Node getNextNode(String centralityType) {
        return null;
    }
}
