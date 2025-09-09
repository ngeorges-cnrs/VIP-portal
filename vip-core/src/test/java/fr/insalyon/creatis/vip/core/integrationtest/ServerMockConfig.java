package fr.insalyon.creatis.vip.core.integrationtest;

import fr.insalyon.creatis.vip.core.server.CarminProperties;
import fr.insalyon.creatis.vip.core.server.business.Server;
import fr.insalyon.creatis.vip.core.server.model.Module;
import fr.insalyon.creatis.vip.core.server.model.SupportedTransferProtocol;
import org.mockito.Mockito;
import org.mockito.quality.Strictness;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.util.Arrays;

import static fr.insalyon.creatis.vip.core.server.CarminProperties.*;
import static fr.insalyon.creatis.vip.core.server.CarminProperties.UNSUPPORTED_METHODS;
import static org.mockito.Mockito.*;

/**
 * Spring configuration class for tests.
 * <p>
 * Overrides the Server default implementation by
 * a mocked one, so this does not need a vip.conf file presence.
 */
@Configuration
@Profile("test")
public class ServerMockConfig {

    public static final String TEST_ADMIN_FIRST_NAME = "test admin";
    public static final String TEST_ADMIN_LAST_NAME = "TEST ADMIN";
    public static final String TEST_ADMIN_EMAIL = "test-admin@test.com";
    public static final String TEST_ADMIN_PASSWORD = "test-admin-password";
    public static final String TEST_ADMIN_INSTITUTION = "test admin institution";
    public static final String LAB_TRUSTSTORE_FILE = "lab_truststore_file";
    public static final String LAB_TRUSTSTORE_PASS = "lab_truststore_pass";

    // Added because the max number of simulations was 0
    public static final String MAX_NUMBER_EXECUTIONS = "5";
    public static final String TEST_CAS_URL = "testCasURL";

    // paths stuff
    public static final String TEST_USERS_ROOT = "/test/prefix/vip/data/test_users";
    public static final String TEST_GROUP_ROOT = "/test/prefix/vip/data/test_groups";

    public static void reset(Server server) {
        Mockito.reset(server);
        Mockito.when(server.getAdminFirstName()).thenReturn(TEST_ADMIN_FIRST_NAME);
        Mockito.when(server.getAdminLastName()).thenReturn(TEST_ADMIN_LAST_NAME);
        Mockito.when(server.getAdminEmail()).thenReturn(TEST_ADMIN_EMAIL);
        Mockito.when(server.getAdminPassword()).thenReturn(TEST_ADMIN_PASSWORD);
        Mockito.when(server.getAdminInstitution()).thenReturn(TEST_ADMIN_INSTITUTION);
        Mockito.when(server.getCasURL()).thenReturn(TEST_CAS_URL);
        Mockito.when(server.getTruststoreFile()).thenReturn(LAB_TRUSTSTORE_FILE);
        Mockito.when(server.getTruststorePass()).thenReturn(LAB_TRUSTSTORE_PASS);
        when(server.getMaxPlatformRunningSimulations()).thenReturn(Integer.valueOf(MAX_NUMBER_EXECUTIONS));
        when(server.getDataManagerUsersHome()).thenReturn(TEST_USERS_ROOT);
        when(server.getDataManagerGroupsHome()).thenReturn(TEST_GROUP_ROOT);
        when(server.getVoRoot()).thenReturn("/vo_test/root");
        // XXX some of these should rather be in vip-api module, but test-only injection is unclear
        Mockito.when(server.getEnvProperty(APIKEY_HEADER_NAME)).thenReturn("testapikey");
        Mockito.when(server.getEnvProperty(API_PIPELINE_WHITE_LIST, String[].class)).thenReturn(new String[]{});
        Mockito.when(server.getEnvProperty(CORS_AUTHORIZED_DOMAINS, String[].class)).thenReturn(new String[]{});
        Mockito.when(server.getEnvProperty(APIKEY_GENERATE_NEW_EACH_TIME, Boolean.class)).thenReturn(false);
        Mockito.when(server.getEnvProperty(KEYCLOAK_ACTIVATED, Boolean.class, Boolean.FALSE)).thenReturn(false);

        Mockito.when(server.getEnvProperty(DEFAULT_LIMIT_LIST_EXECUTION, Integer.class)).thenReturn(42);
        Mockito.when(server.getEnvProperty(SUPPORTED_TRANSFER_PROTOCOLS, SupportedTransferProtocol[].class)).thenReturn(new SupportedTransferProtocol[]{});
        Mockito.when(server.getEnvProperty(SUPPORTED_MODULES, Module[].class)).thenReturn(new Module[]{});
        Mockito.when(server.getEnvProperty(UNSUPPORTED_METHODS, String[].class)).thenReturn(new String[]{});

        // API_DATA_TRANSFERT_MAX_SIZE,API_DIRECTORY_MIME_TYPE: notnull / no stub ?
        // API_DOWNLOAD_TIMEOUT_IN_SECONDS,API_DOWNLOAD_RETRY_IN_SECONDS: in DataApiBusinessTest
    }

    @Bean
    @Primary
    public Server testServer() throws IOException {
        Server server = mock(Server.class, withSettings().strictness(Strictness.STRICT_STUBS));
        reset(server);
        return server;
    }

}
