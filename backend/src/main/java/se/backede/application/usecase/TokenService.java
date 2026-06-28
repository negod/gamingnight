package se.backede.application.usecase;

import se.backede.application.dto.AuthenticatedUser;
import se.backede.domain.model.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {

    private static final String HMAC = "HmacSHA256";
    private final byte[] secret;
    private final Clock clock;

    public TokenService(@Value("${app.auth.token-secret:dev-only-change-me}") String secret, Clock clock) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.clock = clock;
    }

    public String createToken(AuthenticatedUser user) {
        long expiresAt = Instant.now(clock).plusSeconds(60 * 60 * 12).getEpochSecond();
        String payload = user.id() + ":" + user.username() + ":" + user.role() + ":" + user.playerId() + ":" + expiresAt;
        String encodedPayload = base64Url(payload.getBytes(StandardCharsets.UTF_8));
        return encodedPayload + "." + sign(encodedPayload);
    }

    public Optional<AuthenticatedUser> parse(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        var parts = token.split("\\.");
        if (parts.length != 2 || !constantTimeEquals(sign(parts[0]), parts[1])) {
            return Optional.empty();
        }
        try {
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            var fields = payload.split(":");
            if (fields.length != 5) {
                return Optional.empty();
            }
            long expiresAt = Long.parseLong(fields[4]);
            if (expiresAt < Instant.now(clock).getEpochSecond()) {
                return Optional.empty();
            }
            return Optional.of(new AuthenticatedUser(
                    UUID.fromString(fields[0]),
                    fields[1],
                    UserRole.valueOf(fields[2]),
                    UUID.fromString(fields[3])
            ));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private String sign(String encodedPayload) {
        try {
            var mac = Mac.getInstance(HMAC);
            mac.init(new SecretKeySpec(secret, HMAC));
            return base64Url(mac.doFinal(encodedPayload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign token", exception);
        }
    }

    private static String base64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private static boolean constantTimeEquals(String left, String right) {
        return MessageDigestIsEqual.equals(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }

    private static final class MessageDigestIsEqual {
        private static boolean equals(byte[] left, byte[] right) {
            return java.security.MessageDigest.isEqual(left, right);
        }
    }
}
