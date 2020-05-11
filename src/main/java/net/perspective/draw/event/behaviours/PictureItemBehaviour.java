/*
 * PictureItemBehaviour.java
 * 
 * Created on Oct 13, 2013 1:11:50 PM
 * 
 */
package net.perspective.draw.event.behaviours;

import java.util.List;
import javafx.scene.Cursor;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.enums.ContainsType;
import net.perspective.draw.event.DrawAreaListener;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Picture;
import net.perspective.draw.util.CanvasPoint;
import net.perspective.draw.util.R2;
import net.perspective.draw.util.V2;

/**
 * 
 * @author ctipper
 */

public class PictureItemBehaviour implements ItemBehaviours {

    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    @Inject private DrawAreaListener listener;

    @Override
    public boolean selectItem(BehaviourContext context, DrawItem item, int index) {
        boolean found = false;
        int quad;

        List<CanvasPoint[]> vertices = ((Picture) item).getVertices();
        CanvasPoint centre = item.rotationCentre();
        for (CanvasPoint[] vertex : vertices) {
            if (context.getRegion(vertex[0]).contains(listener.getStartX(), listener.getStartY())) {
                quad = R2.quadrant(vertex[1], centre);
                switch (quad) {
                    case 0:
                        view.setSelected(index);
                        context.setContainment(ContainsType.TR);
                        found = true;
                        break;
                    case 1:
                        view.setSelected(index);
                        context.setContainment(ContainsType.TL);
                        found = true;
                        break;
                    case 2:
                        view.setSelected(index);
                        context.setContainment(ContainsType.BL);
                        found = true;
                        break;
                    case 3:
                        view.setSelected(index);
                        context.setContainment(ContainsType.BR);
                        found = true;
                        break;
                    default:
                        break;
                }
            }
        }
        if (found) {
            context.setSgndArea(((Picture) item).sgnd_area());
        }
        if (!found && item.contains(listener.getStartX(), listener.getStartY())) {
            view.setSelected(index);
            context.setContainment(ContainsType.SHAPE);
            found = true;
        }
        return found;
    }

    @Override
    public void hoverItem(BehaviourContext context, DrawItem item) {
        if (context.getRegion(item.getTop()[0]).contains(listener.getTempX(), listener.getTempY())) {
            drawarea.getScene().setCursor(Cursor.CROSSHAIR);
        } else if (context.getRegion(item.getDown()[0]).contains(listener.getTempX(), listener.getTempY())) {
            drawarea.getScene().setCursor(Cursor.CROSSHAIR);
        } else if (context.getRegion(item.getBottom()[0]).contains(listener.getTempX(), listener.getTempY())) {
            drawarea.getScene().setCursor(Cursor.CROSSHAIR);
        } else if (context.getRegion(item.getUp()[0]).contains(listener.getTempX(), listener.getTempY())) {
            drawarea.getScene().setCursor(Cursor.CROSSHAIR);
        } else if (item.contains(listener.getTempX(), listener.getTempY())) {
            drawarea.getScene().getRoot().setCursor(Cursor.OPEN_HAND);
        } else {
            drawarea.getScene().setCursor(Cursor.DEFAULT);
        }
    }

    @Override
    public void alterItem(BehaviourContext context, DrawItem item, double xinc, double yinc) {
        ContainsType contains;
        CanvasPoint s, e, end;
        double sgn;

        CanvasPoint st = item.getStart();
        CanvasPoint en = item.getEnd();
        double scale = ((Picture) item).getScale();

        /**
         * Permute containment selectors
         */
        if (!context.getContainment().equals(ContainsType.SHAPE)
            && !context.getContainment().equals(ContainsType.NONE)) {
            if (context.getContains().equals(ContainsType.NONE)) {
                context.setContains(R2.permute(context.getContainment(), R2.quadrant(st, item.rotationCentre())));
            }
            contains = context.getContains();
        } else {
            contains = context.getContainment();
        }

        /**
         * Adjust for quadrant of TL vertex
         */
        if (context.getQuad() == -1) {
            int quad = R2.quadrant(item.getTop()[1], item.rotationCentre());
            context.setQuad(quad);
        }

        // reproduce unrotated actual coordinates
        s = new CanvasPoint(st.x, st.y);
        e = new CanvasPoint(st.x + en.x * scale, st.y + en.y * scale);

        // retrieve increment correctors
        int[] flip = R2.flip(context.getQuad());
        int cos_t = flip[0];
        int sin_t = flip[1];
        int csx_t = flip[2];
        int csy_t = flip[3];

        // correct increment for angle of rotation
        @SuppressWarnings("deprecation")
        double t = item.getAngle() + (item.isVertical() ? -Math.PI / 2 : 0);
        double delta = V2.norm_angle(4 * t + 2 * Math.PI) / 2;
        CanvasPoint inc = V2.rot(xinc, yinc, t - delta);

        // correct increment for negative scale factor
        if (context.getSgndArea() >= 0) {
            inc.x = -inc.x;
            inc.y = -inc.y;
        }

        // correct increment for picture aspect ratio
        double cni = Math.max(Math.abs(inc.x), Math.abs(inc.y));
        double sgnx = Math.signum(inc.x);
        double sgny = Math.signum(inc.y);

        inc.x = sgnx * cni;
        inc.y = sgny * cni * en.y / en.x;

        boolean sgnD = sgnx == sgny;

        switch (contains) {
            case TL:
                if ((context.getQuad() == 0 || context.getQuad() == 2)) {
                    s.translate((cos_t - sin_t) * inc.x, (cos_t + sin_t) * (sgnD ? -sgnx * Math.abs(inc.y) : inc.y));
                    e.translate((-cos_t + sin_t + csy_t) * inc.x, (-cos_t - sin_t + csx_t) * inc.y);
                } else {
                    s.translate((cos_t - sin_t) * (sgnD ? inc.x : -sgnx * Math.abs(inc.x)), (cos_t + sin_t) * inc.y);
                    e.translate((-cos_t + sin_t + csy_t) * inc.x, (-cos_t - sin_t + csx_t) * inc.y);
                }
                item.setStart(s.x, s.y);
                sgn = Math.signum(e.x - s.x);
                end = new CanvasPoint(e.x - s.x, e.y - s.y);
                ((Picture) item).setScale(V2.L2(end) * sgn / V2.L2(en));
                break;
            case BL:
                if ((context.getQuad() == 0 || context.getQuad() == 2)) {
                    s.translate((cos_t + sin_t) * (sgnD ? inc.x : -sgnx * Math.abs(inc.x)), (-cos_t + sin_t + csy_t) * inc.y);
                    e.translate((-cos_t - sin_t + csx_t) * inc.x, (cos_t - sin_t) * inc.y);
                } else {
                    s.translate((cos_t + sin_t) * (sgnD ? -sgnx * Math.abs(inc.x) : inc.x), (-cos_t + sin_t + csy_t) * inc.y);
                    e.translate((-cos_t - sin_t + csx_t) * inc.x, (cos_t - sin_t) * (sgnD ? sgnx * Math.abs(inc.y) : inc.y));
                }
                item.setStart(s.x, s.y);
                sgn = Math.signum(e.y - s.y);
                end = new CanvasPoint(e.x - s.x, e.y - s.y);
                ((Picture) item).setScale(V2.L2(end) * sgn / V2.L2(en));
                break;
            case BR:
                if ((context.getQuad() == 0 || context.getQuad() == 2)) {
                    s.translate((-cos_t + sin_t + csy_t) * inc.x, (-cos_t - sin_t + csx_t) * inc.y);
                    e.translate((cos_t - sin_t) * inc.x, (cos_t + sin_t) * (sgnD ? -sgnx * Math.abs(inc.y) : inc.y));
                } else {
                    s.translate((-cos_t + sin_t + csy_t) * inc.x, (-cos_t - sin_t + csx_t) * inc.y);
                    e.translate((cos_t - sin_t) * (sgnD ? inc.x : -sgnx * Math.abs(inc.x)), (cos_t + sin_t) * inc.y);
                }
                item.setStart(s.x, s.y);
                sgn = Math.signum(e.x - s.x);
                end = new CanvasPoint(e.x - s.x, e.y - s.y);
                ((Picture) item).setScale(V2.L2(end) * sgn / V2.L2(en));
                break;
            case TR:
                if ((context.getQuad() == 0 || context.getQuad() == 2)) {
                    s.translate((-cos_t - sin_t + csx_t) * inc.x, (cos_t - sin_t) * (sgnD ? inc.y : sgnx * Math.abs(inc.y)));
                    e.translate((cos_t + sin_t) * inc.x, (-cos_t + sin_t + csy_t) * inc.y);
                } else {
                    s.translate((-cos_t - sin_t + csx_t) * inc.x, (cos_t - sin_t) * (sgnD ? sgnx * Math.abs(inc.y) : inc.y));
                    e.translate((cos_t + sin_t) * (sgnD ? -sgnx * Math.abs(inc.x) : inc.x), (-cos_t + sin_t + csy_t) * inc.y);
                }
                item.setStart(s.x, s.y);
                sgn = Math.signum(e.y - s.y);
                end = new CanvasPoint(e.x - s.x, e.y - s.y);
                ((Picture) item).setScale(V2.L2(end) * sgn / V2.L2(en));
                break;
            case SHAPE:
                drawarea.getScene().getRoot().setCursor(Cursor.CLOSED_HAND);
                item.moveShape(xinc, yinc);
                break;
            case NONE:
            default:
                break;
        }
    }

}
