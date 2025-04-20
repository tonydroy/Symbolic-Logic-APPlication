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

package slapp.editor.derivation_explain;

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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import slapp.editor.DiskUtilities;
import slapp.editor.EditorAlerts;
import slapp.editor.EditorMain;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.decorated_rta.KeyboardDiagram;
import slapp.editor.derivation.LineType;
import slapp.editor.derivation.ModelLine;

import slapp.editor.main_window.ControlType;
import slapp.editor.main_window.MainWindow;
import slapp.editor.main_window.MainWindowView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.gluonhq.richtextarea.RichTextAreaSkin.KeyMapValue.*;
import static javafx.scene.control.ButtonType.OK;

public class DrvtnExpCreate {
    private MainWindow mainWindow;
    private RichTextArea statementRTA;
    private DecoratedRTA statementDRTA;
    private double statementTextHeight;
    private TextField nameField;
    private TextField promptField;
    private boolean fieldModified = false;
    private ChangeListener nameListener;
    private ChangeListener promptFieldListener;
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

    private List<DrvtnExpSetupLine> setupLines;
    private GridPane setupLinesPane;
    private Spinner<Double> widthSpinner;
    private ChangeListener defaultWidthListener;
    private Label zoomLabel;
    private Spinner<Integer> zoomSpinner;
    private Button updateHeightButton;
    private Button saveButton;
    private MenuBar menuBar;
    private BorderPane borderPane;
    private VBox upperFieldsBox;
    private ToolBar editToolbar;
    private ToolBar fontsToolbar;
    private ToolBar insertToolbar;
    private ToolBar paragraphToolbar;;
    private ToolBar kbdDiaToolBar;
    private ToolBar sizeToolBar;
    private Button lowerSaveButton;
    private Button saveAsButton;
    private DecoratedRTA dummyDRTA = new DecoratedRTA();

    private Pane spacerPane;
    private HBox namejBox;
    private Spinner<Double> statementHeightSpinner;


    /**
     * Create new derivation exercise
     * @param mainWindow the main window
     */
    public DrvtnExpCreate(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setupWindow();
    }

    /**
     * Open create window for existing exercise
     * @param mainWindow the main window
     * @param originalModel the model for the exercise to be modified
     */
    public DrvtnExpCreate(MainWindow mainWindow, DrvtnExpModel originalModel) {
        this(mainWindow);

        statementRTA.getActionFactory().open(originalModel.getExerciseStatement()).execute(new ActionEvent());
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        statementTextHeight = originalModel.getStatementTextHeight();
        nameField.setText(originalModel.getExerciseName());
        promptField.setText(originalModel.getContentPrompt());

        RichTextAreaSkin.KeyMapValue keyboardSelector = originalModel.getKeyboardSelector();
        if (keyboardSelector == ITALIC_AND_SANS) {italicAndSansCheck.setSelected(true); scriptAndItalicCheck.setSelected(false); scriptAndSansCheck.setSelected(false); italicAndBlackboardCheck.setSelected(false); greekAndFrakturCheck.setSelected(false);}
        if (keyboardSelector == SCRIPT_AND_ITALIC) {italicAndSansCheck.setSelected(true); scriptAndItalicCheck.setSelected(true); scriptAndSansCheck.setSelected(false); italicAndBlackboardCheck.setSelected(false); greekAndFrakturCheck.setSelected(false);}
        if (keyboardSelector == SCRIPT_AND_SANS) {italicAndSansCheck.setSelected(false); scriptAndItalicCheck.setSelected(false); scriptAndSansCheck.setSelected(true); italicAndBlackboardCheck.setSelected(false); greekAndFrakturCheck.setSelected(false);}
        if (keyboardSelector == ITALIC_AND_BLACKBOARD) {italicAndSansCheck.setSelected(false); scriptAndItalicCheck.setSelected(false); scriptAndSansCheck.setSelected(false); italicAndBlackboardCheck.setSelected(true); greekAndFrakturCheck.setSelected(false);}
        if (keyboardSelector == GREEK_AND_FRAKTUR) {italicAndSansCheck.setSelected(false); scriptAndItalicCheck.setSelected(false); scriptAndSansCheck.setSelected(false); italicAndBlackboardCheck.setSelected(false); greekAndFrakturCheck.setSelected(true);}


        scopeLineCheck.setSelected(originalModel.isLeftmostScopeLine());
        defaultShelfCheck.setSelected(originalModel.isDefaultShelf());
        widthSpinner.getValueFactory().setValue(((double) Math.round(originalModel.getGridWidth() * 100/2)) * 2);

        updateSetupLinesFromModel(originalModel);
        updateGridFromSetupLines();
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

        //statement editor
        statementDRTA = new DecoratedRTA();
        statementRTA = statementDRTA.getEditor();
        statementRTA.setPromptText("Exercise Statement:");
        statementRTA.getStylesheets().add("slappTextArea.css");
        statementRTA.setPrefWidth(PrintUtilities.getPageWidth());
        statementRTA.setMinWidth(PrintUtilities.getPageWidth());
        statementRTA.setMaxWidth(PrintUtilities.getPageWidth());
  //      statementRTA.setContentAreaWidth(PrintUtilities.getPageWidth());
        statementRTA.setPrefHeight(100);
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

        //top fields row

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

        Label promptLabel = new Label("Explain prompt");
        promptLabel.setPrefWidth(95);
        promptField = new TextField();
        promptField.setPromptText("(plain text)");
        promptFieldListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
                promptField.textProperty().removeListener(promptFieldListener);
            }
        };
        promptField.textProperty().addListener(promptFieldListener);

        promptField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });


        HBox nameBox = new HBox(10, nameLabel, nameField, promptLabel, promptField);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        nameBox.setMargin(promptLabel, new Insets(0,0,0,20));

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
                    scriptAndSansCheck.setSelected(false);
                    italicAndSansCheck.setSelected(false);
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


        widthSpinner = new Spinner<>(64.0, 100, 0, 2 );
        defaultWidthListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldModified = true;
                widthSpinner.valueProperty().removeListener(defaultWidthListener);
            }
        };
        widthSpinner.valueProperty().addListener(defaultWidthListener);
        widthSpinner.setPrefWidth(65);

        //setup lines control
        Label setupLinesLabel = new Label("Setup Lines: ");
        setupLinesLabel.setPrefWidth(75);
        Button addSetupLineButton = new Button("+");
        Button removeSetupLineButton = new Button("-");
        addSetupLineButton.setFont(new Font(16));
        addSetupLineButton.setPadding(new Insets(0,5,0,5));
        removeSetupLineButton.setFont(new Font(16));
        removeSetupLineButton.setPadding(new Insets(1,8,1,8));

        addSetupLineButton.setOnAction(e -> {
            DrvtnExpSetupLine newLine = new DrvtnExpSetupLine(this);

            setupLines.add(new DrvtnExpSetupLine(this));
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


        HBox keyboardBox = new HBox(10, keyboardLabel, italicAndSansCheck, scriptAndItalicCheck, scriptAndSansCheck, italicAndBlackboardCheck, greekAndFrakturCheck);

        Label widthLabel = new Label("Width: ");
        HBox topFields = new HBox(30, scopeLineCheck, defaultShelfCheck, widthLabel, widthSpinner, setupLinesLabel, addSetupLineButton, removeSetupLineButton);
        topFields.setAlignment(Pos.CENTER_LEFT);
        topFields.setMargin(widthLabel, new Insets(0, -20, 0, 0));
        topFields.setMargin(setupLinesLabel, new Insets(0,-20, 0, 0));

        //setup lines pane
        setupLines = new ArrayList<>();

        DrvtnExpSetupLine firstLine = new DrvtnExpSetupLine(this);
        RichTextArea firstLineFormulaRTA = firstLine.getFormulaBoxedDRTA().getRTA();
        firstLineFormulaRTA.getActionFactory().saveNow().execute(new ActionEvent());


        RichTextArea firstLineJustificationRTA = firstLine.getJustificationBoxedDRTA().getRTA();
        firstLineJustificationRTA.getActionFactory().saveNow().execute(new ActionEvent());

        setupLines.add(firstLine);
        setupLinesPane = new GridPane();
        setupLinesPane.setPadding(new Insets(5,15,10,0));
        setupLinesPane.setHgap(15);
        setupLinesPane.setVgap(15);
        updateGridFromSetupLines();

        upperFieldsBox = new VBox(15, nameBox, keyboardBox, topFields, setupLinesPane);
        upperFieldsBox.setPadding(new Insets(20,0,20,0));

        String helpText = "<body style=\"margin-left:10; margin-right: 20\">"+
                "<p>Derivation Explain is appropriate for any exercise that calls for a derivation together with an explanation.<p>" +
                "<ul>" +
                "<li><p>Setup is the same as Derivation Exercise except that you may add a prompt to appear in the explanation area.  For the derivation explain exercise, provide the exercise name, and explanation prompt.  " +
                "Then select the default keyboard and whether there is to be a leftmost scope line, and/or a \"shelf\" beneath the top line of automatically an generated subderivation; width is the (default) percentage of the window's width allocated to this derivation.</p></li>" +
                "<li><p>After that, insert setup derivation lines as appropriate, and give the exercise statement.</p></li>" +
                "</ul>";

        WebView helpArea = new WebView();
        WebEngine webEngine = helpArea.getEngine();
        webEngine.setUserStyleSheetLocation("data:, body {font: 14px Noto Serif Combo; }");
        webEngine.loadContent(helpText);
        helpArea.setPrefHeight(190);




        centerBox = new VBox(10, upperFieldsBox, statementRTA, helpArea);
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

        });

        sizeToolBar = new ToolBar();
        sizeToolBar.setPrefHeight(38);
        sizeToolBar.getItems().addAll(zoomLabel, zoomSpinner, new Label("     V Sz:"), statementHeightSpinner);



        stage = new Stage();
        stage.initOwner(EditorMain.mainStage);
        stage.setScene(scene);

        stage.setTitle("Create Derivation Explain Exercise:");
        stage.getIcons().addAll(EditorMain.icons);
        stage.setWidth(1030);
        stage.setHeight(850);
        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        stage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 1030));
        stage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 850));

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
     * Update list of setup lines from derivation model
     * @param originalModel the derivation model
     */
    private void updateSetupLinesFromModel(DrvtnExpModel originalModel) {

        List<ModelLine> modelLines = originalModel.getDerivationLines();
        setupLines.clear();
        int i = 0;
        while (i < modelLines.size()) {
            ModelLine modelLine = modelLines.get(i);
            if (LineType.isContentLine(modelLine.getLineType())) {
                DrvtnExpSetupLine setupLine = new DrvtnExpSetupLine(this);

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
            DrvtnExpSetupLine setupLine = setupLines.get(i);
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
            DrvtnExpSetupLine setupLine = setupLines.get(i);
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
     * Clear create window, leaving keyboard, scope, shelf, and prompt settings intact.
     */
    private void clearExercise() {
        if (checkContinue("Confirm Clear", "This exercise appears to have unsaved changes.\nContinue to clear exercise?")) {
            nameField.clear();
            nameField.textProperty().addListener(nameListener);
            promptField.textProperty().addListener(promptFieldListener);
            widthSpinner.getValueFactory().setValue(0.0);

            setupLines.clear();
            DrvtnExpSetupLine firstLine = new DrvtnExpSetupLine(this);
            firstLine.getFormulaBoxedDRTA().getRTA().getActionFactory().saveNow().execute(new ActionEvent());
            firstLine.getJustificationBoxedDRTA().getRTA().getActionFactory().saveNow().execute(new ActionEvent());
            setupLines.add(firstLine);
            updateGridFromSetupLines();

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
        boolean okcontinue = true;
        for (DrvtnExpSetupLine line : setupLines) {
            if (line.isModified()) fieldModified = true;
        }
        if (fieldModified || statementRTA.isModified()) {
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
        DrvtnExpModel model = extractModelFromWindow();
        DrvtnExpExercise exercise = new DrvtnExpExercise(model, mainWindow);
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
        promptField.textProperty().addListener(promptFieldListener);
        scopeLineCheck.selectedProperty().addListener(leftmostScopeListner);
        defaultShelfCheck.selectedProperty().addListener(defaultShelfListener);

        DrvtnExpModel model = extractModelFromWindow();

        boolean success = DiskUtilities.saveExercise(saveAs, model);
        if (success) {
            fieldModified = false;
            for (DrvtnExpSetupLine line : setupLines) { line.setModified(false); }
        }

        lowerSaveButton.setDisable(false);
        saveAsButton.setDisable(false);
    }

    /*
     * Get the derivation explain model for the currently constructed exercise
     * @return the derivation model
     */
    private DrvtnExpModel extractModelFromWindow() {
        String name = nameField.getText();
        String prompt = promptField.getText();

        boolean leftmostScope = scopeLineCheck.isSelected();
        boolean defaultShelf = defaultShelfCheck.isSelected();
        double gridWidth = widthSpinner.getValue()/100.0;

        if (statementRTA.isModified()) fieldModified = true;
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document statementDocument = statementRTA.getDocument();
        double statementPrefHeight = statementTextHeight + 25;
        Document commentDocument = new Document();
        Document explainDocument = new Document();


        List<ModelLine> modelLines = new ArrayList<>();
        for (int i = 0; i < setupLines.size(); i++) {
            DrvtnExpSetupLine setupLine = setupLines.get(i);
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


        DrvtnExpModel model = new DrvtnExpModel(name, false, statementPrefHeight, gridWidth, prompt, leftmostScope, defaultShelf, keyboardSelector, statementDocument, commentDocument, commentDocument, modelLines);
        model.setStatementTextHeight(statementTextHeight);
        return model;
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
