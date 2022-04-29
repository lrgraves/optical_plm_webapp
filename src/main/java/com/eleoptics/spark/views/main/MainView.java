package com.eleoptics.spark.views.main;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.eleoptics.spark.api.Asset;
import com.eleoptics.spark.config.SecurityUtils;
import com.eleoptics.spark.eventbus.EventBusFactory;
import com.eleoptics.spark.events.*;
import com.eleoptics.spark.views.project.*;
import com.eleoptics.spark.api.OpticsApi;
import com.google.common.eventbus.Subscribe;
import com.vaadin.componentfactory.Breadcrumbs;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.material.Material;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import javax.annotation.PostConstruct;


/**
 * The main view is a top-level placeholder for other views.
 */
@PreserveOnRefresh
@JsModule("./styles/shared-styles.js")
@StyleSheet("context://fonts/stylesheet.css")
@CssImport(value = "./styles/my-app-layout.css", themeFor = "vaadin-app-layout")
@PWA(name = "My Project", shortName = "My Project", enableInstallPrompt = false)
@Slf4j
@Theme(value = Material.class)
public class MainView extends AppLayout  {

    private Tabs menu;
    private H4 viewTitle;
    private Breadcrumbs breadcrumbs = new Breadcrumbs();
    private VerticalLayout menuLayout = new VerticalLayout();

    DesignPathEventSubscriber designPathEventSubscriber = new DesignPathEventSubscriber();


    Authentication authentication =
            SecurityContextHolder
                    .getContext()
                    .getAuthentication();

    OAuth2AuthenticationToken oauthToken =
            (OAuth2AuthenticationToken) authentication;


    String jwtToken = SecurityUtils.getJWT();

    Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
    String workspaceName = jwtClaims.get("http://eleoptics.com/workspace-name").toString();

    public MainView() {
        setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());
        menu = createMenu();
        createDrawerContent(menu);
        addToDrawer(menuLayout);


       // EventBusFactory.getEventBus().register(designPathEventSubscriber);


    }



    private Component createHeaderContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setClassName("navigation-topbar");
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.add(new DrawerToggle());
        viewTitle = new H4();
        String name = oauthToken.getPrincipal().getAttribute("name");
        String avatar = oauthToken.getPrincipal().getAttribute("picture");
        String jwtToken = SecurityUtils.getJWT();
        Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
        String workspaceName = jwtClaims.get("http://eleoptics.com/workspace-name").toString();

        log.info("token {}", jwtToken);
        log.info("claims {}", jwtClaims.get("http://eleoptics.com/asset-server-uri").toString());

        // add menu bar

        MenuBar menuBar = new MenuBar();

        menuBar.setClassName("menu-bar");

        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);

        Text selected = new Text("");

        MenuItem help = menuBar.addItem(new Icon(VaadinIcon.QUESTION_CIRCLE));
        //MenuItem notifications = menuBar.addItem(new Icon(VaadinIcon.BELL));
        MenuItem profile = menuBar.addItem(new Icon(VaadinIcon.USER));

        // Adding some horizontal spacing for each icon:
        menuBar.getItems().forEach(
                item -> item.getChildren().findFirst().ifPresent(icon -> icon
                        .getElement().getStyle().set("margin", "0 6px")));

        // submenu for the right side of notifications etc
        Anchor communityLink = new Anchor("https://community.eleoptics.com/c/Support/9", "Community");
        communityLink.setTarget("_blank");

        Anchor contactLink = new Anchor("mailto:support@eleoptics.com", "Contact Us");
        communityLink.setTarget("_blank");

        Anchor documentationLink = new Anchor("https://community.eleoptics.com/c/Support/9", "Documentation");
        documentationLink.setTarget("_blank");

        Anchor logout = new Anchor("/logout", "Logout");
        Anchor passwordReset = new Anchor("/resetPassword/", "Reset Password");


        //logout.setTarget("_blank");

        help.getSubMenu().addItem(documentationLink);
        help.getSubMenu().addItem(communityLink);
        help.getSubMenu().addItem(contactLink);

        /*
        profile.getSubMenu().addItem("Edit Profile",
                e -> selected.setText("Edit Profile"));
        profile.getSubMenu().addItem("Privacy Settings",
                e -> selected.setText("Privacy Settings"));
        profile.getSubMenu().addItem("Terms of Service",
                e -> selected.setText("Terms of Service"));

         */
        profile.getSubMenu().addItem(logout);
        profile.getSubMenu().addItem(passwordReset);

        /*
        notifications.getSubMenu().addItem("Notifications",
                e -> selected.setText("Notifications"));
        notifications.getSubMenu().addItem("Mark as Read",
                e -> selected.setText("Mark as Read"));
                */
        layout.add(menuBar);

        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        //log.info("Name: {} Avatar: {}", name, avatar);

        return layout;
    }

    private VerticalLayout createDrawerContent(Tabs menu) {
        VerticalLayout layout = new VerticalLayout();
        menuLayout.setClassName("navigation-side-menu");
        menuLayout.setSizeFull();
        menuLayout.setPadding(false);
        menuLayout.setSpacing(false);
        menuLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        VerticalLayout emptyDiv = new VerticalLayout();

        Span productName = new Span("SPARK by ELE Optics");
        productName.setClassName("header-spark");
        emptyDiv.add(productName);
        emptyDiv.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        emptyDiv.setAlignItems(FlexComponent.Alignment.CENTER);
        emptyDiv.setClassName("nav-menu-empty-div");
        menuLayout.add(emptyDiv, menu);
        return layout;
    }

    private VerticalLayout updateDrawerContent(Tabs menu) {
        menuLayout.removeAll();
        VerticalLayout layout = new VerticalLayout();
        menuLayout.setClassName("navigation-side-menu");
        menuLayout.setSizeFull();
        menuLayout.setPadding(false);
        menuLayout.setSpacing(false);
        menuLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        VerticalLayout emptyDiv = new VerticalLayout();

        Span productName = new Span("SPARK by ELE Optics");
        productName.setClassName("header-spark");
        emptyDiv.add(productName);
        emptyDiv.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        emptyDiv.setAlignItems(FlexComponent.Alignment.CENTER);
        emptyDiv.setClassName("nav-menu-empty-div");
        menuLayout.add(emptyDiv, menu);
        return layout;
    }

    private Tabs createMenu() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
        Tab[] arrayTabs = (Tab[]) createMenuItems();
        arrayTabs[1].setEnabled(true);
        arrayTabs[0].setEnabled(false);
        tabs.add(arrayTabs);
        return tabs;
    }

    private Component[] createMenuItems() {
        return new Tab[]{
                // Create accordian for workspaces
                createTab("Workspaces", ProjectsView.class),
                createTab(workspaceName + " Workspace", ProjectsView.class),
                //createTab("Deleted", AboutView.class),

        };
    }

    private Tabs allProjectsPathMenu() {
        menu.removeAll();

        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);

        //Add component tabs
        Tab workspaceHomeTab = new Tab(new RouterLink("Workspaces", ProjectsView.class));
        ComponentUtil.setData(workspaceHomeTab, Class.class, ProjectsView.class);
        Tab primaryWorkspaceHomeTab = new Tab(new RouterLink(workspaceName + " Workspace", ProjectsView.class));
        ComponentUtil.setData(workspaceHomeTab, Class.class, ProjectsView.class);

        primaryWorkspaceHomeTab.setEnabled(false);
        workspaceHomeTab.setEnabled(false);


        tabs.add(workspaceHomeTab, primaryWorkspaceHomeTab);

        return tabs;

    }

    private Tabs designPathMenu(Asset parentProject) {
        menu.removeAll();

        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);

        //Add component tabs
        Tab workspaceHomeTab = new Tab(new RouterLink(workspaceName + " Workspace", ProjectsView.class));
        ComponentUtil.setData(workspaceHomeTab, Class.class, ProjectsView.class);

        Tab requirementsTab = new Tab("Requirements");
        requirementsTab.getStyle().set("font-family", "Humnst777 BT");
        requirementsTab.getStyle().set("font-size", "14px");

        Tab commitsTab = new Tab("Project History");
        commitsTab.getStyle().set("font-family", "Humnst777 BT");
        commitsTab.getStyle().set("font-size", "14px");

        commitsTab.setEnabled(false);

        requirementsTab.setEnabled(false);

        tabs.add(workspaceHomeTab, commitsTab, requirementsTab);

        return tabs;

    }

    private Tabs designPathDetailsMenu(Asset parentProject) {
        menu.removeAll();

        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);

        //Add component tabs
        Tab workspaceHomeTab = new Tab(new RouterLink(workspaceName + " Workspace", ProjectsView.class));
        ComponentUtil.setData(workspaceHomeTab, Class.class, ProjectsView.class);
        // Add new design path component
        Tab designPathsTab = new Tab(new RouterLink("Design Paths", DesignPathGrid.class, parentProject.key));
        String key = NavigationKeyStrings.selectedProjectAssets;
        log.info("The main page ui" + UI.getCurrent());
        //ComponentUtil.setData(designPathsTab, key, assetList);

        Tab requirementsTab = new Tab("Requirements");
        requirementsTab.getStyle().set("font-family", "Humnst777 BT");
        requirementsTab.getStyle().set("font-size", "14px");
        requirementsTab.setEnabled(false);

        Tab commitsTab = new Tab("Design Path History");
        commitsTab.getStyle().set("font-family", "Humnst777 BT");
        commitsTab.getStyle().set("font-size", "14px");

        commitsTab.setEnabled(false);

        tabs.add(workspaceHomeTab, designPathsTab, requirementsTab, commitsTab);

        return tabs;

    }

    private static Tab createTab(String text, Class<? extends Component> navigationTarget) {
        final Tab tab = new Tab();
        tab.add(new RouterLink(text, navigationTarget));
        ComponentUtil.setData(tab, Class.class, navigationTarget);
        return tab;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();

        viewTitle.setText(getCurrentPageTitle());
    }

    public class DesignPathEventSubscriber {

        @Subscribe
        public void designPathEventHandler(DesignPathDetailsEvent event) {

            log.info("design paths handler called");
            if (!UI.getCurrent().isClosing() || getUI().isPresent()) {
                getUI().ifPresent(ui -> ui.access(() -> {

                    setPrimarySection(Section.DRAWER);
                    menu = designPathDetailsMenu(event.parentProject);
                    menuLayout.removeAll();
                    updateDrawerContent(menu);
                    addToDrawer(menuLayout);
                }));
                //log.info("event happend" + eventsHandled + " times");
            }
        }

        @Subscribe
        public void projectCardEventHandler(DesignPathsEvent event) {
            getUI().ifPresent(ui -> ui.access(() -> {

                setPrimarySection(Section.DRAWER);

                menu = designPathMenu(event.parentProject);
                menuLayout.removeAll();
                updateDrawerContent(menu);
                addToDrawer(menuLayout);
            }));

        }

        @Subscribe
        public void allProjectsEventHandler(AllProjectsPageEvent event) {
            getUI().ifPresent(ui -> ui.access(() -> {

                setPrimarySection(Section.DRAWER);

                menu = allProjectsPathMenu();
                menuLayout.removeAll();
                updateDrawerContent(menu);
                addToDrawer(menuLayout);
            }));

        }
    }

    private Optional<Tab> getTabForComponent(Component component) {
        return menu.getChildren()
                .filter(tab -> ComponentUtil.getData(tab, Class.class)
                        .equals(component.getClass()))
                .findFirst().map(Tab.class::cast);
    }

    private String getCurrentPageTitle() {
        return getContent().getClass().getAnnotation(Route.class).value();
    }
}
