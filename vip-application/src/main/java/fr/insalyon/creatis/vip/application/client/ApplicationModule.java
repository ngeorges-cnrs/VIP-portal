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
package fr.insalyon.creatis.vip.application.client;

import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.events.CloseClickHandler;
import com.smartgwt.client.widgets.tab.events.TabCloseClickEvent;

import fr.insalyon.creatis.vip.application.client.bean.AppVersion;
import fr.insalyon.creatis.vip.application.client.bean.Application;
import fr.insalyon.creatis.vip.application.client.bean.Tag;
import fr.insalyon.creatis.vip.application.client.inter.CustomApplicationModule;
import fr.insalyon.creatis.vip.application.client.rpc.ApplicationService;
import fr.insalyon.creatis.vip.application.client.rpc.WorkflowService;
import fr.insalyon.creatis.vip.application.client.view.ApplicationHomeParser;
import fr.insalyon.creatis.vip.application.client.view.ApplicationSystemParser;
import fr.insalyon.creatis.vip.application.client.view.ApplicationTileGrid;
import fr.insalyon.creatis.vip.application.client.view.common.AbstractSimulationTab;
import fr.insalyon.creatis.vip.application.client.view.monitor.timeline.TimelineLayout;
import fr.insalyon.creatis.vip.application.client.view.system.applications.app.ManageApplicationsTab;
import fr.insalyon.creatis.vip.core.client.CoreModule;
import fr.insalyon.creatis.vip.core.client.Module;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.core.client.view.CoreConstants;
import fr.insalyon.creatis.vip.core.client.view.layout.CenterTabSet;
import fr.insalyon.creatis.vip.core.client.view.layout.Layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ApplicationModule extends Module {

    public static List<CustomApplicationModule> customModules;
    private Map<String, List<AppVersion>> userTagApps;

    public ApplicationModule() {
        customModules = new ArrayList<>();
        userTagApps = new HashMap<>();

        CoreModule.getHomePageActions().put(CoreConstants.HOME_ACTION_SHOW_APPLICATIONS, new Runnable() {
            @Override
            public void run() {
                Layout.getInstance().addTab(
                    ApplicationConstants.TAB_MANAGE_APPLICATION,
                    () -> new ManageApplicationsTab(true));
            }
        });
    }

    @Override
    public void load() {
        final AsyncCallback<Map<Application, List<AppVersion>>> callback = new AsyncCallback<>() {
            @Override
            public void onFailure(Throwable caught) {
                Layout.getInstance().setWarningMessage("Unable to load users groups:<br />" + caught.getMessage());
            }

            @Override
            public void onSuccess(Map<Application, List<AppVersion>> apps) {
                apps.forEach((app, versions) -> {
                    versions.forEach((version) -> {
                        String classTag = version.getTags().stream()
                            .filter(t -> t.getKey().equalsIgnoreCase("class"))
                            .findFirst()
                            .map(Tag::getValue)
                            .orElse("Others");

                        if (version.isVisible()) {
                            userTagApps.computeIfAbsent(classTag, k -> new ArrayList<>()).add(version);
                        }
                    });
                });
                render();
            }
        };
        ApplicationService.Util.getInstance().getApplications(callback);
    }

    private void render() {
        Layout.getInstance().removeTab(ApplicationConstants.TAB_MANAGE_APPLICATION);
        
        CoreModule.addGeneralApplicationParser(new ApplicationHomeParser());
        CoreModule.addSystemApplicationParser(new ApplicationSystemParser());
        CoreModule.addLayoutToHomeTab(TimelineLayout.getInstance());

        userTagApps.keySet().stream().sorted().forEach(name ->
                CoreModule.addApplicationsTileGrid(new ApplicationTileGrid(name, userTagApps.get(name))));

        // Simulation close tab
        CenterTabSet.getInstance().addCloseClickHandler(new CloseClickHandler() {
            @Override
            public void onCloseClick(TabCloseClickEvent event) {
                Tab tab = event.getTab();
                try {
                    ((AbstractSimulationTab) tab).destroy();
                } catch (ClassCastException ex) {
                    // do nothing
                }
            }
        });
    }

    @Override
    public void postLoading() { }

    @Override
    public void terminate(Set<Tab> removedTabs) {
        TimelineLayout.getInstance().terminate();
        for (Tab tab : removedTabs) {
            if (tab instanceof AbstractSimulationTab) {
                ((AbstractSimulationTab) tab).destroy();
            }
        }
    }

    @Override
    public void userRemoved(User user) {
        final AsyncCallback<Void> callback = new AsyncCallback<>() {
            @Override
            public void onFailure(Throwable caught) {
                Layout.getInstance().setWarningMessage("Unable to anonymize user data:<br />" + caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
            }
        };
        WorkflowService.Util.getInstance().updateUser(user.getFullName(), "User-" + Random.nextInt(100000), callback);
    }

    @Override
    public void userUpdated(User oldUser, User updatedUser) {
        if ( ! oldUser.getFullName().equals(updatedUser.getFullName())) {
            final AsyncCallback<Void> callback = new AsyncCallback<>() {
                @Override
                public void onFailure(Throwable caught) {
                    Layout.getInstance().setWarningMessage("Unable to anonymize user data:<br />" + caught.getMessage());
                }

                @Override
                public void onSuccess(Void result) {
                }
            };
            WorkflowService.Util.getInstance().updateUser(oldUser.getFullName(), updatedUser.getFullName(), callback);
        }
    }
}
