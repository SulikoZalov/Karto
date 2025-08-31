package org.project.karto.application.controller;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response.Status;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.karto.application.dto.gift_card.GiftCardDTO;
import org.project.karto.application.dto.user.UserDTO;
import org.project.karto.application.pagination.PageRequest;
import org.project.karto.domain.common.value_objects.Email;
import org.project.karto.domain.user.entities.User;
import org.project.karto.domain.user.repository.UserRepository;
import org.project.karto.domain.user.values_objects.PersonalData;

import java.util.List;

import static org.project.karto.application.util.RestUtil.responseException;
import static org.project.karto.domain.common.util.Utils.required;

@Authenticated
@Path("/user/profile")
public class UserProfileResource {

  private final JsonWebToken jwt;

  private final UserRepository repo;

  UserProfileResource(UserRepository repo, Instance<JsonWebToken> jwt) {
    this.repo = repo;
    this.jwt = jwt.get();
  }

  @GET
  public UserDTO userProfile() {
    User user = repo
        .findBy(new Email(jwt.getName()))
        .orElseThrow();

    PersonalData profile = user.personalData();
    return new UserDTO(
        profile.firstname(),
        profile.surname(),
        profile.email(),
        profile.phone().orElse(null),
        profile.birthDate(),
        user.isVerified(),
        user.is2FAEnabled(),
        user.cashbackStorage().amount());
  }

  @GET
  @Path("/gift_cards")
  public List<GiftCardDTO> userCards(@QueryParam("pageSize") int limit, @QueryParam("pageNumber") int offset) {
    required("pageSize", limit);
    required("pageNumber", offset);

    return repo.userCards(new PageRequest(limit, offset), new Email(jwt.getName()))
            .orElseThrow(() -> responseException(Status.BAD_REQUEST, "No user gift card found for this page."));
  }
}
