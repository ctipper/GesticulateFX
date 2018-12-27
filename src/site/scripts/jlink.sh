#!/bin/bash
jlink --no-header-files --no-man-pages --strip-debug --module-path $JAVA_HOME/jmods:$JAVAFX_HOME/jmods --add-modules java.base,java.compiler,java.desktop,java.management,java.naming,java.prefs,java.rmi,java.scripting,java.sql,javafx.controls,javafx.fxml,jdk.unsupported,jdk.xml.dom --output target/jfx/java-runtime
