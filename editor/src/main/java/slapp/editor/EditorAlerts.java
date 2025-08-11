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

package slapp.editor;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import slapp.editor.main_window.MainWindowView;

import java.util.List;

/**
 * Utility class to show pop-up alerts
 */
public class EditorAlerts {

    static boolean showPreliminary = false;
    static String prelimMessage = "";

    /**
     * Information alert with 'OK' to close
     * @param header alert header string
     * @param message alert message
     */
    public static void showSimpleAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(header);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        Stage mainStage = EditorMain.mainStage;
        alert.setX(mainStage.getX() + mainStage.getWidth()/3);
        alert.setY(mainStage.getY() + mainStage.getHeight()/3);

//        alert.initOwner(EditorMain.mainStage);
        alert.showAndWait();

    }

    /**
     * Information alert with 'OK' to close
     * @param header alert header string
     * @param messageTxts list of texts for message
     */
    //collects list of texts into text flow for showSimpleTxtFlowAlert
    public static void showSimpleTxtListAlert(String header, List<Text> messageTxts) {

        if (showPreliminary) {
            fleetingRedPopup(prelimMessage);
        }

        TextFlow textFlow = new TextFlow();
        if (messageTxts != null) {
            Text[] resultArray = new Text[messageTxts.size()];
            messageTxts.toArray(resultArray);
            textFlow = new TextFlow(resultArray);
        }
            EditorAlerts.showSimpleTxtFlowAlert(header, textFlow);
    }

    /**
     * Information alert with 'OK' to close
     * @param header alert header string
     * @param message alert message
     */
    public static void showSimpleTxtFlowAlert(String header, TextFlow message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(header);
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(message);
        alert.getDialogPane().setGraphic(null);
        alert.initModality(Modality.APPLICATION_MODAL);
        Stage mainStage = EditorMain.mainStage;

        message.setPrefWidth(400);
        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        alert.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 420));
        alert.setY(mainStage.getY() + mainStage.getHeight()/4);
        alert.initOwner(mainStage);
        alert.showAndWait();

    }

    /**
     * Self-closing information alert
     * @param header alert header string
     * @param message alert message
     */
    public static void showFleetingAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(header);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        Stage mainStage = EditorMain.mainStage;
        alert.setX(mainStage.getX() + mainStage.getWidth()/3);
        alert.setY(mainStage.getY() + mainStage.getHeight()/3);
        alert.initOwner(EditorMain.mainStage);
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> alert.hide());
        delay.play();
        alert.showAndWait();

    }

    /**
     * Confirmation alert with OK/Cancel options.  The alert is returned, and must by shown by caller
     * @param title alert header string
     * @param content alert message
     * @return the alert
     */
    public static Alert confirmationAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
    //    alert.initModality(Modality.APPLICATION_MODAL);
        Stage mainStage = EditorMain.mainStage;
        alert.setX(mainStage.getX() + mainStage.getWidth()/3);
        alert.setY(mainStage.getY() + mainStage.getHeight()/3);
        alert.initOwner(EditorMain.mainStage);                        //put in to stop (irregular) exception -- took out for mac. . .
        return alert;
    }

    /**
     * Self-closing popup message
     * @param message the string (text) message
     */
    public static void fleetingPopup(String message) {
        Popup popup = new Popup();
        Label label = new Label(message);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-background-color: aliceblue; -fx-border-color: blue;");

        Text text = new Text(message);
        new Scene(new Group(text));
        text.applyCss();
        double width = Math.max(200, text.getLayoutBounds().getWidth());


        label.setMinWidth(width + 20);
        label.setMaxWidth(1000);
        label.setMinHeight(100);
        popup.getContent().add(label);

        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> popup.hide());
        popup.show(EditorMain.mainStage);
        delay.play();
    }

    public static void fleetingRedPopup(String message) {
        Popup popup = new Popup();
        Label label = new Label(message);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-background-color: palevioletred; -fx-border-color: darkred;");

        Text text = new Text(message);
        new Scene(new Group(text));
        text.applyCss();
        double width = Math.max(200, text.getLayoutBounds().getWidth());


        label.setMinWidth(width + 20);
        label.setMaxWidth(1000);
        label.setMinHeight(100);
        popup.getContent().add(label);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> popup.hide());
        popup.show(EditorMain.mainStage);
        delay.play();
    }

    /**
     * Self-closing popup with label (node) message area
     * @param label
     */
    public static void fleetingPopup(Label label) {
        Popup popup = new Popup();
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-background-color: aliceblue; -fx-border-color: blue;");


        new Scene(new Group(label));
        label.applyCss();
        double width = Math.max(200, label.getLayoutBounds().getWidth());


        label.setMinWidth(width + 20);
        label.setMaxWidth(1000);
        label.setMinHeight(100);
        popup.getContent().add(label);

        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> popup.hide());
        popup.show(EditorMain.mainStage);
        delay.play();
    }

    public static void setShowPreliminary(boolean showPrelim, String messageString) {
        showPreliminary = showPrelim;
        prelimMessage = messageString;
    }




}
