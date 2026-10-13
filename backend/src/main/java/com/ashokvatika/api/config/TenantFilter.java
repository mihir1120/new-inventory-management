package com.ashokvatika.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TenantFilter extends OncePerRequestFilter {
    private static final String TENANT_HEADER = "X-Tenant-Id";
    private final Environment environment;

    public TenantFilter(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator/")
                || request.getRequestURI().startsWith("/api/v1/public/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String tenantId = request.getHeader(TENANT_HEADER);

        if (tenantId == null || tenantId.isBlank()) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing X-Tenant-Id header");
            return;
        }

        try {
            UUID requestedTenant = UUID.fromString(tenantId);
            if (!environment.acceptsProfiles(Profiles.of("local"))) {
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                if (!(authentication instanceof JwtAuthenticationToken jwt)
                        || !jwt.isAuthenticated()
                        || !requestedTenant.toString().equals(jwt.getToken().getClaimAsString("tenant_id"))) {
                    response.sendError(HttpStatus.FORBIDDEN.value(), "Tenant access denied");
                    return;
                }
            }
            TenantContext.setTenantId(requestedTenant);
            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid X-Tenant-Id header");
        } finally {
            TenantContext.clear();
        }
    }
}
