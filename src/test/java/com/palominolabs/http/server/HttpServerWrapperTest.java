/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.palominolabs.http.server;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.palominolabs.config.ConfigModuleBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.LogManager;

import static org.junit.Assert.assertEquals;

public final class HttpServerWrapperTest {

    private CloseableHttpClient client;
    private HttpServerWrapper server;

    @BeforeClass
    public static void setUpClass() throws Exception {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

    @After
    public void tearDown() throws Exception {
        client.close();
        server.stop();
    }

    @Before
    public void setUp() throws Exception {

        server = getServer(getDefaultConfig());

        server.start();

        SSLContext sslContext = SSLContext.getInstance("TLS");
        X509TrustManager x509TrustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                for (X509Certificate x509Certificate : x509Certificates) {
                    // the serial for the self-signed cert
                    if (x509Certificate.getSerialNumber().equals(new BigInteger("11945333926250205190"))) {
                        return;
                    }
                }

                throw new CertificateException();
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sslContext.init(null, new TrustManager[]{x509TrustManager}, null);

        client = HttpClients.custom()
            .setSslcontext(sslContext)
            .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER
            )).build();
    }

    @Test
    public void testHttps() throws Exception {
        HttpResponse response = client.execute(new HttpGet("https://localhost:" + 8443 + "/test"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("test", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testHttp() throws Exception {
        HttpResponse response = client.execute(new HttpGet("http://localhost:" + 8080 + "/test"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("test", EntityUtils.toString(response.getEntity()));
    }

    private static HttpServerWrapper getServer(HttpServerWrapperConfig config) {

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                binder().requireExplicitBindings();
                install(new ConfigModuleBuilder().build());
                install(new HttpServerWrapperModule());
                install(new ServletModule() {
                    @Override
                    protected void configureServlets() {
                        bind(TestServlet.class);
                        serve("/test").with(TestServlet.class);
                    }
                });
            }
        });

        return injector.getInstance(HttpServerWrapperFactory.class).getHttpServer(config);
    }

    private static HttpServerWrapperConfig getDefaultConfig() {
        HttpServerWrapperConfig config = new HttpServerWrapperConfig();

        config.addHttpServerListenerConfig(HttpServerListenerConfig.forHttp("localhost", 8080));
        HttpServerListenerConfig httpsConfig = HttpServerListenerConfig.forHttps("localhost", 8443);

        httpsConfig.setTlsKeystoreUri(URI.create("classpath:/cert-and-key.p12"));
        httpsConfig.setTlsKeystorePassphrase("password");

        config.addHttpServerListenerConfig(httpsConfig);

        config.setAccessLogConfigFileInClasspath("/logback-access-test.xml");
        return config;
    }

    @Singleton
    public static class TestServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setContentType("UTF-8");
            resp.getWriter().append("test");
        }
    }
}
