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

package slapp.editor.simple_trans;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
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
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import slapp.editor.DiskUtilities;
import slapp.editor.EditorAlerts;
import slapp.editor.EditorMain;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.decorated_rta.KeyboardDiagram;
import slapp.editor.main_window.ControlType;
import slapp.editor.main_window.MainWindow;
import slapp.editor.main_window.MainWindowView;

import java.util.Optional;
import java.util.function.UnaryOperator;

import static javafx.scene.control.ButtonType.OK;

/**
 * Create window for the simple translate exercise
 */
public class SimpleTransCreate {
    private MainWindow mainWindow;
    private MenuBar menuBar;
    private BorderPane borderPane;
    private RichTextArea statementRTA;
    private DecoratedRTA statementDRTA;
    private double statementTextHeight;
    private RichTextArea interpretationRTA;
    private DecoratedRTA interpretationDRTA;
    private double interpretationTextHeight;
    private TextField nameField;
    private boolean fieldModified = false;
    private ChangeListener nameListener;
    private ChangeListener promptListener;
    private double scale =1.0;
    private Scene scene;
    private Stage stage;
    private SimpleDoubleProperty centerHeightProperty;
    private TextArea helpArea;
    private VBox centerBox;
    private Button saveButton;
    private Button saveAsButton;
    private ToolBar sizeToolBar;
    private VBox nameVBox;
    private DecoratedRTA dummyDRTA = new DecoratedRTA();

    private Pane spacerPane;
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> interpretationHeightSpinner;
    private Spinner<Double> verticalSizeSpinner;
    private RichTextArea currentSpinnerNode;
    private TextField pointsPossibleTextField;

    /**
     * Construct the simple translate create window from scratch
     * @param mainWindow the main window
     */
    public SimpleTransCreate(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setupWindow();
    }

    /**
     * Open create window for modification of existing simple translate exercise.
     * @param mainWindow the main window
     * @param originalModel model for the existing exercise
     */
    public SimpleTransCreate(MainWindow mainWindow, SimpleTransModel originalModel) {
        this(mainWindow);

        statementRTA.getActionFactory().open(originalModel.getExerciseStatement()).execute(new ActionEvent());
//        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        statementTextHeight = originalModel.getStatementTextHeight();

        interpretationRTA.getActionFactory().open(originalModel.getExerciseInterpretation()).execute(new ActionEvent());
//        interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());
        interpretationTextHeight = originalModel.getInterpretationTextHeight();

        nameField.setText(originalModel.getExerciseName());
        nameField.textProperty().addListener(nameListener);
        pointsPossibleTextField.setText(Integer.toString(originalModel.getPointsPossible()));
        fieldModified = false;
    }

    /*
     * Set up the create view
     */
    private void setupWindow() {

        borderPane = new BorderPane();

        verticalSizeSpinner = new Spinner<>(0.0, 999.0, 0.0, 5.0);
        verticalSizeSpinner.setPrefWidth(65);
        verticalSizeSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        verticalSizeSpinner.setDisable(true);

        //empty bar for consistent look
        Menu helpMenu = new Menu("");
        menuBar = new MenuBar(helpMenu);

        statementDRTA = new DecoratedRTA();
        statementRTA = statementDRTA.getEditor();
        statementRTA.setPromptText("Exercise Statement:");
        statementRTA.getStylesheets().add("slappTextArea.css");
        statementRTA.setPrefWidth(PrintUtilities.getPageWidth() );
        statementRTA.setMinWidth(PrintUtilities.getPageWidth());
        statementRTA.setMaxWidth(PrintUtilities.getPageWidth());

        statementRTA.setMinHeight(100);

        double statementInitialHeight = Math.round(100 / mainWindow.getMainView().getScalePageHeight() * 100.0);
        statementHeightSpinner = new Spinner<>(0.0, 999.0, statementInitialHeight, 1.0);
        statementHeightSpinner.setPrefWidth(60);
        statementHeightSpinner.setDisable(false);
        statementHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        statementRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(mainWindow.getMainView().scalePageHeightProperty(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));
        statementHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = statementHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = statementHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        mainWindow.getMainView().scalePageHeightProperty().addListener((ob, ov, nv) -> {
            statementRTA.prefHeightProperty().unbind();
            statementHeightSpinner.getValueFactory().setValue((double) Math.round(statementHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            statementRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

        });

        statementRTA.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            fieldModified = true;
            statementTextHeight = mainWindow.getMainView().getRTATextHeight(statementRTA);
        });
        statementRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(statementDRTA, ControlType.AREA);
            }
        });
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());

        interpretationDRTA = new DecoratedRTA();
        interpretationDRTA.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.BASE_AND_SANS);
        interpretationRTA = interpretationDRTA.getEditor();
        interpretationRTA.setPromptText("Interpretation Function (may be blank)");
        interpretationRTA.getStylesheets().add("slappTextArea.css");
        interpretationRTA.setPrefWidth(PrintUtilities.getPageWidth() );
        interpretationRTA.setMinWidth(PrintUtilities.getPageWidth());
        interpretationRTA.setMaxWidth(PrintUtilities.getPageWidth());
        interpretationRTA.setMinHeight(100);

        double interpretationInitialHeight = Math.round(100 / mainWindow.getMainView().getScalePageHeight() * 100 );


        interpretationHeightSpinner = new Spinner<>(0.0, 999.0, interpretationInitialHeight, 1.0);
        interpretationHeightSpinner.setPrefWidth(60);
        interpretationHeightSpinner.setDisable(false);
        interpretationHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        interpretationRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(mainWindow.getMainView().scalePageHeightProperty(), DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));
        interpretationHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = interpretationHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = interpretationHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        mainWindow.getMainView().scalePageHeightProperty().addListener((ob, ov, nv) -> {
            interpretationRTA.prefHeightProperty().unbind();
            interpretationHeightSpinner.getValueFactory().setValue((double) Math.round(interpretationHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            interpretationRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

        });

        interpretationRTA.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            fieldModified = true;
            interpretationTextHeight = mainWindow.getMainView().getRTATextHeight(interpretationRTA);
        });
        interpretationRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(interpretationDRTA, ControlType.AREA);
            }
        });
        interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());

        Label nameLabel = new Label("Exercise Name: ");
        nameLabel.setPrefWidth(95);
        nameField  = new TextField();
        nameField.setPromptText("(plain text)");
        nameListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
                nameField.textProperty().removeListener(nameListener);
            }
        };
        nameField.textProperty().addListener(nameListener);

        nameField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        Label pointsPossibleLabel = new Label("Points Possible: ");
        pointsPossibleLabel.setPrefWidth(95);
        pointsPossibleTextField = new TextField();
        pointsPossibleTextField.setPrefWidth(35);
        pointsPossibleTextField.setPadding(new Insets(5,5,5,5));
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getText();
            if (text.matches("[0-9]*")) {
                return change;
            }
            return null;
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        pointsPossibleTextField.setTextFormatter(textFormatter);
        pointsPossibleTextField.setText("0");
        pointsPossibleTextField.textProperty().addListener((ob,ov,nv) -> { fieldModified = true; });
        pointsPossibleTextField.focusedProperty().addListener((ob, ov, nv) -> { if (nv) textFieldInFocus();  });


        HBox nameBox = new HBox(nameLabel, nameField);
        nameBox.setAlignment(Pos.BASELINE_LEFT);
        HBox pointsBox = new HBox(pointsPossibleLabel, pointsPossibleTextField);
        pointsBox.setAlignment(Pos.BASELINE_LEFT);

        VBox nameAndPointsBox = new VBox(10, nameBox, pointsBox);
        nameAndPointsBox.setPadding(new Insets(20,0,20,0));




        String helpText = "<body style=\"margin-left:10; margin-right: 20\">" +
                "<p>Simple Translation is appropriate for any exercise that calls for an interpretation function and simple formal translation (usually of an ordinary language sentence).<p>" +
                "<ul>" +
                "<li><p>Provide the exercise name.</p></li>" +
                "<li><p>If 'points possible' is other than zero, a points field is added to the exercise comment area (and one for total assignment points into the assignment comment area).</p></li>" +
                "<li><p>Provide the exercise statement.</p></li>" +
                "<li><p>If desired, you may include an interpretation function to appear along with the exercise.  If this field is left blank, an empty field for an interpretation function that is to be filled in by the student appears along with the exercise.</p></li>" +
                "</ul>";
        WebView helpArea = new WebView();
        WebEngine webEngine = helpArea.getEngine();
        webEngine.setUserStyleSheetLocation("data:, body {font: 14px Noto Serif Combo; }");
        webEngine.loadContent(helpText);
        helpArea.setPrefHeight(250);

        centerBox = new VBox(10, nameAndPointsBox, statementRTA, interpretationRTA, helpArea);
        centerBox.setPadding(new Insets(10,0,10,20));

        spacerPane = new Pane();
        spacerPane.prefHeightProperty().bind(centerBox.heightProperty());
        spacerPane.prefWidthProperty().bind(centerBox.widthProperty());
        Group group = new Group(spacerPane);
        AnchorPane comboPane = new AnchorPane(group, centerBox);
        ScrollPane centerPane = new ScrollPane(comboPane);


        borderPane.setCenter(centerPane);

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
        });


        sizeToolBar = new ToolBar();
        sizeToolBar.setPrefHeight(38);
        sizeToolBar.getItems().addAll(zoomLabel, zoomSpinner, new Label("     V Sz:"), verticalSizeSpinner);

        setSizeSpinners();

        stage = new Stage();
        stage.initOwner(EditorMain.mainStage);
        stage.setScene(scene);
        stage.setTitle("Create Simple Translation Exercise:");
        stage.getIcons().addAll(EditorMain.icons);

        stage.setWidth(890);
        stage.setHeight(860);
        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        stage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 890));
        stage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 860));

        stage.initModality(Modality.WINDOW_MODAL);
        stage.setOnCloseRequest(e-> {
            e.consume();
            closeWindow();
        });

        stage.show();
        statementRTA.getActionFactory().save().execute(new ActionEvent());
        centerBox.layout();

        Platform.runLater(() -> nameField.requestFocus());
    }

    /*
     * Update the zoom value
     */
    private void updateZoom() {
        KeyboardDiagram keyboardDiagram = KeyboardDiagram.getInstance();
        keyboardDiagram.updateFontSize(scale);
        keyboardDiagram.initialize(statementDRTA);
        keyboardDiagram.update();

        centerBox.getTransforms().clear();
        centerBox.getTransforms().add(new Scale(scale, scale));
        spacerPane.getTransforms().clear();
        spacerPane.getTransforms().add(new Scale(scale, scale));
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
     * Clear the exercise (leaving the interpretation area intact)
     */
    private void clearExercise() {
        if (checkContinue("Confirm Clear", "This exercise appears to have unsaved changes.\nContinue to clear exercise?")) {
            nameField.clear();
            nameField.textProperty().addListener(nameListener);
            statementRTA.getActionFactory().newDocumentNow().execute(new ActionEvent());
   //         statementRTA.getActionFactory().open(new Document()).execute(new ActionEvent());
            viewExercise();
            fieldModified = false;
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

        if (fieldModified || statementRTA.isModified() || interpretationRTA.isModified()) {
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
        SimpleTransModel model = extractModelFromWindow();
        SimpleTransExercise exercise = new SimpleTransExercise(model, mainWindow);
        RichTextArea rta = exercise.getExerciseView().getExerciseStatement().getEditor();
        rta.prefHeightProperty().unbind();
        rta.setEditable(false);
        exercise.getExerciseView().setStatementPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getStatementPrefHeight()));

        RichTextArea interpRTA = exercise.getExerciseView().getExerciseInterpretation().getEditor();
        interpRTA.prefHeightProperty().unbind();
        exercise.getExerciseView().setInterpretationPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getInterpretationPrefHeight()));

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

        SimpleTransModel model = extractModelFromWindow();
        boolean success = DiskUtilities.saveExercise(saveAs, model);
        if (success) {
            fieldModified = false;
        }
        saveButton.setDisable(false);
        saveAsButton.setDisable(false);
    }

    /*
     * Generate simple translate model from window contents
     * @return the simple translate model
     */
    private SimpleTransModel extractModelFromWindow() {
        SimpleTransModel model = new SimpleTransModel(nameField.getText());
        if (statementRTA.isModified() || interpretationRTA.isModified()) fieldModified = true;

        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseStatement(statementRTA.getDocument());
        model.setStatementTextHeight(statementTextHeight);
        model.setStatementPrefHeight(statementTextHeight + 29);

        interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseInterpretation(interpretationRTA.getDocument());

        model.setInterpretationTextHeight(interpretationTextHeight);
        model.setInterpretationPrefHeight(Math.max(100, interpretationTextHeight + 25));

        if (!pointsPossibleTextField.getText().equals("")) model.setPointsPossible(Integer.parseInt(pointsPossibleTextField.getText()));
        else {
            model.setPointsPossible(0);
            pointsPossibleTextField.setText("0");
        }

        return model;
    }

    private void setSizeSpinners() {

        scene.focusOwnerProperty().addListener((ob, ov, nv) -> {

            if (inHierarchy(nv, statementRTA) && currentSpinnerNode != statementRTA) {
                currentSpinnerNode = statementRTA;
                sizeToolBar.getItems().remove(3);
                sizeToolBar.getItems().add(3, statementHeightSpinner);
                return;
            }
            if (inHierarchy(nv, interpretationRTA) && currentSpinnerNode != interpretationRTA) {
                currentSpinnerNode = interpretationRTA;
                sizeToolBar.getItems().remove(3);
                sizeToolBar.getItems().add(3, interpretationHeightSpinner);
                return;
            }
        });
    }

    private static boolean inHierarchy(Node node, Node potentialHierarchyElement) {
        if (potentialHierarchyElement == null) {
            return true;
        }
        while (node != null) {
            if (node == potentialHierarchyElement) {
                return true;
            }
            node = node.getParent();
        }
        return false;
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

        /*
        ToolBar editToolbar = decoratedRTA.getKbdSelectorToolbar();
        ToolBar fontsToolbar = decoratedRTA.getEditToolbar();
        ToolBar paragraphToolbar = decoratedRTA.getParagraphToolbar();
        ToolBar kbdDiaToolBar = decoratedRTA.getKbdDiaToolbar();
        kbdDiaToolBar.setPrefHeight(38);

        switch (control) {
            case NONE: {
                kbdDiaToolBar.setDisable(true);
            }
            case STATEMENT: {
                editToolbar.setDisable(true);
                fontsToolbar.setDisable(true);
            }
            case FIELD: {
                paragraphToolbar.setDisable(true);
            }
            case AREA: { }
        }
        sizeToolBar.setDisable(kbdDiaToolBar.isDisable());

        HBox editAndKbdBox = new HBox(editToolbar, sizeToolBar, kbdDiaToolBar);
        editAndKbdBox.setHgrow(kbdDiaToolBar, Priority.ALWAYS);
        editAndKbdBox.layout();

         */
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

        VBox topBox = new VBox(menuBar, paragraphToolbar, fontsAndEditBox, kbdBox);


        borderPane.topProperty().setValue(topBox);
    }

    /*
     * When a text field is in focus, show deactivated toolbars for dummy RTA
     */
    private void textFieldInFocus() {
        editorInFocus(dummyDRTA, ControlType.STATEMENT);
    }



}
