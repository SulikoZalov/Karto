package org.project.karto.application.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.io.InputStream;

import org.project.karto.application.dto.auth.CompanyRegistrationForm;
import org.project.karto.application.service.AdminService;
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
    @Path("/patner/cards/picture")
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
        return Response.ok(adminService.loadProfilePicture(companyName)).build();
    }

    @PATCH
    @Path("/ban/user")
    public Response banUser(@QueryParam("phone") String phone) {
        adminService.banUser(phone);
        return Response.ok().build();
    }
}
