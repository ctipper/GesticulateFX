/**
 * ContainsRotator.java
 *
 * Created on Nov 8, 2010, 2:13:16 PM
 *
 */
package net.perspective.draw.event;

/**
 *
 * @author ctipper
 *
 * Permute ContainsType according to angle of rotation
 */
import net.perspective.draw.enums.ContainsType;

public class ContainsRotator {

    static ContainsType[] typeArray = {ContainsType.TL, ContainsType.BL,
        ContainsType.BR, ContainsType.TR};

    /**
     * Prevent instance creation
     */
    private ContainsRotator() {
    }

    public static ContainsType permute(ContainsType type, double angle) {
        int index, offset;

        index = 0;

        for (int i = 0; i < 4; i++) {
            if (type.equals(typeArray[i])) {
                index = i;
            }
        }

        offset = angleOffset(angle);

//        if (isVertical) {
//            offset = offset - 1;
//        }

        return typeArray[((index + offset) + 4) % 4];
    }

    private static int angleOffset(double angle) {
        int offset = 0;
        if ((angle >= -Math.PI / 4) && (angle < Math.PI / 4)) {
            offset = 0;
        }
        if ((angle >= Math.PI / 4) && (angle < 0.75 * Math.PI)) {
            offset = -1;
        }
        if (((angle >= 0.75 * Math.PI) && (angle < Math.PI))
                || ((angle < -Math.PI / 4) && (angle > -Math.PI))
                || (Math.abs(angle) == Math.PI)) {
            offset = -2;
        }
        if ((angle >= -0.75 * Math.PI) && (angle < -Math.PI / 4)) {
            offset = -3;
        }
        return offset;
    }
}
