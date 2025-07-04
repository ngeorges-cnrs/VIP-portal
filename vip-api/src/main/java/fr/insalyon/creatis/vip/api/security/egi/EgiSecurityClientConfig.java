package fr.insalyon.creatis.vip.api.security.egi;

import fr.insalyon.creatis.vip.api.CarminProperties;
import fr.insalyon.creatis.vip.core.server.business.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

@Configuration
public class EgiSecurityClientConfig {

    private final Environment env;
    private final Server server;

    @Autowired
    public EgiSecurityClientConfig(Environment env, Server server) {
        this.env = env;
        this.server = server;
    }

    /*
        This needs the properties that could be in vip-api.conf (especially in the test)
        For vip-api.conf properties to be loaded, the ApiPropertyInitializer must be run before
        Spring is not aware of that, so we must tell him explicitly with @DependsOn
     */
    // XXX no longer needed, nothing in vip-api.conf: @DependsOn("apiPropertiesInitializer")
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration r1 = this.egiClientRegistration();

        String r2Id = "lsaai";
        // see also templates in https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/oauth2/client/registration/ClientRegistration.Builder.html#redirectUri(java.lang.String)
        String redirectUri = server.getHostURL() + "/login/oauth2/code/" + r2Id;
        String publicUri = "http://192.168.122.102:8080";
        String kcUri = "http://192.168.122.52:8179/realms/test/protocol/openid-connect";
        ClientRegistration r2 = ClientRegistration.withRegistrationId(r2Id)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("openid", "profile", "email") // voperson_external_id eduperson_scoped_affiliation
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC) // default
                .clientId("testcli")
                .clientSecret("ws8kDStvkzNQH99CJWktyIUGMY5V0K7e")
                .redirectUri(publicUri+"/login/oauth2/code/lsaai")
                // doesn't work as a 4 to 1 uri replacement, unsure why: .issuerUri()
                .authorizationUri(kcUri+"/auth")
                .tokenUri(kcUri+"/token")
                .userInfoUri(kcUri+"/userinfo")
                .jwkSetUri(kcUri+"/certs")
                .build();

        return new InMemoryClientRegistrationRepository(r1, r2);
    }

    private ClientRegistration egiClientRegistration() {
        return ClientRegistration.withRegistrationId("egi")
                .clientId(env.getRequiredProperty(CarminProperties.EGI_CLIENT_ID))
                .clientSecret(env.getRequiredProperty(CarminProperties.EGI_CLIENT_SECRET))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(env.getRequiredProperty(CarminProperties.EGI_REDIRECT_URI))
                .scope("openid", "profile", "email", "voperson_id", "eduperson_scoped_affiliation")
                .authorizationUri(env.getRequiredProperty(CarminProperties.EGI_AUTHORIZATION_URI))
                .tokenUri(env.getRequiredProperty(CarminProperties.EGI_TOKEN_URI))
                .userInfoUri(env.getRequiredProperty(CarminProperties.EGI_USER_INFO_URI))
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .jwkSetUri(env.getRequiredProperty(CarminProperties.EGI_JWK_SET_URI))
                //.clientName("foobar")
                .build();
    }
}
