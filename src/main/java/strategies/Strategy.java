package strategies;

import org.gephi.filters.api.FilterController;
import org.gephi.graph.api.Column;
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

public abstract class Strategy implements Algorithm {
    GraphModel graphModel;
    UndirectedGraph graph;
    GraphDistance distance;
    final int iterations = 10;

    public void start() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        Workspace workspace = pc.getCurrentWorkspace();

        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        graph = graphModel.getUndirectedGraph();

        // Generate graph
        importTXT(workspace, "/graph/facebook_combined.txt");

        // Get centrality
        distance = new GraphDistance();
        distance.setDirected(false);
        distance.execute(graph);

        System.out.println("Average shortest path length of graph is: " + distance.getPathLength());
        System.out.println("Diameter of graph is: " + distance.getDiameter());
        System.out.println("Radius of graph is: " + distance.getRadius());

        System.out.println("Algorithm has started executing");

        // Create new node as newcomer
        Node newcomer = graphModel.factory().newNode("Newcomer");
        newcomer.setLabel("Newcomer");
        graph.addNode(newcomer);

        // Begin algorithm
        execute(newcomer);

        System.out.println("Algorithm completed, calculating metrics for newcomer");

        distance.execute(graph);

        Column betweenness = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        Column closeness = graphModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);
        Column eccentricity = graphModel.getNodeTable().getColumn(GraphDistance.ECCENTRICITY);

        System.out.println("Betweenness of newcomer is: " + newcomer.getAttribute(betweenness));
        System.out.println("Closeness of newcomer is: " + newcomer.getAttribute(closeness));
        System.out.println("Eccentricity of newcomer is: " + newcomer.getAttribute(eccentricity));
    }

    public void generateNWS(Workspace workspace, UndirectedGraph graph) {
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
