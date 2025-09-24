/**
 * DrawAppModule.java
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

import javax.inject.Provider;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
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
import net.perspective.draw.geom.FigureFactoryImpl;
import net.perspective.draw.geom.Picture;
import net.perspective.draw.geom.StreetMap;
import net.perspective.draw.text.Editor;
import net.perspective.draw.text.TextEditor;
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

@Module(subcomponents = {FxAppComponent.class})
public class DrawAppModule {

    @Provides
    @Singleton
    ApplicationController provideApplicationController(
            Provider<DrawingArea> drawingAreaProvider,
            Provider<CanvasView> canvasViewProvider) {
        return new ApplicationController(drawingAreaProvider, canvasViewProvider);
    }
    
    @Provides
    @Singleton
    DrawingArea provideDrawingArea(CanvasView view, ApplicationController controller, DrawAppComponent component) {
        DrawingArea drawingArea = new DrawingArea(view, controller);
        component.inject(drawingArea);
        return drawingArea;
    }

    @Provides
    @Singleton
    CanvasTransferHandler provideCanvasTransferHandler() {
        return new CanvasTransferHandler();
    }

    @Provides
    @Singleton
    Dropper provideDropper() {
        return new Dropper();
    }

    @Provides
    @Singleton
    DrawAreaListener provideDrawAreaListener() {
        return new DrawAreaListener();
    }

    @Provides
    @Singleton
    FigureHandler provideFigureHandler() {
        return new FigureHandler();
    }

    @Provides
    @Singleton
    RotationHandler provideRotationHandler() {
        return new RotationHandler();
    }

    @Provides
    @Singleton
    SelectionHandler provideSelectionHandler() {
        return new SelectionHandler();
    }

    @Provides
    @Singleton
    SketchHandler provideSketchHandler() {
        return new SketchHandler();
    }

    @Provides
    @Singleton
    TextHandler provideTextHandler() {
        return new TextHandler();
    }

    @Provides
    @Singleton
    MapHandler provideMapHandler() {
        return new MapHandler();
    }

    @Provides
    @Singleton
    KeyListener provideKeyListener() {
        return new KeyListener();
    }

    @Provides
    @Singleton
    DummyKeyHandler provideDummyKeyHandler() {
        return new DummyKeyHandler();
    }

    @Provides
    @Singleton
    MapKeyHandler provideMapKeyHandler() {
        return new MapKeyHandler();
    }

    @Provides
    @Singleton
    MoveKeyHandler provideMoveKeyHandler() {
        return new MoveKeyHandler();
    }

    @Provides
    @Singleton
    TextKeyHandler provideTextKeyHandler() {
        return new TextKeyHandler();
    }

    @Provides
    @Singleton
    BehaviourContext provideBehaviourContext() {
        return new BehaviourContext();
    }

    @Provides
    @Singleton
    FigureItemBehaviour provideFigureItemBehaviour() {
        return new FigureItemBehaviour();
    }

    @Provides
    @Singleton
    PictureItemBehaviour providePictureItemBehaviour() {
        return new PictureItemBehaviour();
    }

    @Provides
    @Singleton
    GroupedItemBehaviour provideGroupedItemBehaviour() {
        return new GroupedItemBehaviour();
    }

    @Provides
    @Singleton
    MapItemBehaviour provideMapItemBehaviour() {
        return new MapItemBehaviour();
    }

    @Provides
    @Singleton
    TextItemBehaviour provideTextItemBehaviour() {
        return new TextItemBehaviour();
    }

    @Provides
    @Singleton
    FigureFactory provideFigureFactory() {
        return new FigureFactoryImpl();
    }

    @Provides
    @Singleton
    MapController provideMapController() {
        return new MapController();
    }

//    @Provides
//    @Singleton
//    TextController provideTextController(DrawingArea drawarea, Provider<CanvasView> viewProvider,
//            ApplicationController controller, Provider<Editor> editorProvider) {
//        return new TextController(drawarea, viewProvider, controller, editorProvider);
//    }

    @Provides
    @Singleton
    Editor provideEditor() {
        return new TextEditor();
    }

    @Provides
    @Singleton
    ShareUtils provideShareUtils() {
        return new ShareUtils();
    }

    @Provides
    @Singleton
    ReadInFunnel provideReadInFunnel() {
        return new ReadInFunnel();
    }

    @Provides
    @Singleton
    WriteOutStreamer provideWriteOutStreamer() {
        return new WriteOutStreamer();
    }

    @Provides
    @Singleton
    ImageLoadWorker provideImageLoadWorker() {
        return new ImageLoadWorker();
    }

    @Provides
    @Singleton
    PDFWorker providePDFWorker() {
        return new PDFWorker();
    }

    @Provides
    @Singleton
    SVGWorker provideSVGWorker() {
        return new SVGWorker();
    }

    @Provides
    @Singleton
    PNGWorker providePNGWorker() {
        return new PNGWorker();
    }

    @Provides
    @Singleton
    G2 provideG2() {
        return new G2();
    }

    @Provides
    Picture providePicture() {
        return new Picture();
    }

    @Provides
    StreetMap provideStreetMap() {
        return new StreetMap();
    }

}
