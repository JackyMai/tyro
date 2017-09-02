import strategies.BrokerExpress;
import strategies.BrokerHybrid;
import strategies.CentrePeriphery;
import strategies.Global;
import strategies.Local;
import strategies.Community;

public class Main {
    private static final String GLOBAL = "global";
    private static final String LOCAL = "local";
    private static final String CENTREPERIPHERY = "centrePeriphery";
    private static final String BROKEREXPRESS = "brokerExpress";
    private static final String BROKERHYBRID = "brokerHybrid";
    private static final String COMMUNITY = "community";

    public static void main(String args[]) {
        String strategy = COMMUNITY;

        switch (strategy) {
            case GLOBAL:
                Global global = new Global();
                global.start();
                break;
            case LOCAL:
                Local local = new Local();
                local.start();
                break;
            case CENTREPERIPHERY:
                CentrePeriphery centrePeriphery = new CentrePeriphery();
                centrePeriphery.start();
                break;
            case BROKEREXPRESS:
                BrokerExpress brokerExpress = new BrokerExpress();
                brokerExpress.start();
                break;
            case BROKERHYBRID:
                BrokerHybrid brokerHybrid = new BrokerHybrid();
                brokerHybrid.start();
                break;
            case COMMUNITY:
                Community community = new Community();
                community.start();
                break;
        }
    }
}