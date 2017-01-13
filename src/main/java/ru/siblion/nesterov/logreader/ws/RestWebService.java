package ru.siblion.nesterov.logreader.ws;

import ru.siblion.nesterov.logreader.type.Response;
import ru.siblion.nesterov.logreader.util.MyLogger;
import ru.siblion.nesterov.logreader.type.Request;

import javax.ws.rs.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by alexander on 14.12.2016.
 */
@Path("/restWebService")
public class RestWebService {
    private static final Logger logger = MyLogger.getLogger();

    @POST
    @Consumes(value = {"application/xml,application/json"})
    @Produces(value = {"application/xml,application/json"})
    public Response getListOfLogMessages(Request request) {
        logger.log(Level.INFO, "rest web webservice");
        try {
            logger.log(Level.INFO, "getting request: " + request);
            return request.getResponse();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Какая-то ошибка", e);
        }
        return null;
    }
}

