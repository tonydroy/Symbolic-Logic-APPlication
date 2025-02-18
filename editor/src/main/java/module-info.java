/*
Copyright (c) 2024 Tony Roy

This file is part of the Symbolic Logic APPlication (SLAPP).

SLAPP is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

SLAPP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with SLAPP.  If not, see
<https://www.gnu.org/licenses/>.
 */

module slapp.editor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires com.gluonhq.richtextarea;
    requires com.gluonhq.emoji;
    requires javafx.media;


    requires org.apache.commons.lang3;
    //requires org.apache.logging.log4j.core;
    requires jdk.xml.dom;
    requires com.install4j.runtime;
    //requires com.
    requires java.logging;
    requires java.desktop;
    requires java.compiler;

    opens slapp.editor to javafx.fxml;
    exports slapp.editor;
    exports slapp.editor.decorated_rta;
    opens slapp.editor.decorated_rta to javafx.fxml;
//    exports slapp.editor.tests;
//    opens slapp.editor.tests to javafx.fxml;
    exports slapp.editor.vertical_tree.drag_drop;
}