/*
 * DropperTest.java
 * 
 * Created on 29 Jun 2020 14:38:59
 * 
 */

/**
 * Copyright (c) 2026 Christopher Tipper
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
package net.perspective.draw;

import java.awt.BasicStroke;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DropperTest
 *
 * @author ctipper
 */

public class DropperTest {

    Dropper dropper;

    java.util.List<Float> strokeTypes = Arrays.asList(1.0f, 1.5f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 8.0f, 10.0f);

    public DropperTest() {
        dropper = new Dropper();
    }

    private static final Logger logger = LoggerFactory.getLogger(DropperTest.class.getName());

    @BeforeEach
    public void setUp() {
        logger.info("* DropperTest: setUp() method");
    }

    @AfterEach
    public void tearDown() {
        logger.info("* DropperTest: tearDown() method");
    }

    @Test
    @DisplayName("Test binary search")
    public void getBinaryTest() {
        for (var width : strokeTypes) {
            BasicStroke stroke = new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            assertEquals(dropper.getStrokeIdLinear(stroke), dropper.getStrokeIdBinary(stroke),
                    () -> "[binary] stroke width " + width + "failed");
        }
    }

    @Test
    @DisplayName("Test functional stream search")
    public void getFunctionalTest() {
        for (var width : strokeTypes) {
            BasicStroke stroke = new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            assertEquals(dropper.getStrokeIdLinear(stroke), dropper.getStrokeIdFunctionalStream(stroke),
                    () -> "[functional] stroke width " + width + "failed");
        }
    }

    @Test
    @DisplayName("Test that binary and stream search concur")
    public void getBothTest() {
        for (var width : strokeTypes) {
            BasicStroke stroke = new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            assertEquals(dropper.getStrokeIdBinary(stroke), dropper.getStrokeIdFunctionalStream(stroke),
                    () -> "[stream] stroke width " + width + "failed");
        }
    }

}
