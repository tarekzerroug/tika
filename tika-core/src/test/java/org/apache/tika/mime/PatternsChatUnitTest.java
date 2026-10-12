/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.mime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PatternsChatUnitTest {

    private Patterns patterns;
    private MimeTypes types;

    @BeforeEach
    public void setUp() throws MimeTypeException {
        patterns = new Patterns(new MediaTypeRegistry());
        types = new MimeTypes();
    }

    @Test
    public void testMatchesExactName() throws Exception {
        MimeType text = types.forName("text/plain");

        patterns.add("text/plain", text);

        MimeType result = patterns.matches("text/plain");

        assertEquals(text, result);
    }

    @Test
    public void testMatchesExtension() throws Exception {
        MimeType text = types.forName("text/plain");

        patterns.add("*.txt", text);

        MimeType result = patterns.matches("example.txt");

        assertEquals(text, result);
    }

    @Test
    public void testAddGlobPattern() throws Exception {
        MimeType text = types.forName("text/plain");

        patterns.add("text/*", text);

        MimeType result = patterns.matches("text/plain");

        assertEquals(text, result);
    }

    @Test
    public void testMatchesNullName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> patterns.matches(null)
        );
    }
}