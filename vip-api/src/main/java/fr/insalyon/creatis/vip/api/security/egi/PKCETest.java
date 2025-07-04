package fr.insalyon.creatis.vip.api.security.egi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public class PKCETest {
    private static final StringKeyGenerator DEFAULT_SECURE_KEY_GENERATOR = new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 96);
    private static final Logger logger = LoggerFactory.getLogger(PKCETest.class);

    private PKCETest() {
    }

    public static Consumer<OAuth2AuthorizationRequest.Builder> withPkce() {
        // XXX TODO logger
        logger.info("XXX withPkce");
        return PKCETest::applyPkce;
    }

    private static void applyPkce(OAuth2AuthorizationRequest.Builder builder) {
        if (!isPkceAlreadyApplied(builder)) {
            String codeVerifier = DEFAULT_SECURE_KEY_GENERATOR.generateKey();
            builder.attributes((attrs) -> {
                attrs.put("code_verifier", codeVerifier);
            });
            builder.additionalParameters((params) -> {
                try {
                    String codeChallenge = createHash(codeVerifier);
                    params.put("code_challenge", codeChallenge);
                    params.put("code_challenge_method", "S256");
                } catch (NoSuchAlgorithmException var3) {
                    params.put("code_challenge", codeVerifier);
                }

            });
        }
    }

    private static boolean isPkceAlreadyApplied(OAuth2AuthorizationRequest.Builder builder) {
        AtomicBoolean pkceApplied = new AtomicBoolean(false);
        builder.additionalParameters((params) -> {
            if (params.containsKey("code_challenge")) {
                pkceApplied.set(true);
            }

        });
        return pkceApplied.get();
    }

    private static String createHash(String value) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(value.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
}
