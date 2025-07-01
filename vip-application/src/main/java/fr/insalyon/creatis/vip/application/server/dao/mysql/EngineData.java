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
package fr.insalyon.creatis.vip.application.server.dao.mysql;

import fr.insalyon.creatis.vip.application.client.bean.Engine;
import fr.insalyon.creatis.vip.application.client.bean.Resource;
import fr.insalyon.creatis.vip.application.server.dao.EngineDAO;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rafael Ferreira da Silva
 */
@Repository
@Transactional
public class EngineData extends JdbcDaoSupport implements EngineDAO {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public void useDataSource(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public void add(Engine engine) throws DAOException {
        String query = "INSERT INTO VIPEngines(name, endpoint, status) VALUES (?,?,?)";

        try (PreparedStatement ps = getConnection().prepareStatement(query)) {
            ps.setString(1, engine.getName());
            ps.setString(2, engine.getEndpoint());
            ps.setString(3, engine.getStatus());
            ps.executeUpdate();

        } catch (SQLException ex) {
            if (ex.getMessage().contains("Unique index or primary key violation") || ex.getMessage().contains("Duplicate entry ")) {
                logger.error("An engine named \"{}\" already exists.", engine.getName());
                throw new DAOException("An engine named \"" + engine.getName() + "\" already exists.");
            } else {
                logger.error("Error adding engine {}", engine.getEndpoint(), ex);
                throw new DAOException(ex);
            }
        }
    }

    @Override
    public void update(Engine engine) throws DAOException {
        String query = "UPDATE VIPEngines SET endpoint = ?, status = ? WHERE name = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(query)) {
            ps.setString(1, engine.getEndpoint());
            ps.setString(2, engine.getStatus());
            ps.setString(3, engine.getName());
            ps.executeUpdate();

        } catch (SQLException ex) {
            logger.error("Error updating engine {} to {}", engine.getName(), engine.getEndpoint(), ex);
            throw new DAOException(ex);
        }
    }

    @Override
    public void remove(String name) throws DAOException {
        String query = "DELETE FROM VIPEngines WHERE name = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(query)) {
            ps.setString(1, name);
            ps.executeUpdate();

        } catch (SQLException ex) {
            if (ex.getMessage().contains("Unique index or primary key violation") || ex.getMessage().contains("Duplicate entry ")) {
                logger.error("There is no engine registered with the name {}", name);
                throw new DAOException("There is no engine registered with the name : " + name);
            } else {
                logger.error("Error removing engine {}", name, ex);
                throw new DAOException(ex);
            }
        }
    }

    @Override
    public List<Engine> get() throws DAOException {
        String query = "SELECT name, endpoint, status FROM VIPEngines ORDER BY name";

        try (PreparedStatement ps = getConnection().prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            List<Engine> list = new ArrayList<Engine>();

            while (rs.next()) {
                list.add(new Engine(rs.getString("name"), rs.getString("endpoint"), rs.getString("status")));
            }
            return list;

        } catch (SQLException ex) {
            logger.error("Error getting all engines", ex);
            throw new DAOException(ex);
        }
    }

    @Override
    public List<Engine> getByResource(Resource resource) throws DAOException {
        String query =  "SELECT * FROM VIPEngines e "
        +               "JOIN VIPResourcesEngines re ON e.name = re.enginename "
        +               "WHERE re.resourcename = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(query)) {
            ps.setString(1, resource.getName());

            ResultSet rs = ps.executeQuery();
            List<Engine> list = new ArrayList<Engine>();

            while (rs.next()) {
                list.add(new Engine(rs.getString("engineName"), rs.getString("endpoint"), rs.getString("status")));
            }
            return list;

        } catch (SQLException ex) {
            logger.error("Error getting engines by resource {}", resource.getName(), ex);
            throw new DAOException(ex);
        }  
    }
}
