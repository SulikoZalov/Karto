package org.project.karto.application.util;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.project.karto.application.dto.common.ErrorMessage;

public class RestUtil {

    private RestUtil() {}

    public static WebApplicationException responseException(Response.Status status, String message) {
        return new WebApplicationException(Response
                .status(status)
                .entity(new ErrorMessage(message))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build());
    }
}