package com.palominolabs.http.server;

import javax.annotation.concurrent.NotThreadSafe;
import java.net.URI;
import java.security.KeyStore;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@NotThreadSafe
public final class HttpServerListenerConfig {

    private final boolean tls;
    private final int listenPort;
    private final String listenHost;

    private KeyStore tlsKeystore = null;
    private String tlsKeystorePassphrase;
    private List<String> tlsCipherSuites =
        newArrayList("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
            "TLS_RSA_WITH_AES_256_CBC_SHA",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
            "TLS_RSA_WITH_AES_128_CBC_SHA",
            "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
            "TLS_EMPTY_RENEGOTIATION_INFO_SCSV");

    // SSLv2Hello in default because ELB's actual requests (not health checks) arrive using SSLv2 ClientHello
    private List<String> tlsProtocols = newArrayList("SSLv2Hello", "TLSv1", "TLSv1.1", "TLSv1.2");

    public HttpServerListenerConfig(String listenHost, int listenPort, boolean tls) {
        this.tls = tls;
        this.listenPort = listenPort;
        this.listenHost = listenHost;
    }

    static HttpServerListenerConfig forHttp(String host, int port) {
        return new HttpServerListenerConfig(host, port, false);
    }

    static HttpServerListenerConfig forHttps(String host, int port) {
        return new HttpServerListenerConfig(host, port, true);
    }

    public boolean isTls() {
        return tls;
    }

    public int getListenPort() {
        return listenPort;
    }

    public String getListenHost() {
        return listenHost;
    }

    public List<String> getTlsCipherSuites() {
        return tlsCipherSuites;
    }

    public void setTlsCipherSuites(List<String> tlsCipherSuites) {
        this.tlsCipherSuites = tlsCipherSuites;
    }

    public List<String> getTlsProtocols() {
        return tlsProtocols;
    }

    public void setTlsProtocols(List<String> tlsProtocols) {
        this.tlsProtocols = tlsProtocols;
    }

    HttpServerListenerConfig withTlsCipherSuites(List<String> tlsCipherSuites) {
        setTlsCipherSuites(tlsCipherSuites);
        return this;
    }

    HttpServerListenerConfig withTlsProtocols(List<String> tlsProtocols) {
        setTlsProtocols(tlsProtocols);
        return this;
    }

    public KeyStore getTlsKeystore() {
        return tlsKeystore;
    }

    public void setTlsKeystore(KeyStore tlsKeystore) {
        this.tlsKeystore = tlsKeystore;
    }

    public String getTlsKeystorePassphrase() {
        return tlsKeystorePassphrase;
    }

    public void setTlsKeystorePassphrase(String tlsKeystorePassphrase) {
        this.tlsKeystorePassphrase = tlsKeystorePassphrase;
    }
}
