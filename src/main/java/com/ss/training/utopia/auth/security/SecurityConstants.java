package com.ss.training.utopia.auth.security;

public class SecurityConstants {
    private SecurityConstants(){}

    public static final String SECRET = "secretWord";
    public static final int EXPIRATION_TIME = 864000000; // 10 days
    public static final String TOKEN_PREFIX = "Bearer";
    public static final String HEADER_STRING = "Authorization";
    public static final String ISSUER = "amattson-sm-jwt";

    public static final String USER_ID_CLAIM = "UserSpecificClaim";
    public static final String AUTHORITY_CLAIM = "AuthorityClaim";
}
