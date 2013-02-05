/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.guvnor.configresource.client.widget;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.kie.guvnor.commons.ui.client.popup.FormPopup;
import org.kie.guvnor.commons.ui.client.popup.PopupSetFieldCommand;
import org.kie.guvnor.services.config.model.imports.Import;
import org.kie.guvnor.services.config.model.imports.Imports;

import static org.kie.commons.validation.PortablePreconditions.checkNotNull;

public class ImportsWidgetPresenter
        implements ImportsWidgetView.Presenter, IsWidget {

    private Imports imports;
    private ImportsWidgetView view;
    private final FormPopup addImportPopup;

    @Inject
    public ImportsWidgetPresenter(ImportsWidgetView view,
                                  FormPopup addImportPopup) {
        this.view = view;
        this.addImportPopup = addImportPopup;
        view.setPresenter(this);
    }

    public void setReadOnly() {
        view.setupReadOnly();
    }

    public void setImports(Imports imports) {
        checkNotNull("imports", imports);
        checkNotNull("imports", imports.getImports());

        this.imports = imports;

        for (Import item : imports.getImports()) {
            view.addImport(item.getType());
        }
    }

    @Override
    public void onAddImport() {
        addImportPopup.show(new PopupSetFieldCommand() {
            @Override
            public void setName(String name) {
                imports.getImports().add(new Import(name));
                view.addImport(name);
            }
        });
    }

    @Override
    public void onRemoveImport() {
        String selected = view.getSelected();
        if (selected == null) {
            view.showPleaseSelectAnImport();
        } else {
            imports.removeImport(selected);
            view.removeImport(selected);
        }
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public boolean isDirty() {
        return false; // TODO: -Rikkola-
    }
}