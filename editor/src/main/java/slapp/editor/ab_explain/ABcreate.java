/*
Copyright (c) 2024 Tony Roy

This file is part of the Symbolic Logic APPlication (SLAPP).

SLAPP is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

SLAPP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with SLAPP.
If not, see <https://www.gnu.org/licenses/>.
 */

package slapp.editor.ab_explain;


import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import slapp.editor.EditorAlerts;
import slapp.editor.EditorMain;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.decorated_rta.KeyboardDiagram;
import slapp.editor.main_window.ControlType;
import slapp.editor.main_window.MainWindow;
import slapp.editor.main_window.MainWindowView;
import slapp.editor.page_editor.PageContent;

import java.util.ArrayList;
import java.util.Optional;

import static javafx.scene.control.ButtonType.OK;

/**
 * Create window for the AB edit exercise
 */
public class ABcreate {
    private MainWindow mainWindow;
    private RichTextArea statementRTA;
    private DecoratedRTA statementDRTA;
    private double statementTextHeight;
    private TextField nameField;
    private TextField leaderField;
    private TextField promptFieldA;
    private TextField promptFieldB;
    private TextField contentPromptField;
    private boolean fieldsModified = false;
    private ChangeListener nameListener;
    private ChangeListener leaderListener;
    private ChangeListener fieldListenerA;
    private ChangeListener fieldListenerB;
    private ChangeListener contentPromptListener;
    private double scale =1.0;
    private Scene scene;
    private Stage stage;
    private SimpleDoubleProperty centerHeightProperty;
    private TextArea helpArea;
    private VBox centerBox;
    private Button saveButton;
    private Button saveAsButton;
    private ToolBar sizeToolBar;
    private DecoratedRTA dummyDRTA = new DecoratedRTA();
    private MenuBar menuBar;
    VBox textFieldsPromptBox;
    private BorderPane borderPane;


    /**
     * Construct AB edit create window from scratch
     * @param mainWindow the main window
     */
    public ABcreate(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setupWindow();
    }

    /**
     * Open create window for modification of existing AB edit exercise
     * @param mainWindow the main window
     * @param originalModel model for the existing exercise
     */
    public ABcreate(MainWindow mainWindow, ABmodel originalModel) {
        this(mainWindow);
        statementRTA.getActionFactory().open(originalModel.getExerciseStatement()).execute(new ActionEvent());
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        statementTextHeight = originalModel.getStatementTextHeight();
        nameField.setText(originalModel.getExerciseName());
        contentPromptField.setText(originalModel.getContentPrompt());
        ABmodelExtra fields = originalModel.getModelFields();
        leaderField.setText(fields.getLeader());
        promptFieldA.setText(fields.getPromptA());
        promptFieldB.setText(fields.getPromptB());

        fieldsModified = false;
        nameField.textProperty().addListener(nameListener);
        contentPromptField.textProperty().addListener(contentPromptListener);
        leaderField.textProperty().addListener(leaderListener);
        promptFieldA.textProperty().addListener(fieldListenerA);
        promptFieldB.textProperty().addListener(fieldListenerB);
    }

    /*
     * Set up the create view
     */
    private void setupWindow() {
        borderPane = new BorderPane();

        //empty bar for consistent look
        Menu helpMenu = new Menu("");
        menuBar = new MenuBar(helpMenu);

        statementDRTA = new DecoratedRTA();
        statementRTA = statementDRTA.getEditor();
        statementRTA.getStylesheets().add("slappTextArea.css");
        statementRTA.setPromptText("Exercise Statement:");

        statementRTA.setPrefWidth(PrintUtilities.getPageWidth());
    //    statementRTA.setContentAreaWidth(PrintUtilities.getPageWidth());
        statementRTA.setPrefHeight(200);

        statementRTA.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            fieldsModified = true;
            statementTextHeight = mainWindow.getMainView().getRTATextHeight(statementRTA);
        });

        statementRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(statementDRTA, ControlType.AREA);
            }
        });

        Label nameLabel = new Label("Exercise Name: ");
        nameLabel.setPrefWidth(100);
        nameField  = new TextField();
        nameField.setPromptText("(plain text)");
        nameListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldsModified = true;
                nameField.textProperty().removeListener(nameListener);
            }
        };
        nameField.textProperty().addListener(nameListener);

        nameField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        Label leaderLabel = new Label("Checkbox Lead: ");
        leaderLabel.setPrefWidth(100);
        leaderField = new TextField();
        leaderField.setPromptText("(plain text)");
        leaderListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldsModified = true;
                leaderField.textProperty().removeListener(leaderListener);
            }
        };
        leaderField.textProperty().addListener(leaderListener);

        leaderField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        Label promptLabelA = new Label("A Prompt: ");
        promptLabelA.setPrefWidth(100);
        promptFieldA = new TextField();
        promptFieldA.setPromptText("(plain text)");
        fieldListenerA = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldsModified = true;
                promptFieldA.textProperty().removeListener(fieldListenerA);
            }
        };
        promptFieldA.textProperty().addListener(fieldListenerA);

        promptFieldA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        Label promptLabelB = new Label("B Prompt: ");
        promptLabelB.setPrefWidth(100);
        promptFieldB = new TextField();
        promptFieldB.setPromptText("(plain text)");
        fieldListenerB = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldsModified = true;
                promptFieldB.textProperty().removeListener(fieldListenerB);
            }
        };
        promptFieldB.textProperty().addListener(fieldListenerB);

        promptFieldB.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        Label contentPromptLabel = new Label("Explain prompt: ");
        contentPromptLabel.setPrefWidth(100);
        contentPromptField = new TextField();
        contentPromptField.setPromptText("(plain text)");
        contentPromptListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldsModified = true;
                contentPromptField.textProperty().removeListener(contentPromptListener);
            }
        };
        contentPromptField.textProperty().addListener(contentPromptListener);

        contentPromptField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        HBox nameBox = new HBox(nameLabel, nameField);
        nameBox.setAlignment(Pos.BASELINE_LEFT);
        HBox promptBox = new HBox(contentPromptLabel, contentPromptField);
        promptBox.setAlignment(Pos.BASELINE_LEFT);
        HBox leaderBox = new HBox(leaderLabel, leaderField);
        leaderBox.setAlignment(Pos.BASELINE_LEFT);
        HBox aBbox = new HBox(promptLabelA, promptFieldA);
        promptBox.setAlignment(Pos.BASELINE_LEFT);
        HBox bBox = new HBox(promptLabelB, promptFieldB);
        bBox.setAlignment(Pos.BASELINE_LEFT);
        textFieldsPromptBox = new VBox(10,nameBox, promptBox, leaderBox, aBbox, bBox);
        textFieldsPromptBox.setPadding(new Insets(20,0,20,70));

        String helpText = "AB Explain is appropriate for any exercise that requires a choice between mutually exclusive options (as true/false, consistent/inconsistent) together with an explanation or justification.\n\n" +
                "For the AB Explain exercise, you supply the exercise name and, if desired, a prompt to appear in the explanation field.  Then the Checkbox Lead appears prior to the check boxes, the A Prompt with the first box, and the B Prompt with the second.";
        helpArea = new TextArea(helpText);
        helpArea.setWrapText(true);
        helpArea.setPrefHeight(120);
        helpArea.setEditable(false);
        helpArea.setFocusTraversable(false);
        helpArea.setMouseTransparent(true);
        helpArea.setStyle("-fx-text-fill: mediumslateblue");

        centerBox = new VBox(10, statementRTA, helpArea);

        Group centerGroup = new Group(centerBox);
        borderPane.setCenter(centerGroup);

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> closeWindow());

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> clearExercise());

        Button viewButton = new Button("View");
        viewButton.setOnAction(e -> viewExercise());

        saveButton = new Button ("Save");
        saveButton.setOnAction(e -> saveExercise(false));

        saveAsButton = new Button("Save As");
        saveAsButton.setOnAction(e -> saveExercise(true));

        HBox buttonBox = new HBox(saveAsButton, saveButton, viewButton, clearButton, closeButton);
        buttonBox.setSpacing(30);
        buttonBox.setPadding(new Insets(20,50,20,50));
        buttonBox.setAlignment(Pos.BASELINE_CENTER);
        borderPane.setBottom(buttonBox);

        if (EditorMain.secondaryCopy) {
            saveButton.setDisable(true);
            saveAsButton.setDisable(true);
        }

        scene = new Scene(borderPane);
        scene.getStylesheets().add(DecoratedRTA.class.getClassLoader().getResource("slappEditor.css").toExternalForm());

        Label zoomLabel = new Label(" Zoom ");
        Spinner<Integer> zoomSpinner = new Spinner(25, 500, 100, 5);
        zoomSpinner.setPrefSize(65,25);
        zoomSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = zoomSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = zoomSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);

            scale = (double) nv/100;
            updateZoom();
            scene.getWindow().setWidth(Math.max(860, PrintUtilities.getPageWidth() * scale + 55));
            setCenterVgrow();
        });

        sizeToolBar = new ToolBar();
        sizeToolBar.setPrefHeight(38);
        sizeToolBar.getItems().addAll(zoomLabel, zoomSpinner, new Label("     "));

        stage = new Stage();
        stage.initOwner(EditorMain.mainStage);
        stage.setScene(scene);
        stage.setTitle("Create AB Explain Exercise:");
        stage.getIcons().addAll(EditorMain.icons);




        stage.setWidth(860);
        stage.setHeight(800);

        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        stage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 860));
        stage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 800));




        stage.initModality(Modality.WINDOW_MODAL);
        stage.setOnCloseRequest(e-> {
            e.consume();
            closeWindow();
        });

        stage.show();
        statementRTA.getActionFactory().save().execute(new ActionEvent());
        centerBox.layout();
        setCenterVgrow();
        Platform.runLater(() -> nameField.requestFocus());
    }

    /*
     * Update zoom value
     */
    private void updateZoom() {
        centerBox.setScaleX(scale);
        centerBox.setScaleY(scale);
        scene.getWindow().setWidth(Math.max(860, PrintUtilities.getPageWidth() * scale + 55));
        setCenterVgrow();

        KeyboardDiagram keyboardDiagram = KeyboardDiagram.getInstance();
        keyboardDiagram.updateFontSize(scale);
        keyboardDiagram.initialize(statementDRTA);
        keyboardDiagram.update();
    }

    /*
     * The center statement area sizes as the stage is sized
     */
    private void setCenterVgrow() {
        double fixedHeight = helpArea.getHeight() * scale + 500;
        DoubleProperty fixedValueProperty = new SimpleDoubleProperty(fixedHeight);
        DoubleProperty maximumHeightProperty = new SimpleDoubleProperty(PrintUtilities.getPageHeight() );
        DoubleProperty scaleProperty = new SimpleDoubleProperty(scale);
        centerHeightProperty = new SimpleDoubleProperty();
        centerHeightProperty.bind(Bindings.min(maximumHeightProperty, (stage.heightProperty().subtract(fixedValueProperty)).divide(scaleProperty)));
        statementRTA.prefHeightProperty().bind(centerHeightProperty);
    }

    /*
     * Close the create window
     */
    private void closeWindow() {
        if (checkContinue("Confirm Close", "This exercise appears to have unsaved changes.\nContinue to close window?")) {
            mainWindow.closeExercise();
            stage.close();
        }
    }

    /*
     * Clear the exercise (leaving explain prompt and checkbox setup)
     */
    private void clearExercise() {
        if (checkContinue("Confirm Clear", "This exercise appears to have unsaved changes.\nContinue to clear exercise?")) {
            nameField.clear();
            nameField.textProperty().addListener(nameListener);
            statementRTA.getActionFactory().newDocumentNow().execute(new ActionEvent());
 //           statementRTA.getActionFactory().open(new Document()).execute(new ActionEvent());
            statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
            viewExercise();
            fieldsModified = false;
        }
    }

    /*
     * If modified, check to continue action
     * @param title the title of the confirmation box
     * @param content the content of the confirmation box
     * @return true if ok to continue, and otherwise false
     */
    private boolean checkContinue(String title, String content) {
        boolean okcontinue = true;
        if (fieldsModified || statementRTA.isModified()) {
            Alert confirm = EditorAlerts.confirmationAlert(title, content);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.get() != OK) okcontinue = false;
        }
        return okcontinue;
    }

    /*
     * View the exercise as currently constructed
     */
    private void viewExercise() {
        ABmodel model = extractModelFromWindow();
        ABexercise exercise = new ABexercise(model, mainWindow);
        RichTextArea rta = exercise.getExerciseView().getExerciseStatement().getEditor();
        rta.prefHeightProperty().unbind();
        rta.setEditable(false);
        exercise.getExerciseView().setStatementPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getStatementPrefHeight()));
        mainWindow.setUpExercise(exercise);
    }

    /*
     * Save the currently constructed exercise
     * @param saveAs true if "save as" selected, and otherwise false
     */
    private void saveExercise(boolean saveAs) {
        saveButton.setDisable(true);
        saveAsButton.setDisable(true);
        nameField.textProperty().addListener(nameListener);

        ABmodel model = extractModelFromWindow();
        ABexercise exercise = new ABexercise(model, mainWindow);
        RichTextArea rta = exercise.getExerciseView().getExerciseStatement().getEditor();
        rta.setEditable(true);
        rta.prefHeightProperty().unbind();
        rta.setEditable(false);
        exercise.getExerciseView().setStatementPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getStatementPrefHeight()));

        exercise.saveExercise(saveAs);
        saveButton.setDisable(false);
        saveAsButton.setDisable(false);
        fieldsModified = false;
    }

    /*
     * Generate an AB EditModel from window contents
     * @return the simple edit model
     */
    private ABmodel extractModelFromWindow() {
        String name = nameField.getText();
        String leader = leaderField.getText();
        String Aprompt = promptFieldA.getText();
        String Bprompt = promptFieldB.getText();
        ABmodelExtra fields = new ABmodelExtra(leader, Aprompt, false, Bprompt, false);
        String prompt = contentPromptField.getText();
        if (statementRTA.isModified()) fieldsModified = true;
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document statementDocument = statementRTA.getDocument();
        Document commentDoc = new Document();
        double statementPrefHeight = statementTextHeight + 25;
        ABmodel model = new ABmodel(name, fields,  false, prompt, statementPrefHeight, statementDocument, commentDoc, new ArrayList<PageContent>());
        model.setStatementTextHeight(statementTextHeight);
        return model;
    }

    /*
     * Display and enable RTA toolbars
     * @param decoratedRTA the RTA
     * @param control the {@link slapp.editor.main_window.ControlType} for this RTA instance
     */
    private void editorInFocus(DecoratedRTA decoratedRTA, ControlType control) {

        KeyboardDiagram keyboardDiagram = KeyboardDiagram.getInstance();
        keyboardDiagram.initialize(decoratedRTA);
        if (keyboardDiagram.isShowing()) {
            keyboardDiagram.updateAndShow();
        }

        ToolBar paragraphToolbar = decoratedRTA.getParagraphToolbar();
        paragraphToolbar.setMinWidth(870);

        ToolBar fontsToolbar = decoratedRTA.getFontsToolbar();
        fontsToolbar.setMinWidth(520);

        ToolBar editToolbar = decoratedRTA.getEditToolbar();
        editToolbar.setMinWidth(300);

        ToolBar kbdSelectorToolBar = decoratedRTA.getKbdSelectorToolbar();
        ToolBar kbdDiaToolBar = decoratedRTA.getKbdDiaToolbar();


        editToolbar.setPrefHeight(38);
        fontsToolbar.setPrefHeight(38);
        kbdDiaToolBar.setPrefHeight(38);

        //this "cascades" disable requests starting from the control type -- so if NONE, all are disabled, etc.
        switch (control) {
            case NONE: {
                kbdDiaToolBar.setDisable(true);
            }
            case STATEMENT: {
                editToolbar.setDisable(true);
                kbdSelectorToolBar.setDisable(true);
            }
            case JUSTIFICATION: {
                fontsToolbar.setDisable(true);
            }
            case FIELD: {
                paragraphToolbar.setDisable(true);
            }
            case AREA: { }
        }
        sizeToolBar.setDisable(kbdDiaToolBar.isDisable());


        HBox fontsAndEditBox = new HBox(fontsToolbar, editToolbar);
        HBox kbdBox = new HBox(kbdSelectorToolBar, kbdDiaToolBar, sizeToolBar);

        fontsAndEditBox.setHgrow(editToolbar, Priority.ALWAYS);
        kbdBox.setHgrow(sizeToolBar, Priority.ALWAYS);

        VBox topBox = new VBox(menuBar, paragraphToolbar, fontsAndEditBox, kbdBox, textFieldsPromptBox);

        borderPane.topProperty().setValue(topBox);
    }

    /*
     * When a text field is in focus, show deactivated toolbars for dummy RTA
     */
    private void textFieldInFocus() {
        editorInFocus(dummyDRTA, ControlType.STATEMENT);
    }

}

