package strategies;

import org.gephi.filters.api.FilterController;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.generator.plugin.RandomGraph;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.plugin.file.ImporterCSV;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import java.io.File;
import java.util.Collection;

public class Global implements Strategy {
    private GraphModel graphModel;
    private UndirectedGraph graph;
    private Collection<Node> uncovered;
    private final int iterations = 20;

    @Override
    public void execute() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        Workspace workspace = pc.getCurrentWorkspace();

        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        graph = graphModel.getUndirectedGraph();

        // Generate graph
        importTXT(workspace, "/graph/facebook_combined.txt");

        // Get list of uncovered nodes from input graph
        uncovered = graph.getNodes().toCollection();

        // Get centrality
        GraphDistance distance = new GraphDistance();
        distance.setDirected(false);
        distance.execute(graph);

        System.out.println("Average shortest path length of graph is: " + distance.getPathLength());
        System.out.println("Diameter of graph is: " + distance.getDiameter());
        System.out.println("Radius of graph is: " + distance.getRadius());

        int radius = (int) distance.getRadius();

        System.out.println("Algorithm has started executing");

        // Create new node as newcomer
        Node newcomer = graphModel.factory().newNode("Newcomer");
        newcomer.setLabel("Newcomer");
        graph.addNode(newcomer);

        // Begin algorithm
        for (int i = 0; i < iterations && uncovered.size() != 0; i++) {
            // Establish edge between newcomer and selected node
            Node nextNode = getNextNode(GraphDistance.BETWEENNESS);
            Edge edge = graphModel.factory().newEdge(newcomer, nextNode, 0, 1f, false);
            graph.addEdge(edge);

            // Compute all nodes with distance < rad(G) from selected node and remove from uncovered list
            Collection<Node> neighbors = graph.getNeighbors(nextNode, radius-1).toCollection();
            uncovered.removeAll(neighbors);
        }

        System.out.println("Algorithm completed, calculating metrics for newcomer");

        distance.execute(graph);

        Column betweenness = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        Column closeness = graphModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);
        Column eccentricity = graphModel.getNodeTable().getColumn(GraphDistance.ECCENTRICITY);

        System.out.println("Betweenness of newcomer is: " + newcomer.getAttribute(betweenness));
        System.out.println("Closeness of newcomer is: " + newcomer.getAttribute(closeness));
        System.out.println("Eccentricity of newcomer is: " + newcomer.getAttribute(eccentricity));
    }

    private Node getNextNode(String centralityType) {
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

    public void generateNWS(Workspace workspace) {
        Container container = Lookup.getDefault().lookup(Container.Factory.class).newContainer();
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);

        RandomGraph randomGraph = new RandomGraph();
        randomGraph.setNumberOfNodes(1000);
        randomGraph.setWiringProbability(0.005);
        randomGraph.generate(container.getLoader());

        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        importController.process(container, new DefaultProcessor(), workspace);

        // Remove all nodes with no neighbor, i.e. isolated nodes
        for (Node n : graph.getNodes().toArray()) {
            Node[] neighbors = graph.getNeighbors(n).toArray();
            if (neighbors.length == 0) {
                graph.removeNode(n);
            }
        }
    }

    public void importTXT(Workspace workspace, String filePath) {
        Container container;
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);

        try {
            File file = new File(getClass().getResource(filePath).toURI());
            container = importController.importFile(file, new ImporterCSV());
            container.getLoader().setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);  // Force UNDIRECTED
            container.getLoader().setAllowAutoNode(true);  // Create missing nodes
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        importController.process(container, new DefaultProcessor(), workspace);
    }

    public void importSupportedFormat(Workspace workspace, String filePath) {
        Container container;
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);

        try {
            File file = new File(getClass().getResource(filePath).toURI());
            container = importController.importFile(file);
            container.getLoader().setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);  // Force UNDIRECTED
            container.getLoader().setAllowAutoNode(true);  // Create missing nodes
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        importController.process(container, new DefaultProcessor(), workspace);
    }

}