package fr.insalyon.creatis.vip.api.security.oidc;

import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.net.URI;

import fr.insalyon.creatis.vip.api.CarminProperties;

@Service
public class OidcConfig {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Environment env;
    private final Map<String, OidcServer> servers;

    public class OidcServer {
        public final String issuer;
        public final Boolean useResourceRoleMappings;
        public final String resourceName;

        OidcServer(String issuer, Boolean useResourceRoleMappings, String resourceName) {
            this.issuer = issuer;
            if (useResourceRoleMappings && (resourceName == null || resourceName.isEmpty())) {
                throw new IllegalArgumentException("useResourceRoleMappings enabled but no resourceName defined");
            }
            this.useResourceRoleMappings = useResourceRoleMappings;
            this.resourceName = resourceName;
        }
    }

    @Autowired
    public OidcConfig(Environment env, Resource vipConfigFolder) throws IOException, URISyntaxException, BusinessException {
        this.env = env;
        // Build the list of OIDC servers from config file. If OIDC is disabled, just create an empty list.
        HashMap<String, OidcServer> servers = new HashMap<>();
        if (isOIDCActive()) {
            // Many errors are possible here:
            // IOException in getFile() (can't read file), JsonProcessingException in readTree() (bad JSON),
            // URISyntaxException in URI() (bad URL syntax), NullPointerException in get().asText() (missing JSON key),
            // and probably more...
            // As long as isOIDCActive(), we do not try to handle them: just let them bubble up, causing a boot-time error.
            final String basename = "vip-oidc.json";

            // Read and parse vip-oidc.json file into a list of OIDC servers.
            // The expected file content is:
            // { "servers": [{"url":"...", "use-resource-role-mapping":false, "resource":"name"}]}
            // servers: array, mandatory
            //     List of OIDC servers.
            // servers[].url: string, mandatory
            //     Base URL of the OIDC server. Must be unique across servers.
            // servers[].use-resource-role-map: boolean, optional, default false
            //     Whether to use realm_access (if false) or resource_access (if true) for roles mapping.
            // servers[].resource: string, mandatory if use-resource-role-map=true
            //     Resource name (i.e. Keycloak client ID) to use for roles mapping.
            //
            // A note on Keycloak vs OIDC: Keycloak is a specific server software, providing an implementation of OIDC.
            // In order to favor future compatibility with other OIDC implementations, things that are Keycloak-specific
            // should be explicitly documented as such:
            // - A Keycloak URL is typically https://hostname/realms/<realmName>. The concept of "realm" is Keycloak-specific.
            // - Spring OIDC does an HTTP GET on <url>/.well-known/openid-configuration to get the various OIDC endpoints.
            //   Since our base url can be anything and does not explicitly involve realms, this is generic OIDC.
            // - How roles are encoded in the JWT, and the concept of "resource", is Keycloak-specific.
            // So, currently, our only Keycloak-specific behaviour is how roles are mapped in OIDCResolver.parseAuthorities().
            // Adding a new "type" field in the OidcServer object, which defaults to type=keycloak and conditions how roles are mapped,
            // would allow to extend compatibility with other OIDC servers.
            File file = vipConfigFolder.getFile().toPath().resolve(basename).toFile();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(file);
            if (!(rootNode.hasNonNull("servers") && rootNode.get("servers").isArray())) {
                throw new BusinessException("Failed parsing " + basename + ": missing mandatory field: servers");
            }
            for (JsonNode serverNode : rootNode.get("servers")) {
                if (!(serverNode.hasNonNull("url"))) {
                    throw new BusinessException("Failed parsing " + basename + ": missing mandatory field: url");
                }
                // url field is mandatory: here we just check for its presence, content will be validated by URI() below
                String baseURL = serverNode.get("url").asText();
                // optional fields
                Boolean useResourceRoleMappings;
                if (serverNode.hasNonNull("use-resource-role-mapping")) {
                    useResourceRoleMappings = serverNode.get("use-resource-role-mapping").asBoolean();
                } else {
                    useResourceRoleMappings = false;
                }
                String resourceName;
                if (serverNode.hasNonNull("resource")) {
                    resourceName = serverNode.get("resource").asText();
                } else {
                    resourceName = "";
                }
                // Add a new OIDC server to our config.
                URI url = new URI(baseURL);
                String issuer = url.toASCIIString();
                if (servers.get(issuer) != null) {
                    throw new BusinessException("Failed parsing " + basename + ": duplicate issuers");
                }
                servers.put(issuer, new OidcServer(issuer, useResourceRoleMappings, resourceName));
            }
        }
        this.servers = servers;
    }

    public boolean isOIDCActive() {
        return env.getProperty(CarminProperties.KEYCLOAK_ACTIVATED, Boolean.class, Boolean.FALSE);
    }

    // list of issuers URLs
    public Collection<String> getServers() {
        return servers.keySet();
    }

    // get resource name property for a given issuer URL
    public OidcServer getServerConfig(String issuer) {
        return servers.get(issuer);
    }
}