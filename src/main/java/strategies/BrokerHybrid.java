package strategies;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.GraphDistance;

import java.util.ArrayList;
import java.util.Collection;

public class BrokerHybrid extends Strategy {
    private static final String STRATEGY_ONE = "strategyOne";
    private static final String STRATEGY_TWO = "strategyTwo";
    private int strategyOneCount = 0;
    private int strategyTwoCount = 0;
    private ArrayList<Double> stratOneRankCls = new ArrayList<>();
    private ArrayList<Double> stratTwoRankCls = new ArrayList<>();

    @Override
    public void execute(Node node) {
        // Run both algorithms in the first and second round

        // TODO: adjust selection of strategy based on edge limit
        for (int i=0; i < iterations; i++) {
            if (i % 2 == 0) {
                // Use first strategy
            } else {
                // Use second strategy
            }
        }
    }

    private Double getClosenessRank(Node node) {
        Collection<Node> nodeList = graph.getNodes().toCollection();
        Column column = graphModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);
        Double selectedCentrality = (Double) node.getAttribute(column);
        int counter = 0;

        for (Node n : nodeList) {
            Double centrality = (Double) n.getAttribute(column);

            if(centrality >= selectedCentrality) {
                counter++;
            }
        }

        return (double) (counter / graph.getNodeCount());
    }

    private double getUpperConfidenceBound(String strategy, Node node, int roundsPassed) {
        // Get number of times that the corresponding strategy is used
        int strategySelectionCount;
        if(strategy.equals(STRATEGY_ONE)) {
            strategySelectionCount = strategyOneCount;
        } else {
            strategySelectionCount = strategyTwoCount;
        }

        // TODO: finish the average function
        // Calculate value of the average function
        double avgFuncValue = 0;
        for(int i=1; i<=strategySelectionCount; i++) {
            avgFuncValue += getClosenessRank(node) - getClosenessRank(node);
        }

        avgFuncValue /= strategySelectionCount;

        // Calculate value of the padding function
        double paddingFuncValue = Math.sqrt((2 * Math.log(roundsPassed)) / strategySelectionCount);

        return avgFuncValue + paddingFuncValue;
    }

    @Override
    public Node getNextNode(String centralityType) {
        return null;
    }
}
