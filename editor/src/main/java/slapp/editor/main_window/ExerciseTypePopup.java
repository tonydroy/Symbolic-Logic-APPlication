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

package slapp.editor.main_window;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import slapp.editor.EditorAlerts;
import slapp.editor.EditorMain;
import javafx.scene.layout.*;

/**
 * Create pop-up window for user to select an {@link slapp.editor.main_window.ExerciseType}.
 * Usually for the purpose of creating a new exercise of that type.
 */
public class ExerciseTypePopup {
    private static ExerciseType selectedItem = null;
    private static ExerciseType lastSelectedItem = null;

    private static Stage stage;


    /**
     * Display window with ListView of exercise types, and return the one selected by the user.
     *
     * @return the exercise type
     */
    public static ExerciseType getType(MainWindowView mainView) {

        ListView<ExerciseType> typeList = new ListView();
        typeList.getItems().setAll(ExerciseType.values());
        typeList.setPrefWidth(200);
        typeList.getSelectionModel().select(lastSelectedItem);

        typeList.setCellFactory(cell -> new ListCell<ExerciseType>() {
            final Tooltip tooltip = new Tooltip();

            @Override
            protected void updateItem(ExerciseType type, boolean empty) {
                super.updateItem(type, empty);

                if (type == null || empty) {
                    setText(null);
                    setTooltip(null);
                }
                else {
                    setText(type.toString());
                    tooltip.setText(type.getDescription());
                    tooltip.setStyle("-fx-font-size: 12");
                    setTooltip(tooltip);

                    setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                                if (mouseEvent.getClickCount() == 2) {
                                    ExerciseType selection = typeList.getSelectionModel().getSelectedItem();
                                    selectedItem = selection;
                                    lastSelectedItem = selection;
                                    stage.close();
                                }
                            }
                        }
                    });
                }
            }

        });

        typeList.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                if (e.getCode() == KeyCode.ENTER)  {
                    ExerciseType selection = typeList.getSelectionModel().getSelectedItem();
                    selectedItem = selection;
                    lastSelectedItem = selection;
                    e.consume();
                    stage.close();
                }
            }
        });


        Button selectButton = new Button("Select");
        Button cancelButton = new Button("Cancel");
        Region spacer = new Region();

        VBox buttonBox = new VBox(20);
        buttonBox.getChildren().addAll(selectButton, spacer, cancelButton);
        buttonBox.setVgrow(spacer, Priority.ALWAYS);
        buttonBox.setMargin(selectButton, new Insets(10));
        buttonBox.setMargin(cancelButton, new Insets(10));

        HBox mainBox = new HBox(10);
        mainBox.setPadding(new Insets(5,0,20,20));
        mainBox.getChildren().addAll(typeList, buttonBox);

        Label title = new Label("Select Exercise Type:");
        title.setFont(new Font(16));

        VBox titledBox = new VBox(5);
        titledBox.setPadding(new Insets(20));
        titledBox.getChildren().addAll(title, mainBox);

        StackPane pane = new StackPane(titledBox);
        Scene scene = new Scene(pane);

        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Exercise Type");

        //
        stage.initOwner(EditorMain.mainStage);
        stage.initModality(Modality.WINDOW_MODAL);

//        stage.initModality(Modality.APPLICATION_MODAL);
        //

        stage.getIcons().addAll(EditorMain.icons);





        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        stage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 400));
        stage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 200));

        selectButton.setOnAction(e -> {
            ExerciseType selection = typeList.getSelectionModel().getSelectedItem();
            if (selection == null) EditorAlerts.showSimpleAlert("No Selection Made", "Please select an exercise type (or cancel)");
            else {
                selectedItem = selection;
                lastSelectedItem = selection;
                stage.close();
            }
        });
        cancelButton.setOnAction(e -> {
            selectedItem = null;
            stage.close();
        });
        stage.setOnCloseRequest(e -> {
            selectedItem = null;
            stage.close();
        });


        stage.showAndWait();
        return selectedItem;
    }



}
