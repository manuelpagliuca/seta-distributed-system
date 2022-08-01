/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package REST.Services;

import REST.AdministratorServer;
import REST.JSONClass.TaxiInfo;
import com.google.gson.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

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
            return Response.status(400, "Bad request or wrong formatting").build();
        }

        try {
            outputInfo = administratorServer.addTaxi(inputInfo);
        } catch (JsonParseException e) {
            return Response.status(400, "Bad request or wrong formatting").build();
        }

        return Response.ok(gson.toJson(outputInfo, TaxiInfo.class)).build();
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