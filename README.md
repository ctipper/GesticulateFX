# GesticulateFX, a freehand drawing app

An app for sketching freehand diagrams. GesticulateFX is not geared towards graphic design and is intended to be used for more casual scenarios where it is required to be able to sketch an idea with some notation and then to be able to export to a variety of formats.

It supports vector shapes, curve-fitted lines, images may be pasted or imported from shell and there is a mapping component.

To build and run, a `JAVAFX_HOME` environment variable needs to be set and this uses the latest javafx release. ```mvn clean compile exec:exec```

Platform builds are in the site directory using `ant` script. This assumes that JDK is in `~/Applications` folder.

An ant runner is provided `mvn clean package -P ant-builder` for app bundle or `mvn clean install -P ant-builder` for platform installer.

![GesticulateFX, sketch freehand diagrams using JavaFX](https://www.e-conomist.me.uk/images/144dpi/gesticulate-fx-15.png)

