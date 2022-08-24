/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package AdminServer;

import Taxi.Statistics.StatisticsInfo;
import Utility.Utility;
import com.google.gson.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import Taxi.Data.TaxiInfo;
import Taxi.Data.TaxiSchema;

import java.util.ArrayList;

@Path("/")
public class AdminServerServices {
    private final AdminServer administratorServer = AdminServer.getInstance();
    private final Gson gson = new Gson();

    /*
     * HTTP POST Request at "/taxi-init" for initializing a taxi client
     * ------------------------------------------------------------------
     * This method will receive the buffered information of a new taxi
     * process and add it through the 'addTaxi()' method exposed from the
     * administrator server.
     *
     * The input information regarding the taxi will contain a tentative
     * ID which could be used only if retained valid from the administrator
     * server (it will handle the checking), otherwise it will use an
     * arbitrary generated one.
     *
     * In both case the same or the new ID (contained in the TaxiInfo class)
     * will be returned to the client with the list of other taxis present
     * on the server. This will happen through a wrapper class "TaxiSchema".
     */
    @POST
    @Path("taxi-init")
    @Consumes("application/json")
    @Produces("application/json")
    public Response newTaxi(String json) {
        if (json.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Bad request or wrong formatting")
                    .build();
        }

        TaxiInfo inputTaxiInfo;
        try {
            inputTaxiInfo = gson.fromJson(json, TaxiInfo.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Bad request or wrong formatting")
                    .build();
        }

        String outputInfoJson;
        TaxiSchema taxiSchema = new TaxiSchema();
        synchronized (administratorServer) {
            // Saving the taxi list of the server first, in this way it will better approximate
            // the correct amount of taxis
            taxiSchema.setTaxis(AdminServer.getTaxis());
            // The addTaxi method will return a TaxiInfo with the possible corrected ID
            taxiSchema.setTaxiInfo(administratorServer.addTaxi(inputTaxiInfo));
        }
        outputInfoJson = gson.toJson(taxiSchema, TaxiSchema.class);

        return Response.ok(outputInfoJson).build();
    }

    /*
     * HTTP POST Request at "/get-taxis" for getting list of other taxis.
     * ------------------------------------------------------------------
     * it will return the list of the taxis which are present on the
     * administrator server.
     */
    @GET
    @Path("get-taxis")
    @Produces("application/json")
    public Response getOtherTaxis() {
        ArrayList<TaxiInfo> taxis = AdminServer.getTaxis();
        String outputInfo;
        try {
            outputInfo = gson.toJson(taxis, ArrayList.class);
            return Response.ok(outputInfo).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /*
     * Remove a single taxi given a taxi ID
     * ----------------------------------------------------------------
     * This function can be both called by the administrator client that
     * by the taxi process itself (both from CLI).
     */
    @DELETE
    @Path("del-taxi/{id}")
    @Consumes("application/json")
    public Response deleteTaxi(@PathParam("id") int taxiId) {
        boolean ans = AdminServer.getInstance().removeTaxi(taxiId);

        if (ans)
            return Response
                    .ok("Removal from the administrator server completed successfully.")
                    .build();
        else
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("The given ID was not found in the administrator server.")
                    .build();
    }

    @POST
    @Path("stats")
    @Consumes("application/json")
    public Response addLocalStats(String json) {
        if (json.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Bad request or wrong formatting")
                    .build();
        }

        StatisticsInfo statInfo;
        try {
            statInfo = gson.fromJson(json, StatisticsInfo.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Bad request or wrong formatting")
                    .build();
        }

        administratorServer.addLocalStatistics(statInfo);

        return Response
                .status(Response.Status.OK)
                .entity("The local statistics of the taxi " + statInfo.getTaxiID()
                        + " have been added correctly on the administrator server.")
                .build();
    }

    @GET
    @Path("stats/{id}_{n}")
    @Produces("application/json")
    public Response getTaxiLocalStats(@PathParam("id") int taxiId,
                                      @PathParam(("n")) int n) {
        System.out.println("Received " + taxiId + " and " + n);

        if (!administratorServer.taxiIsPresent(taxiId)) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("The requested ID is not present in the smart city.")
                    .build();
        }

        if (administratorServer.getLocalTaxiStats(taxiId).size() < n) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("The quantity of chosen measurements is bigger than the" +
                            "actual measurements, use a smaller number.")
                    .build();
        }

        StatisticsInfo avgTaxiStats = administratorServer.getAveragesStats(taxiId, n);
        String output = Utility.GSON.toJson(avgTaxiStats);

        return Response.ok().entity(output).build();
    }


    @GET
    @Path("stats/{timestamp1}_{timestamp2}")
    @Produces("application/json")
    public Response getTaxiLocalStatsTimestamp(@PathParam("timestamp1") long timestamp1,
                                               @PathParam(("timestamp2")) long timestamp2) {

        return Response.ok().entity("").build();
    }
}