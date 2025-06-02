package org.project.karto.application.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.project.karto.application.service.AdminService;

@Path("/admin/auth")
public class AdminAuthResource {

    private final AdminService adminService;

    AdminAuthResource(AdminService adminService) {
        this.adminService = adminService;
    }

    @POST
    @PermitAll
    @Path("/login")
    public Response login(@HeaderParam("X-VERIFICATION-KEY") String verificationKey) {
        return Response.ok(adminService.auth(verificationKey)).build();
    }
}
