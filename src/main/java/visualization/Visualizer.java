package visualization;

import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.api.ExportController;
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

public class Visualizer {
    private GraphModel graphModel;
    private PreviewController previewController;
    private PreviewSketch previewSketch;
    private int iterations;
    private int outputCount = 0;

    private final boolean SHOW_GRAPH = false;  // ShowGraph doesn't work with a high frame rate
    private final boolean EXPORT_GRAPH = true;
    private final boolean HIGH_FRAME_RATE = true;
    private final int FRAME_RATE = 60;

    public Visualizer(GraphModel graphModel, int iterations) {
        this.graphModel = graphModel;
        this.iterations = iterations;
    }

    public void setUpView(){
        previewController = Lookup.getDefault().lookup(PreviewController.class);
        PreviewModelImpl previewModel = (PreviewModelImpl) previewController.getModel();
        PreviewProperties previewProperties = previewModel.getProperties();
        previewProperties.putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
        previewProperties.putValue(PreviewProperty.BACKGROUND_COLOR, Color.LIGHT_GRAY);

        for (Node node : graphModel.getUndirectedGraph().getNodes().toCollection()){
            node.setColor(Color.WHITE);
        }

        if(SHOW_GRAPH){
            //New Processing target inserted into PreviewSketch (a JPanel)
            G2DTarget target = (G2DTarget) previewController.getRenderTarget(RenderTarget.G2D_TARGET);
            previewSketch = new PreviewSketch(target);
        }

        updateView();

        if(SHOW_GRAPH) {
            //Add the PreviewSketch to a JFrame and display
            JFrame frame = new JFrame("Tyro");
            frame.setLayout(new BorderLayout());

            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.add(previewSketch, BorderLayout.CENTER);

            frame.setSize(1024, 768);

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
    }

    public void updateView(){
        if (HIGH_FRAME_RATE) {
            // Layout based on configured frame rate
            for (int i = 0; i < FRAME_RATE; i++) {
                AutoLayout autoLayout = new AutoLayout(1, TimeUnit.SECONDS);
                privateUpdateView(autoLayout);
            }
        } else {
            // Layout for 1 minute
            AutoLayout autoLayout = new AutoLayout(1, TimeUnit.MINUTES);
            privateUpdateView(autoLayout);
        }

        System.out.println("Preview has updated");
    }

    private void privateUpdateView(AutoLayout autoLayout){
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
            //Simple PNG export, can export .png, .pdf, .svg, etc...
            ExportController ec = Lookup.getDefault().lookup(ExportController.class);
            try {
                ec.exportFile(new File("preview/Output_" + outputCount + ".png"));
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
            outputCount++;
        }
    }

    public Color getColor(int index) {
        return new Color(Color.HSBtoRGB((float)index/iterations, (float)1.0, (float)0.6));
    }
}
