package fr.insalyon.creatis.vip.api.security.oidc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class OIDCAuthenticationProvider implements
        AuthenticationProvider, InitializingBean, MessageSourceAware {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    public final void afterPropertiesSet() throws Exception {
        Assert.notNull(this.messages, "A message source must be set");
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        logger.info("XXX in OIDCAuthenticationProvider.supports: " + authentication);
        // return ApikeyAuthenticationToken.class.isAssignableFrom(authentication);
        return true; // XXX
    }

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        logger.info("XXX in OIDCAuthenticationProvider.authenticate: " + authentication);
        throw new BadCredentialsException(
                messages.getMessage(
                        "AbstractUserDetailsAuthenticationProvider.badCredentials",
                        "Bad credentials"));
    }

    private final String jwkSetUri = "http://192.168.122.184:8179/realms/test/protocol/openid-connect/certs";
    @Bean
    JwtDecoder jwtDecoder() {
        logger.info("XXX in ApiSecurityConfig.jwtDecoder");
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
    }
}
