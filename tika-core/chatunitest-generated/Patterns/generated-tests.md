<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

```java
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

public class PatternsTest {

    private Patterns patterns;
    private MediaTypeRegistry registry;

    @BeforeEach
    public void setUp() throws IOException {
        registry = new MediaTypeRegistry();
        patterns = new Patterns(registry);
    }

    @Test
    public void testAddName() throws MimeTypeException {
        patterns.add("image/jpeg", false, new MimeType(MediaType.IMAGE_JPEG));
        assertEquals(patterns.names.get("image/jpeg"), new MimeType(MediaType.IMAGE_JPEG));
    }

    @Test
    public void testAddExtension() throws MimeTypeException {
        patterns.add("*jpeg", false, new MimeType(MediaType.IMAGE_JPEG));
        assertEquals(patterns.extensions.get("*jpeg"), new MimeType(MediaType.IMAGE_JPEG));
    }

    @Test
    public void testAddGlob() throws MimeTypeException {
        patterns.add("image/*", false, new MimeType(MediaType.IMAGE_JPEG));
        assertEquals(patterns.globs.get("image/*"), new MimeType(MediaType.IMAGE_JPEG));
    }

    @Test
    public void testAddNameWithSpecialization() throws MimeTypeException {
        registry.register(new MediaType(MediaType.IMAGE_JPEG));
        patterns.add("image/jpeg", false, new MimeType(MediaType.IMAGE_JPEG));
        assertEquals(patterns.names.get("image/jpeg"), new MimeType(MediaType.IMAGE_JPEG));
    }

    @Test
    public void testAddExtensionWithSpecialization() throws MimeTypeException {
        registry.register(new MediaType(MediaType.IMAGE_JPEG));
        patterns.add("*jpeg", false, new MimeType(MediaType.IMAGE_JPEG));
        assertEquals(patterns.extensions.get("*jpeg"), new MimeType(MediaType.IMAGE_JPEG));
    }

    @Test
    public void testAddGlobWithSpecialization() throws MimeTypeException {
        registry.register(new MediaType(MediaType.IMAGE_JPEG));
        patterns.add("image/*", false, new MimeType(MediaType.IMAGE_JPEG));
        assertEquals(patterns.globs.get("image/*"), new MimeType(MediaType.IMAGE_J

```java
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PatternsTest {

    private Patterns patterns;

    @BeforeEach
    public void setUp() throws MimeTypeException {
        patterns = new Patterns(MediaTypeRegistry.getDefaultRegistry());
    }

    @Test
    public void testAddValidPattern() throws MimeTypeException {
        patterns.add("text/plain", MediaType.TEXT_PLAIN);
        assertEquals(1, patterns.names.size());
        assertEquals(0, patterns.extensions.size());
        assertEquals(0, patterns.globs.size());
    }

    @Test
    public void testAddInvalidPattern() throws MimeTypeException {
        try {
            patterns.add("", MediaType.TEXT_PLAIN);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected behavior
        }
    }

    @Test
    public void testAddGlobPattern() throws MimeTypeException {
        patterns.add("*text/plain", false, MediaType.TEXT_PLAIN);
        assertEquals(1, patterns.names.size());
        assertEquals(0, patterns.extensions.size());
        assertEquals(1, patterns.globs.size());
    }

    @Test
    public void testAddNamePattern() throws MimeTypeException {
        patterns.add("text/plain", false, MediaType.TEXT_PLAIN);
        assertEquals(1, patterns.names.size());
        assertEquals(0, patterns.extensions.size());
        assertEquals(0, patterns.globs.size());
    }

    @Test
    public void testAddExtensionPattern() throws MimeTypeException {
        patterns.add("txt", false, MediaType.TEXT_PLAIN);
        assertEquals(1, patterns.names.size());
        assertEquals(1, patterns.extensions.size());
        assertEquals(0, patterns.globs.size());
    }
}
```
```java
import org.apache.tika.mime.MediaTypeRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class PatternsTest {

    private Patterns patterns;
    private MediaTypeRegistry registry;

    @BeforeEach
    public void setUp() {
        registry = new MediaTypeRegistry();
        patterns = new Patterns(registry);
    }

    @Test
    public void testMatchesExactName() throws Exception {
        // Arrange
        String name = "text/plain";
        MimeType expected = registry.getMediaType("text/plain");

        // Act
        MimeType result = patterns.matches(name);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    public void testMatchesExtension() throws Exception {
        // Arrange
        String name = "example.txt";
        MimeType expected = registry.getMediaType("text/plain");

        // Act
        MimeType result = patterns.matches(name);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    public void testMatchesComplexRegexp() throws Exception {
        // Arrange
        String name = "application/pdf";
        MimeType expected = registry.getMediaType("application/pdf");

        // Act
        MimeType result = patterns.matches(name);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    public void testMatchesNullName() throws Exception {
        // Arrange

        // Act
        MimeType result = patterns.matches(null);

        // Assert
        assertNull(result);
    }
}
```
