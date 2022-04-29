package com.eleoptics.spark.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final LogoutHandler logoutHandler;

    public SecurityConfig(LogoutHandler logoutHandler) {
        this.logoutHandler = logoutHandler;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests()
                // allow all users to access the home pages and the static images directory
                .mvcMatchers("/images/**").permitAll()
                .antMatchers("/vaadinServlet/UIDL/**").permitAll()
                .antMatchers("/vaadinServlet/HEARTBEAT/**").permitAll()
                .mvcMatchers("/projectReport**").permitAll()
                .mvcMatchers("/icons/**").permitAll()
                .mvcMatchers("/fonts/**").permitAll()
                .mvcMatchers("/offline/**").permitAll()


                //allow framework requests
                .requestMatchers(SecurityUtils::isFrameworkInternalRequest).permitAll()
                // all other requests must be authenticated
                .anyRequest().authenticated()
                .and().oauth2Login()
                .and().logout()
                // handle logout requests at /logout path
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                // customize logout handler to log out of Auth0
                .addLogoutHandler(logoutHandler);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
                // Vaadin Flow static resources //
                "/VAADIN/**",
                // the standard favicon URI
                "/favicon.ico",
                // the robots exclusion standard
                "/robots.txt",
                // web application manifest //
                "/manifest.webmanifest",
                "/sw.js",
                "/offline-page.html",
                // (development mode) static resources //
                "/frontend/**",
                // (development mode) webjars //
                "/webjars/**",
                // (production mode) static resources //
                "/frontend-es5/**", "/frontend-es6/**");
    }

}
