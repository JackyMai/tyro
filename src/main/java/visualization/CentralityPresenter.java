package visualization;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.statistics.plugin.EigenvectorCentrality;

import java.awt.*;
import java.util.HashMap;

/**
 * This class is used to organise the Visualiser to paint the nodes pending on their centralities.
 */
public class CentralityPresenter {
    Graph graph;
    Visualizer visualizer;

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
     * organise the Visualiser to paint the nodes pending on their centralities.
     */
    public void present() {
        GraphModel graphModel = graph.getModel();

        //remember what the color of each node is.
        HashMap<Node,Color> nodes = new HashMap<>();
        for (Node node : graph.getNodes().toCollection()) {
            nodes.put(node,node.getColor());
        }

        //paint each node depending on its centrality
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

        //reset each node to its original color
        for (Node node : graph.getNodes().toCollection()) {
            node.setColor(nodes.get(node));
        }
    }

    private void resetExtrema() {
        // DONT RESET originalMaxEccentricity or originalMaxDegree
        maxBetweenness = 0.0;
        maxCloseness = 0.0;
        maxEccentricity = 0.0;
        maxEigenvector = 0.0;
        maxDegree = 0.0;

        minBetweenness = Double.POSITIVE_INFINITY;
        minCloseness = Double.POSITIVE_INFINITY;
        minEccentricity = Double.POSITIVE_INFINITY;
        minEigenvector = Double.POSITIVE_INFINITY;
        minDegree = Double.POSITIVE_INFINITY;
    }

    private void findExtrema() {
        for (Node node : graph.getNodes().toCollection()) {
            double nodeBetweenness = (Double) node.getAttribute(betweenness);
            double nodeClosenness = (Double) node.getAttribute(closeness);
            double nodeEccentricity = (Double) node.getAttribute(eccentricity);
            double nodeEigenvector = (Double) node.getAttribute(eigenvector);
            double nodeDegree = graph.getDegree(node);

            if (nodeBetweenness > maxBetweenness) maxBetweenness = nodeBetweenness;
            if (nodeBetweenness < minBetweenness) minBetweenness = nodeBetweenness;

            if (nodeClosenness > maxCloseness) maxCloseness = nodeClosenness;
            if (nodeClosenness < minCloseness) minCloseness = nodeClosenness;

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
                if (centralityType.equals(CENTRALITY_SCORE)) {
                    node.setColor(visualizer.getColor(((Double)(
                                    (Double) node.getAttribute(betweenness) +
                                            (Double) node.getAttribute(closeness) +
                                            (Double) node.getAttribute(eigenvector))).floatValue()
                            ,(float) 3.3));

                } else if (centralityType.equals(BETWEENNESS)) {
                    node.setColor(visualizer.getColor(((Double) node.getAttribute(betweenness)).floatValue(),
                            (float) 1.1));

                } else if (centralityType.equals(CLOSENESS)) {
                    node.setColor(visualizer.getColor(((Double) node.getAttribute(closeness)).floatValue(),
                            (float) 1.1));

                } else if (centralityType.equals(EIGENVECTOR)) {
                    node.setColor(visualizer.getColor(((Double) node.getAttribute(eigenvector)).floatValue(),
                            (float) 1.1));

                } else if (centralityType.equals(DEGREE)) {
                    node.setColor(visualizer.getColor((float)(graph.getDegree(node)),
                            (float)(originalMaxDegree + originalMaxDegree/3)));

                } else if (centralityType.equals(ECCENTRICITY)) {
                    node.setColor(visualizer.getColor(
                            ((Double) node.getAttribute(eccentricity)).floatValue(),
                            (float) originalMaxEccentricity + (float) 1.0));
                }
            } else if (range.equals(RELATIVE)) {
                if (centralityType.equals(CENTRALITY_SCORE)) {
                    float denominator = (float) (rangeBetweenness + rangeCloseness + rangeEigenvector);
                    node.setColor(visualizer.getColor(
                            ((Double) (
                                    (Double) node.getAttribute(betweenness) - minBetweenness +
                                            (Double) node.getAttribute(closeness) - minCloseness+
                                            (Double) node.getAttribute(eigenvector) - minEigenvector)).floatValue(),
                            (float) (denominator + 0.3)));

                } else if (centralityType.equals(BETWEENNESS)) {
                    node.setColor(visualizer.getColor(
                            ((Double) node.getAttribute(betweenness)).floatValue() - (float) minBetweenness,
                            (float) rangeBetweenness + (float)rangeBetweenness/4));

                } else if (centralityType.equals(CLOSENESS)) {
                    node.setColor(visualizer.getColor(
                            ((Double) node.getAttribute(closeness)).floatValue() - (float) minCloseness,
                            (float) rangeCloseness + (float)0.1));

                } else if (centralityType.equals(EIGENVECTOR)) {
                    node.setColor(visualizer.getColor(
                            ((Double) node.getAttribute(eigenvector)).floatValue() - (float) minEigenvector,
                            (float) rangeEigenvector + (float)0.1));

                } else if (centralityType.equals(DEGREE)) {
                    node.setColor(visualizer.getColor((float)(graph.getDegree(node)),
                            (float) rangeDegree + (float)1));

                } else if (centralityType.equals(ECCENTRICITY)) {
                    node.setColor(visualizer.getColor(
                            ((Double) node.getAttribute(eccentricity)).floatValue() - (float) minEccentricity,
                            (float) rangeEccentricity + (float)1.0));

                }
            }
        }

        visualizer.snapShot(centralityType, range);
    }
}
