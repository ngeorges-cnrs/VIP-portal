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
package fr.insalyon.creatis.vip.api.rest.controller;

import fr.insalyon.creatis.vip.api.bean.Execution;
import fr.insalyon.creatis.vip.api.business.*;
import fr.insalyon.creatis.vip.api.rest.RestApiBusiness;
import fr.insalyon.creatis.vip.api.rest.model.DeleteExecutionConfiguration;
import fr.insalyon.creatis.vip.application.server.business.*;
import fr.insalyon.creatis.vip.core.server.business.ConfigurationBusiness;
import fr.insalyon.creatis.vip.datamanager.server.business.TransferPoolBusiness;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static jdk.nashorn.internal.runtime.regexp.joni.constants.StackType.POS;

/**
 * Created by abonnet on 7/13/16.
 */
@RestController
@RequestMapping("/executions")
public class ExecutionControler {

    public static final Logger logger = Logger.getLogger(ExecutionControler.class);

    @Autowired
    private WorkflowBusiness workflowBusiness;
    @Autowired
    private ApplicationBusiness applicationBusiness;
    @Autowired
    private ClassBusiness classBusiness;
    @Autowired
    private SimulationBusiness simulationBusiness;
    @Autowired
    private ConfigurationBusiness configurationBusiness;
    @Autowired
    private TransferPoolBusiness transferPoolBusiness;

    // although the controller is a singleton, these are proxies that always point on the current request
    @Autowired
    HttpServletRequest httpServletRequest;

    private PipelineBusiness buildPipelineBusiness(ApiContext apiContext) {
        return new PipelineBusiness(apiContext,
                workflowBusiness, applicationBusiness, classBusiness);
    }


    private ExecutionBusiness buildExecutionBusiness(ApiContext apiContext) {
        return buildExecutionBusiness(apiContext, null);
    }

    private ExecutionBusiness buildExecutionBusiness(ApiContext apiContext,
                                                     PipelineBusiness pipelineBusiness) {
        if (pipelineBusiness == null) {pipelineBusiness = new PipelineBusiness(apiContext, workflowBusiness,
                applicationBusiness, classBusiness);
        }
        return new ExecutionBusiness(apiContext,
                simulationBusiness, workflowBusiness, configurationBusiness, applicationBusiness,
                pipelineBusiness, transferPoolBusiness);
    }

    @RequestMapping("/{executionId}")
    public Execution getExecution(@PathVariable String executionId) throws ApiException {
        ApiUtils.methodInvocationLog("getExecution", executionId);
        ApiContext apiContext = new RestApiBusiness().getApiContext(httpServletRequest, true);
        ExecutionBusiness eb = buildExecutionBusiness(apiContext);
        eb.checkIfUserCanAccessExecution(executionId);
        return eb.getExecution(executionId,false);
    }

    @RequestMapping(value = "/{executionId}", method = RequestMethod.PUT)
    public Execution updateExecution(@PathVariable String executionId,
                                     @RequestBody @Valid Execution execution) throws ApiException {
        ApiUtils.methodInvocationLog("updateExecution", executionId);
        execution.setIdentifier(executionId);
        ApiContext apiContext = new RestApiBusiness().getApiContext(httpServletRequest, true);
        ExecutionBusiness eb = buildExecutionBusiness(apiContext);
        eb.checkIfUserCanAccessExecution(executionId);
        eb.updateExecution(execution);
        return eb.getExecution(executionId,false);
    }

    @RequestMapping(method = RequestMethod.POST)
    public Execution initExecution(@RequestBody @Valid Execution execution) throws ApiException {
        ApiUtils.methodInvocationLog("initExecution", execution);
        ApiContext apiContext = new RestApiBusiness().getApiContext(httpServletRequest, true);
        PipelineBusiness pb = buildPipelineBusiness(apiContext);
        pb.checkIfUserCanAccessPipeline(execution.getPipelineIdentifier());
        ExecutionBusiness executionBusiness = buildExecutionBusiness(apiContext, pb);
        String execId = executionBusiness.initExecution(execution);
        return executionBusiness.getExecution(execId,false);
    }

    @RequestMapping("/{executionId}/stdout")
    public String getStdout(@PathVariable String executionId) throws ApiException {
        ApiUtils.methodInvocationLog("getStdout", executionId);
        ApiContext apiContext = new RestApiBusiness().getApiContext(httpServletRequest, true);
        ExecutionBusiness eb = buildExecutionBusiness(apiContext);
        eb.checkIfUserCanAccessExecution(executionId);
        return eb.getStdOut(executionId);
    }

    @RequestMapping("/{executionId}/stderr")
    public String getStderr(@PathVariable String executionId) throws ApiException {
        ApiUtils.methodInvocationLog("getStderr", executionId);
        ApiContext apiContext = new RestApiBusiness().getApiContext(httpServletRequest, true);
        ExecutionBusiness eb = buildExecutionBusiness(apiContext);
        eb.checkIfUserCanAccessExecution(executionId);
        return eb.getStdErr(executionId);
    }

    @RequestMapping(value = "/{executionId}/play", method = RequestMethod.PUT)
    public void playExecution(@PathVariable String executionId) throws ApiException {
        ApiUtils.methodInvocationLog("playExecution", executionId);
        ApiContext apiContext = new RestApiBusiness().getApiContext(httpServletRequest, true);
        ExecutionBusiness eb = buildExecutionBusiness(apiContext);
        eb.checkIfUserCanAccessExecution(executionId);
        eb.playExecution(executionId);
    }

    @RequestMapping(value = "/{executionId}/kill", method = RequestMethod.PUT)
    public void killExecution(@PathVariable String executionId) throws ApiException {
        ApiUtils.methodInvocationLog("killExecution", executionId);
        ApiContext apiContext = new RestApiBusiness().getApiContext(httpServletRequest, true);
        ExecutionBusiness eb = buildExecutionBusiness(apiContext);
        eb.checkIfUserCanAccessExecution(executionId);
        eb.killExecution(executionId);
    }

    @RequestMapping(value = "/{executionId}/delete", method = RequestMethod.PUT)
    public void deleteExecution(@PathVariable String executionId,
                                @RequestBody @Valid DeleteExecutionConfiguration delConfig) throws ApiException {
        ApiUtils.methodInvocationLog("deleteExecution", executionId);
        ApiContext apiContext = new RestApiBusiness().getApiContext(httpServletRequest, true);
        ExecutionBusiness eb = buildExecutionBusiness(apiContext);
        eb.checkIfUserCanAccessExecution(executionId);
        eb.deleteExecution(executionId, delConfig.getDeleteFiles());
    }
}
