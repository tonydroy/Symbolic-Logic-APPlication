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

package slapp.editor.truth_table;

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

import static javafx.scene.control.ButtonType.OK;

/**
 * Create window for the truth table exercise
 */
public class TruthTableCreate {
    private MainWindow mainWindow;
    private MainWindowView mainView;
    private TextField nameField;
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
    private ChangeListener nameListener;
    private double formulaBoxHeight = 27;
    private Button lowerSaveButton;
    private Button saveAsButton;
    private BoxedDRTA focusedBoxedDRTA;
    private ToolBar sizeToolBar;
    private DecoratedRTA dummyDRTA = new DecoratedRTA();


    /**
     * Create new truth table exercise
     * @param mainWindow the main window
     */
    public TruthTableCreate(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setupWindow();
    }

    /**
     * Create window for existing exercise
     * @param mainWindow the main window
     * @param originalModel the model for the exercise to be modified
     */
    public TruthTableCreate(MainWindow mainWindow, TruthTableModel originalModel) {
        this(mainWindow);

        nameField.setText(originalModel.getExerciseName());
        statementRTA.getActionFactory().open(originalModel.getExerciseStatement()).execute(new ActionEvent());
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        statementTextHeight = originalModel.getStatementTextHeight();
        conclusionDividerCheck.setSelected(originalModel.isConclusionDivider());
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
        statementRTA.setPrefWidth(PrintUtilities.getPageWidth());
    //    statementRTA.setContentAreaWidth(PrintUtilities.getPageWidth());
        statementRTA.setPrefHeight(100);
        statementRTA.setMinHeight(50);
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

        HBox nameBox = new HBox(10, nameLabel, nameField);
        nameBox.setAlignment(Pos.CENTER_LEFT);

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

        upperFieldsBox = new VBox(10, nameBox, languagePresetsBox, unaryOperatorBox, binaryOperatorBox, mainFormulasTop, mainFormulasPane);
        upperFieldsBox.setPadding(new Insets(20, 0, 20, 20));

        //center area
        String helpText = "Truth Table Exercise is appropriate for any exercise that requires a truth table as response.\n\n" +
                "For the Truth Table exercise, supply the exercise name and exercise statement.  " +
                "The preset operator buttons set operators according to the official and abbreviating languages from Symbolic Logic; alternatively, you may edit sentential operator symbols individually. " +
                "Finally supply formulas to appear across the top of the truth table (not including the base column).  The \"conclusion divider\" merely inserts an extra space and slash ('/') prior to the last formula." ;

        helpArea = new TextArea(helpText);
        helpArea.setWrapText(true);
        helpArea.setPrefHeight(150);
        helpArea.setEditable(false);
        helpArea.setFocusTraversable(false);
        helpArea.setMouseTransparent(true);
        helpArea.setStyle("-fx-text-fill: mediumslateblue");

        centerBox = new VBox(10, statementRTA, helpArea);
        centerBox = new VBox(10, statementRTA, helpArea);
        Group centerGroup = new Group(centerBox);
        borderPane.setCenter(centerGroup);

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
            setCenterVgrow();
        });

        sizeToolBar = new ToolBar();
        sizeToolBar.setPrefHeight(38);
        sizeToolBar.getItems().addAll(zoomLabel, zoomSpinner, new Label("     "));

        //setup  window
        scene = new Scene(borderPane);
        scene.getStylesheets().add(DecoratedRTA.class.getClassLoader().getResource("slappEditor.css").toExternalForm());

        stage = new Stage();
        stage.initOwner(EditorMain.mainStage);
        stage.setScene(scene);

        stage.setTitle("Create Truth Table Exercise:");
        stage.getIcons().addAll(EditorMain.icons);
        stage.setWidth(860);
        stage.setMinWidth(860);
        stage.setHeight(900);
        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        stage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 860));
        stage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 900));


        stage.initModality(Modality.WINDOW_MODAL);
        stage.setOnCloseRequest(e-> {
            e.consume();
            closeWindow();
        });

        stage.show();
        statementRTA.getActionFactory().save().execute(new ActionEvent());
        centerBox.layout();
        setCenterVgrow();
        Platform.runLater(() -> nameField.requestFocus());

    }

    /*
     * Update operator Boxed DRTA fields from model
     * @param model the model
     */
    private void updateOperatorFieldsFromModel(TruthTableModel model){
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
    private void updateMainFormulaFieldsFromModel(TruthTableModel model){
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
        BoxedDRTA boxedDRTA = new BoxedDRTA();
        DecoratedRTA drta = boxedDRTA.getDRTA();
        drta.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);
        RichTextArea rta = boxedDRTA.getRTA();
        rta.setMaxHeight(formulaBoxHeight);
        rta.setMinHeight(formulaBoxHeight);
        rta.setPrefWidth(30);
        rta.getStylesheets().add("RichTextFieldWide.css");

        rta.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(drta, ControlType.STATEMENT);
            }
        });
        return boxedDRTA;
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
        TruthTableModel model = extractModelFromWindow();
        TruthTableExercise exercise = new TruthTableExercise(model, mainWindow, true);
        exercise.generateEmptyTableModel();
        RichTextArea rta = exercise.getExerciseView().getExerciseStatement().getEditor();
        rta.setEditable(false);
        rta.prefHeightProperty().unbind();
        exercise.getExerciseView().setStatementPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getStatementPrefHeight()));
        mainWindow.setUpExercise(exercise);
    }

    /*
     * Clear create window, leaving operator and divider settings intact.
     */
    private void clearExercise() {
        if (checkContinue("Confirm Clear", "This exercise appears to have unsaved changes.\nContinue to clear exercise?")) {
            nameField.clear();
            nameField.textProperty().addListener(nameListener);

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
        TruthTableModel model = extractModelFromWindow();
        TruthTableExercise exercise = new TruthTableExercise(model, mainWindow, true);
        RichTextArea rta = exercise.getExerciseView().getExerciseStatement().getEditor();
        rta.setEditable(false);
        rta.prefHeightProperty().unbind();
        exercise.getExerciseView().setStatementPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getStatementPrefHeight()));

        //this works (as opposed to TTgen) because the exercise doesn't extract formula fields from the view (which would fail for DRTA not on scene) but simply repeats the model values.
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
            if (bdrta.getRTA().isModified()) {fieldModified = true; }
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
        centerBox.setScaleX(scale);
        centerBox.setScaleY(scale);
        scene.getWindow().setWidth(Math.max(1030, PrintUtilities.getPageWidth() * scale + 55));
        setCenterVgrow();

        KeyboardDiagram keyboardDiagram = KeyboardDiagram.getInstance();
        keyboardDiagram.updateFontSize(scale);
        keyboardDiagram.initialize(statementDRTA);
        keyboardDiagram.update();
    }

    /*
     *  Bind vertical height of statement field to window size
     */
    private void setCenterVgrow() {
        double fixedHeight = helpArea.getHeight() * scale + 550;
        DoubleProperty fixedValueProperty = new SimpleDoubleProperty(fixedHeight);
        DoubleProperty externalHeightProperty = new SimpleDoubleProperty();
        externalHeightProperty.bind(fixedValueProperty.add(mainFormulasPane.heightProperty()));
        DoubleProperty maximumHeightProperty = new SimpleDoubleProperty(PrintUtilities.getPageHeight() );
        DoubleProperty scaleProperty = new SimpleDoubleProperty(scale);
        centerHeightProperty = new SimpleDoubleProperty();
        centerHeightProperty.bind(Bindings.min(maximumHeightProperty, (stage.heightProperty().subtract(externalHeightProperty)).divide(scaleProperty)));
        statementRTA.prefHeightProperty().bind(centerHeightProperty);
    }


    /*
     * Get the truth table model for the currently constructed exercise.
     * This leaves tableValues, rowComments, columnHighlights to be initialized by the TruthTableExercise (based on the model mainFormulas).
     * @return the derivation model
     */
    private TruthTableModel extractModelFromWindow() {
        TruthTableModel model = new TruthTableModel();

        model.setExerciseName(nameField.getText());
        model.setStarted(false);

        if (statementRTA.isModified()) fieldModified = true;
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseStatement(statementRTA.getDocument());
        model.setStatementPrefHeight(statementTextHeight + 25);
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

        VBox topBox = new VBox(menuBar, paragraphToolbar, fontsAndEditBox, kbdBox, upperFieldsBox);

        borderPane.topProperty().setValue(topBox);
    }

    /*
     * With text field in focus, set disabled DRTA controls
     */
    private void textFieldInFocus() {
        editorInFocus(dummyDRTA, ControlType.STATEMENT);
    }




}
