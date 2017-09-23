package visualization;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.EigenvectorCentrality;
import org.gephi.statistics.plugin.GraphDistance;

import java.awt.*;
import java.util.HashMap;

/**
 * This class is used to organise the Visualiser to paint the nodes pending on their centralities.
 */
public class CentralityPresenter {
    private Graph graph;
    private Visualizer visualizer;

    private double originalMaxEccentricity, originalMaxDegree;
    private double maxBetweenness, maxCloseness, maxEccentricity, maxEigenvector, maxDegree;
    private double minBetweenness, minCloseness, minEccentricity, minEigenvector, minDegree;
    private double rangeBetweenness, rangeCloseness, rangeEccentricity, rangeEigenvector, rangeDegree;

    private Column betweenness, closeness, eccentricity, eigenvector;

    private static final String BETWEENNESS = "betweenness";
    private static final String CLOSENESS = "closeness";
    private static final String ECCENTRICITY = "eccentricity";
    private static final String EIGENVECTOR = "eigenvector";
    private static final String DEGREE = "degree";
    private static final String CENTRALITY_SCORE = "centralityScore";

    private static final String ABSOLUTE = "absolute";
    private static final String RELATIVE = "relative";

    public CentralityPresenter(Graph graph, Visualizer visualizer) {
        this.graph = graph;
        this.visualizer = visualizer;
    }

    /**
     * Organise the Visualiser to paint the nodes pending on their centralities.
     */
    public void present() {
        GraphModel graphModel = graph.getModel();

        // Remember what the color of each node is.
        HashMap<Node,Color> nodes = new HashMap<>();
        for (Node node : graph.getNodes().toCollection()) {
            nodes.put(node,node.getColor());
        }

        // Paint each node depending on its centrality
        GraphDistance distance = new GraphDistance();
        distance.setDirected(false);
        distance.setNormalized(true);
        distance.execute(graph);

        EigenvectorCentrality eigenvectorCentrality = new EigenvectorCentrality();
        eigenvectorCentrality.setDirected(false);
        eigenvectorCentrality.execute(graph);

        betweenness = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        closeness = graphModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);
        eccentricity = graphModel.getNodeTable().getColumn(GraphDistance.ECCENTRICITY);
        eigenvector = graphModel.getNodeTable().getColumn(EigenvectorCentrality.EIGENVECTOR);

        resetExtrema();
        findExtrema();

        if (originalMaxEccentricity == 0.0) originalMaxEccentricity = maxEccentricity;
        if (originalMaxDegree == 0.0) originalMaxDegree = maxDegree;

        findRanges();

        centralityPaint(BETWEENNESS, ABSOLUTE);
        centralityPaint(BETWEENNESS, RELATIVE);
        centralityPaint(CLOSENESS, ABSOLUTE);
        centralityPaint(CLOSENESS, RELATIVE);
        centralityPaint(ECCENTRICITY, ABSOLUTE);
        centralityPaint(ECCENTRICITY, RELATIVE);
        centralityPaint(EIGENVECTOR, ABSOLUTE);
        centralityPaint(EIGENVECTOR, RELATIVE);
        centralityPaint(DEGREE, ABSOLUTE);
        centralityPaint(DEGREE, RELATIVE);
        centralityPaint(CENTRALITY_SCORE, ABSOLUTE);
        centralityPaint(CENTRALITY_SCORE, RELATIVE);

        // Reset each node to its original color
        for (Node node : graph.getNodes().toCollection()) {
            node.setColor(nodes.get(node));
        }
    }

    private void resetExtrema() {
        // DON'T RESET originalMaxEccentricity or originalMaxDegree
        maxBetweenness = maxCloseness = maxEccentricity = maxEigenvector = maxDegree = 0.0;
        minBetweenness = minCloseness = minEccentricity = minEigenvector = minDegree = Double.POSITIVE_INFINITY;
    }

    private void findExtrema() {
        for (Node node : graph.getNodes().toCollection()) {
            double nodeBetweenness = (Double) node.getAttribute(betweenness);
            double nodeCloseness = (Double) node.getAttribute(closeness);
            double nodeEccentricity = (Double) node.getAttribute(eccentricity);
            double nodeEigenvector = (Double) node.getAttribute(eigenvector);
            double nodeDegree = graph.getDegree(node);

            if (nodeBetweenness > maxBetweenness) maxBetweenness = nodeBetweenness;
            if (nodeBetweenness < minBetweenness) minBetweenness = nodeBetweenness;

            if (nodeCloseness > maxCloseness) maxCloseness = nodeCloseness;
            if (nodeCloseness < minCloseness) minCloseness = nodeCloseness;

            if (nodeEccentricity > maxEccentricity) maxEccentricity = nodeEccentricity;
            if (nodeEccentricity < minEccentricity) minEccentricity = nodeEccentricity;

            if (nodeEigenvector > maxEigenvector) maxEigenvector = nodeEigenvector;
            if (nodeEigenvector < minEigenvector) minEigenvector = nodeEigenvector;

            if (nodeDegree > maxDegree) maxDegree = nodeDegree;
            if (nodeDegree < minDegree) minDegree = nodeDegree;
        }
    }

    private void findRanges() {
        rangeBetweenness = maxBetweenness - minBetweenness;
        rangeCloseness = maxCloseness - minCloseness;
        rangeEccentricity = maxEccentricity - minEccentricity;
        rangeEigenvector = maxEigenvector - minEigenvector;
        rangeDegree = maxDegree - minDegree;
    }

    private void centralityPaint(String centralityType, String range) {
        for (Node node : graph.getNodes().toCollection()) {
            if(range.equals(ABSOLUTE)) {
                paintAbsoluteRange(centralityType, node);
            } else if (range.equals(RELATIVE)) {
                paintRelativeRange(centralityType, node);
            }
        }

        visualizer.snapShot(centralityType, range);
    }
    
    private void paintAbsoluteRange(String centralityType, Node node) {
        float numerator, denominator;
        switch (centralityType) {
            case CENTRALITY_SCORE:
                numerator = ((Double) ((Double) node.getAttribute(betweenness)
                                        + (Double) node.getAttribute(closeness)
                                        + (Double) node.getAttribute(eigenvector))).floatValue();
                denominator = 3.3f;
                node.setColor(visualizer.getColor(numerator, denominator));
                break;
            case BETWEENNESS:
                numerator = ((Double) node.getAttribute(betweenness)).floatValue();
                denominator = 1.1f;
                node.setColor(visualizer.getColor(numerator, denominator));
                break;
            case CLOSENESS:
                numerator = ((Double) node.getAttribute(closeness)).floatValue();
                denominator = 1.1f;
                node.setColor(visualizer.getColor(numerator, denominator));
                break;
            case EIGENVECTOR:
                numerator = ((Double) node.getAttribute(eigenvector)).floatValue();
                denominator = 1.1f;
                node.setColor(visualizer.getColor(numerator, denominator));
                break;
            case DEGREE:
                numerator = (float) (graph.getDegree(node));
                denominator = (float) (originalMaxDegree + originalMaxDegree / 3);
                node.setColor(visualizer.getColor(numerator, denominator));
                break;
            case ECCENTRICITY:
                numerator = ((Double) node.getAttribute(eccentricity)).floatValue();
                denominator = (float) (originalMaxEccentricity + 1.0f);
                node.setColor(visualizer.getColor(numerator, denominator));
                break;
        }
    }

    private void paintRelativeRange(String centralityType, Node node) {
        float numerator, denominator;
        switch (centralityType) {
            case CENTRALITY_SCORE:
                numerator = ((Double) ((Double) node.getAttribute(betweenness) - minBetweenness
                                        + (Double) node.getAttribute(closeness) - minCloseness
                                        + (Double) node.getAttribute(eigenvector) - minEigenvector)).floatValue();
                denominator = (float) (rangeBetweenness + rangeCloseness + rangeEigenvector + 0.3);
                node.setColor(visualizer.getColor(numerator, denominator));
                break;
            case BETWEENNESS:
                numerator = ((Double) node.getAttribute(betweenness)).floatValue() - (float) minBetweenness;
                denominator = (float) rangeBetweenness + (float) rangeBetweenness / 4;
                node.setColor(visualizer.getColor(numerator, denominator));
                break;
            case CLOSENESS:
                numerator = ((Double) node.getAttribute(closeness)).floatValue() - (float) minCloseness;
                denominator = (float) rangeCloseness + 0.1f;
                node.setColor(visualizer.getColor(numerator, denominator));
                break;
            case EIGENVECTOR:
                numerator = ((Double) node.getAttribute(eigenvector)).floatValue() - (float) minEigenvector;
                denominator = (float) rangeEigenvector + 0.1f;
                node.setColor(visualizer.getColor(numerator, denominator));
                break;
            case DEGREE:
                numerator = (float) (graph.getDegree(node));
                denominator = (float) rangeDegree + 1f;
                node.setColor(visualizer.getColor(numerator, denominator));
                break;
            case ECCENTRICITY:
                numerator = ((Double) node.getAttribute(eccentricity)).floatValue() - (float) minEccentricity;
                denominator = (float) rangeEccentricity + 1.0f;
                node.setColor(visualizer.getColor(numerator, denominator));
                break;
        }
    }
}
