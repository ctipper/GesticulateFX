/*
 * SelectionHandler.java
 * 
 * Created on Oct 19, 2013 8:20:36 PM
 * 
 */

/**
 * Copyright (c) 2023 Christopher Tipper
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
package net.perspective.draw.event;

import com.google.inject.Injector;
import java.awt.BasicStroke;
import java.util.ArrayList;
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
import net.perspective.draw.event.behaviours.MapItemBehaviour;
import net.perspective.draw.event.behaviours.PictureItemBehaviour;
import net.perspective.draw.event.behaviours.TextItemBehaviour;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.geom.FigureFactory;
import net.perspective.draw.geom.Grouped;
import net.perspective.draw.geom.Picture;
import net.perspective.draw.geom.StreetMap;
import net.perspective.draw.geom.Text;
import net.perspective.draw.util.CanvasPoint;

/**
 * 
 * @author ctipper
 */

public class SelectionHandler implements Handler {

    @Inject private Injector injector;
    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    @Inject private ApplicationController controller;
    @Inject private DrawAreaListener listener;
    @Inject private BehaviourContext context;
    @Inject private FigureFactory figurefactory;
    private List<Double> coordsX, coordsY;
    private List<Double> midX, midY;

    // Following fields apply to marquee
    private static final BasicStroke marqueeStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Color marqueeColor = Color.rgb(204, 102, 255);         // Light blue
    private static final Color fillColor = Color.rgb(48, 96, 255);              // Dark blue

    /**
     * Creates a new instance of <code>SelectionHandler</code> 
     */
    public SelectionHandler() {
    }

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
            view.moveSelection(view.getSelected());
            context.resetContainment();
        }
        view.setGuides(false);
        drawarea.resetGuides();
    }

    @Override
    public void downEvent() {
        List<DrawItem> drawings = view.getDrawings();
        if (view.isEditing()) {
            // Text isEditing code here
            if (!listener.getRightClick()) {
                DrawItem item = drawings.get(view.getSelected());
                context.setBehaviour(injector.getInstance(TextItemBehaviour.class));
                context.select(item, 0);
            }
        } else {
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
                        if (item instanceof StreetMap) {
                            context.setBehaviour(injector.getInstance(MapItemBehaviour.class));
                            boolean found = context.select(item, i);
                            if (found) {
                                break;
                            }
                        } else {
                            context.setBehaviour(injector.getInstance(PictureItemBehaviour.class));
                            boolean found = context.select(item, i);
                            if (found) {
                                break;
                            }
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
        if (view.getSelected() != -1 && listener.isSnapEnabled()) {
            CanvasPoint start = drawings.get(view.getSelected()).getStart();
            context.setOmega(start.getX(), start.getY());
        }
        /**
         * setup data structures for guides
         */
        if (view.getSelected() != -1 && !listener.isSnapEnabled() && drawarea.isGuideEnabled() && drawings.size() < 15) {
            coordsX = new ArrayList<>();
            coordsY = new ArrayList<>();
            midX = new ArrayList<>();
            midY = new ArrayList<>();
            DrawItem item = drawings.get(view.getSelected());
            for (var drawing : drawings) {
                if (drawings.indexOf(drawing) != view.getSelected()) {
                    if (!item.bounds().intersects(drawing.bounds().getBounds2D())) {
                        computeCoords(drawing);
                    }
                }
            }
        }
    }

    @Override
    public void clickEvent() {
        if (listener.doubleClicked()) {
            List<DrawItem> drawings = view.getDrawings();
            if (!drawings.isEmpty()) {
                int i = drawings.size() - 1;
                do {
                    DrawItem item = drawings.get(i);
                    if (item.contains(listener.getTempX(), listener.getTempY())) {
                        if (item instanceof Text) {
                            context.setBehaviour(injector.getInstance(TextItemBehaviour.class));
                            context.edit(item, i);
                            break;
                        } else if (item instanceof StreetMap) {
                            context.setBehaviour(injector.getInstance(MapItemBehaviour.class));
                            context.edit(item, i);
                            break;
                        }
                    }
                    i--;
                } while (i >= 0);
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
            } else if (item instanceof Text) {
                context.setBehaviour(injector.getInstance(TextItemBehaviour.class));
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

                if (listener.isSnapEnabled()) {
                    context.setOmega(context.getOmega().getX() + xinc, context.getOmega().getY() + yinc);
                } else if (drawarea.isGuideEnabled() && view.getDrawings().size() < 15) {
                    drawarea.resetGuides();
                    // compute bounds X
                    Double topX = item.getTop()[0].getX();
                    Double upX = item.getUp()[0].getX();
                    Double downX = item.getDown()[0].getX();
                    Double botX = item.getBottom()[0].getX();
                    // compute min max
                    Double minx = minimum(topX, upX, downX, botX);
                    Double maxx = maximum(topX, upX, downX, botX);
                    // compute mid
                    Double midx = (minx + maxx) / 2;
                    // compute bounds Y
                    Double topY = item.getTop()[0].getY();
                    Double upY = item.getUp()[0].getY();
                    Double downY = item.getDown()[0].getY();
                    Double botY = item.getBottom()[0].getY();
                    // compute min max
                    Double miny = minimum(topY, upY, downY, botY);
                    Double maxy = maximum(topY, upY, downY, botY);
                    // compute mid
                    Double midy = (miny + maxy) / 2;
                    boolean added = false;
                    for (var coord : coordsX) {
                        if (minx.intValue() == coord.intValue()) {
                            drawarea.addGuide(false, minx);
                            added = true;
                        }
                        if (maxx.intValue() == coord.intValue()) {
                            drawarea.addGuide(false, maxx);
                            added = true;
                        }
                    }
                    int i = 0;
                    for (var coord : coordsY) {
                        if (miny.intValue() == coord.intValue()) {
                            drawarea.addGuide(true, miny);
                            added = true;
                        }
                        if (maxy.intValue() == coord.intValue()) {
                            drawarea.addGuide(true, maxy);
                            added = true;
                        }
                    }
                    for (var coord : midX) {
                        if (midx.intValue() == coord.intValue()) {
                            drawarea.addGuide(false, midx);
                            added = true;
                        }
                    }
                    for (var coord : midY) {
                        if (midy.intValue() == coord.intValue()) {
                            drawarea.addGuide(true, midy);
                            added = true;
                        }
                    }
                    view.setGuides(added);
                }
                if (item instanceof Text) {
                    if (!view.isEditing()) {
                        if (listener.isSnapEnabled()) {
                            xinc = context.getOmega().getX() - item.getStart().getX();
                            yinc = context.getOmega().getY() - item.getStart().getY();
                            drawarea.moveToWithIncrements(item, xinc, yinc);
                        } else {
                            item.moveTo(xinc, yinc);
                        }
                    } else {
                        context.setBehaviour(new TextItemBehaviour());
                        context.alter(item, 0, 0);
                    }
                } else if (item instanceof Figure) {
                    context.setBehaviour(injector.getInstance(FigureItemBehaviour.class));
                    context.alter(item, xinc, yinc);
                } else if (item instanceof Picture) {
                    if (item instanceof StreetMap) {
                        context.setBehaviour(injector.getInstance(MapItemBehaviour.class));
                        context.alter(item, xinc, yinc);
                    } else {
                        context.setBehaviour(injector.getInstance(PictureItemBehaviour.class));
                        context.alter(item, xinc, yinc);
                    }
                } else if (item instanceof Grouped) {
                    context.setBehaviour(injector.getInstance(GroupedItemBehaviour.class));
                    context.alter(item, xinc, yinc);
                } else {
                    // Rest of shapes
                    if (listener.isSnapEnabled()) {
                        xinc = context.getOmega().getX() - item.getStart().getX();
                        yinc = context.getOmega().getY() - item.getStart().getY();
                        drawarea.moveToWithIncrements(item, xinc, yinc);
                    } else {
                        item.moveTo(xinc, yinc);
                    }
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

    @Override
    public void zoomEvent() {
    }

    private void computeCoords(DrawItem item) {
        // compute bounds X
        Double topX = item.getTop()[0].getX();
        Double upX = item.getUp()[0].getX();
        Double downX = item.getDown()[0].getX();
        Double botX = item.getBottom()[0].getX();
        // compute min mid max
        Double minx = minimum(topX, upX, downX, botX);
        coordsX.add(minx);
        Double maxx = maximum(topX, upX, downX, botX);
        coordsX.add(maxx);
        // compute mid
        midX.add((minx + maxx) / 2);
        // compute bounds Y
        Double topY = item.getTop()[0].getY();
        Double upY = item.getUp()[0].getY();
        Double downY = item.getDown()[0].getY();
        Double botY = item.getBottom()[0].getY();
        // compute min mid max
        Double miny = minimum(topY, upY, downY, botY);
        coordsY.add(miny);
        Double maxy = maximum(topY, upY, downY, botY);
        coordsY.add(maxy);
        // compute mid
        midY.add((miny + maxy) / 2);
    }

    private Double minimum(Double a, Double b, Double c, Double d) {
        Double min1 = Math.min(a, b);
        Double min2 = Math.min(c, d);
        Double min3 = Math.min(min1, min2);
        return min3;
    }

     private Double maximum(Double a, Double b, Double c, Double d) {
         Double max1 = Math.max(a, b);
         Double max2 = Math.max(c, d);
         Double max3 = Math.max(max1, max2);
         return max3;
     }

}
