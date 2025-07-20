package org.project.karto.application.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.project.karto.application.dto.auth.CompanyRegistrationForm;
import org.project.karto.application.service.AdminService;

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
    @Path("/ban/user")
    public Response banUser(@QueryParam("phone") String phone) {
        adminService.banUser(phone);
        return Response.ok().build();
    }
}
