package fr.insalyon.creatis.vip.application.server.dao;

import java.util.List;
import fr.insalyon.creatis.vip.application.client.bean.AppVersion;
import fr.insalyon.creatis.vip.application.client.bean.Engine;
import fr.insalyon.creatis.vip.application.client.bean.Resource;
import fr.insalyon.creatis.vip.core.client.bean.Group;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;

public interface ResourceDAO {
    
    public void add(Resource resource) throws DAOException;

    public void update(Resource resource) throws DAOException;

    public void remove(Resource resource) throws DAOException;

    public List<Resource> getAll() throws DAOException;

    public List<Resource> getByUser(User user) throws DAOException;

    public List<Resource> getByEngine(Engine engine) throws DAOException;

    public List<Resource> getByAppVersion(AppVersion appVersion) throws DAOException;

    public List<Resource> getByGroup(Group group) throws DAOException;

    public void associate(Resource resource, Group group) throws DAOException;

    public void dissociate(Resource resource, Group group) throws DAOException;

    public void associate(Resource resource, AppVersion appVersion) throws DAOException;

    public void dissociate(Resource resource, AppVersion appVersion) throws DAOException;

    public void associate(Resource resource, Engine engine) throws DAOException;

    public void dissociate(Resource resource, Engine engine) throws DAOException;
}
