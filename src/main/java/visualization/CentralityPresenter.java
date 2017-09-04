package visualization;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.GraphDistance;

import java.awt.*;
import java.util.HashMap;

public class CentralityPresenter {

    Graph graph;
    Visualizer visualizer;

    double originalMaxEccentricity;

    double maxBetweenness;
    double maxCloseness;
    double maxEccentricity;

    double minBetweenness;
    double minCloseness;
    double minEccentricity;

    double rangeBetweenness;
    double rangeCloseness;
    double rangeEccentricity;

    Column betweenness;
    Column closeness;
    Column eccentricity;

    private static final String ABSOLUTE = "absolute";
    private static final String RELATIVE = "relative";

    public CentralityPresenter(Graph graph, Visualizer visualizer){
        this.graph = graph;
        this.visualizer = visualizer;
    }

    public void present(){
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

        betweenness = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        closeness = graphModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);
        eccentricity = graphModel.getNodeTable().getColumn(GraphDistance.ECCENTRICITY);

        resetExtrema();

        findExtrema();

        if (originalMaxEccentricity == 0.0) originalMaxEccentricity = maxEccentricity;

        findRanges();

        centralityPaint(betweenness, ABSOLUTE);
        centralityPaint(betweenness, RELATIVE);
        centralityPaint(closeness, ABSOLUTE);
        centralityPaint(closeness, RELATIVE);
        centralityPaint(eccentricity, ABSOLUTE);
        centralityPaint(eccentricity, RELATIVE);

        //reset each node to its original color
        for (Node node : graph.getNodes().toCollection()) {
            node.setColor(nodes.get(node));
        }
    }

    private void resetExtrema(){
        // DONT RESET originalMaxEccentricity
        maxBetweenness = 0.0;
        maxCloseness = 0.0;
        maxEccentricity =0.0;

        minBetweenness = Double.POSITIVE_INFINITY;
        minCloseness = Double.POSITIVE_INFINITY;
        minEccentricity = Double.POSITIVE_INFINITY;
    }

    private void findExtrema(){

        for (Node node : graph.getNodes().toCollection()) {
            double nodeBetweenness = (Double) node.getAttribute(betweenness);
            double nodeClosenness = (Double) node.getAttribute(closeness);
            double nodeEccentricity = (Double) node.getAttribute(eccentricity);

            if (nodeBetweenness > maxBetweenness) {
                maxBetweenness = nodeBetweenness;
            }
            if (nodeBetweenness < minBetweenness) {
                minBetweenness = nodeBetweenness;
            }

            if (nodeClosenness > maxCloseness) {
                maxCloseness = nodeClosenness;
            }
            if (nodeClosenness < minCloseness) {
                minCloseness = nodeClosenness;
            }

            if (nodeEccentricity > maxEccentricity) {
                maxEccentricity = nodeEccentricity;
            }
            if (nodeEccentricity < minEccentricity) {
                minEccentricity = nodeEccentricity;
            }
        }
    }

    private void findRanges(){
        rangeBetweenness = maxBetweenness - minBetweenness;
        rangeCloseness = maxCloseness - minCloseness;
        rangeEccentricity = maxEccentricity - minEccentricity;
    }

    private void centralityPaint(Column centrality, String range){

        for (Node node : graph.getNodes().toCollection()) {
            if(range.equals(ABSOLUTE)) {
                if (centrality != eccentricity) {
                    node.setColor(visualizer.getColor(((Double) node.getAttribute(centrality)).floatValue(),
                            (float) 1.1));
                } else {
                    node.setColor(visualizer.getColor(
                            ((Double) node.getAttribute(eccentricity)).floatValue(),
                            (float) originalMaxEccentricity + (float) 1.0));
                }
            }
            if (range.equals(RELATIVE)){
                if (centrality == betweenness) {
                    node.setColor(visualizer.getColor(
                            ((Double) node.getAttribute(betweenness)).floatValue() - (float) minBetweenness,
                            (float) rangeBetweenness + (float)0.1));

                } else if (centrality == closeness) {
                    node.setColor(visualizer.getColor(
                            ((Double) node.getAttribute(closeness)).floatValue() - (float) minCloseness,
                            (float) rangeCloseness + (float)0.1));

                } else if (centrality == eccentricity) {
                    node.setColor(visualizer.getColor(
                            ((Double) node.getAttribute(eccentricity)).floatValue() - (float) minEccentricity,
                            (float) rangeEccentricity + (float)1.0));
                }
            }
        }


        if (centrality == betweenness) {
            visualizer.snapShot("betweenness", range);
        }else if (centrality == closeness){
            visualizer.snapShot("closeness", range);
        } else if (centrality == eccentricity){
            visualizer.snapShot("eccentricity", range);
        }
    }
}
