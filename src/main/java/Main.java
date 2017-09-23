import strategies.BrokerConnect;
import strategies.BrokerExpress;
import strategies.BrokerHybrid;
import strategies.CentrePeriphery;
import strategies.Community;
import strategies.Global;
import strategies.Local;
import strategies.Random;
import strategies.Strategy;

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

    private static final String FACEBOOK_1400 = "socfb-Haverford76";
    private static final String FACEBOOK_2600 = "socfb-Trinity100";
    private static final String FACEBOOK_2900 = "ego-facebook";
    private static final String FACEBOOK_4000 = "facebook_combined";
    private static final String[] REAL_WORLD_GRAPHS = {FACEBOOK_1400, FACEBOOK_2600, FACEBOOK_2900, FACEBOOK_4000};

    private static int edgeLimit = 10;
    private static boolean updateEveryRound = true;
    private static boolean visualise = false;
    private static boolean export = true;

    public static void main(String args[]) {
        runCompleteTest();
        // runSingleTest(BROKER_CONNECT, "/graph/barabasi-albert/ba_250_01.graphml", "./results/output.csv");   // Example
    }

    private static void executeStrategy(String graphFilePath, String strategy, String testFilePath) {
        switch (strategy) {
            case BROKER_CONNECT:
                Strategy brokerConnect = new BrokerConnect(graphFilePath, edgeLimit, updateEveryRound, visualise, export, testFilePath);
                brokerConnect.start();
                break;
            case BROKER_EXPRESS:
                Strategy brokerExpress = new BrokerExpress(graphFilePath, edgeLimit, updateEveryRound, visualise, export, testFilePath);
                brokerExpress.start();
                break;
            case BROKER_HYBRID:
                Strategy brokerHybrid = new BrokerHybrid(graphFilePath, edgeLimit, updateEveryRound, visualise, export, testFilePath);
                brokerHybrid.start();
                break;
            case CENTRE_PERIPHERY:
                Strategy centrePeriphery = new CentrePeriphery(graphFilePath, edgeLimit, updateEveryRound, visualise, export, testFilePath);
                centrePeriphery.start();
                break;
            case COMMUNITY:
                Strategy community = new Community(graphFilePath, edgeLimit, updateEveryRound, visualise, export, testFilePath);
                community.start();
                break;
            case RANDOM:
                Strategy random = new Random(graphFilePath, edgeLimit, updateEveryRound, visualise, export, testFilePath);
                random.start();
                break;
            case GLOBAL:
                Strategy global = new Global(graphFilePath, edgeLimit, updateEveryRound, visualise, export, testFilePath);
                global.start();
                break;
            case LOCAL:
                Strategy local = new Local(graphFilePath, edgeLimit, updateEveryRound, visualise, export, testFilePath);
                local.start();
                break;
        }
    }

    private static void runSingleTest(String strategy, String graphFilePath, String outputFilePath) {
        File outputDir = new File(outputFilePath);
        outputDir.getParentFile().mkdirs();     // Create parent directories if not already exist
        createCsvFile(getCsvTitle(), outputFilePath);
        executeStrategy(graphFilePath, strategy, outputFilePath);
    }

    private static void runCompleteTest() {
        for(String strategy : strategies) {
            for (String graphType : ARTIFICIAL_GRAPH_TYPES) {
                String graphName = "";

                if (graphType.equals(BA_GRAPH)) {
                    graphName = "ba";
                } else if (graphType.equals(WS_GRAPH)) {
                    graphName = "ws";
                }

                for (String size : GRAPH_SIZES) {
                    String outputFilePath = "./results/" + strategy + "/" + graphType + "/" + size +".csv";

                    for (int variationID = 1; variationID <= 20; variationID++) {
                        String graphFilePath = "/graph/" + graphType + "/" + graphName + "_" + size + "_" + String.format("%02d", variationID) + ".graphml";
                        runSingleTest(strategy, graphFilePath, outputFilePath);
                    }
                }
            }

            for (String graphType : REAL_WORLD_GRAPHS) {
                String graphFilePath = "/graph/real-world/" + graphType + ".txt";
                String outputFilePath = "./results/" + strategy + "/real-world/" + graphType +".csv";
                runSingleTest(strategy, graphFilePath, outputFilePath);
            }
        }
    }

    private static byte[] getCsvTitle() {
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

    private static void createCsvFile(byte[] csvData, String pathString) {
        Path path = Paths.get(pathString);

        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path, CREATE))) {
            out.write(csvData, 0, csvData.length);
        } catch (IOException e) {
            e.getStackTrace();
        }
    }
}