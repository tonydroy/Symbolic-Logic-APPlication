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

package slapp.editor.truth_table_explain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Scale;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import slapp.editor.EditorAlerts;
import slapp.editor.EditorMain;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.decorated_rta.KeyboardDiagram;
import slapp.editor.main_window.ControlType;
import slapp.editor.main_window.MainWindow;
import slapp.editor.main_window.MainWindowView;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static javafx.scene.control.ButtonType.OK;

/**
 * Create window for the truth table explain exercise
 */
public class TruthTableExpCreate {
    private MainWindow mainWindow;
    private TextField nameField;
    private TextField explainPromptField;
    private RichTextArea statementRTA;
    private DecoratedRTA statementDRTA;
    private double statementTextHeight;
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
    private List<BoxedDRTA> unaryOperatorList;
    private List<BoxedDRTA> binaryOperatorList;
    private List<BoxedDRTA> mainFormulaList;
    private GridPane unaryOperatorsPane;
    private GridPane binaryOperatorsPane;
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
    private ChangeListener explainPromptListener;
    private double formulaBoxHeight = 27;
    private Button lowerSaveButton;
    private Button saveAsButton;
    private BoxedDRTA focusedBoxedDRTA;
    private DecoratedRTA dummyDRTA = new DecoratedRTA();

    private Pane spacerPane;
    private Spinner<Double> statementHeightSpinner;
    private TextField pointsPossibleTextField;

    /**
     * Create new truth table explain exercise
     * @param mainWindow the main window
     */
    public TruthTableExpCreate(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setupWindow();
    }

    /**
     * Create window for existing exercise
     * @param mainWindow the main window
     * @param originalModel the model for the exercise to be modified
     */
    public TruthTableExpCreate(MainWindow mainWindow, TruthTableExpModel originalModel) {
        this(mainWindow);

        nameField.setText(originalModel.getExerciseName());
        explainPromptField.setText(originalModel.getExplainPrompt());
        statementRTA.getActionFactory().open(originalModel.getExerciseStatement()).execute(new ActionEvent());
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        statementTextHeight = originalModel.getStatementTextHeight();
        choiceLeadField.setText(originalModel.getChoiceLead());
        aPromptField.setText(originalModel.getaPrompt());
        bPromptField.setText(originalModel.getbPrompt());
        conclusionDividerCheck.setSelected(originalModel.isConclusionDivider());
        pointsPossibleTextField.setText(Integer.toString(originalModel.getPointsPossible()));
        updateOperatorFieldsFromModel(originalModel);
        updateUnaryOperatorGridFromFields();
        updateBinaryOperatorGridFromFields();
        updateMainFormulaFieldsFromModel(originalModel);
        updateMainFormulaGridFromFields();

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

        //name field
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

        Label explainPromptLabel = new Label("Explain Prompt: ");
        explainPromptLabel.setAlignment(Pos.CENTER_RIGHT);
        explainPromptLabel.setPrefWidth(95);
        explainPromptField  = new TextField();


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

        HBox nameBox = new HBox(10, nameLabel, nameField, explainPromptLabel, explainPromptField, pointsPossibleLabel, pointsPossibleTextField);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        //choice fields
        Label choiceLeadLabel = new Label("Checkbox lead: ");
        choiceLeadLabel.setPrefWidth(95);
        choiceLeadField  = new TextField();
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
        aPromptField  = new TextField();
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
        bPromptField  = new TextField();
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

        //language presets
        Label operatorPresetLabel = new Label("Preset operators: ");
        Button baseLangPresetButton = new Button();
        Button plusLangPresetButton = new Button();
        baseLangPresetButton.setPrefHeight(25); baseLangPresetButton.setPrefWidth(45);
        plusLangPresetButton.setPrefHeight(25); plusLangPresetButton.setPrefWidth(45);

        Font flowFont = new Font("Noto Serif Combo", 14);
        Text t0 = new Text("\u2112");
        Text t1 = new Text("\u2112");
        Text t2 = new Text("\ud835\udcc8");
        Text t3 = new Text("\ud835\udcc8+");
        t0.setFont(flowFont); t1.setFont(flowFont); t2.setFont(flowFont); t3.setFont(flowFont);

        t2.setTranslateY(t2.getFont().getSize() * 0.2);
        t2.setTranslateX(-t2.getFont().getSize()*0.1);
        t3.setTranslateY(t3.getFont().getSize() * 0.2);
        t3.setTranslateX(-t3.getFont().getSize()*0.1);
        TextFlow baseFlow = new TextFlow();
        baseFlow.getChildren().addAll(t0,t2);
        baseFlow.setTextAlignment(TextAlignment.CENTER);
        TextFlow plusFlow = new TextFlow();
        plusFlow.getChildren().addAll(t1,t3);
        plusFlow.setTextAlignment(TextAlignment.CENTER);
        baseLangPresetButton.setGraphic(baseFlow);
        plusLangPresetButton.setGraphic(plusFlow);

        HBox languagePresetsBox = new HBox(10, operatorPresetLabel, baseLangPresetButton, plusLangPresetButton);
        languagePresetsBox.setAlignment(Pos.CENTER_LEFT);

        baseLangPresetButton.setOnAction(e -> {
           unaryOperatorList.clear();
           unaryOperatorList.add(contentOperatorBoxedDRTA("\u223c"));
           updateUnaryOperatorGridFromFields();
           binaryOperatorList.clear();
           binaryOperatorList.add(contentOperatorBoxedDRTA("\u2192"));
           updateBinaryOperatorGridFromFields();
           fieldModified = true;
        });
        plusLangPresetButton.setOnAction(e -> {
            unaryOperatorList.clear();
            unaryOperatorList.add(contentOperatorBoxedDRTA("\u223c"));
            updateUnaryOperatorGridFromFields();
            binaryOperatorList.clear();
            binaryOperatorList.add(contentOperatorBoxedDRTA("\u2192"));
            binaryOperatorList.add(contentOperatorBoxedDRTA("\u2194"));
            binaryOperatorList.add(contentOperatorBoxedDRTA("\u2227"));
            binaryOperatorList.add(contentOperatorBoxedDRTA("\u2228"));
            updateBinaryOperatorGridFromFields();
            fieldModified = true;
        });

        //unary operators
        unaryOperatorsPane = new GridPane();
        unaryOperatorsPane.setHgap(10);
        unaryOperatorList = new ArrayList<>();

        Label unaryOperatorLabel = new Label("Unary operators: ");
        unaryOperatorLabel.setPrefWidth(95);
        Button addUnaryOperatorButton = new Button("+");
        Button removeUnaryOperatorButton = new Button("-");
        addUnaryOperatorButton.setFont(new Font(16)); removeUnaryOperatorButton.setFont(new Font(16));
        addUnaryOperatorButton.setPadding(new Insets(0, 5, 0, 5)); removeUnaryOperatorButton.setPadding(new Insets(1, 8, 1, 8));

        addUnaryOperatorButton.setOnAction(e -> {
            BoxedDRTA bdrta = newOperatorBoxedDRTA();
            unaryOperatorList.add(bdrta);
            fieldModified = true;
            updateUnaryOperatorGridFromFields();
        });
        removeUnaryOperatorButton.setOnAction(e -> {
            int index = unaryOperatorList.size();
            index--;
            if (index >= 0) {
                unaryOperatorList.remove(index);
                fieldModified = true;
                updateUnaryOperatorGridFromFields();
            }
        });

        HBox unaryOperatorBox = new HBox(10, unaryOperatorLabel, addUnaryOperatorButton, removeUnaryOperatorButton, unaryOperatorsPane);
        unaryOperatorBox.setAlignment(Pos.CENTER_LEFT);

        //binary operators
        binaryOperatorsPane = new GridPane();
        binaryOperatorsPane.setHgap(10);
        binaryOperatorList = new ArrayList<>();

        Label binaryOperatorLabel = new Label("Binary operators: ");
        binaryOperatorLabel.setPrefWidth(95);
        Button addBinaryOperatorButton = new Button("+");
        Button removeBinaryOperatorButton = new Button("-");
        addBinaryOperatorButton.setFont(new Font(16)); removeBinaryOperatorButton.setFont(new Font(16));
        addBinaryOperatorButton.setPadding(new Insets(0, 5, 0, 5)); removeBinaryOperatorButton.setPadding(new Insets(1, 8, 1, 8));

        addBinaryOperatorButton.setOnAction(e -> {
            BoxedDRTA bdrta = newOperatorBoxedDRTA();
            binaryOperatorList.add(bdrta);
            fieldModified = true;
            updateBinaryOperatorGridFromFields();
        });
        removeBinaryOperatorButton.setOnAction(e -> {
            int index = binaryOperatorList.size();
            index--;
            if (index >= 0) {
                binaryOperatorList.remove(index);
                fieldModified = true;
                updateBinaryOperatorGridFromFields();
            }
        });

        HBox binaryOperatorBox = new HBox(10, binaryOperatorLabel, addBinaryOperatorButton, removeBinaryOperatorButton, binaryOperatorsPane);
        binaryOperatorBox.setAlignment(Pos.CENTER_LEFT);

        //main formulas top
        mainFormulasPane = new GridPane();
        mainFormulasPane.setPadding(new Insets(10, 0, 0, 105));
        mainFormulasPane.setVgap(10);
        mainFormulaList = new ArrayList<>();
        BoxedDRTA mainFormulaBoxedDRTA = newMainFormulaBoxedDRTA();
        mainFormulaBoxedDRTA.getRTA().getActionFactory().saveNow().execute(new ActionEvent());
        mainFormulaList.add(mainFormulaBoxedDRTA);
        updateMainFormulaGridFromFields();

        Label mainFormulaLabel = new Label("Main formulas: ");
        mainFormulaLabel.setPrefWidth(95);
        Button addMainFormulaButton = new Button("+");
        Button removeMainFormulaButton = new Button("-");
        addMainFormulaButton.setFont(new Font(16)); removeMainFormulaButton.setFont(new Font(16));
        addMainFormulaButton.setPadding(new Insets(0, 5, 0, 5)); removeMainFormulaButton.setPadding(new Insets(1, 8, 1, 8));

        addMainFormulaButton.setOnAction(e -> {
            BoxedDRTA bdrta = newMainFormulaBoxedDRTA();
            mainFormulaList.add(bdrta);
            fieldModified = true;
            updateMainFormulaGridFromFields();
        });
        removeMainFormulaButton.setOnAction(e -> {
            int index = mainFormulaList.size();
            index--;
            if (index > 0) {
                mainFormulaList.remove(index);
                fieldModified = true;
                updateMainFormulaGridFromFields();
            } else {
                EditorAlerts.showSimpleAlert("Cannot Remove", "A truth table must include at least one formula.");
            }
        });

        mainFormulasPane.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            int index = mainFormulaList.indexOf(focusedBoxedDRTA);
            if (index >= 0) {
                KeyCode code = e.getCode();
                if (code == KeyCode.ENTER || code == KeyCode.DOWN) {
                    if (index + 1 < mainFormulaList.size()) {
                        mainFormulaList.get(index + 1).getRTA().requestFocus();
                    }
                    e.consume();
                }
                if (code == KeyCode.UP) {
                    if (index > 0) {
                        mainFormulaList.get(index - 1).getRTA().requestFocus();
                    }
                    e.consume();
                }
            }
        });

        conclusionDividerCheck = new CheckBox("Include Conclusion Divider");
        ChangeListener conclusionDividerCheckListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
            }
        };
        conclusionDividerCheck.selectedProperty().addListener(conclusionDividerCheckListener);

        HBox mainFormulasTop = new HBox(10, mainFormulaLabel, addMainFormulaButton, removeMainFormulaButton, conclusionDividerCheck);
        mainFormulasTop.setAlignment(Pos.CENTER_LEFT);
        mainFormulasTop.setMargin(conclusionDividerCheck, new Insets(0, 0, 0, 100));

        upperFieldsBox = new VBox(10, nameBox, choicesBox, languagePresetsBox, unaryOperatorBox, binaryOperatorBox, mainFormulasTop, mainFormulasPane);
        upperFieldsBox.setPadding(new Insets(20, 0, 20, 0));

        //center area
        String helpText = "<body style=\"margin-left:10; margin-right: 20\">" +
                "<p>The Truth Table AB Explain Exercise is like Truth Table Exercise except that it requests a choice between some mutually exclusive options (as valid/invalid) along with a short explanation.</p>" +
                "<ul>" +
                "<li><p>Begin with the exercise name, prompt, and points fields.  If 'points possible' is other than zero, a points field is added to the exercise comment area (and one for total assignment points into the assignment comment area).</p></li>" +
                "<li><p>The checkbox lead appears prior to the check boxes, the A prompt with the first box, and the B prompt with the second.</p></li>" +
                "<li><p>The preset operator buttons set operators according to the official and abbreviating sentential languages from <em>Symbolic Logic</em>; alternatively, you may edit sentential operator symbols individually using the plus and minus buttons to add and remove fields.</p></li> " +
                "<li><p>Then supply formulas to appear across the top of the truth table (not including the base column), using the plus and minus buttons to add and remove fields.  The \"conclusion divider\" merely inserts an extra space and slash ('/') prior to the last formula." +
                "<li><p>Finally provide the exercise statement.</p></li>" +
                "</ul>";
        WebView helpArea = new WebView();
        WebEngine webEngine = helpArea.getEngine();
        webEngine.setUserStyleSheetLocation("data:, body {font: 14px Noto Serif Combo; }");
        webEngine.loadContent(helpText);
        helpArea.setPrefHeight(230);


        centerBox = new VBox(10, upperFieldsBox, statementRTA, helpArea);
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
        zoomSpinner = new Spinner(25, 500, 100, 5);
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

        //setup  window
        scene = new Scene(borderPane);
        scene.getStylesheets().add(DecoratedRTA.class.getClassLoader().getResource("slappEditor.css").toExternalForm());

        stage = new Stage();
        stage.initOwner(EditorMain.mainStage);
        stage.setScene(scene);

        stage.setTitle("Create Truth Table AB Explain Exercise:");
        stage.getIcons().addAll(EditorMain.icons);
        stage.setWidth(890);
        stage.setMinWidth(860);
        stage.setHeight(940);

        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        stage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 890));
        stage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 940));

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
     * Update operator Boxed DRTA fields from model
     * @param model the model
     */
    private void updateOperatorFieldsFromModel(TruthTableExpModel model){
        unaryOperatorList.clear();
        List<String> unaryList = model.getUnaryOperators();
        for (String str : unaryList) {
            BoxedDRTA bdrta = newOperatorBoxedDRTA();
            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().open(new Document(str)).execute(new ActionEvent());
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            unaryOperatorList.add(bdrta);
        }
        binaryOperatorList.clear();
        List<String> binaryList = model.getBinaryOperators();
       for (String str : binaryList) {
           BoxedDRTA bdrta = newOperatorBoxedDRTA();
           RichTextArea rta = bdrta.getRTA();
           rta.getActionFactory().open(new Document(str)).execute(new ActionEvent());
           rta.getActionFactory().saveNow().execute(new ActionEvent());
           binaryOperatorList.add(bdrta);
       }
    }

    /*
     * Update the unary operator grid pane from bdrta fields
     */
    private void updateUnaryOperatorGridFromFields(){
        unaryOperatorsPane.getChildren().clear();
        for (int i = 0; i < unaryOperatorList.size(); i++) {
            BoxedDRTA bdrta = unaryOperatorList.get(i);
            unaryOperatorsPane.add(bdrta.getBoxedRTA(), i, 0);
        }
    }

    /*
     * Update the binary operator grid pane from bdrta fields
     */
    private void updateBinaryOperatorGridFromFields(){
        binaryOperatorsPane.getChildren().clear();
        for (int i = 0; i < binaryOperatorList.size(); i++) {
            BoxedDRTA bdrta = binaryOperatorList.get(i);
            binaryOperatorsPane.add(bdrta.getBoxedRTA(), i, 0);
        }
    }

    /*
     * Update Boxed DRTA main formula fields from model
     * @param model the model
     */
    private void updateMainFormulaFieldsFromModel(TruthTableExpModel model){
        mainFormulaList.clear();
        List<Document> formulasList = model.getMainFormulas();

        for (Document doc : formulasList) {
            BoxedDRTA bdrta = newMainFormulaBoxedDRTA();
            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().open(doc).execute(new ActionEvent());
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            mainFormulaList.add(bdrta);
        }
    }

    /*
     * Update the main operator grid pane from the bdrta fields
     */
    private void updateMainFormulaGridFromFields(){
        mainFormulasPane.getChildren().clear();

        for (int i = 0; i < mainFormulaList.size(); i++) {
            BoxedDRTA bdrta = mainFormulaList.get(i);
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
        drta.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);
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
            mainWindow.closeExercise();
            stage.close();
        }
    }

    /*
     * View the exercise as currently constructed
     */
    private void viewExercise() {
        TruthTableExpModel model = extractModelFromWindow();
        TruthTableExpExercise exercise = new TruthTableExpExercise(model, mainWindow, true);
        exercise.generateEmptyTableModel();
        RichTextArea rta = exercise.getExerciseView().getExerciseStatement().getEditor();
        rta.setEditable(false);
        rta.prefHeightProperty().unbind();
        exercise.getExerciseView().setStatementPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getStatementPrefHeight()));
        mainWindow.setUpExercise(exercise);
    }

    /*
     * Clear create window, leaving operator, checkbox, explain prompt, and divider settings intact.
     */
    private void clearExercise() {
        if (checkContinue("Confirm Clear", "This exercise appears to have unsaved changes.\nContinue to clear exercise?")) {
            nameField.clear();
            nameField.textProperty().addListener(nameListener);
            choiceLeadField.textProperty().addListener(choiceLeadListener);
            aPromptField.textProperty().addListener(aPromptListener);
            bPromptField.textProperty().addListener(bPromptListener);
            mainFormulaList.clear();
            BoxedDRTA mainFormulaBoxedDRTA = newMainFormulaBoxedDRTA();
            mainFormulaList.add(mainFormulaBoxedDRTA);
            updateMainFormulaGridFromFields();
            statementRTA.getActionFactory().newDocumentNow().execute(new ActionEvent());
            viewExercise();
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

        TruthTableExpModel model = extractModelFromWindow();
        TruthTableExpExercise exercise = new TruthTableExpExercise(model, mainWindow, true);

        RichTextArea rta = exercise.getExerciseView().getExerciseStatement().getEditor();
        rta.setEditable(false);
        rta.prefHeightProperty().unbind();
        exercise.getExerciseView().setStatementPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getStatementPrefHeight()));
        exercise.saveExercise(saveAs);
        lowerSaveButton.setDisable(false);
        saveAsButton.setDisable(false);
        fieldModified = false;
    }

    /*
     * If exercise modified, check for continue
     * @param title String title of confirmation box
     * @param content String content of confirmation box
     * @return true if ok to continue, and otherwise false
     */
    private boolean checkContinue(String title, String content) {
        boolean okcontinue = true;

        for (BoxedDRTA bdrta : unaryOperatorList) {
            if (bdrta.getRTA().isModified()) {fieldModified = true; }
        }
        for (BoxedDRTA bdrta : binaryOperatorList) {
            if (bdrta.getRTA().isModified()) {fieldModified = true; }
        }
        for (BoxedDRTA bdrta : mainFormulaList) {
            if (bdrta.getRTA().isModified()) {fieldModified = true;  }
        }
        if (statementRTA.isModified()) {fieldModified = true;  }

        if (fieldModified) {
            Alert confirm = EditorAlerts.confirmationAlert(title, content);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.get() != OK) okcontinue = false;
        }

        return okcontinue;
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
     * Get the truth table explain model for the currently constructed exercise.
     * This leaves tableValues, rowComments, columnHighlights to be initialized by the TruthTableExercise (based on the model mainFormulas).
     * @return the derivation model
     */
    private TruthTableExpModel extractModelFromWindow() {
        TruthTableExpModel model = new TruthTableExpModel();

        model.setExerciseName(nameField.getText());
        model.setExplainPrompt(explainPromptField.getText());
        model.setStarted(false);

        if (statementRTA.isModified()) fieldModified = true;
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseStatement(statementRTA.getDocument());
        model.setStatementPrefHeight(statementTextHeight + 29);
        model.setStatementTextHeight(statementTextHeight);

        List<String> unaryOperatorStrings = new ArrayList<>();
        for (BoxedDRTA bdrta : unaryOperatorList) {
            RichTextArea rta = bdrta.getRTA();
            if (rta.isModified()) fieldModified = true;
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            String op = rta.getDocument().getText();
            unaryOperatorStrings.add(op);
        }
        model.setUnaryOperators(unaryOperatorStrings);

        List<String> binaryOperatorStrings = new ArrayList<>();
        for (BoxedDRTA bdrta : binaryOperatorList) {
            RichTextArea rta = bdrta.getRTA();
            if (rta.isModified()) fieldModified = true;
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            String op = rta.getDocument().getText();
            binaryOperatorStrings.add(op);
        }
        model.setBinaryOperators(binaryOperatorStrings);

        List<Document> mainFormulaDocs = new ArrayList<>();
        for (BoxedDRTA bdrta : mainFormulaList) {
            RichTextArea rta = bdrta.getRTA();
            if (rta.isModified()) fieldModified = true;
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            Document doc = rta.getDocument();
            mainFormulaDocs.add(doc);
        }
        model.setMainFormulas(mainFormulaDocs);

        model.setConclusionDivider(conclusionDividerCheck.isSelected());
        model.setChoiceLead(choiceLeadField.getText());
        model.setaPrompt(aPromptField.getText());
        model.setbPrompt(bPromptField.getText());

        if (!pointsPossibleTextField.getText().equals("")) model.setPointsPossible(Integer.parseInt(pointsPossibleTextField.getText()));
        else {
            model.setPointsPossible(0);
            pointsPossibleTextField.setText("0");
        }

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
