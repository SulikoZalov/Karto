package org.project.karto.application.controller;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.project.karto.domain.common.value_objects.Email;
import org.project.karto.domain.user.entities.User;
import org.project.karto.domain.user.repository.UserRepository;
import org.project.karto.domain.user.values_objects.PersonalData;
import org.project.karto.application.dto.gift_card.GiftCardDTO;
import org.project.karto.application.dto.user.UserDTO;
import org.project.karto.application.pagination.PageRequest;
import org.eclipse.microprofile.jwt.JsonWebToken;
import static org.project.karto.domain.common.util.Utils.required;
import static org.project.karto.application.util.RestUtil.responseException;

import java.util.List;

@Authenticated
@Path("/user/profile")
public class UserProfileResource {

  private final JsonWebToken jwt;

  private final UserRepository repo;

  UserProfileResource(UserRepository repo, JsonWebToken jwt) {
    this.repo = repo;
    this.jwt = jwt;
  }

  @GET
  public Response userProfile() {
    User user = repo
        .findBy(new Email(jwt.getName()))
        .orElseThrow();

    PersonalData profile = user.personalData();
    return Response.ok(new UserDTO(
        profile.firstname(),
        profile.surname(),
        profile.email(),
        profile.phone().orElse(null),
        profile.birthDate(),
        user.isVerified(),
        user.is2FAEnabled(),
        user.cashbackStorage().amount())).build();
  }

  @GET
  @Path("/gift_cards")
  public Response userCards(@QueryParam("pageSize") int limit, @QueryParam("pageNumber") int offset) {
    required("pageSize", limit);
    required("pageNumber", offset);

    List<GiftCardDTO> listOfCards = repo
        .userCards(new PageRequest(limit, offset), new Email(jwt.getName()))
        .orElseThrow(() -> responseException(Status.BAD_REQUEST, "No user goft card found for this page."));

    return Response.ok(listOfCards).build();
  }
}
