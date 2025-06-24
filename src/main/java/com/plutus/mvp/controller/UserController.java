package com.plutus.mvp.controller;

import com.plutus.mvp.dto.*;
import com.plutus.mvp.security.JWTUtil;
import com.plutus.mvp.security.RolesAllowed;
import com.plutus.mvp.service.PasswordResetService;
import com.plutus.mvp.service.UserService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {
    @Inject
    private UserService userService;

    @Inject
    private PasswordResetService passwordResetService;

    @Inject
    private JWTUtil jwtUtil;

    @POST
    @Path("/login")
    public Response login(
        @Valid LoginDTO loginDTO, 
        @Context HttpHeaders headers
    ) {
        try {
            // Extract IP address (simple approach, might need refinement in production)
            String ipAddress = headers.getHeaderString("X-Forwarded-For");
            if (ipAddress == null) {
                ipAddress = headers.getHeaderString("RemoteAddr");
            }
            
            // Default to localhost if no IP found
            ipAddress = ipAddress != null ? ipAddress : "127.0.0.1";

            AuthenticationResponseDTO authResponse = userService.login(loginDTO, ipAddress);

            if (authResponse != null) {
                return Response.ok(authResponse).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Invalid credentials"))
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Login failed"))
                .build();
        }
    }

    @GET
    @Path("/profile")
    @RolesAllowed({"USER", "ADMIN", "MODERATOR"})
    public Response getUserProfile(@Context HttpHeaders headers) {
        try {
            // Extract token from Authorization header
            String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
            String token = authHeader.substring(7); // Remove "Bearer "

            // Extract user details from token
            Long userId = jwtUtil.getUserIdFromToken(token);

            // You would typically fetch user details from the repository here
            // For this example, we'll just return the user ID and roles
            return Response.ok(Map.of(
                "userId", userId,
                "roles", jwtUtil.getRolesFromToken(token)
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "Unable to retrieve profile"))
                .build();
        }
    }

    @GET
    @Path("/admin")
    @RolesAllowed({"ADMIN"})
    public Response getAdminDashboard() {
        return Response.ok(Map.of("message", "Welcome to the admin dashboard")).build();
    }

    // Previous methods for registration, password reset, etc. remain unchanged
}