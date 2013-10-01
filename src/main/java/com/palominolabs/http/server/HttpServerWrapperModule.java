/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.palominolabs.http.server;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.GuiceFilter;

/**
 * Binds an HTTP server with Guice servlet, Jackson and authentication support.
 */
public class HttpServerWrapperModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(GuiceFilter.class);
        bind(HttpServerWrapperFactory.class);
    }
}
