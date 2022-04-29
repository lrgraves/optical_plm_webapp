package com.eleoptics.spark.views.main;

import com.eleoptics.spark.api.OpticsApi;
import com.eleoptics.spark.config.SecurityUtils;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Map;

@Route(value = "resetPassword/", layout = MainView.class)
@RouteAlias(value = "resetPassword", layout = MainView.class)
@PageTitle("")
@Slf4j
public class PasswordReset extends VerticalLayout {
    @Autowired
    OpticsApi opticsApi;

    PasswordReset(){
        Span resetPage = new Span("An email has been sent to your account with a password reset link." +
                " Please wait a few minutes and check your spam folder if you do not see the link.");

        add(resetPage);


    }

    @PostConstruct
    private void resetAPICall(){
        String jwtToken = SecurityUtils.getJWT();
        Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
        String userEmail = jwtClaims.get("email").toString();
        opticsApi.resetPassword(userEmail);
    }
}
