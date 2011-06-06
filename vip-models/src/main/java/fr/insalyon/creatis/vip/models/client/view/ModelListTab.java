/* Copyright CNRS-CREATIS
 *
 * Rafael Silva
 * rafael.silva@creatis.insa-lyon.fr
 * http://www.creatis.insa-lyon.fr/~silva
 *
 * This software is a grid-enabled data-driven workflow manager and editor.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
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
 * knowledge of the CeCILL license and that you accept its terms.
 */
package fr.insalyon.creatis.vip.models.client.view;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.events.RowContextClickEvent;
import com.smartgwt.client.widgets.grid.events.RowContextClickHandler;
import com.smartgwt.client.widgets.grid.events.RowMouseDownEvent;
import com.smartgwt.client.widgets.grid.events.RowMouseDownHandler;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import fr.cnrs.i3s.neusemstore.vip.semantic.simulation.model.client.bean.SimulationObjectModelLight;
import fr.insalyon.creatis.vip.common.client.view.modal.ModalWindow;
import fr.insalyon.creatis.vip.core.client.view.layout.Layout;
import fr.insalyon.creatis.vip.models.client.rpc.ModelService;
import fr.insalyon.creatis.vip.models.client.rpc.ModelServiceAsync;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tristan Glatard
 */
public class ModelListTab extends Tab {

    protected ListGrid grid;
    protected ModalWindow modal;
    protected HandlerRegistration rowContextClickHandler;
    protected HandlerRegistration rowMouseDownHandler;

    public ModelListTab() {

        this.setTitle("List models");
        this.setID("model-browse-tab");
        this.setCanClose(true);

        configureGrid();
        modal = new ModalWindow(grid);

        VLayout layout = new VLayout();

        ToolStrip toolStrip = new ToolStrip();
        toolStrip.setWidth100();

        ToolStripButton refreshButton = new ToolStripButton();
        refreshButton.setIcon("icon-refresh.png");
        refreshButton.setTitle("Refresh");
        refreshButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                ModelListTab modelsTab = (ModelListTab) Layout.getInstance().getTab("model-browse-tab");
                modelsTab.loadModels();
            }
        });
        toolStrip.addButton(refreshButton);

        ToolStripButton addButton = new ToolStripButton();
        addButton.setIcon("icon-add.png");
        addButton.setTitle("Upload");
        addButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                Layout.getInstance().addTab(new ModelImportTab());
            }
        });
        toolStrip.addButton(addButton);

        ToolStripButton deleteButton = new ToolStripButton();
        deleteButton.setIcon("icon-clear.png");
        deleteButton.setTitle("Delete all");
        deleteButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                SC.confirm("Do you really want to delete all the models? zip files will not be removed.", new BooleanCallback() {

                    public void execute(Boolean value) {

                        if (value != null && value) {
                            AsyncCallback<Void> callback = new AsyncCallback<Void>() {

                                public void onFailure(Throwable caught) {
                                    SC.warn("Failed to delete all models");
                                }

                                public void onSuccess(Void result) {
                                    SC.say("All models were deleted");
                                    ModelListTab modelsTab = (ModelListTab) Layout.getInstance().getTab("model-browse-tab");
                                    modelsTab.loadModels();
                                }
                            };

                            ModelServiceAsync ms = ModelService.Util.getInstance();
                            ms.deleteAllModelsInTheTripleStore(callback);
                        }
                    }
                });
            }
        });
        toolStrip.addButton(deleteButton);

        loadModels();

        layout.addMember(toolStrip);
        layout.addMember(grid);

        //this will be triggered from the context menu
        //TODO: call model.storageURL when Germain implements it
        // String lfnModel = "/grid/biomed/creatis/vip/data/groups/VIP/Models/adam.zip";
        //  downloadModel(lfnModel) ;

        this.setPane(layout);

    }

    public void resetTab() {
        return;
    }

    private void configureGrid() {
        grid = new ListGrid();
        grid.setWidth100();
        grid.setHeight100();
        grid.setShowAllRecords(false);
        grid.setShowRowNumbers(true);
        grid.setShowEmptyMessage(true);
        // grid.setSelectionType(SelectionStyle.SIMPLE);
        grid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
        grid.setEmptyMessage("<br>No data available.");

        //    ListGridField statusIcoField = FieldUtil.getIconGridField("statusIco");
        ListGridField modelNameField = new ListGridField("name", "Name");
        ListGridField typeField = new ListGridField("types", "Type(s)");
        ListGridField longitudinalField = new ListGridField("longitudinal", "Longitudinal");
        ListGridField movementField = new ListGridField("movement", "Movement");
        ListGridField URIField = new ListGridField("uri", "URI");

        grid.setFields(modelNameField, typeField, longitudinalField, movementField, URIField);

        rowContextClickHandler = grid.addRowContextClickHandler(new RowContextClickHandler() {

            public void onRowContextClick(RowContextClickEvent event) {
                event.cancel();
                //call download model method below to download the model zip file.

                //SC.say("context click");
//                String simulationId = event.getRecord().getAttribute("simulationId");
//                String status = event.getRecord().getAttribute("status");
//                new SimulationsContextMenu(modal, simulationId, status).showContextMenu();
            }
        });
        rowMouseDownHandler = grid.addRowMouseDownHandler(new RowMouseDownHandler() {

            public void onRowMouseDown(RowMouseDownEvent event) {
                Layout.getInstance().addTab(new ModelDisplayTab(event.getRecord().getAttribute("uri")));
            }
        });
    }

    public void loadModels() {
        ModelServiceAsync ms = ModelService.Util.getInstance();
        final AsyncCallback<List<SimulationObjectModelLight>> callback = new AsyncCallback<List<SimulationObjectModelLight>>() {

            public void onFailure(Throwable caught) {
                SC.warn("Cannot list models");
                modal.hide();
            }

            public void onSuccess(List<SimulationObjectModelLight> result) {
                List<SimulationObjectModelLightRecord> dataList = new ArrayList<SimulationObjectModelLightRecord>();
                for (SimulationObjectModelLight s : result) {
                    String type = "";
                    boolean[] saxes = s.getSemanticAxes();
                    boolean init = false;
                    if (saxes[0]) {
                        type += "anatomical";
                        init = true;
                    }
                    if (saxes[1]) {
                        if (init) {
                            type += ", ";
                        }
                        init = true;
                        type += "pathological";
                    }
                    if (saxes[2]) {
                        if (init) {
                            type += ", ";
                        }
                        init = true;
                        type += "geometrical";
                    }
                    if (saxes[3]) {
                        if (init) {
                            type += ", ";
                        }
                        init = true;
                        type += "foreign object";
                    }
                    if (saxes[4]) {
                        if (init) {
                            type += ", ";
                        }
                        init = true;
                        type += "external agent";
                    }
                    dataList.add(new SimulationObjectModelLightRecord(s.getModelName(), type, "" + s.isLongitudinal(), "" + s.isMoving(), s.getURI()));
                }
                grid.setData(dataList.toArray(new SimulationObjectModelLightRecord[]{}));
                modal.hide();
            }
        };
        ms.listAllModels(callback);
        modal.show("Loading Models...", true);
    }
}