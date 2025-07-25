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
package fr.insalyon.creatis.vip.application.client.bean;

import com.google.gwt.user.client.rpc.IsSerializable;

import fr.insalyon.creatis.vip.core.client.bean.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Application implements IsSerializable {

    private String name;
    private String citation;
    private String owner;
    private String fullName;
    private List<Group> groups;

    public Application() {}

    public Application(String name, String citation) {
        this(name, null, null, citation);
    }

    public Application(String name, String citation, List<Group> groups) {
        this(name, null, null, citation, groups);
    }

    public Application(String name, String owner, String fullName, String citation) {
        this(name, owner, fullName, citation, new ArrayList<>());
    }

    public Application(String name, String owner, String citation) {
        this(name, owner, null, citation, new ArrayList<>());
    }

    public Application(String name, String owner, String fullName, String citation, List<Group> groups) {
        this.name = name;
        this.citation = citation;
        this.owner = owner;
        this.fullName = fullName;
        this.groups = groups;
    }

    public String getName() {
        return name;
    }

    public String getCitation() {
        return citation;
    }

    public String getOwner() {
        return owner;
    }

    public void removeOwner() {
        owner = null;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getFullName() {
        return fullName;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public List<String> getGroupsNames() {
        return groups.stream().map(Group::getName).collect(Collectors.toList());
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}
