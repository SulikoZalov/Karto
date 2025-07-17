package org.project.karto.infrastructure.exceptions_handler;

import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.project.karto.domain.common.exceptions.DomainException;

@Provider
public class ExceptionHandler implements ExceptionMapper<Throwable> {

  @Override
  public Response toResponse(Throwable e) {
    Log.error("Global error handler: ", e);
    if (e instanceof DomainException) return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(e.getMessage())
            .build();

    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity("Unexpected error occurred. Please contact support.")
            .build();
  }
}