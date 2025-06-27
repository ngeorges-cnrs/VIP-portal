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
package fr.insalyon.creatis.vip.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Tristan Glatard
 */
public class Pipeline {

    private String identifier;
    private String name;
    private String description;
    private String version;
    private ArrayList<PipelineParameter> parameters;
    private boolean canExecute;
    @JsonIgnore
    private Map<String, String> overriddenInputs;

    public Pipeline() {
    }
    
     public Pipeline(String identifier, String name, String version) {
        this.identifier = identifier;
        this.name = name;       
        this.version = version;
        this.canExecute = true;
        parameters = new ArrayList<>();
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

    public ArrayList<PipelineParameter> getParameters() {
        return parameters;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("canExecute") // jackson only take into account getter by default
    public boolean canExecute(){
        return canExecute;
    }

    public Map<String, String> getOverriddenInputs() {
        return overriddenInputs;
    }

    public void setOverriddenInputs(Map<String, String> overriddenInputs) {
        this.overriddenInputs = overriddenInputs;
    }
}
