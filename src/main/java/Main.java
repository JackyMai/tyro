import strategies.CentrePeriphery;
import strategies.Global;
import strategies.Local;
import strategies.MinLeaf;

public class Main {
    private static final String GLOBAL = "global";
    private static final String LOCAL = "local";
    private static final String MINLEAF = "minLeaf";
    private static final String CENTREPERIPHERY = "centrePeriphery";

    public static void main(String args[]) {
        String strategy = GLOBAL;

        switch (strategy) {
            case GLOBAL:
                Global global = new Global();
                global.start();
                break;
            case LOCAL:
                Local local = new Local();
                local.start();
                break;
            case MINLEAF:
                MinLeaf minLeaf = new MinLeaf();
                minLeaf.start();
                break;
            case CENTREPERIPHERY:
                CentrePeriphery centrePeriphery = new CentrePeriphery();
                centrePeriphery.start();
                break;
        }
    }
}