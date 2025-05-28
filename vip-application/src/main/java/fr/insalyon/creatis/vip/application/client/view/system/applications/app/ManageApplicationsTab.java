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
package fr.insalyon.creatis.vip.application.client.view.system.applications.app;

import java.util.Map;

import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import fr.insalyon.creatis.vip.application.client.ApplicationConstants;
import fr.insalyon.creatis.vip.application.client.view.system.applications.version.EditVersionLayout;
import fr.insalyon.creatis.vip.application.client.view.system.applications.version.ManageVersionLayout;
import fr.insalyon.creatis.vip.application.client.view.system.applications.version.VersionsLayout;
import fr.insalyon.creatis.vip.core.client.view.common.AbstractManageTab;
import fr.insalyon.creatis.vip.core.client.view.util.WidgetUtil;

/**
 *
 * @author Rafael Ferreira da Silva
 */
public class ManageApplicationsTab extends AbstractManageTab {

    private ApplicationsLayout appsLayout;
    private EditApplicationLayout editLayout;
    private VersionsLayout versionsLayout;
    private EditVersionLayout editVersionLayout;
    private ManageVersionLayout manageVersionLayout;
    private Label infoAppText;

    public ManageApplicationsTab(boolean onlyPublicApps) {

        super(ApplicationConstants.ICON_APPLICATION, ApplicationConstants.APP_APPLICATION, ApplicationConstants.TAB_MANAGE_APPLICATION);

        appsLayout = new ApplicationsLayout(onlyPublicApps);

        titleInfoApp();
        HLayout appLayout = new HLayout(5);
        appLayout.setHeight("50%");
        appLayout.addMember(appsLayout);
        vLayout.addMember(infoAppText);
        vLayout.addMember(appLayout);

        if( ! onlyPublicApps ) {
            editLayout = new EditApplicationLayout();
            versionsLayout = new VersionsLayout();
            editVersionLayout = new EditVersionLayout();
            manageVersionLayout = new ManageVersionLayout();
            appLayout.addMember(editLayout);
            VLayout versionInfoLayout = new VLayout(5);

            HLayout versionLayout = new HLayout(5);
            versionLayout.setHeight("50%");
            versionLayout.addMember(versionsLayout);

            versionInfoLayout.addMember(editVersionLayout);
            versionInfoLayout.addMember(manageVersionLayout);
            versionLayout.addMember(versionInfoLayout);
            vLayout.addMember(versionLayout);
        }
    }

    public void loadApplications() {
        appsLayout.loadData();
    }

    public void loadVersions(String applicationName) {
        versionsLayout.setApplication(applicationName);
        editVersionLayout.setApplication(applicationName);
        manageVersionLayout.setApplication(applicationName);
    }

    public void setApplication(String name, String owner, String citation, String[] groups, boolean isPublic) {
        editLayout.setApplication(name, owner, citation, groups, isPublic);
    }

    public void setVersion(String version, String descriptor, String doi, Map<String, String> settings,
            boolean isVisible, String[] tags, String[] resources) {
        editVersionLayout.setVersion(version, descriptor, isVisible, settings, tags, resources);
        manageVersionLayout.setVersion(version, descriptor, doi);
    }

    private void titleInfoApp(){
        infoAppText = WidgetUtil.getLabel("<font size=\"3\"><b> This is a table containing " +
                "all the applications that are publicly available on VIP. You can see in the last " +
                "colum which group(s) give access to each application. Once registered and logged " +
                "onto VIP, you can configure the groups you belong to by accessing your account page. " +
                "Please refresh the VIP page after updating your groups to apply the change.</b></font>", 20);
    }
}
