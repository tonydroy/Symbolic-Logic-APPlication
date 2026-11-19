package com.gluonhq.richtextarea;

import javafx.animation.PauseTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import static com.gluonhq.richtextarea.RichTextArea.mainStage;

public class Alerts {



    public static void showSimpleAlert(String header, String message, Stage stage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(header);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setX(mainStage.getX() + mainStage.getWidth()/3);
        alert.setY(mainStage.getY() + mainStage.getHeight()/3);

//        alert.initOwner(stage);
        alert.showAndWait();
    }

    public static void fleetingMiniPopup(String message) {
        Popup popup = new Popup();
        Label label = new Label(message);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-background-color: aliceblue; -fx-border-color: blue;");



        Text text = new Text(message);
        new Scene(new Group(text));
        text.applyCss();
        double width = Math.max(50, text.getLayoutBounds().getWidth());


        label.setMinWidth(width + 20);
        label.setMaxWidth(1000);
        label.setMinHeight(50);
        popup.getContent().add(label);

        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> popup.hide());

        Bounds sceneBounds = mainStage.getScene().getRoot().localToScreen(mainStage.getScene().getRoot().getBoundsInLocal());
        if (sceneBounds !=null) {
            double x = sceneBounds.getMinX();
            double y = sceneBounds.getMaxY();
            popup.show(mainStage, (x + 5), (y - 55));
        }
        else {
            popup.show(mainStage);
         //   popup.setY(popup.getY() + 45);
        }
        delay.play();
    }
}
