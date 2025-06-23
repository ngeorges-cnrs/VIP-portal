package fr.insalyon.creatis.vip.application.client.view.system.tags;

import java.util.Arrays;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.MultipleAppearance;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.fields.BooleanItem;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import fr.insalyon.creatis.vip.application.client.ApplicationConstants;
import fr.insalyon.creatis.vip.application.client.bean.Tag;
import fr.insalyon.creatis.vip.application.client.rpc.ApplicationService;
import fr.insalyon.creatis.vip.core.client.view.CoreConstants;
import fr.insalyon.creatis.vip.core.client.view.common.AbstractFormLayout;
import fr.insalyon.creatis.vip.core.client.view.layout.Layout;
import fr.insalyon.creatis.vip.core.client.view.util.FieldUtil;
import fr.insalyon.creatis.vip.core.client.view.util.WidgetUtil;

public class EditTagLayout extends AbstractFormLayout {
    private Tag actualTag;

    private TextItem keyField;
    private TextItem valueField;
    private SelectItem typeField;
    private TextItem applicationField;
    private TextItem versionField;
    private CheckboxItem visibleField;
    private CheckboxItem boutiquesField;

    private IButton saveButton;
    private IButton removeButton;

    public EditTagLayout() {
        super(480, 200);
        addTitle("Edit tag", ApplicationConstants.ICON_TAG);

        configure();
    }

    private void configure() {
        keyField = FieldUtil.getTextItem(350, null);
        valueField = FieldUtil.getTextItem(350, null);
        visibleField = new BooleanItem().setShowTitle(false).setWidth(350);
        boutiquesField = new BooleanItem().setShowTitle(false).setWidth(350);

        typeField = new SelectItem();
        typeField.setShowTitle(false);
        typeField.setMultipleAppearance(MultipleAppearance.PICKLIST);
        typeField.setValueMap(Arrays.stream(Tag.ValueType.values())
            .map(v -> v.toString())
            .toArray(String[]::new));
        typeField.setWidth(350);

        applicationField = FieldUtil.getTextItem(350, null);
        applicationField.setDisabled(true);
        
        versionField = FieldUtil.getTextItem(350, null);
        versionField.setDisabled(true);
        
        saveButton = WidgetUtil.getIButton("Save", CoreConstants.ICON_SAVED,
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (keyField.validate() && valueField.validate() && applicationField.validate() && versionField.validate()) {
                            save(tagFromFields());
                        }
                    }
                });

        removeButton = WidgetUtil.getIButton("Remove", CoreConstants.ICON_DELETE,
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        SC.ask("Do you really want to remove this tag?", new BooleanCallback() {
                            @Override
                            public void execute(Boolean value) {
                                if (value) {
                                    remove(tagFromFields());
                                }
                            }
                        });
                    }
                });
        removeButton.setDisabled(true);

        addField("Key", keyField);
        addField("Value", valueField);
        addField("Type", typeField);
        addField("Application", applicationField);
        addField("Version", versionField);
        addField("Visible", visibleField);
        addField("Boutiques", boutiquesField);
        addButtons(saveButton, removeButton);
    }

    public void setTag(Tag tag) {
        if (tag != null) {
            this.actualTag = tag;
            this.keyField.setValue(tag.getKey());
            this.valueField.setValue(tag.getValue());
            this.typeField.setValue(tag.getType().toString());
            this.applicationField.setValue(tag.getApplication());
            this.versionField.setValue(tag.getVersion());
            this.visibleField.setValue(tag.isVisible());
            this.boutiquesField.setValue(tag.isBoutiques());
            this.removeButton.setDisabled(false);
        }
    }

    private void save(Tag tag) {
        WidgetUtil.setLoadingIButton(saveButton, "Saving...");
        ApplicationService.Util.getInstance().updateTag(actualTag, tag, getCallback("update"));
    }

    private void remove(Tag tag) {
        WidgetUtil.setLoadingIButton(removeButton, "Removing...");
        ApplicationService.Util.getInstance().removeTag(tag, getCallback("remove"));
    }

    private AsyncCallback<Void> getCallback(final String text) {
        return new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                WidgetUtil.resetIButton(saveButton, "Save", CoreConstants.ICON_SAVED);
                WidgetUtil.resetIButton(removeButton, "Remove", CoreConstants.ICON_DELETE);
                Layout.getInstance().setWarningMessage("Unable to " + text + " tag:<br />" + caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                WidgetUtil.resetIButton(saveButton, "Save", CoreConstants.ICON_SAVED);
                WidgetUtil.resetIButton(removeButton, "Remove", CoreConstants.ICON_DELETE);
                setTag(null);
                ManageTagsTab tab = (ManageTagsTab) Layout.getInstance().
                        getTab(ApplicationConstants.TAB_MANAGE_TAG);
                tab.loadTags();
            }
        };
    }

    private Tag tagFromFields() {
        return new Tag(
            keyField.getValueAsString().trim(),
            valueField.getValueAsString().trim(),
            Tag.ValueType.valueOf(typeField.getValueAsString()),
            applicationField.getValueAsString(),
            versionField.getValueAsString().trim(),
            visibleField.getValueAsBoolean(),
            boutiquesField.getValueAsBoolean()
        );
    }
}
