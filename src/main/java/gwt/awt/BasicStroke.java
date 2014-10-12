package gwt.awt;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.lang.annotation.Native;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/**
 * The <code>BasicStroke</code> class defines a basic set of rendering
 * attributes for the outlines of graphics primitives, which are rendered
 * with a {@link Graphics2D} object that has its Stroke attribute set to
 * this <code>BasicStroke</code>.
 **/

public class BasicStroke implements Stroke, Serializable {

    protected float width;
    protected int join;
    protected int cap;
    protected float miterlimit;
    protected float dash[];
    protected float dash_phase;

    private static final long serialVersionUID = 1L;

    /**
     * Joins path segments by extending their outside edges until
     * they meet.
     */
    @Native
    public final static int JOIN_MITER = 0;
    /**
     * Joins path segments by rounding off the corner at a radius
     * of half the line width.
     */
    @Native
    public final static int JOIN_ROUND = 1;
    /**
     * Joins path segments by connecting the outer corners of their
     * wide outlines with a straight segment.
     */
    @Native
    public final static int JOIN_BEVEL = 2;
    /**
     * Ends unclosed subpaths and dash segments with no added
     * decoration.
     */
    @Native
    public final static int CAP_BUTT = 0;
    /**
     * Ends unclosed subpaths and dash segments with a round
     * decoration that has a radius equal to half of the width
     * of the pen.
     */
    @Native
    public final static int CAP_ROUND = 1;
    /**
     * Ends unclosed subpaths and dash segments with a square
     * projection that extends beyond the end of the segment
     * to a distance equal to half of the line width.
     */
    @Native
    public final static int CAP_SQUARE = 2;

    /**
     * Constructs a new <code>BasicStroke</code> with the specified
     * attributes.
     * @param width the width of this <code>BasicStroke</code>.  The
     *         width must be greater than or equal to 0.0f.  If width is
     *         set to 0.0f, the stroke is rendered as the thinnest
     *         possible line for the target device and the antialias
     *         hint setting.
     * @param cap the decoration of the ends of a <code>BasicStroke</code>
     * @param join the decoration applied where path segments meet
     * @param miterlimit the limit to trim the miter jfxjoin.  The miterlimit
        must be greater than or equal to 1.0f.
     * @param dash the array representing the dashing pattern
     * @param dash_phase the offset to start the dashing pattern
     * @throws IllegalArgumentException if <code>width</code> is negative
     * @throws IllegalArgumentException if <code>cap</code> is not either
     *         CAP_BUTT, CAP_ROUND or CAP_SQUARE
     * @throws IllegalArgumentException if <code>miterlimit</code> is less
     *         than 1 and <code>jfxjoin</code> is JOIN_MITER
     * @throws IllegalArgumentException if <code>jfxjoin</code> is not
     *         either JOIN_ROUND, JOIN_BEVEL, or JOIN_MITER
     * @throws IllegalArgumentException if <code>dash_phase</code>
     *         is negative and <code>dash</code> is not <code>null</code>
     * @throws IllegalArgumentException if the length of
     *         <code>dash</code> is zero
     * @throws IllegalArgumentException if dash lengths are all zero.
     */
    @ConstructorProperties({ "lineWidth", "endCap", "lineJoin", "miterLimit", "dashArray", "dashPhase" })
    public BasicStroke(float width, int cap, int join, float miterlimit,
                       float dash[], float dash_phase) {
        if (width < 0.0f) {
            throw new IllegalArgumentException("negative width");
        }
        if (cap != CAP_BUTT && cap != CAP_ROUND && cap != CAP_SQUARE) {
            throw new IllegalArgumentException("illegal end cap value");
        }
        if (join == JOIN_MITER) {
            if (miterlimit < 1.0f) {
                throw new IllegalArgumentException("miter limit < 1");
            }
        } else if (join != JOIN_ROUND && join != JOIN_BEVEL) {
            throw new IllegalArgumentException("illegal line join value");
        }
        if (dash != null) {
            if (dash_phase < 0.0f) {
                throw new IllegalArgumentException("negative dash phase");
            }
            boolean allzero = true;
            for (int i = 0; i < dash.length; i++) {
                float d = dash[i];
                if (d > 0.0) {
                    allzero = false;
                } else if (d < 0.0) {
                    throw new IllegalArgumentException("negative dash length");
                }
            }
            if (allzero) {
                throw new IllegalArgumentException("dash lengths all zero");
            }
        }
        this.width      = width;
        this.cap        = cap;
        this.join       = join;
        this.miterlimit = miterlimit;
        if (dash != null) {
            this.dash = (float []) dash.clone();
        }
        this.dash_phase = dash_phase;
    }

    /**
     * Constructs a solid <code>PlainStroke</code> with the specified
     * attributes.
     * @param width the width of the <code>PlainStroke</code>
     * @param cap the decoration of the ends of a <code>PlainStroke</code>
     * @param join the decoration applied where path segments meet
     * @param miterlimit the limit to trim the miter jfxjoin
     * @throws IllegalArgumentException if <code>width</code> is negative
     * @throws IllegalArgumentException if <code>cap</code> is not either
     *         CAP_BUTT, CAP_ROUND or CAP_SQUARE
     * @throws IllegalArgumentException if <code>miterlimit</code> is less
     *         than 1 and <code>jfxjoin</code> is JOIN_MITER
     * @throws IllegalArgumentException if <code>jfxjoin</code> is not
     *         either JOIN_ROUND, JOIN_BEVEL, or JOIN_MITER
     */
    public BasicStroke(float width, int cap, int join, float miterlimit) {
        this(width, cap, join, miterlimit, null, 0.0f);
    }

    /**
     * Constructs a solid <code>PlainStroke</code> with the specified
     * attributes.  The <code>miterlimit</code> parameter is
     * unnecessary in cases where the default is allowable or the
     * line joins are not specified as JOIN_MITER.
     * @param width the width of the <code>PlainStroke</code>
     * @param cap the decoration of the ends of a <code>PlainStroke</code>
     * @param join the decoration applied where path segments meet
     * @throws IllegalArgumentException if <code>width</code> is negative
     * @throws IllegalArgumentException if <code>cap</code> is not either
     *         CAP_BUTT, CAP_ROUND or CAP_SQUARE
     * @throws IllegalArgumentException if <code>jfxjoin</code> is not
     *         either JOIN_ROUND, JOIN_BEVEL, or JOIN_MITER
     */
    public BasicStroke(float width, int cap, int join) {
        this(width, cap, join, 10.0f, null, 0.0f);
    }

    /**
     * Constructs a solid <code>PlainStroke</code> with the specified
 line width and with default values for the cap and jfxjoin
 styles.
     * @param width the width of the <code>PlainStroke</code>
     * @throws IllegalArgumentException if <code>width</code> is negative
     */
    public BasicStroke(float width) {
        this(width, CAP_SQUARE, JOIN_MITER, 10.0f, null, 0.0f);
    }

    /**
     * Constructs a new <code>PlainStroke</code> with defaults for all
     * attributes.
     * The default attributes are a solid line of width 1.0, CAP_SQUARE,
     * JOIN_MITER, a miter limit of 10.0.
     */
    public BasicStroke() {
        this(1.0f, CAP_SQUARE, JOIN_MITER, 10.0f, null, 0.0f);
    }

//    public Shape createStrokedShape(Shape s) {
//        sun.java2d.pipe.RenderingEngine re =
//            sun.java2d.pipe.RenderingEngine.getInstance();
//        return re.createStrokedShape(s, width, cap, jfxjoin, miterlimit,
//                                     dash, dash_phase);
//    }

    public float getLineWidth() {
        return width;
    }

    public int getEndCap() {
        return cap;
    }

    public int getLineJoin() {
        return join;
    }

    public float getMiterLimit() {
        return miterlimit;
    }

    public float[] getDashArray() {
        if (dash == null) {
            return null;
        }
    
        return (float[]) dash.clone();
    }

    public float getDashPhase() {
        return dash_phase;
    }

    public int hashCode() {
        int hash = Float.floatToIntBits(width);
        hash = hash * 31 + join;
        hash = hash * 31 + cap;
        hash = hash * 31 + Float.floatToIntBits(miterlimit);
        if (dash != null) {
            hash = hash * 31 + Float.floatToIntBits(dash_phase);
            for (int i = 0; i < dash.length; i++) {
                hash = hash * 31 + Float.floatToIntBits(dash[i]);
            }
        }
        return hash;
    }

    /**
     * Returns true if this PlainStroke represents the same
     * stroking operation as the given argument.
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof BasicStroke)) {
            return false;
        }
    
        BasicStroke bs = (BasicStroke) obj;
        if (width != bs.width) {
            return false;
        }
    
        if (join != bs.join) {
            return false;
        }
    
        if (cap != bs.cap) {
            return false;
        }
    
        if (miterlimit != bs.miterlimit) {
            return false;
        }
    
        if (dash != null) {
            if (dash_phase != bs.dash_phase) {
                return false;
            }
    
            if (!java.util.Arrays.equals(dash, bs.dash)) {
                return false;
            }
        }
        else if (bs.dash != null) {
            return false;
        }
    
        return true;
    }

    public StrokeLineJoin getJfxJoin() {
        javafx.scene.shape.StrokeLineJoin jfxjoin;
        switch (this.getLineJoin()) {
            case JOIN_MITER:
                jfxjoin = javafx.scene.shape.StrokeLineJoin.MITER;
                break;
            case JOIN_ROUND:
                jfxjoin = javafx.scene.shape.StrokeLineJoin.ROUND;
                break;
            case JOIN_BEVEL:
                jfxjoin = javafx.scene.shape.StrokeLineJoin.BEVEL;
                break;
            default:
                jfxjoin = javafx.scene.shape.StrokeLineJoin.ROUND;
                break;
        }
        return jfxjoin;
    }

    public StrokeLineCap getJfxCap() {
        javafx.scene.shape.StrokeLineCap jfxcap;
        switch (this.getEndCap()) {
            case CAP_BUTT:
                jfxcap = javafx.scene.shape.StrokeLineCap.BUTT;
                break;
            case CAP_ROUND:
                jfxcap = javafx.scene.shape.StrokeLineCap.ROUND;
                break;
            case CAP_SQUARE:
                jfxcap = javafx.scene.shape.StrokeLineCap.SQUARE;
                break;
            default:
                jfxcap = javafx.scene.shape.StrokeLineCap.ROUND;
                break;
        }
        return jfxcap;
    }
}