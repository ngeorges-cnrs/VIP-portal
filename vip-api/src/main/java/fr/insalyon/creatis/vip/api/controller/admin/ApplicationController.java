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
import fr.insalyon.creatis.vip.application.client.bean.Application;
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
import java.util.List;

@RestController
@RequestMapping("admin/applications")
public class ApplicationController extends ApiController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ApplicationBusiness applicationBusiness;

    @Autowired
    protected ApplicationController(ApplicationBusiness applicationBusiness) {
        this.applicationBusiness = applicationBusiness;
    }

    @RequestMapping
    public List<Application> listApplications() throws ApiException {
        logMethodInvocation(logger, "listApplications");
        try {
            return applicationBusiness.getApplications();
        } catch (BusinessException e) {
            throw new ApiException(e);
        }
    }

    @RequestMapping("{applicationId}")
    public Application getApplication(@PathVariable String applicationId) throws ApiException {
        logMethodInvocation(logger, "getApplication", applicationId);
        try {
            applicationId = URLDecoder.decode(applicationId, "UTF8");
        } catch (UnsupportedEncodingException e) {
            logger.error("Error decoding applicationId {}", applicationId, e);
            throw new ApiException("cannot decode applicationId : " + applicationId);
        }
        try {
            Application app = applicationBusiness.getApplication(applicationId);
            if (app == null) {
                throw new ApiException("Not found"); // XXX should 404/403 ?
            }
            return app;
        } catch (BusinessException e) {
            throw new ApiException(e);
        }
    }

}
