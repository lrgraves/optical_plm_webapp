package com.eleoptics.spark.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.server.ServletHelper.RequestType;
import com.vaadin.flow.shared.ApplicationConstants;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Stream;

/**
 * SecurityUtils takes care of all such static operations that have to do with
 * security and querying rights from different beans of the UI.
 *
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Util methods only
    }

    /**
     * Tests if the request is an internal framework request. The test consists of
     * checking if the request parameter is present and if its value is consistent
     * with any of the request types know.
     *
     * @param request
     *            {@link HttpServletRequest}
     * @return true if is an internal framework request. False otherwise.
     */
    static boolean isFrameworkInternalRequest(HttpServletRequest request) {
        final String parameterValue = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
        return parameterValue != null
                && Stream.of(RequestType.values()).anyMatch(r -> r.getIdentifier().equals(parameterValue));
    }

    /**
     * Tests if some user is authenticated. As Spring Security always will create an {@link AnonymousAuthenticationToken}
     * we have to ignore those tokens explicitly.
     */
    static boolean isUserLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.isAuthenticated();
    }

    public static String getJWT() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String jwtToken = ((OidcUser) authentication.
                getPrincipal()).
                getIdToken().
                getTokenValue().toString();

        return jwtToken;
    }

    public static Map<String, Object> getJWTClaims(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> jwtToken = ((OidcUser) authentication.
                getPrincipal()).
                getClaims();
        return jwtToken;
    }

    public static String getUserID(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        OidcUser user = ((OidcUser) authentication.getPrincipal());

        return user.getName();

    }
}
