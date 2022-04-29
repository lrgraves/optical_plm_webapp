package com.eleoptics.spark.views.about;

import com.eleoptics.spark.views.main.MainView;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "about", layout = MainView.class)
@PageTitle("About")
@CssImport("./styles/style.css")
public class AboutView extends Div {

    public AboutView() {
        setId("about-view");
        add(new Label("Content placeholder"));
    }

}
