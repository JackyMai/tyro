import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import strategies.CentrePeriphery;
import strategies.Global;
import strategies.Local;
import strategies.MinLeaf;

public class Main {
    private ProjectController pc;
    private Workspace workspace;

    public static void main(String args[]) {
        Global global = new Global();
        global.execute();

        Local local = new Local();
        local.execute();

        MinLeaf minLeaf = new MinLeaf();
        minLeaf.execute();

        CentrePeriphery centrePeriphery = new CentrePeriphery();
        centrePeriphery.execute();
    }
}