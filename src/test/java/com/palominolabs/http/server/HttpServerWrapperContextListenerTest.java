package com.palominolabs.http.server;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;
import java.util.logging.LogManager;

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;

public class HttpServerWrapperContextListenerTest {

    private static final int HTTP_PORT = 28080;

    @BeforeClass
    public static void setUpClass() throws Exception {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

    @Test
    public void testListenerInstanceIsCalled() throws Exception {
        HttpServerWrapperConfig config = getDefaultConfig();

        ServletContextListener listener = EasyMock.createStrictMock(ServletContextListener.class);

        listener.contextInitialized(EasyMock.<ServletContextEvent>anyObject());
        listener.contextDestroyed(EasyMock.<ServletContextEvent>anyObject());

        replay(listener);

        config.addServletContextListener(listener);
        HttpServerWrapper server = getServer(config,
            getInjector(Lists.<Module>newArrayList(), Lists.<BinderProviderCapture<?>>newArrayList()));

        server.start();
        server.stop();

        verify(listener);
    }

    @Test
    public void testListenerProviderIsCalled() throws Exception {
        HttpServerWrapperConfig config = getDefaultConfig();

        Module listenerModule = new AbstractModule() {
            @Override
            protected void configure() {
                bind(DummyListener.class);
            }
        };

        BinderProviderCapture<? extends ServletContextListener> capture =
            new BinderProviderCapture<DummyListener>(DummyListener.class);
        config.addServletContextListenerProvider(capture);

        Injector injector =
            getInjector(Lists.newArrayList(listenerModule), Lists.<BinderProviderCapture<?>>newArrayList(capture));

        DummyListener listener = injector.getInstance(DummyListener.class);

        HttpServerWrapper server = getServer(config, injector);

        server.start();
        server.stop();

        assertNotNull(listener.initEvent);
        assertNotNull(listener.destroyEvent);
    }

    private static HttpServerWrapper getServer(HttpServerWrapperConfig config, Injector injector) {

        return injector.getInstance(HttpServerWrapperFactory.class).getHttpServerWrapper(config);
    }

    private static Injector getInjector(final List<Module> modules,
        final List<BinderProviderCapture<?>> binderCaptures) {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                binder().requireExplicitBindings();
                install(new HttpServerWrapperModule());
                install(new ServletModule() {
                    @Override
                    protected void configureServlets() {
                        bind(TestServlet.class);
                        serve("/test").with(TestServlet.class);
                    }
                });

                for (Module module : modules) {
                    install(module);
                }

                for (BinderProviderCapture<?> binderAction : binderCaptures) {
                    binderAction.saveProvider(binder());
                }
            }
        });
    }

    private static HttpServerWrapperConfig getDefaultConfig() {
        return new HttpServerWrapperConfig()
            .withAccessLogConfigFileInClasspath("/logback-access-test.xml")
            .withHttpServerConnectorConfig(HttpServerConnectorConfig.forHttp("localhost", HTTP_PORT));
    }

    @Singleton
    private static class DummyListener implements ServletContextListener {

        private ServletContextEvent initEvent;
        private ServletContextEvent destroyEvent;

        @Override
        public void contextInitialized(ServletContextEvent sce) {
            initEvent = sce;
        }

        @Override
        public void contextDestroyed(ServletContextEvent sce) {
            destroyEvent = sce;
        }
    }
}
