/*
 * SketchHandler.java
 * 
 * Created on Oct 27, 2013 10:22:09 PM
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

import javafx.scene.paint.Color;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.geom.ArrowLine;
import net.perspective.draw.geom.ArrowType;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.geom.FigureFactory;
import net.perspective.draw.geom.FigureType;

/**
 * 
 * @author ctipper
 */

public class SketchHandler implements Handler  {

    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    @Inject private DrawAreaListener listener;
    @Inject private FigureFactory figurefactory;

    /**
     * Creates a new instance of <code>SketchHandler</code> 
     */
    @Inject
    public SketchHandler() {
    }

    @Override
    public void upEvent() {
        view.setDrawing(false);
        if (view.getNewItem().isEmpty() || drawarea.getDrawType().isEmpty()) {
            return;
        }
        // add figure to canvas
        Figure item = (Figure) view.getNewItem().get();
        item.setEndPoints();
        item.updateProperties(drawarea);
        view.setNewItem(item);
        view.resetNewItem();
    }

    @Override
    public void downEvent() {
        // Create figure
        Figure item = figurefactory.createFigure(drawarea.getDrawType().orElse(DrawingType.POINT));
        // Initialise stroke and fill
        item.setStroke(drawarea.getPlainStroke());
        item.setColor(Color.web(drawarea.getThemeFillColor()));
        item.setFillColor(Color.web(drawarea.getCanvasBackgroundColor()));
        // Initialise sketch
        item.setPoints(drawarea.getDrawType().orElse(DrawingType.POINT));
        item.addPoint(listener.getStartX(), listener.getStartY());
        item.setEndPoints();
    	item.setPath();
        view.setNewItem(item);
        view.setDrawing(true);
    }

    @Override
    public void clickEvent() {
    }

    @Override
    public void hoverEvent() {
    }

    @Override
    public void dragEvent() {
        // Create Figure
        Figure item = (Figure) view.getNewItem().orElse(null);
        if (item == null) return;
        if (!(item instanceof ArrowLine) && (drawarea.getArrow().equals(ArrowType.END) || drawarea.getArrow().equals(ArrowType.BOTH))) {
            // Create arrow line
            if (!((Figure) item).getType().equals(FigureType.POLYGON)) {
                ArrowLine arrow = new ArrowLine((Figure) item);
                arrow.setArrowStroke(drawarea.getStroke());
                arrow.setArrowType(drawarea.getArrow());
                arrow.setEndPoints();
                item = arrow;
            }
        }        // continue sketch
        item.addPoint(listener.getTempX(), listener.getTempY());
        item.setEndPoints();
        item.setPath();
        view.setNewItem(item);
    }

    @Override
    public void zoomEvent() {

    }

}
