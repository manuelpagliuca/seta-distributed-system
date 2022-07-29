package REST.ServerServices;

import REST.AdministratorServer;
import REST.Client.Taxi;
import REST.JSONClass.TaxiInitInfos;
import com.google.gson.Gson;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Path("/")
public class Init {
    private AdministratorServer administratorServer;
    private Gson gson = new Gson();
    @GET
    @Path("init")
    @Produces("application/json")
    public String assignDistrict() {
        Random rnd = new Random();
        final int lowerBound = 1;
        final int upperBound = 4;
        int[] position = new int[2];
        int district = rnd.nextInt(lowerBound, upperBound + 1);

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
        List<Taxi> taxis = Collections.emptyList();
        TaxiInitInfos infos = new TaxiInitInfos(district, position, taxis);

        Gson gson = new Gson();
        return gson.toJson(infos);
    }

    @POST
    @Path("new_taxi")
    @Consumes("application/json")
    @Produces("application/json")
    public String addTaxi(String taxiInfoJson) {
        try {
            TaxiInitInfos taxiInfos = gson.fromJson(taxiInfoJson, TaxiInitInfos.class);
            Taxi taxi = new Taxi(taxiInfos.getId(), taxiInfos.getTaxis(), taxiInfos.getGrpcPort());
            System.out.println("Adding Taxi with ID: " + taxi.getID() + " to the Administrator Server");


            // TODO: Se l'ID non é corretto va cambiato
            int newID = AdministratorServer.getInstance().addTaxi(taxi);

            AdministratorServer.getInstance().printAllTaxis();

            taxiInfos.setDistrict(randomDistrict());
            taxiInfos.setPosition(getPosition(taxiInfos.getDistrict()));
            taxiInfos.setTaxis(AdministratorServer.getInstance().getTaxis());

            return gson.toJson(taxiInfos);

            //Response response = new Response();

            //return Response.ok().build();
        } catch (RuntimeException ex) {
            // Controllare che il codice d'errore sia corretto
            ex.printStackTrace();
            return gson.toJson(ex.getMessage());
        }
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