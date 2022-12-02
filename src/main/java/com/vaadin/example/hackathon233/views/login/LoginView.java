package com.vaadin.example.hackathon233.views.login;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import com.vaadin.example.hackathon233.security.AuthenticatedUser;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
@PageTitle("Login")
@Route(value = "login")
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    private final AuthenticatedUser authenticatedUser;

    public LoginView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Hackathon-23-3");
        i18n.getHeader().setDescription("Login using user/user or admin/admin");
        i18n.setAdditionalInformation(null);
        setI18n(i18n);

        setForgotPasswordButtonVisible(false);
        setOpened(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Tracer trace = GlobalOpenTelemetry.getTracer("app-instrumentation",
                "1.0");
        final Span span = trace.spanBuilder("Already logged in check").startSpan();
        try {
            span.addEvent("Checking whether user is already authenticated.");
            if (authenticatedUser.get().isPresent()) {
                // Already logged in
                setOpened(false);
                event.forwardTo("");
                Attributes attributes = Attributes.builder().put("authentication.user", authenticatedUser.get().get().getUsername()).build();
                span.addEvent("Redirecting since user already logged in", attributes);
            } else {
                span.addEvent("No user logged in, proceeding with the login page.");
            }
        } finally {
            span.end();
        }


        setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }
}
