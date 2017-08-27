package strategies;

import org.gephi.filters.api.FilterController;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.generator.plugin.RandomGraph;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.plugin.file.ImporterCSV;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.preview.PreviewModelImpl;
import org.gephi.preview.api.*;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.swing.*;


public abstract class Strategy implements Algorithm {
    GraphModel graphModel;
    UndirectedGraph graph;
    GraphDistance distance;
    String centralityType = GraphDistance.BETWEENNESS;
    final int iterations = 10;

    PreviewController previewController;
    strategies.PreviewSketch previewSketch;
    G2DTarget target;
    int outputCount = 0;

    boolean showGraph = true; // showGraph doesn't work with a high frame rate.
    boolean exportGraph = true;
    boolean highFrameRate = false;
    boolean visualise = (showGraph || exportGraph);

    public void start() {

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        graph = graphModel.getUndirectedGraph();

        // Generate graph
        importTXT(workspace, "/graph/facebook_combined.txt");
//        importTXT(workspace, "/graph/testing_graph.txt");

        System.out.println("Successfully imported graph");

//------------------------------------------------------------------------------------------------------------------------------------------------
       System.out.println("start setting up JFrame");
       if (visualise) {
           setUpView();
       }
//------------------------------------------------------------------------------------------------------------------------------------------------

        System.out.println("Calculating initial metrics for graph...");

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
//------------------------------------------------------------------------------------------

        if (visualise) {
            newcomer.setSize(100);
            newcomer.setColor(Color.BLACK);
        }
//------------------------------------------------------------------------------------------

        graph.addNode(newcomer);

        // Begin algorithm
        execute(newcomer);

        System.out.println("Algorithm completed, calculating metrics for newcomer");

        distance.setNormalized(true);
        distance.execute(graph);

        Column betweenness = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        Column closeness = graphModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);
        Column eccentricity = graphModel.getNodeTable().getColumn(GraphDistance.ECCENTRICITY);

        System.out.println("Betweenness of newcomer is: " + newcomer.getAttribute(betweenness));
        System.out.println("Closeness of newcomer is: " + newcomer.getAttribute(closeness));
        System.out.println("Eccentricity of newcomer is: " + newcomer.getAttribute(eccentricity));

    }

    public void setUpView(){


        previewController = Lookup.getDefault().lookup(PreviewController.class);
        PreviewModelImpl previewModel = (PreviewModelImpl) previewController.getModel();
        PreviewProperties previewProperties = previewModel.getProperties();
//        previewProperties.putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
//        previewProperties.putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.DARK_GRAY));
        previewProperties.putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
//        previewProperties.putValue(PreviewProperty.EDGE_OPACITY, 100);
        previewProperties.putValue(PreviewProperty.BACKGROUND_COLOR, Color.LIGHT_GRAY);

        for (Node node : graph.getNodes().toCollection()){
            node.setColor(Color.WHITE);
        }

        if(showGraph){
            //New Processing target, get the PApplet
            target = (G2DTarget) previewController.getRenderTarget(RenderTarget.G2D_TARGET);
            previewSketch = new strategies.PreviewSketch(target);
//        previewController.refreshPreview();
        }

        updateView();

        if(showGraph) {
            //Add the applet to a JFrame and display
            JFrame frame = new JFrame("Tyro");
            frame.setLayout(new BorderLayout());

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(previewSketch, BorderLayout.CENTER);

            frame.setSize(1024, 768);

            //Wait for the frame to be visible before painting, or the result drawing will be strange
            frame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    previewSketch.resetZoom();
                }

                @Override
                public void componentResized(ComponentEvent e) {
                    previewSketch.resetZoom();
                }
            });
            frame.setVisible(true);
        }
    }

    public void updateView(){
        System.out.println("update started");
        if (highFrameRate) {
            for (int i = 0; i < 60; i++) {
                AutoLayout autoLayout = new AutoLayout(1, TimeUnit.SECONDS);
                privateUpdateView(autoLayout);
            }
        } else {
            //Layout for 1 minute
            AutoLayout autoLayout = new AutoLayout(1, TimeUnit.MINUTES);
            privateUpdateView(autoLayout);
        }
        System.out.println("update ended");
    }

    private void privateUpdateView(AutoLayout autoLayout){

        autoLayout.setGraphModel(graphModel);
        YifanHuLayout firstLayout = new YifanHuLayout(null, new StepDisplacement(1f));
        ForceAtlasLayout secondLayout = new ForceAtlasLayout(null);
        AutoLayout.DynamicProperty adjustBySizeProperty = AutoLayout.createDynamicProperty("forceAtlas.adjustSizes.name", Boolean.TRUE, 0.1f);//True after 10% of layout time
        AutoLayout.DynamicProperty repulsionProperty = AutoLayout.createDynamicProperty("forceAtlas.repulsionStrength.name", new Double(500.), 0f);//500 for the complete period
        autoLayout.addLayout(firstLayout, 0.5f);
        autoLayout.addLayout(secondLayout, 0.5f, new AutoLayout.DynamicProperty[]{adjustBySizeProperty, repulsionProperty});
        autoLayout.execute();

        if (showGraph) {
            previewController.refreshPreview();
            previewSketch.resetZoom();
        }

        if (exportGraph) {
            //Simple PDF export
            ExportController ec = Lookup.getDefault().lookup(ExportController.class);
            try {
                ec.exportFile(new File("Output_" + outputCount + ".png"));
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
            outputCount++;
        }
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

    protected Color getColor(int index) {
        return new Color(Color.HSBtoRGB((float)index/iterations,(float)1.0,(float)0.6));
    }
}
