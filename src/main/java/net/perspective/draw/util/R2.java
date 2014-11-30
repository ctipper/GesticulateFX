/*
 * R2.java
 * 
 * Created on Nov 16, 2014 11:39:22 AM
 * 
 */
package net.perspective.draw.util;

/**
 *
 * @author ctipper
 */
import net.perspective.draw.enums.ContainsType;

public class R2 {

    static ContainsType[] flip1Array = { ContainsType.TR, ContainsType.BL, ContainsType.BR, ContainsType.TL };

    static ContainsType[] flip2Array = { ContainsType.BR, ContainsType.BL, ContainsType.TR, ContainsType.TL };

    static ContainsType[] flip3Array = { ContainsType.TL, ContainsType.BR, ContainsType.BL, ContainsType.TR };

    static ContainsType[] flip4Array = { ContainsType.BL, ContainsType.BR, ContainsType.TL, ContainsType.TR };

    static double[] flip1 = { 0, 1 };

    static double[] flip2 = { 1, 0 };

    static double[] flip3 = { 0, -1 };

    static double[] flip4 = { -1, 0 };

    public static int quadrant(CanvasPoint vertex, CanvasPoint centre) {
        double theta = V2.declination(vertex.x - centre.x, vertex.y - centre.y);
        return V2.quadrant(theta);
    }

    public static ContainsType permute(ContainsType contains, int quad) {
        switch (quad) {
            case 0:
                contains = R2.flip1(contains, quad);
                break;
            case 1:
                contains = R2.flip2(contains, quad);
                break;
            case 2:
                contains = R2.flip3(contains, quad);
                break;
            case 3:
                contains = R2.flip4(contains, quad);
                break;
            default:
                contains = ContainsType.NONE;
                break;
        }
        return contains;
    }
    
    public static double[] flip(int quad) {
        double[] flip;
        switch (quad) {
            case 0:
                flip = flip1;
                break;
            case 1:
                flip = flip2;
                break;
            case 2:
                flip = flip3;
                break;
            case 3:
                flip = flip4;
                break;
            default:
                flip = flip3;
                break;
        }
        return flip;
    } 

    private static ContainsType flip1(ContainsType type, int quad) {
        int index = 0;

        for (int i = 0; i < 4; i++) {
            if (type.equals(flip3Array[i])) {
                index = i;
            }
        }

        return flip1Array[((index - quad) + 4) % 4];
    }

    private static ContainsType flip2(ContainsType type, int quad) {
        int index = 0;

        for (int i = 0; i < 4; i++) {
            if (type.equals(flip3Array[i])) {
                index = i;
            }
        }

        return flip2Array[((index - quad) + 4) % 4];
    }

    private static ContainsType flip3(ContainsType type, int quad) {
        int index = 0;

        for (int i = 0; i < 4; i++) {
            if (type.equals(flip3Array[i])) {
                index = i;
            }
        }

        return flip3Array[((index - quad) + 4) % 4];
    }

    private static ContainsType flip4(ContainsType type, int quad) {
        int index = 0;

        for (int i = 0; i < 4; i++) {
            if (type.equals(flip3Array[i])) {
                index = i;
            }
        }

        return flip4Array[((index - quad) + 4) % 4];
    }

    /**
     * Prevent instance creation
     */
    private R2() {
    }
}
