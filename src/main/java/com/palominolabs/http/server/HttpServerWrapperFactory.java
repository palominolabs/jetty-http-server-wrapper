/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.palominolabs.http.server;

import com.google.inject.Inject;
import com.google.inject.servlet.GuiceFilter;

public class HttpServerWrapperFactory {

    private final GuiceFilter filter;

    @Inject
    HttpServerWrapperFactory(GuiceFilter filter) {
        this.filter = filter;
    }

    public HttpServerWrapper getHttpServerWrapper(HttpServerWrapperConfig config) {
        return new HttpServerWrapper(config, filter);
    }

}
