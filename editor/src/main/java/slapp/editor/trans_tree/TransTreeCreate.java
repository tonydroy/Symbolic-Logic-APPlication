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

package slapp.editor.trans_tree;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.view.javafx.BrowserView;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
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
import javafx.util.Duration;
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
import slapp.editor.main_window.TextHelpPopup;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.Languages;
import slapp.editor.parser.ParseUtilities;

import java.util.*;
import java.util.function.UnaryOperator;

import static com.gluonhq.richtextarea.RichTextAreaSkin.KeyMapValue.*;
import static javafx.scene.control.ButtonType.OK;
import static slapp.editor.EditorMain.RTA_SCREEN_BMP;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;

public class TransTreeCreate {
    private MainWindow mainWindow;
    private TextField nameField;
    private TextField promptField;
    private DecoratedRTA statementDRTA;
    private RichTextArea statementRTA;
    private double statementTextHeight;
    private RichTextArea interpretationRTA;
    private DecoratedRTA interpretationDRTA;
    private double interpretationTextHeight;
    private Spinner<Double> interpretationHeightSpinner;
    private ChangeListener nameListener;
    private ChangeListener promptListener;
    private boolean modified = false;

    private double scale = 1.0;
    private Stage stage;
    private Scene scene;
    private VBox centerBox;
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
    private TextField auxNameField;
    private Spinner<Double> staticHelpHeightSpinner;
    private RichTextArea currentSpinnerNode;
    private List<BoxedDRTA> targetFormulaList;
    private GridPane mainFormulasPane;
    private double formulaBoxHeight = 27;
    private BoxedDRTA focusedBoxedDRTA;
    private CheckBox useAuxExerCheck;
    private CheckBox naturalVsGoodCheck;
    private CheckBox includedInterpFnCheck;
    private CheckBox omitFormalTreeCheck;
    private CheckBox treesOptionalCheck;
    private RichTextAreaSkin.KeyMapValue targetKeyMapValue;

    private final DoubleProperty localScalePageHeight = new SimpleDoubleProperty(1.0);
    private final DoubleProperty localScalePageWidth = new SimpleDoubleProperty(1.0);


    public TransTreeCreate(MainWindow mainWindow) {
        this.mainWindow = mainWindow;

        localScalePageHeight.bind(mainWindow.getMainView().scalePageHeightProperty());
        localScalePageWidth.bind(mainWindow.getMainView().scalePageWidthProperty());
        setupWindow();
        modified = false;

    }

    public TransTreeCreate(MainWindow mainWindow, TransTreeModel originalModel) {
        this(mainWindow);

        statementRTA.getActionFactory().open(originalModel.getExerciseStatement()).execute(new ActionEvent());
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        statementTextHeight = originalModel.getStatementTextHeight();

        nameField.setText(originalModel.getExerciseName());
        pointsPossibleTextField.setText(Integer.toString(originalModel.getPointsPossible()));
        auxNameField.setText(originalModel.getAuxExerName());
        includedInterpFnCheck.setSelected(originalModel.isInterpFnIncluded());



        TTreeCheckSetup checkSetup = originalModel.getCheckSetup();
        if (checkSetup == null) checkSetup = new TTreeCheckSetup();

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


        staticHelpCheck.setSelected(checkSetup.isStaticHelp());
        staticHelpRTA.getActionFactory().open(checkSetup.getStaticHelpDoc()).execute(new ActionEvent());
        staticHelpRTA.getActionFactory().saveNow().execute(new ActionEvent());
        checkMaxSpinner.getValueFactory().setValue(checkSetup.getCheckMax());
        omitFormalTreeCheck.setSelected(checkSetup.isExcludeFormalTree());
        useAuxExerCheck.setSelected(checkSetup.isParseTreeFromAux());
        naturalVsGoodCheck.setSelected(checkSetup.isCheckNatural());
        treesOptionalCheck.setSelected(checkSetup.isTreesOptional());

        updateMainFormulaFieldsFromModel(originalModel);
        updateTargetFormulaGridFromFields();

        modified = false;
    }

    private void setupWindow() {
        borderPane = new BorderPane();

        //empty bar for consistent look
        Menu helpMenu = new Menu("");
        menuBar = new MenuBar(helpMenu);


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


        //statementDRTA
        statementDRTA = new DecoratedRTA();
        statementRTA = statementDRTA.getEditor();
        statementRTA.getStylesheets().add("slappTextArea.css");
        statementRTA.setPromptText("Exercise Statement");
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

        /*
        statementHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = statementHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = statementHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });
         */

        localScalePageHeight.addListener((ob, ov, nv) -> {
            statementRTA.prefHeightProperty().unbind();
            statementHeightSpinner.getValueFactory().setValue((double) Math.round(statementHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            statementRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

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

        //static help editor
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

        /*
        staticHelpHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = staticHelpHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = staticHelpHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });
         */

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

        HBox lowerTextBoxes = new HBox(10, statementRTA, staticHelpRTA);
        VBox textBoxes = new VBox(10, interpretationRTA, lowerTextBoxes);
        textBoxes.setPadding(new Insets(20,0,0,0));

        //name
        Label nameLabel = new Label("Exercise Name:   ");
        nameLabel.setPrefWidth(105);
        nameLabel.setAlignment(Pos.CENTER_RIGHT);
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
        pointsPossibleTextField.setAlignment(Pos.CENTER_RIGHT);
        pointsPossibleTextField.setPadding(new Insets(5,5,5,5));

        pointsPossibleTextField.setText("0");
        pointsPossibleTextField.textProperty().addListener((ob,ov,nv) -> { modified = true; });

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


        HBox pointsBox = new HBox(pointsPossibleLabel, pointsPossibleTextField);
        pointsBox.setAlignment(Pos.BASELINE_LEFT);

        HBox topFieldsBox = new HBox(30, nameBox, pointsBox);

        Label auxNameLabel = new Label("Aux Exer Name:   ");
        auxNameLabel.setPrefWidth(105);
        auxNameLabel.setAlignment(Pos.CENTER_RIGHT);
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

        HBox auxExerNameBox = new HBox( auxNameLabel, auxNameField);

        useAuxExerCheck = new CheckBox();
        useAuxExerCheck.setSelected(false);
        Label useAuxExerLabel = new Label("Parse tree from aux exer:");
        HBox useAuxExerBox = new HBox(10, useAuxExerLabel, useAuxExerCheck);
        useAuxExerBox.setAlignment(Pos.CENTER_LEFT);

        useAuxExerCheck.selectedProperty().addListener((ob, ov, nv) -> {
            modified = true;
            if (nv) {
                omitFormalTreeCheck.setSelected(false);
                treesOptionalCheck.setSelected(false);
            }
        });

        Label treesOptionalLabel = new Label("Trees optional:");
        treesOptionalCheck = new CheckBox();
        treesOptionalCheck.setSelected(false);
        HBox treesOptionalBox = new HBox(10, treesOptionalLabel, treesOptionalCheck);
        treesOptionalBox.setAlignment(Pos.CENTER_LEFT);


        treesOptionalCheck.selectedProperty().addListener((ob, ov, nv) -> {
            modified = true;
            if (nv) {
                useAuxExerCheck.setSelected(false);
                omitFormalTreeCheck.setSelected(false);
            }
        });


        omitFormalTreeCheck = new CheckBox();
        omitFormalTreeCheck.setSelected(false);
        omitFormalTreeCheck.selectedProperty().addListener((ob, ov, nv) -> {
            modified = true;
            if (nv) {
                naturalVsGoodCheck.setSelected(false);
                useAuxExerCheck.setSelected(false);
                treesOptionalCheck.setSelected(false);
            }
        });
        Label omitFormalTreeLabel = new Label("Exclude formal tree from check:");
        HBox omitFormalTreeBox = new HBox(10, omitFormalTreeLabel, omitFormalTreeCheck);
        omitFormalTreeBox.setAlignment(Pos.CENTER_LEFT);

        HBox auxNameBox = new HBox(30, auxExerNameBox, omitFormalTreeBox, useAuxExerBox, treesOptionalBox);
        auxNameBox.setAlignment(Pos.CENTER_LEFT);

        targetKeyMapValue = ITALIC_AND_SANS;
        interpretationDRTA.getKeyboardSelector().valueProperty().setValue(BASE);

        includedInterpFnCheck = new CheckBox();
        includedInterpFnCheck.setSelected(true);
        includedInterpFnCheck.selectedProperty().addListener((ob, ov, nv) -> {
            modified = true;
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

        //natual vs good
        naturalVsGoodCheck = new CheckBox();
        naturalVsGoodCheck.setSelected(true);
        naturalVsGoodCheck.selectedProperty().addListener((ob, ov, nv) -> {
            modified = true;
            if (nv) omitFormalTreeCheck.setSelected(false);
        });
        Label naturalVsGoodLabel = new Label("Distinguish natural from (merely) good translations:");
        HBox naturalVsGoodBox = new HBox(10, naturalVsGoodLabel, naturalVsGoodCheck);
        naturalVsGoodBox.setAlignment(Pos.CENTER_LEFT);

        //main formulas top
        mainFormulasPane = new GridPane();
        mainFormulasPane.setPadding(new Insets(10, 0, 0, 105));
        mainFormulasPane.setVgap(10);
        targetFormulaList = new ArrayList<>();
        BoxedDRTA mainFormulaBoxedDRTA = newMainFormulaBoxedDRTA();
        mainFormulaBoxedDRTA.getRTA().getActionFactory().saveNow().execute(new ActionEvent());
        targetFormulaList.add(mainFormulaBoxedDRTA);
        updateTargetFormulaGridFromFields();

        Label mainFormulaLabel = new Label("Target(s): ");
        mainFormulaLabel.setPrefWidth(105);
        mainFormulaLabel.setAlignment(Pos.CENTER_RIGHT);
        Button addMainFormulaButton = new Button("+");
        Button removeMainFormulaButton = new Button("-");
        addMainFormulaButton.setFont(new Font(16)); removeMainFormulaButton.setFont(new Font(16));
        addMainFormulaButton.setPadding(new Insets(0, 5, 0, 5)); removeMainFormulaButton.setPadding(new Insets(1, 8, 1, 8));

        addMainFormulaButton.setOnAction(e -> {
            BoxedDRTA bdrta = newMainFormulaBoxedDRTA();
            targetFormulaList.add(bdrta);
            modified = true;
            updateTargetFormulaGridFromFields();
        });
        removeMainFormulaButton.setOnAction(e -> {
            int index = targetFormulaList.size();
            index--;
            if (index > 0) {
                targetFormulaList.remove(index);
                modified = true;
                updateTargetFormulaGridFromFields();
            } else {
                EditorAlerts.showSimpleAlert("Cannot Remove", "Check requires at leat one target formula.");
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

        HBox mainFormulasTop = new HBox(20, mainFormulaLabel, addMainFormulaButton, removeMainFormulaButton, naturalVsGoodBox);
        mainFormulasTop.setPadding(new Insets(20,0,0,0));
        mainFormulasTop.setMargin(naturalVsGoodBox, new Insets(0,0,0,20));
        mainFormulasTop.setAlignment(Pos.CENTER_LEFT);

        fieldsBox = new VBox(15, topFieldsBox, auxNameBox, mainFormulasTop, mainFormulasPane);
        fieldsBox.setPadding(new Insets(20,0, 0, 20));

        //check items
        staticHelpCheck = new CheckBox();
        staticHelpCheck.setSelected(false);
        ChangeListener staticHelpCheckListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                modified = true;
            }
        };
        staticHelpCheck.selectedProperty().addListener(staticHelpCheckListener);
        Label staticHelpCheckLabel = new Label("Static Help: ");
        staticHelpCheckLabel.setPrefWidth(105);
        staticHelpCheckLabel.setAlignment(Pos.CENTER_RIGHT);
        HBox staticHelpBox = new HBox(10, staticHelpCheckLabel, staticHelpCheck);
        staticHelpBox.setAlignment(Pos.CENTER_LEFT);

        checkMaxSpinner = new Spinner<>(-1, 99, 0);
        checkMaxSpinner.setPrefWidth(60);
        Label checkMaxLabel = new Label("Check Max:");
        HBox checkMaxBox = new HBox(10, checkMaxLabel, checkMaxSpinner);
        checkMaxBox.setAlignment(Pos.CENTER_LEFT);

        HBox checkLine1 = new HBox(30, staticHelpBox, checkMaxBox, includeInterpFnBox);
        checkLine1.setAlignment(Pos.CENTER_LEFT);

        fieldsBox = new VBox(15, topFieldsBox, auxNameBox, checkLine1, mainFormulasTop, mainFormulasPane);
        fieldsBox.setPadding(new Insets(20,0, 0, 20));

        //center
        String header = TextHelpPopup.getHeader14();
        String footer = TextHelpPopup.getFooter();
        String helpText = header +
                "<p>Translation Tree exercises build sentential translations by constructing \"parallel trees\" as from exercises in section 5.2 of <i>Symbolic Logic</i>.</p>" +
                "<ul>" +
                "<li><p>Begin with the exercise name and points possible.</p></li>" +
                "<li><p>In the context of an assignment containing a previous exercise relevant to the one being worked, it is possible to add its name in order to include a thumbnail that will pop up its work area. </p>" +
                "<p>In case an auxiliary exercise is itself a translation tree exercise, it is possible to split checking between the two - taking the parse tree and interpretation function from one, and the formal tree from the other.  In this case the former has 'Exclude formal tree from check' and the latter 'Parse tree from aux exer'.  For this it is sufficient to state the target formula(s) and interpretation function in the former exercise.</p>"+
                "<p>It is possible to make trees optional.  If the 'Optional' check is selected, and trees are omitted, check looks just at the interpretation function and answer (but use \"Simple Translation Exercise\" if the problem is quantificational or you don't want the trees field at all). </p></li>" +
                "<li><p>The static help check activates the Static Help button which pops up a message which you may state in the right-hand text area below.  If Check Max is 0, checking is disabled; if -1 checks are unlimited; otherwise the value sets the maximum number of allowable check tries. </p></li>" +
                "<li><p>For these exercises check takes as inputs your formal target translation and the interpretation function.  " +
                "In the vocabulary of <i>Symbolic Logic</i> it looks for an internally well-constructed parse tree and then, given the interpretation function, a node-for-node parallel formal tree ending with the target. </p></li>" +
                "<ul>" +
                "<li><p>If the interpretation function is to be supplied by you, formal expressions for the target and interpretation function should be stated using the formal object language.</p>" +
                "<p>If the interpretation function will be supplied by the student, then state the target and interpretation function in the formal metalanguage, using any of capital script \ud835\udc9c . . . \u2110, \ud835\udcae . . . \ud835\udcb5 (script A-I, S-Z with or without positive integer subscripts) as sentence letters - as the student need only supply an instance.  " +
                "In this case, you should include interpretation entries for each element of a numbered exercise - in case the student is doing so as well (perhaps copying the interpretation function from one member to the next).</p></li>" +
                "<li><p>Again in the vocabulary of <i>Symbolic Logic</i> it is possible to distinguish 'natual' from (merely) 'good' translations.  If this is so, you may provide multiple (equivalent) natural target expressions.   </p></li>" +
                "<li><p>The interpretation function (object or meta) should be a list with items of the sort: sentence letter/colon/sentence.  Check uses some light natural language processing to recognize (a limited range of) ordinary language equivalents.  </p></li>" +
                "</ul>" +
                "<li><p>Finally, provide the exercise statement, and if desired static help. </p></li>" +
                "</ul>" +
                header;

        scene = new Scene(borderPane);

        Browser browser = mainWindow.getCreateHlpBrowser();
        if (browser != null) {
            BrowserView view = BrowserView.newInstance(browser);
            view.setMouseTransparent(true);
            Platform.runLater(() -> TextHelpPopup.loadHtmlSmoothly(browser, view, helpText));
            view.setPrefHeight(750);
            view.prefWidthProperty().bind(textBoxes.widthProperty());
            centerBox = new VBox(10, fieldsBox, textBoxes, view);
            centerBox.setFillWidth(true);
        }
        else {
            centerBox = new VBox(10, fieldsBox, textBoxes, new Label("JxBrowser not started.  Please try again."));
        }

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
        Spinner<Integer> zoomSpinner = new Spinner(25, 500, 100, 1);
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

        //generate view

        scene.getStylesheets().add(DecoratedRTA.class.getClassLoader().getResource("slappEditor.css").toExternalForm());

        setSizeSpinners();
        stage = new Stage();
  //      stage.initOwner(EditorMain.mainStage);
        stage.setScene(scene);
        stage.setTitle("Create Translation Trees Exercise:");
        stage.getIcons().addAll(EditorMain.icons);

        stage.setWidth(1040);

        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        double stageHeight = Math.min(1025, bounds.getHeight() - 20);
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
        staticHelpRTA.getActionFactory().save().execute(new ActionEvent());
        centerBox.layout();

        Platform.runLater(() -> Platform.runLater(() -> nameField.requestFocus()));

    }


    private BoxedDRTA newMainFormulaBoxedDRTA() {
        BoxedDRTA boxedDRTA = new BoxedDRTA();
        DecoratedRTA drta = boxedDRTA.getDRTA();
        drta.getKeyboardSelector().valueProperty().setValue(targetKeyMapValue);
        RichTextArea rta = boxedDRTA.getRTA();
        rta.setMaxHeight(formulaBoxHeight);
        rta.setMinHeight(formulaBoxHeight);
        rta.setPrefWidth(300);
        rta.getStylesheets().add("RichTextFieldWide.css");
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
    private void updateMainFormulaFieldsFromModel(TransTreeModel model){
        targetFormulaList.clear();
        List<Document> formulasList = model.getCheckSetup().getTargetDocs();
        for (Document doc : formulasList) {
            BoxedDRTA bdrta = newMainFormulaBoxedDRTA();

            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().open(doc).execute(new ActionEvent());
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            targetFormulaList.add(bdrta);
        }
    }

    /*
     * Update the main operator grid pane from the bdrta fields
     */
    private void updateTargetFormulaGridFromFields(){
        mainFormulasPane.getChildren().clear();
        for (int i = 0; i < targetFormulaList.size(); i++) {
            BoxedDRTA bdrta = targetFormulaList.get(i);
            mainFormulasPane.add(bdrta.getBoxedRTA(), 0, i);
        }
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
        if (modified || statementRTA.isModified() || staticHelpRTA.isModified() || interpretationRTA.isModified() ) {
            Alert confirm = EditorAlerts.confirmationAlert(title, content);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.get() != OK) okcontinue = false;
        }
        return okcontinue;
    }

    private void closeWindow() {
        if (checkContinue("Confirm Close", "This exercise appears to have unsaved changes.\nContinue to close window?")) {
            localScalePageHeight.unbind();
            localScalePageWidth.unbind();
            mainWindow.closeExercise();
            stage.close();
        }
    }


    private void clearExercise() {
        if (checkContinue("Confirm Clear", "This exercise appears to have unsaved changes.\nContinue to clear exercise?")) {
            nameField.clear();
            nameField.textProperty().addListener(nameListener);
            auxNameField.clear();
            statementRTA.getActionFactory().newDocumentNow().execute(new ActionEvent());
            staticHelpRTA.getActionFactory().newDocumentNow().execute(new ActionEvent());
            viewExercise();

            targetFormulaList.clear();
            BoxedDRTA mainFormulaBoxedDRTA = newMainFormulaBoxedDRTA();
            mainFormulaBoxedDRTA.getRTA().getActionFactory().saveNow().execute(new ActionEvent());
            targetFormulaList.add(mainFormulaBoxedDRTA);
            updateTargetFormulaGridFromFields();

            modified = false;
        }
    }

    private void viewExercise() {
        TransTreeModel model = extractModelFromWindow();
        TransTreeExercise exercise = new TransTreeExercise(model, mainWindow);
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

        TransTreeModel model = extractModelFromWindow();
        DiskUtilities.saveExercise(saveAs, model);

        saveButton.setDisable(false);
        saveAsButton.setDisable(false);
        modified = false;
    }

    private TransTreeModel extractModelFromWindow() {
        TransTreeModel model = new TransTreeModel();
        model.setExerciseName(nameField.getText());

        if (!pointsPossibleTextField.getText().equals("")) model.setPointsPossible(Integer.parseInt(pointsPossibleTextField.getText()));
        else {
            model.setPointsPossible(0);
            pointsPossibleTextField.setText("0");
        }

        if (statementRTA.isModified()) modified = true;
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseStatement(statementRTA.getDocument());

        statementTextHeight = statementRTA.getFullHeight();
        model.setStatementTextHeight(statementTextHeight);
        model.setStatementPrefHeight(Math.max(40, statementTextHeight + RTA_SCREEN_BMP));



        model.setAuxExerName(auxNameField.getText());
        boolean interpFnIncluded = includedInterpFnCheck.isSelected();
        model.setInterpFnIncluded(interpFnIncluded);

        TTreeCheckSetup checkSetup = model.getCheckSetup();
        if (checkSetup == null) checkSetup = new TTreeCheckSetup();
        checkSetup.setCheckNatural(naturalVsGoodCheck.isSelected());
        checkSetup.setExcludeFormalTree(omitFormalTreeCheck.isSelected());
        checkSetup.setParseTreeFromAux(useAuxExerCheck.isSelected());
        int checkMax = (Integer) checkMaxSpinner.getValue();
        checkSetup.setCheckMax(checkMax);
        checkSetup.setStaticHelp(staticHelpCheck.isSelected());
        checkSetup.setTreesOptional(treesOptionalCheck.isSelected());

        List<Document> targetDocs = checkSetup.getTargetDocs();
        targetDocs.clear();
        for (BoxedDRTA bdrta : targetFormulaList) {
            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            targetDocs.add(rta.getDocument());
        }

        if (interpretationRTA.isModified()) modified = true;
        interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());

        if (interpFnIncluded) {
            model.setInterpFnDoc(interpretationRTA.getDocument());
            model.setInterpFnTextHeight(interpretationTextHeight);
            model.setInterpFnPrefHeight(Math.max(60, interpretationTextHeight + RTA_SCREEN_BMP)); //interpretationRTA.getPrefHeight() + RTA_SCREEN_BMP));
        }
        else {
            checkSetup.setKeyInterpFnDoc(interpretationRTA.getDocument());
            model.setInterpFnPrefHeight(100);
        }


        if (staticHelpRTA.isModified()) modified = true;
        staticHelpRTA.getActionFactory().saveNow().execute(new ActionEvent());
        checkSetup.setStaticHelpDoc(staticHelpRTA.getDocument());

        /*checks:
        target expressions are formulas
        target formulas equivalent
        process interpretation function

         */
        String langName;
        if (interpFnIncluded) langName = "\u2112\ud835\udcc8 (w/abv)";
        else langName = "Meta Sym";
        boolean goodCheck = true;

        if (checkMax != 0) {
            String message = "";

            for (int i = 0; i < targetDocs.size(); i++) {
                Document doc = targetDocs.get(i);
                if (doc.getText().equals("")) {
                    goodCheck = false;
                    message = "Check requires only non-empty target formula(s).  Check disabled.";
                    break;
                } else {
                    List<Expression> parseList = ParseUtilities.parseDoc(doc, langName);
                    if (parseList.size() != 1) {
                        goodCheck = false;
                        if (targetDocs.size() == 1)
                            message = "Target expression not a formula of " + langName + ". Check disabled.";
                        else
                            message = "Target expression in blank" + ++i + " not a formula of " + langName + ". Check disabled.";
                        break;
                    } else {
                        Expression targetExp = parseList.get(0);
                        if (targetExp.getType() != ExpressionType.FORMULA) {
                            goodCheck = false;
                            if (targetDocs.size() == 1)
                                message = "Target expression not a formula of " + langName + ". Check disabled.";
                            else
                                message = "Target expression in blank " + ++i + " not a formula of " + langName + ". Check disabled.";
                            break;
                        }
                    }
                }
            }
            if (goodCheck) {
                outer:
                for (int i = 0; i < targetDocs.size(); i++) {
                    for (int j = i + 1; j < targetDocs.size(); j++) {
                        if (!TransTreeCheck.checkEquivalence(targetDocs.get(i), targetDocs.get(j), langName)) {
                            goodCheck = false;
                            message = "Target expressions in blanks " + ++i + " and " + ++j + " not equivalent.  Check disabled.";
                            break outer;
                        }
                    }
                }
            }

            if (!goodCheck) {
                checkMaxSpinner.getValueFactory().setValue(0);
                checkSetup.setCheckMax(0);
                EditorAlerts.fleetingRedPopup(message);
            }
        }


        if (goodCheck && checkMax != 0) {
            List<InterpFnElement> elements = TransTreeCheck.processInterpFn(interpretationRTA.getDocument(), Languages.getLanguage(langName));
            if (elements == null) {
                checkMaxSpinner.getValueFactory().setValue(0);;
                checkSetup.setCheckMax(0);
            }
            else {
                for (Document doc : targetDocs) {
                    if (!TransTreeCheck.verifyInterpFnAdequacy(elements, doc, Languages.getLanguage(langName), false)) {
                        checkMaxSpinner.getValueFactory().setValue(0);
                        checkSetup.setCheckMax(0);
                        break;
                    }
                }
            }
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
