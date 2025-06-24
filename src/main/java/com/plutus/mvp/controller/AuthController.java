package com.plutus.mvp.controller;

import com.plutus.mvp.dto.AuthenticationResponseDTO;
import com.plutus.mvp.dto.RefreshTokenRequestDTO;
import com.plutus.mvp.service.AuthService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {
    @Inject
    private AuthService authService;

    @POST
    @Path("/refresh")
    public Response refreshToken(@Valid RefreshTokenRequestDTO refreshTokenRequest) {
        try {
            AuthenticationResponseDTO authResponse = authService.refreshToken(refreshTokenRequest);

            if (authResponse != null) {
                return Response.ok(authResponse).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Invalid or expired refresh token"))
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Token refresh failed"))
                .build();
        }
    }

    @POST
    @Path("/logout")
    public Response logout(@Valid RefreshTokenRequestDTO refreshTokenRequest) {
        try {
            // Validate and extract user ID from refresh token
            Long userId = authService.getUserIdFromRefreshToken(refreshTokenRequest.getRefreshToken());
            
            if (userId != null) {
                // Invalidate all user tokens
                authService.invalidateUserTokens(userId);
                
                return Response.ok(Map.of("message", "Logged out successfully")).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Invalid refresh token"))
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Logout failed"))
                .build();
        }
    }
}