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

package slapp.editor.trans_uline;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
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

import java.util.*;
import java.util.function.UnaryOperator;

import static javafx.scene.control.ButtonType.OK;
import static slapp.editor.EditorMain.RTA_SCREEN_BMP;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;
import static slapp.editor.trans_uline.TransULCheck.processULTree;

public class TransULCreate implements ULBoxContainer {
    private MainWindow mainWindow;
    private TextField nameField;
    private DecoratedRTA statementDRTA;
    private RichTextArea statementRTA;
    private double statementTextHeight;
    private ChangeListener nameListener;
    private ChangeListener auxExerListener;
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
    private Spinner<Double> staticHelpHeightSpinner;
    private RichTextArea currentSpinnerNode;
    private TextField auxExerNameField;
    private SimpleULineBox ulineBox;

    private final DoubleProperty localScalePageHeight = new SimpleDoubleProperty(1.0);
    private final DoubleProperty localScalePageWidth = new SimpleDoubleProperty(1.0);

    public TransULCreate(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        localScalePageHeight.bind(mainWindow.getMainView().scalePageHeightProperty());
        localScalePageWidth.bind(mainWindow.getMainView().scalePageWidthProperty());

        setupWindow();
    }

    public TransULCreate(MainWindow mainWindow, TransULModel originalModel) {
        this(mainWindow);
        nameField.setText(originalModel.getExerciseName());
        auxExerNameField.setText(originalModel.getAuxExerName());
        pointsPossibleTextField.setText(Integer.toString(originalModel.getPointsPossible()));

        ulineBox.setupBox(originalModel.getKeyModel());



        statementRTA.getActionFactory().open(originalModel.getExerciseStatement()).execute(new ActionEvent());
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        statementTextHeight = originalModel.getStatementTextHeight();

        ULcheckSetup checkSetup = originalModel.getCheckSetup();
        if (checkSetup == null) checkSetup = new ULcheckSetup();
        staticHelpCheck.setSelected(checkSetup.isStaticHelp());
        staticHelpRTA.getActionFactory().open(checkSetup.getStaticHelpDoc()).execute(new ActionEvent());
        staticHelpRTA.getActionFactory().saveNow().execute(new ActionEvent());
        checkMaxSpinner.getValueFactory().setValue(checkSetup.getCheckMax());

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


        HBox textBoxes = new HBox(10, statementRTA, staticHelpRTA);

        //name
        Label nameLabel = new Label("Exercise Name: ");
        nameLabel.setPrefWidth(100);
        nameField  = new TextField();
        nameField.setPromptText("(plain text)");
        nameField.setPrefWidth(150);
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

        Label auxExerLabel = new Label("Aux Exer Name: ");
        auxExerLabel.setPrefWidth(100);
        auxExerNameField = new TextField();
        auxExerNameField.setPadding(new Insets(5));
        auxExerNameField.setPromptText("(plain text)");
        auxExerNameField.setPrefWidth(150);
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
        HBox auxExerNameBox = new HBox(auxExerLabel, auxExerNameField);
        auxExerNameBox.setAlignment(Pos.BASELINE_LEFT);

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







        //sentence key
        ulineBox = new SimpleULineBox(this);
   //     ulineBox.getFormulaBox().getBoxedRTA().setMinWidth(800);

        DecoratedRTA ulineDRTA = ulineBox.getFormulaBox().getDRTA();
        RichTextArea ulineRTA = ulineDRTA.getEditor();
        ulineRTA.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            modified = true;
        });
        ulineRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editorInFocus(ulineDRTA, ControlType.FIELD);
            }
        });

        VBox ulineVBox = new VBox(ulineBox);
        ulineVBox.setPadding(new Insets(20,10, 0, 0));

        ulineVBox.setMinHeight(70);
        ulineVBox.setAlignment(Pos.TOP_LEFT);

        ulineVBox.setStyle("-fx-background-color: rgb(252,252,252)");  //azure

        Label ulineLable = new Label("Sentence/Key: ");
        HBox ulineHBox = new HBox(20, ulineLable, ulineVBox);
        ulineHBox.setPadding(new Insets(20, 0, 20, 10));
        ulineHBox.setAlignment(Pos.CENTER_LEFT);



        HBox checkLine1 = new HBox(30, staticHelpBox, checkMaxBox);
        checkLine1.setAlignment(Pos.CENTER_LEFT);

        fieldsBox = new VBox(10, nameBox, auxExerNameBox, pointsBox, checkLine1, ulineHBox);


        //center

        String header = TextHelpPopup.getHeader14();
        String footer = TextHelpPopup.getFooter();
        String helpText = header +
                "<p>Translate Underline is a sort of exercise found in <i>Symbolic Logic</i> as preparatory to sentential translation -- it asks students to identify ordinary sentences as simple or compound and, in the latter case, to identify the scope of sentential operators by underlines, and then to state the main operator.</p>" +
                "<ul>" +
                "<li><p>Start with the exercise name, auxiliary exercise (if desired), and points fields.  If 'points possible' is other than zero, a points field is added to the exercise comment area (and one for total assignment points into the assignment comment area).</p></li>" +
                "<li><p>The static help check activates the Static Help button which pops up a message which you may state in the right hand text area below. " +
                "If CheckMax is 0 checking is disabled; if -1 checks are unlimited; otherwise the value sets the maximum number of allowable check tries.</li></p>" +
                "<li><p>Then provide the sentence to be evaluated and, if checking is enabled, underlines to constitute an answer key.  " +
                "Information about whether the sentence is simple or compound and about the main operator is derived from the lines.</p>" +
                "<p>Underlines work just as for students (and vertical trees): F10 puts a marker at the cursor position, move the cursor and F10 again puts another marker, then F11 adds an underline whose leftmost point is at the left marker and rightmost point at the right marker.  Right-click removes the underlines and any markers.   " +
                "A student will not be able to modify the sentence your provide - so its sentential operator structure should be explicit.  </p></li>" +
                "<li><p>Finally provide the exercise statement and, if desired the static help message.</p></li>" +
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
    //    stage.initOwner(EditorMain.mainStage);
        stage.setScene(scene);
        stage.setTitle("Create Translate Underline Exercise:");
        stage.getIcons().addAll(EditorMain.icons);

        stage.setWidth(1040);

        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        double stageHeight = Math.min(1000, bounds.getHeight() - 20);
        stage.setHeight(stageHeight);
        double mainWinY = EditorMain.mainStage.getY();
        if (mainWinY + stageHeight + 20 < bounds.getMaxY()) {
            stage.setY(mainWinY + 10);
        }
        else {
            stage.setY(bounds.getMaxY() - (stageHeight + 10));
        }
        stage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 1040));

   //     stage.initModality(Modality.WINDOW_MODAL);
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

    private ULTreeObj extractULTreeObj() {
        return null;
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
        if (modified || statementRTA.isModified() || staticHelpRTA.isModified()) {
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
            auxExerNameField.clear();
            ulineBox.getFormulaBox().getRTA().getActionFactory().newDocumentNow().execute(new ActionEvent());
            TransULCheck.setEmptyOK(true);
            viewExercise();
            TransULCheck.setEmptyOK(false);
            modified = false;
        }
    }
    private void viewExercise() {
        TransULModel model = extractModelFromWindow();
        TransULExercise exercise = new TransULExercise(model, mainWindow);
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

        TransULModel model = extractModelFromWindow();
        DiskUtilities.saveExercise(saveAs, model);

        saveButton.setDisable(false);
        saveAsButton.setDisable(false);
        modified = false;
    }



    private TransULModel extractModelFromWindow() {
        TransULModel model = new TransULModel();
        model.setExerciseName(nameField.getText());
        model.setAuxExerName(auxExerNameField.getText());


        if (statementRTA.isModified()) modified = true;
        statementRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseStatement(statementRTA.getDocument());

        statementTextHeight = statementRTA.getFullHeight();
        model.setStatementTextHeight(statementTextHeight);
        model.setStatementPrefHeight(Math.max(40, statementTextHeight + RTA_SCREEN_BMP));

        if (!pointsPossibleTextField.getText().equals("")) model.setPointsPossible(Integer.parseInt(pointsPossibleTextField.getText()));
        else {
            model.setPointsPossible(0);
            pointsPossibleTextField.setText("0");
        }

        model.setKeyModel(ulineBox.getTreeObjects());

        List<ULTreeObj> treeObjs = new ArrayList<>();
        String text = ulineBox.getTreeObjects().get(0).getText();
        ULTreeObj rootObj = new ULTreeObj(-1.0, -1.0, 0, text.length(), -1);
        rootObj.setText(text);
        rootObj.setRoot(true);
        treeObjs.add(0, rootObj);
        model.setMainModel(treeObjs);


        ULcheckSetup checkSetup = model.getCheckSetup();
        if (checkSetup == null) checkSetup = new ULcheckSetup();

        if (staticHelpRTA.isModified()) modified = true;
        staticHelpRTA.getActionFactory().saveNow().execute(new ActionEvent());
        checkSetup.setStaticHelpDoc(staticHelpRTA.getDocument());


        checkSetup.setStaticHelp(staticHelpCheck.isSelected());
        int checkMax = (Integer) checkMaxSpinner.getValue();
        checkSetup.setCheckMax(checkMax);

        ULTreeObj keyObj = null;
        if (checkMax != 0) {
            keyObj = processULTree(ulineBox.getTreeObjects());
            if (keyObj != null) checkSetup.setExerKeyTreeObj(keyObj);
        }
        if (keyObj == null) checkSetup.setExerKeyTreeObj(rootObj);
        model.setUlBoxPrintWidth(ulineBox.getFormulaBox().getRTA().getPrefWidth());


        //checks
        boolean good = true;
        if (checkMax != 0) {
            if (keyObj == null) {
                good = false;
                EditorAlerts.fleetingRedPopup("Cannot process underline structure for key.  Check disabled.");
            }

            if (!good) {
                checkMaxSpinner.getValueFactory().setValue(0);
                checkSetup.setCheckMax(0);
            }
        }
        return model;
    }

    @Override  //ULBoxContainer
    public void editorInFocus(DecoratedRTA decoratedRTA, ControlType control) {

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

    public MainWindowView getMainView() {return mainWindow.getMainView();}

    @Override  //ULBoxContainer
    public void pushUndoRedo() {}

    @Override //ULBoxContainer
    public void setExerciseModified(boolean modified) {
        this.modified = modified;
    }

    @Override //ULBoxContainer
    public DoubleProperty getScalePageWidth() {
        return localScalePageWidth;
    }
}
