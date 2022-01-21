package com.ss.training.utopia.auth.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ss.training.utopia.auth.dto.LoginDto;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    public AuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * Triggers on any POST to /login
     * Grabs JSON information for a login object and confirms user authentication
     * @param request request information including headers
     * @param response response template for building
     * @return authentication token if it is created
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // get credentials as DTO from HTTP request
        LoginDto credentials;
        try {
            credentials = new ObjectMapper().readValue(request.getInputStream(), LoginDto.class);
        } catch (IOException e) {
            return null;
        }

        // create token from DTO
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            credentials.getUsername(),
            credentials.getPassword(),
            new ArrayList<>());

        // pass token to authentication
        return authenticationManager.authenticate(authenticationToken);
    }

    /**
     * Runs automatically on successful authentication
     * Creates the JWT token to use and attaches it as a header to the response
     * @param request request information, including headers
     * @param response response template to add information to
     * @param chain filter chain to use for custom and built-in filters
     * @param authResult authorization result object from the previous method
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) {
        // get User from authorization result
        UserDetailsImpl user = (UserDetailsImpl) authResult.getPrincipal();
        String auth = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

        // create token for user
        String token = JWT.create()
            .withSubject(user.getUsername())
            .withIssuer(SecurityConstants.ISSUER)
            .withClaim(SecurityConstants.USER_ID_CLAIM, user.getId())
            .withClaim(SecurityConstants.AUTHORITY_CLAIM, auth)
            .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
            .sign(Algorithm.HMAC512(SecurityConstants.SECRET.getBytes()));

        // add JWT token to response header
        response.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX + " " + token);
    }
}
