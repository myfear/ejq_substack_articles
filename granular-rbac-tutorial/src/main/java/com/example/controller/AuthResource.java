package com.example.controller;

import java.time.Duration;

import org.eclipse.microprofile.jwt.Claims;
import org.mindrot.jbcrypt.BCrypt;

import com.example.dto.AuthRequest;
import com.example.entity.User;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @POST
    @Path("/register")
    @Transactional
    public Response register(@Valid AuthRequest req) {
        if (User.findByUsername(req.username) != null)
            return Response.status(Response.Status.CONFLICT).build();

        User.add(req.username, req.password);
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/login")
    @Transactional
    public Response login(@Valid AuthRequest req) {
        User u = User.findByUsername(req.username);
        if (u == null || !BCrypt.checkpw(req.password, u.password))
            return Response.status(Response.Status.UNAUTHORIZED).build();

        String token = Jwt.issuer("https://example.com/issuer")
                .upn(u.username)
                .claim(Claims.email.name(), u.username + "@example.com")
                .claim("user_id", u.id.toString())
                .expiresIn(Duration.ofHours(1))
                .sign();
        return Response.ok(token).build();
    }
}