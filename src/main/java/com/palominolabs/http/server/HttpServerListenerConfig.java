package com.palominolabs.http.server;

import javax.annotation.concurrent.NotThreadSafe;
import java.net.URI;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@NotThreadSafe
public final class HttpServerListenerConfig {

    private final boolean tls;
    private int listenPort = 8080;
    private String listenHost = "127.0.0.1";

    private URI tlsKeystoreUri = null;
    private String tlsKeystoreType = "PKCS12";
    private String tlsKeystorePassphrase = null;
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

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public String getListenHost() {
        return listenHost;
    }

    public void setListenHost(String listenHost) {
        this.listenHost = listenHost;
    }

    public URI getTlsKeystoreUri() {
        return tlsKeystoreUri;
    }

    public void setTlsKeystoreUri(URI tlsKeystoreUri) {
        this.tlsKeystoreUri = tlsKeystoreUri;
    }

    public String getTlsKeystoreType() {
        return tlsKeystoreType;
    }

    public void setTlsKeystoreType(String tlsKeystoreType) {
        this.tlsKeystoreType = tlsKeystoreType;
    }

    public String getTlsKeystorePassphrase() {
        return tlsKeystorePassphrase;
    }

    public void setTlsKeystorePassphrase(String tlsKeystorePassphrase) {
        this.tlsKeystorePassphrase = tlsKeystorePassphrase;
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

    HttpServerListenerConfig withTlsKeystoreUri(URI tlsKeystore) {
        setTlsKeystoreUri(tlsKeystore);
        return this;
    }

    HttpServerListenerConfig withTlsKeystorePassphrase(String tlsPassphrase) {
        setTlsKeystorePassphrase(tlsPassphrase);
        return this;
    }

    HttpServerListenerConfig withTlsKeystoreType(String tlsKeystoreType) {
        setTlsKeystoreType(tlsKeystoreType);
        return this;
    }

    HttpServerListenerConfig withTlsCipherSuites(List<String> tlsCipherSuites) {
        setTlsCipherSuites(tlsCipherSuites);
        return this;
    }

    HttpServerListenerConfig withTlsProtocols(List<String> tlsProtocols) {
        setTlsProtocols(tlsProtocols);
        return this;
    }
}
