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

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insalyon.creatis.vip.application.client.bean.AppVersion;
import fr.insalyon.creatis.vip.application.server.model.boutiques.BoutiquesDescriptor;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import fr.insalyon.creatis.vip.core.server.business.Server;
import fr.insalyon.creatis.vip.datamanager.server.business.DataManagerBusiness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abonnet on 2/21/19.
 */
@Service
@Transactional
public class BoutiquesBusiness {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Server server;
    private DataManagerBusiness dataManagerBusiness;
    private AppVersionBusiness appVersionBusiness;
    private ObjectMapper objectMapper;

    @Autowired
    public BoutiquesBusiness(Server server, DataManagerBusiness dataManagerBusiness,
            ObjectMapper objectMapper, AppVersionBusiness appVersionBusiness) {
        this.server = server;
        this.dataManagerBusiness = dataManagerBusiness;
        this.objectMapper = objectMapper;
        this.appVersionBusiness = appVersionBusiness;
    }

    public String publishVersion(User user, String applicationName, String version)
            throws BusinessException {
        AppVersion appVersion = appVersionBusiness.getVersion(applicationName, version);

        // verify that the descriptor has an author
        BoutiquesDescriptor descriptor = parseBoutiquesString(appVersion.getDescriptor());
        String author = descriptor.getAuthor();
        if (author == null || author.isEmpty()) {
            logger.error("Can't publish an descriptor with no author");
            throw new BusinessException("Can't publish an descriptor with no author");
        }

        // The basename of the file used in bosh publish will be visible on zenodo,
        // so we want a "clean" name, and a unique location on the filesystem to avoid race conditions.
        Path tempDir, tempFile;
        try {
            // create a dedicated unique temporary directory
            tempDir = Files.createTempDirectory("VipPublish-");
            // create the descriptor filename inside this dir
            tempFile = tempDir.resolve(appVersion.getDescriptorFilename());
            // write the descriptor content to that file
            Files.write(tempFile, appVersion.getDescriptor().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.error("Failed creating temporary file for publish", e);
            throw new BusinessException("Failed creating temporary file for publish", e);
        }

        // do the publishing, and cleanup tempFile+tempDir afterward
        String doi;
        try {
            // call publish command
            String command = "FILE=" + tempFile + "; " + server.getPublicationCommandLine();
            List<String> output = runCommandAndFailOnError(command);

            // get the doi
            // There should be only one line with the DOI
            doi = getDoiFromPublishOutput(output);

            // save the doi in database
            saveDoiForVersion(doi, applicationName, version);
        } finally {
            // failure to clean up temp files is not fatal
            try {
                Files.delete(tempFile);
                Files.delete(tempDir);
            } catch (IOException e) {
                logger.warn("Failed deleting temporary file after publish:" + tempFile, e);
            }
        }
        return doi;
    }

    public void validateBoutiqueFile(String localPath) throws BusinessException {
        // check file size, 100 kiB max
        try {
            if (Files.size(Paths.get(localPath)) >= 100 * 1024) {
                throw new BusinessException("Boutiques file too large");
            }
        } catch (IOException e) {
            throw new BusinessException("Can't get boutiques file size", e);
        }
        // call validate command
        String command = "bosh validate " + localPath;
        try {
            // if no exception : the command was  successful
            runCommand(command);
        } catch (CommandErrorException e) {
            // if there's an error, only keep the first line because the output can be very long
            // and the first line contains the json validation error message
            String firstLine = e.getCout().isEmpty() ? "< No Information> " : e.getCout().get(0);
            throw new BusinessException("Boutiques file not valid : " + firstLine);
        }
    }

    public String getApplicationDescriptorString(String applicationName, String applicationVersion)
            throws BusinessException {
        AppVersion appVersion = appVersionBusiness.getVersion(applicationName, applicationVersion);
        return appVersion.getDescriptor();
    }

    public BoutiquesDescriptor parseBoutiquesFile(File boutiquesFile) throws BusinessException {
        try {
            return objectMapper.readValue(boutiquesFile, BoutiquesDescriptor.class);
        } catch (IOException e) {
            logger.error("Error reading {} file for boutiques parsing", boutiquesFile, e);
            throw new BusinessException("Error reading boutiques file", e);
        }
    }

    public BoutiquesDescriptor parseBoutiquesString(String descriptor) throws BusinessException {
        try {
            return objectMapper.readValue(descriptor, BoutiquesDescriptor.class);
        } catch (IOException e) {
            logger.error("Error parsing descriptor", e);
            throw new BusinessException("Error parsing descriptor", e);
        }
    }

    private void saveDoiForVersion(String doi, String applicationName, String applicationVersion) throws BusinessException {
        appVersionBusiness.updateDoiForVersion(doi, applicationName, applicationVersion);
    }

    private String getDoiFromPublishOutput(List<String> publishOutput) throws BusinessException {
        if (publishOutput.size() != 1) {
            logger.error("Wrong publication output, there should be only one line : {}",
                    String.join("\n", publishOutput));
            throw new BusinessException("Wrong publication output.");
        }
        return publishOutput.get(0);
    }

    private class CommandErrorException extends Exception {

        private List<String> cout;

        public CommandErrorException(List<String> cout) {
            this.cout = cout;
        }

        public List<String> getCout() {
            return cout;
        }
    }


    private List<String> runCommandAndFailOnError(String command) throws BusinessException {
        try {
            return runCommand(command);
        } catch (CommandErrorException e) {
            throw new BusinessException("Command {" + command + "} failed : " + String.join("\n", e.getCout()));
        }
    }

    private List<String> runCommand(String command) throws CommandErrorException, BusinessException {
        ProcessBuilder builder = new ProcessBuilder("bash", "-c", command);
        builder.redirectErrorStream(true);
        Process process = null;
        List<String> cout = new ArrayList<>();

        try {
            logger.info("Executing command : " + command);
            process = builder.start();
            BufferedReader r = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String s;
            while ((s = r.readLine()) != null) {
                cout.add(s);
            }
            process.waitFor();
            closeProcess(process);
        } catch (IOException | InterruptedException e) {
            logger.error("Unexpected error in a boutiques command : {}",
                    String.join("\n", cout), e);
            throw new BusinessException("Unexpected error in a boutiques command", e);
        } finally {
            closeProcess(process);
        }

        if (process.exitValue() != 0) {
            logger.error("Command failed : {}",
                    String.join("\n", cout));
            throw new CommandErrorException(cout);
        }
        process = null;
        return cout;
    }

    private void closeProcess(Process process) {
        if (process == null) return;
        close(process.getOutputStream());
        close(process.getInputStream());
        close(process.getErrorStream());
        process.destroy();
    }

    private void close(Closeable c) {

        if (c != null) {
            try {
                c.close();
            } catch (IOException ex) {
                logger.error("Error closing {}", c);
            }
        }
    }
}
