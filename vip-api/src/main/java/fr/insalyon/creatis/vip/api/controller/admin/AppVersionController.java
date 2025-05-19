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
package fr.insalyon.creatis.vip.api.controller.admin;

import fr.insalyon.creatis.vip.api.controller.ApiController;
import fr.insalyon.creatis.vip.api.exception.ApiException;
import fr.insalyon.creatis.vip.application.client.bean.AppVersion;
import fr.insalyon.creatis.vip.application.client.bean.Application;
import fr.insalyon.creatis.vip.application.server.business.AppVersionBusiness;
import fr.insalyon.creatis.vip.application.server.business.ApplicationBusiness;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("admin/appVersions")
public class AppVersionController extends ApiController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ApplicationBusiness applicationBusiness;
    private final AppVersionBusiness appVersionBusiness;

    @Autowired
    protected AppVersionController(ApplicationBusiness applicationBusiness, AppVersionBusiness appVersionBusiness) {
        this.applicationBusiness = applicationBusiness;
        this.appVersionBusiness = appVersionBusiness;
    }

    @RequestMapping
    public List<AppVersion> listAppVersions() throws ApiException {
        logMethodInvocation(logger, "listAppVersions");
        try {
            List<Application> apps = applicationBusiness.getApplications();
            List<AppVersion> appVersions = new ArrayList<>();
            for (Application app : apps) {
                appVersions.addAll(appVersionBusiness.getVersions(app.getName()));
            }
            return appVersions;
        } catch (BusinessException e) {
            throw new ApiException(e);
        }
    }

    @RequestMapping("{appVersionId}")
    public AppVersion getAppVersion(@PathVariable String appVersionId) throws ApiException {
        logMethodInvocation(logger, "getAppVersion", appVersionId);
        try {
            appVersionId = URLDecoder.decode(appVersionId, "UTF8");
        } catch (UnsupportedEncodingException e) {
            logger.error("Error decoding appVersionId {}", appVersionId, e);
            throw new ApiException("cannot decode appVersionId : " + appVersionId);
        }
        try {
            AppVersion appVersion = null;
            int delimiterPos = appVersionId.lastIndexOf("/");
            if (delimiterPos >= 0) {
                String appName = appVersionId.substring(0, delimiterPos);
                String version = appVersionId.substring(delimiterPos + 1);
                appVersion = appVersionBusiness.getVersion(appName, version); // XXX NPE on unknown appName/version
            }
            if (appVersion == null) {
                throw new ApiException("Not found"); // XXX should 404/403 ?
            }
            return appVersion;
        } catch (BusinessException e) {
            throw new ApiException(e);
        }
    }

    @RequestMapping("{appVersionIdFirstPart}/{appVersionIdSecondPart}")
    public AppVersion getAppVersion(@PathVariable String appVersionIdFirstPart,
                                    @PathVariable String appVersionIdSecondPart) throws ApiException {
        return getAppVersion(appVersionIdFirstPart + "/" + appVersionIdSecondPart);
    }
}
