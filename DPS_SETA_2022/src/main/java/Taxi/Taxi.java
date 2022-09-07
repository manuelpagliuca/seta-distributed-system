/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi;

import Taxi.Structures.LogicalClock;
import Taxi.Structures.TaxiInfo;
import Taxi.MQTT.MQTTModule;
import Taxi.Workers.Menu.InputCheckerThread;
import Taxi.Workers.Menu.CLIThread;
import Taxi.Workers.LocalStatsThread;
import Taxi.Statistics.PollutionBuffer;
import Taxi.Statistics.Simulators.PM10Simulator;
import Taxi.Workers.RechargeThread;
import Taxi.gRPC.GrpcModule;

import Misc.Utility;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.client.Client;

import Taxi.Structures.TaxiSchema;

import java.util.ArrayList;

import static Misc.Utility.*;

/*
 * Taxi
 * ------------------------------------------------------------------------------
 * This class represents a single taxi process (which in the test will be started
 * several times), it has its own logic clock that advances by a random offset
 * (chosen at) during the actions it will perform in its lifecycle.
 *
 * It has an endpoint with the administrator server, in fact it will benefit from
 * the REST API exposed by the latter.
 *
 * It presents a module to manage communications through gRPC, the services of the
 * latter are implemented on the class 'GrpcServices'.
 *
 * There is another module to handle MQTT communications.
 *
 * The taxi once started process initiates an initialization/registration procedure
 * on the adminsitrator server through POST. It then prepares threads to handle
 * local statistics, pollution simulators, user command line, and battery check
 * (to then start the charging procedure).
 *
 * The threads are started in a specific order that must be respected, then the
 * server side of the taxi is started for gRPC communication.
 *
 * Finally, an initialization procedure is started for MQTT through the module,
 * this will subscribe to the topics of interest and manage the publications and
 * messages received (runs).
 */
public class Taxi {
    // Server addresses
    private final static String ADMIN_SERVER_ADDRESS = "localhost";
    private final static int ADMIN_SERVER_PORT = 9001;
    private final static String ADMIN_SERVER_URL = "http://" + ADMIN_SERVER_ADDRESS + ":" + ADMIN_SERVER_PORT;

    // Logical clock
    public final static long CLOCK_OFFSET = Utility.generateRndLong(0, 15L);
    public final static LogicalClock logicalClock = new LogicalClock(CLOCK_OFFSET);
    private final static GrpcModule grpcModule = GrpcModule.getInstance();
    private static final TaxiInfo thisTaxi = new TaxiInfo();
    private static Client client;
    private static ArrayList<TaxiInfo> otherTaxis = new ArrayList<>();

    public static void main(String[] args) {
        logicalClock.increment();

        // Procedure for initialize through the administrator server
        postInit();

        // Create Local Stats & PM10 threads
        PollutionBuffer pollutionBuffer = new PollutionBuffer();
        PM10Simulator pm10SimulatorThread =
                new PM10Simulator(Integer.toString(generateRndInteger(0, 10)), pollutionBuffer);

        LocalStatsThread localStatsThread =
                new LocalStatsThread(thisTaxi, ADMIN_SERVER_URL, client, pollutionBuffer);

        // Create recharge thread
        Object checkBattery = new Object();
        RechargeThread rechargeThread = new RechargeThread(thisTaxi, otherTaxis, checkBattery, grpcModule);

        // Create the thread for CLI and for checking the input
        Object inputAvailable = new Object();
        Object checkRechargeCLI = new Object();
        CLIThread cliThread = new CLIThread(thisTaxi, otherTaxis, inputAvailable, checkRechargeCLI, rechargeThread);
        InputCheckerThread inputCheckerThread = new InputCheckerThread(inputAvailable);

        // Start the threads
        cliThread.start();
        inputCheckerThread.start();
        localStatsThread.start();
        pm10SimulatorThread.start();
        rechargeThread.start();

        // GRPC
        TaxiSchema taxiSchema = new TaxiSchema();
        taxiSchema.setTaxiInfo(thisTaxi);
        taxiSchema.setTaxis(otherTaxis);

        grpcModule.setTaxiData(taxiSchema);
        grpcModule.setClockAndPort(logicalClock, thisTaxi.getGrpcPort());
        grpcModule.startServer();
        grpcModule.broadcastPresentationSync();

        // MQTT
        MQTTModule mqttModule = new MQTTModule(taxiSchema, checkBattery, checkRechargeCLI);
        mqttModule.initMQTTConnection();

        // TODO: Function for terminating correctly all the threads.
    }

    /*
     * Initialization of the Taxi process through the administrator server
     * ------------------------------------------------------------------------------
     * The taxi sends his generated data to the administrator server, in which
     * there is a proposal ID. This will be checked from server side and in the case
     * it is already taken (not available) the server will return a valid ID.
     *
     * The server answer will contain the initial position of the taxi which is one of the
     * four recharge stations in the smart city, this will depend on the random assignment
     * of the district.
     */
    private static void postInit() {
        thisTaxi.setGrpcPort(Utility.generateRndInteger(0, 65535));
        client = ClientBuilder.newClient();

        // Send the taxi initialization request with a tentative random ID
        final String INIT_PATH = "/taxi-init";
        final int taxiID = Utility.generateRndInteger(0, 100 + 1);
        TaxiInfo initInfo = new TaxiInfo(taxiID, thisTaxi.getGrpcPort(), ADMIN_SERVER_ADDRESS);

        // Receive the initialization data from the server: valid ID, position, list of other taxis
        String serverInitInfos = Utility.postRequest(client, ADMIN_SERVER_URL + INIT_PATH, GSON.toJson(initInfo));

        TaxiSchema taxiSchema = GSON.fromJson(serverInitInfos, TaxiSchema.class);

        TaxiInfo serverData = taxiSchema.getTaxiInfo();

        thisTaxi.setId(serverData.getId());
        thisTaxi.setGrpcPort(serverData.getGrpcPort());
        thisTaxi.setPosition(serverData.getPosition());
        thisTaxi.setDistrict(serverData.getDistrict());
        thisTaxi.setBattery(40);
        otherTaxis = taxiSchema.getTaxis();

        System.out.println(serverData);
    }

    /*
     * Delete this taxi from the administrator server
     * ------------------------------------------------------------------------------
     * Build the specific URL path for performing the DELETE request on the
     * administrator server.
     */
    public static void removeTaxi() throws InterruptedException {
        grpcModule.broadcastGoodbyeSync();

        final String INIT_PATH = "/del-taxi/" + thisTaxi.getId();
        String serverInitInfos = delRequest(client, ADMIN_SERVER_URL + INIT_PATH);
        System.out.println(serverInitInfos);
        System.exit(0);
    }
}