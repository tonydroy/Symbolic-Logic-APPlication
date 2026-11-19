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

package slapp.editor.ab_explain;


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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;

import javafx.stage.Modality;
import javafx.stage.Stage;
import slapp.editor.DiskUtilities;
import slapp.editor.EditorAlerts;
import slapp.editor.EditorMain;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.decorated_rta.KeyboardDiagram;
import slapp.editor.main_window.ControlType;
import slapp.editor.main_window.MainWindow;
import slapp.editor.main_window.MainWindowView;
import slapp.editor.main_window.TextHelpPopup;
import slapp.editor.page_editor.PageContent;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static javafx.scene.control.ButtonType.OK;
import static slapp.editor.EditorMain.RTA_SCREEN_BMP;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;

/**
 * Create window for the AB edit exercise
 */
public class ABcreate {
    private MainWindow mainWindow;
    private RichTextArea statementRTA;
    private DecoratedRTA statementDRTA;
    private double statementTextHeight;
    private TextField nameField;
    private TextField leaderField;
    private TextField promptFieldA;
    private TextField promptFieldB;
    private TextField contentPromptField;
    private boolean fieldsModified = false;
    private ChangeListener nameListener;
    private ChangeListener leaderListener;
    private ChangeListener fieldListenerA;
    private ChangeListener fieldListenerB;
    private ChangeListener contentPromptListener;
    private ChangeListener pointsPossibleListener;
    private ChangeListener auxExerListener;
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
    private VBox textFieldsPromptBox;
    private BorderPane borderPane;

    private Pane spacerPane;
    private Spinner<Double> statementHeightSpinner;

    private TextField pointsPossibleTextField;
    private TextField auxExerNameField;

    private CheckBox staticHelpCheck;
    private Spinner<Double> staticHelpHeightSpinner;
    private RichTextArea staticHelpRTA;
    private DecoratedRTA staticHelpDRTA;

    private Spinner checkMaxSpinner;
    private ABCheckSetup checkSetup;

    CheckBox choiceABox;
    CheckBox choiceBBox;

    private final DoubleProperty localScalePageHeight = new SimpleDoubleProperty(1.0);
    private final DoubleProperty localScalePageWidth = new SimpleDoubleProperty(1.0);

    /**
     * Construct AB edit create window from scratch
     * @param mainWindow the main window
     */
    public ABcreate(MainWindow mainWindow) {
        this.mainWindow = mainWindow;

        localScalePageHeight.bind(mainWindow.getMainView().scalePageHeightProperty());
        localScalePageWidth.bind(mainWindow.getMainView().scalePageWidthProperty());
        setupWindow();
    }

    /**
     * Open create window for modification of existing AB edit exercise
     * @param mainWindow the main window
     * @param originalModel model for the existing exercise
     */
    public ABcreate(MainWindow mainWindow, ABmodel originalModel) {
        this(mainWindow);
        statementRTA.getActionFactory().open(originalModel.getExerciseStatement()).execute(new ActionEvent());
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        statementTextHeight = originalModel.getStatementTextHeight();
        nameField.setText(originalModel.getExerciseName());
        contentPromptField.setText(originalModel.getContentPrompt());

        pointsPossibleTextField.setText(Integer.toString(originalModel.getPointsPossible()));

        ABmodelExtra fields = originalModel.getModelFields();
        leaderField.setText(fields.getLeader());
        promptFieldA.setText(fields.getPromptA());
        promptFieldB.setText(fields.getPromptB());
        auxExerNameField.setText(originalModel.getAuxExerName());

        checkSetup = originalModel.getCheckSetup();
        if (checkSetup == null) checkSetup = new ABCheckSetup();

        staticHelpCheck.setSelected(checkSetup.isStaticHelp());
        staticHelpRTA.getActionFactory().open(checkSetup.getStaticHelpDoc()).execute(new ActionEvent());
        staticHelpRTA.getActionFactory().saveNow().execute(new ActionEvent());

        checkMaxSpinner.getValueFactory().setValue(checkSetup.getCheckMax());
        choiceABox.setSelected(checkSetup.isChoiceA());
        choiceBBox.setSelected(checkSetup.isChoiceB());

        fieldsModified = false;
        nameField.textProperty().addListener(nameListener);
        contentPromptField.textProperty().addListener(contentPromptListener);
        leaderField.textProperty().addListener(leaderListener);
        promptFieldA.textProperty().addListener(fieldListenerA);
        promptFieldB.textProperty().addListener(fieldListenerB);
        auxExerNameField.textProperty().addListener(auxExerListener);

    }

    /*
     * Set up the create view
     */
    private void setupWindow() {
        borderPane = new BorderPane();

        //empty bar for consistent look
        Menu helpMenu = new Menu("");
        menuBar = new MenuBar(helpMenu);

        statementDRTA = new DecoratedRTA();
        statementRTA = statementDRTA.getEditor();
        statementRTA.getStylesheets().add("slappTextArea.css");
        statementRTA.setPromptText("Exercise Statement:");

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

        staticHelpCheck = new CheckBox();
        staticHelpCheck.setSelected(false);
        ChangeListener staticHelpCheckListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldsModified = true;
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



        Label choiceALabel = new Label("    Correct     \u21d2     A choice:");
        choiceABox = new CheckBox();
        choiceABox.setSelected(false);
        choiceABox.selectedProperty().addListener((ob, ov, nv) -> {
            fieldsModified = true;
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
            fieldsModified = true;
            boolean selected = (boolean) nv;
            if (selected) {
                choiceABox.setSelected(false);
            }
        });
        HBox choiceBHBox = new HBox(10, choiceBLabel, choiceBBox);
        choiceBHBox.setAlignment(Pos.CENTER_LEFT);

        HBox helpChoiceBox = new HBox(20, staticHelpBox, checkMaxBox, choiceAHBox, choiceBHBox);


        HBox textBoxes = new HBox(10, statementRTA, staticHelpRTA);

        Label nameLabel = new Label("Exercise Name: ");
        nameLabel.setPrefWidth(100);
        nameField  = new TextField();
        nameField.setPromptText("(plain text)");
        nameListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldsModified = true;
                nameField.textProperty().removeListener(nameListener);
            }
        };
        nameField.textProperty().addListener(nameListener);

        nameField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        Label auxExerLabel = new Label("Aux Exer Name: ");
        auxExerLabel.setPrefWidth(100);
        auxExerNameField = new TextField();
        auxExerNameField.setPadding(new Insets(5));
        auxExerNameField.setPromptText("(plain text)");
        auxExerNameField.setPrefWidth(200);
        auxExerListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldsModified = true;
            }
        };
        auxExerNameField.textProperty().addListener(auxExerListener);
        auxExerNameField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });



        Label leaderLabel = new Label("Checkbox Lead: ");
        leaderLabel.setPrefWidth(100);
        leaderField = new TextField();
        leaderField.setPromptText("(plain text)");
        leaderListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldsModified = true;
                leaderField.textProperty().removeListener(leaderListener);
            }
        };
        leaderField.textProperty().addListener(leaderListener);

        leaderField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        Label promptLabelA = new Label("A Prompt: ");
        promptLabelA.setPrefWidth(100);
        promptFieldA = new TextField();
        promptFieldA.setPromptText("(plain text)");
        fieldListenerA = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldsModified = true;
                promptFieldA.textProperty().removeListener(fieldListenerA);
            }
        };
        promptFieldA.textProperty().addListener(fieldListenerA);

        promptFieldA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        Label promptLabelB = new Label("B Prompt: ");
        promptLabelB.setPrefWidth(100);
        promptFieldB = new TextField();
        promptFieldB.setPromptText("(plain text)");
        fieldListenerB = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldsModified = true;
                promptFieldB.textProperty().removeListener(fieldListenerB);
            }
        };
        promptFieldB.textProperty().addListener(fieldListenerB);

        promptFieldB.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });

        Label contentPromptLabel = new Label("Explain Prompt: ");
        contentPromptLabel.setPrefWidth(100);
        contentPromptField = new TextField();
        contentPromptField.setPromptText("(plain text)");
        contentPromptListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                fieldsModified = true;
                contentPromptField.textProperty().removeListener(contentPromptListener);
            }
        };
        contentPromptField.textProperty().addListener(contentPromptListener);

        contentPromptField.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) textFieldInFocus();
        });



        Label pointsPossibleLabel = new Label("Points Possible: ");
        pointsPossibleLabel.setPrefWidth(100);

        pointsPossibleTextField = new TextField();
        pointsPossibleTextField.setPrefWidth(35);
        pointsPossibleTextField.setAlignment(Pos.CENTER_RIGHT);
        pointsPossibleTextField.setPadding(new Insets(5,5,5,5));

        pointsPossibleTextField.setText("0");
        pointsPossibleTextField.textProperty().addListener((ob,ov,nv) -> { fieldsModified = true; });

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



        HBox nameBox = new HBox(nameLabel, nameField);
        nameBox.setAlignment(Pos.BASELINE_LEFT);
        HBox promptBox = new HBox(contentPromptLabel, contentPromptField);
        promptBox.setAlignment(Pos.BASELINE_LEFT);
        HBox leaderBox = new HBox(leaderLabel, leaderField);
        leaderBox.setAlignment(Pos.BASELINE_LEFT);
        HBox aBbox = new HBox(promptLabelA, promptFieldA);
        promptBox.setAlignment(Pos.BASELINE_LEFT);
        HBox bBox = new HBox(promptLabelB, promptFieldB);
        bBox.setAlignment(Pos.BASELINE_LEFT);
        HBox auxExerBox = new HBox(auxExerLabel, auxExerNameField);
        auxExerBox.setAlignment(Pos.BASELINE_LEFT);


        HBox pointsBox = new HBox(pointsPossibleLabel, pointsPossibleTextField, helpChoiceBox);
        pointsBox.setAlignment(Pos.CENTER_LEFT);

        textFieldsPromptBox = new VBox(10,nameBox, promptBox, leaderBox, aBbox, bBox, auxExerBox, pointsBox);
        textFieldsPromptBox.setPadding(new Insets(20,0,20,0));

        String header = TextHelpPopup.getHeader14();
        String footer = TextHelpPopup.getFooter();
        String helpText = header +
                "<p>AB Explain is appropriate for any exercise that requires a choice between mutually exclusive options (as true/false, consistent/inconsistent) together with an explanation or justification.</p>" +
                "<ul>" +
                "<li><p>For the AB Explain exercise, you supply the exercise name and, if desired, a prompt to appear in the explanation field.</p></li> " +
                "<li><p>Then the Checkbox Lead appears prior to the check boxes, the A Prompt with the first box, and the B Prompt with the second.</p></li>" +
                "<li><p>In the context of an assignment containing a previous exercise relevant to the one being worked, it is possible to add its name in order to include a thumbnail that will pop up its work area. </p></li>" +
                "<li><p>If 'points possible' is other than zero, a points field is added to the exercise comment area (and one for total assignment points into the assignment comment area).</p>" +
                "<p>The Static Help check activates the Static Help button which pops up a message which you may state in the right-hand text area below.</p>" +
                "<p>If Check Max is 0, checking is disabled; if -1 checks are unlimited; otherwise the value sets the maximum number of allowable check tries.  Selecting an A/B choice by itself enables choice check for instructors.  For students choice check is enabled only if in addition Check Max is other than 0 (in which case the check essentially gives the answer).  </p></li>" +

                "<li><p>Finally, provide the exercise statement and, if desired, static help.</p></li>" +
                "</ul>" +
                footer;

        scene = new Scene(borderPane);

        Browser browser = mainWindow.getCreateHlpBrowser();
        if (browser != null) {
            BrowserView view = BrowserView.newInstance(browser);
            view.setMouseTransparent(true);
            Platform.runLater(() -> TextHelpPopup.loadHtmlSmoothly(browser, view, helpText));
            view.setPrefHeight(400);
            view.prefWidthProperty().bind(textBoxes.widthProperty());
            centerBox = new VBox(10, textFieldsPromptBox, textBoxes, view);
            centerBox.setFillWidth(true);
        }
        else {
            centerBox = new VBox(10, textFieldsPromptBox, textBoxes, new Label("JxBrowser not started.  Please try again."));
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
        sizeToolBar.getItems().addAll(zoomLabel, zoomSpinner, new Label("     V Sz:"), statementHeightSpinner);

        stage = new Stage();
    //    stage.initOwner(EditorMain.mainStage);
        stage.setScene(scene);
        stage.setTitle("Create AB Explain Exercise:");
        stage.getIcons().addAll(EditorMain.icons);




        stage.setWidth(1040);
        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        double stageHeight = Math.min(995, bounds.getHeight() - 20);
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

    /*
     * Update zoom value
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
     * Clear the exercise (leaving explain prompt and checkbox setup)
     */
    private void clearExercise() {
        if (checkContinue("Confirm Clear", "This exercise appears to have unsaved changes.\nContinue to clear exercise?")) {
            nameField.clear();
            nameField.textProperty().addListener(nameListener);
            auxExerNameField.clear();
            choiceABox.setSelected(false);
            choiceBBox.setSelected(false);
            statementRTA.getActionFactory().newDocumentNow().execute(new ActionEvent());
            staticHelpRTA.getActionFactory().newDocumentNow().execute(new ActionEvent());
            viewExercise();
            fieldsModified = false;
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
        if (fieldsModified || statementRTA.isModified() || staticHelpRTA.isModified()) {
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
        ABmodel model = extractModelFromWindow();
        ABexercise exercise = new ABexercise(model, mainWindow);
        RichTextArea rta = exercise.getExerciseView().getExerciseStatement().getEditor();
        rta.prefHeightProperty().unbind();
        rta.setEditable(false);
        exercise.getExerciseView().setStatementPrefHeight(Math.min(PrintUtilities.getPageHeight(), model.getStatementPrefHeight()));
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

        ABmodel model = extractModelFromWindow();

        boolean success = DiskUtilities.saveExercise(saveAs, model);
        if (success) {
            fieldsModified = false;
        }

        saveButton.setDisable(false);
        saveAsButton.setDisable(false);
    }

    /*
     * Generate an AB EditModel from window contents
     * @return the simple edit model
     */
    private ABmodel extractModelFromWindow() {
        String name = nameField.getText();
        String leader = leaderField.getText();
        String Aprompt = promptFieldA.getText();
        String Bprompt = promptFieldB.getText();
        ABmodelExtra fields = new ABmodelExtra(leader, Aprompt, false, Bprompt, false);
        String prompt = contentPromptField.getText();
        if (statementRTA.isModified()) fieldsModified = true;
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document statementDocument = statementRTA.getDocument();
        Document commentDoc = new Document();

        statementTextHeight = statementRTA.getFullHeight();
        double statementPrefHeight = Math.max(40, statementTextHeight + RTA_SCREEN_BMP);
        ABmodel model = new ABmodel(name, fields,  false, prompt, statementPrefHeight, statementDocument, commentDoc, new ArrayList<PageContent>());
        model.setStatementTextHeight(statementTextHeight);

        ABCheckSetup checkSetup = model.getCheckSetup();
        if (checkSetup == null) checkSetup = new ABCheckSetup();

        if (staticHelpRTA.isModified()) fieldsModified = true;
        staticHelpRTA.getActionFactory().saveNow().execute(new ActionEvent());
        checkSetup.setStaticHelpDoc(staticHelpRTA.getDocument());
        checkSetup.setStaticHelp(staticHelpCheck.isSelected());
        int checkMax = (Integer) checkMaxSpinner.getValue();
        checkSetup.setCheckMax(checkMax);
        checkSetup.setChoiceA(choiceABox.isSelected());
        checkSetup.setChoiceB(choiceBBox.isSelected());

        model.setCheckSetup(checkSetup);


        if (!pointsPossibleTextField.getText().equals("")) model.setPointsPossible(Integer.parseInt(pointsPossibleTextField.getText()));
        else {
            model.setPointsPossible(0);
            pointsPossibleTextField.setText("0");
        }
        model.setAuxExerName(auxExerNameField.getText());

        return model;
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

