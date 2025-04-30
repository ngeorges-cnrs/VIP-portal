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
package fr.insalyon.creatis.vip.core.client.view.user.account;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.widgets.*;
import com.smartgwt.client.widgets.events.*;
import fr.insalyon.creatis.vip.core.client.rpc.*;
import fr.insalyon.creatis.vip.core.client.view.CoreConstants;
import fr.insalyon.creatis.vip.core.client.view.common.AbstractFormLayout;
import fr.insalyon.creatis.vip.core.client.view.layout.Layout;
import fr.insalyon.creatis.vip.core.client.view.util.*;

/**
 *
 * @author Rafael Ferreira da Silva
 */
public class ApikeyLayout extends AbstractFormLayout {

    private String apikeyValue;
    private Label apikeyText;
    private IButton deleteApikey;
    private IButton generateNewApikey;
    private IButton showApikey;

    public ApikeyLayout() {
        super("100%", "115");
        addTitle("API key", CoreConstants.ICON_PASSWORD);

        configure();
    }

    private void configure() {
        this.addMember(WidgetUtil.getLabel("<b>Current key</b>", 15));
        this.addMember(apikeyText = WidgetUtil.getLabel("", 15));
        apikeyText.setCanSelectText(true);

        showApikey = WidgetUtil.getIButton(
                "Show key",
                CoreConstants.ICON_INFO,
                new ShowApikeyClickHandler());
        showApikey.setWidth(150);
        showApikey.disable();
        addButtons(showApikey);

        deleteApikey = WidgetUtil.getIButton(
                "Delete key",
                CoreConstants.ICON_DELETE,
                new DeleteApikeyClickHandler());
        deleteApikey.setWidth(150);
        deleteApikey.disable();
        generateNewApikey = WidgetUtil.getIButton(
                "Generate new key",
                CoreConstants.ICON_EDIT,
                new GenerateNewKeyClickHandler());
        generateNewApikey.setWidth(150);
        generateNewApikey.disable();
        addButtons(deleteApikey, generateNewApikey);
        ConfigurationServiceAsync service = ConfigurationService.Util.getInstance();
        service.getUserApikey(null, new ApikeyReceivedCallback() );
    }

    private class ApikeyReceivedCallback implements AsyncCallback<String> {
        @Override
        public void onFailure(Throwable caught) {
            Layout.getInstance().setWarningMessage("Unable to get current API key:<br />" + caught.getMessage());
        }

        @Override
        public void onSuccess(String apikey) {
            generateNewApikey.enable();
            if (apikey == null) {
                apikeyValue = "";
                apikeyText.setContents("<i>None</i>");
            } else {
                apikeyValue = apikey;
                apikeyText.setContents("***");
                deleteApikey.enable();
                showApikey.enable();
            }
        }
    }

    private class DeleteApikeyClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent clickEvent) {
            deleteApikey.disable();
            showApikey.disable();
            generateNewApikey.disable();
            ConfigurationServiceAsync service = ConfigurationService.Util.getInstance();
            service.deleteUserApikey(null, new DeleteApikeyCallback() );
        }
    }

    private class DeleteApikeyCallback implements AsyncCallback<Void> {
        @Override
        public void onFailure(Throwable caught) {
            Layout.getInstance().setWarningMessage("Unable to delete new API key:<br />" + caught.getMessage());
            deleteApikey.enable();
            showApikey.enable();
            generateNewApikey.enable();
        }

        @Override
        public void onSuccess(Void result) {
            generateNewApikey.enable();
            apikeyText.setContents("<i>None</i>");
        }
    }

    private class ShowApikeyClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent clickEvent) {
            if (apikeyText.getContents() == apikeyValue) {
                apikeyText.setContents("***");
                showApikey.setTitle("Show key");
            } else {
                apikeyText.setContents(apikeyValue);
                showApikey.setTitle("Hide key");
            }
        }
    }

    private class GenerateNewKeyClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent clickEvent) {
            deleteApikey.disable();
            showApikey.disable();
            generateNewApikey.disable();
            ConfigurationServiceAsync service = ConfigurationService.Util.getInstance();
            service.generateNewUserApikey(null, new NewApikeyGeneratedCallback() );
        }
    }

    private class NewApikeyGeneratedCallback implements AsyncCallback<String> {
        @Override
        public void onFailure(Throwable caught) {
            Layout.getInstance().setWarningMessage("Unable to generate new API key:<br />" + caught.getMessage());
            generateNewApikey.enable();
        }

        @Override
        public void onSuccess(String apikey) {
            generateNewApikey.enable();
            deleteApikey.enable();
            showApikey.enable();
            apikeyValue = apikey;
            showApikey.setTitle("Hide key");
            apikeyText.setContents(apikey);
        }
    }
}
