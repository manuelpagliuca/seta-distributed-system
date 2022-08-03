/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package Server.Services;

import Schemes.TaxiSchema;
import Server.AdministratorServer;
import Clients.Taxi.TaxiInfo;
import com.google.gson.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;

@Path("/")
public class AdministratorServerServices {
    private AdministratorServer administratorServer = AdministratorServer.getInstance();
    private Gson gson = new Gson();

    @POST
    @Path("taxi-init")
    @Consumes("application/json")
    @Produces("application/json")
    public Response newTaxi(String json) {
        if (json.isEmpty()) {
            return Response.status(400, "Bad request or wrong formatting").build();
        }

        TaxiInfo inputTaxiInfo;
        try {
            inputTaxiInfo = gson.fromJson(json, TaxiInfo.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
            return Response.status(400, "Bad request or wrong formatting").build();
        }
        /* Set taxis before adding the new taxi, in this way it will better approximate the correct
           amount of taxis in the smartcity.*/
        String outputInfoJson;
        TaxiSchema taxiSchema = new TaxiSchema();
        taxiSchema.setTaxis((ArrayList<TaxiInfo>) AdministratorServer.getTaxis().clone());
        taxiSchema.setTaxiInfo(administratorServer.addTaxi(inputTaxiInfo));

        outputInfoJson = gson.toJson(taxiSchema, TaxiSchema.class);
        return Response.ok(outputInfoJson).build();
    }

    @POST
    @Path("get-taxis")
    @Consumes("application/json")
    @Produces("application/json")
    public Response getTaxis(String applicantTaxiJson) {
        if (applicantTaxiJson.isEmpty()) {
            return Response.status(400, "Bad request or wrong formatting").build();
        }

        TaxiInfo applicantTaxi = gson.fromJson(applicantTaxiJson, TaxiInfo.class);

        // TODO: something here
        //if(administratorServer.getTaxis())

        ArrayList<TaxiInfo> taxis = (ArrayList<TaxiInfo>) administratorServer.getTaxis().clone();
        taxis.removeIf(t -> t.getId() == applicantTaxi.getId());

        TaxiSchema outputTaxi = new TaxiSchema();
        outputTaxi.setTaxis(taxis);
        outputTaxi.setTaxiInfo(applicantTaxi);

        String outputInfo;
        try {
            outputInfo = gson.toJson(outputTaxi, TaxiSchema.class);
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