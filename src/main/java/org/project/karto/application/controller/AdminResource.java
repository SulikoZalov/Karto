package org.project.karto.application.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.project.karto.application.dto.auth.CompanyRegistrationForm;
import org.project.karto.application.service.AdminService;
import org.project.karto.domain.companies.value_objects.PictureOfCards;

import java.io.InputStream;
import java.util.Map;

import static org.project.karto.application.util.RestUtil.responseException;

@Path("/admin")
@RolesAllowed("ADMIN")
public class AdminResource {

    private final AdminService adminService;

    AdminResource(AdminService adminService) {
        this.adminService = adminService;
    }

    @POST
    @Path("/register/partner")
    public Response partnerRegistration(CompanyRegistrationForm registrationForm) {
        adminService.registerPartner(registrationForm);
        return Response.ok().build();
    }

    @PATCH
    @Path("/patner/cards/picture/put")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response addPicture(
            InputStream inputStream,
            @QueryParam("companyName") String companyName) {

        if (inputStream == null)
            throw responseException(Status.BAD_REQUEST, "Picture cannot be null.");

        adminService.putPartnerCardPicture(inputStream, companyName);
        return Response.accepted().build();
    }

    @GET
    @Path("/partner/cards/picture")
    public Response loadPicture(@QueryParam("companyName") String companyName) {
        PictureOfCards pictureOfCards = adminService.loadProfilePicture(companyName);
        return Response.ok(Map.of(
                "profilePicture", pictureOfCards.profilePicture(),
                "imageType", pictureOfCards.imageType()))
                .build();
    }

    @PATCH
    @Path("/ban/user")
    public Response banUser(@QueryParam("phone") String phone) {
        adminService.banUser(phone);
        return Response.ok().build();
    }
}
