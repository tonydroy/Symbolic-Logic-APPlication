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

package slapp.editor.map_abexplain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import slapp.editor.DiskUtilities;
import slapp.editor.EditorAlerts;
import slapp.editor.EditorMain;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.decorated_rta.KeyboardDiagram;
import slapp.editor.main_window.ControlType;
import slapp.editor.main_window.MainWindow;
import slapp.editor.main_window.MainWindowView;
import slapp.editor.parser.*;
import slapp.editor.vertical_tree.VTCheckType;
import slapp.editor.vertical_tree.drag_drop.DragIconType;
import slapp.editor.vertical_tree.object_models.MapFormulaBoxMod;
import slapp.editor.vertical_tree.object_models.ObjectControlType;

import java.util.*;
import java.util.function.UnaryOperator;

import static com.gluonhq.richtextarea.RichTextAreaSkin.KeyMapValue.*;
import static javafx.scene.control.ButtonType.OK;
import static slapp.editor.vertical_tree.drag_drop.DragIconType.*;
import static slapp.editor.vertical_tree.object_models.ObjectControlType.*;

public class MapABExpCreate {
    private MainWindow mainWindow;
    private TextField nameField;
    private TextField explainPromptField;
    private DecoratedRTA statementDRTA;
    private RichTextArea statementRTA;
    private double statementTextHeight;
    private TextArea helpArea;
    private TextField choiceLeadField;
    private TextField aPromptField;
    private TextField bPromptField;
    private ChangeListener nameListener;
    ChangeListener choiceLeadListener;
    ChangeListener aPromptListener;
    ChangeListener bPromptListener;
    ChangeListener explainPromptListener;
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

    private CheckBox staticHelpCheck;
    private RichTextArea staticHelpRTA;
    private DecoratedRTA staticHelpDRTA;
    private Spinner checkMaxSpinner;
    private CheckBox justificationCheck;
    private Map<String, SimpleBooleanProperty> langMap;
    private String defaultLangName = "\u2112\ud835\udcc6 (w/abv)";  //Lq (w/abv)
    CheckBox formulaCheck;
    CheckBox unabbreviationCheck;
    CheckBox checkMarkup;
    BoxedDRTA targetBoxedDRTA;
    TextField auxNameField;
    Spinner<Double> staticHelpHeightSpinner;
    private RichTextArea currentSpinnerNode;

    CheckBox checkChoicesBox;
    CheckBox choiceABox;
    CheckBox choiceBBox;

    BoxedDRTA metaFormulaBoxedDRTA;
    BoxedDRTA objectFormulaBoxedDRTA;



    public MapABExpCreate(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setupWindow();
        modified = false;
    }

    public MapABExpCreate(MainWindow mainWindow, MapABExpModel originalModel) {
        this(mainWindow);

        statementRTA.getActionFactory().open(originalModel.getExerciseStatement()).execute(new ActionEvent());
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        statementTextHeight = originalModel.getStatementTextHeight();
        nameField.setText(originalModel.getExerciseName());
        treeBoxKeyboardSelector = originalModel.getDefaultKeyboardType();

        choiceLeadField.setText(originalModel.getChoiceLead());
        aPromptField.setText(originalModel.getaPrompt());
        bPromptField.setText(originalModel.getbPrompt());
        explainPromptField.setText(originalModel.getExplainPrompt());

        metaFormulaBoxedDRTA.getRTA().getActionFactory().open(originalModel.getMapFormulaBoxes().get(0).getText()).execute(new ActionEvent());
        metaFormulaBoxedDRTA.getRTA().getActionFactory().saveNow().execute(new ActionEvent());
        objectFormulaBoxedDRTA.getRTA().getActionFactory().open(originalModel.getMapFormulaBoxes().get(1).getText()).execute(new ActionEvent());
        objectFormulaBoxedDRTA.getRTA().getActionFactory().saveNow().execute(new ActionEvent());


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

        MapCheckSetup checkSetup = originalModel.getCheckSetup();
        if (checkSetup == null) checkSetup = new MapCheckSetup();
        staticHelpCheck.setSelected(checkSetup.isStaticHelp());
        staticHelpRTA.getActionFactory().open(checkSetup.getStaticHelpDoc()).execute(new ActionEvent());
        staticHelpRTA.getActionFactory().saveNow().execute(new ActionEvent());
        checkMaxSpinner.getValueFactory().setValue(checkSetup.getCheckMax());

        checkChoicesBox.setSelected(checkSetup.isCheckChoices());
        choiceABox.setSelected(checkSetup.isChoiceA());
        choiceBBox.setSelected(checkSetup.isChoiceB());


        for (String key : langMap.keySet()) {
            if (key.equals(checkSetup.getObjLangName())) {
                langMap.get(key).setValue(true);
            }
            else langMap.get(key).setValue(false);
        }

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

        //static help editor
        staticHelpDRTA = new DecoratedRTA();
        staticHelpDRTA.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.BASE_AND_SANS);
        staticHelpRTA = staticHelpDRTA.getEditor();
        staticHelpRTA.setPromptText("Static Help (complete if static help selected)");
        staticHelpRTA.getStylesheets().add("slappTextArea.css");
        staticHelpRTA.setPrefWidth(350);
        staticHelpRTA.setMinWidth(350);
        staticHelpRTA.setMaxWidth(350);
        staticHelpRTA.setMinHeight(100);

        double staticHelpInitialHeight = Math.round(100 / mainWindow.getMainView().getScalePageHeight() * 100 );

        staticHelpHeightSpinner = new Spinner<>(0.0, 999.0, staticHelpInitialHeight, 1.0);
        staticHelpHeightSpinner.setPrefWidth(60);
        staticHelpHeightSpinner.setDisable(false);
        staticHelpHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        staticHelpRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(mainWindow.getMainView().scalePageHeightProperty(), DoubleProperty.doubleProperty(staticHelpHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));
        staticHelpHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = staticHelpHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = staticHelpHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        mainWindow.getMainView().scalePageHeightProperty().addListener((ob, ov, nv) -> {
            staticHelpRTA.prefHeightProperty().unbind();
            staticHelpHeightSpinner.getValueFactory().setValue((double) Math.round(staticHelpHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            staticHelpRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(staticHelpHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));
        });

        staticHelpRTA.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            modified = true;
        });
        staticHelpRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(staticHelpDRTA, ControlType.AREA);
            }
        });

        HBox textBoxes = new HBox(10, statementRTA, staticHelpRTA);


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

        //points
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

        //explain prompt
        Label promptLabel = new Label("Explain Prompt: ");
        promptLabel.setPrefWidth(100);
        explainPromptField = new TextField();
        explainPromptField.setPromptText("(plain text)");
        explainPromptListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                modified = true;
            }
        };
        explainPromptField.textProperty().addListener(explainPromptListener);

        explainPromptField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        HBox promptBox = new HBox(promptLabel, explainPromptField);
        promptBox.setAlignment(Pos.BASELINE_LEFT);

        //choice fields
        Label choiceLeadLabel = new Label("Checkbox lead: ");
        choiceLeadLabel.setPrefWidth(90);
        choiceLeadField  = new TextField();
        choiceLeadField.setPromptText("(plain text)");
        choiceLeadListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                modified = true;
                choiceLeadField.textProperty().removeListener(choiceLeadListener);
            }
        };
        choiceLeadField.textProperty().addListener(choiceLeadListener);

        choiceLeadField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        Label aPromptLabel = new Label("A prompt: ");
        aPromptLabel.setPrefWidth(90);
        aPromptLabel.setAlignment(Pos.CENTER_RIGHT);
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

        HBox choicesBox = new HBox(10, choiceLeadLabel, choiceLeadField, aPromptLabel, aPromptField, bPromptLabel, bPromptField);
        choicesBox.setAlignment(Pos.CENTER_LEFT);

        //formula fields

        metaFormulaBoxedDRTA = new BoxedDRTA();
        metaFormulaBoxedDRTA.getDRTA().getKeyboardSelector().valueProperty().setValue(SCRIPT_AND_ITALIC);
        RichTextArea metaFormulaRTA = metaFormulaBoxedDRTA.getRTA();

        metaFormulaRTA.setMaxHeight(27);
        metaFormulaRTA.setMinHeight(27);
        metaFormulaRTA.setPrefWidth(400);
        metaFormulaRTA.setContentAreaWidth(500);
        metaFormulaRTA.getStylesheets().add("RichTextFieldWide.css");
        metaFormulaRTA.setPromptText("Meta Expression");

        metaFormulaRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(metaFormulaBoxedDRTA.getDRTA(), ControlType.FIELD);
            }
        });
        Label metaFormulaLabel = new Label("Meta Exp:");
        metaFormulaLabel.setPrefWidth(90);
        HBox metaBox = new HBox(10, metaFormulaLabel, metaFormulaBoxedDRTA.getBoxedRTA());
        metaBox.setAlignment(Pos.CENTER_LEFT);

        objectFormulaBoxedDRTA = new BoxedDRTA();
        objectFormulaBoxedDRTA.getDRTA().getKeyboardSelector().valueProperty().setValue(ITALIC_AND_SANS);
        RichTextArea objectFormulaRTA = objectFormulaBoxedDRTA.getRTA();

        objectFormulaRTA.setMaxHeight(27);
        objectFormulaRTA.setMinHeight(27);
        objectFormulaRTA.setPrefWidth(400);
        objectFormulaRTA.setContentAreaWidth(500);
        objectFormulaRTA.getStylesheets().add("RichTextFieldWide.css");
        objectFormulaRTA.setPromptText("Object Expression");

        objectFormulaRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(objectFormulaBoxedDRTA.getDRTA(), ControlType.FIELD);
            }
        });
        Label objectFormulaLabel = new Label("Object Exp:");
        objectFormulaLabel.setPrefWidth(90);
        HBox objectBox = new HBox(10, objectFormulaLabel, objectFormulaBoxedDRTA.getBoxedRTA());
        metaBox.setAlignment(Pos.CENTER_LEFT);

        VBox topFieldsBox = new VBox(10, nameBox, pointsBox, promptBox, choicesBox, metaBox, objectBox);



        //drag bar
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


        //drag bar

        verticalBracketCheck = new CheckBox("Vertical Bracket");
        verticalBracketCheck.setSelected(true);
        verticalBracketCheck.selectedProperty().addListener((ob, ov, nv) -> { modified = true; });
        dashedLineCheck = new CheckBox("Dashed Line");
        dashedLineCheck.setSelected(true);
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

        fieldsBox = new VBox(15, topFieldsBox);
  //      fieldsBox = new VBox(15, topFieldsBox, dragBox, controlBox);
        fieldsBox.setPadding(new Insets(20,0, 0, 20));

        //check items

        Label helpCheckLabel = new Label("Help/Check: ");
        helpCheckLabel.setPrefWidth(80);

        staticHelpCheck = new CheckBox();
        staticHelpCheck.setSelected(false);
        ChangeListener staticHelpCheckListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                modified = true;
            }
        };
        staticHelpCheck.selectedProperty().addListener(staticHelpCheckListener);
        Label staticHelpCheckLabel = new Label("Static Help:");
        HBox staticHelpBox = new HBox(10, staticHelpCheckLabel, staticHelpCheck);
        staticHelpBox.setAlignment(Pos.CENTER_LEFT);

        checkMaxSpinner = new Spinner<>(-1, 99, 0);
        checkMaxSpinner.setPrefWidth(60);
        Label checkMaxLabel = new Label("Check Max:");
        HBox checkMaxBox = new HBox(10, checkMaxLabel, checkMaxSpinner);
        checkMaxBox.setAlignment(Pos.CENTER_LEFT);

        justificationCheck = new CheckBox();
        justificationCheck.setSelected(false);
        ChangeListener justificationCheckListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                modified = true;
            }
        };
        justificationCheck.selectedProperty().addListener(justificationCheckListener);
        Label justificationCheckLabel = new Label("Check Justifications:");
        HBox justificationCheckBox = new HBox(10, justificationCheckLabel, justificationCheck);
        justificationCheckBox.setAlignment(Pos.CENTER_LEFT);

        //language list view
        Label languageLabel = new Label("   Object Language:");
        langMap = new LinkedHashMap<>();
 //       for (int i = 0; i < Languages.getFixedLanguages().size() - 1; i++) {
 //           Language language = Languages.getFixedLanguages().get(i);

        for (int i = 0; i < Languages.getMappingLanguages().size(); i++) {
            Language language = Languages.getMappingLanguages().get(i);
            SimpleBooleanProperty booleanProperty = new SimpleBooleanProperty(false);
            if (language.getNameString().equals(defaultLangName)) booleanProperty.setValue(true); //default check
            setSingleSelection(booleanProperty, langMap);
            langMap.put(language.getNameString(), booleanProperty);
        }
        ListView<String> languageListView = new ListView<>();
        languageListView.setPrefSize(120, 95);

        languageListView.getItems().addAll(langMap.keySet());
        Callback<String, ObservableValue<Boolean>> langToBoolean = (String item) -> langMap.get(item);
        languageListView.setCellFactory(CheckBoxListCell.forListView(langToBoolean));

        HBox checkLine1 = new HBox(30, staticHelpBox, checkMaxBox, languageLabel, languageListView);
  //      HBox checkLine1 = new HBox(30, staticHelpBox, checkMaxBox, justificationCheckBox, languageLabel, languageListView);
        checkLine1.setAlignment(Pos.CENTER_LEFT);


        formulaCheck = new CheckBox();
        formulaCheck.setSelected(false);

        formulaCheck.selectedProperty().addListener((ob, ov, nv) -> {
            modified = true;
            boolean selected = (boolean) nv;
            if (selected) {
                unabbreviationCheck.setSelected(false);
            }
        });
        Label formulaCheckLabel = new Label("Formula Check:");
        //      formulaCheckLabel.setPrefWidth(90);
        HBox formulaCheckBox = new HBox(10, formulaCheckLabel, formulaCheck);
        formulaCheckBox.setAlignment(Pos.CENTER_LEFT);

        checkMarkup = new CheckBox();
        checkMarkup.setSelected(false);
        ChangeListener markupCheckListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                modified = true;
            }
        };
        checkMarkup.selectedProperty().addListener(markupCheckListener);
        Label markupCheckLabel = new Label("Check Markup:");
        HBox markupCheckBox = new HBox(10, markupCheckLabel, checkMarkup);
        markupCheckBox.setAlignment(Pos.CENTER_LEFT);

        targetBoxedDRTA = new BoxedDRTA();
        targetBoxedDRTA.getDRTA().getKeyboardSelector().valueProperty().setValue(ITALIC_AND_SANS);
        RichTextArea targetRTA = targetBoxedDRTA.getRTA();

        targetRTA.setMaxHeight(27);
        targetRTA.setMinHeight(27);
        targetRTA.setPrefWidth(400);
        targetRTA.setContentAreaWidth(500);
        targetRTA.getStylesheets().add("RichTextFieldWide.css");
        targetRTA.setPromptText("Formula");

        targetRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(targetBoxedDRTA.getDRTA(), ControlType.FIELD);
            }
        });
        Label targetLabel = new Label("Target:");
        HBox targetBox = new HBox(10, targetLabel, targetBoxedDRTA.getBoxedRTA());
        targetBox.setAlignment(Pos.CENTER_LEFT);

        HBox checkLine2 = new HBox(30, formulaCheckBox, markupCheckBox, targetBox);
        checkLine2.setAlignment(Pos.TOP_LEFT);

        unabbreviationCheck = new CheckBox();
        unabbreviationCheck.setSelected(false);
        unabbreviationCheck.selectedProperty().addListener((ob, ov, nv) -> {
            modified = true;
            boolean selected = (boolean) nv;
            if (selected) {
                formulaCheck.setSelected(false);
            }
        });

        Label unabbCheckLabel = new Label("Unabbreviation Check:");
        //     unabbCheckLabel.setPrefWidth(150);
        HBox unabbCheckBox = new HBox(10, unabbCheckLabel, unabbreviationCheck);
        unabbCheckBox.setAlignment(Pos.CENTER_LEFT);


        Label auxNameLabel = new Label("Auxiliary Exercise Name: ");
        //    auxNameLabel.setPrefWidth(150);
        auxNameField  = new TextField();
        auxNameField.setPromptText("(plain text)");
        ChangeListener auxNameListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                modified = true;
            }
        };
        auxNameField.textProperty().addListener(auxNameListener);
        auxNameField.setPromptText("(plain text)");
        auxNameField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });
        HBox auxNameBox = new HBox(10, auxNameLabel, auxNameField);
        auxNameBox.setAlignment(Pos.CENTER_LEFT);

        HBox checkLine3 = new HBox(30, unabbCheckBox, auxNameBox);
        checkLine3.setAlignment(Pos.CENTER_LEFT);

        //check choices
        Label checkChoicesLabel = new Label("Check Choices:");
        checkChoicesBox = new CheckBox();
        checkChoicesBox.setSelected(false);
        checkChoicesBox.selectedProperty().addListener((ob, ov, nv) -> {
            modified = true;
        });
        HBox checkChoicesHBox = new HBox(10, checkChoicesLabel, checkChoicesBox);

        Label choiceALabel = new Label("Correct     \u21d2     A choice:");
        choiceABox = new CheckBox();
        choiceABox.setSelected(false);
        choiceABox.selectedProperty().addListener((ob, ov, nv) -> {
            modified = true;
            boolean selected = (boolean) nv;
            if (selected) {
                choiceBBox.setSelected(false);
            }
        });
        HBox choiceAHBox = new HBox(10, choiceALabel, choiceABox);

        Label choiceBLabel = new Label("B choice:");
        choiceBBox = new CheckBox();
        choiceBBox.setSelected(false);
        choiceBBox.selectedProperty().addListener((ob, ov, nv) -> {
            modified = true;
            boolean selected = (boolean) nv;
            if (selected) {
                choiceABox.setSelected(false);
            }
        });
        HBox choiceBHBox = new HBox(10, choiceBLabel, choiceBBox);

        HBox checkLine4 = new HBox(30, checkChoicesHBox, choiceAHBox, choiceBHBox);


        VBox checksVBox = new VBox(10, checkLine1, checkLine4);
   //     VBox checksVBox = new VBox(10, checkLine2, checkLine3, checkLine1, checkLine4);

        HBox checksBox = new HBox(20, helpCheckLabel, checksVBox);
        checksBox.setAlignment(Pos.TOP_LEFT);
        checksBox.setPadding(new Insets(10,0,0,20));



        //center
        /*
        This is a kludge.  The help areas are usually HTML documents displayed in WebView.  But the JavaFX WebView
        doesn't display Unicode supplemental characters properly.  So we use an RTA document.  To edit this document
        see Derivation Exercise.
         */


        RichTextArea helpRTA = new RichTextArea(EditorMain.mainStage);
        Document helpDoc = mainWindow.getSlappProgData().getMapCreateHelp();
        helpRTA.getActionFactory().open(helpDoc).execute(new ActionEvent());
        helpRTA.setEditable(false);
        helpRTA.getStylesheets().add("slappTextArea.css");


        helpRTA.setPrefHeight(300);

        centerBox = new VBox(10, fieldsBox, checksBox, textBoxes, helpRTA);
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

        setSizeSpinners();
        stage = new Stage();
        stage.initOwner(EditorMain.mainStage);
        stage.setScene(scene);
        stage.setTitle("Create Map AB Explain Exercise:");
        stage.getIcons().addAll(EditorMain.icons);

        stage.setWidth(930);
        stage.setHeight(1025);
        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        stage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 930));
        stage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 1025));

        stage.initModality(Modality.WINDOW_MODAL);
        stage.setOnCloseRequest(e-> {
            e.consume();
            closeWindow();
        });

        stage.show();
        statementRTA.getActionFactory().save().execute(new ActionEvent());
        targetBoxedDRTA.getRTA().getActionFactory().save().execute(new ActionEvent());
        staticHelpRTA.getActionFactory().save().execute(new ActionEvent());
        centerBox.layout();

        Platform.runLater(() -> nameField.requestFocus());

    }

    //force single selection on list views
    private void setSingleSelection(SimpleBooleanProperty booleanProperty, Map<String, SimpleBooleanProperty> map) {
        booleanProperty.addListener((ob, ov, nv) -> {
            if (nv.booleanValue() != ov.booleanValue()) modified = true;
            if (nv) {
                for (SimpleBooleanProperty prop : map.values()) {
                    if (prop != booleanProperty) {
                        prop.set(false);
                    }
                }
            }

        });
    }

    private void setSizeSpinners() {

        scene.focusOwnerProperty().addListener((ob, ov, nv) -> {

            if (inHierarchy(nv, statementRTA) && currentSpinnerNode != statementRTA) {
                currentSpinnerNode = statementRTA;
                sizeToolBar.getItems().remove(3);
                sizeToolBar.getItems().add(3, statementHeightSpinner);
                return;
            }
            if (inHierarchy(nv, staticHelpRTA) && currentSpinnerNode != staticHelpRTA) {
                currentSpinnerNode = staticHelpRTA;
                sizeToolBar.getItems().remove(3);
                sizeToolBar.getItems().add(3, staticHelpHeightSpinner);
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
        if (modified || statementRTA.isModified() || staticHelpRTA.isModified() || targetBoxedDRTA.getRTA().isModified()) {
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
            targetBoxedDRTA.getRTA().getActionFactory().newDocumentNow().execute(new ActionEvent());
            statementRTA.getActionFactory().newDocumentNow().execute(new ActionEvent());
            staticHelpRTA.getActionFactory().newDocumentNow().execute(new ActionEvent());
            auxNameField.clear();
            choiceABox.setSelected(false);
            choiceBBox.setSelected(false);

            choiceLeadField.textProperty().addListener(choiceLeadListener);
            aPromptField.textProperty().addListener(aPromptListener);
            bPromptField.textProperty().addListener(bPromptListener);

            viewExercise();
            modified = false;
        }
    }
    private void viewExercise() {
        MapABExpModel model = extractModelFromWindow();
        MapABExpExercise exercise = new MapABExpExercise(model, mainWindow);
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
        choiceLeadField.textProperty().addListener(choiceLeadListener);
        aPromptField.textProperty().addListener(aPromptListener);
        bPromptField.textProperty().addListener(bPromptListener);

        MapABExpModel model = extractModelFromWindow();
        DiskUtilities.saveExercise(saveAs, model);

  //      MapABExpExercise exercise = new MapABExpExercise(model, mainWindow);
   //     RichTextArea rta = exercise.getExerciseView().getExerciseStatement().getEditor();
  //      rta.setEditable(false);
 //       rta.prefHeightProperty().unbind();
 //       exercise.getExerciseView().setStatementPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getStatementPrefHeight()));
 //       exercise.saveExercise(saveAs);
        saveButton.setDisable(false);
        saveAsButton.setDisable(false);
        modified = false;
    }

    private MapABExpModel extractModelFromWindow() {
        MapABExpModel model = new MapABExpModel();
        model.setExerciseName(nameField.getText());

        model.setChoiceLead(choiceLeadField.getText());
        model.setaPrompt(aPromptField.getText());
        model.setbPrompt(bPromptField.getText());
        model.setExplainPrompt(explainPromptField.getText());

        MapFormulaBoxMod metaBoxMod = new MapFormulaBoxMod();
        metaBoxMod.setIdString("d2f211c8-4a16-4033-9512-06a25cc79df1");
        metaBoxMod.setLayoutX(62.0);
        metaBoxMod.setLayoutY(24.0);
        metaBoxMod.setLinkIdStrings(new ArrayList<>());
        metaBoxMod.setMetaBox(true);
        RichTextArea metaRTA = metaFormulaBoxedDRTA.getRTA();
        if (metaRTA.isModified()) modified = true;
        metaRTA.getActionFactory().saveNow().execute(new ActionEvent());
        metaBoxMod.setText(metaRTA.getDocument());

        MapFormulaBoxMod objectBoxMod = new MapFormulaBoxMod();
        objectBoxMod.setIdString("33f6eb99-8991-45aa-b9bc-2168975a79bb");
        objectBoxMod.setLayoutX(62.0);
        objectBoxMod.setLayoutY(96.0);
        objectBoxMod.setLinkIdStrings(new ArrayList<>());
        objectBoxMod.setMetaBox(false);
        RichTextArea objectRTA = objectFormulaBoxedDRTA.getRTA();
        if (objectRTA.isModified()) modified = true;
        objectRTA.getActionFactory().saveNow().execute(new ActionEvent());
        objectBoxMod.setText(objectRTA.getDocument());

        model.getMapFormulaBoxes().add(metaBoxMod);
        model.getMapFormulaBoxes().add(objectBoxMod);




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

        MapCheckSetup checkSetup = model.getCheckSetup();
        if (staticHelpRTA.isModified()) modified = true;
        staticHelpRTA.getActionFactory().saveNow().execute(new ActionEvent());
        checkSetup.setStaticHelpDoc(staticHelpRTA.getDocument());



        checkSetup.setStaticHelp(staticHelpCheck.isSelected());
        int checkMax = (Integer) checkMaxSpinner.getValue();
        checkSetup.setCheckMax(checkMax);


        String langName = null;
        for (String key : langMap.keySet()) {
            ObservableValue<Boolean> value = langMap.get(key);
            if (value.getValue()) {
                langName = key;
                break;
            }
        }
        if (langName != null) {
            checkSetup.setObjLangName(langName);
        }
        else {
            EditorAlerts.showSimpleAlert("No language selected:", "Without a language selection, SLAPP will revert to the default, \u2112\ud835\udcc6 (w/abv).");
        }

        checkSetup.setCheckChoices(checkChoicesBox.isSelected());
        checkSetup.setChoiceA(choiceABox.isSelected());
        checkSetup.setChoiceB(choiceBBox.isSelected());

        //consistency checks



        if (checkChoicesBox.isSelected() && !(choiceABox.isSelected() || choiceBBox.isSelected()))  {
            checkChoicesBox.setSelected(false);
            checkSetup.setCheckChoices(false);
            EditorAlerts.fleetingRedPopup("There is no choice to check.  Check Choices disabled.");
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
