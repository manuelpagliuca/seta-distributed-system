package REST.ServerServices;

import REST.AdministratorServer;
import REST.Client.Taxi;
import REST.JSONClass.TaxiInitInfo;
import com.google.gson.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Random;

@Path("/")
public class Init {
    private AdministratorServer administratorServer;
    private Gson gson = new Gson();

    @POST
    @Path("taxi-init")
    @Consumes("application/json")
    @Produces("application/json")
    public Response newTaxi(String json) {
        if (json.isEmpty()) {
            return Response.status(400, "Bad request or wrong formatting").build();
        }
        TaxiInitInfo taxiInfos;

        try {
            taxiInfos = gson.fromJson(json, TaxiInitInfo.class);
        } catch (JsonParseException e) {
            return Response.status(400, "Bad request or wrong formatting").build();
        }

        Taxi taxi = new Taxi(taxiInfos.getId(), taxiInfos.getTaxis(), taxiInfos.getGrpcPort());

        // Add taxi and return the same ID if valid, otherwise a new valid one
        int id = AdministratorServer.getInstance().addTaxi(taxi);
        taxiInfos.setId(id);
        // Select and set a random district
        taxiInfos.setDistrict(randomDistrict());
        // Set a random position given the random district
        taxiInfos.setPosition(getPosition(taxiInfos.getDistrict()));
        // Set the list of the other available taxis
        HashMap<Integer, Taxi> otherTaxis =
                (HashMap<Integer, Taxi>) AdministratorServer.getInstance().getTaxis().clone();
        otherTaxis.remove(id);
        taxiInfos.setTaxis(otherTaxis);

        return Response.ok(gson.toJson(taxiInfos)).build();
    }

    private int randomDistrict() {
        Random rnd = new Random();
        final int lowerBound = 1;
        final int upperBound = 4;

        int district = rnd.nextInt(lowerBound, upperBound + 1);

        return district;
    }

    private int[] getPosition(int district) {
        int[] position = new int[2];

        switch (district) {
            case 1:
                position[0] = 0;
                position[1] = 0;
                break;
            case 2:
                position[0] = 0;
                position[1] = 9;
                break;
            case 3:
                position[0] = 9;
                position[1] = 0;
                break;
            case 4:
                position[0] = 9;
                position[1] = 9;
                break;
        }
        return position;
    }

    @GET
    @Produces("text/plain")
    public String helloWorld() {
        return "Hello world!";
    }

    @GET
    @Path("{name}")
    @Produces({"text/plain"})
    public String helloWorldName(@PathParam("name") String name) {

        return "Hello, " + name + "!";

    }

    @GET
    @Produces("application/json")
    public String helloWorld2() {
        return "{\"message\": \"helloWorld\"}";

    }

    @Path("inner")
    @GET
    @Produces("text/plain")
    public String innerHello() {

        return "Inner Hello!";
    }
}