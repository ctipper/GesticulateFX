/*
 * DropperTest.java
 * 
 * Created on 29 Jun 2020 14:38:59
 * 
 */
package net.perspective.draw;

import java.awt.BasicStroke;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
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
