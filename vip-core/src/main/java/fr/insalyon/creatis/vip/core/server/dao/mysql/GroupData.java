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
package fr.insalyon.creatis.vip.core.server.dao.mysql;

import fr.insalyon.creatis.vip.core.client.bean.Group;
import fr.insalyon.creatis.vip.core.client.bean.GroupType;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import fr.insalyon.creatis.vip.core.server.dao.GroupDAO;
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
public class GroupData extends JdbcDaoSupport implements GroupDAO {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public void useDataSource(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public void add(Group group) throws DAOException {
        String query = "INSERT INTO VIPGroups(name, public, type, auto) VALUES(?, ?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(query)) {
            ps.setString(1, group.getName());
            ps.setBoolean(2, group.isPublicGroup());
            ps.setString(3, group.getType().toString());
            ps.setBoolean(4, group.isAuto());
            ps.execute();

        } catch (SQLException ex) {
            if (ex.getMessage().contains("Unique index or primary key violation") || ex.getMessage().contains("Duplicate entry ")) {
                logger.error("A group named {} already exists", group.getName());
                throw new DAOException("A group named " + group.getName() + " already exists");
            } else {
                logger.error("Error adding group {}", group.getName(), ex);
                throw new DAOException(ex);
            }
        }
    }

    @Override
    public void remove(String groupName) throws DAOException {
        String query = "DELETE FROM VIPGroups WHERE name=?";

        try (PreparedStatement ps = getConnection().prepareStatement(query)) {
            ps.setString(1, groupName);
            ps.execute();

        } catch (SQLException ex) {
            logger.error("Error removing group {}", groupName, ex);
            throw new DAOException(ex);
        }
    }

    @Override
    public void update(String name, Group group) throws DAOException {
        String query = "UPDATE VIPGroups SET name=?, public=?, type=?, auto=? WHERE name=?";

        try (PreparedStatement ps = getConnection().prepareStatement(query)) {
            ps.setString(1, group.getName());
            ps.setBoolean(2, group.isPublicGroup());
            ps.setString(3, group.getType().toString());
            ps.setBoolean(4, group.isAuto());
            ps.setString(5, name);
            ps.executeUpdate();

        } catch (SQLException ex) {
            logger.error("Error updating group {}", group.getName(), ex);
            throw new DAOException(ex);
        }
    }

    @Override
    public List<Group> get() throws DAOException {
        List<Group> groups = new ArrayList<Group>();
        String query = "SELECT * FROM VIPGroups ORDER BY LOWER(name)";
 
        try (PreparedStatement ps = getConnection().prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                groups.add(fromRs(rs));
            }
            return groups;

        } catch (SQLException ex) {
            logger.error("Error getting all groups", ex);
            throw new DAOException(ex);
        }
    }

    @Override
    public List<Group> getByType(GroupType type) throws DAOException {
        List<Group> groups = new ArrayList<Group>();
        String query = "SELECT * FROM VIPGroups WHERE type = ? ORDER BY LOWER(name)";
 
        try (PreparedStatement ps = getConnection().prepareStatement(query)) {
            ps.setString(1, type.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                groups.add(fromRs(rs));
            }
            return groups;

        } catch (SQLException ex) {
            logger.error("Error getting group with type " + type, ex);
            throw new DAOException(ex);
        }
    }

    @Override
    public List<Group> getByApplication(String applicationName) throws DAOException {
        List<Group> groups = new ArrayList<Group>();
        String query =  "SELECT * FROM VIPGroups g "
        +               "JOIN VIPGroupsApplications ga ON ga.groupname = g.name "
        +               "WHERE ga.applicationname = ?";
 
        try (PreparedStatement ps = getConnection().prepareStatement(query)) {
            ps.setString(1, applicationName);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                groups.add(fromRs(rs));
            }
            return groups;

        } catch (SQLException ex) {
            logger.error("Error getting group linked to app " + applicationName, ex);
            throw new DAOException(ex);
        }
    }

    @Override
    public List<Group> getByRessource(String resourceName) throws DAOException {
        List<Group> groups = new ArrayList<Group>();
        String query =  "SELECT * FROM VIPGroups g "
        +               "JOIN VIPGroupsResources ga ON ga.groupname = g.name "
        +               "WHERE ga.resourcename = ?";
 
        try (PreparedStatement ps = getConnection().prepareStatement(query)) {
            ps.setString(1, resourceName);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                groups.add(fromRs(rs));
            }
            return groups;

        } catch (SQLException ex) {
            logger.error("Error getting group linked to ressource " + resourceName, ex);
            throw new DAOException(ex);
        }
    }

    private Group fromRs(ResultSet rs) throws SQLException {
        return new Group(rs.getString("name"), rs.getBoolean("public"),
            rs.getString("type"), rs.getBoolean("auto"));
    }
}
