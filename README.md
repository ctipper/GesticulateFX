# GesticulateFX, a freehand drawing app

An app for sketching freehand diagrams. GesticulateFX is not geared towards graphic design and is intended to be used for more casual scenarios where it is required to be able to sketch an idea with some notation and then to be able to export to a variety of formats.

It supports vector shapes, curve-fitted lines, images may be pasted or imported from shell and there is a mapping component.

To build and run, a `JAVAFX_HOME` environment variable needs to be set and this uses the latest JavaFX release. ```mvn clean compile exec:exec```

Platform builds are in the site directory using `ant` script. This assumes that the JDK is in `~/Applications` folder.

An ant runner is provided `mvn clean package -P ant-builder` for app bundle or `mvn clean install -P ant-builder` for platform installer.

![GesticulateFX, sketch freehand diagrams using JavaFX](https://www.e-conomist.me.uk/images/144dpi/gesticulate-fx-15.png)

## Possible directions

1 - The branch `inputstream` represents a naive implementation of input method event handling for the text editing feature. After much back-and-forth with an AI it seems that 
[github.com/openjdk/jfx](https://github.com/openjdk/jfx) is not set up for users to customise this. Currently it would require me to implement with a TextField node as opposed to the custom code with a TextFlow node. This is not too difficult but it would require dismantling the infrastructure to provide formatted text.

2 - For the future it would be desirable to provide multi-line text blocks with formatting, similar to [Microsoft OneNote](https://www.onenote.com/). Given that [github.com/openjdk/jfx](https://github.com/openjdk/jfx) is not designed for developers to customise a third-party library is needed, and serialisation is the key challenge.

Pull requests are welcome, but also I am not providing this as a commercial service so don't @ me if turn-around is lengthy.


