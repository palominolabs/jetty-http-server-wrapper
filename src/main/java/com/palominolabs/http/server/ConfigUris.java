/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.palominolabs.http.server;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

final class ConfigUris {

    private ConfigUris() {
    }

    /**
     * @param uri a classpath:/ or file:/ uri
     * @return a url
     * @throws IllegalArgumentException if the uri has a scheme other than classpath or file, or if the uri is
     *                                  malformed, or if the uri points to a classpath resource that can't be found
     */
    @Nonnull
    static URL toUrl(URI uri) throws IllegalArgumentException {
        String scheme = uri.getScheme();

        try {
            if (scheme.equals("file")) {
                return uri.toURL();
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }

        if (scheme.equals("classpath")) {
            URL resource = ConfigUris.class.getResource(uri.getSchemeSpecificPart());
            if (resource == null) {
                throw new IllegalArgumentException("Classpath uri can't be found: <" + uri + ">");
            }

            return resource;
        }

        throw new IllegalArgumentException("Unknown scheme: <" + uri + ">");
    }
}
