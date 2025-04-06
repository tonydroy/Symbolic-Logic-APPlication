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

package slapp.editor.front_page;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import slapp.editor.main_window.MainWindowView;

public class FrontPageAnimation {

    ParallelTransition p;

    VBox root;

    public VBox getFrontPageBox() {

        AnchorPane treePane = new AnchorPane();
        treePane.setPrefHeight(370);


        Circle dot1 = new Circle(150,50,25, Paint.valueOf("2cf4e0"));
        Circle dot2 = new Circle(300, 50, 25, Paint.valueOf("50d0c3"));
        Circle dot3 = new Circle(450, 50, 25, Paint.valueOf("afeeee"));
        Circle dot4 = new Circle(225, 150, 25, Paint.valueOf("00ced1"));
        Circle dot5 = new Circle(450, 150, 25, Paint.valueOf("40e0d0"));
        Circle dot6 = new Circle(362.5, 250, 25, Paint.valueOf("48d1cc"));
        Circle dot7 = new Circle(362.5, 350, 25, Paint.valueOf("31b6a8"));

        Line line1 = new Line(150, 50, 225, 150);
        Line line2 = new Line(300,50, 225, 150);
        Line line3 = new Line(225, 150, 362.5, 250);
        Line line4 = new Line(450, 50, 450, 150);
        Line line5 = new Line(362.5, 250, 362.50, 350);
        Line line6 = new Line(450, 150, 362.5, 250);

        line1.setStroke(Color.DARKTURQUOISE);
        line2.setStroke(Color.DARKTURQUOISE);
        line3.setStroke(Color.DARKTURQUOISE);
        line4.setStroke(Color.DARKTURQUOISE);
        line5.setStroke(Color.DARKTURQUOISE);
        line6.setStroke(Color.DARKTURQUOISE);

        line1.setStrokeWidth(3.0);
        line2.setStrokeWidth(3.0);
        line3.setStrokeWidth(3.0);
        line4.setStrokeWidth(3.0);
        line5.setStrokeWidth(3.0);
        line6.setStrokeWidth(3.0);


        treePane.getChildren().addAll(line1, line2, line3, line4, line5, line6, dot1, dot2, dot3, dot4, dot5, dot6, dot7);

        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(3);
        shadow.setOffsetY(3);
        treePane.setEffect(shadow);

        Pane pane1 = new Pane(treePane);

        pane1.setStyle("-fx-background-color: transparent");
//        pane1.setStyle("-fx-background-color: #AFEEEE");
        Pane pane2 = new Pane(pane1);
        pane2.setStyle("-fx-background-color: transparent");
//        pane2.setStyle("-fx-background-color: #AFEEEE");
        Pane pane3 = new Pane(pane2);
        pane3.setStyle("-fx-background-color: transparent");
//        pane3.setStyle("-fx-background-color: #AFEEEE");



        Text title0 = new Text("SLAPP");
        title0.setFont(Font.font("Noto Serif Combo", FontWeight.BOLD, FontPosture.ITALIC, 48));
        title0.setStroke(Color.BLACK);
        title0.setStrokeWidth(1.5);
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0.2, Color.WHITE), new Stop(0.8, Color.BLUEVIOLET));
        title0.setFill(gradient);
        Glow g = new Glow();
        g.setLevel(1.0);
        title0.setEffect(g);

        Text title1 = new Text("Symbolic Logic APPlication");
        title1.setFont(Font.font("Noto Serif Combo", FontWeight.BOLD, 36));

        Text title2 = new Text("SLAPP editor, v3.0");
        title2.setFont(Font.font("Noto Serif Combo",20));
        VBox titleBox = new VBox(title0, title1, title2);
        titleBox.setPadding(new Insets(10, 20, 10, 20));
        titleBox.setAlignment(Pos.CENTER);


        RotateTransition rt = new RotateTransition(Duration.seconds(5), treePane);
        rt.setFromAngle(0.0);
        rt.setToAngle(360.0);
        rt.setCycleCount(1);
        rt.setAutoReverse(false);
        rt.setAxis(Rotate.X_AXIS);

        RotateTransition rt1 = new RotateTransition(Duration.seconds(5), pane1);
        rt1.setFromAngle(0.0);
        rt1.setToAngle(360.0);
        rt1.setCycleCount(1);
        rt1.setAutoReverse(false);
        rt1.setAxis(Rotate.Y_AXIS);

        RotateTransition rt2 = new RotateTransition(Duration.seconds(5), pane2);
        rt2.setFromAngle(0.0);
        rt2.setToAngle(360.0);
        rt2.setCycleCount(1);
        rt2.setAutoReverse(false);
        rt2.setAxis(Rotate.Z_AXIS);

        ScaleTransition sc = new ScaleTransition(Duration.seconds(5), pane3);
        sc.setFromX(0.0);
        sc.setToX(1.0);
        sc.setFromY(0.0);
        sc.setToY(1.0);
        sc.setFromZ(0.0);
        sc.setToZ(1.0);
        sc.setCycleCount(1);
        sc.setAutoReverse(false);

        FadeTransition fd = new FadeTransition(Duration.seconds(5), titleBox);
        fd.setFromValue(0);
        fd.setToValue(1.0);
        fd.setInterpolator(Interpolator.EASE_IN);


        p = new ParallelTransition(rt, rt1, rt2, sc, fd);


        root = new VBox(30, pane3, titleBox);
//        root.setStyle("-fx-background-color: white");
//        root.setStyle("-fx-background-color: #AFEEEE");




        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #afeef0, #3bd6da); -fx-background-radius: 10 10 10 10") ;


        root.setPrefWidth(600);
        root.setPrefHeight(600);

        root.setOpacity(0);



        return root;

    }

    public void playFrontAnimation() {
        root.setOpacity(1);
        p.play();
    }


}
