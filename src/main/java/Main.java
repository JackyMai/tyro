import strategies.BrokerConnect;
import strategies.BrokerExpress;
import strategies.BrokerHybrid;
import strategies.CentrePeriphery;
import strategies.Community;
import strategies.Global;
import strategies.Local;
import strategies.Random;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.CREATE;

public class Main {
    private static final String BROKER_CONNECT = "brokerConnect";
    private static final String BROKER_EXPRESS = "brokerExpress";
    private static final String BROKER_HYBRID = "brokerHybrid";
    private static final String CENTRE_PERIPHERY = "centrePeriphery";
    private static final String COMMUNITY = "community";
    private static final String GLOBAL = "global";
    private static final String LOCAL = "local";
    private static final String RANDOM = "random";
    private static final String[] strategies = {BROKER_CONNECT, BROKER_EXPRESS, CENTRE_PERIPHERY, COMMUNITY, RANDOM};

    private static final String BA_GRAPH = "barabasi-albert";
    private static final String WS_GRAPH = "watts-strogatz";
    private static final String[] ARTIFICIAL_GRAPH_TYPES = {BA_GRAPH, WS_GRAPH};

    private static final String[] GRAPH_SIZES = {"250", "500", "750", "1000", "1250", "1500", "1750", "2000"};

    public static  final String FACEBOOK_2k = "socfb-Amherst41";
    public static  final String FACEBOOK_4k = "facebook_combined";
    public static  final String FACEBOOK_6k = "socfb-MIT";
    public static final String[] REAL_WORLD_GRAPHS = {FACEBOOK_2k, FACEBOOK_4k, FACEBOOK_6k};

    private static int edgeLimit = 10;
    private static boolean updateEveryRound = false;
    private static boolean export = true;
    private static boolean visualise = false;

    private static byte[] data;

    public static void main(String args[]) {
        runCompleteTest();
        // executeStrategy();
    }

    private static void executeStrategy(String graphFilePath, String strategy, String testFilePath) {
        switch (strategy) {
            case CENTRE_PERIPHERY:
                CentrePeriphery centrePeriphery = new CentrePeriphery(graphFilePath, edgeLimit, updateEveryRound, visualise, export, testFilePath);
                centrePeriphery.start();
                break;
            case BROKER_CONNECT:
                BrokerConnect brokerConnect = new BrokerConnect(graphFilePath, edgeLimit, updateEveryRound, visualise, export, testFilePath);
                brokerConnect.start();
                break;
            case BROKER_EXPRESS:
                BrokerExpress brokerExpress = new BrokerExpress(graphFilePath, edgeLimit, updateEveryRound, visualise, export, testFilePath);
                brokerExpress.start();
                break;
            case BROKER_HYBRID:
                BrokerHybrid brokerHybrid = new BrokerHybrid(graphFilePath, edgeLimit, updateEveryRound, visualise, export, testFilePath);
                brokerHybrid.start();
                break;
            case COMMUNITY:
                Community community = new Community(graphFilePath, edgeLimit, updateEveryRound, visualise, export, testFilePath);
                community.start();
                break;
            case RANDOM:
                Random random = new Random(graphFilePath, edgeLimit, updateEveryRound, visualise, export, testFilePath);
                random.start();
                break;
            case GLOBAL:
                Global global = new Global(graphFilePath, edgeLimit, updateEveryRound, visualise, export, testFilePath);
                global.start();
                break;
            case LOCAL:
                Local local = new Local(graphFilePath, edgeLimit, updateEveryRound, visualise, export, testFilePath);
                local.start();
                break;
        }
    }

    private static void runCompleteTest() {
        data = createCsvTitle();

        new File("./results").mkdirs();

        for(String strategy : strategies) {
            new File("./results/" + strategy).mkdirs();

            for (String graphType : ARTIFICIAL_GRAPH_TYPES) {
                new File("./results/" + strategy + "/" + graphType).mkdirs();

                String graphName ="";
                if (graphType.equals(BA_GRAPH)) graphName = "ba";
                if (graphType.equals(WS_GRAPH)) graphName = "ws";

                for (String size : GRAPH_SIZES) {
                    String testFilePath = "./results/" + strategy + "/" + graphType + "/" + size +".csv";
                    createCsvFile(testFilePath);

                    for (int trial = 1; trial <=20; trial++) {
                        String trialString = Integer.toString(trial);
                        if (trial <10) trialString = "0" + trialString;

                        String graphFilePath = "/graph/" + graphType + "/" + graphName + "_" + size + "_" +
                                trialString + ".graphml";
                        executeStrategy(graphFilePath, strategy, testFilePath);
                    }
                }
            }
            new File("./results/" + strategy + "/real-world").mkdirs();

            for (String graphType : REAL_WORLD_GRAPHS) {
                String testFilePath = "./results/" + strategy + "/real-world/" + graphType +".csv";
                createCsvFile(testFilePath);

                String graphFilePath = "/graph/" + graphType;
                graphFilePath += ".txt";

                executeStrategy(graphFilePath, strategy, testFilePath);
            }
        }
    }

    private static byte[] createCsvTitle() {
        StringBuilder csvTitle = new StringBuilder("Metrics for Graph before algorithm,,,");
        for (int i = 1; i<= edgeLimit; i++) {
            csvTitle.append(",Iteration ").append(i).append(",,,");
        }
        csvTitle.append(",Metrics for Graph after algorithm\n" + "trial");

        csvTitle.append(",Avg shortest path,Diameter,Radius");
        for (int i = 0; i< edgeLimit; i++) {
            csvTitle.append(",Betweenness,Closeness,Eccentricity,Eigenvector");
        }
        csvTitle.append(",Avg shortest path,Diameter,Radius\n");

        return csvTitle.toString().getBytes();
    }

    private static void createCsvFile(String pathString) {
        Path path = Paths.get(pathString);

        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path, CREATE))) {
            out.write(data, 0, data.length);
        } catch (IOException e) {
            e.getStackTrace();
        }
    }
}