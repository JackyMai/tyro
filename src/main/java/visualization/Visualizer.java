package visualization;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PNGExporter;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.preview.PreviewModelImpl;
import org.gephi.preview.api.G2DTarget;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible for exporting png files, and showing JFrames required to visualise the graphs.
 */
public class Visualizer {
    private Graph graph;
    private GraphModel graphModel;
    private PreviewController previewController;
    private PreviewSketch previewSketch;
    private int iterations;
    private int outputCount = 0;
    private String graphFileName;
    private String strategyName;

    private final boolean SHOW_GRAPH = false;  // Produces a JFrame (doesn't work with a high frame rate)
    private final boolean EXPORT_GRAPH = true;  // Export screenshots of graph
    private final boolean WARMUP_LAYOUT = false; // Set true for larger graphs so that Layout of nodes has extra time
    private final boolean HIGH_FRAME_RATE = false; // Only true if you want to produce pictures for a video
    private final int FRAME_RATE = 60;

    private final boolean PRESENT_CENTRALITY = false; // Enables Centrality Presenter
    private CentralityPresenter centralityPresenter;

    private final int SCREENSHOT_WIDTH = 1920;
    private final int SCREENSHOT_HEIGHT = 1080;

    public Visualizer(Graph graph, int iterations, String graphFilePath, String outputFilePath) {
        this.graph = graph;
        this.graphModel = graph.getModel();
        this.iterations = iterations;
        this.graphFileName = graphFilePath.substring(graphFilePath.lastIndexOf("/"), graphFilePath.indexOf("."));
        this.strategyName = outputFilePath.split("/")[2];
    }

    public void setUpView() {
        if (PRESENT_CENTRALITY) {
            centralityPresenter = new CentralityPresenter(graph, this);
        }

        previewController = Lookup.getDefault().lookup(PreviewController.class);
        PreviewModelImpl previewModel = (PreviewModelImpl) previewController.getModel();
        PreviewProperties previewProperties = previewModel.getProperties();
        previewProperties.putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
        previewProperties.putValue(PreviewProperty.BACKGROUND_COLOR, Color.LIGHT_GRAY);

        for (Node node : graphModel.getUndirectedGraph().getNodes().toCollection()) {
            node.setColor(Color.WHITE);
        }

        if (SHOW_GRAPH) {
            // New Processing target inserted into PreviewSketch (a JPanel)
            G2DTarget target = (G2DTarget) previewController.getRenderTarget(RenderTarget.G2D_TARGET);
            previewSketch = new PreviewSketch(target);
        }

        if (WARMUP_LAYOUT) {
            layoutWarmUp();
        }

        updateView();

        if (SHOW_GRAPH) {
            displayJFrame();
        }
    }

    private void displayJFrame() {
        // Add the PreviewSketch to a JFrame and display
        JFrame frame = new JFrame("Tyro");
        frame.setSize(1024, 768);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(previewSketch, BorderLayout.CENTER);

        // Wait for the frame to be visible before painting, or the result drawing will be strange
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

    /**
     * This method calls for the JFrame to be refreshed and/or the graph to be reprinted
     * (pending on the boolean fields of this class).
     */
    public void updateView() {
        if (HIGH_FRAME_RATE) {
            // Layout based on configured frame rate
            for (int i = 0; i < FRAME_RATE; i++) {
                AutoLayout autoLayout = new AutoLayout(1, TimeUnit.SECONDS);
                updateView(autoLayout);
            }
        } else {
            // Organise the nodes on the display for 10 seconds
            AutoLayout autoLayout = new AutoLayout(10, TimeUnit.SECONDS);
            updateView(autoLayout);
        }

        if (PRESENT_CENTRALITY) {
            centralityPresenter.present();
        }

        System.out.println("Preview has updated");
    }

    /**
     * This method refreshes the JFrame and/or reprints the graph
     * (pending on the boolean fields of this class).
     */
    private void updateView(AutoLayout autoLayout) {
        autoLayout.setGraphModel(graphModel);
        YifanHuLayout firstLayout = new YifanHuLayout(null, new StepDisplacement(1f));
        ForceAtlasLayout secondLayout = new ForceAtlasLayout(null);
        AutoLayout.DynamicProperty adjustBySizeProperty = AutoLayout.createDynamicProperty("forceAtlas.adjustSizes.name", Boolean.TRUE, 0.1f); // True after 10% of layout time
        AutoLayout.DynamicProperty repulsionProperty = AutoLayout.createDynamicProperty("forceAtlas.repulsionStrength.name", 500., 0f); // 500 for the complete period
        autoLayout.addLayout(firstLayout, 0.5f);
        autoLayout.addLayout(secondLayout, 0.5f, new AutoLayout.DynamicProperty[]{adjustBySizeProperty, repulsionProperty});
        autoLayout.execute();

        if (SHOW_GRAPH) {
            previewController.refreshPreview();
            previewSketch.resetZoom();
        }

        if (EXPORT_GRAPH) {
            snapshot("preview/" + strategyName + "/" + graphFileName + "/Output_" + outputCount + ".png");
        }
    }

    /**
     * Prints a screenshot of the graph in PNG format without having it's layout altered
     * @param fullPath: the location where the screenshot will be exported to
     */
    private void snapshot(String fullPath) {
        File outputDir = new File(fullPath);
        outputDir.getParentFile().mkdirs();

        // Simple PNG export, can export .png, .pdf, .svg, etc...
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        PNGExporter pngExporter = (PNGExporter) ec.getExporter("png");
        pngExporter.setWidth(SCREENSHOT_WIDTH);
        pngExporter.setHeight(SCREENSHOT_HEIGHT);

        try {
            ec.exportFile(outputDir, pngExporter);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        outputCount++;
    }

    /**
     * Prints a screenshot of the graph in PNG format without having it's layout altered
     * @param centralityType specifies the location where the snapshot will go
     * @param range specifies the location where the snapshot will go
     */
    void snapshot(String centralityType, String range) {
        this.snapshot("preview/" + strategyName + "/" + graphFileName + "/" + centralityType + "/" + range +
                "/Output_" + outputCount + "_" + centralityType + range + ".png");
    }

    /**
     * This method is applied before the graph is shown, it is used on large graphs that require more time in order
     * for the autoLayout process to occur.
     */
    private void layoutWarmUp() {
        AutoLayout autoLayout = new AutoLayout(3, TimeUnit.MINUTES);
        autoLayout.setGraphModel(graphModel);
        YifanHuLayout firstLayout = new YifanHuLayout(null, new StepDisplacement(1f));
        ForceAtlasLayout secondLayout = new ForceAtlasLayout(null);
        AutoLayout.DynamicProperty adjustBySizeProperty = AutoLayout.createDynamicProperty("forceAtlas.adjustSizes.name", Boolean.TRUE, 0.1f); // True after 10% of layout time
        AutoLayout.DynamicProperty repulsionProperty = AutoLayout.createDynamicProperty("forceAtlas.repulsionStrength.name", 500., 0f); // 500 for the complete period
        autoLayout.addLayout(firstLayout, 0.5f);
        autoLayout.addLayout(secondLayout, 0.5f, new AutoLayout.DynamicProperty[]{adjustBySizeProperty, repulsionProperty});
        autoLayout.execute();
    }

    /**
     * This method returns the color that a node should be painted
     * @param numerator this int is 0 for the first node, 1 for the second node, etc...
     */
    public Color getColor(int numerator) {
        return new Color(Color.HSBtoRGB((float)numerator/iterations, (float)1.0, (float)0.6));
    }

    /**
     * This method returns the color that a node should be painted
     * @param numerator  this param is often a centrality measure
     * @param denominator this param is often the maximum value that a centrality measure can be
     */
    public Color getColor(float numerator, float denominator) {
        return new Color(Color.HSBtoRGB(numerator/denominator, (float)1.0, (float)0.6));
    }
}
