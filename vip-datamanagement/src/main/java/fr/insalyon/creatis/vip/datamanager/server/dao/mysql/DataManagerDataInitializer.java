package fr.insalyon.creatis.vip.datamanager.server.dao.mysql;

import fr.insalyon.creatis.vip.core.server.dao.mysql.TableInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Component
@Transactional
public class DataManagerDataInitializer extends JdbcDaoSupport {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private TableInitializer tableInitializer;

    @Autowired
    public DataManagerDataInitializer(
            DataSource dataSource, TableInitializer tableInitializer) {
        setDataSource(dataSource);
        this.tableInitializer = tableInitializer;
    }

    @EventListener(ContextRefreshedEvent.class)
    @Order(20) // DataManager tables references vip-core tables and must be created after
    public void onStartup() {
        logger.info("Configuring VIP External Platforms database.");
        tableInitializer.createTable("VIPExternalPlatforms",
                "identifier VARCHAR(50) NOT NULL, "
                        + "type VARCHAR(50) NOT NULL, "
                        + "description VARCHAR(1000), "
                        + "url VARCHAR(255), "
                        + "upload_url VARCHAR(255), "
                        + "keycloak_client_id VARCHAR(255), "
                        + "refresh_token_url VARCHAR(255), "
                        + "PRIMARY KEY (identifier)");

        logger.info("Configuring VIP api keys database.");
        tableInitializer.createTable("VIPApiKeys",
                "email VARCHAR(255),"
                        + "identifier VARCHAR(50) NOT NULL,"
                        + "apiKey VARCHAR(255),"
                        + "FOREIGN KEY (email) REFERENCES VIPUsers(email)"
                        + "  ON DELETE CASCADE ON UPDATE CASCADE,"
                        + "FOREIGN KEY (identifier)"
                        + "  REFERENCES VIPExternalPlatforms(identifier)"
                        + "  ON DELETE CASCADE ON UPDATE CASCADE");
    }
}
