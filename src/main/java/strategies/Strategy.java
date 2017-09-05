package strategies;

import org.gephi.graph.api.*;
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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;


public abstract class Strategy implements Algorithm {
    GraphModel graphModel;
    Graph graph;
    GraphDistance distance;
    EigenvectorCentrality eigenvectorCentrality;
    Visualizer visualizer;

    // Settings
    final String graphFilePath;
    final int iterations;
    final boolean visualise;
    final boolean test;
    final String testFilePath;

    public Strategy (String graphFilePath, int iterations, boolean visualise, boolean test, String testFilePath){
        this.iterations = iterations;
        this.graphFilePath = graphFilePath;
        this.visualise = visualise;
        this.test = test;
        this.testFilePath = testFilePath;
    }

    public void start() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        graph = graphModel.getUndirectedGraph();

        // Generate graph
        importGraph(workspace, graphFilePath);

        System.out.println("Successfully imported graph: " + graphFilePath + " for: " + this.getClass());

        // Set up visualization if enabled
        if (visualise) {
            System.out.println("Start setting up view");
            visualizer = new Visualizer(graph, iterations);
            visualizer.setUpView();
        }

        System.out.println("Calculating initial metrics for graph...");

        findCentralities();

        if (test) exportGraphMetrics(true);

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

        System.out.println("Calculating final metrics for graph...");

        if (test) {
            exportGraphMetrics(false);
        }
    }

    private void importGraph(Workspace workspace, String filePath) {
        Container container;
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);

        try {
            File file = new File(getClass().getResource(filePath).toURI());

            if (filePath.endsWith(".txt")) {
                container = importController.importFile(file, new ImporterCSV());
            } else {
                container = importController.importFile(file);
            }

            container.getLoader().setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);  // Force UNDIRECTED
            container.getLoader().setAllowAutoNode(true);  // Create missing nodes
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        importController.process(container, new DefaultProcessor(), workspace);
    }

    private void findCentralities(){
        eigenvectorCentrality = new EigenvectorCentrality();
        eigenvectorCentrality.setDirected(false);
        eigenvectorCentrality.execute(graph);

        distance = new GraphDistance();
        distance.setDirected(false);
        distance.setNormalized(true);
        distance.execute(graph);
    }

    public void exportGraphMetrics(boolean initialMetrics){

        int first = graphFilePath.lastIndexOf('/');
        int last = graphFilePath.indexOf('.');
        String firstCell = graphFilePath.substring(first +1, last);

        String csvString = "";
        if (initialMetrics) csvString += firstCell;

        csvString += "," + distance.getPathLength() + "," + distance.getDiameter() + "," + distance.getRadius();

        if (!initialMetrics) csvString += "\n";

        byte[] csvData = csvString.getBytes();

        Path path = Paths.get(testFilePath);

        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path, APPEND))) {
            out.write(csvData, 0, csvData.length);
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    public void exportUpdatedCentralities(Node newcomer){

        eigenvectorCentrality = new EigenvectorCentrality();
        eigenvectorCentrality.setDirected(false);
        eigenvectorCentrality.execute(graph);

        distance = new GraphDistance();
        distance.setDirected(false);
        distance.setNormalized(true);
        distance.execute(graph);

        Column betweenness = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        Column closeness = graphModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);
        Column eccentricity = graphModel.getNodeTable().getColumn(GraphDistance.ECCENTRICITY);
        Column eigenvector = graphModel.getNodeTable().getColumn(EigenvectorCentrality.EIGENVECTOR);

        String csvString = "," + newcomer.getAttribute(betweenness) + "," + newcomer.getAttribute(closeness) + "," +
                newcomer.getAttribute(eccentricity) + "," + newcomer.getAttribute(eigenvector);

        byte[] csvData = csvString.getBytes();

        Path path = Paths.get(testFilePath);

        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path, APPEND))) {
            out.write(csvData, 0, csvData.length);
        } catch (IOException e) {
            e.getStackTrace();
        }
    }
}
