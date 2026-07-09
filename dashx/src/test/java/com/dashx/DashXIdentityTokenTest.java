package com.dashx;

import static org.junit.jupiter.api.Assertions.*;

import com.dashx.exception.DashXConfigurationException;
import com.dashx.exception.DashXValidationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DashXIdentityTokenTest {

    // HS256 requires a key of at least 256 bits (32 bytes); real DashX private
    // keys are 32 chars, so use a 32-char key here.
    private static final String PRIVATE_KEY = "0123456789abcdef0123456789abcdef";

    private DashX dashx;

    @BeforeEach
    void setUp() {
        DashX.resetInstances();
        dashx = DashX.getInstance("identity-token-test");
        dashx.configure(
            new DashXConfig.Builder()
                .publicKey("test-public-key")
                .privateKey(PRIVATE_KEY)
                .targetEnvironment("test")
                .build()
        );
    }

    @AfterEach
    void tearDown() {
        DashX.resetInstances();
    }

    private Claims parse(String token) {
        SecretKey key = new SecretKeySpec(
            PRIVATE_KEY.getBytes(StandardCharsets.UTF_8),
            "HmacSHA256"
        );
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    @Test
    void tokenVerifiesWithThePrivateKeyAndCarriesTheExpectedClaims() {
        String token = dashx.generateIdentityToken("visitor-123", "VISITOR");

        // A token that doesn't verify with the private key would throw here.
        Claims claims = parse(token);

        assertEquals("VISITOR", claims.get("kind", String.class));
        assertEquals("visitor-123", claims.get("uid", String.class));
        assertNotNull(claims.getExpiration());
    }

    @Test
    void tokenIsAThreeSegmentJwt() {
        String token = dashx.generateIdentityToken("user-1");

        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void expirationIsInSecondsScaleNotMilliseconds() {
        long expiresInSeconds = 3600L;
        long before = System.currentTimeMillis();

        String token = dashx.generateIdentityToken(
            "user-1",
            "USER",
            expiresInSeconds
        );
        Claims claims = parse(token);

        // jjwt stores `exp` as unix seconds and rebuilds the Date from it. If the
        // claim were written in milliseconds, the expiration would land ~1000x
        // further in the future and this bound would fail.
        long expectedUpperBound = before + (expiresInSeconds + 60) * 1000L;
        long expirationMillis = claims.getExpiration().getTime();

        assertTrue(
            expirationMillis <= expectedUpperBound,
            "exp must be unix seconds, not milliseconds"
        );
        assertTrue(
            expirationMillis >= before + (expiresInSeconds - 60) * 1000L,
            "exp should be roughly now + expiresInSeconds"
        );
    }

    @Test
    void kindIsNormalizedToUppercase() {
        String token = dashx.generateIdentityToken("visitor-1", "visitor");

        assertEquals("VISITOR", parse(token).get("kind", String.class));
    }

    @Test
    void singleArgOverloadDefaultsToUserKind() {
        String token = dashx.generateIdentityToken("user-1");

        assertEquals("USER", parse(token).get("kind", String.class));
    }

    @Test
    void rejectsUnknownKind() {
        assertThrows(DashXValidationException.class, () ->
            dashx.generateIdentityToken("user-1", "admin")
        );
    }

    @Test
    void rejectsNullKind() {
        assertThrows(DashXValidationException.class, () ->
            dashx.generateIdentityToken("user-1", null)
        );
    }

    @Test
    void rejectsBlankUid() {
        assertThrows(DashXValidationException.class, () ->
            dashx.generateIdentityToken("   ", "USER")
        );
    }

    @Test
    void throwsWhenNotConfigured() {
        DashX.resetInstances();
        DashX unconfigured = DashX.getInstance("unconfigured");

        assertThrows(DashXConfigurationException.class, () ->
            unconfigured.generateIdentityToken("user-1")
        );
    }
}
