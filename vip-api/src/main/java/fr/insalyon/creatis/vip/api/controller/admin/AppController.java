package fr.insalyon.creatis.vip.api.controller.admin;

import fr.insalyon.creatis.vip.api.controller.ApiController;
import fr.insalyon.creatis.vip.api.exception.ApiException;
import fr.insalyon.creatis.vip.api.model.Pipeline;
import fr.insalyon.creatis.vip.application.client.bean.AppVersion;
import fr.insalyon.creatis.vip.application.client.bean.Application;
import fr.insalyon.creatis.vip.application.server.business.AppVersionBusiness;
import fr.insalyon.creatis.vip.application.server.business.ApplicationBusiness;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@RestController
@RequestMapping("/admin/apps")
public class AppController extends ApiController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Supplier<User> currentUserProvider;
    private final ApplicationBusiness applicationBusiness;
    private final AppVersionBusiness appVersionBusiness;

    @Autowired
    protected AppController(Supplier<User> currentUserProvider,
                            ApplicationBusiness applicationBusiness,
                            AppVersionBusiness appVersionBusiness) {
        this.currentUserProvider = currentUserProvider; // XXX TODO check admin ?
        this.applicationBusiness = applicationBusiness;
        this.appVersionBusiness = appVersionBusiness;
    }

    @RequestMapping
    public List<Application> listApps(
            @RequestParam(required = false) String appIdentifier) throws ApiException {
        try {
            // XXX TODO log, descriptor, ...
            logger.info("XXX getApplications id=", appIdentifier);
            logMethodInvocation(logger, "listApps");
            return applicationBusiness.getApplications();
        } catch (BusinessException e) {
            throw new ApiException(e);
        }
    }

    @RequestMapping("{appId}")
    public Application getApp(@PathVariable String appId) throws ApiException {
        try {
            // XXX logger, should 404 if not found, ...
            return applicationBusiness.getApplication(appId);
        } catch (BusinessException e) {
            throw new ApiException(e);
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public Application createApp(@RequestBody @Valid Application app)
            throws ApiException {
        logMethodInvocation(logger, "createApp", app);
        // XXX new Application("foo", "admin@example.com", "", false);
        return app;
    }

    @RequestMapping(path = "{appId}", method = RequestMethod.PUT)
    public Application updateApp(@PathVariable String appId, @RequestBody @Valid Application app)
            throws ApiException {
        try {
            logger.info("XXX updateApp" + "(" + appId + "):" + app);
            Application app0 = applicationBusiness.getApplication(appId);
            // XXX check app0/app
            applicationBusiness.update(app);
            return app;
        } catch (BusinessException e) {
            throw new ApiException(e);
        }
    }

    // XXX TODO separate controller
    @RequestMapping("/versions")
    public List<AppVersion> listAppVersions() throws ApiException {
        try {
            List<AppVersion> versions = new ArrayList<>();
            List<Application> apps = applicationBusiness.getApplications();
            for (Application app : apps) {
                List<AppVersion> appVersions = appVersionBusiness.getVersions(app.getName());
                versions.addAll(appVersions);
            }
            return versions;
        } catch (BusinessException e) {
            throw new ApiException(e);
        }
    }
}
