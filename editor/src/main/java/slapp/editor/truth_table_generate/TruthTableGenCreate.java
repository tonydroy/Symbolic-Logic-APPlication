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

package slapp.editor.truth_table_generate;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.view.javafx.BrowserView;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import slapp.editor.DiskUtilities;
import slapp.editor.EditorAlerts;
import slapp.editor.EditorMain;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.decorated_rta.KeyboardDiagram;
import slapp.editor.derivation.CheckSetup;
import slapp.editor.main_window.ControlType;
import slapp.editor.main_window.MainWindow;
import slapp.editor.main_window.MainWindowView;
import slapp.editor.main_window.TextHelpPopup;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.Languages;
import slapp.editor.parser.ParseUtilities;
import slapp.editor.trans_tree.InterpFnElement;
import slapp.editor.trans_tree.TransTreeCheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.gluonhq.richtextarea.RichTextAreaSkin.KeyMapValue.*;
import static com.gluonhq.richtextarea.RichTextAreaSkin.KeyMapValue.BASE_AND_SCRIPT;
import static com.gluonhq.richtextarea.RichTextAreaSkin.KeyMapValue.SCRIPT_AND_ITALIC;
import static javafx.scene.control.ButtonType.OK;
import static slapp.editor.EditorMain.RTA_SCREEN_BMP;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;

/**
 * Create window for the truth table generate exercise
 */
public class TruthTableGenCreate {
    private MainWindow mainWindow;
    private MainWindowView mainView;
    private TextField nameField;
    private TextField explainPromptField;
    private RichTextArea statementRTA;
    private DecoratedRTA statementDRTA;
    private double statementTextHeight;
    private RichTextArea interpretationRTA;
    private DecoratedRTA interpretationDRTA;
    private double interpretationTextHeight;
    private Spinner<Double> interpretationHeightSpinner;
    private RichTextArea currentSpinnerNode;
    private CheckBox includedInterpFnCheck;
    private boolean fieldModified = false;
    private double scale = 1.0;
    private Scene scene;
    private Stage stage;
    private BorderPane borderPane;
    private SimpleDoubleProperty centerHeightProperty;
    private TextArea helpArea;
    private VBox centerBox;
    private CheckBox conclusionDividerCheck;
    private TextField choiceLeadField;
    private TextField aPromptField;
    private TextField bPromptField;
    private List<BoxedDRTA> targetFormulaList;
    private GridPane mainFormulasPane;
    private Label zoomLabel;
    private Spinner<Integer> zoomSpinner;
    private MenuBar menuBar;
    private VBox upperFieldsBox;
    private ToolBar sizeToolBar;
    private ChangeListener nameListener;
    private ChangeListener choiceLeadListener;
    private ChangeListener aPromptListener;
    private ChangeListener bPromptListener;
    private ChangeListener auxExerListener;
    private ChangeListener explainPromptListener;
    private TextField interpPromptField;
    private ChangeListener interpPromptListener;
    private double formulaBoxHeight = 27;
    private Button lowerSaveButton;
    private Button saveAsButton;
    private BoxedDRTA focusedBoxedDRTA;
    private DecoratedRTA dummyDRTA = new DecoratedRTA();

    private Pane spacerPane;
    private Spinner<Double> statementHeightSpinner;
    private TextField pointsPossibleTextField;
    private TextField auxExerNameField;

    private CheckBox staticHelpCheck;
    private Spinner<Double> staticHelpHeightSpinner;
    private RichTextArea staticHelpRTA;
    private DecoratedRTA staticHelpDRTA;

    private Spinner checkMaxSpinner;
    private TTGCheckSetup checkSetup;

    CheckBox choiceABox;
    CheckBox choiceBBox;

    private RichTextAreaSkin.KeyMapValue targetKeyMapValue;

    private CheckBox skipBasicSentencesBox;
    CheckBox permitShortTableBox;
    CheckBox autoCheckBox;
    CheckBox checkChoicesBox;

    CheckBox checkTransBox;

    private final DoubleProperty localScalePageHeight = new SimpleDoubleProperty(1.0);
    private final DoubleProperty localScalePageWidth = new SimpleDoubleProperty(1.0);

    /**
     * Create new truth table generate exercise
     * @param mainWindow the main window
     */
    public TruthTableGenCreate(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        localScalePageHeight.bind(mainWindow.getMainView().scalePageHeightProperty());
        localScalePageWidth.bind(mainWindow.getMainView().scalePageWidthProperty());

        setupWindow();
    }

    /**
     * Create window for existing exercise
     * @param mainWindow the main window
     * @param originalModel the model for the exercise to be modified
     */
    public TruthTableGenCreate(MainWindow mainWindow, TruthTableGenModel originalModel) {
        this(mainWindow);

        TTGCheckSetup checkSetup = originalModel.getCheckSetup();
        if (checkSetup == null) checkSetup = new TTGCheckSetup();

        nameField.setText(originalModel.getExerciseName());


        auxExerNameField.setText(originalModel.getAuxExerName());
        auxExerNameField.textProperty().addListener(auxExerListener);
        pointsPossibleTextField.setText(Integer.toString(originalModel.getPointsPossible()));

        explainPromptField.setText(originalModel.getExplainPrompt());
        interpPromptField.setText(originalModel.getInterpretationPrompt());

        choiceLeadField.setText(originalModel.getChoiceLead());
        aPromptField.setText(originalModel.getaPrompt());
        bPromptField.setText(originalModel.getbPrompt());
        checkTransBox.setSelected(originalModel.isCheckTrans());

        staticHelpCheck.setSelected(checkSetup.isStaticHelp());
        checkMaxSpinner.getValueFactory().setValue(checkSetup.getCheckMax());
        includedInterpFnCheck.setSelected(originalModel.isInterpFnIncluded());
        skipBasicSentencesBox.setSelected(checkSetup.isSkipBasicsOK());
        permitShortTableBox.setSelected(checkSetup.isPermitShortTable());
        autoCheckBox.setSelected(checkSetup.isAutoCheckValidity());
        checkChoicesBox.setSelected(checkSetup.isCheckChoices());
        choiceABox.setSelected(checkSetup.isChoiceA());
        choiceBBox.setSelected(checkSetup.isChoiceB());



        updateKeyFormulaFieldsFromCheckSetup(checkSetup);
        updateMainFormulaGridFromFields();
        conclusionDividerCheck.setSelected(originalModel.isConclusionDivider());

        if (originalModel.isInterpFnIncluded()) {
            targetKeyMapValue = ITALIC_AND_SANS;
            for (BoxedDRTA bdrta : targetFormulaList) {
                bdrta.getDRTA().getKeyboardSelector().valueProperty().setValue(ITALIC_AND_SANS);
            }
            interpretationDRTA.getKeyboardSelector().valueProperty().setValue(BASE);
        }
        else {
            targetKeyMapValue = SCRIPT_AND_ITALIC;
            for (BoxedDRTA bdrta : targetFormulaList) {
                bdrta.getDRTA().getKeyboardSelector().valueProperty().setValue(SCRIPT_AND_ITALIC);
            }
            interpretationDRTA.getKeyboardSelector().valueProperty().setValue(BASE_AND_SCRIPT);
        }


        statementRTA.getActionFactory().open(originalModel.getExerciseStatement()).execute(new ActionEvent());
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        statementTextHeight = originalModel.getStatementTextHeight();

        staticHelpRTA.getActionFactory().open(checkSetup.getStaticHelpDoc()).execute(new ActionEvent());
        staticHelpRTA.getActionFactory().saveNow().execute(new ActionEvent());

        if (includedInterpFnCheck.isSelected()) {
            interpretationRTA.getActionFactory().open(originalModel.getInterpFnDoc()).execute(new ActionEvent());
            interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());
            interpretationTextHeight = originalModel.getInterpFnTextHeight();
        }
        else {
            interpretationRTA.getActionFactory().open(checkSetup.getKeyInterpFnDoc()).execute(new ActionEvent());
            interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());
            interpretationTextHeight = checkSetup.getKeyInterpFnTextHeight();
        }

        fieldModified = false;
    }

    /*
     * Set up the create window
     */
    private void setupWindow(){
        borderPane = new BorderPane();

        //empty bar for consistent look
        Menu helpMenu = new Menu("");
        menuBar = new MenuBar(helpMenu);

        //statement editor
        statementDRTA = new DecoratedRTA();
        statementRTA = statementDRTA.getEditor();
        statementRTA.setPromptText("Exercise Statement:");
        statementRTA.getStylesheets().add("slappTextArea.css");

        statementRTA.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        statementRTA.minWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        statementRTA.contentAreaWidthProperty().bind(localScalePageWidth);

        statementRTA.setMinHeight(100);

        double statementInitialHeight = Math.round(100 / localScalePageHeight.get() * 100.0);
        statementHeightSpinner = new Spinner<>(statementInitialHeight, 999.0, statementInitialHeight, 1.0);
        statementHeightSpinner.setPrefWidth(60);
        statementHeightSpinner.setDisable(false);
        statementHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        statementRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        localScalePageHeight.addListener((ob, ov, nv) -> {
            statementRTA.prefHeightProperty().unbind();
            statementHeightSpinner.getValueFactory().setValue((double) Math.round(statementHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            statementRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

        });

        final boolean[] statementPrimed = {false};
        statementRTA.fullHeightProperty().addListener((ob, ov, nv) -> {
            if (!statementPrimed[0]) {
                statementPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            statementTextHeight = height;
        });

        statementRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(statementDRTA, ControlType.AREA);
            }
        });
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());

        //-------------

        staticHelpDRTA = new DecoratedRTA();
        staticHelpDRTA.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.BASE_AND_SANS);
        staticHelpRTA = staticHelpDRTA.getEditor();
        staticHelpRTA.setPromptText("Static Help (complete if static help selected)");
        staticHelpRTA.getStylesheets().add("slappTextArea.css");
        staticHelpRTA.setMinWidth(400 + RTA_WIDTH_BMP);
        staticHelpRTA.setMaxWidth(400 + RTA_WIDTH_BMP);
        staticHelpRTA.setContentAreaWidth(400);
        staticHelpRTA.setMinHeight(100);

        double staticHelpInitialHeight = Math.round(100 / localScalePageHeight.get() * 100 );
        staticHelpHeightSpinner = new Spinner<>(staticHelpInitialHeight, 999.0, staticHelpInitialHeight, 1.0);
        staticHelpHeightSpinner.setPrefWidth(60);
        staticHelpHeightSpinner.setDisable(false);
        staticHelpHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        staticHelpRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(staticHelpHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        localScalePageHeight.addListener((ob, ov, nv) -> {
            staticHelpRTA.prefHeightProperty().unbind();
            staticHelpHeightSpinner.getValueFactory().setValue((double) Math.round(staticHelpHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            staticHelpRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(staticHelpHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        });
        staticHelpRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(staticHelpDRTA, ControlType.AREA);
            }
        });
        staticHelpRTA.getActionFactory().saveNow().execute(new ActionEvent());

        //interpretation
        interpretationDRTA = new DecoratedRTA();    ;
        interpretationRTA = interpretationDRTA.getEditor();
        interpretationRTA.setPromptText("Interpretation Function");
        interpretationRTA.getStylesheets().add("slappTextArea.css");
        interpretationRTA.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        interpretationRTA.minWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        interpretationRTA.contentAreaWidthProperty().bind(localScalePageWidth);

        interpretationRTA.setMinHeight(100);

        double interpretationInitialHeight = Math.round(100 / localScalePageHeight.get() * 100 );

        interpretationHeightSpinner = new Spinner<>(interpretationInitialHeight, 999.0, interpretationInitialHeight, 1.0);
        interpretationHeightSpinner.setPrefWidth(60);
        interpretationHeightSpinner.setDisable(false);
        interpretationHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        interpretationRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        interpretationDRTA.getKeyboardSelector().valueProperty().setValue(BASE_AND_SCRIPT);

        localScalePageHeight.addListener((ob, ov, nv) -> {
            interpretationRTA.prefHeightProperty().unbind();
            interpretationHeightSpinner.getValueFactory().setValue((double) Math.round(interpretationHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            interpretationRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        });

        final boolean[] interpPrimed = {false};
        interpretationRTA.fullHeightProperty().addListener((ob, ov, nv) -> {
            if (!interpPrimed[0]) {
                interpPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            interpretationTextHeight = height;
        });

        interpretationRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(interpretationDRTA, ControlType.AREA);
            }
        });
        interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());



        staticHelpCheck = new CheckBox();
        staticHelpCheck.setSelected(false);
        ChangeListener staticHelpCheckListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
            }
        };
        staticHelpCheck.selectedProperty().addListener(staticHelpCheckListener);
        Label staticHelpCheckLabel = new Label("Static Help: ");
        staticHelpCheckLabel.setPrefWidth(85);
        staticHelpCheckLabel.setAlignment(Pos.CENTER_RIGHT);
        HBox staticHelpBox = new HBox(10, staticHelpCheckLabel, staticHelpCheck);
        staticHelpBox.setAlignment(Pos.CENTER_LEFT);

        checkMaxSpinner = new Spinner<>(-1, 99, 0);
        checkMaxSpinner.setPrefWidth(60);
        Label checkMaxLabel = new Label("Check Max:");
        HBox checkMaxBox = new HBox(10, checkMaxLabel, checkMaxSpinner);
        checkMaxBox.setAlignment(Pos.CENTER_LEFT);

        includedInterpFnCheck = new CheckBox();


        includedInterpFnCheck.selectedProperty().addListener((ob, ov, nv) -> {
            fieldModified = true;
            if (nv) {
                targetKeyMapValue = ITALIC_AND_SANS;
                for (BoxedDRTA bdrta : targetFormulaList) {
                    bdrta.getDRTA().getKeyboardSelector().valueProperty().setValue(ITALIC_AND_SANS);
                }
                interpretationDRTA.getKeyboardSelector().valueProperty().setValue(BASE);
            }
            else {
                targetKeyMapValue = SCRIPT_AND_ITALIC;
                for (BoxedDRTA bdrta : targetFormulaList) {
                    bdrta.getDRTA().getKeyboardSelector().valueProperty().setValue(SCRIPT_AND_ITALIC);
                }
                interpretationDRTA.getKeyboardSelector().valueProperty().setValue(BASE_AND_SCRIPT);
            }
        });

        Label includedInterpFnLabel = new Label("Interpretation function included in exercise:");
        HBox includeInterpFnBox = new HBox(10, includedInterpFnLabel, includedInterpFnCheck);
        includeInterpFnBox.setAlignment(Pos.CENTER_LEFT);

        checkTransBox = new CheckBox();
        ChangeListener checkTransListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
            }
        };
        checkTransBox.selectedProperty().addListener(checkTransListener);

        Label checkTransLabel = new Label("Check translation:");
        HBox checkTransHBox = new HBox(10, checkTransLabel, checkTransBox);
        checkTransHBox.setAlignment(Pos.CENTER_LEFT);


        conclusionDividerCheck = new CheckBox("Include Conclusion Divider");
        conclusionDividerCheck.setSelected(true);
        ChangeListener conclusionDividerCheckListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
            }
        };
        conclusionDividerCheck.selectedProperty().addListener(conclusionDividerCheckListener);

        //check choices
        Label permitShortTableLabel = new Label("Permit short table:");
        permitShortTableBox = new CheckBox();
        permitShortTableBox.setSelected(true);
        permitShortTableBox.selectedProperty().addListener((ob, ov, nv) -> {
            fieldModified = true;
        });
        HBox permitShortTableHBox = new HBox(10, permitShortTableLabel, permitShortTableBox);


        Label autoCheckLabel = new Label("Auto check validity:");
        autoCheckBox = new CheckBox();
        autoCheckBox.setSelected(true);
        autoCheckBox.selectedProperty().addListener((ob, ov, nv) -> {
            fieldModified = true;
            if (nv) checkChoicesBox.setSelected(false);
        });
        HBox autoCheckHBox = new HBox(10, autoCheckLabel, autoCheckBox);


        Label checkChoicesLabel = new Label("Manual Check Choices:");
        checkChoicesBox = new CheckBox();
        checkChoicesBox.setSelected(false);
        checkChoicesBox.selectedProperty().addListener((ob, ov, nv) -> {
            fieldModified = true;
            if (nv) autoCheckBox.setSelected(false);
        });
        HBox checkChoicesHBox = new HBox(10, checkChoicesLabel, checkChoicesBox);

        skipBasicSentencesBox = new CheckBox();
        skipBasicSentencesBox.setSelected(true);
        ChangeListener justificationCheckListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
            }
        };
        skipBasicSentencesBox.selectedProperty().addListener(justificationCheckListener);
        Label basicSentencesCheckLabel = new Label("Skip Basic Sentences OK:");
        HBox basicSentencesCheckBox = new HBox(10, basicSentencesCheckLabel, skipBasicSentencesBox);
        basicSentencesCheckBox.setAlignment(Pos.CENTER_LEFT);




        Label choiceALabel = new Label("    Correct     \u21d2     A choice:");
        choiceABox = new CheckBox();
        choiceABox.setSelected(false);
        choiceABox.selectedProperty().addListener((ob, ov, nv) -> {
            fieldModified = true;
            boolean selected = (boolean) nv;
            if (selected) {
                choiceBBox.setSelected(false);
            }
        });

        HBox choiceAHBox = new HBox(10, choiceALabel, choiceABox);
        choiceAHBox.setAlignment(Pos.CENTER_LEFT);

        Label choiceBLabel = new Label("B choice:");
        choiceBBox = new CheckBox();
        choiceBBox.setSelected(false);
        choiceBBox.selectedProperty().addListener((ob, ov, nv) -> {
            fieldModified = true;
            boolean selected = (boolean) nv;
            if (selected) {
                choiceABox.setSelected(false);
            }
        });
        HBox choiceBHBox = new HBox(10, choiceBLabel, choiceBBox);
        choiceBHBox.setAlignment(Pos.CENTER_LEFT);

        HBox helpChoiceBox = new HBox(20, staticHelpBox, checkMaxBox, conclusionDividerCheck);
        helpChoiceBox.setMargin(conclusionDividerCheck, new Insets(0, 0, 0, 50));



        HBox checkChoices = new HBox(20, basicSentencesCheckBox, permitShortTableHBox, autoCheckHBox, checkChoicesHBox, choiceAHBox, choiceBHBox);


        HBox lowerTextBoxes = new HBox(10, statementRTA, staticHelpRTA);
       // VBox textBoxes = new VBox(10, interpretationRTA, lowerTextBoxes);

        VBox textBoxes = new VBox(10, lowerTextBoxes);



        //name explain prompt fields
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
        pointsPossibleTextField.setAlignment(Pos.CENTER_RIGHT);
        pointsPossibleTextField.setPadding(new Insets(5,5,5,5));

        pointsPossibleTextField.setText("0");
        pointsPossibleTextField.textProperty().addListener((ob,ov,nv) -> { fieldModified = true; });

        UnaryOperator<TextFormatter.Change> pointsPossibleFilter = change -> {
            if (change == null) return null;
            String text = change.getText();
            if (text.matches("[0-9]*")) return change;
            return null;
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(pointsPossibleFilter);
        pointsPossibleTextField.setTextFormatter(textFormatter);


        pointsPossibleTextField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                Platform.runLater(() -> pointsPossibleTextField.selectAll());
                textFieldInFocus();
            } else if (pointsPossibleTextField.getText().isEmpty()) {
                pointsPossibleTextField.setText("0");
            }
        });

        pointsPossibleTextField.setOnMouseClicked(event -> {
            if (pointsPossibleTextField.getSelectedText().isEmpty()) {
                pointsPossibleTextField.selectAll();
            }
        });



        Label auxExerLabel = new Label("Aux Exer Name: ");
        auxExerLabel.setPrefWidth(95);
        auxExerNameField = new TextField();
        auxExerNameField.setPadding(new Insets(5));
        auxExerNameField.setPromptText("(plain text)");
        auxExerNameField.setPrefWidth(200);
        auxExerListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
            }
        };
        auxExerNameField.textProperty().addListener(auxExerListener);
        auxExerNameField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        HBox auxExerBox = new HBox(10, auxExerLabel, auxExerNameField);
        auxExerBox.setAlignment(Pos.CENTER_LEFT);

        HBox nameBox = new HBox(10, nameLabel, nameField, auxExerBox, pointsPossibleLabel, pointsPossibleTextField);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        Label genPromptLabel = new Label("Interp Prompt: ");
        genPromptLabel.setPrefWidth(95);
        interpPromptField = new TextField();
        interpPromptField.setPromptText("(plain text)");
        interpPromptListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
            }
        };
        interpPromptField.textProperty().addListener(interpPromptListener);

        interpPromptField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        Label explainPromptLabel = new Label("Explain Prompt: ");
        explainPromptLabel.setPrefWidth(95);
        explainPromptLabel.setAlignment(Pos.CENTER_RIGHT);
        explainPromptField  = new TextField();
        explainPromptField.setPromptText("(plain text)");
        explainPromptListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
            }
        };
        explainPromptField.textProperty().addListener(nameListener);

        explainPromptField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });


        HBox genPromptBox = new HBox(10, genPromptLabel, interpPromptField, explainPromptLabel, explainPromptField);
        genPromptBox.setAlignment(Pos.CENTER_LEFT);

        //choice fields
        Label choiceLeadLabel = new Label("Checkbox lead: ");
        choiceLeadLabel.setPrefWidth(95);
        choiceLeadField  = new TextField("This argument is");
        choiceLeadField.setPromptText("(plain text)");
        choiceLeadListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
                choiceLeadField.textProperty().removeListener(choiceLeadListener);
            }
        };
        choiceLeadField.textProperty().addListener(choiceLeadListener);

        choiceLeadField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        Label aPromptLabel = new Label("A prompt: ");
        aPromptLabel.setPrefWidth(95);
        aPromptLabel.setAlignment(Pos.CENTER_RIGHT);
        aPromptField  = new TextField("sententially valid");
        aPromptField.setPromptText("(plain text)");
        aPromptListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
                aPromptField.textProperty().removeListener(aPromptListener);
            }
        };
        aPromptField.textProperty().addListener(aPromptListener);

        aPromptField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        Label bPromptLabel = new Label("B prompt: ");
        bPromptField  = new TextField("not sententially valid");
        bPromptField.setPromptText("(plain text)");
        bPromptListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
                bPromptField.textProperty().removeListener(bPromptListener);
            }
        };
        bPromptField.textProperty().addListener(bPromptListener);

        bPromptField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        HBox choicesBox = new HBox(10, choiceLeadLabel, choiceLeadField, aPromptLabel, aPromptField, bPromptLabel, bPromptField);
        choicesBox.setAlignment(Pos.CENTER_LEFT);




        //main formulas top
        mainFormulasPane = new GridPane();
        mainFormulasPane.setPadding(new Insets(10, 0, 0, 105));
        mainFormulasPane.setVgap(10);
        targetFormulaList = new ArrayList<>();
        BoxedDRTA mainFormulaBoxedDRTA = newMainFormulaBoxedDRTA();
        mainFormulaBoxedDRTA.getRTA().getActionFactory().saveNow().execute(new ActionEvent());
        targetFormulaList.add(mainFormulaBoxedDRTA);
        updateMainFormulaGridFromFields();

        Label mainFormulaLabel = new Label("Table formulas: ");
        mainFormulaLabel.setPrefWidth(95);
        Button addMainFormulaButton = new Button("+");
        Button removeMainFormulaButton = new Button("-");
        addMainFormulaButton.setFont(new Font(16)); removeMainFormulaButton.setFont(new Font(16));
        addMainFormulaButton.setPadding(new Insets(0, 5, 0, 5)); removeMainFormulaButton.setPadding(new Insets(1, 8, 1, 8));

        addMainFormulaButton.setOnAction(e -> {
            BoxedDRTA bdrta = newMainFormulaBoxedDRTA();
            targetFormulaList.add(bdrta);
            fieldModified = true;
            updateMainFormulaGridFromFields();
        });
        removeMainFormulaButton.setOnAction(e -> {
            int index = targetFormulaList.size();
            index--;
            if (index > 0) {
                targetFormulaList.remove(index);
                fieldModified = true;
                updateMainFormulaGridFromFields();
            } else {
                EditorAlerts.showSimpleAlert("Cannot Remove", "A truth table must include at least one formula.");
            }
        });

        mainFormulasPane.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
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




        HBox mainFormulasTop = new HBox(10, mainFormulaLabel, addMainFormulaButton, removeMainFormulaButton);
//HBox mainFormulasTop = new HBox(10, mainFormulaLabel, addMainFormulaButton, removeMainFormulaButton, checkTransHBox, includeInterpFnBox);
        mainFormulasTop.setAlignment(Pos.CENTER_LEFT);
        mainFormulasTop.setMargin(checkTransHBox, new Insets(0, 0, 0, 75));
        mainFormulasTop.setMargin(includeInterpFnBox, new Insets(0, 0, 0 , 40));

        upperFieldsBox = new VBox(15, nameBox, genPromptBox, choicesBox, helpChoiceBox, checkChoices, mainFormulasTop, mainFormulasPane);
        upperFieldsBox.setPadding(new Insets(20, 0, 20, 0));



        //center area
        String header = TextHelpPopup.getHeader14();
        String footer = TextHelpPopup.getFooter();
        String helpText = header +
                "<p>Truth Table Gen Exercise is like Truth Table Explain Exercise in that it requires a truth table along with choice between some mutually exclusive options and an explanation.  " +
                "In addition the truth table gen exercise lets the student generate the sentences for the table (so it applies naturally to translated arguments).</p>" +
                "<ul>" +
                "<li><p>Begin with the exercise name, auxiliary exercise and points fields. In the context of an assignment containing a previous exercise relevant to the one being worked, it is possible to add its name in order to include a thumbnail that will pop up its work area.  If 'points possible' is other than zero, a points field is added to the exercise comment area (and one for total assignment points into the assignment comment area).</p></li>" +
                "<li><p>Add prompts to appear in the interpretation and explanation fields. " +
                "The Checkbox lead appears prior to the check boxes, the A prompt with the first box, and the B prompt with the second.</p></li>" +
                "<li><p>The Static Help check activates the Static Help button which pops up a message which you may state in the right-hand text area below. If Check Max is 0, checking is disabled; if -1 checks are unlimited; otherwise the value sets the maximum number of allowable check tries.  The interpretation function included checkbox says whether the stated interpretation function is to be used as a key for checking or to be directly provided to students.</p></li>" +
                "<li><p>The Basic Sentences box says whether check requires that basic sentence values be repeated for basic proper subformulas in the main part of the table.  The short table check permits a student to try a short table (as from 4.1.3 of <i>Symbolic Logic</i>), and have just the short table checked.  If auto check validity is selected, check asks whether the table has a marked row with the first sentences true and the last false.  You can change the choice prompts but checking applies just to validity (A choice) and invalidity (B choice).  Manual checking checks the choice based just on your selection. " +
                "<li><p>The table formulas appear across the top of the table.  In the usual case you will leave these blank as they may be overridden by the student.  The \"conclusion divider\" merely inserts an extra space and slash ('/') prior to the last formula. </p></li> " +

                "<li><p>Finally, provide the exercise statement and, if desired static help.</p></li>" +
                "</ul>" +
                footer;

        scene = new Scene(borderPane);

        Browser browser = mainWindow.getCreateHlpBrowser();
        if (browser != null) {
            BrowserView view = BrowserView.newInstance(browser);
            view.setMouseTransparent(true);
            Platform.runLater(() -> TextHelpPopup.loadHtmlSmoothly(browser, view, helpText));
            view.setPrefHeight(545);
            view.prefWidthProperty().bind(textBoxes.widthProperty());
            centerBox = new VBox(10, upperFieldsBox, textBoxes, view);
            centerBox.setFillWidth(true);
        }
        else {
            centerBox = new VBox(10, upperFieldsBox, textBoxes, new Label("JxBrowser not started.  Please try again."));
        }
        centerBox.setPadding(new Insets(10,0,10,20));

        spacerPane = new Pane();
        spacerPane.prefHeightProperty().bind(centerBox.heightProperty());
        spacerPane.prefWidthProperty().bind(centerBox.widthProperty());
        Group group = new Group(spacerPane);
        AnchorPane comboPane = new AnchorPane(group, centerBox);
        ScrollPane centerPane = new ScrollPane(comboPane);

        borderPane.setCenter(centerPane);

        //bottom buttons
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> closeWindow());

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> clearExercise());

        Button viewButton = new Button("View");
        viewButton.setOnAction(e -> viewExercise());

        lowerSaveButton = new Button ("Save");
        lowerSaveButton.setOnAction(e -> saveExercise(false));

        saveAsButton = new Button("Save As");
        saveAsButton.setOnAction(e -> saveExercise(true));

        HBox buttonBox = new HBox(saveAsButton, lowerSaveButton, viewButton, clearButton, closeButton);
        buttonBox.setSpacing(30);
        buttonBox.setPadding(new Insets(20,50,20,50));
        buttonBox.setAlignment(Pos.BASELINE_CENTER);
        borderPane.setBottom(buttonBox);

        if (EditorMain.secondaryCopy) {
            lowerSaveButton.setDisable(true);
            saveAsButton.setDisable(true);
        }

        //editor decoration misc
        zoomLabel = new Label(" Zoom ");
        zoomSpinner = new Spinner(25, 500, 100, 1);
        zoomSpinner.setPrefSize(65,25);
        zoomSpinner.valueProperty().addListener((obs, ov, nv) -> {
            /*
            Node increment = zoomSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = zoomSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
                         */

            scale = (double) nv/100;
            updateZoom();

        });

        sizeToolBar = new ToolBar();
        sizeToolBar.setPrefHeight(38);
        sizeToolBar.getItems().addAll(zoomLabel, zoomSpinner, new Label("     V Sz:"), statementHeightSpinner);

        //setup  window

        scene.getStylesheets().add(DecoratedRTA.class.getClassLoader().getResource("slappEditor.css").toExternalForm());

        setSizeSpinners();
        stage = new Stage();
    //    stage.initOwner(EditorMain.mainStage);
        stage.setScene(scene);

        stage.setTitle("Create Truth Table Gen Exercise:");
        stage.getIcons().addAll(EditorMain.icons);
        stage.setWidth(1040);
        stage.setMinWidth(860);

        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        double stageHeight = Math.min(970, bounds.getHeight() - 20);
        stage.setHeight(stageHeight);
        double mainWinY = EditorMain.mainStage.getY();
        if (mainWinY + stageHeight + 20 < bounds.getMaxY()) {
            stage.setY(mainWinY + 10);
        }
        else {
            stage.setY(bounds.getMaxY() - (stageHeight + 10));
        }
        stage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 1040));



    //    stage.initModality(Modality.WINDOW_MODAL);
        EditorAlerts.makeIndependentBlockingStage(stage);
        stage.setOnCloseRequest(e-> {
            e.consume();
            closeWindow();
        });

        stage.show();
        statementRTA.getActionFactory().save().execute(new ActionEvent());
        centerBox.layout();

        Platform.runLater(() -> Platform.runLater(() -> nameField.requestFocus()));
    }



    private void updateKeyFormulaFieldsFromCheckSetup(TTGCheckSetup setup){
        targetFormulaList.clear();
        List<Document> formulasList = setup.getKeyFormulas();

        for (Document doc : formulasList) {
            BoxedDRTA bdrta = newMainFormulaBoxedDRTA();
            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().open(doc).execute(new ActionEvent());
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            targetFormulaList.add(bdrta);
        }
        if (targetFormulaList.isEmpty()) targetFormulaList.add(newMainFormulaBoxedDRTA());
    }

    /*
     * Update Boxed DRTA main formula fields from model
     * @param model the model
     */
    private void updateMainFormulaGridFromFields(){
        mainFormulasPane.getChildren().clear();

        for (int i = 0; i < targetFormulaList.size(); i++) {
            BoxedDRTA bdrta = targetFormulaList.get(i);
            mainFormulasPane.add(bdrta.getBoxedRTA(), 0, i);
        }
    }

    /*
     * Boxed DRTA for an operator field
     * @return the bdrta
     */
    private BoxedDRTA newOperatorBoxedDRTA() {
        BoxedDRTA bdrta = new BoxedDRTA();
        DecoratedRTA drta = bdrta.getDRTA();
        drta.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);
        RichTextArea rta = bdrta.getRTA();
        rta.setMaxHeight(formulaBoxHeight);
        rta.setMinHeight(formulaBoxHeight);
        rta.setPrefWidth(30);
        rta.getStylesheets().add("RichTextFieldWide.css");

        rta.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(drta, ControlType.STATEMENT);
            }
        });
        return bdrta;
    }

    /*
     * Boxed DRTA given a specific content operator
     * @param operator the operator
     * @return the Boxed DRTA
     */
    private BoxedDRTA contentOperatorBoxedDRTA(String operator) {
        BoxedDRTA bdrta = newOperatorBoxedDRTA();
        RichTextArea rta = bdrta.getRTA();
        rta.getActionFactory().open(new Document(operator)).execute(new ActionEvent());
        rta.getActionFactory().saveNow().execute(new ActionEvent());
        return bdrta;
    }

    /*
     * Boxed DRTA for a formula field
     * @return the bdrta
     */
    private BoxedDRTA newMainFormulaBoxedDRTA() {
        BoxedDRTA bdrta = new BoxedDRTA();
        DecoratedRTA drta = bdrta.getDRTA();
        if (includedInterpFnCheck.isSelected())
            drta.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);
        else
            drta.getKeyboardSelector().valueProperty().setValue(SCRIPT_AND_ITALIC);
        RichTextArea rta = bdrta.getRTA();
//        rta.getActionFactory().saveNow().execute(new ActionEvent());
        rta.setMaxHeight(formulaBoxHeight);
        rta.setMinHeight(formulaBoxHeight);
        rta.setPrefWidth(300);
        rta.getStylesheets().add("RichTextFieldWide.css");
        rta.setPromptText("Formula");
        rta.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(drta, ControlType.FIELD);
                focusedBoxedDRTA = bdrta;
            }
        });
        return bdrta;
    }

    /*
     * Check for changes and close
     */
    private void closeWindow() {
        if (checkContinue("Confirm Close", "This exercise appears to have unsaved changes.\nContinue to close window?")) {
            localScalePageHeight.unbind();
            localScalePageWidth.unbind();
            mainWindow.closeExercise();
            stage.close();
        }
    }

    /*
     * View the exercise as currently constructed
     */
    private void viewExercise() {
        TruthTableGenModel model = extractModelFromWindow();
        TruthTableGenExercise exercise = new TruthTableGenExercise(model, mainWindow, true);
        exercise.generateEmptyTableModel();
        RichTextArea rta = exercise.getExerciseView().getExerciseStatement().getEditor();
        rta.setEditable(false);
        rta.prefHeightProperty().unbind();
        exercise.getExerciseView().setStatementPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getStatementPrefHeight()));
        mainWindow.setUpExercise(exercise);
    }

    /*
     * Clear create window, leaving operator, prompt, and divider settings intact.
     */
    private void clearExercise() {
        if (checkContinue("Confirm Clear", "This exercise appears to have unsaved changes.\nContinue to clear exercise?")) {
            nameField.clear();
            nameField.textProperty().addListener(nameListener);
            auxExerNameField.clear();
            choiceLeadField.textProperty().addListener(choiceLeadListener);
            aPromptField.textProperty().addListener(aPromptListener);
            bPromptField.textProperty().addListener(bPromptListener);
            statementRTA.getActionFactory().newDocumentNow().execute(new ActionEvent());
            staticHelpRTA.getActionFactory().newDocumentNow().execute(new ActionEvent());
            choiceABox.setSelected(false);
            choiceBBox.setSelected(false);

            viewExercise();

            targetFormulaList.clear();
            BoxedDRTA mainFormulaBoxedDRTA = newMainFormulaBoxedDRTA();
            targetFormulaList.add(mainFormulaBoxedDRTA);
            updateMainFormulaGridFromFields();

            fieldModified = false;
        }
    }

    /*
     * Save the currently constructed exercise to disk
     * @param saveAs true to open save as window and otherwise false
     */
    private void saveExercise(boolean saveAs) {
        lowerSaveButton.setDisable(true);
        saveAsButton.setDisable(true);
        nameField.textProperty().addListener(nameListener);
        choiceLeadField.textProperty().addListener(choiceLeadListener);
        aPromptField.textProperty().addListener(aPromptListener);
        bPromptField.textProperty().addListener(bPromptListener);

        TruthTableGenModel model = extractModelFromWindow();

        /* ????
        //this is required (compared to TT create) because exercise not on scene doesn't extract formula fields: thus supplement model extracted here with empty comment and highlight values
        TruthTableGenExercise exercise = new TruthTableGenExercise(model, mainWindow, true);
        model.setRowComments(new Document[0]);
        int tableColumns = exercise.getTableColumns();
        boolean[] highlightValues = new boolean[tableColumns];
        Arrays.fill(highlightValues, false);
        model.setColumnHighlights(highlightValues);
         */

        boolean success = DiskUtilities.saveExercise(saveAs, model) ;
        if (success) fieldModified = false;
        lowerSaveButton.setDisable(false);
        saveAsButton.setDisable(false);

    }

    /*
     * If exercise modified, check for continue
     * @param title String title of confirmation box
     * @param content String content of confirmation box
     * @return true if ok to continue, and otherwise false
     */
    private boolean checkContinue(String title, String content) {
        boolean okcontinue = true;


        for (BoxedDRTA bdrta : targetFormulaList) {
            if (bdrta.getRTA().isModified()) {fieldModified = true;    }
        }
        if (statementRTA.isModified()) {fieldModified = true;   }

        if (staticHelpRTA.isModified())  {fieldModified = true; }

        if (interpretationRTA.isModified()) {fieldModified = true; }

        if (fieldModified) {
            Alert confirm = EditorAlerts.confirmationAlert(title, content);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.get() != OK) okcontinue = false;
        }
        return okcontinue;
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


    /*
     * Update zoom setting
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
     * Get the truth table generate model for the currently constructed exercise.
     * This leaves tableValues, rowComments, columnHighlights to be initialized by the TruthTableExercise (based on the model mainFormulas).
     * @return the derivation model
     */
    private TruthTableGenModel extractModelFromWindow() {
        TruthTableGenModel model = new TruthTableGenModel();

        TTGCheckSetup checkSetup = model.getCheckSetup();
        if (checkSetup == null) checkSetup = new TTGCheckSetup();

        model.setExerciseName(nameField.getText());
        model.setInterpretationPrompt(interpPromptField.getText());
        model.setExplainPrompt(explainPromptField.getText());
        model.setCheckTrans(checkTransBox.isSelected());

        model.getUnaryOperatorList().add("\u223c");
        model.getBinaryOperatorList().add("\u2192");
        model.getBinaryOperatorList().add("\u2194");
        model.getBinaryOperatorList().add("\u2227");
        model.getBinaryOperatorList().add("\u2228");



        if (statementRTA.isModified()) fieldModified = true;
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseStatement(statementRTA.getDocument());

        statementTextHeight = statementRTA.getFullHeight();
        model.setStatementTextHeight(statementTextHeight);
        model.setStatementPrefHeight(Math.max(40, statementTextHeight + RTA_SCREEN_BMP));

        if (interpretationRTA.isModified()) fieldModified = true;
        interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());

        if (includedInterpFnCheck.isSelected()) {
            model.setInterpFnDoc(interpretationRTA.getDocument());
            model.setInterpFnTextHeight(interpretationTextHeight);
            model.setInterpFnPrefHeight(Math.max(40, interpretationTextHeight + RTA_SCREEN_BMP));
        }
        else {
            checkSetup.setKeyInterpFnDoc(interpretationRTA.getDocument());
        }


        if (staticHelpRTA.isModified()) fieldModified = true;
        staticHelpRTA.getActionFactory().saveNow().execute(new ActionEvent());
        checkSetup.setStaticHelpDoc(staticHelpRTA.getDocument());
        checkSetup.setStaticHelp(staticHelpCheck.isSelected());


        List<Document> keyFormulaDocs = new ArrayList<>();
        for (BoxedDRTA bdrta : targetFormulaList) {
            RichTextArea rta = bdrta.getRTA();
            if (rta.isModified()) fieldModified = true;
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            Document doc = rta.getDocument();
            keyFormulaDocs.add(doc);
        }
        checkSetup.setKeyFormulas(keyFormulaDocs);

        model.setAuxExerName(auxExerNameField.getText());
        boolean interpFnIncluded = includedInterpFnCheck.isSelected();
        model.setInterpFnIncluded(interpFnIncluded);

        if (interpFnIncluded) {
            model.setInterpFnDoc(interpretationRTA.getDocument());
            model.setInterpFnTextHeight(interpretationTextHeight);
            model.setInterpFnPrefHeight(Math.max(60, interpretationTextHeight + RTA_SCREEN_BMP)); //interpretationRTA.getPrefHeight() + RTA_SCREEN_BMP));
        }
        else {
            checkSetup.setKeyInterpFnDoc(interpretationRTA.getDocument());
            model.setInterpFnPrefHeight(100);
        }

        model.setConclusionDivider(conclusionDividerCheck.isSelected());
        model.setChoiceLead(choiceLeadField.getText());
        model.setaPrompt(aPromptField.getText());
        model.setbPrompt(bPromptField.getText());

        if (!pointsPossibleTextField.getText().equals("")) model.setPointsPossible(Integer.parseInt(pointsPossibleTextField.getText()));
        else {
            model.setPointsPossible(0);
            pointsPossibleTextField.setText("0");
        }

        int checkMax = (Integer) checkMaxSpinner.getValue();
        checkSetup.setCheckMax(checkMax);
        checkSetup.setAutoCheckValidity(autoCheckBox.isSelected());
        checkSetup.setCheckChoices(checkChoicesBox.isSelected());
        checkSetup.setChoiceA(choiceABox.isSelected());
        checkSetup.setChoiceB(choiceBBox.isSelected());
        checkSetup.setSkipBasicsOK(skipBasicSentencesBox.isSelected());
        checkSetup.setPermitShortTable(permitShortTableBox.isSelected());

        model.setStarted(false);
        model.setCheckSetup(checkSetup);

        //coherence checks

        //translation: key expressions are formulas, process interpretation function

        if (checkTransBox.isSelected()) {
            String langName;
            if (interpFnIncluded) langName = "\u2112\ud835\udcc8 (w/abv)";
            else langName = "Meta Sym";

            for (int i = 0; i < keyFormulaDocs.size(); i++) {
                Document doc = keyFormulaDocs.get(i);
                if (doc.getText().equals("")) {
                    EditorAlerts.fleetingRedPopup("Check requires that all key formulas be non-empty. Check disabled.");
                    checkMaxSpinner.getValueFactory().setValue(0);
                    checkSetup.setCheckMax(0);
                    return model;
                }

                List<Expression> parseList = ParseUtilities.parseDoc(doc, langName);
                if (parseList.size() != 1) {
                    if (keyFormulaDocs.size() == 1)
                        EditorAlerts.fleetingRedPopup("Key expression not a formula of " + langName + ".  Check disabled.");
                    else
                        EditorAlerts.fleetingRedPopup("Target expression in blank" + ++i + " not a formula of " + langName + ".  Check disabled.");
                    checkMaxSpinner.getValueFactory().setValue(0);
                    checkSetup.setCheckMax(0);
                    return model;
                } else {
                    Expression targetExp = parseList.get(0);
                    if (targetExp.getType() != ExpressionType.FORMULA) {
                        if (keyFormulaDocs.size() == 1)
                            EditorAlerts.fleetingRedPopup("Target expression not a formula of " + langName + ".  Check disabled.");
                        else
                            EditorAlerts.fleetingRedPopup("Target expression in blank " + ++i + " not a formula of " + langName + ". Check disabled.");
                        checkMaxSpinner.getValueFactory().setValue(0);
                        checkSetup.setCheckMax(0);
                        return model;
                    }
                }
            }

            List<InterpFnElement> elements = TransTreeCheck.processInterpFn(interpretationRTA.getDocument(), Languages.getLanguage(langName));

            if (elements == null) {
                EditorAlerts.fleetingRedPopup("Check cancelled.");
                checkMaxSpinner.getValueFactory().setValue(0);
                checkSetup.setCheckMax(0);
                return model;
            } else {
                for (Document doc : keyFormulaDocs) {
                    if (!TransTreeCheck.verifyInterpFnAdequacy(elements, doc, Languages.getLanguage(langName), false)) {
                        EditorAlerts.fleetingRedPopup("Check cancelled.");
                        checkMaxSpinner.getValueFactory().setValue(0);
                        checkSetup.setCheckMax(0);
                        return model;
                    }
                }
            }
        }

        //table has no checks as it is not filled in any way.





        return model;
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
     * With text field in focus, set disabled DRTA controls
     */
    private void textFieldInFocus() {
        editorInFocus(dummyDRTA, ControlType.STATEMENT);
    }





}
