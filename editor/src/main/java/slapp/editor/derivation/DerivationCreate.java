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

package slapp.editor.derivation;

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
import javafx.collections.*;
import javafx.event.ActionEvent;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
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
import slapp.editor.derivation.der_systems.DerivationRuleset;
import slapp.editor.derivation.der_systems.DerivationRulesets;
import slapp.editor.derivation.theorems.TheoremSet;
import slapp.editor.derivation.theorems.TheoremSets;
import slapp.editor.derivation.theorems.ThrmSetElement;
import slapp.editor.main_window.ControlType;
import slapp.editor.main_window.MainWindow;
import slapp.editor.main_window.MainWindowView;
import slapp.editor.parser.Language;
import slapp.editor.parser.Languages;

import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gluonhq.richtextarea.RichTextAreaSkin.KeyMapValue.*;
import static javafx.scene.control.ButtonType.OK;


/**
 * Create window for derivation exercise.
 */
public class DerivationCreate {
    private MainWindow mainWindow;
    private RichTextArea statementRTA;
    private DecoratedRTA statementDRTA;
    private double statementTextHeight;
    private TextField nameField;
    private boolean fieldModified = false;
    private ChangeListener nameListener;
    private ChangeListener leftmostScopeListner;
    private ChangeListener defaultShelfListener;
    private double scale = 1.0;
    private Scene scene;
    private Stage stage;
    private SimpleDoubleProperty centerHeightProperty;
    private TextArea helpArea;
    private VBox centerBox;
    private CheckBox scopeLineCheck;
    private CheckBox defaultShelfCheck;
    private CheckBox italicAndSansCheck;
    private CheckBox scriptAndItalicCheck;
    private CheckBox scriptAndSansCheck;
    private CheckBox italicAndBlackboardCheck;
    private CheckBox greekAndFrakturCheck;
    private ChangeListener italicAndSansListener;
    private ChangeListener scriptAndItalicListener;
    private ChangeListener scriptAndSansListener;
    private ChangeListener italicAndBlackboardListener;
    private ChangeListener greekAndFrakturListener;
    private RichTextAreaSkin.KeyMapValue keyboardSelector = ITALIC_AND_SANS;
    private List<SetupLine> setupLines;
    private GridPane setupLinesPane;
    private Spinner<Double> widthSpinner;
    private ChangeListener defaultWidthListener;
    private Label zoomLabel;
    private Spinner<Integer> zoomSpinner;
    private MenuBar menuBar;
    private BorderPane borderPane;
    private VBox upperFieldsBox;
    private ToolBar editToolbar;
    private ToolBar fontsToolbar;
    private ToolBar sizeToolBar;
    private ToolBar insertToolbar;
    private ToolBar paragraphToolbar;;
    private ToolBar kbdDiaToolBar;
    private Button lowerSaveButton;
    private Button saveAsButton;
    private DecoratedRTA dummyDRTA = new DecoratedRTA();

    private Pane spacerPane;
    private HBox nameBox;
    private Spinner<Double> statementHeightSpinner;
    private Spinner checkMaxSpinner;
    private Spinner helpMaxSpinner;
    private Map<String, SimpleBooleanProperty> ruleMap;
    private Map<String, SimpleBooleanProperty> langMap;
    private Map<EmptyObject, SimpleBooleanProperty> thrmMap1;
    private Map<EmptyObject, SimpleBooleanProperty> thrmMap2;
    private ObservableList<String> theoremSetList;
    private String defaultRulesetName = "\ud835\udc41\ud835\udc37+"; //ND+
    private String defaultLangName = "\u2112\ud835\udcc6 (w/abv)";  //Lq (w/abv)
    private CheckBox showMetalanguageCheck;
    private CheckBox staticHelpCheck;
    private RichTextArea staticHelpRTA;
    private DecoratedRTA staticHelpDRTA;
    private double staticHelpTextHeight;
    private Spinner<Double> staticHelpHeightSpinner;

    private Spinner<Double> verticalSizeSpinner;
    private RichTextArea currentSpinnerNode;



    /**
     * Create new derivation exercise
     * @param mainWindow the main window
     */
    public DerivationCreate(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setupWindow();
    }

    /**
     * Open create window for existing exercise
     * @param mainWindow the main window
     * @param originalModel the model for the exercise to be modified
     */
    public DerivationCreate(MainWindow mainWindow, DerivationModel originalModel) {
        this(mainWindow);
        if (originalModel.getCheckSetup() == null) originalModel.setCheckSetup(new CheckSetup());

        statementRTA.getActionFactory().open(originalModel.getExerciseStatement()).execute(new ActionEvent());
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        nameField.setText(originalModel.getExerciseName());
        statementTextHeight = originalModel.getStatementTextHeight();



        RichTextAreaSkin.KeyMapValue keyboardSelector = originalModel.getKeyboardSelector();
        if (keyboardSelector == ITALIC_AND_SANS) {italicAndSansCheck.setSelected(true); scriptAndItalicCheck.setSelected(false); scriptAndSansCheck.setSelected(false); italicAndBlackboardCheck.setSelected(false); greekAndFrakturCheck.setSelected(false);}
        if (keyboardSelector == SCRIPT_AND_ITALIC) {scriptAndItalicCheck.setSelected(true); italicAndSansCheck.setSelected(false); scriptAndSansCheck.setSelected(false); italicAndBlackboardCheck.setSelected(false); greekAndFrakturCheck.setSelected(false);}

        if (keyboardSelector == SCRIPT_AND_SANS) {italicAndSansCheck.setSelected(false); scriptAndItalicCheck.setSelected(false); scriptAndSansCheck.setSelected(true); italicAndBlackboardCheck.setSelected(false); greekAndFrakturCheck.setSelected(false);}
        if (keyboardSelector == ITALIC_AND_BLACKBOARD) {italicAndSansCheck.setSelected(false); scriptAndItalicCheck.setSelected(false); scriptAndSansCheck.setSelected(false); italicAndBlackboardCheck.setSelected(true); greekAndFrakturCheck.setSelected(false);}
        if (keyboardSelector == GREEK_AND_FRAKTUR) {italicAndSansCheck.setSelected(false); scriptAndItalicCheck.setSelected(false); scriptAndSansCheck.setSelected(false); italicAndBlackboardCheck.setSelected(false); greekAndFrakturCheck.setSelected(true);}

        scopeLineCheck.setSelected(originalModel.isLeftmostScopeLine());
        defaultShelfCheck.setSelected(originalModel.isDefaultShelf());
        widthSpinner.getValueFactory().setValue(((double) Math.round(originalModel.getGridWidth() * 100/2)) * 2);

        CheckSetup checkSetup = originalModel.getCheckSetup();
        showMetalanguageCheck.setSelected(checkSetup.showMetalanguageButton);

        staticHelpCheck.setSelected(checkSetup.staticHelpButton);
        staticHelpRTA.getActionFactory().open(checkSetup.getStaticHelpDoc()).execute(new ActionEvent());
        staticHelpRTA.getActionFactory().saveNow().execute(new ActionEvent());


        checkMaxSpinner.getValueFactory().setValue(checkSetup.getCheckMax());
        helpMaxSpinner.getValueFactory().setValue(checkSetup.getHelpMax());



        for (String key : langMap.keySet()) {
            if (key.equals(checkSetup.getObjLangName())) {
                langMap.get(key).setValue(true);
            }
            else langMap.get(key).setValue(false);
        }

        for (String key : ruleMap.keySet()) {
            if (key.equals(checkSetup.getRulesetName())) {
                ruleMap.get(key).setValue(true);
            }
            else ruleMap.get(key).setValue(false);
        }

        List<SimpleBooleanProperty> thrmList1 = new ArrayList(thrmMap1.values());
        List<SimpleBooleanProperty> thrmList2 = new ArrayList(thrmMap2.values());
        boolean valueSet = false;
        for (int i = 0; i < theoremSetList.size(); i++) {
            for (int j = 0; j < checkSetup.getTheoremSets().size(); j++) {
                Pair<String, Boolean> thrmSetPair = checkSetup.getTheoremSets().get(j);
                if (theoremSetList.get(i).equals(thrmSetPair.getKey())) {
                    thrmList1.get(i).set(true);
                    thrmList2.get(i).set(thrmSetPair.getValue());
                    valueSet = true;
                    break;
                }
            }
            if (!valueSet) {
                thrmList1.get(i).set(false);
                thrmList2.get(i).set(false);
            }
        }

        updateSetupLinesFromModel(originalModel);
        updateGridFromSetupLines();
        fieldModified = false;
    }

    /*
     * Set up the create window
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

        //statement editor
        statementDRTA = new DecoratedRTA();
        statementRTA = statementDRTA.getEditor();
        statementRTA.setPromptText("Exercise Statement:");
        statementRTA.getStylesheets().add("slappTextArea.css");
        statementRTA.setPrefWidth(PrintUtilities.getPageWidth());
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

        //static help editor

        staticHelpDRTA = new DecoratedRTA();
        staticHelpDRTA.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.BASE_AND_SANS);
        staticHelpRTA = staticHelpDRTA.getEditor();
        staticHelpRTA.setPromptText("Static Help (complete if static help selected)");
        staticHelpRTA.getStylesheets().add("slappTextArea.css");
        staticHelpRTA.setPrefWidth(400);
        staticHelpRTA.setMinWidth(400);
        staticHelpRTA.setMaxWidth(400);
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
            fieldModified = true;
            staticHelpTextHeight = mainWindow.getMainView().getRTATextHeight(staticHelpRTA);
        });
        staticHelpRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(staticHelpDRTA, ControlType.AREA);
            }
        });
        staticHelpRTA.getActionFactory().saveNow().execute(new ActionEvent());


        //top fields row

        //name field
        Label nameLabel = new Label("Exercise Name: ");
        nameLabel.setPrefWidth(95);
        nameField = new TextField();
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

        //check boxes
        scopeLineCheck = new CheckBox("Leftmost scope line");
        leftmostScopeListner = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
                scopeLineCheck.selectedProperty().removeListener(leftmostScopeListner);
            }
        };
        scopeLineCheck.setSelected(true);
        scopeLineCheck.selectedProperty().addListener(leftmostScopeListner);

        defaultShelfCheck = new CheckBox("Default shelf");
        defaultShelfListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
                defaultShelfCheck.selectedProperty().removeListener(defaultShelfListener);
            }
        };
        defaultShelfCheck.setSelected(true);
        defaultShelfCheck.selectedProperty().addListener(defaultShelfListener);

        Label keyboardLabel = new Label("Default Derivation Keyboard: ");
        italicAndSansCheck = new CheckBox("Italic and Sans");
        italicAndSansCheck.setSelected(true);
        scriptAndItalicCheck = new CheckBox("Script and Italic");
        scriptAndItalicCheck.setSelected(false);
        scriptAndSansCheck = new CheckBox("Script and Sans");
        scriptAndSansCheck.setSelected(false);
        italicAndBlackboardCheck = new CheckBox("Italic and Blackboard");
        italicAndBlackboardCheck.setSelected(false);
        greekAndFrakturCheck = new CheckBox("Greek and Fraktur");
        greekAndFrakturCheck.setSelected(false);

        italicAndSansListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                boolean selected = (boolean) nv;
                if (selected) {
                    fieldModified = true;
                    keyboardSelector = ITALIC_AND_SANS;
                    updateKeyboard();
                    scriptAndItalicCheck.setSelected(false);
                    scriptAndSansCheck.setSelected(false);
                    italicAndBlackboardCheck.setSelected(false);
                    greekAndFrakturCheck.setSelected(false);
                }
            }
        };
        italicAndSansCheck.selectedProperty().addListener(italicAndSansListener);

        scriptAndItalicListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                boolean selected = (boolean) nv;
                if (selected) {
                    fieldModified = true;
                    keyboardSelector = SCRIPT_AND_ITALIC;
                    updateKeyboard();
                    italicAndSansCheck.setSelected(false);
                    scriptAndSansCheck.setSelected(false);
                    italicAndBlackboardCheck.setSelected(false);
                    greekAndFrakturCheck.setSelected(false);
                }
            }
        };
        scriptAndItalicCheck.selectedProperty().addListener(scriptAndItalicListener);

        scriptAndSansListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                boolean selected = (boolean) nv;
                if (selected) {
                    fieldModified = true;
                    keyboardSelector = SCRIPT_AND_SANS;
                    updateKeyboard();
                    italicAndSansCheck.setSelected(false);
                    scriptAndItalicCheck.setSelected(false);
                    italicAndBlackboardCheck.setSelected(false);
                    greekAndFrakturCheck.setSelected(false);
                }
            }
        };
        scriptAndSansCheck.selectedProperty().addListener(scriptAndSansListener);

        italicAndBlackboardListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                boolean selected = (boolean) nv;
                if (selected) {
                    fieldModified = true;
                    keyboardSelector = ITALIC_AND_BLACKBOARD;
                    updateKeyboard();
                    italicAndSansCheck.setSelected(false);
                    scriptAndItalicCheck.setSelected(false);
                    scriptAndSansCheck.setSelected(false);
                    greekAndFrakturCheck.setSelected(false);
                }
            }
        };
        italicAndBlackboardCheck.selectedProperty().addListener(italicAndBlackboardListener);

        greekAndFrakturListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                boolean selected = (boolean) nv;
                if (selected) {
                    fieldModified = true;
                    keyboardSelector = GREEK_AND_FRAKTUR;
                    updateKeyboard();
                    italicAndSansCheck.setSelected(false);
                    scriptAndItalicCheck.setSelected(false);
                    scriptAndSansCheck.setSelected(false);
                    italicAndBlackboardCheck.setSelected(false);
                }
            }
        };
        greekAndFrakturCheck.selectedProperty().addListener(greekAndFrakturListener);


        widthSpinner = new Spinner<>(64.0, 100, 0, 2);
        defaultWidthListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
                widthSpinner.valueProperty().removeListener(defaultWidthListener);
            }
        };
        widthSpinner.valueProperty().addListener(defaultWidthListener);
        widthSpinner.setPrefWidth(65);


        //check controls

        checkMaxSpinner = new Spinner<>(-1, 99, 0);
        checkMaxSpinner.setPrefWidth(60);
        Label checkMaxLabel = new Label("Check Max:");

        helpMaxSpinner = new Spinner<>(-1, 99, 0);
        helpMaxSpinner.setPrefWidth(60);
        Label helpMaxLabel = new Label("   Help Max:");

        //language list view
        Label languageLabel = new Label("   Language:");
        langMap = new LinkedHashMap<>();
        for (int i = 0; i < Languages.getFixedLanguages().size() - 1; i++) {
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

        //ruleset listview
        Label rulesetLabel = new Label("   Rule Set:");
        ruleMap = new LinkedHashMap<>();
        for (int i = 0; i < DerivationRulesets.getRulesets().size(); i++) {
            DerivationRuleset ruleset = DerivationRulesets.getRulesets().get(i);
            SimpleBooleanProperty booleanProperty = new SimpleBooleanProperty(false);
            if (ruleset.getName().equals(defaultRulesetName)) booleanProperty.set(true);  //default check
            setSingleSelection(booleanProperty, ruleMap);
            ruleMap.put(ruleset.getName(), booleanProperty);
        }

        ListView<String> rulesetListView = new ListView<>();
        rulesetListView.setPrefSize(120, 95);

        rulesetListView.getItems().addAll(ruleMap.keySet());
        Callback<String, ObservableValue<Boolean>> rulesetToBoolean = (String item) -> ruleMap.get(item);
        rulesetListView.setCellFactory(CheckBoxListCell.forListView(rulesetToBoolean));

        //empty list view 1
        Label theoremSetLabel = new Label("   Theorem Set(s):");
        thrmMap1 = new LinkedHashMap<>();
        for (int i = 0; i < TheoremSets.getTheoremSets().size(); i++) {
            EmptyObject emptyObject = new EmptyObject();
            SimpleBooleanProperty booleanProperty = new SimpleBooleanProperty(false);
            setModificationListener(booleanProperty);
            thrmMap1.put(emptyObject, booleanProperty);
        }
        ListView<EmptyObject> thrmSet1ListView = new ListView<>();
        thrmSet1ListView.setPrefSize(45, 95);

        thrmSet1ListView.getItems().addAll(thrmMap1.keySet());
        Callback<EmptyObject, ObservableValue<Boolean>> thrmSet1ToBoolean = (EmptyObject item) -> thrmMap1.get(item);
        thrmSet1ListView.setCellFactory(CheckBoxListCell.forListView(thrmSet1ToBoolean));
        thrmSet1ListView.getStylesheets().add("EmptyListView.css");
        thrmSet1ListView.setStyle("-fx-background-color: transparent");


        //empty list view 2
        thrmMap2 = new LinkedHashMap<>();
        for (int i = 0; i < TheoremSets.getTheoremSets().size(); i++) {
            EmptyObject emptyObject = new EmptyObject();
            SimpleBooleanProperty booleanProperty = new SimpleBooleanProperty(false);
     //       setModificationListener(booleanProperty);
            setSingleSelection2(booleanProperty, thrmMap2);
            thrmMap2.put(emptyObject, booleanProperty);
        }
        ListView<EmptyObject> thrmSet2ListView = new ListView<>();
        thrmSet2ListView.setPrefSize(45, 95);

        thrmSet2ListView.getItems().addAll(thrmMap2.keySet());
        Callback<EmptyObject, ObservableValue<Boolean>> thrmSet2ToBoolean = (EmptyObject item) -> thrmMap2.get(item);
        thrmSet2ListView.setCellFactory(CheckBoxListCell.forListView(thrmSet2ToBoolean));
        thrmSet2ListView.getStylesheets().add("EmptyListView.css");
        thrmSet2ListView.setStyle("-fx-background-color: transparent");

       theoremSetList = FXCollections.observableArrayList();
        for (TheoremSet theoremSet : TheoremSets.getTheoremSets()) {
            theoremSetList.add(theoremSet.toString());
        }
        ListView<String> thrmListView = new ListView<>(theoremSetList);
        thrmListView.setPrefSize(120, 95);
        thrmListView.setStyle("-fx-background-color: transparent");

        thrmListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setGraphic(null);
                    setOnMouseClicked(mouseClickedEvent -> {
                        if (mouseClickedEvent.getButton().equals(MouseButton.PRIMARY) && mouseClickedEvent.getClickCount() == 2) {
                            System.out.println(item);
                        }
                    });
                }
            }
        });


        HBox thrmBox = new HBox(thrmSet1ListView, thrmSet2ListView, thrmListView);
        thrmBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1");

        HBox checkerBox = new HBox(10, checkMaxLabel, checkMaxSpinner, helpMaxLabel, helpMaxSpinner, languageLabel, languageListView, rulesetLabel, rulesetListView,  theoremSetLabel, thrmBox);
        checkerBox.setMargin(thrmSet2ListView, new Insets(0, 0, 0, -10));
        checkerBox.setMargin(thrmListView, new Insets(0, 0, 0, -10));

        checkerBox.setAlignment(Pos.CENTER_LEFT);


        //////////////////

        showMetalanguageCheck = new CheckBox();
        showMetalanguageCheck.setSelected(false);

        ChangeListener metalanguageCheckListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
            }
        };
        showMetalanguageCheck.selectedProperty().addListener(metalanguageCheckListener);
        Label metalangCheckLabel = new Label("Show \u2133\u2112 Button:");


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


        //setup lines control
        Label setupLinesLabel = new Label("Setup Lines: ");
        setupLinesLabel.setPrefWidth(75);
        Button addSetupLineButton = new Button("+");
        Button removeSetupLineButton = new Button("-");
        addSetupLineButton.setFont(new Font(16));
        addSetupLineButton.setPadding(new Insets(0, 5, 0, 5));
        removeSetupLineButton.setFont(new Font(16));
        removeSetupLineButton.setPadding(new Insets(1, 8, 1, 8));

        addSetupLineButton.setOnAction(e -> {
            SetupLine newLine = new SetupLine(this);

            setupLines.add(new SetupLine(this));
            fieldModified = true;
            updateGridFromSetupLines();
        });
        removeSetupLineButton.setOnAction(e -> {
            int index = setupLines.size();
            index--;
            if (index > 0) {
                setupLines.remove(index);
                fieldModified = true;
                updateGridFromSetupLines();
            } else {
                EditorAlerts.showSimpleAlert("Cannot Remove", "A derivation must include at least one setup line.");
            }
        });

        nameBox = new HBox(10, nameLabel, nameField);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        HBox keyboardBox = new HBox(10, keyboardLabel, italicAndSansCheck, scriptAndItalicCheck, scriptAndSansCheck, italicAndBlackboardCheck, greekAndFrakturCheck);

        Label widthLabel = new Label("Width: ");
        HBox topFields = new HBox(30, scopeLineCheck, defaultShelfCheck, widthLabel, widthSpinner);
        topFields.setAlignment(Pos.CENTER_LEFT);
        topFields.setMargin(widthLabel, new Insets(0, -10, 0, 0));


        HBox setupLineButtons = new HBox(30, metalangCheckLabel, showMetalanguageCheck, staticHelpCheckLabel, staticHelpCheck, setupLinesLabel, addSetupLineButton, removeSetupLineButton);
        setupLineButtons.setAlignment(Pos.CENTER_LEFT);
        setupLineButtons.setMargin(showMetalanguageCheck, new Insets(0, 20, 0, -15));
        setupLineButtons.setMargin(staticHelpCheck, new Insets(0, 20, 0, -15));
        setupLineButtons.setMargin(setupLinesLabel, new Insets(0, -20, 0, 10));

        //setup lines pane
        setupLines = new ArrayList<>();

        SetupLine firstLine = new SetupLine(this);
        RichTextArea firstLineFormulaRTA = firstLine.getFormulaBoxedDRTA().getRTA();
        firstLineFormulaRTA.getActionFactory().saveNow().execute(new ActionEvent());


        RichTextArea firstLineJustificationRTA = firstLine.getJustificationBoxedDRTA().getRTA();
        firstLineJustificationRTA.getActionFactory().saveNow().execute(new ActionEvent());

        setupLines.add(firstLine);
        setupLinesPane = new GridPane();
        setupLinesPane.setPadding(new Insets(5, 15, 10, 0));
        setupLinesPane.setHgap(15);
        setupLinesPane.setVgap(15);
        updateGridFromSetupLines();


        upperFieldsBox = new VBox(15, nameBox, keyboardBox, topFields, checkerBox, setupLineButtons, setupLinesPane);

        upperFieldsBox.setPadding(new Insets(20, 0, 20, 0));
        //   upperFieldsBox.setPadding(new Insets(20,0,20,20));

        String helpText = "<body style=\"margin-left:10; margin-right: 20\">" +
                "<p>Derivation Exercise is appropriate for any exercise that calls for a derivation as response.<p>" +
                "<ul>" +

                "<li><p>Supply the exercise name.  The name for a theorem exercise should end with underscore and then the name (as '_T3.14') as the check feature uses the name to identify 'prior' theorems available in a derivation.  Then select the default keyboard for derivation content lines, and select whether there is to be a leftmost scope line, and/or a \"shelf\" beneath the top line of an automatically generated subderivation. " +
                "A typical natural derivation system (as chapter 6 of <em>Symbolic Logic</em>) selects 'italic and sans', 'leftmost scope line' and 'default shelf'.  The width is the (default) percentage of the window's width allocated to this derivation.</p></li>" +
                "<li><p>For the check and help functions: A 'max value' of -1 corresponds to 'unlimited', 0 to 'none', and otherwise to the maximum number of check or help tries allotted for the exercise.  " +
                "Then there are checkboxes to select the formal language and ruleset.  Notice that a given keyboard selection may be compatible with multiple formal languages, but a given formal language will typically have some preferred default keyboard. </p>" +
                "<p>The Theorem Set option is relevant only to exercises which appeal to axiom or theorem lists.  Multiple selections are possible, though behavior is unpredictable in case members of selected sets have the same name. " +
                "The first checkbox selects the set; the second whether its members are \"cumulative\" -- in the sense that one member depends just on ones before. (Ordinarily, then, an axiom set leaves the second box unchecked, but exercises that generate sequenced theorems have the second check.) " +
                "</p>" +
                "<p> The Show \u2133\u2112 check inserts a metalanguage help button into the lower left corner of the exercise statement (and so is relevant only to derivations conducted in the metalanguage).  " +
                "The static help check activates the Static Help button which pops up a message which you may state in the right hand text area below." +
                "</p>" +
                "</li>" +
                "<li><p>After that, insert setup derivation lines as appropriate.  In the ordinary case, setup lines will include some premise lines with justification 'P' (the last sitting on a shelf), a blank line, and a conclusion line (without justification), all at scope depth 1. " +
                "A line identified as a premise cannot have either its formula or justification modified; one identified as a conclusion cannot have its formula modified.  Different arrangements (as, e.g. \"fill in the justification\" exercises) are possible.</p></li>" +
                "<li><p>Finally, fill in the exercise statement.</p></li>" +
                "</ul>";

        WebView helpArea = new WebView();
        WebEngine webEngine = helpArea.getEngine();
        webEngine.setUserStyleSheetLocation("data:, body {font: 14px Noto Serif Combo; }");
        webEngine.loadContent(helpText);
        helpArea.setPrefHeight(230);


        HBox textEntryRow = new HBox(20, statementRTA, staticHelpRTA);


        centerBox = new VBox(10, upperFieldsBox, textEntryRow, helpArea);
        centerBox.setPadding(new Insets(10, 0, 10, 20));

  //      centerBox.setVgrow(statementRTA, Priority.ALWAYS);
   //     centerBox.setMaxWidth(PrintUtilities.getPageWidth() + 20);

        spacerPane = new Pane();
        spacerPane.prefHeightProperty().bind(centerBox.heightProperty());
        spacerPane.prefWidthProperty().bind(centerBox.widthProperty());
        Group group = new Group(spacerPane);
        AnchorPane comboPane = new AnchorPane(group, centerBox);
        ScrollPane centerPane = new ScrollPane(comboPane);

    //    centerPane.setPadding(new Insets(0,50,0,50));
 //       centerPane.setMaxWidth(PrintUtilities.getPageWidth() + 100);

        borderPane.setCenter(centerPane);

//        Group centerGroup = new Group(centerBox);
//        borderPane.setCenter(centerGroup);

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

        scene = new Scene(borderPane);
        scene.getStylesheets().add(DecoratedRTA.class.getClassLoader().getResource("slappEditor.css").toExternalForm());

        zoomLabel = new Label(" Zoom ");
        zoomSpinner = new Spinner(25, 500, 100, 5);
        zoomSpinner.setPrefSize(65,25);
        zoomSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = zoomSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = zoomSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);

            scale = (double) nv/100;
            updateZoom();
 //          setCenterVgrow();
        });



        sizeToolBar = new ToolBar();
        sizeToolBar.setPrefHeight(38);
        sizeToolBar.getItems().addAll(zoomLabel, zoomSpinner, new Label("     V Sz:"), verticalSizeSpinner);

        setSizeSpinners();

        stage = new Stage();
        stage.initOwner(EditorMain.mainStage);
        stage.setScene(scene);

        stage.setTitle("Create Derivation Exercise:");
        stage.getIcons().addAll(EditorMain.icons);
        stage.setWidth(1035);
        stage.setHeight(950);

        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        stage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 1035));
        stage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 900));

        stage.initModality(Modality.NONE);
        stage.setOnCloseRequest(e-> {
            e.consume();
            closeWindow();
        });

        stage.show();
        statementRTA.getActionFactory().save().execute(new ActionEvent());
        centerBox.layout();

        //lookup works only after showing the stage / scene
        Node n1 = thrmSet1ListView.lookup(".scroll-bar");
        if (n1 instanceof ScrollBar) {
            ScrollBar scrollBar1 = (ScrollBar) n1;
            Node n2 = thrmSet2ListView.lookup(".scroll-bar");
            if (n2 instanceof ScrollBar) {
                ScrollBar scrollBar2 = (ScrollBar) n2;
                Node n3 = thrmListView.lookup(".scroll-bar");
                if (n3 instanceof ScrollBar) {
                    ScrollBar scrollBar3 = (ScrollBar) n3;
                    scrollBar1.valueProperty().bindBidirectional(scrollBar2.valueProperty());
                    scrollBar2.valueProperty().bindBidirectional(scrollBar3.valueProperty());
                }
            }
        }
        ////

        Platform.runLater(() -> nameField.requestFocus());
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

    private void setSingleSelection2(SimpleBooleanProperty booleanProperty, Map<EmptyObject, SimpleBooleanProperty> map) {
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


    private void setModificationListener(SimpleBooleanProperty booleanProperty) {
        booleanProperty.addListener((ob, ov, nv) -> {
            if (nv.booleanValue() != ov.booleanValue()) fieldModified = true;
        });
    }

    /*
     * Update list of setup lines from derivation model
     * @param originalModel the derivation model
     */
    private void updateSetupLinesFromModel(DerivationModel originalModel) {

        List<ModelLine> modelLines = originalModel.getDerivationLines();
        setupLines.clear();
        int i = 0;
        while (i < modelLines.size()) {
            ModelLine modelLine = modelLines.get(i);
            if (LineType.isContentLine(modelLine.getLineType())) {
                SetupLine setupLine = new SetupLine(this);

                BoxedDRTA formulaBoxedDRTA = setupLine.getFormulaBoxedDRTA();
                RichTextArea formulaRTA = formulaBoxedDRTA.getRTA();
                formulaRTA.getActionFactory().open(modelLine.getLineContentDoc()).execute(new ActionEvent());
                formulaRTA.getActionFactory().saveNow().execute(new ActionEvent());

                BoxedDRTA justificationBoxedDRTA = setupLine.getJustificationBoxedDRTA();
                RichTextArea justificationRTA = justificationBoxedDRTA.getRTA();
                justificationRTA.getActionFactory().open(new Document(modelLine.getJustification())).execute(new ActionEvent());
                justificationRTA.getActionFactory().saveNow().execute(new ActionEvent());

                setupLine.getDepthSpinner().getValueFactory().setValue(modelLine.getDepth());
                setupLine.getPremiseBox().setSelected(modelLine.getLineType() == LineType.PREMISE_LINE);
                setupLine.getConclusionBox().setSelected(modelLine.getLineType() == LineType.CONCLUSION_LINE);

                i++;
                if (i < modelLines.size()) {
                    ModelLine nextLine = modelLines.get(i);
                    if (LineType.isShelfLine(nextLine.getLineType())) {
                        setupLine.getAddShelfBox().setSelected(true);
                        i++;
                    } else if (LineType.isGapLine(nextLine.getLineType())) {
                        setupLine.getAddGapBox().setSelected(true);
                        i++;
                    }
                }
                setupLine.setModified(false);
                setupLines.add(setupLine);
            }
        }
    }

    /*
     * Update gridpane from setup lines list
     */
    private void updateGridFromSetupLines() {
        setupLinesPane.getChildren().clear();
        for (int i = 0; i < setupLines.size(); i++) {
            SetupLine setupLine = setupLines.get(i);
            BoxedDRTA formulaBoxedDRTA = setupLine.getFormulaBoxedDRTA();
            BoxedDRTA justificationBoxedDRTA = setupLine.getJustificationBoxedDRTA();
            setupLinesPane.addRow(i,
                    formulaBoxedDRTA.getBoxedRTA(),
                    justificationBoxedDRTA.getBoxedRTA(),
                    setupLine.getSpinnerBox(),
                    setupLine.getPremiseBox(),
                    setupLine.getConclusionBox(),
                    setupLine.getAddShelfBox(),
                    setupLine.getAddGapBox()
            );
        }
    }

    /*
     * Update default keyboard for grid RTAs
     */
    private void updateKeyboard() {
        for (int i = 0; i < setupLines.size(); i++) {
            SetupLine setupLine = setupLines.get(i);
            BoxedDRTA formulaBoxedDRTA = setupLine.getFormulaBoxedDRTA();
            formulaBoxedDRTA.getDRTA().getKeyboardSelector().valueProperty().setValue(keyboardSelector);
        }
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
     * Check for changes and close
     */
    private void closeWindow() {
        if (checkContinue("Confirm Close", "This exercise appears to have unsaved changes.\nContinue to close window?")) {
            mainWindow.closeExercise();
            stage.close();
        }
    }

    /*
     * Clear create window, leaving keyboard, scope, and shelf settings intact.
     */
    private void clearExercise() {
        if (checkContinue("Confirm Clear", "This exercise appears to have unsaved changes.\nContinue to clear exercise?")) {
            nameField.clear();
            nameField.textProperty().addListener(nameListener);
            widthSpinner.getValueFactory().setValue(0.0);

            setupLines.clear();
            SetupLine firstLine = new SetupLine(this);
            firstLine.getFormulaBoxedDRTA().getRTA().getActionFactory().saveNow().execute(new ActionEvent());
            firstLine.getJustificationBoxedDRTA().getRTA().getActionFactory().saveNow().execute(new ActionEvent());
            setupLines.add(firstLine);
            updateGridFromSetupLines();
            statementRTA.getActionFactory().newDocumentNow().execute(new ActionEvent());
            staticHelpRTA.getActionFactory().newDocumentNow().execute(new ActionEvent());
            fieldModified = false;
            viewExercise();
        }
    }

    /*
     * If exercise modified, check for continue
     * @param title String title of confirmation box
     * @param content String content of confirmation box
     * @return true if ok to continue, and otherwise false
     */
    private boolean checkContinue(String title, String content) {
        boolean okcontinue = true;
        for (SetupLine line : setupLines) {
            if (line.isModified()) fieldModified = true;
        }
        if (fieldModified || statementRTA.isModified() || staticHelpRTA.isModified()) {
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
        DerivationModel model = extractModelFromWindow();
        DerivationExercise exercise = new DerivationExercise(model, mainWindow);
        RichTextArea rta = exercise.getExerciseView().getExerciseStatement().getEditor();
        rta.setEditable(false);
        rta.prefHeightProperty().unbind();
        exercise.getExerciseView().setStatementPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getStatementPrefHeight()));

        mainWindow.setUpExercise(exercise);
    }

    /*
     * Save the currently constructed exercise to disk
     * @param saveAs true to open save as window and otherwise false
     */
    private void saveExercise(boolean saveAs) {
        lowerSaveButton.setDisable(true);
        saveAsButton.setDisable(true);

        nameField.textProperty().addListener(nameListener);
        scopeLineCheck.selectedProperty().addListener(leftmostScopeListner);
        defaultShelfCheck.selectedProperty().addListener(defaultShelfListener);

        DerivationModel model = extractModelFromWindow();

        boolean success = DiskUtilities.saveExercise(saveAs, model);
        if (success) {
            fieldModified = false;
            for (SetupLine line : setupLines) { line.setModified(false); }
        }

        lowerSaveButton.setDisable(false);
        saveAsButton.setDisable(false);

    }






    /*
     * Get the derivation model for the currently constructed exercise
     * @return the derivation model
     */
    private DerivationModel extractModelFromWindow() {
        String name = nameField.getText();

        boolean leftmostScope = scopeLineCheck.isSelected();
        boolean defaultShelf = defaultShelfCheck.isSelected();
        double gridWidth = widthSpinner.getValue()/100.0;

        if (statementRTA.isModified()) fieldModified = true;
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document statementDocument = statementRTA.getDocument();
        double statementPrefHeight = statementTextHeight + 25;
        Document commentDocument = new Document();

        if (staticHelpRTA.isModified()) fieldModified = true;
        staticHelpRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document staticHelpDocument = staticHelpRTA.getDocument();
        double staticHelpPrefHeight = staticHelpTextHeight + 25;


        List<ModelLine> modelLines = new ArrayList<>();
        for (int i = 0; i < setupLines.size(); i++) {
            SetupLine setupLine = setupLines.get(i);
            if (setupLine.isModified()) fieldModified = true;

            int depth = (Integer) setupLine.getDepthSpinner().getValue();

            RichTextArea formulaRTA = setupLine.getFormulaBoxedDRTA().getRTA();
            formulaRTA.getActionFactory().saveNow().execute(new ActionEvent());
            Document formulaDocument = formulaRTA.getDocument();

            RichTextArea justificationRTA = setupLine.getJustificationBoxedDRTA().getRTA();
            justificationRTA.getActionFactory().saveNow().execute(new ActionEvent());
            String justificationString = justificationRTA.getDocument().getText();

            LineType lineType = LineType.MAIN_CONTENT_LINE;
            if (setupLine.getPremiseBox().isSelected()) lineType = LineType.PREMISE_LINE;
            else if (setupLine.getConclusionBox().isSelected()) lineType = LineType.CONCLUSION_LINE;

            ModelLine modelLine = new ModelLine(depth, formulaDocument, justificationString, lineType );
            modelLines.add(modelLine);

           if (modelLine.getLineType() == LineType.PREMISE_LINE && setupLine.getAddShelfBox().isSelected()) {
                modelLines.add(new ModelLine(depth, null, "", LineType.SETUP_SHELF_LINE ));
            } else if (setupLine.getAddShelfBox().isSelected()) {
               modelLines.add(new ModelLine(depth, null, "", LineType.SHELF_LINE));
           } else if (setupLine.getAddGapBox().isSelected()) {
               modelLines.add(new ModelLine(depth, null, "", LineType.GAP_LINE));
           }
        }

        DerivationModel model = new DerivationModel(name, false, statementPrefHeight, gridWidth, leftmostScope, defaultShelf, keyboardSelector, statementDocument, commentDocument, modelLines);
        model.setStatementTextHeight(statementTextHeight);

        //
        CheckSetup checkSetup = model.getCheckSetup();
        checkSetup.setCheckMax((Integer) checkMaxSpinner.getValue());
        checkSetup.setHelpMax((Integer) helpMaxSpinner.getValue());
        checkSetup.setShowMetalanguageButton(showMetalanguageCheck.isSelected());
        checkSetup.setStaticHelpButton(staticHelpCheck.isSelected());
        checkSetup.setStaticHelpDoc(staticHelpDocument);

        String rulesetName = null;
        for (String key : ruleMap.keySet()) {
            ObservableValue<Boolean> value = ruleMap.get(key);
            if (value.getValue()) {
                rulesetName = key;
                break;
            }
        }
        if (rulesetName != null) {
            checkSetup.setRulesetName(rulesetName);
        }
        else {
            EditorAlerts.showSimpleAlert("No ruleset selected:", "Without a ruleset selection, SLAPP will revert to the default, \ud835\udc41\ud835\udc37+.");
        }

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

        checkSetup.getTheoremSets().clear();
        List<SimpleBooleanProperty> thrmList1 = new ArrayList(thrmMap1.values());
        List<SimpleBooleanProperty> thrmList2 = new ArrayList(thrmMap2.values());
        for (int i = 0; i < theoremSetList.size(); i++) {
            if (thrmList1.get(i).getValue()) {
                checkSetup.getTheoremSets().add(new Pair(theoremSetList.get(i), thrmList2.get(i).getValue()));
            }
        }


        //was trying a serious filter, but too many options (as for A*).  So just look for non-empty part after underscore.
        Pattern pattern = Pattern.compile("^.+_.+$");
        Matcher matcher = pattern.matcher(name);
        boolean goodThrmName = matcher.find();
     //   System.out.println(goodThrmName);

        List<ThrmSetElement> modelThrmSetElements = new ArrayList();
        for (int i = 0; i < theoremSetList.size(); i++) {
            if (thrmList1.get(i).getValue()) {
                TheoremSet theoremSet = TheoremSets.getTheoremSet(theoremSetList.get(i));
                List<ThrmSetElement> thrmSetElements = theoremSet.getElements();
                for (int j = 0; j < thrmSetElements.size(); j++ ) {

                    ThrmSetElement thrmSetElement = thrmSetElements.get(j);

                    boolean numberOK = false;
                    if (goodThrmName) {
                        int lastIndex = name.lastIndexOf("_");
                        String thisThrmName = name.substring(lastIndex + 1);
                        numberOK = thrmSetElement.isPriorTo(thisThrmName);
                    }
                    if (!thrmList2.get(i).getValue() || (thrmList2.get(i).getValue() && numberOK)) {
                        modelThrmSetElements.add(thrmSetElement);
                    }
                }
            }
        }
        checkSetup.setThrmSetElements(modelThrmSetElements);



        if (checkSetup.getHelpMax() != 0 && (!DerivationRulesets.getRuleset(checkSetup.getRulesetName()).isContextualHelpCompatible()  || !checkSetup.getTheoremSets().isEmpty())) {
            checkSetup.setHelpMax(0);
            helpMaxSpinner.getValueFactory().setValue(0);
            String optionalStr = "";
            if (!checkSetup.getTheoremSets().isEmpty()) optionalStr = " (with selected theorem sets)";
            EditorAlerts.fleetingRedPopup(checkSetup.getRulesetName() + optionalStr + " is not compatible with contextual help.\n\nHelp Max reset to zero.");
        }

/*
        for (ThrmSetElement thrmSetElement : modelThrmSetElements) {
            System.out.println(thrmSetElement.getName());
        }

 */


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
     * Update keyboard diagram and edit controls to currently selected DRTA
     * @param decoratedRTA the DRTA
     * @param control the {slapp.editor.main_window.ControlType}
     */
    void editorInFocus(DecoratedRTA decoratedRTA, ControlType control) {

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


        //VBox topBox = new VBox(menuBar, paragraphToolbar, fontsAndEditBox, kbdBox, upperFieldsBox);
        VBox topBox = new VBox(menuBar, paragraphToolbar, fontsAndEditBox, kbdBox);

        borderPane.topProperty().setValue(topBox);
    }

    /*
     * With text field in focus, set disabled DRTA controls
     */
    private void textFieldInFocus() {
        editorInFocus(dummyDRTA, ControlType.STATEMENT);
    }

    /**
     * The selected default {@link com.gluonhq.richtextarea.RichTextAreaSkin.KeyMapValue}
     * @return
     */
    public RichTextAreaSkin.KeyMapValue getKeyboardSelector() {return keyboardSelector;}

}
