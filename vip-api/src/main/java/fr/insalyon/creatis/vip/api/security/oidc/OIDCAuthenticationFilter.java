package fr.insalyon.creatis.vip.api.security.oidc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OIDCAuthenticationFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AuthenticationManager authenticationManager;

    public OIDCAuthenticationFilter(
            AuthenticationEntryPoint authenticationEntryPoint,
            AuthenticationManager authenticationManager) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(this.authenticationManager,
                "An AuthenticationManager is required");

        Assert.notNull(this.authenticationEntryPoint,
                "An AuthenticationEntryPoint is required");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        // TODO: get and validate JWT bearer token, header "Authorization: Bearer <token>"
        String header = request.getHeader("Authorization");

        logger.info("XXX in OIDCAuthenticationFilter.doFilterInternal: header='" + header + "'");
        filterChain.doFilter(request, response);
    }
}
