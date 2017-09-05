import strategies.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class Main {
    private static final String BROKER_CONNECT = "brokerConnect";
    private static final String BROKER_EXPRESS = "brokerExpress";
    private static final String BROKER_HYBRID = "brokerHybrid";
    private static final String CENTRE_PERIPHERY = "centrePeriphery";
    private static final String COMMUNITY = "community";
//    private static final String GLOBAL = "global";
//    private static final String LOCAL = "local";
    private static final String RANDOM = "random";
    public static final String[] strategies = {BROKER_CONNECT, BROKER_EXPRESS, BROKER_HYBRID, CENTRE_PERIPHERY, COMMUNITY, RANDOM};
//    public static final String[] strategies = {RANDOM};

    private static final String BA_GRAPH = "barabasi-albert";
    private static final String WS_GRAPH = "watts-strogatz";
    public static final String[] ARTIFICIAL_GRAPH_TYPES = {BA_GRAPH, WS_GRAPH};

    public static final String[] GRAPH_SIZES = {"250", "500", "750", "1000", "1250", "1500", "1750", "2000"};

    public static  final String FACEBOOK = "facebook_combined";
    public static final String[] REAL_WORLD_GRAPHS = {FACEBOOK};

    private static int newEdgesCreated = 10;
    private static boolean visualise = false;
    private static boolean test = true;

    private static byte[] data;

    public static void main(String args[]) {

        runCompleteTest();
//        executeStrategy(BROKER_CONNECT);

    }

    private static void runCompleteTest(){
        data = createCsvTitle();

        new File("./results").mkdirs();

        for(String strategy : strategies){
            new File("./results/" + strategy).mkdirs();

            for (String graphType : ARTIFICIAL_GRAPH_TYPES){
                new File("./results/" + strategy + "/" + graphType).mkdirs();

                String graphName ="";
                if (graphType.equals(BA_GRAPH)) graphName = "ba";
                if (graphType.equals(WS_GRAPH)) graphName = "ws";

                for (String size : GRAPH_SIZES){
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

            for (String graphType : REAL_WORLD_GRAPHS){

                String testFilePath = "./results/" + strategy + "/real-world/" + graphType +".csv";
                createCsvFile(testFilePath);

                String graphFilePath = "/graph/" + graphType;
                if (graphType.equals(FACEBOOK)) graphFilePath += ".txt";

                executeStrategy(graphFilePath, strategy, testFilePath);
            }
        }
    }

    private static byte[] createCsvTitle(){
        String csvTitle = "Metrics for Graph before algorithm,,,";
        for (int i = 1; i<= newEdgesCreated; i++){
            csvTitle += ",Iteration " + i + ",,,";
        }
        csvTitle += ",Metrics for Graph after algorithm\n" + "trial";

        csvTitle += ",Avg shortest path,Diameter,Radius";
        for (int i = 0; i< newEdgesCreated; i++){
            csvTitle += ",Betweenness,Closeness,Eccentricity,Eigenvector";
        }
        csvTitle += ",Avg shortest path,Diameter,Radius\n";

        return csvTitle.getBytes();
    }

    private static void createCsvFile(String pathString){
        Path path = Paths.get(pathString);

        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path, CREATE))) {
            out.write(data, 0, data.length);
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    private static void executeStrategy(String graphFilePath, String strategy, String testFilePath){
        switch (strategy) {
            case CENTRE_PERIPHERY:
                CentrePeriphery centrePeriphery = new CentrePeriphery(graphFilePath, newEdgesCreated, visualise, test, testFilePath);
                centrePeriphery.start();
                break;
            case BROKER_CONNECT:
                BrokerConnect brokerConnect = new BrokerConnect(graphFilePath, newEdgesCreated, visualise, test, testFilePath);
                brokerConnect.start();
                break;
            case BROKER_EXPRESS:
                BrokerExpress brokerExpress = new BrokerExpress(graphFilePath, newEdgesCreated, visualise, test, testFilePath);
                brokerExpress.start();
                break;
            case BROKER_HYBRID:
                BrokerHybrid brokerHybrid = new BrokerHybrid(graphFilePath, newEdgesCreated, visualise, test, testFilePath);
                brokerHybrid.start();
                break;
            case COMMUNITY:
                Community community = new Community(graphFilePath, newEdgesCreated, visualise, test, testFilePath);
                community.start();
                break;
            case RANDOM:
                Random random = new Random(graphFilePath, newEdgesCreated, visualise, test, testFilePath);
                random.start();
                break;
            /*case GLOBAL:
                Global global = new Global(filePath, newEdgesCreated, visualise);
                global.start();
                break;
            case LOCAL:
                Local local = new Local(filePath, newEdgesCreated, visualise);
                local.start();
                break;*/
        }
    }
}