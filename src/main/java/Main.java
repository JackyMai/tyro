import strategies.CentrePeriphery;
import strategies.Global;
import strategies.Local;
import strategies.MinLeaf;

public class Main {
    public static void main(String args[]) {
        Global global = new Global();
        global.start();

//        Local local = new Local();
//        local.start();
//
//        MinLeaf minLeaf = new MinLeaf();
//        minLeaf.start();
//
//        CentrePeriphery centrePeriphery = new CentrePeriphery();
//        centrePeriphery.start();
    }
}