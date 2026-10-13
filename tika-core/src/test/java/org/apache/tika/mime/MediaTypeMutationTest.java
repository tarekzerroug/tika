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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class MediaTypeMutationTest {

    @Test
    public void testUnionPreservesExistingAndNewParameters() {
        Map<String, String> existingParameters = new HashMap<>();
        existingParameters.put("charset", "UTF-8");

        MediaType original = new MediaType(
                "text", "plain", existingParameters);

        Map<String, String> newParameters = new HashMap<>();
        newParameters.put("version", "1");

        MediaType combined = new MediaType(original, newParameters);

        assertEquals("UTF-8", combined.getParameters().get("charset"));
        assertEquals("1", combined.getParameters().get("version"));
        assertEquals(2, combined.getParameters().size());
    }

    @Test
    public void testParseSimpleMediaType() {
        MediaType result = MediaType.parse("application/json");

        assertNotNull(result);
        assertEquals("application", result.getType());
        assertEquals("json", result.getSubtype());
        assertEquals("application/json", result.toString());
    }

    @Test
    public void testParseSimpleNameWithValidCharacters() {
        MediaType result = MediaType.parse("application/x-test123");

        assertNotNull(result);
        assertEquals("application", result.getType());
        assertEquals("x-test123", result.getSubtype());
        assertEquals("application/x-test123", result.toString());
    }

    @Test
    public void testParseCachesSimpleMediaTypes() {
        String mimeType = "application/x-ift3913-cache-test-2026";

        MediaType first = MediaType.parse(mimeType);
        MediaType second = MediaType.parse(mimeType);

        assertSame(first, second);
    }
}
