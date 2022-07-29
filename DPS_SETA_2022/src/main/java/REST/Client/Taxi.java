package REST.Client;

import REST.JSONClass.TaxiInitInfos;
import com.google.gson.Gson;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

public class Taxi {
    final static String AdminServerAddr = "localhost";
    final static int adminServerPort = 9001;
    final static String adminServerUrl = "http://" + AdminServerAddr + ":" + adminServerPort;
    public float battery = 100.f;
    static int grpcPort;
    static long id;
    static List<Taxi> taxis;


    public static void main(String[] args) throws Exception {
        Client client = ClientBuilder.newClient();
        Response clientResponse = null;

        //GET REQUEST
        String initPath = "/init";
        clientResponse = getRequest(client, adminServerUrl + initPath);
        //System.out.println(clientResponse.getEntity().toString());
    }

    public static Response getRequest(Client client, String url) {
        WebTarget webTarget = client.target(url);

        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = builder.get();
        response.bufferEntity();

        String responseJson = null;
        try {
            responseJson = response.readEntity(String.class);
            System.out.println(responseJson);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Gson gson = new Gson();
        TaxiInitInfos dis = gson.fromJson(responseJson, TaxiInitInfos.class);
        System.out.println(dis.toString());

        return response;
    }

    public long getID() {
        return id;
    }
}