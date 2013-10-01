/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.palominolabs.http.server;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * A simple bean to hold config info.
 */
@NotThreadSafe
public final class HttpServerWrapperConfig {

    private final List<HttpServerListenerConfig> listenerConfigs = newArrayList();

    private int maxFormContentSize = -1;

    private String accessLogConfigFileInClasspath = "/" + this.getClass().getPackage()
        .getName().replace('.', '/') + "/pl-default-logback-access.xml";
    private String accessLogConfigFileInFilesystem = null;

    public String getAccessLogConfigFileInClasspath() {
        return accessLogConfigFileInClasspath;
    }

    public void setAccessLogConfigFileInClasspath(String accessLogConfigFileInClasspath) {
        this.accessLogConfigFileInFilesystem = null;
        this.accessLogConfigFileInClasspath = accessLogConfigFileInClasspath;
    }

    public String getAccessLogConfigFileInFilesystem() {
        return accessLogConfigFileInFilesystem;
    }

    public void setAccessLogConfigFileInFilesystem(String accessLogConfigFileInFilesystem) {
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
     */
    public void setMaxFormContentSize(int maxFormContentSize) {
        this.maxFormContentSize = maxFormContentSize;
    }

    public void addHttpServerListenerConfig(HttpServerListenerConfig config) {
        listenerConfigs.add(config);
    }

    public HttpServerWrapperConfig withHttpServerListenerConfig(HttpServerListenerConfig config) {
        addHttpServerListenerConfig(config);
        return this;
    }

    List<HttpServerListenerConfig> getHttpServerListeners() {
        return listenerConfigs;
    }
}
