package com.ss.training.utopia.auth.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.ss.training.utopia.auth.dao.UserDao;
import com.ss.training.utopia.auth.entity.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class AuthorizationFilter extends BasicAuthenticationFilter {

    private final UserDao dao;

    public AuthorizationFilter(AuthenticationManager authenticationManager, UserDao dao) {
        super(authenticationManager);
        this.dao = dao;
    }

    /**
     * Endpoint hit by every request
     * @param request Http request with header information
     * @param response response template to begin building response
     * @param chain the filter chain we are using
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // grab header by label
        String header = request.getHeader(SecurityConstants.HEADER_STRING);

        // check header exists and is correctly formatted
        if (header == null || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            // still do the filter, but without auth
            chain.doFilter(request, response);
            return;
        }

        // get authentication verified if possible, and set authentication status
        Authentication authentication = getUsernamePasswordAuthentication(request); // uses below method
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // perform the chain filter action
        chain.doFilter(request, response);
    }

    /**
     * Parses the Http request to determine user authorization
     * @param request Http request to parse Jwt Tokens from
     * @return the authorizations (if found), or null otherwise
     */
    private Authentication getUsernamePasswordAuthentication(HttpServletRequest request) {
        // get header by label
        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        if (token != null) {
            // from header, get username after decrypting
            String username = JWT.require(Algorithm.HMAC512(SecurityConstants.SECRET.getBytes()))
                .build()
                .verify(token.replace(SecurityConstants.TOKEN_PREFIX + " ", ""))
                .getSubject();

            // if user found, create authentication token and return
            if (username != null) {
                Optional<User> user = dao.findByUsername(username);
                if (user.isEmpty())
                    return null;
                UserDetailsImpl userDetails = new UserDetailsImpl(user.get());
                return new UsernamePasswordAuthenticationToken(username, null, userDetails.getAuthorities());
            }
            return null;
        }
        return null;
    }
}
