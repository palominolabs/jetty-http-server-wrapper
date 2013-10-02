/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.palominolabs.http.server;

import javax.annotation.Nonnull;

public interface HttpServerWrapperFactory {

    @Nonnull
    public HttpServerWrapper getHttpServerWrapper(@Nonnull HttpServerWrapperConfig config);
}
