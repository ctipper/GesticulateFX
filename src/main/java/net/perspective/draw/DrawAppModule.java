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
import net.perspective.draw.geom.Picture;
import net.perspective.draw.geom.StreetMap;
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
    Gesticulate provideGesticulate() {
        return new Gesticulate();
    }

    @Provides
    @Singleton
    ApplicationController provideApplicationController(Provider<DrawingArea> drawingAreaProvider,
            Provider<CanvasView> viewProvider, DrawAppComponent component) {
        ApplicationController applicationController = new ApplicationController(drawingAreaProvider, viewProvider);
        return applicationController;
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
    CanvasView provideCanvasView(Provider<DrawingArea> drawareaProvider,
            Provider<ApplicationController> controllerProvider,
            Provider<TextController> textControllerProvider,
            DrawAppComponent component) {
        CanvasView view = new CanvasView(drawareaProvider, controllerProvider, textControllerProvider);
        component.inject(view);
        return view;

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
    FigureHandler provideFigureHandler(DrawingArea drawarea, CanvasView view, DrawAppComponent component) {
        FigureHandler figureHandler = new FigureHandler(drawarea, view);
        component.inject(figureHandler);
        return figureHandler;
    }

    @Provides
    SketchHandler provideSketchHandler(DrawingArea drawarea, CanvasView view, DrawAppComponent component) {
        SketchHandler sketchHandler = new SketchHandler(drawarea, view);
        component.inject(sketchHandler);
        return sketchHandler;
    }

    @Provides
    RotationHandler provideRotationHandler(DrawingArea drawarea, CanvasView view) {
        return new RotationHandler(drawarea, view);
    }

    @Provides
    SelectionHandler provideSelectionHandler(DrawingArea drawarea,
            CanvasView view, ApplicationController controller,
            DrawAppComponent component) {
        SelectionHandler selectionHandler = new SelectionHandler(drawarea, view, controller);
        component.inject(selectionHandler);
        return selectionHandler;
    }

    @Provides
    TextHandler provideTextHandler(DrawingArea drawarea, CanvasView view, DrawAppComponent component) {
        TextHandler textHandler = new TextHandler(drawarea, view);
        component.inject(textHandler);
        return textHandler;
    }

    @Provides
    MapHandler provideMapHandler(DrawAppComponent component) {
        MapHandler mapHandler = new MapHandler();
        component.inject(mapHandler);
        return mapHandler;
    }

    @Provides
    @Singleton
    KeyListener provideKeyListener() {
        return new KeyListener();
    }

    @Provides
    DummyKeyHandler provideDummyKeyHandler(DrawAppComponent component) {
        DummyKeyHandler dummyKeyHandler = new DummyKeyHandler();
        component.inject(dummyKeyHandler);
        return dummyKeyHandler;
    }

    @Provides
    MapKeyHandler provideMapKeyHandler(DrawingArea drawarea, CanvasView view, DrawAppComponent component) {
        MapKeyHandler mapKeyHandler = new MapKeyHandler(drawarea, view);
        component.inject(mapKeyHandler);
        return mapKeyHandler;
    }

    @Provides
    MoveKeyHandler provideMoveKeyHandler(DrawingArea drawarea, CanvasView view, DrawAppComponent component) {
        MoveKeyHandler moveKeyHandler = new MoveKeyHandler(drawarea, view);
        component.inject(moveKeyHandler);
        return moveKeyHandler;
    }

    @Provides
    TextKeyHandler provideTextKeyHandler(DrawingArea drawarea, CanvasView view,
            ApplicationController controller, DrawAppComponent component) {
        TextKeyHandler textKeyHandler = new TextKeyHandler(drawarea, view, controller);
        component.inject(textKeyHandler);
        return textKeyHandler;
    }

    @Provides
    @Singleton
    BehaviourContext provideBehaviourContext() {
        return new BehaviourContext();
    }

    @Provides
    FigureItemBehaviour provideFigureItemBehaviour(DrawingArea drawarea, CanvasView view, DrawAppComponent component) {
        FigureItemBehaviour figureItemBehaviour = new FigureItemBehaviour(drawarea, view);
        component.inject(figureItemBehaviour);
        return figureItemBehaviour;
    }

    @Provides
    PictureItemBehaviour providePictureItemBehaviour(DrawingArea drawarea, CanvasView view, DrawAppComponent component) {
        PictureItemBehaviour pictureItemBehaviour = new PictureItemBehaviour(drawarea, view);
        component.inject(pictureItemBehaviour);
        return pictureItemBehaviour;
    }

    @Provides
    GroupedItemBehaviour provideGroupedItemBehaviour(DrawingArea drawarea, CanvasView view, DrawAppComponent component) {
        GroupedItemBehaviour groupedItemBehaviour = new GroupedItemBehaviour(drawarea, view);
        component.inject(groupedItemBehaviour);
        return groupedItemBehaviour;
    }

    @Provides
    MapItemBehaviour provideMapItemBehaviour(DrawingArea drawarea, CanvasView view, DrawAppComponent component) {
        MapItemBehaviour mapItemBehaviour = new MapItemBehaviour(drawarea, view);
        component.inject(mapItemBehaviour);
        return mapItemBehaviour;
    }

    @Provides
    TextItemBehaviour provideTextItemBehaviour(DrawingArea drawarea, CanvasView view, DrawAppComponent component) {
        TextItemBehaviour textItemBehaviour = new TextItemBehaviour(drawarea, view);
        component.inject(textItemBehaviour);
        return textItemBehaviour;
    }

    @Provides
    @Singleton
    MapController provideMapController(Provider<DrawingArea> drawareaProvider, Provider<CanvasView> viewProvider,
            DrawAppComponent component) {
        MapController mapController = new MapController(drawareaProvider, viewProvider);
        component.inject(mapController);
        return mapController;
    }

    @Provides
    @Singleton
    TextController provideTextController(DrawingArea drawarea, Provider<CanvasView> viewProvider,
            ApplicationController controller, DrawAppComponent component) {
        TextController textController = new TextController(drawarea, viewProvider, controller);
        component.inject(textController);
        return textController;
    }

    @Provides
    @Singleton
    ShareUtils provideShareUtils(Provider<Gesticulate> applicationProvider,
            Provider<CanvasView> viewProvider,
            Provider<ApplicationController> controllerProvider,
            DrawAppComponent component) {
        ShareUtils shareUtils = new ShareUtils(applicationProvider, viewProvider, controllerProvider);
        component.inject(shareUtils);
        return shareUtils;
    }

    @Provides
    ReadInFunnel provideReadInFunnel(Provider<DrawingArea> drawareaProvider,
            Provider<CanvasView> viewProvider,
            Provider<ApplicationController> controllerProvider,
            DrawAppComponent component) {
        ReadInFunnel readInFunnel = new ReadInFunnel(drawareaProvider, viewProvider, controllerProvider);
        component.inject(readInFunnel);
        return readInFunnel;
    }

    @Provides
    WriteOutStreamer provideWriteOutStreamer(Provider<CanvasView> viewProvider,
            Provider<ApplicationController> controllerProvider,
            DrawAppComponent component) {
        WriteOutStreamer writeOutStreamer = new WriteOutStreamer(viewProvider, controllerProvider);
        component.inject(writeOutStreamer);
        return writeOutStreamer;

    }

    @Provides
    ImageLoadWorker provideImageLoadWorker(Provider<DrawingArea> drawareaProvider,
            Provider<CanvasView> viewProvider,
            Provider<ApplicationController> controllerProvider,
            DrawAppComponent component) {
        ImageLoadWorker imageLoadWorker = new ImageLoadWorker(drawareaProvider, viewProvider, controllerProvider);
        component.inject(imageLoadWorker);
        return imageLoadWorker;
    }

    @Provides
    PDFWorker providePDFWorker(Provider<CanvasView> viewProvider,
            Provider<ApplicationController> controllerProvider,
            DrawAppComponent component) {
        PDFWorker pdfWorker = new PDFWorker(viewProvider, controllerProvider);
        component.inject(pdfWorker);
        return pdfWorker;
    }

    @Provides
    SVGWorker provideSVGWorker(Provider<CanvasView> viewProvider,
            Provider<ApplicationController> controllerProvider,
            DrawAppComponent component) {
        SVGWorker svgWorker = new SVGWorker(viewProvider, controllerProvider);
        component.inject(svgWorker);
        return svgWorker;
    }

    @Provides
    PNGWorker providePNGWorker(Provider<CanvasView> viewProvider,
            Provider<ApplicationController> controllerProvider,
            DrawAppComponent component) {
        PNGWorker pngWorker = new PNGWorker(viewProvider, controllerProvider);
        component.inject(pngWorker);
        return pngWorker;
    }

    @Provides
    @Singleton
    G2 provideG2(Provider<DrawingArea> drawareaProvider,
            Provider<ApplicationController> controllerProvider,
            Provider<TextController> textControllerProvider) {
        return new G2(drawareaProvider, controllerProvider, textControllerProvider);
    }

    @Provides
    Picture providePicture(DrawingArea drawarea, CanvasView view) {
        return new Picture(drawarea, view);
    }

    @Provides
    StreetMap provideStreetMap(DrawingArea drawarea, CanvasView view) {
        return new StreetMap(drawarea, view);
    }

}
