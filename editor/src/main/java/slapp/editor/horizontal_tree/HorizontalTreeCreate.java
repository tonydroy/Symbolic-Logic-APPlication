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

package slapp.editor.horizontal_tree;

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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
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
import slapp.editor.parser.grammatical_parts.Formula;

import java.util.*;
import java.util.function.UnaryOperator;

import static javafx.scene.control.ButtonType.OK;
import static slapp.editor.EditorMain.RTA_SCREEN_BMP;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;

/**
 * Create window for horizontal tree exercise
 */
public class HorizontalTreeCreate {
    private MainWindow mainWindow;
    private RichTextArea statementRTA;
    private DecoratedRTA statementDRTA;
    private double statementTextHeight;
    private RichTextArea interpretationRTA;
    private DecoratedRTA interpretationDRTA;
    private double interpretationTextHeight;
    private Spinner<Double> interpretationHeightSpinner;
    private TextField nameField;
    private boolean fieldModified = false;
    private ChangeListener nameListener;
    private TextField promptField;
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
    private DecoratedRTA dummyDRTA = new DecoratedRTA();
    private MenuBar menuBar;
    private VBox topFieldsBox;
    private BorderPane borderPane;

    private Pane spacerPane;
    private Spinner<Double> statementHeightSpinner;
    private TextField pointsPossibleTextField;

    private CheckBox staticHelpCheck;
    private RichTextArea staticHelpRTA;
    private DecoratedRTA staticHelpDRTA;
    private Spinner checkMaxSpinner;
    private CheckBox emptyBranchesOK;
    private Map<String, SimpleBooleanProperty> langMap;
    private String defaultLangName = "\u2112\ud835\udcc6 (w/abv)";  //Lq (w/abv)
    Spinner<Double> staticHelpHeightSpinner;
    private RichTextArea currentSpinnerNode;

    private GridPane targetFormulasPane;
    private List<BoxedDRTA> targetFormulaList;
    private double formulaBoxHeight = 27;
    private BoxedDRTA focusedBoxedDRTA;

    /**
     * Create new horizontal tree exercise
     * @param mainWindow the main window
     */
    public HorizontalTreeCreate(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setupWindow();
    }

    /**
     * Open create window for existing exercise
     * @param mainWindow the main window
     * @param originalModel the model for the exercise to be modified
     */
    public HorizontalTreeCreate(MainWindow mainWindow, HorizontalTreeModel originalModel) {
        this(mainWindow);

        statementRTA.getActionFactory().open(originalModel.getExerciseStatement()).execute(new ActionEvent());
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        statementTextHeight = originalModel.getStatementTextHeight();

        nameField.setText(originalModel.getExerciseName());
        promptField.setText(originalModel.getExplainPrompt());
        nameField.textProperty().addListener(nameListener);
        pointsPossibleTextField.setText(Integer.toString(originalModel.getPointsPossible()));

        HTcheckSetup checkSetup = originalModel.getCheckSetup();
        if (checkSetup == null) {
            checkSetup = new HTcheckSetup();
            originalModel.setCheckSetup(checkSetup);
        }
        updateTargetFormulaFieldsFromSetup(originalModel);
        updateTargetFormulaGridFromFields();

        staticHelpCheck.setSelected(checkSetup.isStaticHelp());
        staticHelpRTA.getActionFactory().open(checkSetup.getStaticHelpDoc()).execute(new ActionEvent());
        staticHelpRTA.getActionFactory().saveNow().execute(new ActionEvent());
        emptyBranchesOK.setSelected(checkSetup.isEmptyBranchesOK());
        checkMaxSpinner.getValueFactory().setValue(checkSetup.getCheckMax());

        interpretationRTA.getActionFactory().open(checkSetup.getInterpretationDoc()).execute(new ActionEvent());
        interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());
        interpretationTextHeight = checkSetup.getInterpretationTextHeight();

        for (String key : langMap.keySet()) {
            if (key.equals(checkSetup.getObjLangName())) {
                langMap.get(key).setValue(true);
            }
            else langMap.get(key).setValue(false);
        }
        fieldModified = false;
    }

    /*
     * Set up the create window
     */
    private void setupWindow() {
        borderPane = new BorderPane();

        //empty bar for consistent look
        Menu helpMenu = new Menu("");
        menuBar = new MenuBar(helpMenu);

        //statement field
        statementDRTA = new DecoratedRTA();
        statementRTA = statementDRTA.getEditor();
        statementRTA.setPromptText("Exercise Statement");
        statementRTA.getStylesheets().add("slappTextArea.css");
        statementRTA.maxWidthProperty().bind(Bindings.add(mainWindow.getMainView().scalePageWidthProperty(), RTA_WIDTH_BMP));
        statementRTA.minWidthProperty().bind(Bindings.add(mainWindow.getMainView().scalePageWidthProperty(), RTA_WIDTH_BMP));
        statementRTA.contentAreaWidthProperty().bind(mainWindow.getMainView().scalePageWidthProperty());

        statementRTA.setMinHeight(100);

        double statementInitialHeight = Math.round(100 / mainWindow.getMainView().getScalePageHeight() * 100.0);
        statementHeightSpinner = new Spinner<>(0.0, 999.0, statementInitialHeight, 1.0);
        statementHeightSpinner.setPrefWidth(60);
        statementHeightSpinner.setDisable(false);
        statementHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        statementRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(mainWindow.getMainView().scalePageHeightProperty(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
        statementHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = statementHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = statementHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        mainWindow.getMainView().scalePageHeightProperty().addListener((ob, ov, nv) -> {
            statementRTA.prefHeightProperty().unbind();
            statementHeightSpinner.getValueFactory().setValue((double) Math.round(statementHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            statementRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        });

        statementRTA.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            fieldModified = true;
            statementTextHeight = mainWindow.getMainView().getRTATextHeight(statementRTA);
        });

        statementRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(statementDRTA, ControlType.AREA);
                statementTextHeight = mainWindow.getMainView().getRTATextHeight(statementRTA);
            }
        });

        //static help editor
        staticHelpDRTA = new DecoratedRTA();
        staticHelpDRTA.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.BASE_AND_SANS);
        staticHelpRTA = staticHelpDRTA.getEditor();
        staticHelpRTA.getActionFactory().saveNow().execute(new ActionEvent());
        staticHelpRTA.setPromptText("Static Help (complete if static help selected)");
        staticHelpRTA.getStylesheets().add("slappTextArea.css");
        staticHelpRTA.setMinWidth(400 + RTA_WIDTH_BMP);
        staticHelpRTA.setMaxWidth(400 + RTA_WIDTH_BMP);
        staticHelpRTA.setContentAreaWidth(400);
        staticHelpRTA.setMinHeight(100);

        double staticHelpInitialHeight = Math.round(100 / mainWindow.getMainView().getScalePageHeight() * 100 );

        staticHelpHeightSpinner = new Spinner<>(0.0, 999.0, staticHelpInitialHeight, 1.0);
        staticHelpHeightSpinner.setPrefWidth(60);
        staticHelpHeightSpinner.setDisable(false);
        staticHelpHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        staticHelpRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(mainWindow.getMainView().scalePageHeightProperty(), DoubleProperty.doubleProperty(staticHelpHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
        staticHelpHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = staticHelpHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = staticHelpHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        mainWindow.getMainView().scalePageHeightProperty().addListener((ob, ov, nv) -> {
            staticHelpRTA.prefHeightProperty().unbind();
            staticHelpHeightSpinner.getValueFactory().setValue((double) Math.round(staticHelpHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            staticHelpRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(staticHelpHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        });

        staticHelpRTA.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            fieldModified = true;
            mainWindow.getMainView().getRTATextHeight(staticHelpRTA);
        });
        staticHelpRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(staticHelpDRTA, ControlType.AREA);
                mainWindow.getMainView().getRTATextHeight(staticHelpRTA);
            }
        });

        HBox textBoxes = new HBox(10, statementRTA, staticHelpRTA);

        //interpretation
        interpretationDRTA = new DecoratedRTA();
        interpretationDRTA.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);
        interpretationRTA = interpretationDRTA.getEditor();
        interpretationRTA.setPromptText("Interpretation (may be blank)");
        interpretationRTA.getStylesheets().add("slappTextArea.css");
        interpretationRTA.maxWidthProperty().bind(Bindings.add(mainWindow.getMainView().scalePageWidthProperty(), RTA_WIDTH_BMP));
        interpretationRTA.minWidthProperty().bind(Bindings.add(mainWindow.getMainView().scalePageWidthProperty(), RTA_WIDTH_BMP));
        interpretationRTA.contentAreaWidthProperty().bind(mainWindow.getMainView().scalePageWidthProperty());

        interpretationRTA.setMinHeight(100);

        double interpretationInitialHeight = Math.round(100 / mainWindow.getMainView().getScalePageHeight() * 100 );


        interpretationHeightSpinner = new Spinner<>(0.0, 999.0, interpretationInitialHeight, 1.0);
        interpretationHeightSpinner.setPrefWidth(60);
        interpretationHeightSpinner.setDisable(false);
        interpretationHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        interpretationRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(mainWindow.getMainView().scalePageHeightProperty(), DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
        interpretationHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = interpretationHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = interpretationHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        mainWindow.getMainView().scalePageHeightProperty().addListener((ob, ov, nv) -> {
            interpretationRTA.prefHeightProperty().unbind();
            interpretationHeightSpinner.getValueFactory().setValue((double) Math.round(interpretationHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            interpretationRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        });

        interpretationRTA.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            fieldModified = true;
            interpretationTextHeight = mainWindow.getMainView().getRTATextHeight(interpretationRTA);
        });
        interpretationRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(interpretationDRTA, ControlType.AREA);
                interpretationTextHeight = mainWindow.getMainView().getRTATextHeight(interpretationRTA);
            }
        });
        interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());


        //name field
        Label nameLabel = new Label("Exercise Name: ");
        nameLabel.setPrefWidth(110);
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

        HBox nameBox = new HBox(nameLabel, nameField);
        nameBox.setAlignment(Pos.BASELINE_LEFT);

        //prompt
        Label promptLabel = new Label("Explain Prompt: ");
        promptLabel.setPrefWidth(110);
        promptField = new TextField();
        promptField.setPromptText("(plain text)");
        promptListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
            }
        };
        promptField.textProperty().addListener(promptListener);
        promptField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        HBox promptBox = new HBox(promptLabel, promptField);
        promptBox.setAlignment(Pos.BASELINE_LEFT);

        //points possible
        Label pointsPossibleLabel = new Label("Points Possible: ");
        pointsPossibleLabel.setPrefWidth(110);
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
        HBox pointsBox = new HBox(pointsPossibleLabel, pointsPossibleTextField);
        pointsBox.setAlignment(Pos.CENTER_LEFT);

        //target formulas
        targetFormulasPane = new GridPane();
        targetFormulasPane.setVgap(10);
        targetFormulaList = new ArrayList<>();
        BoxedDRTA mainFormulaBoxedDRTA = newMainFormulaBoxedDRTA();
        mainFormulaBoxedDRTA.getRTA().getActionFactory().saveNow().execute(new ActionEvent());
        targetFormulaList.add(mainFormulaBoxedDRTA);
        updateTargetFormulaGridFromFields();

        Label targetFormulaLabel = new Label("Target Formula(s): ");
        targetFormulaLabel.setPrefWidth(100);
        targetFormulaLabel.setPadding(new Insets(5,0,0,0));
        Button addTargetFormulaButton = new Button("+");
        Button removeTargetFormulaButton = new Button("-");
        addTargetFormulaButton.setFont(new Font(16)); removeTargetFormulaButton.setFont(new Font(16));
        addTargetFormulaButton.setPadding(new Insets(0, 5, 0, 5)); removeTargetFormulaButton.setPadding(new Insets(1, 8, 1, 8));

        addTargetFormulaButton.setOnAction(e -> {
            BoxedDRTA bdrta = newMainFormulaBoxedDRTA();
            targetFormulaList.add(bdrta);
            fieldModified = true;
            updateTargetFormulaGridFromFields();
        });
        removeTargetFormulaButton.setOnAction(e -> {
            int index = targetFormulaList.size();
            index--;
            if (index > 0) {
                targetFormulaList.remove(index);
                fieldModified = true;
                updateTargetFormulaGridFromFields();
            }
        });

        targetFormulasPane.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            int index = targetFormulaList.indexOf(focusedBoxedDRTA);
            if (index >= 0) {
                KeyCode code = e.getCode();
                if (code == KeyCode.ENTER || code == KeyCode.DOWN) {
                    if (index + 1 < targetFormulaList.size()) {
                        targetFormulaList.get(index + 1).getRTA().requestFocus();
                    }
                    e.consume();
                }
                if (code == KeyCode.UP) {
                    if (index > 0) {
                        targetFormulaList.get(index - 1).getRTA().requestFocus();
                    }
                    e.consume();
                }
            }
        });

        HBox mainFormulasTop = new HBox(15, targetFormulaLabel, addTargetFormulaButton, removeTargetFormulaButton, targetFormulasPane);
        mainFormulasTop.setAlignment(Pos.TOP_LEFT);
        mainFormulasTop.setMargin(targetFormulasPane, new Insets(0, 0, 0, 20));



        topFieldsBox = new VBox(15, nameBox, promptBox, pointsBox, mainFormulasTop);
        topFieldsBox.setPadding(new Insets(20, 0,20, 0));

        //help area
        String helpText = "<body style=\"margin-left:10; margin-right: 20\">" +
                "<p>The Horizontal Tree Explain Exercise is appropriate for exercises that involve horizontal trees (of the sort that appear in Chapter 4 of <em>Symbolic Logic</em>), along with an explanation of some sort.</p>" +
                "<ul>" +
                "<li><p>Provide the exercise name.</p></li>" +
                "<li><p>Provide a prompt for the explain field.</p></li>" +
                "<li><p>If 'points possible' is other than zero, a points field is added to the exercise comment area (and one for total assignment points into the assignment comment area).</p></li>" +
                "<li><p>The static help check activates the Static Help button which pops up a message which you may state in the text area below.</p></li>" +
                "<li><p>Check applies to a tree or trees, but does not evaluate (text) answers about truth, validity or the like:</p></li>" +
                "<ul>" +
                "<li><p>Enter the target formula(s).  If there are multiple targets, SLAPP activates an option in the exercise to permit multiple tree windows.</p></li>" +
                "<li><p>If Check Max is 0, checking is disabled; if -1 checks are unlimited; otherwise the value sets the maximum number of allowable check tries.  The Empty Branch box says whether check permits \"incomplete\" branches (supposing completed branches suffice for a result).  And you may need to select the object language.</p></li>" +
                "<li><p>SLAPP finds an interpretation (and variable assignment as necessary) stated in interpretation field - as supplied by you as part of the exercise, or by the student.  SLAPP is picky about this: In particular a universe is of the sort <font face = \"Noto Sans\">UI</font> = {. . .} or <font face = \"Noto Sans\">UJ</font> = {. . .} (the universe <i>for</i> interpretation  <font face = \"Noto Sans\">I</font> or <font face = \"Noto Sans\">J</font> or . . . equals the set) with the regular (not Shift-Alt/Option) equals sign, and objects in the universe identified by non-negative numerals or proper names - all in the sans font.  If the interpretation field includes 'interpretation \ue3bd' it includes a preconfigued \"fragment\" of \ue3bd from <i>Symbolic Logic</i> with a universe including just 0 ... 99 and the functions all mod 100 (this won't be useful for any expression with a quantifier).</p></li>" +
                "</ul>" +
                "<li><p>And, finally, provided the exercise statement, and if desired static help.</p></li>" +
                "</ul>";
        WebView helpArea = new WebView();
        WebEngine webEngine = helpArea.getEngine();
        webEngine.setUserStyleSheetLocation("data:, body {font: 14px Noto Serif Combo; }");
        webEngine.loadContent(helpText);
        helpArea.setPrefHeight(230);

        //check items
        Label helpCheckLabel = new Label("Help/Check: ");
        helpCheckLabel.setPrefWidth(80);

        staticHelpCheck = new CheckBox();
        staticHelpCheck.setSelected(false);
        ChangeListener staticHelpCheckListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
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

        emptyBranchesOK = new CheckBox();
        emptyBranchesOK.setSelected(false);
        ChangeListener justificationCheckListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
            }
        };
        emptyBranchesOK.selectedProperty().addListener(justificationCheckListener);
        Label justificationCheckLabel = new Label("Permit Empty Branches:");
        HBox justificationCheckBox = new HBox(10, justificationCheckLabel, emptyBranchesOK);
        justificationCheckBox.setAlignment(Pos.CENTER_LEFT);

        //language list view
        Label languageLabel = new Label("   Object Language:");
        langMap = new LinkedHashMap<>();
        for (int i = 0; i < Languages.getFixedLanguages().size(); i++) {
            Language language = Languages.getFixedLanguages().get(i);
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

        HBox checkLine1 = new HBox(30, staticHelpBox, checkMaxBox, justificationCheckBox, languageLabel, languageListView);
        checkLine1.setAlignment(Pos.CENTER_LEFT);

        //center
        centerBox = new VBox(10, topFieldsBox, checkLine1, interpretationRTA, textBoxes, helpArea);
        centerBox.setPadding(new Insets(10,0,10,20));

        spacerPane = new Pane();
        spacerPane.prefHeightProperty().bind(centerBox.heightProperty());
        spacerPane.prefWidthProperty().bind(centerBox.widthProperty());
        Group group = new Group(spacerPane);
        AnchorPane comboPane = new AnchorPane(group, centerBox);
        ScrollPane centerPane = new ScrollPane(comboPane);

        borderPane.setCenter(centerPane);


        //buttons
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

        //window setup
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
        sizeToolBar.getItems().addAll(zoomLabel, zoomSpinner, new Label("     V Sz:"), currentSpinnerNode);

        setSizeSpinners();

        stage = new Stage();
        stage.initOwner(EditorMain.mainStage);
        stage.setScene(scene);
        stage.setTitle("Create Horizontal Tree Explain Exercise:");
        stage.getIcons().addAll(EditorMain.icons);
        stage.setWidth(930);
        stage.setHeight(1000);

        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        stage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 930));
        stage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 1000));
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
     * Boxed DRTA for a formula field
     * @return the bdrta
     */
    private BoxedDRTA newMainFormulaBoxedDRTA() {
        BoxedDRTA boxedDRTA = new BoxedDRTA();
        DecoratedRTA drta = boxedDRTA.getDRTA();
        drta.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);
        RichTextArea rta = boxedDRTA.getRTA();
//        rta.getActionFactory().saveNow().execute(new ActionEvent());
        rta.setMaxHeight(formulaBoxHeight);
        rta.setMinHeight(formulaBoxHeight);
        rta.setPrefWidth(300);
        rta.getStylesheets().add("RichTextFieldWide.css");
        rta.setPromptText("Formula");
        rta.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(drta, ControlType.FIELD);
                focusedBoxedDRTA = boxedDRTA;
            }
        });
        return boxedDRTA;
    }

    /*
     * Update Boxed DRTA main formula fields from model
     * @param model the model
     */


    private void updateTargetFormulaFieldsFromSetup(HorizontalTreeModel model){
        targetFormulaList.clear();
        List<Document> formulasList = model.getCheckSetup().getTargetFormulas();
        for (Document doc : formulasList) {
            BoxedDRTA bdrta = newMainFormulaBoxedDRTA();

            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().open(doc).execute(new ActionEvent());
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            targetFormulaList.add(bdrta);
        }
    }

    /*
     * Update the main formula grid pane from the bdrta fields
     */
    private void updateTargetFormulaGridFromFields(){
        targetFormulasPane.getChildren().clear();
        for (int i = 0; i < targetFormulaList.size(); i++) {
            BoxedDRTA bdrta = targetFormulaList.get(i);
            targetFormulasPane.add(bdrta.getBoxedRTA(), 0, i);
        }
    }




    //force single selection on list views
    private void setSingleSelection(SimpleBooleanProperty booleanProperty, Map<String, SimpleBooleanProperty> map) {
        booleanProperty.addListener((ob, ov, nv) -> {
            if (nv.booleanValue() != ov.booleanValue()) fieldModified = true;
            if (nv) {
                for (SimpleBooleanProperty prop : map.values()) {
                    if (prop != booleanProperty) {
                        prop.set(false);
                    }
                }
            }

        });
    }

    /*
     * update zoom setting
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
     * Check for changes and close
     */
    private void closeWindow() {
        if (checkContinue("Confirm Close", "This exercise appears to have unsaved changes.\nContinue to close window?")) {
            mainWindow.closeExercise();
            stage.close();
        }
    }

    /*
     * Clear create window, leaving just the prompt intact
     */
    private void clearExercise() {
        if (checkContinue("Confirm Clear", "This exercise appears to have unsaved changes.\nContinue to clear exercise?")) {
            nameField.clear();
            nameField.textProperty().addListener(nameListener);
            targetFormulaList.clear();
            BoxedDRTA mainFormulaBoxedDRTA = newMainFormulaBoxedDRTA();
            targetFormulaList.add(mainFormulaBoxedDRTA);
            updateTargetFormulaGridFromFields();
            statementRTA.getActionFactory().newDocumentNow().execute(new ActionEvent());
            viewExercise();
            fieldModified = false;
        }
    }

    /*
     * If exercise modified, check for continue
     * @param title String title of confirmation box
     * @param content String content of confirmation box
     * @return true if ok to continue, and otherwise false
     */
    private boolean checkContinue(String title, String content) {
        for (BoxedDRTA bdrta : targetFormulaList) {
            if (bdrta.getRTA().isModified()) {fieldModified = true;}
        }
        if (statementRTA.isModified()) {fieldModified = true; }
        if (staticHelpRTA.isModified()) {fieldModified = true; }
        if (interpretationRTA.isModified()) {fieldModified = true; }
        boolean okcontinue = true;
        if (fieldModified) {
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
        HorizontalTreeModel model = extractModelFromWindow();
        HorizontalTreeExercise exercise = new HorizontalTreeExercise(model, mainWindow);
        RichTextArea rta = exercise.getExerciseView().getExerciseStatement().getEditor();
        rta.setEditable(false);
        rta.prefHeightProperty().unbind();
        exercise.getExerciseView().setStatementPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getStatementPrefHeight()));

        RichTextArea interpRTA = exercise.getExerciseView().getInterpretationDRTA().getEditor();
        interpRTA.prefHeightProperty().unbind();
        exercise.getExerciseView().setInterpretationPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getCheckSetup().getInterpretationPrefHeight()));
        mainWindow.setUpExercise(exercise);
    }

    /*
     * Save the currently constructed exercise to disk
     * @param saveAs true to open save as window and otherwise false
     */
    private void saveExercise(boolean saveAs) {
        saveButton.setDisable(true);
        saveAsButton.setDisable(true);
        nameField.textProperty().addListener(nameListener);

        HorizontalTreeModel model = extractModelFromWindow();
        boolean success = DiskUtilities.saveExercise(saveAs, model);
        if (success) {
            fieldModified = false;
        }

        /*
        HorizontalTreeExercise exercise = new HorizontalTreeExercise(model, mainWindow);
        RichTextArea rta = exercise.getExerciseView().getExerciseStatement().getEditor();
        rta.setEditable(false);
        rta.prefHeightProperty().unbind();
        exercise.getExerciseView().setStatementPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getStatementPrefHeight()));
        exercise.saveExercise(saveAs);

         */

        saveButton.setDisable(false);
        saveAsButton.setDisable(false);
        fieldModified = false;
    }

    /*
     * Get the horizontal tree model for the currently constructed exercise
     * @return the horizontal tree model
     */
    private HorizontalTreeModel extractModelFromWindow() {
        HorizontalTreeModel model = new HorizontalTreeModel();
        model.setExerciseName(nameField.getText());
        model.setExplainPrompt(promptField.getText());

        if (statementRTA.isModified()) fieldModified = true;
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseStatement(statementRTA.getDocument());
        model.setStatementTextHeight(statementTextHeight);
        model.setStatementPrefHeight(Math.max(40, statementTextHeight + RTA_SCREEN_BMP));


        if (!pointsPossibleTextField.getText().equals("")) model.setPointsPossible(Integer.parseInt(pointsPossibleTextField.getText()));
        else {
            model.setPointsPossible(0);
            pointsPossibleTextField.setText("0");
        }

        HTcheckSetup checkSetup = model.getCheckSetup();
        if (checkSetup == null) checkSetup = new HTcheckSetup();
        interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());
        checkSetup.setInterpretationDoc(interpretationRTA.getDocument());

        checkSetup.setInterpretationTextHeight(interpretationTextHeight);
        checkSetup.setInterpretationPrefHeight(Math.max(60, interpretationTextHeight + RTA_SCREEN_BMP));

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

        List<Document> targetDocs = new ArrayList<>();
        for (BoxedDRTA bdrta : targetFormulaList) {
            RichTextArea rta = bdrta.getRTA();
            if (rta.isModified()) fieldModified = true;
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            Document doc = rta.getDocument();

            List<Expression> expressions = ParseUtilities.parseDoc(doc, langName);
            if (expressions.size() != 0 && (expressions.size() != 1 || !(expressions.get(0) instanceof Formula))) {
                bdrta.setVTtreeBoxHighlight();
                mainWindow.getMainView().scrollToNode(bdrta.getRTA());
                List<Text> texts = new ArrayList<>();
                texts.add(ParseUtilities.newRegularText("Expression is not a formula of "));
                texts.addAll(Languages.getLanguage(langName).getNameTextList());
                texts.add(ParseUtilities.newRegularText("."));
                EditorAlerts.showSimpleTxtListAlert("Not A Sentence:", texts);
                rta.getActionFactory().open(new Document()).execute(new ActionEvent());
                fieldModified = true;
                bdrta.resetVTtreeBoxHighlight();
            }
            targetDocs.add(doc);
        }
        checkSetup.setTargetFormulas(targetDocs);

        if (staticHelpRTA.isModified()) fieldModified = true;
        staticHelpRTA.getActionFactory().saveNow().execute(new ActionEvent());
        checkSetup.setStaticHelpDoc(staticHelpRTA.getDocument());

        checkSetup.setStaticHelp(staticHelpCheck.isSelected());
        int checkMax = (Integer) checkMaxSpinner.getValue();
        checkSetup.setCheckMax(checkMax);
        checkSetup.setEmptyBranchesOK(emptyBranchesOK.isSelected());
        model.setCheckSetup(checkSetup);


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
            else if (inHierarchy(nv, staticHelpRTA) && currentSpinnerNode != staticHelpRTA) {
                currentSpinnerNode = staticHelpRTA;
                sizeToolBar.getItems().remove(3);
                sizeToolBar.getItems().add(3, staticHelpHeightSpinner);
                return;
            }
            else if (inHierarchy(nv, interpretationRTA) && currentSpinnerNode != interpretationRTA) {
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
     * Update keyboard diagram and edit controls to currently selected DRTA
     * @param decoratedRTA the DRTA
     * @param control the {slapp.editor.main_window.ControlType}
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

        VBox topBox = new VBox(menuBar, paragraphToolbar, fontsAndEditBox, kbdBox);

        borderPane.topProperty().setValue(topBox);
    }

    /*
     * With text field in focus, set disabled DRTA controls
     */
    private void textFieldInFocus() {
        editorInFocus(dummyDRTA, ControlType.STATEMENT);
    }


}
