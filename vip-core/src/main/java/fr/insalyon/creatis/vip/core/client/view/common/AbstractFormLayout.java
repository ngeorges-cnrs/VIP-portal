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
package fr.insalyon.creatis.vip.core.client.view.common;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import fr.insalyon.creatis.vip.core.client.view.util.FieldUtil;
import fr.insalyon.creatis.vip.core.client.view.util.WidgetUtil;

public abstract class AbstractFormLayout extends VLayout {

    public AbstractFormLayout(int width, int height) {
        this(Integer.toString(width), Integer.toString(height));
    }

    public AbstractFormLayout(int width, String height) {
        this(Integer.toString(width), height);
    }
    
    public AbstractFormLayout() {
        this.setBorder("1px solid #C0C0C0");
        this.setBackgroundColor("#F5F5F5");
        this.setPadding(10);
        this.setMembersMargin(5);
    }

    public AbstractFormLayout(String width, String height) {
        this.setWidth(width);
        this.setHeight(height);
        this.setBorder("1px solid #C0C0C0");
        this.setBackgroundColor("#F5F5F5");
        this.setPadding(10);
        this.setMembersMargin(5);
    }

    /**
     * Adds a title to the form.
     */
    protected void addTitle(String title, String icon) {

        Label label = WidgetUtil.getLabel("<b>" + title + "</b>",
                icon, 15);
        label.setWidth100();
        label.setAlign(Alignment.LEFT);
        this.addMember(label);
    }

    /**
     * Adds a field to the form.
     */
    public void addField(String title, FormItem item) {
        this.addMember(WidgetUtil.getLabel("<b>" + title + "</b>", 15));
        this.addMember(FieldUtil.getForm(item));
    }
    
    public void addField100(String title, FormItem item) {

        this.addMember(WidgetUtil.getLabel("<b>" + title + "</b>", 15));
        this.addMember(FieldUtil.getFormOneColumn(item));
    }
     
    public void addFieldResponsiveHeight(String title, FormItem item) {

        this.addMember(WidgetUtil.getLabel("<b>" + title + "</b>", 15));
        this.addMember(FieldUtil.getFormOneColumnResponsiveHeight(item));
    }

    public void addInline(Canvas... canvas) {
        HLayout hLayout = new HLayout(5);

        for (Canvas cv : canvas) {
            hLayout.addMember(cv);
        }
        addMember(hLayout);
    }

    /**
     * Adds a set of buttons displayed in line.
     */
    protected void addButtons(IButton... buttons) {

        HLayout hLayout = new HLayout(5);
        hLayout.setWidth100();
        
        for (IButton button : buttons) {
            hLayout.addMember(button);
        }
        this.addMember(hLayout);
    }
}
