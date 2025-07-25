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
package fr.insalyon.creatis.vip.application.server.business;

import fr.insalyon.creatis.vip.application.client.bean.AppVersion;
import fr.insalyon.creatis.vip.application.client.bean.Application;
import fr.insalyon.creatis.vip.application.server.dao.ApplicationDAO;
import fr.insalyon.creatis.vip.core.client.bean.Group;
import fr.insalyon.creatis.vip.core.client.bean.GroupType;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import fr.insalyon.creatis.vip.core.server.business.ConfigurationBusiness;
import fr.insalyon.creatis.vip.core.server.business.GroupBusiness;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ApplicationBusiness {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ApplicationDAO applicationDAO;
    private GroupBusiness groupBusiness;
    private ConfigurationBusiness configurationBusiness;
    private AppVersionBusiness appVersionBusiness;

    @Autowired
    public ApplicationBusiness(ApplicationDAO applicationDAO, GroupBusiness groupBusiness, ConfigurationBusiness configurationBusiness, AppVersionBusiness appVersionBusiness) {
        this.applicationDAO = applicationDAO;
        this.groupBusiness = groupBusiness;
        this.configurationBusiness = configurationBusiness;
        this.appVersionBusiness = appVersionBusiness;
    }

    public void add(Application application) throws BusinessException {
        try {
            applicationDAO.add(application);

            for (String groupName : application.getGroupsNames()) {
                associate(application, new Group(groupName));
            }
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    public void remove(String name) throws BusinessException {
        try {
            applicationDAO.remove(name);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    public void update(Application application) throws BusinessException {
        try {
            Application before = getApplication(application.getName());
            List<String> beforeGroupsNames = before.getGroupsNames();

            applicationDAO.update(application);
            for (String group : application.getGroupsNames()) {
                if ( ! beforeGroupsNames.removeIf((s) -> s.equals(group))) {
                    associate(application, new Group(group));
                }
            }
            for (String group : beforeGroupsNames) {
                dissociate(application, new Group(group));
            }
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    public Application getApplication(String applicationName) throws BusinessException {
        try {
            return mapGroups(applicationDAO.getApplication(applicationName));
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    public List<Application> getApplications() throws BusinessException {
        try {
            return mapGroups(applicationDAO.getApplications());
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    public List<Application> getApplications(User user) throws BusinessException {
        List<Group> userGroups = configurationBusiness.getUserGroups(user.getEmail()).keySet()
            .stream()
            .filter((g) -> g.getType().equals(GroupType.APPLICATION))
            .collect(Collectors.toList());
        List<Application> result = new ArrayList<>();
        Set<String> appNames = new HashSet<>();

        for (Group group : userGroups) {
            for (Application app : getApplications(group)) {
                if (appNames.add(app.getName())) {
                    result.add(app);
                }
            }
        }
        return result;
    }

    public List<Application> getApplications(Group group) throws BusinessException {
        try {
            return mapGroups(applicationDAO.getApplicationsByGroup(group));
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    public List<Application> getApplicationsWithOwner(String email) throws BusinessException {

        try {
            return mapGroups(applicationDAO.getApplicationsWithOwner(email));
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    public List<Application> getPublicApplications() throws BusinessException {
        List<Group> publicAppGroups = groupBusiness.getPublic()
            .stream()
            .filter((g) -> g.getType().equals(GroupType.APPLICATION))
            .collect(Collectors.toList());
        List<Application> apps = new ArrayList<>();

        for (Group group : publicAppGroups) {
            for (Application app : getApplications(group)) {
                // keep application if at least a Version is visible
                if (appVersionBusiness.getVersions(app.getName()).stream().anyMatch(AppVersion::isVisible)) {
                    apps.add(app);
                }
            }
        }

        // remove doublons + sort
        return apps.stream().collect(Collectors.toMap(Application::getName, a -> a, (a1, a2) -> a1)).values()
                .stream().sorted(Comparator.comparing(Application::getName)).collect(Collectors.toList());
    }

    public List<Group> getPublicGroupsForApplication(String applicationName) throws BusinessException {
        Application application = getApplication(applicationName);

        if (application == null) {
            logger.error("No application exists with name {}", applicationName);
            throw new BusinessException("Wrong application name");
        }

        List<Group> appGroups = groupBusiness.getByApplication(applicationName);

        return appGroups.stream()
            .filter((g) -> g.isPublicGroup())
            .collect(Collectors.toList());
    }

    public List<String> getApplicationNames() throws BusinessException {

        List<String> applicationNames = new ArrayList<String>();
        for (Application application : getApplications()) {
            applicationNames.add(application.getName());
        }
        return applicationNames;
    }

    public String getCitation(String name) throws BusinessException {
        try {
            return applicationDAO.getCitation(name);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    public void associate(Application app, Group group) throws BusinessException {
        app = getApplication(app.getName());
        group = groupBusiness.get(group.getName());

        try {
            applicationDAO.associate(app, group);
        } catch (DAOException e) {
            throw new BusinessException(e);
        }
    }

    public void dissociate(Application app, Group group) throws BusinessException {
        try {
            applicationDAO.dissociate(app, group);
        } catch (DAOException e) {
            throw new BusinessException(e);
        }
    }

    private Application mapGroups(Application app) throws BusinessException {
        if (app == null) {
            return null;
        } else {
            app.setGroups(groupBusiness.getByApplication(app.getName()));
            return app;
        }
    }

    private List<Application> mapGroups(List<Application> apps) throws BusinessException {
        for (Application app : apps) {
            mapGroups(app);
        }
        return apps;
    }
}
