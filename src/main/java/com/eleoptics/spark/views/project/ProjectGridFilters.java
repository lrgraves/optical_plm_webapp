package com.eleoptics.spark.views.project;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectGridFilters extends Div {
    FormLayout formLayout = new FormLayout();

    TextField projectNameField = new TextField("Project Title");
    ComboBox<String> creatorField = new ComboBox<>("Project Creator");

    Registration registration = projectNameField
            .addKeyUpListener(e ->
                    log.info(String.join("",e.getKey().getKeys())));

    public ProjectGridFilters() {

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("25em", 1),
                new FormLayout.ResponsiveStep("32em", 2));
        this.add(formLayout);
        formLayout.add(projectNameField);
        formLayout.add(creatorField);
    }

}
