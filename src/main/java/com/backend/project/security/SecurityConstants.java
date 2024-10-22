package com.backend.project.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;

public class SecurityConstants {

    public static final long JWT_EXPIRATION = 30 * 60 * 1000;

    public static final Key JWT_SECRET = Keys.secretKeyFor(SignatureAlgorithm.HS512);


}


