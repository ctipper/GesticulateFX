/*
 * SelectionHandler.java
 * 
 * Created on Oct 19, 2013 8:20:36 PM
 * 
 */
package net.perspective.draw.event;

import com.google.inject.Injector;
import java.awt.BasicStroke;
import java.util.List;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javax.inject.Inject;
import net.perspective.draw.ApplicationController;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.enums.ContainsType;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.event.behaviours.BehaviourContext;
import net.perspective.draw.event.behaviours.FigureItemBehaviour;
import net.perspective.draw.event.behaviours.GroupedItemBehaviour;
import net.perspective.draw.event.behaviours.PictureItemBehaviour;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.geom.FigureFactory;
import net.perspective.draw.geom.Grouped;
import net.perspective.draw.geom.Picture;

/**
 * 
 * @author ctipper
 */

public class SelectionHandler implements Handler {

    @Inject Injector injector;
    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    @Inject private ApplicationController controller;
    @Inject private DrawAreaListener listener;
    @Inject private BehaviourContext context;
    @Inject private FigureFactory figurefactory;

    // Following fields apply to marquee
    private static final BasicStroke marqueeStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Color marqueeColor = Color.rgb(204, 102, 255);         // Light blue
    private static final Color fillColor = Color.rgb(48, 96, 255);              // Dark blue

    @Override
    public void upEvent() {
        if (view.isMarquee()) {
            view.setMarquee(false);
            drawarea.setMultiSelectEnabled(true);
            view.selectShapes(drawarea.getMarquee());
            drawarea.setMultiSelectEnabled(false);
        }
        if (view.getSelected() != -1 && !listener.getRightClick()) {
            view.updateSelectedItem();
            context.resetContainment();
        }
    }

    @Override
    public void downEvent() {
        List<DrawItem> drawings = view.getDrawings();
        if (!drawings.isEmpty() && !listener.getRightClick()) {
            int i = drawings.size() - 1;
            context.setContainment(ContainsType.NONE);
            do {
                DrawItem item = drawings.get(i);
                if (item instanceof Figure) {
                    context.setBehaviour(injector.getInstance(FigureItemBehaviour.class));
                    boolean found = context.select(item, i);
                    if (found) {
                        break;
                    }
                } else if (item instanceof Picture) {
                    context.setBehaviour(injector.getInstance(PictureItemBehaviour.class));
                    boolean found = context.select(item, i);
                    if (found) {
                        break;
                    }
                } else if (item.contains(listener.getStartX(), listener.getStartY())) {
                    // Rest of Shapes
                    view.setSelected(i);
                    context.setContainment(ContainsType.SHAPE);
                    break;
                }
                i--;
            } while (i >= 0);
            if (context.getContainment().equals(ContainsType.NONE) && (!view.isMultiSelected() || !drawarea.isMultiSelectEnabled())) {
                view.setSelected(-1);
            }
        }
    }

    @Override
    public void hoverEvent() {
        if (view.getSelected() != -1) {
            DrawItem item = view.getDrawings().get(view.getSelected());
            if (item instanceof Figure) {
                context.setBehaviour(injector.getInstance(FigureItemBehaviour.class));
                context.hover(item);
            } else if (item instanceof Picture) {
                context.setBehaviour(injector.getInstance(PictureItemBehaviour.class));
                context.hover(item);
            } else if (item instanceof Grouped) {
                context.setBehaviour(injector.getInstance(GroupedItemBehaviour.class));
                context.hover(item);
            }
        } else {
            if (controller.getDropperDisabled()) {
                drawarea.getScene().setCursor(Cursor.DEFAULT);
            } else {
                drawarea.getScene().setCursor(Cursor.HAND);
            }
        }
    }

    @Override
    public void dragEvent() {
        if (view.getSelected() != -1) {
            double xinc = listener.getTempX() - listener.getStartX();
            double yinc = listener.getTempY() - listener.getStartY();
            for (Integer selection : view.getMultiSelection()) {
                DrawItem item = view.getDrawings().get(selection);
                if (item instanceof Figure) {
                    context.setBehaviour(injector.getInstance(FigureItemBehaviour.class));
                    context.alter(item, xinc, yinc);
                } else if (item instanceof Picture) {
                    context.setBehaviour(injector.getInstance(PictureItemBehaviour.class));
                    context.alter(item, xinc, yinc);
                } else if (item instanceof Grouped) {
                    context.setBehaviour(injector.getInstance(GroupedItemBehaviour.class));
                    context.alter(item, xinc, yinc);
                } else {
                    // Rest of shapes
                    item.moveShape(xinc, yinc);
                }
                item.updateProperties(drawarea);
                view.updateCanvasItem(selection, item);
                view.moveSelection(selection);
            }
            listener.setStartX(listener.getTempX());
            listener.setStartY(listener.getTempY());
        } else {
            Figure marquee = figurefactory.createFigure(DrawingType.RECTANGLE);
            marquee.setStart(listener.getStartX(), listener.getStartY());
            marquee.setEnd(listener.getTempX(), listener.getTempY());
            marquee.setPoints(DrawingType.RECTANGLE);
            marquee.setEndPoints();
            marquee.setPath();
            marquee.setStroke(marqueeStroke);
            marquee.setColor(marqueeColor);
            marquee.setFillColor(fillColor);
            marquee.setTransparency(25);
            drawarea.setMarquee(marquee);
            // draw Marquee
            view.setMarquee(true);
        }
    }

}
