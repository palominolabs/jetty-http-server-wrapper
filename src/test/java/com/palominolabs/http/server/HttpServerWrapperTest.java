/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.palominolabs.http.server;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.palominolabs.config.ConfigModuleBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.LogManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class HttpServerWrapperTest {

    private static final int TLS_PORT = 28443;
    private static final int HTTP_PORT = 28080;

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
    public void testServletHttps() throws Exception {
        HttpResponse response = client.execute(new HttpGet("https://localhost:" + TLS_PORT + "/test"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("test", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testServletHttp() throws Exception {
        HttpResponse response = client.execute(new HttpGet("http://localhost:" + HTTP_PORT + "/test"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("test", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void test404() throws IOException {
        HttpResponse response = client.execute(new HttpGet("http://localhost:" + HTTP_PORT + "/nowhere"));
        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testResourceHandler() throws IOException {
        HttpResponse response = client.execute(new HttpGet("http://localhost:" + HTTP_PORT +
            "/static1/static-res-1.txt"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("res1", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testResourceHandlerMappedToRoot() throws IOException {
        HttpResponse response = client.execute(new HttpGet("http://localhost:" + HTTP_PORT +
            "/static-res-2.txt"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("res2", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testResourceHandlersMappedToSameContextPrefersFirst() throws IOException {
        HttpResponse response = client.execute(new HttpGet("http://localhost:" + HTTP_PORT +
            "/conflict/name-conflict.txt"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("conflict1", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testUrlMappedToServletAndResourceHandlerIsHandledByResourceHandler() throws IOException {
        HttpResponse response = client.execute(new HttpGet("http://localhost:" + HTTP_PORT +
            "/conflict-with-servlet"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("static resource", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testNoDirectoryListing() throws IOException {
        HttpResponse response = client.execute(new HttpGet("http://localhost:" + HTTP_PORT +
            "/static1"));
        assertEquals(403, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testWithDirectoryListing() throws IOException {
        HttpResponse response = client.execute(new HttpGet("http://localhost:" + HTTP_PORT +
            "/with-dir-listing"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(EntityUtils.toString(response.getEntity()).contains("static-res-1.txt"));
    }

    @Test
    public void testWithIndexPage() throws IOException {
        HttpResponse response = client.execute(new HttpGet("http://localhost:" + HTTP_PORT +
            "/with-index"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("index file", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testWithAltIndexPage() throws IOException {
        HttpResponse response = client.execute(new HttpGet("http://localhost:" + HTTP_PORT +
            "/with-alt-index"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("alt index", EntityUtils.toString(response.getEntity()));
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
                        serve("/conflict-with-servlet").with(TestServlet.class);
                    }
                });
            }
        });

        return injector.getInstance(HttpServerWrapperFactory.class).getHttpServerWrapper(config);
    }

    private static HttpServerWrapperConfig getDefaultConfig() throws KeyStoreException, CertificateException,
        NoSuchAlgorithmException, IOException {

        InputStream stream = HttpServerWrapperTest.class.getResourceAsStream("/cert-and-key.p12");
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(stream, "password".toCharArray());

        HttpServerConnectorConfig httpsConfig = HttpServerConnectorConfig.forHttps("localhost", TLS_PORT)
            .withTlsKeystore(keyStore)
            .withTlsKeystorePassphrase("password");

        HttpResourceHandlerConfig httpResourceHandlerConfig1 = new HttpResourceHandlerConfig()
            .withBaseResource(Resource.newClassPathResource("/resourceBase1"))
            .withContextPath("/static1");

        HttpResourceHandlerConfig httpResourceHandlerConfig2 = new HttpResourceHandlerConfig()
            .withBaseResource(Resource.newClassPathResource("/resourceBase1"))
            .withContextPath("/");

        HttpResourceHandlerConfig httpResourceHandlerConfigConflict1 = new HttpResourceHandlerConfig()
            .withBaseResource(Resource.newClassPathResource("/resourceBase1"))
            .withContextPath("/conflict");

        HttpResourceHandlerConfig httpResourceHandlerConfigConflict2 = new HttpResourceHandlerConfig()
            .withBaseResource(Resource.newClassPathResource("/resourceBase2"))
            .withContextPath("/conflict");

        HttpResourceHandlerConfig withDirListing = new HttpResourceHandlerConfig()
            .withBaseResource(Resource.newClassPathResource("/resourceBase1"))
            .withDirectoryListing(true)
            .withContextPath("/with-dir-listing");

        HttpResourceHandlerConfig withIndex = new HttpResourceHandlerConfig()
            .withBaseResource(Resource.newClassPathResource("/withIndex"))
            .withContextPath("/with-index");

        HttpResourceHandlerConfig withAltIndex = new HttpResourceHandlerConfig()
            .withBaseResource(Resource.newClassPathResource("/withAltIndex"))
            .withContextPath("/with-alt-index")
            .withWelcomeFiles(Lists.newArrayList("alt-index.txt"));

        return new HttpServerWrapperConfig()
            .withAccessLogConfigFileInClasspath("/logback-access-test.xml")
            .withHttpServerConnectorConfig(httpsConfig)
            .withHttpServerConnectorConfig(HttpServerConnectorConfig.forHttp("localhost", HTTP_PORT))
            .withResourceHandlerConfig(httpResourceHandlerConfig1)
            .withResourceHandlerConfig(httpResourceHandlerConfig2)
            .withResourceHandlerConfig(httpResourceHandlerConfigConflict1)
            .withResourceHandlerConfig(httpResourceHandlerConfigConflict2)
            .withResourceHandlerConfig(withDirListing)
            .withResourceHandlerConfig(withIndex)
            .withResourceHandlerConfig(withAltIndex);
    }
}
