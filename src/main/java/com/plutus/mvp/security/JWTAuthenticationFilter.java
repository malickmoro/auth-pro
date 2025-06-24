package com.plutus.mvp.security;

import com.plutus.mvp.entity.UserRole;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JWTAuthenticationFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    @Inject
    private JWTUtil jwtUtil;

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Skip authentication for certain paths
        String path = requestContext.getUriInfo().getPath();
        if (isPublicPath(path)) {
            return;
        }

        // Special handling for token refresh endpoint
        if (path.contains("/auth/refresh")) {
            validateRefreshToken(requestContext);
            return;
        }

        // Check for Authorization header
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            abortWithUnauthorized(requestContext, "Missing or invalid Authorization header");
            return;
        }

        // Extract token
        String token = authHeader.substring(7);

        // Validate token
        if (!jwtUtil.validateToken(token)) {
            abortWithUnauthorized(requestContext, "Invalid or expired token");
            return;
        }

        // Ensure it's an access token
        if (!jwtUtil.isAccessToken(token)) {
            abortWithUnauthorized(requestContext, "Invalid token type");
            return;
        }

        // Check role-based access if annotated
        if (!checkRoleAccess(token)) {
            abortWithForbidden(requestContext, "Insufficient privileges");
        }
    }

    private void validateRefreshToken(ContainerRequestContext requestContext) {
        // Extract token from request body or Authorization header
        String token = extractRefreshTokenFromRequest(requestContext);

        if (token == null) {
            abortWithUnauthorized(requestContext, "Missing refresh token");
            return;
        }

        // Validate token
        if (!jwtUtil.validateToken(token)) {
            abortWithUnauthorized(requestContext, "Invalid or expired refresh token");
            return;
        }

        // Ensure it's a refresh token
        if (!jwtUtil.isRefreshToken(token)) {
            abortWithUnauthorized(requestContext, "Invalid token type");
        }
    }

    private String extractRefreshTokenFromRequest(ContainerRequestContext requestContext) {
        // This is a simplified approach. In a real-world scenario, 
        // you'd parse the request body to extract the refresh token
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean isPublicPath(String path) {
        // Define public paths that don't require authentication
        return path.contains("/users/login") || 
               path.contains("/users/register") || 
               path.contains("/users/reset") ||
               path.contains("/auth/refresh");
    }

    private boolean checkRoleAccess(String token) {
        Method method = resourceInfo.getResourceMethod();
        
        // Check if method has RolesAllowed annotation
        Annotation[] annotations = method.getAnnotations();
        RolesAllowed rolesAllowedAnnotation = Arrays.stream(annotations)
            .filter(a -> a instanceof RolesAllowed)
            .map(a -> (RolesAllowed) a)
            .findFirst()
            .orElse(null);

        // If no role restriction, allow access
        if (rolesAllowedAnnotation == null) {
            return true;
        }

        // Get roles from token
        List<UserRole> tokenRoles = jwtUtil.getRolesFromToken(token);

        // Check if any of the token roles match the required roles
        return Arrays.stream(rolesAllowedAnnotation.value())
            .anyMatch(requiredRole -> 
                tokenRoles.stream()
                    .anyMatch(tokenRole -> tokenRole.name().equals(requiredRole))
            );
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        logger.warn("Unauthorized access attempt: {}", message);
        requestContext.abortWith(
            Response.status(Response.Status.UNAUTHORIZED)
                .entity(message)
                .build()
        );
    }

    private void abortWithForbidden(ContainerRequestContext requestContext, String message) {
        logger.warn("Forbidden access attempt: {}", message);
        requestContext.abortWith(
            Response.status(Response.Status.FORBIDDEN)
                .entity(message)
                .build()
        );
    }
}