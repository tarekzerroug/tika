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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PatternsManualTest {

    private Patterns patterns;
    private MimeTypes types;

    @BeforeEach
    public void setUp() {
        patterns = new Patterns(new MediaTypeRegistry());
        types = new MimeTypes();
    }

    @Test
    public void testExplicitJavaRegex() throws Exception {
        MimeType text = types.forName("text/plain");

        patterns.add("file[0-9]+\\.txt", true, text);

        assertEquals(text, patterns.matches("file123.txt"));
        assertNull(patterns.matches("fileABC.txt"));
    }

    @Test
    public void testQuestionMarkPattern() throws Exception {
        MimeType text = types.forName("text/plain");

        patterns.add("file?.txt", text);

        assertEquals(text, patterns.matches("file1.txt"));
        assertNull(patterns.matches("file12.txt"));
    }

    
    @Test
    public void testBracketPattern() throws Exception {
    MimeType text = types.forName("text/plain");

    patterns.add("[abc]", text);

    assertEquals(text, patterns.matches("[abc]"));
}

    @Test
    public void testLongerGlobHasPriority() throws Exception {
        MimeType general = types.forName("text/plain");
        MimeType specific = types.forName("application/pdf");

        patterns.add("file*", general);
        patterns.add("file*.pdf", specific);

        assertEquals(specific, patterns.matches("file-test.pdf"));
    }
}
