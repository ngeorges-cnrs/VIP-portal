/*
 * Copyright and authors: see LICENSE.txt in base repository.
 *
 * This software is a web portal for pipeline execution on distributed systems.
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package fr.insalyon.creatis.vip.application.server.dao.h2;

import fr.insalyon.creatis.vip.core.server.business.Server;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Creates dao for the h2 database of a simulation.
 * Each dao is specific to a single database, and so to a single simulation.
 *
 * The default is to access the h2 database through an h2 server via tcp,
 * but this is changeable to use (for instance) a memory or a local h2
 * database for testing or local use
 *
 * @author Rafael Silva
 */
public abstract class AbstractJobData {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String DRIVER = "org.h2.Driver";

    @Value("${workflows.db.scheme:tcp}")
    private String workflowsScheme = "tcp";

    protected Server server;
    private String dbPath;
    protected Connection connection;

    @Autowired
    public final void setServer(Server server) {
        this.server = server;
    }

    public AbstractJobData(String dbPath) {
        this.dbPath = dbPath;
    }

    protected String getDbPath() {
        return dbPath;
    }

    @PostConstruct
    public final void initConnection() throws DAOException {
        try {
            Class.forName(DRIVER);
            StringBuilder dbUrl = new StringBuilder();
            dbUrl.append("jdbc:h2:").append(workflowsScheme).append(":");
            if ("tcp".equals(workflowsScheme)) {
                // if tcp, add server and port
                // otherwise, its a local file, only the path is needed
                dbUrl.append("//")
                        .append(server.getWorkflowsHost())
                        .append(":9092/");
            }
            dbUrl.append(server.getWorkflowsPath())
                    .append("/")
                    .append(dbPath)
                    .append("/db/jobs");

            connection = DriverManager.getConnection(dbUrl.toString(), "gasw", "gasw");
            connection.setAutoCommit(true);

        } catch (ClassNotFoundException | SQLException ex) {
            logger.error("Error creating database connection for {}", dbPath,ex);
            throw new DAOException(ex);
        }
    }
    

    /**
     * Closes database connection.
     */
    protected void close(Logger logger) {

        try {
            connection.close();
        } catch (SQLException ex) {
            logger.error("Error closing connection", ex);
        }
    }
}
