package com.gluonhq.richtextarea;

import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
}
