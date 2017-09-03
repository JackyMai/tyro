package strategies;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.plugin.file.ImporterCSV;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.EigenvectorCentrality;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;
import visualization.Visualizer;

import java.awt.*;
import java.io.File;


public abstract class Strategy implements Algorithm {
    GraphModel graphModel;
    UndirectedGraph graph;
    GraphDistance distance;
    EigenvectorCentrality eigenvectorCentrality;
    Visualizer visualizer;

    // Settings
    String filePath = "/graph/facebook_combined.txt";
    String centralityType = GraphDistance.BETWEENNESS;
    final int iterations = 10;
    final boolean visualise = false;

    public void start() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        graph = graphModel.getUndirectedGraph();

        // Generate graph
        importTXT(workspace, filePath);

        System.out.println("Successfully imported graph");

        // Set up visualization if enabled
        if (visualise) {
            System.out.println("Start setting up view");
            visualizer = new Visualizer(graphModel, iterations);
            visualizer.setUpView();
        }

        System.out.println("Calculating initial metrics for graph...");

        // Get centrality
        distance = new GraphDistance();
        distance.setDirected(false);
        distance.execute(graph);

        eigenvectorCentrality = new EigenvectorCentrality();
        eigenvectorCentrality.setDirected(false);
        eigenvectorCentrality.execute(graph);

        System.out.println("Average shortest path length of graph is: " + distance.getPathLength());
        System.out.println("Diameter of graph is: " + distance.getDiameter());
        System.out.println("Radius of graph is: " + distance.getRadius());

        System.out.println("Algorithm has started executing");

        // Create new node as newcomer
        Node newcomer = graphModel.factory().newNode("Newcomer");
        newcomer.setLabel("Newcomer");

        // Set size and colour for newcomer
        if (visualise) {
            newcomer.setSize(100);
            newcomer.setColor(Color.BLACK);
        }

        graph.addNode(newcomer);

        // Begin algorithm
        execute(newcomer);

        System.out.println("Algorithm completed, calculating metrics for newcomer");

        // Update centrality after algorithm ends
        distance.setNormalized(true);
        distance.execute(graph);
        eigenvectorCentrality.execute(graph);

        Column betweenness = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        Column closeness = graphModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);
        Column eccentricity = graphModel.getNodeTable().getColumn(GraphDistance.ECCENTRICITY);
        Column eigenvector = graphModel.getNodeTable().getColumn(EigenvectorCentrality.EIGENVECTOR);

        System.out.println("Betweenness of newcomer is: " + newcomer.getAttribute(betweenness));
        System.out.println("Closeness of newcomer is: " + newcomer.getAttribute(closeness));
        System.out.println("Eigenvector of newcomer is: " + newcomer.getAttribute(eigenvector));
        System.out.println("Eccentricity of newcomer is: " + newcomer.getAttribute(eccentricity));
    }

    private void importTXT(Workspace workspace, String filePath) {
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

    private void importSupportedFormat(Workspace workspace, String filePath) {
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
