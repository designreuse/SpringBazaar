package com.springbazaar.web.ui;

import com.springbazaar.service.security.SecurityService;
import com.springbazaar.web.ui.tool.component.RegisterLink;
import com.vaadin.annotations.Theme;
import com.vaadin.event.ShortcutAction;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.beans.factory.annotation.Autowired;

@SpringUI(path = LoginUI.NAME)
@Theme("valo")
public class LoginUI extends UI {
    public static final String NAME = "/login";

    private final SecurityService securityService;

    private final TextField username = new TextField("Username:");
    private final PasswordField password = new PasswordField("Password:");
    private final CheckBox rememberMe = new CheckBox("Remember me");
    private final Button forgotPassword = new Button("Forgot password?");
    private final Button loginButton = new Button("Login", this::loginButtonClick);
    private final RegisterLink registerLink = new RegisterLink();

    @Autowired
    public LoginUI(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    protected void init(VaadinRequest request) {
        FormLayout loginForm = new FormLayout();
        loginForm.setMargin(true);
        loginForm.addStyleName("outlined");
        loginForm.setSizeUndefined();

        username.setWidth(100.0f, Unit.PERCENTAGE);
        username.setRequiredIndicatorVisible(true);
        loginForm.addComponent(username);

        password.setWidth(100.0f, Unit.PERCENTAGE);
        password.setRequiredIndicatorVisible(true);
        loginForm.addComponent(password);

        loginForm.addComponent(rememberMe);

        forgotPassword.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Notification.show("Hint: Try anything ;-)", Notification.Type.HUMANIZED_MESSAGE);
            }
        });
        forgotPassword.addStyleName(ValoTheme.BUTTON_LINK);
        forgotPassword.setHeight("25px");
        loginForm.addComponent(forgotPassword);

        loginButton.setIcon(VaadinIcons.KEY);
        loginButton.setDisableOnClick(true);
        loginButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        HorizontalLayout buttonsLayout = new HorizontalLayout(loginButton, registerLink);

        loginForm.addComponent(buttonsLayout);

        setFocusedComponent(username);
        VerticalLayout rootUi = new VerticalLayout(loginForm);
        rootUi.setSizeFull();
        rootUi.setComponentAlignment(loginForm, Alignment.MIDDLE_CENTER);

        setContent(rootUi);
    }

    private void loginButtonClick(Button.ClickEvent e) {
        if (securityService.login(username.getValue(), password.getValue())) {
            getPage().setLocation(WelcomeUI.NAME);
        } else e.getButton().setEnabled(true);
    }
}
