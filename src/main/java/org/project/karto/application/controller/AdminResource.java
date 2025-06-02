package org.project.karto.application.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Path;
import org.project.karto.application.service.AdminService;

@Path("/admin")
@RolesAllowed("ADMIN")
public class AdminResource {

    private final AdminService adminService;

    AdminResource(AdminService adminService) {
        this.adminService = adminService;
    }
}
