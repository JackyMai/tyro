import strategies.BrokerConnect;
import strategies.BrokerExpress;
import strategies.BrokerHybrid;
import strategies.CentrePeriphery;
import strategies.Global;
import strategies.Local;
import strategies.Community;

public class Main {
    private static final String GLOBAL = "global";
    private static final String LOCAL = "local";
    private static final String CENTRE_PERIPHERY = "centrePeriphery";
    private static final String BROKER_CONNECT = "brokerConnect";
    private static final String BROKER_EXPRESS = "brokerExpress";
    private static final String BROKER_HYBRID = "brokerHybrid";
    private static final String COMMUNITY = "community";

    public static void main(String args[]) {
        String strategy = BROKER_CONNECT;

        switch (strategy) {
            case GLOBAL:
                Global global = new Global();
                global.start();
                break;
            case LOCAL:
                Local local = new Local();
                local.start();
                break;
            case CENTRE_PERIPHERY:
                CentrePeriphery centrePeriphery = new CentrePeriphery();
                centrePeriphery.start();
                break;
            case BROKER_CONNECT:
                BrokerConnect brokerConnect = new BrokerConnect();
                brokerConnect.start();
                break;
            case BROKER_EXPRESS:
                BrokerExpress brokerExpress = new BrokerExpress();
                brokerExpress.start();
                break;
            case BROKER_HYBRID:
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