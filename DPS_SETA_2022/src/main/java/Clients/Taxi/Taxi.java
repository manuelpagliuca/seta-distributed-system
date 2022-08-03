/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package Clients.Taxi;

import Clients.SETA.RideInfo;
import Schemes.TaxiSchema;
import com.google.gson.Gson;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.paho.client.mqttv3.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

// Taxis are considered as single processes and not as threads
public class Taxi {
    private final static String ADMIN_SERVER_ADDR = "localhost";
    private final static int ADMIN_SERVER_PORT = 9001;
    private final static String ADMIN_SERVER_URL = "http://" + ADMIN_SERVER_ADDR + ":" + ADMIN_SERVER_PORT;
    private final static Gson gson = new Gson();

    private final static String broker = "tcp://localhost:1883";

    public static void main(String[] args) throws MqttException {
        Client client = ClientBuilder.newClient();
        int grpcPort = 3005;

        TaxiSchema taxiSchema = postInit(client, grpcPort);

        TaxiInfo thisTaxi = taxiSchema.getTaxiInfo();
        AtomicReference<ArrayList<TaxiInfo>> taxis = new AtomicReference<>(taxiSchema.getTaxis());

        updatesTaxisFromAdminServer(client, thisTaxi, taxis);

        // TODO: Iscrizione al topic MQTT del proprio distretto
        String clientId = MqttClient.generateClientId();
        MqttClient mqttClient = new MqttClient(broker, clientId, null);

        seekingRides(mqttClient, thisTaxi);

        //closingMqttConnection(mqttClient); // Utility function will be helpful when from console i want to quit the taxi

        // Debug
        while (true) {
            //printFormattedInfos(thisTaxi, taxis.get());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // TODO: Inizia l'acquisizione dei dati dal sensore
    }


    /*  Seeks for rides by connecting it to the MQTT broker and subscribing on the
        topic of the district. */
    private static MqttClient seekingRides(MqttClient mqttClient, TaxiInfo thisTaxi) throws MqttException {
        String topic = "seta/smartcity/rides/district" + thisTaxi.getDistrict();
        int qos = 2;
        System.out.println("Listening on topic: " + topic);
        
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        mqttClient.connect(connectOptions);

        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String msg = new String(message.getPayload());
                System.out.println(msg);
                RideInfo rideInfo = gson.fromJson(message.getPayload().toString(), RideInfo.class);

                // TODO: Algoritmo decentralizzato da accordare attraverso le gRPC

                // You enter this scope only if this taxi got the priority for taking the ride
                {
                    // TODO: al completamento della corsa aggiornare il distretto dell'oggetto this taxi in maniera che si
                    // metta in ascolto su topic corretto.
                    ride(rideInfo);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        mqttClient.subscribe(topic, qos);

        return mqttClient;
    }

    private static void ride(RideInfo rideInfo) throws InterruptedException {
        // TODO: Ricordarsi il consumo di batteria dei taxi

        // Requirement from the assignment paper
        final int DELIVERY_TIME = 5000;
        Thread.sleep(DELIVERY_TIME);


    }


    private static void updatesTaxisFromAdminServer(Client client, TaxiInfo thisTaxi, AtomicReference<ArrayList<TaxiInfo>> taxis) {
        Thread updatesFromServer = new Thread(() -> {
            while (true) {
                taxis.set(getTaxisOnServer(client, thisTaxi));
            }
        });
        updatesFromServer.start();
    }

    /*  Initialization of the Taxi through the administrator server.
        The taxi sends his sensible data to the administrator server, in this
        data there is the proposal of an ID. This will be checked from the server side
        if it is available or already taken, in the second case the server will return
        a valid ID.

        The server answer will contain the initial position of the taxi which is one of the
        four recharge stations in the smart city, this will depend from the random assignment
        of the district. */
    private static TaxiSchema postInit(Client client, int grpcPort) {
        // Send the taxi initialization request with a tentative random ID
        final String INIT_PATH = "/taxi-init";

        TaxiInfo initInfo = new TaxiInfo(generateRndID(), grpcPort, ADMIN_SERVER_ADDR);
        // Receive the initialization data from the server: valid ID, position, list of other taxis

        String serverInitInfos = postRequest(client, ADMIN_SERVER_URL + INIT_PATH, gson.toJson(initInfo));

        TaxiSchema info = gson.fromJson(serverInitInfos, TaxiSchema.class);
        return info;
    }

    private static ArrayList<TaxiInfo> getTaxisOnServer(Client client, TaxiInfo thisTaxi) {
        final String GET_PATH = "/get-taxis";

        String serverResponse = postRequest(client, ADMIN_SERVER_URL + GET_PATH, gson.toJson(thisTaxi));
        TaxiSchema ans = gson.fromJson(serverResponse, TaxiSchema.class);

        ArrayList<TaxiInfo> taxis = ans.getTaxis();

        return taxis;
    }

    // Utility
    public static String postRequest(Client client, String url, String body) {
        WebTarget webTarget = client.target(url);

        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = builder.post(Entity.json(body));
        response.bufferEntity();

        String responseJson = null;
        try {
            responseJson = response.readEntity(String.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return responseJson;
    }

    public static String getRequest(Client client, String url) {
        WebTarget webTarget = client.target(url);

        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = builder.get();
        response.bufferEntity();

        String responseJson = null;
        try {
            responseJson = response.readEntity(String.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return responseJson;
    }

    private static void closingMqttConnection(MqttClient mqttClient) throws MqttException {
        if (false) {
            mqttClient.disconnect();
        }
    }

    private static int generateRndID() {
        Random random = new Random();
        return random.nextInt(1, 101);
    }

    private static String printFormattedInfos(int id, int district, int[] position,
                                              float battery, ArrayList<TaxiInfo> taxis) {
        String infos = String.format("ID: " + id + ", District: " + district +
                ", Position: " + position[0] + ", " + position[1] +
                "Battery: " + battery + ", Other taxis: [");

        for (TaxiInfo t : taxis) {
            infos += t.getId() + ", ";
        }

        if (infos.endsWith(",")) {
            infos = infos.substring(0, infos.length() - 1);
        }
        return infos;
    }

    private static void printFormattedInfos(TaxiInfo initInfo, ArrayList<TaxiInfo> taxis) {
        String infos = "[" + initInfo.toString() + ", taxis=[";

        infos += "[";
        if (!taxis.isEmpty()) {
            for (TaxiInfo e : taxis)
                infos += "id=" + e.getId() + ",";
        }

        if (infos.endsWith(",")) {
            infos = infos.substring(0, infos.length() - 1);
        }
        infos += "]]";

        System.out.println(infos);
    }

    // Getters & Setters
}