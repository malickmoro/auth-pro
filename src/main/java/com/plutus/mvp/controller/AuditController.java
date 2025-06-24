package com.plutus.mvp.controller;

import com.plutus.mvp.dto.AuditLogDTO;
import com.plutus.mvp.security.RolesAllowed;
import com.plutus.mvp.service.AuditService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/admin/audit")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuditController {
    @Inject
    private AuditService auditService;

    @GET
    @Path("/logs")
    @RolesAllowed("ADMIN")
    public Response getAuditLogs(
        @QueryParam("page") 
        @DefaultValue("1") int page,
        @QueryParam("size") 
        @DefaultValue("10") int size
    ) {
        try {
            // Validate page and size parameters
            page = Math.max(1, page);
            size = Math.min(Math.max(1, size), 100); // Limit page size

            // Fetch paginated audit logs
            AuditLogDTO auditLogs = auditService.getPaginatedLogs(page, size);

            return Response.ok(auditLogs).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error retrieving audit logs")
                .build();
        }
    }

    @DELETE
    @Path("/logs/cleanup")
    @RolesAllowed("ADMIN")
    public Response cleanupOldLogs(
        @QueryParam("retentionDays") 
        @DefaultValue("30") int retentionDays
    ) {
        try {
            // Validate retention days
            retentionDays = Math.max(7, Math.min(retentionDays, 365));

            // Cleanup old logs
            auditService.cleanupOldLogs(retentionDays);

            return Response.ok("Audit logs cleaned up successfully").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error cleaning up audit logs")
                .build();
        }
    }
}