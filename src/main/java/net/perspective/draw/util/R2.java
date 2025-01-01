/*
 * R2.java
 * 
 * Created on Nov 16, 2014 11:39:22 AM
 * 
 */

/**
 * Copyright (c) 2025 Christopher Tipper
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
package net.perspective.draw.util;

import net.perspective.draw.enums.ContainsType;

/**
 * 
 * @author ctipper
 */

public class R2 {

    /**
     * vertex transformers
     */

    static ContainsType[] flip0Array = { ContainsType.TL, ContainsType.BR, ContainsType.BL, ContainsType.TR };

    static ContainsType[] flip1Array = { ContainsType.TR, ContainsType.BL, ContainsType.BR, ContainsType.TL };

    static ContainsType[] flip2Array = { ContainsType.BR, ContainsType.BL, ContainsType.TR, ContainsType.TL };

    static ContainsType[] flip3Array = { ContainsType.TL, ContainsType.BR, ContainsType.BL, ContainsType.TR };

    static ContainsType[] flip4Array = { ContainsType.BL, ContainsType.BR, ContainsType.TL, ContainsType.TR };

    /**
     * edge transformers
     */

    static ContainsType[] swap0Array = { ContainsType.TT, ContainsType.LL, ContainsType.BB, ContainsType.RR};

    static ContainsType[] swap1Array = { ContainsType.TT, ContainsType.RR, ContainsType.BB, ContainsType.LL };

    static ContainsType[] swap2Array = { ContainsType.LL, ContainsType.BB, ContainsType.RR, ContainsType.TT };

    static ContainsType[] swap3Array = { ContainsType.TT, ContainsType.RR, ContainsType.BB, ContainsType.LL};

    static ContainsType[] swap4Array = { ContainsType.LL, ContainsType.BB, ContainsType.RR, ContainsType.TT };

    /**
     * orientation increments the constants are
     * aligned with { cos_t, sin_t, csx_t, csy_t }
     * 
     * @see net.perspective.draw.event.behaviour.FigureItemBehaviour.java#alterItem()
     * also @see net.perspective.draw.event.behaviour.PictureItemBehaviour.java#alterItem()
     */

    static int[] flip1 = {  0,  1,  1, -1 };

    static int[] flip2 = {  1,  0,  1,  1 };

    static int[] flip3 = {  0, -1, -1,  1 };

    static int[] flip4 = { -1,  0, -1, -1 };

    /**
     * Determine vertex quadrant
     * 
     * @param vertex the vertex
     * @param centre the centre point
     * @return an integer index, see {@link net.perspective.draw.util.V2#declination(double, double)}
     */
    public static int quadrant(CanvasPoint vertex, CanvasPoint centre) {
        double theta = V2.declination(vertex.x - centre.x, vertex.y - centre.y);
        return V2.quadrant(theta);
    }

    /**
     * Determine edge quarter
     * 
     * @param edge the edge
     * @param centre the centre point
     * @return an integer index, see {@link net.perspective.draw.util.V2#declination(double, double)}
     */
    public static int quarter(CanvasPoint edge, CanvasPoint centre) {
        double theta = V2.declination(edge.x - centre.x, edge.y - centre.y);
        return V2.side(theta);
    }

    /**
     * Determine vertex octant
     * 
     * @param vertex the vertex
     * @param centre the centre point
     * @return an integer index, see {@link net.perspective.draw.util.V2#declination(double, double)}
     */
    public static int octant(CanvasPoint vertex, CanvasPoint centre) {
        double theta = V2.declination(vertex.x - centre.x, vertex.y - centre.y);
        return V2.octet(theta);
    }

    /**
     * Permute vertex labels
     * 
     * @param contains a {@link net.perspective.draw.enums.ContainsType}
     * @param quad quadrant
     * @return a {@link net.perspective.draw.enums.ContainsType}e
     */
    public static ContainsType permute(ContainsType contains, int quad) {
        contains = switch (quad) {
            case 0 -> flip1(contains, quad);
            case 1 -> flip2(contains, quad);
            case 2 -> flip3(contains, quad);
            case 3 -> flip4(contains, quad);
            default -> ContainsType.NONE;
        };
        return contains;
    }

    /**
     * Mutate edge labels
     * 
     * @param contains a {@link net.perspective.draw.enums.ContainsType}
     * @param quad quadrant
     * @return a {@link net.perspective.draw.enums.ContainsType}
     */
    public static ContainsType mutate(ContainsType contains, int quad) {
        contains = switch (quad) {
            case 0 -> swap1(contains, quad);
            case 1 -> swap2(contains, quad);
            case 2 -> swap3(contains, quad);
            case 3 -> swap4(contains, quad);
            default -> ContainsType.NONE;
        };
        return contains;
    }

    /**
     * Retrieve TL coordinate transforms
     * 
     * @param quad quadrant
     * @return a numeric pair
     */
    public static int[] flip(int quad) {
        int[] flip;
        flip = switch (quad) {
            case 0 -> flip1;
            case 1 -> flip2;
            case 2 -> flip3;
            case 3 -> flip4;
            default -> flip3;
        };
        return flip;
    }

    private static ContainsType flip1(ContainsType type, int quad) {
        int index = 0;

        for (int i = 0; i < 4; i++) {
            if (type.equals(flip0Array[i])) {
                index = i;
            }
        }

        return flip1Array[((index - quad) + 4) % 4];
    }

    private static ContainsType flip2(ContainsType type, int quad) {
        int index = 0;

        for (int i = 0; i < 4; i++) {
            if (type.equals(flip0Array[i])) {
                index = i;
            }
        }

        return flip2Array[((index - quad) + 4) % 4];
    }

    private static ContainsType flip3(ContainsType type, int quad) {
        int index = 0;

        for (int i = 0; i < 4; i++) {
            if (type.equals(flip0Array[i])) {
                index = i;
            }
        }

        return flip3Array[((index - quad) + 4) % 4];
    }

    private static ContainsType flip4(ContainsType type, int quad) {
        int index = 0;

        for (int i = 0; i < 4; i++) {
            if (type.equals(flip0Array[i])) {
                index = i;
            }
        }

        return flip4Array[((index - quad) + 4) % 4];
    }

    private static ContainsType swap1(ContainsType type, int quad) {
        int index = 0;

        for (int i = 0; i < 4; i++) {
            if (type.equals(swap0Array[i])) {
                index = i;
            }
        }

        return swap1Array[((index - quad) + 4) % 4];
    }

    private static ContainsType swap2(ContainsType type, int quad) {
        int index = 0;

        for (int i = 0; i < 4; i++) {
            if (type.equals(swap0Array[i])) {
                index = i;
            }
        }

        return swap2Array[((index - quad) + 4) % 4];
    }

    private static ContainsType swap3(ContainsType type, int quad) {
        int index = 0;

        for (int i = 0; i < 4; i++) {
            if (type.equals(swap0Array[i])) {
                index = i;
            }
        }

        return swap3Array[((index - quad) + 4) % 4];
    }

    private static ContainsType swap4(ContainsType type, int quad) {
        int index = 0;

        for (int i = 0; i < 4; i++) {
            if (type.equals(swap0Array[i])) {
                index = i;
            }
        }

        return swap4Array[((index - quad) + 4) % 4];
    }

    /**
     * Prevent instance creation
     */
    private R2() {
    }

}

