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

package slapp.editor.vert_tree_abefexplain;

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

public class VerticalTreeABEFExpCreate {
    private MainWindow mainWindow;
    private TextField nameField;
    private TextField explainPromptField;
    private DecoratedRTA statementDRTA;
    private RichTextArea statementRTA;
    private double statementTextHeight;
    private TextArea helpArea;
    private TextField abChoiceLeadField;
    private TextField aPromptField;
    private TextField bPromptField;
    private TextField efChoiceLeadField;
    private TextField ePromptField;
    private TextField fPromptField;
    private ChangeListener nameListener;
    ChangeListener abChoiceLeadListener;
    ChangeListener aPromptListener;
    ChangeListener bPromptListener;
    ChangeListener efChoiceLeadListener;
    ChangeListener ePromptListener;
    ChangeListener fPromptListener;
    ChangeListener explainPromptListener;
    private boolean modified = false;
    private CheckBox treeFormulaBoxCheck;
    private CheckBox verticalBracketCheck;
    private CheckBox dashedLineCheck;
    private CheckBox mapFormulaBoxCheck;
    private CheckBox boxingFormulaCheck;
    private CheckBox circleCheck;
    private CheckBox starCheck;
    private CheckBox annotationCheck;
    private CheckBox underlineCheck;
    private CheckBox mappingCheck;
    private CheckBox baseItalicCheck;
    private CheckBox italicSansCheck;
    private CheckBox scriptItalicCheck;
    private RichTextAreaSkin.KeyMapValue keyboardSelector;
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


    public VerticalTreeABEFExpCreate(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setupWindow();
    }

    public VerticalTreeABEFExpCreate(MainWindow mainWindow, VerticalTreeABEFExpModel originalModel) {
        this(mainWindow);

        statementRTA.getActionFactory().open(originalModel.getExerciseStatement()).execute(new ActionEvent());
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        statementTextHeight = originalModel.getStatementTextHeight();
        nameField.setText(originalModel.getExerciseName());
        keyboardSelector = originalModel.getDefaultKeyboardType();
        italicSansCheck.setSelected(keyboardSelector == ITALIC_AND_SANS);
        baseItalicCheck.setSelected(keyboardSelector == RichTextAreaSkin.KeyMapValue.BASE);
        scriptItalicCheck.setSelected(keyboardSelector == SCRIPT_AND_ITALIC);
        explainPromptField.setText(originalModel.getExplainPrompt());
        abChoiceLeadField.setText(originalModel.getABChoiceLead());
        efChoiceLeadField.setText(originalModel.getEFChoiceLead());
        aPromptField.setText(originalModel.getaPrompt());
        bPromptField.setText(originalModel.getbPrompt());
        ePromptField.setText(originalModel.getePrompt());
        fPromptField.setText(originalModel.getfPrompt());
        pointsPossibleTextField.setText(Integer.toString(originalModel.getPointsPossible()));

        List<DragIconType> dragIconList = originalModel.getDragIconList();
        treeFormulaBoxCheck.setSelected(dragIconList.contains(tree_field));
        verticalBracketCheck.setSelected(dragIconList.contains(bracket));
        dashedLineCheck.setSelected(dragIconList.contains(dashed_line));
        mapFormulaBoxCheck.setSelected(dragIconList.contains(map_field));
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
        nameLabel.setPrefWidth(120);
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

        Label explainPromptLabel = new Label("Explain Prompt: ");
        explainPromptField  = new TextField();
        explainPromptField.setPromptText("(plain text)");
        explainPromptListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                modified = true;
            }
        };
        explainPromptField.textProperty().addListener(nameListener);
        explainPromptField.focusedProperty().addListener((ob, ov, nv) -> {
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
        pointsPossibleTextField.textProperty().addListener((ob,ov,nv) -> { modified = true; });
        pointsPossibleTextField.focusedProperty().addListener((ob, ov, nv) -> { if (nv) textFieldInFocus();  });

        HBox nameBox = new HBox(10, nameLabel, nameField, explainPromptLabel, explainPromptField, pointsPossibleLabel, pointsPossibleTextField);
        nameBox.setAlignment(Pos.BASELINE_LEFT);

        Label keyboardLabel = new Label("Default Keyboard: ");
        keyboardLabel.setPrefWidth(100);
        baseItalicCheck = new CheckBox("Base/Italic");
        baseItalicCheck.setSelected(false);
        baseItalicCheck.selectedProperty().addListener((ob, ov, nv) -> {
            boolean selected = (boolean) nv;
            if (selected) {
                modified = true;
                keyboardSelector = BASE;
                italicSansCheck.setSelected(false);
                scriptItalicCheck.setSelected(false);
            }
        });
        italicSansCheck = new CheckBox("Italic/Sans");
        italicSansCheck.setSelected(true);
        italicSansCheck.selectedProperty().addListener((ob, ov, nv) -> {
            boolean selected = (boolean) nv;
            if (selected) {
                modified = true;
                keyboardSelector = ITALIC_AND_SANS;
                baseItalicCheck.setSelected(false);
                scriptItalicCheck.setSelected(false);
            }
        });
        scriptItalicCheck = new CheckBox("Script/Italic");
        scriptItalicCheck.setSelected(false);
        scriptItalicCheck.selectedProperty().addListener((ob, ov, nv) -> {
            boolean selected = (boolean) nv;
            if (selected) {
                modified = true;
                keyboardSelector = SCRIPT_AND_ITALIC;
                baseItalicCheck.setSelected(false);
                italicSansCheck.setSelected(false);
            }
        });
        HBox keyboardBox = new HBox(20, keyboardLabel, baseItalicCheck, italicSansCheck, scriptItalicCheck);
        keyboardBox.setAlignment(Pos.BASELINE_LEFT);

        //choice fields
        Label abChoiceLeadLabel = new Label("AB checkbox lead: ");
        abChoiceLeadLabel.setPrefWidth(120);
        abChoiceLeadField  = new TextField();
        abChoiceLeadField.setPromptText("(plain text)");
        abChoiceLeadListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                modified = true;
                abChoiceLeadField.textProperty().removeListener(abChoiceLeadListener);
            }
        };
        abChoiceLeadField.textProperty().addListener(abChoiceLeadListener);
        abChoiceLeadField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        Label aPromptLabel = new Label("A prompt: ");
        aPromptField  = new TextField();
        aPromptField.setPromptText("(plain text)");
        aPromptListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                modified = true;
                aPromptField.textProperty().removeListener(aPromptListener);
            }
        };
        aPromptField.textProperty().addListener(aPromptListener);
        aPromptField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        Label bPromptLabel = new Label("B prompt: ");
        bPromptField  = new TextField();
        bPromptField.setPromptText("(plain text)");
        bPromptListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                modified = true;
                bPromptField.textProperty().removeListener(bPromptListener);
            }
        };
        bPromptField.textProperty().addListener(bPromptListener);
        bPromptField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        Label efChoiceLeadLabel = new Label("EF checkbox lead: ");
        efChoiceLeadLabel.setPrefWidth(120);
        efChoiceLeadField  = new TextField();
        efChoiceLeadField.setPromptText("(plain text)");
        efChoiceLeadListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                modified = true;
                efChoiceLeadField.textProperty().removeListener(efChoiceLeadListener);
            }
        };
        efChoiceLeadField.textProperty().addListener(efChoiceLeadListener);
        efChoiceLeadField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        Label ePromptLabel = new Label("E prompt: ");
        ePromptField  = new TextField();
        ePromptField.setPromptText("(plain text)");
        ePromptListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                modified = true;
                ePromptField.textProperty().removeListener(ePromptListener);
            }
        };
        ePromptField.textProperty().addListener(ePromptListener);
        ePromptField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        Label fPromptLabel = new Label("F prompt: ");
        fPromptField  = new TextField();
        fPromptField.setPromptText("(plain text)");
        fPromptListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                modified = true;
                fPromptField.textProperty().removeListener(fPromptListener);
            }
        };
        fPromptField.textProperty().addListener(fPromptListener);
        fPromptField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });



        HBox choicesBox1 = new HBox(10, abChoiceLeadLabel, abChoiceLeadField, aPromptLabel, aPromptField, bPromptLabel, bPromptField);
        choicesBox1.setAlignment(Pos.CENTER_LEFT);
        HBox choicesBox2 = new HBox(10, efChoiceLeadLabel, efChoiceLeadField, ePromptLabel, ePromptField, fPromptLabel, fPromptField);
        choicesBox2.setAlignment(Pos.CENTER_LEFT);


        //drag bar
        Label dragLabel = new Label("Drag Bar: ");
        dragLabel.setPrefWidth(100);

        treeFormulaBoxCheck = new CheckBox("Tree Formula");
        treeFormulaBoxCheck.setSelected(false);
        treeFormulaBoxCheck.selectedProperty().addListener((ob, ov, nv) -> { modified = true; });
        verticalBracketCheck = new CheckBox("Vertical Bracket");
        verticalBracketCheck.setSelected(false);
        verticalBracketCheck.selectedProperty().addListener((ob, ov, nv) -> { modified = true; });
        dashedLineCheck = new CheckBox("Dashed Line");
        dashedLineCheck.setSelected(false);
        dashedLineCheck.selectedProperty().addListener((ob, ov, nv) -> { modified = true; });
        mapFormulaBoxCheck = new CheckBox("Maping Formula");
        mapFormulaBoxCheck.setSelected(false);
        mapFormulaBoxCheck.selectedProperty().addListener((ob, ov, nv) -> { modified = true; });
        HBox dragBox = new HBox(20, dragLabel, treeFormulaBoxCheck, verticalBracketCheck, dashedLineCheck, mapFormulaBoxCheck);
        dragBox.setAlignment(Pos.BASELINE_LEFT);

        //control pane
        Label controlLabel = new Label("Controls Pane: ");
        controlLabel.setPrefWidth(100);

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

        fieldsBox = new VBox(15, nameBox, choicesBox1, choicesBox2, dragBox, controlBox, keyboardBox);
        fieldsBox.setPadding(new Insets(20,0, 0, 20));

        //center
        String helpText =  "<body style=\"margin-left:10; margin-right: 20\">" +
                "<p>Vertical Tree AB/EF Explain Exercise is like Vertical Tree Explain Exercise except that it requires a pair of binary choices.  It is appropriate for any exercise that builds tree or map diagrams and requires two choices together with an explanation.</p>" +
                "<ul>" +
                "<li><p>Start with the exercise name, explain prompt, and points fields.  If 'points possible' is other than zero, a points field is added to the exercise comment area (and one for total assignment points into the assignment comment area).</p></li>" +
                "<li><p>The checkbox leads appear before the choice boxes, then the A/B and E/F prompts with their respective boxes.</p></li>" +
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


        ToolBar editToolbar = statementDRTA.getKbdSelectorToolbar();
        ToolBar fontsToolbar = statementDRTA.getEditToolbar();
        ToolBar paragraphToolbar = statementDRTA.getParagraphToolbar();
        ToolBar kbdDiaToolBar = statementDRTA.getKbdDiaToolbar();
        kbdDiaToolBar.setPrefHeight(38);

        /*
        if (kbdDiaToolBar.getItems().isEmpty()) {
            kbdDiaToolBar.getItems().addAll(statementDRTA.getKeyboardDiagramButton());
        }

        HBox editAndKbdBox = new HBox(editToolbar, sizeToolBar, kbdDiaToolBar);
        editAndKbdBox.setHgrow(kbdDiaToolBar, Priority.ALWAYS);

        VBox topBox = new VBox(menuBar, paragraphToolbar, fontsToolbar, editAndKbdBox, fieldsBox);
        borderPane.topProperty().setValue(topBox);

         */

        //generate view
        scene = new Scene(borderPane);
        scene.getStylesheets().add(DecoratedRTA.class.getClassLoader().getResource("slappEditor.css").toExternalForm());

        stage = new Stage();
        stage.initOwner(EditorMain.mainStage);
        stage.setScene(scene);
        stage.setTitle("Create Vertical Tree AB/EF Explain Exercise:");
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
            abChoiceLeadField.textProperty().addListener(abChoiceLeadListener);
            aPromptField.textProperty().addListener(aPromptListener);
            bPromptField.textProperty().addListener(bPromptListener);
            efChoiceLeadField.textProperty().addListener(efChoiceLeadListener);
            ePromptField.textProperty().addListener(ePromptListener);
            fPromptField.textProperty().addListener(fPromptListener);

            statementRTA.getActionFactory().newDocumentNow().execute(new ActionEvent());
 //           statementRTA.getActionFactory().open(new Document()).execute(new ActionEvent());
  //          statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
            viewExercise();
            modified = false;
        }
    }
    private void viewExercise() {
        VerticalTreeABEFExpModel model = extractModelFromWindow();
        VerticalTreeABEFExpExercise exercise = new VerticalTreeABEFExpExercise(model, mainWindow);
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

        abChoiceLeadField.textProperty().addListener(abChoiceLeadListener);
        aPromptField.textProperty().addListener(aPromptListener);
        bPromptField.textProperty().addListener(bPromptListener);
        efChoiceLeadField.textProperty().addListener(efChoiceLeadListener);
        ePromptField.textProperty().addListener(ePromptListener);
        fPromptField.textProperty().addListener(fPromptListener);


        VerticalTreeABEFExpModel model = extractModelFromWindow();
        VerticalTreeABEFExpExercise exercise = new VerticalTreeABEFExpExercise(model, mainWindow);
        RichTextArea rta = exercise.getExerciseView().getExerciseStatement().getEditor();
        rta.setEditable(false);
        rta.prefHeightProperty().unbind();
        exercise.getExerciseView().setStatementPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getStatementPrefHeight()));
        exercise.saveExercise(saveAs);
        saveButton.setDisable(false);
        saveAsButton.setDisable(false);
        modified = false;
    }

    private VerticalTreeABEFExpModel extractModelFromWindow() {
        VerticalTreeABEFExpModel model = new VerticalTreeABEFExpModel();
        model.setExerciseName(nameField.getText());
        model.setExplainPrompt(explainPromptField.getText());
        if (italicSansCheck.isSelected()) model.setDefaultKeyboardType(ITALIC_AND_SANS);
        else if (baseItalicCheck.isSelected()) model.setDefaultKeyboardType(BASE);
        else model.setDefaultKeyboardType(SCRIPT_AND_ITALIC);

        model.setABChoiceLead(abChoiceLeadField.getText());
        model.setaPrompt(aPromptField.getText());
        model.setbPrompt(bPromptField.getText());
        model.setEFChoiceLead(efChoiceLeadField.getText());
        model.setePrompt(ePromptField.getText());
        model.setfPrompt(fPromptField.getText());

        List<DragIconType> dragList = model.getDragIconList();
        if (treeFormulaBoxCheck.isSelected()) dragList.add(tree_field);
        if (verticalBracketCheck.isSelected()) dragList.add(bracket);
        if (dashedLineCheck.isSelected()) dragList.add(dashed_line);
        if (mapFormulaBoxCheck.isSelected()) dragList.add(map_field);

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

    private void textFieldInFocus() {
        editorInFocus(dummyDRTA, ControlType.STATEMENT);
    }

}
