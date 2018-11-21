package dc.utils;

/**
 * Created by pt on 15-3-25.
 */

import java.util.Map;
import java.util.concurrent.*;

import com.google.common.primitives.Longs;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.appender.routing.Route;
import org.apache.logging.log4j.core.appender.routing.Routes;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.util.Booleans;

/**
 * This Appender "routes" between various Appenders, some of which can be references to
 * Appenders defined earlier in the configuration while others can be dynamically created
 * within this Appender as required. Routing is achieved by specifying a pattern on
 * the Routing appender declaration. The pattern should contain one or more substitution patterns of
 * the form "$${[key:]token}". The pattern will be resolved each time the Appender is called using
 * the built in StrSubstitutor and the StrLookup plugin that matches the specified key.
 */
@Plugin(name = "AutoCleanRouting", category = "Core", elementType = "appender", printObject = true)
public final class AutoCleanRoutingAppender extends AbstractAppender {
    private static final String DEFAULT_KEY = "ROUTING_APPENDER_DEFAULT";
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Routes routes;
    private final Route defaultRoute;
    private final Configuration config;
    private final ConcurrentMap<String, AppenderControl> appenders =
            new ConcurrentHashMap<String, AppenderControl>();
    private final RewritePolicy rewritePolicy;
    private final Long autoCleanDelaySecs;
    private final int safelyDelayOffset = 5;

    private AutoCleanRoutingAppender(final String name, final Filter filter, final boolean ignoreExceptions,
                                     final Long autoCleanDelaySecs, final Routes routes,
                                     final RewritePolicy rewritePolicy, final Configuration config) {
        super(name, filter, null, ignoreExceptions);
        this.routes = routes;
        this.config = config;
        this.rewritePolicy = rewritePolicy;
        this.autoCleanDelaySecs = autoCleanDelaySecs + safelyDelayOffset;
        Route defRoute = null;
        for (final Route route : routes.getRoutes()) {
            if (route.getKey() == null) {
                if (defRoute == null) {
                    defRoute = route;
                } else {
                    error("Multiple default routes. Route " + route.toString() + " will be ignored");
                }
            }
        }
        defaultRoute = defRoute;
    }

    @Override
    public void start() {
        // Register all the static routes.
        for (final Route route : routes.getRoutes()) {
            if (route.getAppenderRef() != null) {
                final Appender appender = config.getAppender(route.getAppenderRef());
                if (appender != null) {
                    final String key = route == defaultRoute ? DEFAULT_KEY : route.getKey();
                    appenders.put(key, new AppenderControl(appender, null, null));
                } else {
                    LOGGER.error("Appender " + route.getAppenderRef() + " cannot be located. Route ignored");
                }
            }
        }
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        final Map<String, Appender> map = config.getAppenders();
        for (final Map.Entry<String, AppenderControl> entry : appenders.entrySet()) {
            final String name = entry.getValue().getAppender().getName();
            if (!map.containsKey(name)) {
                entry.getValue().getAppender().stop();
            }
        }
        this.scheduler.shutdown();
    }

    @Override
    public void append(LogEvent event) {
        if (rewritePolicy != null) {
            event = rewritePolicy.rewrite(event);
        }
        final String key = config.getStrSubstitutor().replace(event, routes.getPattern());
        final AppenderControl control = getControl(key, event);
        if (control != null) {
            control.callAppender(event);
        }
    }

    private synchronized AppenderControl getControl(final String key, final LogEvent event) {
        AppenderControl control = appenders.get(key);
        if (control != null) {
            return control;
        }
        Route route = null;
        for (final Route r : routes.getRoutes()) {
            if (r.getAppenderRef() == null && key.equals(r.getKey())) {
                route = r;
                break;
            }
        }
        if (route == null) {
            route = defaultRoute;
            control = appenders.get(DEFAULT_KEY);
            if (control != null) {
                return control;
            }
        }
        if (route != null) {
            final Appender app = createAppender(route, event);
            if (app == null) {
                return null;
            }
            control = new AppenderControl(app, null, null);
            appenders.put(key, control);
            autoClean(key);
        }

        return control;
    }

    private void autoClean(final String key) {
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                stopAppender(key);
            }
        }, autoCleanDelaySecs, TimeUnit.SECONDS);
    }

    private synchronized void stopAppender(String key) {
        AppenderControl control = appenders.remove(key);
        if (control != null) {
            control.stop();
            final Map<String, Appender> map = config.getAppenders();
            if (!map.containsKey(key))
                control.getAppender().stop();
        }
    }

    private Appender createAppender(final Route route, final LogEvent event) {
        final Node routeNode = route.getNode();
        for (final Node node : routeNode.getChildren()) {
            if (node.getType().getElementName().equals("appender")) {
                final Node appNode = new Node(node);
                config.createConfiguration(appNode, event);
                if (appNode.getObject() instanceof Appender) {
                    final Appender app = (Appender) appNode.getObject();
                    app.start();
                    return app;
                }
                LOGGER.error("Unable to create Appender of type " + node.getName());
                return null;
            }
        }
        LOGGER.error("No Appender was configured for route " + route.getKey());
        return null;
    }

    /**
     * Create a RoutingAppender.
     *
     * @param name          The name of the Appender.
     * @param ignore        If {@code "true"} (default) exceptions encountered when appending events are logged; otherwise
     *                      they are propagated to the caller.
     * @param routes        The routing definitions.
     * @param config        The Configuration (automatically added by the Configuration).
     * @param rewritePolicy A RewritePolicy, if any.
     * @param filter        A Filter to restrict events processed by the Appender or null.
     * @return The RoutingAppender
     */
    @PluginFactory
    public static AutoCleanRoutingAppender createAppender(
            @PluginAttribute("name") final String name,
            @PluginAttribute("ignoreExceptions") final String ignore,
            @PluginAttribute("autoCleanDelaySecs") final String autoCleanDelay,
            @PluginElement("Routes") final Routes routes,
            @PluginConfiguration final Configuration config,
            @PluginElement("RewritePolicy") final RewritePolicy rewritePolicy,
            @PluginElement("Filter") final Filter filter) {

        final boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);
        if (name == null) {
            LOGGER.error("No name provided for RoutingAppender");
            return null;
        }
        if (routes == null) {
            LOGGER.error("No routes defined for RoutingAppender");
            return null;
        }
        final Long autoCleanDelaySecs = Longs.tryParse(autoCleanDelay);
        if(autoCleanDelaySecs == null){
            LOGGER.error("No autoCleanDelaySecs defined for RoutingAppender");
            return null;
        }
        return new AutoCleanRoutingAppender(name, filter, ignoreExceptions, autoCleanDelaySecs, routes, rewritePolicy, config);
    }
}
