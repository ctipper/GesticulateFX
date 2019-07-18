/**
 * Dropper.java
 * 
 * Created on 17-Jul-2019 14:41:16
 * 
 */
package net.perspective.draw;

import java.awt.BasicStroke;
import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 * @author Christopher G D Tipper
 */

@Singleton
public class Dropper {

    java.util.List<Float> strokeTypes = Arrays.asList(1.0f, 1.5f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 8.0f, 10.0f);
    java.util.List<java.util.List<Float>> dashes1 = Arrays.asList(
            Arrays.asList(1.5f, 1.5f), Arrays.asList(1.5f, 1.5f), Arrays.asList(2.0f, 2.0f),
            Arrays.asList(3.0f, 3.0f), Arrays.asList(4.0f, 4.0f), Arrays.asList(5.0f, 5.0f),
            Arrays.asList(6.0f, 6.0f), Arrays.asList(8.0f, 8.0f), Arrays.asList(10.0f, 10.0f));
    java.util.List<java.util.List<Float>> dashes2 = Arrays.asList(
            Arrays.asList(2.0f, 2.0f), Arrays.asList(3.0f, 3.0f), Arrays.asList(4.0f, 4.0f),
            Arrays.asList(5.0f, 5.0f), Arrays.asList(6.0f, 6.0f), Arrays.asList(8.0f, 8.0f),
            Arrays.asList(10.0f, 10.0f), Arrays.asList(12.0f, 12.0f), Arrays.asList(14.0f, 14.0f));
    java.util.List<java.util.List<Float>> dashes3 = Arrays.asList(
            Arrays.asList(4.0f, 4.0f), Arrays.asList(6.0f, 6.0f), Arrays.asList(8.0f, 8.0f),
            Arrays.asList(10.0f, 10.0f), Arrays.asList(12.0f, 12.0f), Arrays.asList(14.0f, 14.0f),
            Arrays.asList(16.0f, 16.0f), Arrays.asList(18.0f, 18.0f), Arrays.asList(20.0f, 20.0f));
    java.util.List<java.util.List<Float>> dashes4 = Arrays.asList(Arrays.asList(3.0f, 2.0f, 2.0f, 2.0f),
            Arrays.asList(5.0f, 3.0f, 3.0f, 3.0f),
            Arrays.asList(8.0f, 4.0f, 4.0f, 4.0f),
            Arrays.asList(10.0f, 5.0f, 5.0f, 5.0f),
            Arrays.asList(12.0f, 6.0f, 6.0f, 6.0f),
            Arrays.asList(16.0f, 8.0f, 8.0f, 8.0f),
            Arrays.asList(18.0f, 10.0f, 10.0f, 10.0f),
            Arrays.asList(24.0f, 12.0f, 12.0f, 12.0f),
            Arrays.asList(28.0f, 14.0f, 14.0f, 14.0f));

    /**
     * Creates a new instance of <code>Dropper</code>
     */
    @Inject
    public Dropper() {
    }

    protected BasicStroke selectStroke(Integer strokeId, String strokeStyle) {
        BasicStroke stroke;
        switch (strokeStyle) {
            case "style1": // Plain stroke
                stroke = new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                break;
            case "style2":
                stroke = new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, getDashes(dashes1.get(strokeId)), 0.0f);
                break;
            case "style3":
                stroke = new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, getDashes(dashes2.get(strokeId)), 0.0f);
                break;
            case "style4":
                stroke = new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, getDashes(dashes3.get(strokeId)), 0.0f);
                break;
            case "style5":
                stroke = new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, getDashes(dashes4.get(strokeId)), 0.0f);
                break;
            case "style6": // Arrow at start
                stroke = new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                break;
            case "style7": // Arrow at start
                stroke = new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, getDashes(dashes1.get(strokeId)), 0.0f);
                break;
            case "style8": // Arrow at both ends
                stroke = new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                break;
            case "style9": // Arrow at both ends
                stroke = new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, getDashes(dashes1.get(strokeId)), 0.0f);
                break;
            default:
                stroke = new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                break;
        }
        return stroke;
    }

    public int getStrokeIdLinear(BasicStroke stroke) {
        float width = stroke.getLineWidth();
        int i = 0;
        do {
            if (strokeTypes.get(i) > width)
                break;
            i++;
        } while (i < strokeTypes.size());
        return i - 1;
    }

    public int getStrokeIdBinary(BasicStroke stroke) {
        float width = stroke.getLineWidth();
        int j = 0;
        int i = strokeTypes.size();
        int k = (i + j) / 2;
        while (true) {
            if (strokeTypes.get(k) <= width) {
                j = k;
            }
            if (strokeTypes.get(k) > width) {
                i = k;
            }
            if ((i - j) < 2)
                break;
            k = (i + j) / 2;
        }
        return j;
    }

    private float[] getDashes(java.util.List<Float> items) {
        float[] value;
        value = new float[4];

        int i = 0;
        for (Float item : items) {
            value[i] = item;
            i++;
        }
        return value;
    }

}
