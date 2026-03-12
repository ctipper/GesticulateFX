/**
 * ComposerTest.java
 * 
 * Created on 12 Mar 2026 14:18:52
 * 
 */

/**
 * Copyright (c) 2026 e-conomist
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.perspective.draw.text;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.perspective.draw.editor.HTMLReader;
import net.perspective.draw.editor.HTMLWriter;
import net.perspective.draw.editor.MarkSpec;
import net.perspective.draw.editor.Node;
import net.perspective.draw.editor.NodeSpec;
import net.perspective.draw.editor.Schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author ctipper
 */

public class ComposerTest {

    private Schema schema;
    private Node doc;
    private HTMLReader reader;
    private HTMLWriter writer;

    private static final Logger logger = LoggerFactory.getLogger(ComposerTest.class.getName());

    /** Creates a new instance of <code>ComposerTest</code> */
    public ComposerTest() {
    }

    @BeforeEach
    public void setUp() {
        this.initbuilder();
        logger.info("* ComposerTest: setUp() method");
    }
    
    @AfterEach
    public void tearDown() {
        logger.info("* ComposerTest: tearDown() method");
    }

    private void initbuilder() {
        // Build the schema
        schema = new Schema(
            new LinkedHashMap<>(Map.of(
                "doc",       new NodeSpec("paragraph+", null, false, Map.of(), null),
                "paragraph", new NodeSpec("text*", "block", false, Map.of(), null),
                "text",      new NodeSpec(null, null, true, Map.of(), null)
            )),
            new LinkedHashMap<>(Map.of(
                "b", new MarkSpec(),
                "i", new MarkSpec(),
                "u", new MarkSpec()
            ))
        );
        reader = new HTMLReader(schema);
        writer = new HTMLWriter();
    }

    @Test
    @DisplayName("Test round-trip of valid HTML")
    public void roundTripTest() {
        copy("<p>Please insert text</p>");
        copy("<p>Please <u>insert</u> text</p>");
        copy("<p>Please <u><b>bold</b> insert</u> text</p>");
        copy("<p>Please <u><b>bold</b> some <i>insert</i></u> text</p>");
        copy("<p><i>Please</i> <b>insert</b> <u>text</u></p>");
        copy("<p><b>Please </b><i>insert </i><u>text</u></p>");
        copy("<p>Please <b><i><u>insert/u></i></b> text</p>");
        copy("<p><b>Please <i>insert</i></b><i> text</i></p>");
        copy("<p><u>Please</u> <u>insert</u> <u>text</u></p>");
        copy("<p>Please <b>insert <i>text <u>all</u> and</i> some</b> more</p>");
        copy("<p><b>A</b>B<i>C</i>D<u>E</u></p>");
        copy("<p><i><b>Please insert text</b></i></p>");
    }

    private void copy(String input) {
        doc = reader.parse(input);
        String list1 = writer.serialize(doc);
        Node copy = reader.parse(list1);
        String list2 = writer.serialize(copy);
        // Canonical round-trip: serialize → parse → serialize is stable
        assertEquals(list1, list2,
                    () -> "[round-trip] " + input + " failed");
        // Node trees are structurally equal
        assertTrue(doc.eq(copy),
                    () -> "[node eq] " + input + " failed");
    }

}
