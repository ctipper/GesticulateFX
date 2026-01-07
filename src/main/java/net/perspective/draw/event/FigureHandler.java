/*
 * FigureHandler.java
 * 
 * Created on Oct 19, 2013 8:24:51 PM
 * 
 */

/**
 * Copyright (c) 2026 Christopher Tipper
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

/**
 * 
 * @author ctipper
 */

public class FigureHandler implements Handler {

    private final DrawingArea drawarea;
    private final CanvasView view;
    @Inject FigureFactory figurefactory;
    @Inject DrawAreaListener listener;

    @Inject
    public FigureHandler(DrawingArea drawarea, CanvasView view) {
        this.drawarea = drawarea;
        this.view = view;
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
    }

    @Override
    public void clickEvent() {
    }

    @Override
    public void hoverEvent() {
    }

    @Override
    public void dragEvent() {
        DrawingType drawType = drawarea.getDrawType().orElse(DrawingType.POINT);
        // Create figure
        Figure item = figurefactory.createFigure(drawType);
        // Initialise stroke and fill
        item.setStroke(drawarea.getPlainStroke());
        item.setColor(Color.web(drawarea.getThemeFillColor()));
        item.setFillColor(Color.web(drawarea.getCanvasBackgroundColor()));
        // Initialise figure
        item.setStart(listener.getStartX(), listener.getStartY());
        item.setEnd(listener.getTempX(), listener.getTempY());
        item.setPoints(drawType);
        item.setPath();
        if (drawType == DrawingType.LINE || drawType == DrawingType.HORIZONTAL || drawType == DrawingType.VERTICAL) {
            // Create arrow line
            if (drawarea.getArrow().equals(ArrowType.END) || drawarea.getArrow().equals(ArrowType.BOTH)) {
                ArrowLine arrow = new ArrowLine(item);
                arrow.setArrowStroke(drawarea.getStroke());
                arrow.setArrowType(drawarea.getArrow());
                arrow.setEndPoints();
                item = arrow;
            }
        }
        view.setNewItem(item);
        view.setDrawing(true);
    }

    @Override
    public void zoomEvent() {
    }

}
