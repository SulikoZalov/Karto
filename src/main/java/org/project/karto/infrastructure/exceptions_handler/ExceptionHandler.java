package org.project.karto.infrastructure.exceptions_handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hadzhy.jetquerious.exceptions.NotFoundException;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.project.karto.application.dto.common.ErrorMessage;
import org.project.karto.domain.common.exceptions.DomainException;
import org.project.karto.domain.user.exceptions.BannedUserException;

@Provider
public class ExceptionHandler implements ExceptionMapper<Throwable> {

  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public Response toResponse(Throwable e) {
    Log.error("Global error handler: ", e);
    if (e instanceof BannedUserException)
      return Response
          .status(Status.FORBIDDEN)
          .entity(errorMessage(e.getMessage()))
          .type(MediaType.APPLICATION_JSON)
          .build();

    if (e instanceof DomainException)
      return Response
          .status(Response.Status.BAD_REQUEST)
          .entity(errorMessage(e.getMessage()))
          .type(MediaType.APPLICATION_JSON)
          .build();

    if (e instanceof NotFoundException)
      return Response
          .status(Response.Status.NOT_FOUND)
          .type(MediaType.APPLICATION_JSON)
          .build();

    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(errorMessage("Unexpected error occurred. Please contact support."))
        .type(MediaType.APPLICATION_JSON)
        .build();
  }

  private static String errorMessage(String message) {
    try {
      String result = OBJECT_MAPPER.writeValueAsString(new ErrorMessage(message));
      return result;
    } catch (JsonProcessingException e) {
      Log.error("Unexpected error. Cannot process json in exception mapper. " + e.getMessage());
      return null;
    }
  }
}
