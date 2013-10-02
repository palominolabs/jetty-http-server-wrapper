/*
 * Copyright (c) 2012 Palomino Labs, Inc.
 */

package com.palominolabs.http.server;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class ConfigUrisTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

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
    public void testCanReadFromFileCopyOfResource() throws IOException {
        File file = temporaryFolder.newFile();
        FileOutputStream fos = new FileOutputStream(file);

        byte[] resourceBytes = Resources.toByteArray(ConfigUris.toUrl(URI.create("classpath:/sample-resource.txt")));
        fos.write(resourceBytes);
        fos.close();

        byte[] fileBytes = Resources.toByteArray(ConfigUris.toUrl(URI.create("file:" + file.getCanonicalPath())));

        assertArrayEquals(resourceBytes, fileBytes);
    }

}
