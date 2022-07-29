package REST.ServerServices;

import REST.Client.Taxi;
import REST.JSONClass.TaxiInitInfos;
import com.google.gson.Gson;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Path("/")
public class Init {
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