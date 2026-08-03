package com.matchingengine.api.auth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URL;

/**
 * Verifies the Supabase Auth JWT on order submission/cancellation, so
 * persisted orders and trades can be tied to a real, authenticated user.
 *
 * Supabase signs tokens with an asymmetric key (ES256) rather than a
 * single shared secret, so verification here works by fetching Supabase's
 * public key set (JWKS) and checking the token's signature against it -
 * nimbus-jose-jwt handles the fetching, caching, and key-rotation-by-`kid`
 * details. Nothing here is a secret: the JWKS endpoint only ever exposes
 * public keys, which is the whole point of asymmetric signing - anyone
 * can verify a token, only Supabase can issue one.
 *
 * Deliberately NOT a full Spring Security setup - Supabase Auth already
 * owns login, signup, and password storage. All this filter needs to do
 * is: reject the request if the bearer token doesn't verify, and
 * otherwise record who the caller is so the controller/service layer can
 * attach it to the order.
 *
 * Reads are left public (see shouldNotFilter) - there's no reason to gate
 * viewing the order book behind a login.
 *
 * @Order here is important: it must run strictly after CorsConfig's
 * CorsFilter (registered at Ordered.HIGHEST_PRECEDENCE), so that CORS
 * headers are already attached before this filter has a chance to reject
 * the request with a 401.
 */
@Component
@Order(100)
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    public static final String USER_ID_ATTR = "authUserId";
    public static final String USER_EMAIL_ATTR = "authUserEmail";

    // Public by design - see class-level comment. Same project URL that's
    // already visible in the dashboard's app.js.
    private static final String SUPABASE_URL = "https://vxibkcerkykulnmedvcu.supabase.co";

    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    public JwtAuthFilter() {
        try {
            // nimbus's default fetch timeout is a few hundred ms, which is too
            // aggressive for a real network - especially the very first call,
            // before the key set is cached in memory. 5s connect/read is plenty
            // generous without hanging the server if Supabase is genuinely down.
            ResourceRetriever resourceRetriever = new DefaultResourceRetriever(5000, 5000);
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(
                    new URL(SUPABASE_URL + "/auth/v1/.well-known/jwks.json"), resourceRetriever);
            JWSKeySelector<SecurityContext> keySelector =
                    new JWSVerificationKeySelector<>(JWSAlgorithm.ES256, keySource);

            ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
            processor.setJWSKeySelector(keySelector);
            this.jwtProcessor = processor;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set up Supabase JWKS verifier", e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        boolean isOrdersPath = path.startsWith("/api/orders");
        boolean isPublicBookRead = path.equals("/api/orders/book") && "GET".equalsIgnoreCase(request.getMethod());
        return !isOrdersPath || isPublicBookRead;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing bearer token");
            return;
        }

        try {
            JWTClaimsSet claims = jwtProcessor.process(header.substring(7), null);
            request.setAttribute(USER_ID_ATTR, claims.getSubject());
            request.setAttribute(USER_EMAIL_ATTR, claims.getStringClaim("email"));
        } catch (Exception e) {
            log.warn("JWT verification failed: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}