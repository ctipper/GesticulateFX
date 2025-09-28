/**
 * DrawAppComponent.java
 *
 * Created on 20 Sept 2025 14:06:39
 *
 */

/**
 * Copyright (c) 2025 e-conomist
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
package net.perspective.draw;

import javax.inject.Singleton;
import dagger.Component;
import net.perspective.draw.event.DrawAreaListener;
import net.perspective.draw.event.FigureHandler;
import net.perspective.draw.event.MapHandler;
import net.perspective.draw.event.RotationHandler;
import net.perspective.draw.event.SelectionHandler;
import net.perspective.draw.event.SketchHandler;
import net.perspective.draw.event.TextHandler;
import net.perspective.draw.event.behaviours.BehaviourContext;
import net.perspective.draw.event.behaviours.FigureItemBehaviour;
import net.perspective.draw.event.behaviours.GroupedItemBehaviour;
import net.perspective.draw.event.behaviours.MapItemBehaviour;
import net.perspective.draw.event.behaviours.PictureItemBehaviour;
import net.perspective.draw.event.behaviours.TextItemBehaviour;
import net.perspective.draw.event.keyboard.DummyKeyHandler;
import net.perspective.draw.event.keyboard.KeyListener;
import net.perspective.draw.event.keyboard.MapKeyHandler;
import net.perspective.draw.event.keyboard.MoveKeyHandler;
import net.perspective.draw.event.keyboard.TextKeyHandler;
import net.perspective.draw.geom.FigureFactory;
import net.perspective.draw.geom.Picture;
import net.perspective.draw.geom.StreetMap;
import net.perspective.draw.text.Editor;
import net.perspective.draw.util.G2;
import net.perspective.draw.workers.ImageLoadWorker;
import net.perspective.draw.workers.PDFWorker;
import net.perspective.draw.workers.PNGWorker;
import net.perspective.draw.workers.ReadInFunnel;
import net.perspective.draw.workers.SVGWorker;
import net.perspective.draw.workers.WriteOutStreamer;

/**
 *
 * @author ctipper
 */
@Singleton
@Component(modules = {DrawAppModule.class, DrawAppBindingModule.class})
public interface DrawAppComponent {

    void inject(Gesticulate gesticulate);
    
    void inject(CanvasView view);

    void inject(DrawingArea drawingArea);

    void inject(FigureHandler figureHandler);

    void inject(SketchHandler sketchHandler);

    void inject(TextHandler textHandler);
    
    void inject(TextKeyHandler textKeyHandler);

    void inject(TextController textController);
    
    void inject(TextItemBehaviour textItemBehaviour);

    FxAppComponent.Builder fxApp();

    ApplicationController provideApplicationController();
    
    Gesticulate gesticulate();
    
    DrawingArea drawingArea();
    
    CanvasView canvasView();
    
    CanvasTransferHandler canvasTransferHandler();

    Dropper Dropper();

    DrawAreaListener drawAreaListener();

    FigureHandler figureHandler();

    RotationHandler rotationHandler();

    SelectionHandler selectionHandler();

    SketchHandler sketchHandler();

    TextHandler textHandler();

    MapHandler mapHandler();

    KeyListener keyListener();

    DummyKeyHandler dummyKeyHandler();

    MapKeyHandler mapKeyHandler();

    MoveKeyHandler moveKeyHandler();

    TextKeyHandler textKeyHandler();

    BehaviourContext behaviourContext();

    FigureItemBehaviour provideFigureItemBehaviour();

    PictureItemBehaviour providePictureItemBehaviour();

    GroupedItemBehaviour provideGroupedItemBehaviour();

    MapItemBehaviour provideMapItemBehaviour();

    TextItemBehaviour provideTextItemBehaviour();

    FigureFactory figureFactory();

    MapController mapController();

    TextController TextController();

    Editor editor();

    ShareUtils shareUtils();

    ReadInFunnel provideReadInFunnel();

    WriteOutStreamer provideWriteOutStreamer();

    ImageLoadWorker provideImageLoadWorker();

    PDFWorker providePdfWorker();

    SVGWorker provideSvgWorker();

    PNGWorker providePngWorker();

    G2 g2();

    Picture picture();

    StreetMap streetMap();

    @Component.Builder
    interface Builder {

        Builder drawAppModule(DrawAppModule module);

        DrawAppComponent build();
    }

}
