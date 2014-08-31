/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.palominolabs.http.server;

import com.google.inject.Provider;
import org.eclipse.jetty.server.handler.ContextHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.ServletContextListener;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Config info for {@link HttpServerWrapper}.
 */
@NotThreadSafe
public final class HttpServerWrapperConfig {

    private final List<HttpServerConnectorConfig> connectorConfigs = newArrayList();

    private int maxFormContentSize = -1;

    @Nullable
    private String accessLogConfigFileInClasspath = "/" + this.getClass().getPackage()
        .getName().replace('.', '/') + "/pl-default-logback-access.xml";
    @Nullable
    private String accessLogConfigFileInFilesystem = null;

    private boolean logbackAccessQuiet = true;

    private final List<HttpResourceHandlerConfig> httpResourceHandlerConfigs = newArrayList();

    private final List<ListenerRegistration> servletContextListeners = newArrayList();

    @Nullable
    public String getAccessLogConfigFileInClasspath() {
        return accessLogConfigFileInClasspath;
    }

    /**
     * This is checked after the value set in {@link HttpServerWrapperConfig#setAccessLogConfigFileInFilesystem(String)}.
     * The default value points to a bundled config file that prints combined access log to the console's stdout.
     *
     * Setting this nulls the accessLogConfigFileInFilesystem.
     *
     * @param accessLogConfigFileInClasspath Classpath path to logback-access config file.
     */
    public void setAccessLogConfigFileInClasspath(@Nullable String accessLogConfigFileInClasspath) {
        this.accessLogConfigFileInFilesystem = null;
        this.accessLogConfigFileInClasspath = accessLogConfigFileInClasspath;
    }

    @Nullable
    public String getAccessLogConfigFileInFilesystem() {
        return accessLogConfigFileInFilesystem;
    }

    /**
     * If this is not set, the value set in {@link HttpServerWrapperConfig#setAccessLogConfigFileInClasspath(String)} is
     * used.
     *
     * Setting this nulls the accessLogConfigFileInClasspath.
     *
     * @param accessLogConfigFileInFilesystem Filesystem path to logback-access config file.
     */
    public void setAccessLogConfigFileInFilesystem(@Nullable String accessLogConfigFileInFilesystem) {
        this.accessLogConfigFileInClasspath = null;
        this.accessLogConfigFileInFilesystem = accessLogConfigFileInFilesystem;
    }

    public int getMaxFormContentSize() {
        return maxFormContentSize;
    }

    /**
     * Default is -1 (which means that it will be ignored and the server default, currently 200k, will be used)
     *
     * @param maxFormContentSize content size in bytes, -1 to use Jetty default
     * @see ContextHandler#setMaxFormContentSize(int)
     */
    public void setMaxFormContentSize(int maxFormContentSize) {
        this.maxFormContentSize = maxFormContentSize;
    }

    /**
     * Add config for a connector.
     *
     * @param connectorConfig a connector config
     */
    public void addHttpServerConnectorConfig(@Nonnull HttpServerConnectorConfig connectorConfig) {
        connectorConfigs.add(checkNotNull(connectorConfig));
    }

    /**
     * Add a HttpResourceHandlerConfig for serving static resources.
     *
     * @param httpResourceHandlerConfig a resource handler config
     */
    public void addResourceHandlerConfig(@Nonnull HttpResourceHandlerConfig httpResourceHandlerConfig) {
        httpResourceHandlerConfigs.add(httpResourceHandlerConfig);
    }

    /**
     * @param listener context listener
     */
    public void addServletContextListener(ServletContextListener listener) {
        servletContextListeners.add(ListenerRegistration.forListener(listener));
    }

    /**
     * If you want Guice to construct your ServletContextListener instance, consider using {@link BinderProviderCapture}
     * to make it easier to get a Provider for your ServletContextListener implementation. This will allow Guice to
     * check at module initialization time that all needed bindings are available, leading to earlier errors (if any are
     * imminent).
     *
     * @param listenerProvider context listener provider
     */
    public void addServletContextListenerProvider(Provider<? extends ServletContextListener> listenerProvider) {
        servletContextListeners.add(ListenerRegistration.forListenerProvider(listenerProvider));
    }

    @Nonnull
    public List<HttpServerConnectorConfig> getHttpServerConnectorConfigs() {
        return connectorConfigs;
    }

    public boolean isLogbackAccessQuiet() {
        return logbackAccessQuiet;
    }

    /**
     * Sets the Logback Access request log handler's quiet flag.
     *
     * @param logbackAccessQuiet true to muffle Logback status messages on startup, false to allow them.
     */
    public void setLogbackAccessQuiet(boolean logbackAccessQuiet) {
        this.logbackAccessQuiet = logbackAccessQuiet;
    }

    /**
     * @param config connector config
     * @return this
     * @see HttpServerWrapperConfig#addHttpServerConnectorConfig(HttpServerConnectorConfig)
     */
    @Nonnull
    public HttpServerWrapperConfig withHttpServerConnectorConfig(@Nonnull HttpServerConnectorConfig config) {
        addHttpServerConnectorConfig(config);
        return this;
    }

    /**
     * @param accessLogConfigFileInClasspath classpath path to access log config file
     * @return this
     * @see HttpServerWrapperConfig#setAccessLogConfigFileInClasspath(String)
     */
    @Nonnull
    public HttpServerWrapperConfig withAccessLogConfigFileInClasspath(@Nullable String accessLogConfigFileInClasspath) {
        setAccessLogConfigFileInClasspath(accessLogConfigFileInClasspath);
        return this;
    }

    /**
     * @param accessLogConfigFileInFilesystem filesystem path to access log config file
     * @return this
     * @see HttpServerWrapperConfig#setAccessLogConfigFileInFilesystem(String)
     */
    @Nonnull
    public HttpServerWrapperConfig withAccessLogConfigFileInFilesystem(
        @Nullable String accessLogConfigFileInFilesystem) {
        setAccessLogConfigFileInFilesystem(accessLogConfigFileInFilesystem);
        return this;
    }

    /**
     * @param maxFormContentSize max form content size
     * @return this
     * @see HttpServerWrapperConfig#setMaxFormContentSize(int)
     */
    @Nonnull
    public HttpServerWrapperConfig withMaxFormContentSize(int maxFormContentSize) {
        setMaxFormContentSize(maxFormContentSize);
        return this;
    }

    /**
     * @param logbackAccessQuiet logback access 'quiet' mode setting
     * @return this
     * @see HttpServerWrapperConfig#setLogbackAccessQuiet(boolean)
     */
    @Nonnull
    public HttpServerWrapperConfig withLogbackAccessQuiet(boolean logbackAccessQuiet) {
        setLogbackAccessQuiet(logbackAccessQuiet);
        return this;
    }

    /**
     * @param httpResourceHandlerConfig resource handler config
     * @return this
     * @see HttpServerWrapperConfig#addResourceHandlerConfig(HttpResourceHandlerConfig)
     */
    public HttpServerWrapperConfig withResourceHandlerConfig(
        @Nonnull HttpResourceHandlerConfig httpResourceHandlerConfig) {
        addResourceHandlerConfig(httpResourceHandlerConfig);
        return this;
    }

    @Nonnull
    public List<HttpResourceHandlerConfig> getHttpResourceHandlerConfigs() {
        return httpResourceHandlerConfigs;
    }

    @Nonnull
    List<ListenerRegistration> getServletContextListeners() {
        return servletContextListeners;
    }
}
