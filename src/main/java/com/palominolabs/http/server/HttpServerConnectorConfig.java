package com.palominolabs.http.server;

import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.security.KeyStore;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Config for an individual connector that will be used in a {@link HttpServerWrapper}. Defaults, where provided, are
 * sane.
 */
@NotThreadSafe
public final class HttpServerConnectorConfig {

    private final boolean tls;
    private final int listenPort;
    private final String listenHost;

    private KeyStore tlsKeystore;
    private String tlsKeystorePassphrase;
    @Nonnull
    private List<String> tlsCipherSuites =
        newArrayList("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
            "TLS_RSA_WITH_AES_256_CBC_SHA",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
            "TLS_RSA_WITH_AES_128_CBC_SHA",
            "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
            "TLS_EMPTY_RENEGOTIATION_INFO_SCSV");

    /**
     * SSLv2Hello in default because AWS ELB's actual requests (not health checks) arrive using SSLv2 ClientHello
     */
    @Nonnull
    private List<String> tlsProtocols = newArrayList("SSLv2Hello", "TLSv1", "TLSv1.1", "TLSv1.2");

    public HttpServerConnectorConfig(@Nonnull String listenHost, int listenPort, boolean tls) {
        this.tls = tls;
        this.listenPort = listenPort;
        this.listenHost = checkNotNull(listenHost);
    }

    public static HttpServerConnectorConfig forHttp(@Nonnull String host, int port) {
        return new HttpServerConnectorConfig(host, port, false);
    }

    public static HttpServerConnectorConfig forHttps(@Nonnull String host, int port) {
        return new HttpServerConnectorConfig(host, port, true);
    }

    public boolean isTls() {
        return tls;
    }

    public int getListenPort() {
        return listenPort;
    }

    @Nonnull
    public String getListenHost() {
        return listenHost;
    }

    @Nonnull
    public List<String> getTlsCipherSuites() {
        return tlsCipherSuites;
    }

    /**
     * @param tlsCipherSuites List of TLS cipher suites, passed to {@link SslContextFactory#setIncludeCipherSuites(String...)}
     */
    public void setTlsCipherSuites(@Nonnull List<String> tlsCipherSuites) {
        assertTls();
        this.tlsCipherSuites = checkNotNull(tlsCipherSuites);
    }

    @Nonnull
    public List<String> getTlsProtocols() {
        return tlsProtocols;
    }

    /**
     * @param tlsProtocols List of TLS protocols, passed to {@link SslContextFactory#setIncludeProtocols(String...)}
     */
    public void setTlsProtocols(@Nonnull List<String> tlsProtocols) {
        assertTls();
        this.tlsProtocols = checkNotNull(tlsProtocols);
    }

    public KeyStore getTlsKeystore() {
        return tlsKeystore;
    }

    /**
     * @param tlsKeystore Keystore to use for TLS private keys
     */
    public void setTlsKeystore(@Nonnull KeyStore tlsKeystore) {
        assertTls();
        this.tlsKeystore = checkNotNull(tlsKeystore);
    }

    public String getTlsKeystorePassphrase() {
        return tlsKeystorePassphrase;
    }

    public void setTlsKeystorePassphrase(@Nonnull String tlsKeystorePassphrase) {
        assertTls();
        this.tlsKeystorePassphrase = checkNotNull(tlsKeystorePassphrase);
    }

    /**
     * @return this
     * @see HttpServerConnectorConfig#setTlsProtocols(List)
     */
    @Nonnull
    HttpServerConnectorConfig withTlsProtocols(@Nonnull List<String> tlsProtocols) {
        setTlsProtocols(tlsProtocols);
        return this;
    }

    /**
     * @return this
     * @see HttpServerConnectorConfig#setTlsCipherSuites(List)
     */
    @Nonnull
    HttpServerConnectorConfig withTlsCipherSuites(@Nonnull List<String> tlsCipherSuites) {
        setTlsCipherSuites(tlsCipherSuites);
        return this;
    }

    /**
     * @return this
     * @see HttpServerConnectorConfig#setTlsKeystorePassphrase(String)
     */
    @Nonnull
    HttpServerConnectorConfig withTlsKeystorePassphrase(@Nonnull String tlsKeystorePassphrase) {
        setTlsKeystorePassphrase(tlsKeystorePassphrase);
        return this;
    }

    /**
     * @return this
     * @see HttpServerConnectorConfig#setTlsKeystore(KeyStore)
     */
    @Nonnull
    HttpServerConnectorConfig withTlsKeystore(@Nonnull KeyStore tlsKeystore) {
        setTlsKeystore(tlsKeystore);
        return this;
    }

    /**
     * @throws IllegalStateException if tls is false
     */
    private void assertTls() {
        if (!tls) {
            throw new IllegalStateException("Only applicable to TLS connectors");
        }
    }
}
