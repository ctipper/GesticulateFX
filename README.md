](https://github.com/openjdk/jfx# GesticulateFX, a freehand drawing app

An app for sketching freehand diagrams. GesticulateFX is not geared towards graphic design and is intended to be used for more casual scenarios where it is required to be able to sketch an idea with some notation and then to be able to export to a variety of formats.

It supports vector shapes, curve-fitted lines, images may be pasted or imported from shell and there is a mapping component.

To build and run, a `JAVAFX_HOME` environment variable needs to be set and this uses the latest javafx release. ```mvn clean compile exec:exec```

Platform builds are in the site directory using `ant` script. This assumes that JDK is in `~/Applications` folder.

An ant runner is provided `mvn clean package -P ant-builder` for app bundle or `mvn clean install -P ant-builder` for platform installer.

![GesticulateFX, sketch freehand diagrams using JavaFX](https://www.e-conomist.me.uk/images/144dpi/gesticulate-fx-15.png)

## Possible directions

1 - The branch `inputstream` represents a naïve implmentation of input method event handling for the text editing feature. It currently has no effect, even though a best effort has been made to use patterns from an existing java implementation (not published). 

Any help complying with javafx frameworks for IME is appreciated, though I strongly suspect [github.com/openjdk/jfx](https://github.com/openjdk/jfx) has not envisioned this scenario and that much work needs to be done upstream. Be careful, this branch is unstable due to needs to keep in sync with the codebase.

2 - Text editing currently is a very simple implementation. To provide multi-line text blocks with formatting requires a much more robust framework using methods analagous to [github.com/ProseMirror/prosemirrror](https://github.com/ProseMirror/prosemirror) which uses a flattened-node composer which is then serialised both to UI and to backing store. I do not know how to implement this and obviously prosemirror is a robust text editing tool with many capablities that I do not need to implement. It is also written for the web.

