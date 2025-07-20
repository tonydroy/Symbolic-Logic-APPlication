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

package slapp.editor.vertical_tree;

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
import slapp.editor.EditorAlerts;
import slapp.editor.EditorMain;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.decorated_rta.KeyboardDiagram;
import slapp.editor.main_window.ControlType;
import slapp.editor.main_window.MainWindow;
import slapp.editor.main_window.MainWindowView;
import slapp.editor.vertical_tree.drag_drop.DragIconType;
import slapp.editor.vertical_tree.object_models.ObjectControlType;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.gluonhq.richtextarea.RichTextAreaSkin.KeyMapValue.*;
import static javafx.scene.control.ButtonType.OK;
import static slapp.editor.vertical_tree.drag_drop.DragIconType.*;
import static slapp.editor.vertical_tree.object_models.ObjectControlType.*;

public class VerticalTreeCreate {
    private MainWindow mainWindow;
    private TextField nameField;
    private DecoratedRTA statementDRTA;
    private RichTextArea statementRTA;
    private double statementTextHeight;
    private TextArea helpArea;
    private ChangeListener nameListener;
    private boolean modified = false;
    private CheckBox treeBoxBaseItalicCheck;
    private CheckBox treeBoxItalicSansCheck;
    private CheckBox treeBoxScriptItalicCheck;
    private CheckBox mapBoxBaseItalicCheck;
    private CheckBox mapBoxItalicSansCheck;
    private CheckBox mapBoxScriptItalicCheck;
    private CheckBox verticalBracketCheck;
    private CheckBox dashedLineCheck;
    private CheckBox boxingFormulaCheck;
    private CheckBox circleCheck;
    private CheckBox starCheck;
    private CheckBox annotationCheck;
    private CheckBox underlineCheck;
    private CheckBox mappingCheck;


    private RichTextAreaSkin.KeyMapValue treeBoxKeyboardSelector;
    private RichTextAreaSkin.KeyMapValue mapBoxKeyboardSelector;
    private double scale = 1.0;
    private Stage stage;
    private Scene scene;
    private VBox centerBox;
    private SimpleDoubleProperty centerHeightProperty;
    private Button saveButton;
    private Button saveAsButton;
    private ToolBar sizeToolBar;
    private DecoratedRTA dummyDRTA = new DecoratedRTA();
    private MenuBar menuBar;
    private VBox fieldsBox;
    private BorderPane borderPane;

    private Pane spacerPane;
    private Spinner<Double> statementHeightSpinner;
    private TextField pointsPossibleTextField;


    public VerticalTreeCreate(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setupWindow();
    }

    public VerticalTreeCreate(MainWindow mainWindow, VerticalTreeModel originalModel) {
        this(mainWindow);

        statementRTA.getActionFactory().open(originalModel.getExerciseStatement()).execute(new ActionEvent());
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        statementTextHeight = originalModel.getStatementTextHeight();
        nameField.setText(originalModel.getExerciseName());
        treeBoxKeyboardSelector = originalModel.getDefaultKeyboardType();
        mapBoxKeyboardSelector = originalModel.getDefaultMapKeyboardType();

        List<DragIconType> dragIconList = originalModel.getDragIconList();
        treeBoxItalicSansCheck.setSelected(dragIconList.contains(tree_field) && treeBoxKeyboardSelector == ITALIC_AND_SANS);
        treeBoxBaseItalicCheck.setSelected(dragIconList.contains(tree_field) && treeBoxKeyboardSelector == BASE);
        treeBoxScriptItalicCheck.setSelected(dragIconList.contains(tree_field) && treeBoxKeyboardSelector == SCRIPT_AND_ITALIC);
        mapBoxItalicSansCheck.setSelected(dragIconList.contains(map_field) && mapBoxKeyboardSelector == ITALIC_AND_SANS);
        mapBoxBaseItalicCheck.setSelected(dragIconList.contains(map_field) && mapBoxKeyboardSelector == BASE);
        mapBoxScriptItalicCheck.setSelected(dragIconList.contains(map_field) && mapBoxKeyboardSelector == SCRIPT_AND_ITALIC);

        pointsPossibleTextField.setText(Integer.toString(originalModel.getPointsPossible()));
        verticalBracketCheck.setSelected(dragIconList.contains(bracket));
        dashedLineCheck.setSelected(dragIconList.contains(dashed_line));
        List<ObjectControlType> objectControlList = originalModel.getObjectControlList();
        boxingFormulaCheck.setSelected(objectControlList.contains(FORMULA_BOX));
        circleCheck.setSelected(objectControlList.contains(CIRCLE));
        starCheck.setSelected(objectControlList.contains(STAR));
        annotationCheck.setSelected(objectControlList.contains(ANNOTATION));
        underlineCheck.setSelected(objectControlList.contains(UNDERLINE));
        mappingCheck.setSelected(objectControlList.contains(MAPPING));
        modified = false;
    }

    private void setupWindow() {
        borderPane = new BorderPane();

        //empty bar for consistent look
        Menu helpMenu = new Menu("");
        menuBar = new MenuBar(helpMenu);

        //statementDRTA
        statementDRTA = new DecoratedRTA();
        statementRTA = statementDRTA.getEditor();
        statementRTA.getStylesheets().add("slappTextArea.css");
        statementRTA.setPromptText("Exercise Statement:");

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
            modified = true;
            statementTextHeight = mainWindow.getMainView().getRTATextHeight(statementRTA);
        });

        statementRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(statementDRTA, ControlType.AREA);
            }
        });

        //name
        Label nameLabel = new Label("Exercise Name: ");
        nameLabel.setPrefWidth(100);
        nameField  = new TextField();
        nameField.setPromptText("(plain text)");
        nameListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                modified = true;
                nameField.textProperty().removeListener(nameListener);
            }
        };
        nameField.textProperty().addListener(nameListener);
        nameField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });
        HBox nameBox = new HBox(nameLabel, nameField);
        nameBox.setAlignment(Pos.BASELINE_LEFT);

        Label pointsPossibleLabel = new Label("Points Possible: ");
        pointsPossibleLabel.setPrefWidth(100);
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
        pointsPossibleTextField.textProperty().addListener((ob,ov,nv) -> { modified = true; });
        pointsPossibleTextField.focusedProperty().addListener((ob, ov, nv) -> { if (nv) textFieldInFocus();  });
        HBox pointsBox = new HBox(pointsPossibleLabel, pointsPossibleTextField);
        pointsBox.setAlignment(Pos.BASELINE_LEFT);


        Label dragBarLabel = new Label("Drag Bar: ");
        dragBarLabel.setPrefWidth(80);

        Label treeBoxLabel = new Label("Tree Box: ");
        treeBoxBaseItalicCheck = new CheckBox("Base/Italic");
        treeBoxBaseItalicCheck.setSelected(false);
        treeBoxBaseItalicCheck.selectedProperty().addListener((ob, ov, nv) -> {
            boolean selected = (boolean) nv;
            if (selected) {
                modified = true;
                treeBoxItalicSansCheck.setSelected(false);
                treeBoxScriptItalicCheck.setSelected(false);
            }
        });
        treeBoxItalicSansCheck = new CheckBox("Italic/Sans");
        treeBoxItalicSansCheck.setSelected(false);
        treeBoxItalicSansCheck.selectedProperty().addListener((ob, ov, nv) -> {
            boolean selected = (boolean) nv;
            if (selected) {
                modified = true;
                treeBoxBaseItalicCheck.setSelected(false);
                treeBoxScriptItalicCheck.setSelected(false);
            }
        });
        treeBoxScriptItalicCheck = new CheckBox("Script/Italic");
        treeBoxScriptItalicCheck.setSelected(false);
        treeBoxScriptItalicCheck.selectedProperty().addListener((ob, ov, nv) -> {
            boolean selected = (boolean) nv;
            if (selected) {
                modified = true;
                treeBoxBaseItalicCheck.setSelected(false);
                treeBoxItalicSansCheck.setSelected(false);
            }
        });
        HBox treeFormulaBox = new HBox(20, treeBoxLabel, treeBoxBaseItalicCheck, treeBoxItalicSansCheck, treeBoxScriptItalicCheck);
        treeFormulaBox.setAlignment(Pos.BASELINE_LEFT);

        Label mapBoxLabel = new Label("Map Box: ");
        mapBoxBaseItalicCheck = new CheckBox("Base/Italic");
        mapBoxBaseItalicCheck.setSelected(false);
        mapBoxBaseItalicCheck.selectedProperty().addListener((ob, ov, nv) -> {
            boolean selected = (boolean) nv;
            if (selected) {
                modified = true;
                mapBoxItalicSansCheck.setSelected(false);
                mapBoxScriptItalicCheck.setSelected(false);
            }
        });
        mapBoxItalicSansCheck = new CheckBox("Italic/Sans");
        mapBoxItalicSansCheck.setSelected(false);
        mapBoxItalicSansCheck.selectedProperty().addListener((ob, ov, nv) -> {
            boolean selected = (boolean) nv;
            if (selected) {
                modified = true;
                mapBoxBaseItalicCheck.setSelected(false);
                mapBoxScriptItalicCheck.setSelected(false);
            }
        });
        mapBoxScriptItalicCheck = new CheckBox("Script/Italic");
        mapBoxScriptItalicCheck.setSelected(false);
        mapBoxScriptItalicCheck.selectedProperty().addListener((ob, ov, nv) -> {
            boolean selected = (boolean) nv;
            if (selected) {
                modified = true;
                mapBoxBaseItalicCheck.setSelected(false);
                mapBoxItalicSansCheck.setSelected(false);
            }
        });
        HBox mapFormulaBox = new HBox(20, mapBoxLabel, mapBoxBaseItalicCheck, mapBoxItalicSansCheck, mapBoxScriptItalicCheck);
        mapFormulaBox.setAlignment(Pos.BASELINE_LEFT);

        verticalBracketCheck = new CheckBox("Vertical Bracket");
        verticalBracketCheck.setSelected(false);
        verticalBracketCheck.selectedProperty().addListener((ob, ov, nv) -> { modified = true; });
        dashedLineCheck = new CheckBox("Dashed Line");
        dashedLineCheck.setSelected(false);
        dashedLineCheck.selectedProperty().addListener((ob, ov, nv) -> { modified = true; });

        HBox bracketLineChecks = new HBox(20, verticalBracketCheck, dashedLineCheck);

        VBox formulaBoxes = new VBox(15, treeFormulaBox, mapFormulaBox, bracketLineChecks);
        formulaBoxes.setAlignment(Pos.TOP_LEFT);
        HBox dragBox = new HBox(20, dragBarLabel, formulaBoxes);
        dragBox.setAlignment(Pos.TOP_LEFT);

        //control pane
        Label controlLabel = new Label("Controls Pane: ");
        controlLabel.setPrefWidth(80);

        boxingFormulaCheck = new CheckBox("Box Button");
        boxingFormulaCheck.setSelected(false);
        boxingFormulaCheck.selectedProperty().addListener((ob, ov, nv) -> { modified = true; });
        circleCheck = new CheckBox("Circle Button");
        circleCheck.setSelected(false);
        circleCheck.selectedProperty().addListener((ob, ov, nv) -> { modified = true; });
        starCheck  = new CheckBox("Star Button");
        starCheck.setSelected(false);
        starCheck.selectedProperty().addListener((ob, ov, nv) -> { modified = true; });
        annotationCheck  = new CheckBox("Annotation Button");
        annotationCheck.setSelected(false);
        annotationCheck.selectedProperty().addListener((ob, ov, nv) -> { modified = true; });
        underlineCheck  = new CheckBox("Underline Button");
        underlineCheck.setSelected(false);
        underlineCheck.selectedProperty().addListener((ob, ov, nv) -> { modified = true; });
        mappingCheck  = new CheckBox("Mapping Button");
        mappingCheck.setSelected(false);
        mappingCheck.selectedProperty().addListener((ob, ov, nv) -> { modified = true; });
        HBox controlBox = new HBox(20,controlLabel, boxingFormulaCheck, circleCheck, starCheck, annotationCheck, underlineCheck, mappingCheck);
        controlBox.setAlignment(Pos.BASELINE_LEFT);

        fieldsBox = new VBox(15, nameBox, pointsBox, dragBox, controlBox);
        fieldsBox.setPadding(new Insets(20,0, 0, 0));

        //center
        String helpText = "<body style=\"margin-left:10; margin-right: 20\">" +
                "<p>The Vertical Tree Exercise is appropriate for any exercise that builds a tree diagram from the top down (or bottom up), as well as for 'mapping' exercises (as from chapter 2 of <em>Symbolic Logic</em>).</p>" +
                "<ul>" +
                "<li><p>Start with the exercise name and points fields.  If 'points possible' is other than zero, a points field is added to the exercise comment area (and one for total assignment points into the assignment comment area).</p></li>" +
                "<li><p>Use checkboxes to select items that may be dragged into the work area (to appear across the top), and then to select controls for functions applied to the formula boxes (to appear down the left).</p>" +
                "<p>It is unlikely that any one exercise will include all the drag and control options (especially \"star\" and \"annotation\" cannot both apply to the same node) -- but the different options make it possible to accomodate a wide variety of exercises.</p></li>" +
                "<li><p>The keyboard options set the default keyboard for formula boxes.</p></li>" +
                "<li><p>Finally provide the exercise statement.</p></li>" +
                "</ul>";
        WebView helpArea = new WebView();
        WebEngine webEngine = helpArea.getEngine();
        webEngine.setUserStyleSheetLocation("data:, body {font: 14px Noto Serif Combo; }");
        webEngine.loadContent(helpText);
        helpArea.setPrefHeight(230);



        centerBox = new VBox(10, fieldsBox, statementRTA, helpArea);
        centerBox.setPadding(new Insets(10,0,10,20));

        spacerPane = new Pane();
        spacerPane.prefHeightProperty().bind(centerBox.heightProperty());
        spacerPane.prefWidthProperty().bind(centerBox.widthProperty());
        Group group = new Group(spacerPane);
        AnchorPane comboPane = new AnchorPane(group, centerBox);
        ScrollPane centerPane = new ScrollPane(comboPane);

        borderPane.setCenter(centerPane);

        //bottom
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


        //finish up with top
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
        sizeToolBar.getItems().addAll(zoomLabel, zoomSpinner, new Label("     V Sz:"), statementHeightSpinner);


        //generate view
        scene = new Scene(borderPane);
        scene.getStylesheets().add(DecoratedRTA.class.getClassLoader().getResource("slappEditor.css").toExternalForm());

        stage = new Stage();
        stage.initOwner(EditorMain.mainStage);
        stage.setScene(scene);
        stage.setTitle("Create Vertical Tree Exercise:");
        stage.getIcons().addAll(EditorMain.icons);

        stage.setWidth(890);
        stage.setHeight(820);

        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        stage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 890));
        stage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 820));
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


    private boolean checkContinue(String title, String content) {
        boolean okcontinue = true;
        if (modified || statementRTA.isModified()) {
            Alert confirm = EditorAlerts.confirmationAlert(title, content);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.get() != OK) okcontinue = false;
        }
        return okcontinue;
    }

    private void closeWindow() {
        if (checkContinue("Confirm Close", "This exercise appears to have unsaved changes.\nContinue to close window?")) {
            mainWindow.closeExercise();
            stage.close();
        }
    }


    private void clearExercise() {
        if (checkContinue("Confirm Clear", "This exercise appears to have unsaved changes.\nContinue to clear exercise?")) {
            nameField.clear();
            nameField.textProperty().addListener(nameListener);
            statementRTA.getActionFactory().newDocumentNow().execute(new ActionEvent());
 //           statementRTA.getActionFactory().open(new Document()).execute(new ActionEvent());
  //          statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
            viewExercise();
            modified = false;
        }
    }
    private void viewExercise() {
        VerticalTreeModel model = extractModelFromWindow();
        VerticalTreeExercise exercise = new VerticalTreeExercise(model, mainWindow);
        RichTextArea rta = exercise.getExerciseView().getExerciseStatement().getEditor();
        rta.setEditable(false);
        rta.prefHeightProperty().unbind();
        exercise.getExerciseView().setStatementPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getStatementPrefHeight()));
        mainWindow.setUpExercise(exercise);

    }
    private void saveExercise(boolean saveAs) {
        saveButton.setDisable(true);
        saveAsButton.setDisable(true);
        nameField.textProperty().addListener(nameListener);

        VerticalTreeModel model = extractModelFromWindow();
        VerticalTreeExercise exercise = new VerticalTreeExercise(model, mainWindow);
        RichTextArea rta = exercise.getExerciseView().getExerciseStatement().getEditor();
        rta.setEditable(false);
        rta.prefHeightProperty().unbind();
        exercise.getExerciseView().setStatementPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getStatementPrefHeight()));
        exercise.saveExercise(saveAs);
        saveButton.setDisable(false);
        saveAsButton.setDisable(false);
        modified = false;
    }

    private VerticalTreeModel extractModelFromWindow() {
        VerticalTreeModel model = new VerticalTreeModel();
        model.setExerciseName(nameField.getText());

        List<DragIconType> dragList = model.getDragIconList();
        if (treeBoxItalicSansCheck.isSelected()) {
            model.setDefaultKeyboardType(ITALIC_AND_SANS);
            dragList.add(tree_field);
        }
        else if (treeBoxBaseItalicCheck.isSelected()) {
            model.setDefaultKeyboardType(BASE);
            dragList.add(tree_field);
        }
        else if (treeBoxScriptItalicCheck.isSelected()) {
            model.setDefaultKeyboardType(SCRIPT_AND_ITALIC);
            dragList.add(tree_field);
        }

        if (verticalBracketCheck.isSelected()) dragList.add(bracket);
        if (dashedLineCheck.isSelected()) dragList.add(dashed_line);

        if (mapBoxItalicSansCheck.isSelected()) {
            model.setDefaultMapKeyboardType(ITALIC_AND_SANS);
            dragList.add(map_field);
        }
        else if (mapBoxBaseItalicCheck.isSelected()) {
            model.setDefaultMapKeyboardType(BASE);
            dragList.add(map_field);
        }
        else if (mapBoxScriptItalicCheck.isSelected()) {
            model.setDefaultMapKeyboardType(SCRIPT_AND_ITALIC);
            dragList.add(map_field);
        }




        List<ObjectControlType> controlList = model.getObjectControlList();
        if (boxingFormulaCheck.isSelected()) controlList.add(FORMULA_BOX);
        if (circleCheck.isSelected()) controlList.add(CIRCLE);
        if (starCheck.isSelected()) controlList.add(STAR);
        if (annotationCheck.isSelected()) controlList.add(ANNOTATION);
        if (underlineCheck.isSelected()) controlList.add(UNDERLINE);
        if (mappingCheck.isSelected()) controlList.add(MAPPING);

        if (statementRTA.isModified()) modified = true;
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseStatement(statementRTA.getDocument());
        model.setStatementPrefHeight(statementTextHeight + 29);
        model.setStatementTextHeight(statementTextHeight);

        if (!pointsPossibleTextField.getText().equals("")) model.setPointsPossible(Integer.parseInt(pointsPossibleTextField.getText()));
        else {
            model.setPointsPossible(0);
            pointsPossibleTextField.setText("0");
        }

        return model;
    }

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

        VBox topBox = new VBox(menuBar, paragraphToolbar, fontsAndEditBox, kbdBox);

        borderPane.topProperty().setValue(topBox);
    }

    private void textFieldInFocus() {
        editorInFocus(dummyDRTA, ControlType.STATEMENT);
    }

}
