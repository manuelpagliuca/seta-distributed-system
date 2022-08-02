/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package Server.Services;

import Server.AdministratorServer;
import Clients.Taxi.TaxiInfo;
import com.google.gson.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;

@Path("/")
public class AdministratorServerServices {
    private AdministratorServer administratorServer = AdministratorServer.getInstance();

    @POST
    @Path("taxi-init")
    @Consumes("application/json")
    @Produces("application/json")
    public Response newTaxi(String json) {
        if (json.isEmpty()) {
            return Response.status(400, "Bad request or wrong formatting").build();
        }

        TaxiInfo inputInfo, outputInfo;
        Gson gson = new Gson();

        try {
            inputInfo = gson.fromJson(json, TaxiInfo.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
            return Response.status(400, "Bad request or wrong formatting").build();
        }

        try {
            outputInfo = administratorServer.addTaxi(inputInfo);
        } catch (JsonParseException e) {
            e.printStackTrace();
            return Response.status(400, "Bad request or wrong formatting").build();
        }

        String outputInfoJson;
        try {
            outputInfoJson = gson.toJson(outputInfo, TaxiInfo.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
            return Response.status(400, "Bad request or wrong formatting").build();
        }

        try {
            return Response.ok(outputInfoJson).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(400, "something wrong").build();
        }
    }

    @POST
    @Path("get-taxis")
    @Consumes("application/json")
    @Produces("application/json")
    public Response getTaxis(String applicantTaxiJson) {
        if (applicantTaxiJson.isEmpty()) {
            return Response.status(400, "Bad request or wrong formatting").build();
        }

        Gson gson = new Gson();
        TaxiInfo applicantTaxi = gson.fromJson(applicantTaxiJson, TaxiInfo.class);

        //if(administratorServer.getTaxis())

        ArrayList<TaxiInfo> taxis = (ArrayList<TaxiInfo>) administratorServer.getTaxis().clone();
        taxis.removeIf(t -> t.getId() == applicantTaxi.getId());

        applicantTaxi.setTaxis(taxis);
        String outputInfo;
        try {
            outputInfo = gson.toJson(applicantTaxi, TaxiInfo.class);
            return Response.ok(outputInfo).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //TODO: Sensed error code
        return Response.status(400).build();
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