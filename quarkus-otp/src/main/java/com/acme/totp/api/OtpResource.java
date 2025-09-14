package com.acme.totp.api;

import java.math.BigDecimal;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base32;

import com.acme.totp.crypto.TanSigner;
import com.acme.totp.crypto.Totp;
import com.acme.totp.domain.UserStore;
import com.acme.totp.dto.RegisterRequest;
import com.acme.totp.dto.RegisterResponse;
import com.acme.totp.dto.TanChallengeRequest;
import com.acme.totp.dto.TanSignRequest;
import com.acme.totp.dto.TanVerifyRequest;
import com.acme.totp.dto.TotpVerifyRequest;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OtpResource {

    private static final SecureRandom RNG = new SecureRandom();
    private static final int TOTP_DIGITS = 6;
    private static final int TOTP_PERIOD = 30; // seconds
    private static final int TOTP_WINDOW = 1; // tolerate +/- 30s
    private static final String TOTP_HMAC = "HmacSHA1"; // default per RFC 6238

    @POST
    @Path("/register")
    public Response register(RegisterRequest req) {
        if (req == null || blank(req.username) || blank(req.password) || blank(req.issuer)) {
            return Response.status(400).entity("username, password, issuer required").build();
        }
        if (UserStore.exists(req.username)) {
            return Response.status(409).entity("username already registered").build();
        }

        // Generate 20 random bytes (160-bit), Base32 without padding
        byte[] secret = new byte[20];
        RNG.nextBytes(secret);
        String base32 = new Base32().encodeAsString(secret).replace("=", "");

        String bcrypt = BcryptUtil.bcryptHash(req.password); // demo only
        UserStore.put(new UserStore.User(req.username, bcrypt, base32));

        String otpauth = Totp.otpauthUri(req.issuer, req.username, base32,
                TOTP_DIGITS, TOTP_PERIOD, "SHA1");

        String qr = "/qr?data=" + url(otpauth);

        RegisterResponse out = new RegisterResponse();
        out.otpauthUri = otpauth;
        out.qrUrl = qr;
        out.note = "Demo only: your secret lives in memory and is not protected.";
        return Response.ok(out).build();
    }

    @POST
    @Path("/totp/verify")
    public Response verifyTotp(TotpVerifyRequest req) {
        var u = UserStore.get(req.username);
        if (u == null)
            return Response.status(404).entity("unknown user").build();
        if (blank(req.code) || !req.code.matches("\\d{6}")) {
            return Response.status(400).entity("6-digit code required").build();
        }
        boolean ok = Totp.verify(u.base32Secret, req.code, TOTP_DIGITS,
                TOTP_PERIOD, TOTP_WINDOW, TOTP_HMAC, Totp.now());
        return Response.ok("{\"valid\":" + ok + "}").build();
    }

    @POST
    @Path("/tan/challenge")
    public Response tanChallenge(TanChallengeRequest req) {
        var u = UserStore.get(req.username);
        if (u == null)
            return Response.status(404).entity("unknown user").build();
        if (blank(req.txId) || req.amount == null || blank(req.currency) || blank(req.beneficiary)) {
            return Response.status(400).entity("txId, amount, currency, beneficiary required").build();
        }
        String canonical = TanSigner.canonical(req.txId, money(req.amount), req.currency, req.beneficiary);
        // Return canonical string the user/device would sign. Do NOT reveal TAN in real
        // life.
        return Response.ok("{\"canonical\":\"" + escape(canonical) + "\"}").build();
    }

    /**
     * DEMO ONLY: simulate the user's device computing a TAN from the shared secret.
     * In production, this logic must live on a user-controlled device/app, not on
     * the server.
     */
    @POST
    @Path("/tan/sign")
    public Response tanSign(TanSignRequest req) {
        var u = UserStore.get(req.username);
        if (u == null)
            return Response.status(404).entity("unknown user").build();
        if (req.canonical == null || req.canonical.isBlank()) {
            return Response.status(400).entity("canonical required").build();
        }
        int digits = (req.digits == null) ? 8 : req.digits.intValue();

        String tan = TanSigner.sign(u.base32Secret, req.canonical, digits);

        // Return just the TAN. Real systems never compute this on the server.
        return Response.ok("{\"tan\":\"" + tan + "\"}").build();
    }

    @POST
    @Path("/tan/verify")
    public Response tanVerify(TanVerifyRequest req) {
        var u = UserStore.get(req.username);
        if (u == null)
            return Response.status(404).entity("unknown user").build();
        if (blank(req.canonical) || blank(req.tan)) {
            return Response.status(400).entity("canonical and tan required").build();
        }
        // Simulate a user's authenticator computing the TAN on a separate device:
        String expected = TanSigner.sign(u.base32Secret, req.canonical, 8);
        boolean ok = expected.equals(req.tan);
        return Response.ok("{\"valid\":" + ok + "}").build();
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static String url(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }

    private static String money(BigDecimal a) {
        return a.setScale(2).toPlainString();
    }

    private static String escape(String s) {
        // naive JSON string escape for demo; prefer a proper JSON serializer for DTOs
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
