/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.palominolabs.http.server;

import ch.qos.logback.access.jetty.RequestLogImpl;
import com.google.common.base.Preconditions;
import com.google.inject.servlet.GuiceFilter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.DispatcherType;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.util.EnumSet;

/**
 * Runs an embedded jetty server. Sets up the guice servlet filter and request logging.
 */
@ThreadSafe
public class HttpServerWrapper {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerWrapper.class);

    private final HttpServerWrapperConfig httpServerWrapperConfig;
    private final GuiceFilter filter;
    private final Server server = new Server();

    HttpServerWrapper(HttpServerWrapperConfig httpServerWrapperConfig, GuiceFilter filter) {
        this.httpServerWrapperConfig = httpServerWrapperConfig;
        this.filter = filter;
    }

    public void start() throws Exception {

        // servlet handler will contain the UnhandledRequestServlet and the GuiceFilter
        ServletContextHandler servletHandler = new ServletContextHandler();
        servletHandler.setContextPath("/");

        servletHandler.setMaxFormContentSize(httpServerWrapperConfig.getMaxFormContentSize());

        servletHandler.addServlet(new ServletHolder(new UnhandledRequestServlet()), "/*");

        // add guice servlet filter
        FilterHolder filterHolder = new FilterHolder(filter);
        servletHandler.addFilter(filterHolder, "/*", EnumSet.allOf(DispatcherType.class));

        HandlerCollection handlerCollection = new HandlerCollection();
        handlerCollection.addHandler(servletHandler);

        // add logback-access request log
        RequestLogHandler logHandler = new RequestLogHandler();
        RequestLogImpl logbackRequestLog = new RequestLogImpl();
        if (httpServerWrapperConfig.getAccessLogConfigFileInFilesystem() != null) {
            logger.debug("Setting logback access config fs path to " +
                httpServerWrapperConfig.getAccessLogConfigFileInFilesystem());
            logbackRequestLog.setFileName(httpServerWrapperConfig.getAccessLogConfigFileInFilesystem());
        } else if (httpServerWrapperConfig.getAccessLogConfigFileInClasspath() != null) {
            logger.debug("Loading logback access config from classpath path " + httpServerWrapperConfig
                .getAccessLogConfigFileInClasspath());
            logbackRequestLog.setResource(httpServerWrapperConfig.getAccessLogConfigFileInClasspath());
        } else {
            logger.warn("No access logging configured!");
        }
        logHandler.setRequestLog(logbackRequestLog);
        handlerCollection.addHandler(logHandler);

        server.setHandler(handlerCollection);

        for (HttpServerListenerConfig listenerConfig : httpServerWrapperConfig.getHttpServerListeners()) {
            if (listenerConfig.isTls()) {

                SslContextFactory sslContextFactory = new SslContextFactory();
                sslContextFactory.setKeyStore(listenerConfig.getTlsKeystore());
                sslContextFactory.setKeyStorePassword(listenerConfig.getTlsKeystorePassphrase());

                sslContextFactory.setIncludeCipherSuites(listenerConfig.getTlsCipherSuites()
                    .toArray(new String[listenerConfig.getTlsCipherSuites().size()]));
                sslContextFactory.setIncludeProtocols(
                    listenerConfig.getTlsProtocols().toArray(new String[listenerConfig.getTlsProtocols().size()]));

                ServerConnector connector = new ServerConnector(server, sslContextFactory);
                connector.setPort(listenerConfig.getListenPort());
                connector.setHost(listenerConfig.getListenHost());
                server.addConnector(connector);
            } else {
                ServerConnector connector = new ServerConnector(server);
                connector.setPort(listenerConfig.getListenPort());
                connector.setHost(listenerConfig.getListenHost());
                server.addConnector(connector);
            }
        }

        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    /**
     * @return the config for this wrapper
     */
    public HttpServerWrapperConfig getHttpServerWrapperConfig() {
        return httpServerWrapperConfig;
    }
}
