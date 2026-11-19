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

package slapp.editor.basic_trans;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.view.javafx.BrowserView;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;
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
import slapp.editor.parser.*;
import slapp.editor.parser.grammatical_parts.Formula;
import slapp.editor.parser.grammatical_parts.PrefixAtomic;
import slapp.editor.parser.symbols.LanguageElement;
import slapp.editor.parser.symbols.RelationSymbol;
import slapp.editor.trans_tree.InterpFnElement;
import slapp.editor.trans_tree.TransTreeCheck;

import java.util.*;
import java.util.List;
import java.util.function.UnaryOperator;

import static com.gluonhq.richtextarea.RichTextAreaSkin.KeyMapValue.*;
import static com.gluonhq.richtextarea.RichTextAreaSkin.KeyMapValue.SCRIPT_AND_ITALIC;
import static javafx.scene.control.ButtonType.OK;
import static slapp.editor.EditorMain.RTA_SCREEN_BMP;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;
import static slapp.editor.parser.Languages.getQuantificationalLangNames;
import static slapp.editor.parser.Languages.getSententialLangNames;

/**
 * Create window for the simple translate exercise
 */
public class BasicTransCreate {
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
    private boolean modified;
    private ChangeListener nameListener;
    private ChangeListener auxExerListener;
    private double scale = 1.0;
    private Scene scene;
    private Stage stage;
    private SimpleDoubleProperty centerHeightProperty;
    //private TextArea helpArea;
    private VBox centerBox;
    private Button saveButton;
    private Button saveAsButton;
    private ToolBar sizeToolBar;
    private DecoratedRTA dummyDRTA = new DecoratedRTA();

    private Pane spacerPane;
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> interpretationHeightSpinner;
    private Spinner<Double> verticalSizeSpinner;
    private RichTextArea currentSpinnerNode;
    private TextField pointsPossibleTextField;
    private TextField auxExerNameField;
    private CheckBox includedInterpFnCheck;
    private CheckBox staticHelpCheck;
    private Spinner<Double> staticHelpHeightSpinner;
    private Spinner checkMaxSpinner;
    private RichTextArea staticHelpRTA;
    private DecoratedRTA staticHelpDRTA;
    private List<BoxedDRTA> targetFormulaList;
    private GridPane mainFormulasPane;
    private BTransCheckSetup checkSetup;
    private RichTextAreaSkin.KeyMapValue targetKeyMapValue;
    private double targetFormulaBoxHeight = 27;
    private BoxedDRTA focusedBoxedDRTA;
    private VBox helpArea;
    private Map<String, SimpleBooleanProperty> langMap;
    private String defaultLangName = "\u2112\ud835\udcc6 (w/abv)";
    private List<List<Expression>> keyExpressions = new ArrayList<>();
    private KeyObject[] keyObjects = new KeyObject[4];
    private Thread checkThread;
    private Boolean excludeTargetCheck = false;

    private final DoubleProperty localScalePageHeight = new SimpleDoubleProperty(1.0);
    private final DoubleProperty localScalePageWidth = new SimpleDoubleProperty(1.0);
    private CheckBox argumentCheckBox;



    /**
     * Construct the simple translate create window from scratch
     * @param mainWindow the main window
     */
    public BasicTransCreate(MainWindow mainWindow) {
        this.mainWindow = mainWindow;

        localScalePageHeight.bind(mainWindow.getMainView().scalePageHeightProperty());
        localScalePageWidth.bind(mainWindow.getMainView().scalePageWidthProperty());
        setupWindow();
        modified = false;
    }

    /**
     * Open create window for modification of existing simple translate exercise.
     * @param mainWindow the main window
     * @param originalModel model for the existing exercise
     */
    public BasicTransCreate(MainWindow mainWindow, BasicTransModel originalModel) {
        this(mainWindow);

        statementRTA.getActionFactory().open(originalModel.getExerciseStatement()).execute(new ActionEvent());
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        statementTextHeight = originalModel.getStatementTextHeight();

        interpretationRTA.getActionFactory().open(originalModel.getInterpFnDoc()).execute(new ActionEvent());
        interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());

        interpretationTextHeight = originalModel.getInterpFnTextHeight();

        nameField.setText(originalModel.getExerciseName());
        auxExerNameField.setText(originalModel.getAuxExerName());
        pointsPossibleTextField.setText(Integer.toString(originalModel.getPointsPossible()));
        includedInterpFnCheck.setSelected(originalModel.isInterpFnIncluded());



        checkSetup = originalModel.getCheckSetup();
        if (checkSetup == null) checkSetup = new BTransCheckSetup();

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
        defaultLangName = checkSetup.getObjLangName();

        argumentCheckBox.setSelected(checkSetup.isArgument());

        updateTargetFormulaFieldsFromSetup(checkSetup);
        updateTargetFormulaGridFromFields();

        for (String key : langMap.keySet()) {
            if (key.equals(checkSetup.getObjLangName())) {
                langMap.get(key).setValue(true);
            }
            else langMap.get(key).setValue(false);
        }

        modified = false;
    }

    /*
     * Set up the create view
     */
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

        /*
        interpretationHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = interpretationHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = interpretationHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });
         */

        localScalePageHeight.addListener((ob, ov, nv) -> {
            interpretationRTA.prefHeightProperty().unbind();
            interpretationHeightSpinner.getValueFactory().setValue((double) Math.round(interpretationHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            interpretationRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        });

        final boolean[] commentPrimed = {false};
        interpretationRTA.fullHeightProperty().addListener((ob, ov, nv) -> {
            if (!commentPrimed[0]) {
                commentPrimed[0] = true;
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


        //statement
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
            statementRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

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
        Label nameLabel = new Label("Exercise Name: ");
        nameLabel.setPrefWidth(105);
        nameLabel.setAlignment(Pos.CENTER_RIGHT);
        nameField  = new TextField();
        nameField.setPromptText("(plain text)");
        nameField.setPadding(new Insets(5));
        nameField.setPrefWidth(200);
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

        HBox nameBox = new HBox(20, nameLabel, nameField);
        nameBox.setAlignment(Pos.BASELINE_LEFT);

        //points possible
        Label pointsPossibleLabel = new Label("Points Possible: ");
        pointsPossibleLabel.setPrefWidth(105);
        pointsPossibleLabel.setAlignment(Pos.CENTER_RIGHT);


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


        HBox pointsBox = new HBox(20, pointsPossibleLabel, pointsPossibleTextField);
        pointsBox.setAlignment(Pos.BASELINE_LEFT);

        //aux exer
        Label auxExerLabel = new Label("Aux Exer Name:");
        auxExerLabel.setPrefWidth(105);
        auxExerLabel.setAlignment(Pos.CENTER_RIGHT);
        auxExerNameField = new TextField();
        auxExerNameField.setPadding(new Insets(5));
        auxExerNameField.setPromptText("(plain text)");
        auxExerNameField.setPrefWidth(200);
        auxExerListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                modified = true;
            }
        };
        auxExerNameField.textProperty().addListener(auxExerListener);
        auxExerNameField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        HBox auxExerNameBox = new HBox(20, auxExerLabel, auxExerNameField);
        auxExerNameBox.setAlignment(Pos.BASELINE_LEFT);

        HBox nameAndPointsBox = new HBox(30, nameBox, pointsBox, auxExerNameBox);
        nameAndPointsBox.setPadding(new Insets(20,0,20,0));

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
        HBox staticHelpBox = new HBox(20, staticHelpCheckLabel, staticHelpCheck);
        staticHelpBox.setAlignment(Pos.CENTER_LEFT);

        checkMaxSpinner = new Spinner<>(-1, 99, 0);
        checkMaxSpinner.setPrefWidth(60);
        Label checkMaxLabel = new Label("Check Max:");
        HBox checkMaxBox = new HBox(20, checkMaxLabel, checkMaxSpinner);
        checkMaxBox.setAlignment(Pos.CENTER_LEFT);

        targetKeyMapValue = SCRIPT_AND_ITALIC;
        interpretationDRTA.getKeyboardSelector().valueProperty().setValue(BASE_AND_SANS_ITALIC);




        includedInterpFnCheck = new CheckBox();
        includedInterpFnCheck.setSelected(true);
        includedInterpFnCheck.selectedProperty().addListener((ob, ov, nv) -> {
            modified = true;
            if (nv) {
                targetKeyMapValue = SCRIPT_AND_ITALIC;
                for (BoxedDRTA bdrta : targetFormulaList) {
                    bdrta.getDRTA().getKeyboardSelector().valueProperty().setValue(SCRIPT_AND_ITALIC);
                }
                interpretationDRTA.getKeyboardSelector().valueProperty().setValue(BASE_AND_SANS);
            }
            else {
                targetKeyMapValue = SCRIPT_AND_ITALIC;
                for (BoxedDRTA bdrta : targetFormulaList) {
                    bdrta.getDRTA().getKeyboardSelector().valueProperty().setValue(SCRIPT_AND_ITALIC);
                }
                interpretationDRTA.getKeyboardSelector().valueProperty().setValue(BASE_AND_SANS_ITALIC);
            }
        });
        Label includedInterpFnLabel = new Label("Interpretation function included in exercise:");
        HBox includeInterpFnBox = new HBox(10, includedInterpFnLabel, includedInterpFnCheck);
        includeInterpFnBox.setAlignment(Pos.CENTER_LEFT);

        //language list view
        Label languageLabel = new Label("   Object Language:");
        langMap = new LinkedHashMap<>();
        for (int i = 0; i < getSententialLangNames().size(); i++) {
            Language language = Languages.getLanguage(getSententialLangNames().get(i));
            SimpleBooleanProperty booleanProperty = new SimpleBooleanProperty(false);
            if (language.getNameString().equals(defaultLangName)) booleanProperty.setValue(true); //default check
            setSingleSelection(booleanProperty, langMap);
            langMap.put(language.getNameString(), booleanProperty);
        }


        for (int i = 0; i < getQuantificationalLangNames().size(); i++) {
            Language language = Languages.getLanguage(getQuantificationalLangNames().get(i));
            SimpleBooleanProperty booleanProperty = new SimpleBooleanProperty(false);
            if (language.getNameString().equals(defaultLangName)) booleanProperty.setValue(true); //default check
            setSingleSelection(booleanProperty, langMap);
            langMap.put(language.getNameString(), booleanProperty);
        }
        ListView<String> languageListView = new ListView<>();
        languageListView.setPrefSize(150, 95);

        languageListView.getItems().addAll(langMap.keySet());
        Callback<String, ObservableValue<Boolean>> langToBoolean = (String item) -> langMap.get(item);
        languageListView.setCellFactory(CheckBoxListCell.forListView(langToBoolean));

        HBox checkLine1 = new HBox(30, staticHelpBox, checkMaxBox, includeInterpFnBox, languageLabel, languageListView);
        checkLine1.setAlignment(Pos.CENTER_LEFT);

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

        if (includedInterpFnCheck.isSelected()) {
            targetKeyMapValue = SCRIPT_AND_ITALIC;
            for (BoxedDRTA bdrta : targetFormulaList) {
                bdrta.getDRTA().getKeyboardSelector().valueProperty().setValue(SCRIPT_AND_ITALIC);
            }
            interpretationDRTA.getKeyboardSelector().valueProperty().setValue(BASE_AND_SANS);
        }
        else {
            targetKeyMapValue = SCRIPT_AND_ITALIC;
            for (BoxedDRTA bdrta : targetFormulaList) {
                bdrta.getDRTA().getKeyboardSelector().valueProperty().setValue(SCRIPT_AND_ITALIC);
            }
            interpretationDRTA.getKeyboardSelector().valueProperty().setValue(BASE_AND_SANS_ITALIC);
        }

        Label argumentLabel = new Label("Argument:");
        argumentCheckBox = new CheckBox();


        HBox mainFormulasTop = new HBox(20, mainFormulaLabel, addMainFormulaButton, removeMainFormulaButton, argumentLabel, argumentCheckBox);
        mainFormulasTop.setPadding(new Insets(20,0,0,0));
        mainFormulasTop.setAlignment(Pos.CENTER_LEFT);
        mainFormulasTop.setMargin(argumentLabel, new Insets(0,0, 0, 60));

        VBox fieldsBox = new VBox(15, nameAndPointsBox, checkLine1, mainFormulasTop, mainFormulasPane);
        fieldsBox.setPadding(new Insets(20,0, 0, 20));

        String header = TextHelpPopup.getHeader14();
        String footer = TextHelpPopup.getFooter();
        String helpText = header +
                //"<body style=\"margin-left:10; margin-right: 20\">" +
                "<p>Basic Translation applies to exercises calling for a (sentential or quantificational) interpretation function and then a formal translation of some ordinary language sentence or argument.<p>" +
                "<ul>" +
                "<li><p>First provide the exercise name and points possible.  If 'points possible' is other than zero, a points field is added to the exercise comment area (and one for total assignment points into the assignment comment area).</p>" +
                "<p>In the context of an assignment containing a previous exercise relevant to the one being worked, it is possible to add its name in order to include a thumbnail that will pop up its work area. </p></li>" +
                "<li><p>The static help check activates the Static Help button which pops up a message which you may state in the right-hand text area below.  If Check Max is 0, checking is disabled; if -1 checks are unlimited; otherwise the value sets the maximum number of allowable check tries. You can select whether the student will start with your interpretation function given, and the object language.</p></li>" +
                "<li><p>Check requires some target expression(s).  If the translation is of a single sentence, it gives a green checkmark if equivalent to and an instance of, a target expression, purple if equivalent but not an instance -- where \"equivalent\" amounts to \"no countermodel on universe sizes up to 4.\"  " +
                "If the translation is of an argument your targets are translations for the premises and conclusion (taken in order), and it gives a green checkmark if student translations are equivalent.</p></li>" +
                "<ul>" +
                "<li><p>If the interpretation function is to be supplied by you, target expressions should use object constants, function symbols, sentence letters and relation symbols -- but script variables, \ud835\udcca, \ud835\udccb, \ud835\udccc, \ud835\udccd, \ud835\udcce, \ud835\udccf.  In the interpretation function, use the same formal symbols, and sans \ud835\uddba . . . \ud835\uddd3 for object variables - as \ud835\udc3f<sup>2</sup>: {⟨\ud835\uddce, \ud835\uddcf⟩ | \ud835\uddce, \ud835\uddcf ∈ \ud835\uddb4 and \ud835\uddce loves \ud835\uddcf}.</p></li>" +
                "<li><p>If the interpretation function will be supplied by the student, target expressions should be entirely in the script metalanguage using variables \ud835\udcca . . . \ud835\udccf, constants \ud835\udcb6 . . . \u212f, function symbols \ud835\udcbb<sup>\ud835\udc5b</sup> . . . \ud835\udcbd<sup>\ud835\udc5b</sup>, sentence letters \ud835\udc9c . . . \ud835\udca5, \ud835\udcae . . . \ud835\udcb5, and relation symbols \ud835\udca6<sup>\ud835\udc5b</sup> . . . \u211b<sup>\ud835\udc5b</sup>.  In the interpretation function use those same formal symbols but with sans italic for metalinguistic object variables - as \u2112<sup>2</sup>: {⟨\ud835\ude36, \ud835\ude37⟩ | \ud835\ude36, \ud835\ude37 ∈ \ud835\ude1c and \ud835\ude36 loves \ud835\ude37}.  Though not a regular SLAPP keyboard, you can access base/sans-ital by Shift-F3.</p>" +

                "<li><p>Key sentences should include superscripts on relation symbols, just as in the interpretation function (the student won't have to do this). " +
                "<li><p>Notice that \"Clear\" leaves the interpretation function intact from one exercise to the next.  So you can do a single interpretation function for an exercise set, and use it for all the exercises in the set. This is a good idea since a student may copy interpretation function entries from one problem to the next, and so include entries beyond those required for a particular problem.</p></li>" +
                "</ul>" +
                "<li><p>Finally, provide the exercise statement, and if desired static help.</p></li>" +
                "</ul>" +
                footer;


        Browser browser = mainWindow.getCreateHlpBrowser();
        if (browser != null) {
            BrowserView view = BrowserView.newInstance(browser);
            view.setMouseTransparent(true);
            Platform.runLater(() -> TextHelpPopup.loadHtmlSmoothly(browser, view, helpText));
            view.setPrefHeight(690);
            centerBox = new VBox(10, fieldsBox, textBoxes, view);
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
        sizeToolBar.getItems().addAll(zoomLabel, zoomSpinner, new Label("     V Sz:"), verticalSizeSpinner);

        setSizeSpinners();

        stage = new Stage();
   //     stage.initOwner(EditorMain.mainStage);
        stage.setScene(scene);
        stage.setTitle("Create Basic Translation Exercise:");
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

     //   stage.initModality(Modality.WINDOW_MODAL);
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

    private BoxedDRTA newMainFormulaBoxedDRTA() {
        BoxedDRTA boxedDRTA = new BoxedDRTA();
        DecoratedRTA drta = boxedDRTA.getDRTA();
        drta.getKeyboardSelector().valueProperty().setValue(targetKeyMapValue);
        RichTextArea rta = boxedDRTA.getRTA();
        rta.setMaxHeight(targetFormulaBoxHeight);
        rta.setMinHeight(targetFormulaBoxHeight);
        rta.setPrefWidth(600);
        rta.getStylesheets().add("RichTextFieldWide.css");
        rta.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(drta, ControlType.FIELD);
                focusedBoxedDRTA = boxedDRTA;
            }
        });
        return boxedDRTA;
    }

    private void updateTargetFormulaFieldsFromSetup(BTransCheckSetup setup){
        targetFormulaList.clear();
        List<Document> formulasList = setup.getTargetDocs();
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
            localScalePageHeight.unbind();
            localScalePageWidth.unbind();
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
            auxExerNameField.clear();
            statementRTA.getActionFactory().newDocumentNow().execute(new ActionEvent());
            staticHelpRTA.getActionFactory().newDocumentNow().execute(new ActionEvent());

            targetFormulaList = new ArrayList<>();
            BoxedDRTA mainFormulaBoxedDRTA = newMainFormulaBoxedDRTA();
            mainFormulaBoxedDRTA.getRTA().getActionFactory().saveNow().execute(new ActionEvent());
            targetFormulaList.add(mainFormulaBoxedDRTA);
            updateTargetFormulaGridFromFields();
   //         statementRTA.getActionFactory().open(new Document()).execute(new ActionEvent());
            excludeTargetCheck = true;
            int checkMax = (int) checkMaxSpinner.getValue();
            viewExercise();
            checkMaxSpinner.getValueFactory().setValue(checkMax);
            excludeTargetCheck = false;
            modified = false;
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

        if (modified || statementRTA.isModified() || interpretationRTA.isModified() || staticHelpRTA.isModified()) {

   //         System.out.println("mod: " + modified + " statement: " + statementRTA.isModified() + " interp: " + interpretationRTA.isModified() + " static: " + staticHelpRTA.isModified());
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
        BasicTransModel model = extractModelFromWindow();
        BasicTransExercise exercise = new BasicTransExercise(model, mainWindow);
        RichTextArea rta = exercise.getExerciseView().getExerciseStatement().getEditor();
        rta.prefHeightProperty().unbind();
        rta.setEditable(false);
        exercise.getExerciseView().setStatementPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getStatementPrefHeight()));

        RichTextArea interpRTA = exercise.getExerciseView().getExerciseInterpretation().getEditor();
        interpRTA.prefHeightProperty().unbind();
        exercise.getExerciseView().setInterpretationPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getInterpFnPrefHeight()));

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

        BasicTransModel model = extractModelFromWindow();

        BTransCheckSetup checkSetup = model.getCheckSetup();
        if (checkSetup.getCheckMax() != 0 && !argumentCheckBox.isSelected()) {

            Formula keyFormula = (Formula) (keyExpressions.get(0).get(0));
            RichTextArea keyRTA = targetFormulaList.get(0).getRTA();
            keyRTA.getActionFactory().saveNow().execute(new ActionEvent());
       //     checkSetup.setKeyFormulaDoc(keyRTA.getDocument());

            for (int i = 0; i < 4; i++) {
                keyObjects[i] = EquivalenceChecker.getKeyObject(keyFormula, i + 1);
       //         if (keyObjects[i] != null) System.out.println("key objects model size: " + keyObjects[i].getModelSize());
            }
     //       checkSetup.setKeyObjects(keyObjects);
     //       System.out.println("key objects complete");

            for (int i = 1; i < keyExpressions.size(); i++) {
                Formula formulaToCheck = (Formula) (keyExpressions.get(i).get(0));
                checkKeyEquivalence(keyFormula, formulaToCheck, checkSetup, i);
            }
        }

        boolean success = DiskUtilities.saveExercise(saveAs, model);
        if (success) {
            modified = false;
        }
        saveButton.setDisable(false);
        saveAsButton.setDisable(false);
    }

    private void checkKeyEquivalence(Formula keyFormula, Formula formulaToCheck, BTransCheckSetup checkSetup, int targetIndex) {

        checkThread = java.lang.Thread.startVirtualThread(() -> {

            final java.util.concurrent.atomic.AtomicInteger lastN = new java.util.concurrent.atomic.AtomicInteger(0);

            try {
                long startTime = System.currentTimeMillis();
                List<LanguageElement> combinedSyms = EquivalenceChecker.getCombinedSymbolList(keyFormula, formulaToCheck);

                for (int i = 0; i < 4; i++) {
                    final int currentIndex = i;

                    if (Thread.currentThread().isInterrupted()) {
            //            System.out.println("Process interrupted; no error found on universes up to " + (currentIndex) + ".");
                        return;
                    }

                    long timeElapsed = System.currentTimeMillis() - startTime;
                    long timeLeft = 15000 - timeElapsed;

                    if (timeLeft <= 0) {
             //           System.out.println("Process timed out; no error found on universes up to " + (currentIndex) + ".");
                        return;
                    }

                    ModelBlueprint blueprint = new ModelBlueprint(combinedSyms, currentIndex + 1);

                    KeyObject keyObj = keyObjects[currentIndex];

                    if (keyObj != null) {
                        EquivalenceChecker.checkEquivalenceAgainstKey(keyObj, formulaToCheck, currentIndex + 1, combinedSyms, blueprint.infoMap, blueprint.bitToSymbolArray, blueprint.fullModelSize, timeLeft);
                    }
                    else {
                        EquivalenceChecker.checkLargeParallel(keyFormula, formulaToCheck, currentIndex + 1, blueprint.fullSymList, blueprint.infoMap, blueprint.bitToSymbolArray, blueprint.fullModelSize, timeLeft);
                    }
                    if (Thread.currentThread().isInterrupted()) return;
                    lastN.set(currentIndex + 1);

                }
                if (!java.lang.Thread.currentThread().isInterrupted()) {
         //           System.out.println("Process complete; equivalent on universes up to 4.");
                }
                endEquivThread();
            }
            catch (ModelContentException modelExcep) {
                Platform.runLater(() -> {
                    targetFormulaList.get(targetIndex).setVTtreeBoxHighlight();
                });

                List<Text> modelMessageList = new ArrayList<>();
                modelMessageList.add(ParseUtilities.newRegularText("Consider a case where "));
                modelMessageList.addAll(modelExcep.getUniverse());
                modelMessageList.add(ParseUtilities.newRegularText(" and assignments are such that,\n"));

                List<Pair<LanguageElement, List<Text>>> assignmentPairs = modelExcep.getAssignmentList();
                for (int j = 0; j < assignmentPairs.size(); j++) {
                    Pair<LanguageElement, List<Text>> pair = assignmentPairs.get(j);
                    modelMessageList.add(new Text("    "));
                    modelMessageList.addAll(pair.getKey().toTextList());
                    modelMessageList.add(new Text(": "));
                    modelMessageList.addAll(pair.getValue());
                    modelMessageList.add(new Text("\n"));
                }

                if (modelExcep.isProfTrue()) {
                    modelMessageList.add(ParseUtilities.newRegularText("Then the the first target sentence "));
                    modelMessageList.addAll(keyFormula.toTextList());
                    modelMessageList.add(ParseUtilities.newRegularText(" is true and the highlighted member is false."));

                }
                else {
                    modelMessageList.add(ParseUtilities.newRegularText("Then the first target sentence "));
                    modelMessageList.addAll(keyFormula.toTextList());
                    modelMessageList.add(ParseUtilities.newRegularText(" is false and the highlighted member is true."));
                }
                Platform.runLater(() ->{
                    EditorAlerts.showSimpleTxtListAlert("Equivalence Error:", modelMessageList);
                    targetFormulaList.get(targetIndex).resetVTtreeBoxHighlight();

                });
                endEquivThread();
                return;
            }
            catch (Exception e) {
                if (!java.lang.Thread.currentThread().isInterrupted() || e.getCause() instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                else {
                    Platform.runLater(() -> {
                        System.out.println("unexpected thread error");
                    });
                }
                endEquivThread();
            }
            finally {
                if (Thread.currentThread().isInterrupted()) {
                    int finished = lastN.get();
       //             Platform.runLater(() -> System.out.println("Process interrupted or timed out; no error found on universes up to " + finished + "."));
                }
                endEquivThread();
            }
        });
    }


    private void endEquivThread() {
        if (checkThread != null) {
            checkThread = null;
        }
    }


    /*
     * Generate simple translate model from window contents
     * @return the simple translate model
     */
    private BasicTransModel extractModelFromWindow() {
        BasicTransModel model = new BasicTransModel(nameField.getText());
        if (statementRTA.isModified() || interpretationRTA.isModified()) modified = true;

        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseStatement(statementRTA.getDocument());

        statementTextHeight = statementRTA.getFullHeight();
        model.setStatementTextHeight(statementTextHeight);
        model.setStatementPrefHeight(Math.max(40, statementTextHeight + RTA_SCREEN_BMP));

        interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());

        if (!pointsPossibleTextField.getText().equals("")) model.setPointsPossible(Integer.parseInt(pointsPossibleTextField.getText()));
        else {
            model.setPointsPossible(0);
            pointsPossibleTextField.setText("0");
        }
        model.setAuxExerName(auxExerNameField.getText());

        BTransCheckSetup checkSetup = model.getCheckSetup();
        if (checkSetup == null) checkSetup = new BTransCheckSetup();


        if (staticHelpRTA.isModified()) modified = true;
        staticHelpRTA.getActionFactory().saveNow().execute(new ActionEvent());
        checkSetup.setStaticHelpDoc(staticHelpRTA.getDocument());
        checkSetup.setStaticHelp(staticHelpCheck.isSelected());

        int checkMax = (Integer) checkMaxSpinner.getValue();
        checkSetup.setCheckMax(checkMax);

        boolean interpFnIncluded = includedInterpFnCheck.isSelected();
        model.setInterpFnIncluded(interpFnIncluded);

        checkSetup.setArgument(argumentCheckBox.isSelected());

        if (interpFnIncluded) {
            model.setInterpFnDoc(interpretationRTA.getDocument());
            model.setInterpFnTextHeight(interpretationTextHeight);
            model.setInterpFnPrefHeight(Math.max(60, interpretationTextHeight + RTA_SCREEN_BMP));
        }
        else {
            checkSetup.setKeyInterpFnDoc(interpretationRTA.getDocument());
            model.setInterpFnPrefHeight(125);
        }


        boolean goodCheck = true;

        String langName = null;
        for (String key : langMap.keySet()) {
            ObservableValue<Boolean> value = langMap.get(key);
            if (value.getValue()) {
                langName = key;
                break;
            }
        }
        boolean requireUniv = true;
        if (langName != null) {
            checkSetup.setObjLangName(langName);
            List<String> sentLangNames = Languages.getSententialLangNames();
            if (sentLangNames.contains(langName)) requireUniv = false;

        }

        else if (checkMax != 0) {
            EditorAlerts.fleetingRedPopup("No language selected.  Check disabled.");
            checkMaxSpinner.getValueFactory().setValue(0);
     //       System.out.println("1");

            checkSetup.setCheckMax(0);
            goodCheck = false;
            checkSetup.setObjLangName(null);
        }

        //currently when the interp function is not included, check attempts to do matching.  But that won't work for math language.
        //Can see no reason for math language that is not included so ok
        if (!interpFnIncluded &&  Languages.getMathLangNames().contains(langName)) {
            EditorAlerts.fleetingRedPopup("The interpretation function for a mathematical language is expected to be included in exercise.");
            includedInterpFnCheck.setSelected(true);
            interpFnIncluded = true;
            model.setInterpFnIncluded(true);
        }


        String lang = "Mvar" + langName;
        if (!model.isInterpFnIncluded()) lang = "Meta Sym";

        List<Document> targetFormulaDocs = new ArrayList<>();
        keyExpressions.clear();
        for (int i = 0; i < targetFormulaList.size(); i++) {
            BoxedDRTA bdrta = targetFormulaList.get(i);
            RichTextArea rta = bdrta.getRTA();
            if (rta.isModified()) modified = true;
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            Document doc = rta.getDocument();

            List<Expression> expressions = new ArrayList<>();
            if (lang != null) expressions = ParseUtilities.parseDoc(doc, lang);
            if (!expressions.isEmpty()) keyExpressions.add(expressions);


            if (!excludeTargetCheck && checkMax != 0 && expressions.size() == 0 && goodCheck) {
                EditorAlerts.fleetingRedPopup("Cannot have empty target field for check.  Check disabled.");
                checkMaxSpinner.getValueFactory().setValue(0);
         //       System.out.println("2");


                checkSetup.setCheckMax(0);
                goodCheck = false;
            }

            if ((goodCheck && checkMax != 0 && expressions.size() != 0) && (expressions.size() != 1 || !(expressions.get(0) instanceof Formula) || !SyntacticalFns.sentence(expressions.get(0), lang))) {
                bdrta.setVTtreeBoxHighlight();

                List<Text> texts = new ArrayList<>();
                texts.add(ParseUtilities.newRegularText("Expression is not a sentence of "));
                texts.addAll(Languages.getLanguage(lang).getNameTextList());
                texts.add(ParseUtilities.newRegularText(" (with script meta variables, symbols as below, and superscripts on relation symbols).  Check disabled.\n\n"));
                texts.add(ParseUtilities.newRegularText(".\n\nFound:\n"));
                for (Expression exp : expressions) {
                    texts.add(new Text("    "));
                    texts.add(new Text(exp.getType() + ": "));
                    texts.addAll(exp.toTextList());
                    texts.add(new Text("\n"));
                }


                EditorAlerts.showSimpleTxtListAlert("Not A Sentence:", texts);
        //        rta.getActionFactory().open(new Document()).execute(new ActionEvent());
                modified = true;
                checkMaxSpinner.getValueFactory().setValue(0);
         //       System.out.println("3");

                checkSetup.setCheckMax(0);
                goodCheck = false;
                bdrta.resetVTtreeBoxHighlight();
            }

            try {
                if (expressions.size() != 0) checkSuperscripts(expressions.get(0), Languages.getLanguage(lang));
            }
            catch(TextMessageException e) {
                bdrta.setVTtreeBoxHighlight();
                EditorAlerts.showSimpleTxtListAlert("Target Formula:", e.getMessageList());
                modified = true;
                checkMaxSpinner.getValueFactory().setValue(0);
        //        System.out.println("4");

                checkSetup.setCheckMax(0);
                goodCheck = false;
                bdrta.resetVTtreeBoxHighlight();
            }

            targetFormulaDocs.add(doc);
        }
        if (targetFormulaDocs.isEmpty()) {
            targetFormulaDocs.add(new Document());
        }
        checkSetup.setTargetDocs(targetFormulaDocs);

        if (goodCheck && checkMax != 0) {
            List<InterpFnElement> elements = TransTreeCheck.processInterpFn(interpretationRTA.getDocument(), Languages.getLanguage(lang));
            if (elements == null) {
                checkMaxSpinner.getValueFactory().setValue(0);
         //       System.out.println("5");

                checkSetup.setCheckMax(0);
            }
            else {
                for (Document doc : checkSetup.getTargetDocs()) {
                    if (!TransTreeCheck.verifyInterpFnAdequacy(elements, doc, Languages.getLanguage(lang), requireUniv)) {
                        checkMaxSpinner.getValueFactory().setValue(0);
             //           System.out.println("6");

                        checkSetup.setCheckMax(0);
                        break;
                    }
                }
            }
        }


        model.setCheckSetup(checkSetup);

        return model;
    }

    private void checkSuperscripts(Expression exp, Language lang) throws TextMessageException {
        if (exp == null) return;

        if (exp instanceof PrefixAtomic) {
            PrefixAtomic atomic = (PrefixAtomic) exp;
            RelationSymbol relSym = atomic.getMainRelation();

            if (lang.getXrelationSymbols().contains(relSym.getBaseStr())) {
                int places = relSym.getPlaces();

                String supString = relSym.getSuperscriptStr();
                if (supString == null || supString.equals(""))  throw new TextMessageException(Collections.singletonList(new Text("Relation symbol(s) missing superscript.")));

                int supInt = Integer.parseInt(supString);
                if (places != supInt)   throw new TextMessageException(Collections.singletonList(new Text("Relation symbol(s) with places mismatch.")));
            }
            return;
        }

        if (exp.getChildren() != null) {
            for (Expression child : exp.getChildren()) {
                checkSuperscripts(child, lang);
            }
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
