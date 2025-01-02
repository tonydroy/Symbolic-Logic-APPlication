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

package slapp.editor.main_window.assignment;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import slapp.editor.DiskUtilities;
import slapp.editor.EditorAlerts;
import slapp.editor.EditorMain;
import slapp.editor.PrintUtilities;
import slapp.editor.main_window.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static javafx.scene.control.ButtonType.OK;

/**
 * Window to create or revise an assignment
 */
public class CreateAssignment {

    private MainWindow mainWindow;
    private Stage stage;
    private Assignment assignment;
    private int idNumber;
    private int insItemIndex = 0;
    private File exerciseFolder;
    private File assignemtFile;
    private String creationID;
    private TextField assignmentNameField;
    private ChangeListener<String> nameListener;
    private ListView<File> exerciseList;
    private ListView<ExerciseModel> assignmentList;
    private boolean isModified = false;
    private TextArea helpArea;
    private GridPane optionalItemsPane;
    private DoubleProperty centerHeightProperty;
    private HBox workingBox;
    private Button saveButton;
    private Button saveAsButton;
    private Button clearButton;
    private Button exerciseFolderButton;
    private Label creationIDNum;
    private List<TextField> labelFields = new ArrayList<>();
    private List<TextField> valueFields = new ArrayList<>();

    /**
     * Create assignment window
     *
     * @param assignment the (possibly new) assignment to be revised.
     * @param mainWindow main window
     */
    public CreateAssignment(Assignment assignment, MainWindow mainWindow) {
        this.assignment = assignment;
        this.mainWindow = mainWindow;

        setUpWindow();
    }


    /*
    Set up and open create window
     */
    private void setUpWindow() {
        //border pane top

        setCreationID();

        BorderPane borderPane = new BorderPane();

        Label assignmentNameLabel = new Label("Assignment Name: ");
        assignmentNameField = new TextField(assignment.getHeader().getAssignmentName());
        Label creationIDLabel = new Label("Creation ID: ");

        nameListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ob, String ov, String nv) {
                isModified = true;
                assignmentNameField.textProperty().removeListener(this);
            }
        };
        assignmentNameField.textProperty().addListener(nameListener);

        creationIDNum = new Label(creationID);
        Region spacer1 = new Region();


        Menu helpMenu = new Menu("");
        MenuBar menuBar = new MenuBar(helpMenu);
        menuBar.setStyle("-fx-background-color: aliceblue; -fx-border-color: white;");
        HBox idBox = new HBox(assignmentNameLabel, assignmentNameField, spacer1, creationIDLabel, creationIDNum);
        idBox.setPadding(new Insets(10,20,10,20));
        idBox.setMargin(assignmentNameLabel, new Insets(4,2,0,0));
        idBox.setMargin(creationIDLabel, new Insets(4,2,0,0));
        idBox.setMargin(creationIDNum, new Insets(4,0,0,0));
        idBox.setHgrow(spacer1, Priority.ALWAYS);

        Label optionalFieldsLabel = new Label("Optional Fields: ");
        Button addOptionalItemsButton = new Button("+");
        Button removeOptionalItemsButton = new Button("-");
        addOptionalItemsButton.setFont(new Font(16));
        addOptionalItemsButton.setPadding(new Insets(0,5,0,5));

        removeOptionalItemsButton.setFont(new Font(16));
        removeOptionalItemsButton.setPadding(new Insets(1,8,1,8));

        HBox optionalItemBox = new HBox(optionalFieldsLabel, addOptionalItemsButton, removeOptionalItemsButton);
        optionalItemBox.setPadding(new Insets(10,20,5,20));
        optionalItemBox.setAlignment(Pos.CENTER_LEFT);
        optionalItemBox.setMargin(addOptionalItemsButton, new Insets(0,45,0,20));


        optionalItemsPane = new GridPane();
        optionalItemsPane.setPadding(new Insets(5,20,10,20));
        optionalItemsPane.setHgap(10);
        optionalItemsPane.setVgap(10);


        optionalItemsPane.setMargin(optionalFieldsLabel, new Insets(4,0,0,0));

        for (int i = 0; i < assignment.getHeader().getInstructorItems().size(); i++) {
            labelFields.add(insItemIndex, new TextField());
            labelFields.get(insItemIndex).setText(assignment.getHeader().getInstructorItems().get(insItemIndex).getLabel());
            optionalItemsPane.add(labelFields.get(insItemIndex),0, insItemIndex + 1);
            valueFields.add(insItemIndex, new TextField());
            valueFields.get(insItemIndex).setText(assignment.getHeader().getInstructorItems().get(insItemIndex).getValue());
            optionalItemsPane.add(valueFields.get(insItemIndex), 2, insItemIndex + 1);
            insItemIndex++;
        }
        addOptionalItemsButton.setOnAction(e -> {
            labelFields.add(insItemIndex, new TextField());
            labelFields.get(insItemIndex).setPromptText("Label");
            optionalItemsPane.add(labelFields.get(insItemIndex), 0, insItemIndex + 1);
            valueFields.add(insItemIndex, new TextField());
            valueFields.get(insItemIndex).setPromptText("Value");
            optionalItemsPane.add(valueFields.get(insItemIndex), 2, insItemIndex + 1);
            labelFields.get(insItemIndex).requestFocus();
            insItemIndex++;
        });
        removeOptionalItemsButton.setOnAction(e -> {
            if (insItemIndex > 0) {
                insItemIndex--;
                optionalItemsPane.getChildren().remove(labelFields.get(insItemIndex));
                optionalItemsPane.getChildren().remove(valueFields.get(insItemIndex));
                labelFields.remove(insItemIndex);
                valueFields.remove(insItemIndex);
            }
        });

        Label exerciseFolderLabel = new Label("Exercise Folder: ");
        Label exerciseFolderName = new Label("None");
        exerciseFolderButton = new Button("Open Folder");
        Region spacer2 = new Region();
        HBox folderBox = new HBox(exerciseFolderLabel, exerciseFolderName, spacer2, exerciseFolderButton);
        folderBox.setPadding(new Insets(10,20,10,20));
        folderBox.setAlignment(Pos.CENTER_LEFT);
        folderBox.setMargin(exerciseFolderName, new Insets(0,20,0,2));
        folderBox.setHgrow(spacer2, Priority.ALWAYS);


        VBox topBox = new VBox(menuBar, idBox, optionalItemBox, optionalItemsPane, new Separator(Orientation.HORIZONTAL), folderBox);

        borderPane.setTop(topBox);

        //border pane center

        Label exerciseLabel = new Label("Exercise");
        Label assignmtLabel = new Label("Assignment");
        exerciseList = new ListView<>();
        assignmentList = new ListView<>();
        assignmentList.getItems().addAll(assignment.getExerciseModels());

        exerciseList.setCellFactory(new Callback<ListView<File>, ListCell<File>>() {
            public ListCell<File> call(ListView<File> param) {
                return new ListCell<File>() {
                    @Override
                    protected void updateItem(File item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(item == null || empty ? null : item.getName().substring(0, item.getName().length() - 4));
                    }
                };
            }
        });

        VBox exerciseVBox = new VBox(10, exerciseLabel, exerciseList);
        exerciseVBox.setVgrow(exerciseList, Priority.ALWAYS);
        VBox assignmentVBox = new VBox(10, assignmtLabel, assignmentList );
        assignmentVBox.setVgrow(assignmentList, Priority.ALWAYS);
        workingBox = new HBox(50, exerciseVBox, assignmentVBox);
        workingBox.setPadding(new Insets(10,20,20,20));

        exerciseFolderButton.setOnAction(e -> {
           exerciseFolderButton.setDisable(true);
           exerciseFolder = DiskUtilities.getDirectory(DiskUtilities.DirType.EXERCISE);
           if (exerciseFolder != null) {
               exerciseFolderName.setText(exerciseFolder.getAbsolutePath());
               exerciseList.getItems().setAll(DiskUtilities.getFileListFromDir(exerciseFolder, ".sle"));
           }
            exerciseFolderButton.setDisable(false);
        });

        borderPane.setCenter(workingBox);

        // border pane right

        Button addUpButton = new Button("Add \u2191");
        addUpButton.setPrefWidth(65         );
        Button addDownButton = new Button("Add \u2193");
        addDownButton.setPrefWidth(65);
        Button removeButton = new Button("Remove");
        removeButton.setPrefWidth(65);
        saveButton = new Button("Save");
        saveButton.setPrefWidth(65);
        saveAsButton = new Button("Save As");
        saveAsButton.setPrefWidth(65);
        clearButton = new Button("Clear");
        clearButton.setPrefWidth(65);
        Button closeButton = new Button("Close");
        closeButton.setPrefWidth(65);
        VBox buttonBox = new VBox(20, addUpButton, addDownButton, removeButton, saveButton, saveAsButton, clearButton, closeButton);
        buttonBox.setPadding(new Insets(0,20,40,0));
        buttonBox.setMargin(saveButton, new Insets(30,0,0,0));
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        if (EditorMain.secondaryCopy) {
            saveButton.setDisable(true);
            saveAsButton.setDisable(true);
        }

        SelectionModel exerciseSelectionModel = exerciseList.getSelectionModel();
        SelectionModel assignmentSelectionModel = assignmentList.getSelectionModel();


        addDownButton.setOnAction(e -> {
            File exerciseFile = (File) exerciseSelectionModel.getSelectedItem();
            if (exerciseFile != null) {
                ExerciseModel exerciseModel = DiskUtilities.getExerciseModelFromFile(exerciseFile);
                if (exerciseModel !=null) {
                    if (!exerciseModel.isStarted()) {
                        int newIndex = assignmentSelectionModel.getSelectedIndex() + 1;
                        assignmentList.getItems().add(newIndex, exerciseModel);
                        isModified = true;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                assignmentSelectionModel.select(newIndex);
                                assignmentList.getFocusModel().focus(newIndex);
                            }
                        });

                    } else {
                        EditorAlerts.showSimpleAlert("Cannot Add", "The content section of this exercise appears to have been modified.\n\n" +
                                "Please select an empty exercise.");
                    }
                }
            }  else {
                EditorAlerts.fleetingRedPopup("No exercise selected.  Please make selection.");
            }
        });

        addUpButton.setOnAction(e -> {
            File exerciseFile = (File) exerciseSelectionModel.getSelectedItem();
            if (exerciseFile != null) {
                ExerciseModel exerciseModel = DiskUtilities.getExerciseModelFromFile(exerciseFile);
                if (!exerciseModel.isStarted()) {
                    int newIndex = Math.max(0, assignmentSelectionModel.getSelectedIndex());
                    assignmentList.getItems().add(newIndex, exerciseModel);
                    isModified = true;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            assignmentSelectionModel.select(newIndex);
                            assignmentList.getFocusModel().focus(newIndex);
                        }
                    });

                } else {
                    EditorAlerts.showSimpleAlert("Cannot Add", "The content section of this exercise appears to have been modified.\n\n" +
                            "Please select an empty exercise.");
                }
            } else {
                EditorAlerts.fleetingRedPopup("No exercise selected.  Please make selection.");
            }
        });

        assignmentSelectionModel.selectedItemProperty().addListener(new ChangeListener<ExerciseModel>() {
            @Override
            public void changed(ObservableValue ob, ExerciseModel ov, ExerciseModel nv) {
                if (nv != null) {
                    TypeSelectorFactories factory = new TypeSelectorFactories(mainWindow);
                    Exercise exercise = factory.getExerciseFromModelObject(nv);
                    mainWindow.setUpExercise(exercise);
                }
            }
        });

        removeButton.setOnAction(e -> {
            if (assignmentSelectionModel.getSelectedIndex() >= 0) {
                assignmentList.getItems().remove(assignmentSelectionModel.getSelectedIndex());
                isModified = true;
            }
        });

        saveButton.setOnAction(e -> {
            saveAssignment(false);
        });

        saveAsButton.setOnAction(e -> {
           saveAssignment(true);
        });

        clearButton.setOnAction(e -> {
            clearAssignment();
        });


        closeButton.setOnAction(e -> {
            closeWindow();
        });

        borderPane.setRight(buttonBox);

        //border pane bottom

        String helpText = "Name the assignment.  Optional identifying fields (as course, instructor) may be included as appropriate.\n\n" +
                "Build an assignment by opening a folder with SlAPP exercise files (*.sle).  A selected exercise is added to the assignment above or below a selected assignment item by the add buttons.  " +
                "And similarly a selected assignment item is removed by the 'remove' button.";


        helpArea = new TextArea(helpText);
        helpArea.setWrapText(true);
        helpArea.setPrefHeight(110);
        helpArea.setEditable(false);
        helpArea.setFocusTraversable(false);
        helpArea.setMouseTransparent(true);
        helpArea.setPadding(new Insets(5,5,5,5));
        helpArea.setStyle("-fx-text-fill: mediumslateblue");

        borderPane.setBottom(helpArea);

        //scene and stage

        Scene scene = new Scene(borderPane);

        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Edit Assignment:");
        stage.getIcons().addAll(EditorMain.icons);


        //test for mac menu bar
 //       stage.initOwner(EditorMain.mainStage);
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setWidth(605);
   //     stage.setMinHeight(200);

        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        stage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 605));
        stage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 605));
        stage.setOnCloseRequest(e-> {
            e.consume();
            closeWindow();
        });
        stage.show();
        setCenterVgrow();
    }

    /*
    Creation ID is random number set whenever create window is opened
     */
    private void setCreationID() {
        Random rand = new Random();
        idNumber = rand.nextInt(1000000000);
        creationID = Integer.toString(idNumber);
    }

    /*
    Extract assignment model from window
     */
    private Assignment getAssignmentFromWindow() {
        Assignment assignment = new Assignment();
        AssignmentHeader assignmentHeader = assignment.getHeader();
        assignmentHeader.setCreationID(creationID);
        assignmentHeader.setAssignmentName(assignmentNameField.getText());
        List<AssignmentHeaderItem> instructorItems = assignmentHeader.getInstructorItems();
        instructorItems.clear();
        for (int i = 0; i < labelFields.size(); i++) {
            instructorItems.add(new AssignmentHeaderItem(labelFields.get(i).getText(), valueFields.get(i).getText()));
        }
        assignment.setExerciseModels(new ArrayList<ExerciseModel>(assignmentList.getItems()));

        return assignment;
    }

    /*
    Save asignment
     */
    private void saveAssignment(boolean saveAs) {
        Assignment assignment = getAssignmentFromWindow();
        boolean saved = false;
        if (!assignment.getHeader().getAssignmentName().isEmpty()) {
            saveButton.setDisable(true);
            saveAsButton.setDisable(true);
            saved = DiskUtilities.saveAssignment(saveAs, assignment);
            saveButton.setDisable(false);
            saveAsButton.setDisable(false);
            if (saved) isModified = false;
        }
        else EditorAlerts.showSimpleAlert("Cannot Save", "No named assignment to save.");
    }

    /*
    clear name, assignment list, and reset creation ID
     */
    private void clearAssignment() {
        boolean okContinue = true;
        if (isModified) {
            Alert confirm = EditorAlerts.confirmationAlert("Confirm Clear", "This assignment appears to have unsaved changes.\n\nContinue to clear exercise?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.get() != OK) okContinue = false;
        }
        if (okContinue) {
            assignmentNameField.clear();
            assignmentNameField.textProperty().addListener(nameListener);
            setCreationID();
            creationIDNum.setText(creationID);
            assignmentList.getItems().clear();
            isModified = false;
        }
    }

    /*
    Close the exercise showing in the main window, and close create stage.
     */
    private void closeWindow() {
        boolean okContinue = true;
        if (isModified) {
            Alert confirm = EditorAlerts.confirmationAlert("Confirm Close", "This assignment appears to have unsaved changes.\n\nContinue to close window?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.get() != OK) okContinue = false;
        }
        if (okContinue) {
            mainWindow.closeExercise();
            stage.close();
        }
    }

    /*
    Update height of workingBox (border pane center) by dragging window
     */
    private void setCenterVgrow() {
        double fixedHeight = helpArea.getHeight() + 400;

        DoubleProperty fixedValueProperty = new SimpleDoubleProperty(fixedHeight);

        DoubleProperty externalHeightProperty = new SimpleDoubleProperty();
        externalHeightProperty.bind(fixedValueProperty.add(optionalItemsPane.heightProperty()));

        DoubleProperty maximumHeightProperty = new SimpleDoubleProperty(PrintUtilities.getPageHeight() );

        centerHeightProperty = new SimpleDoubleProperty();
        centerHeightProperty.bind(Bindings.min(maximumHeightProperty, (stage.heightProperty().subtract(externalHeightProperty))));
        workingBox.prefHeightProperty().bind(centerHeightProperty);
    }

}
