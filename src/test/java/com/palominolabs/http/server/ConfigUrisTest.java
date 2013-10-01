/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.palominolabs.http.server;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class ConfigUrisTest {

    @Test
    public void testFindsExistingClasspathResource() throws Exception {

        URL url = ConfigUris.toUrl(URI.create("classpath:/sample-resource.txt"));

        String str = Resources.toString(url, Charsets.UTF_8);
        assertEquals("hello", str);
    }

    @Test
    public void testCantFindBadClasspathResource() throws Exception {

        try {
            ConfigUris.toUrl(URI.create("classpath:/zzzzzzzzzzzzz"));
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Classpath uri can't be found: <classpath:/zzzzzzzzzzzzz>", e.getMessage());
        }
    }

    @Test
    public void testGetFileUri() throws URISyntaxException {
        URL url = ConfigUris.toUrl(URI.create("file:/"));
        File f = new File(url.toURI());
        assertTrue(f.list().length > 0);
    }
}
