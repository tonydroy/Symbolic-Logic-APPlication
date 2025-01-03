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

package slapp.editor.main_window;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.*;
import slapp.editor.EditorMain;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.decorated_rta.KeyboardDiagram;
import slapp.editor.main_window.assignment.AssignmentHeader;
import slapp.editor.main_window.assignment.AssignmentHeaderItem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * View for the main SLAPP window.  View has border pane with menu bar and toolbars on top, exercise content in
 * center, the main exercise control on the left, supplementary exercise control on the right (in the current
 * version, just for the free-form exercise), and footer information at the bottom.
 */
public class MainWindowView {
    private Stage stage = EditorMain.mainStage;
    private MainWindow mainWindow;
    private ToolBar editToolbar = new ToolBar();
    private ToolBar fontsToolbar = new ToolBar();
    private ToolBar paragraphToolbar = new ToolBar();
    private ToolBar insertToolbar = new ToolBar();
    private ToolBar kbdDiaToolBar = new ToolBar();
    private ToolBar kbdSelectorToolBar = new ToolBar();
    private ToolBar sizeToolBar = new ToolBar();
    private double scale = 1.0;
    MenuBar menuBar;
    private VBox topBox = new VBox();
    private Pane spacerPane;
    private ScrollPane centerPane;
    private StackPane centerStackPane;
    private VBox centerBox;
    private Spinner<Integer> zoomSpinner;
    private Label zoomLabel;
    double minStageWidth = 860.0;   //870
    private BorderPane borderPane = new BorderPane();
    private Scene mainScene;
    private ExerciseView currentExerciseView;
    private Node statementNode;
    private Node contentNode;
    private DecoratedRTA commentDecoratedRTA;
    private DecoratedRTA lastFocusedDRTA;
    private DecoratedRTA dummyDRTA = new DecoratedRTA();
    private Node commentNode;
    private Node leftControlNode;
    private Node rightControlNode;
    private VBox statusBar;
    private HBox upperStatusBox;
    private FlowPane lowerStatusPane;
    private Button saveButton;
    private Spinner<Double> horizontalSizeSpinner;
    private Spinner<Double> verticalSizeSpinner;
    private ChangeListener verticalListener;
    private ChangeListener horizontalListener;
    private RichTextArea dummyRTA;

    private Group dummyRoot;
    private Scene dummyScene;
    private Stage dummyStage;

    private RichTextAreaSkin dummyRTASkin;
    private MenuItem createNewExerciseItem = new MenuItem("Create New");
    private MenuItem createRevisedExerciseItem = new MenuItem("Create Revised");
    private MenuItem saveExerciseItem = new MenuItem("Save");
    private MenuItem saveAsExerciseItem = new MenuItem("Save As");
    private MenuItem openExerciseItem = new MenuItem("Open");
    private MenuItem printExerciseItem = new MenuItem("Print");
    private MenuItem exportToPDFExerciseItem = new MenuItem("Export to PDF");
    private MenuItem clearExerciseItem = new MenuItem("Reset");
    private MenuItem closeExerciseItem = new MenuItem("Close");
    private MenuItem saveAssignmentItem = new MenuItem("Save");
    private MenuItem saveAsAssignmentItem = new MenuItem("Save As");
    private MenuItem openAssignmentItem = new MenuItem("Open");
    private MenuItem closeAssignmentItem = new MenuItem("Close");
    private MenuItem printAssignmentItem = new MenuItem("Print");
    private MenuItem exportAssignmentToPDFItem = new MenuItem("Export to PDF");
    private MenuItem createNewAssignmentItem = new MenuItem("Create New");
    private MenuItem createRevisedAssignmentItem = new MenuItem("Create Revised");
    private MenuItem exportExerciseToPDFItemPM = new MenuItem("Export Exercise");
    private MenuItem printExerciseItemPM = new MenuItem("Print Exercise");
    private MenuItem printAssignmentItemPM = new MenuItem("Print Assignment");
    private MenuItem exportAssignmentToPDFItemPM = new MenuItem("Export Assignment");
    private MenuItem pageSetupItem = new MenuItem("Page Setup");
    private MenuItem exportSetupItem = new MenuItem("Export Setup");
    private MenuItem scaleSetupItem = new MenuItem("Scale Setup");
    private MenuItem commonElementsTextItem;
    private MenuItem contextualTextItem;
    private MenuItem aboutItem;
    private MenuItem quickStartItem;
    private MenuItem slappEditorItem;
    private MenuItem verticalTreeItem;
    private MenuItem horizontalTreeItem;
    private MenuItem truthTableItem;
    private MenuItem derivationItem;
//    private MenuItem keyboardDiagramItem;
    private MenuItem keyboardShortcutsItem;
    private MenuItem instructorInfoItem;
    private MenuItem reportItem;

    private MenuItem updateItem;

    Menu previousExerciseMenu = new Menu();
    Menu nextExerciseMenu = new Menu();
    Menu goToExerciseMenu = new Menu();
    Menu assignmentCommentMenu = new Menu();
    private static Label progressLabel;
    public static ProgressIndicator progressIndicator;
    public static TextField txtHeightIndicator;
    DoubleProperty scalePageHeight = new SimpleDoubleProperty();
    DoubleProperty scalePageWidth = new SimpleDoubleProperty();

    HBox paraToolbarBox;
    HBox fontsAndEditBox;


    /**
     * Create the main window view
     *
      * @param mainWindow the main window controller
     */
    public MainWindowView(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setupWindow();
    }


    private void setupWindow() {
        //The dummy window is used for getting height of RTA text.
        setUpDummyWindow();


        //menu bars

        Menu assignmentMenu = new Menu("Assignment");
        Menu exerciseMenu = new Menu("Exercise");
        Menu printMenu = new Menu("Print");
        Menu helpMenu = new Menu("Help");

        Text filmItemGraphic0 = new Text("\uf008");
        filmItemGraphic0.setStyle("-fx-font-family: la-solid-900; ");
        quickStartItem = new MenuItem("Intro/Quick Start", filmItemGraphic0);
        Text filmItemGraphic1 = new Text("\uf008");
        filmItemGraphic1.setStyle("-fx-font-family: la-solid-900");
        slappEditorItem = new MenuItem("SLAPP editor", filmItemGraphic1);
        Text filmItemGraphic2 = new Text("\uf008");
        filmItemGraphic2.setStyle("-fx-font-family: la-solid-900");
        verticalTreeItem = new MenuItem("Vertical Trees", filmItemGraphic2);
        Text filmItemGraphic3 = new Text("\uf008");
        filmItemGraphic3.setStyle("-fx-font-family: la-solid-900");
        horizontalTreeItem = new MenuItem("Horizontal Trees", filmItemGraphic3);
        Text filmItemGraphic4 = new Text("\uf008");
        filmItemGraphic4.setStyle("-fx-font-family: la-solid-900");
        truthTableItem = new MenuItem("Truth Tables", filmItemGraphic4);
        Text filmItemGraphic5 = new Text("\uf008");
        filmItemGraphic5.setStyle("-fx-font-family: la-solid-900");
        derivationItem = new MenuItem("Derivations", filmItemGraphic5);
        Text filmItemGraphic6 = new Text("\uf008");
        filmItemGraphic6.setStyle("-fx-font-family: la-solid-900");
        instructorInfoItem = new MenuItem("Instructor Info", filmItemGraphic6);


        Text textItemGraphic1 = new Text("\uf15c");
        textItemGraphic1.setStyle("-fx-font-family: la-solid-900");
        commonElementsTextItem = new MenuItem("General Info", textItemGraphic1);
        Text textItemGraphic2 = new Text("\uf15c");
        textItemGraphic2.setStyle("-fx-font-family: la-solid-900");
        contextualTextItem = new MenuItem("Contextual", textItemGraphic2);


        Text textItemGraphic5 = new Text("\uf15c");
        textItemGraphic5.setStyle("-fx-font-family: la-solid-900");
        keyboardShortcutsItem = new MenuItem("Keyboard Shortcuts", textItemGraphic5);

        Text textItemGraphic3 = new Text("\uf15c");
        textItemGraphic3.setStyle("-fx-font-family: la-solid-900");
        aboutItem = new MenuItem("About", textItemGraphic3);


        Text checkItemGraphic1 = new Text("\uf00c");
        checkItemGraphic1.setStyle("-fx-font-family: la-solid-900");
        updateItem = new MenuItem("Check for Updates", checkItemGraphic1);


        Text reportItemGraphic = new Text("\uf4ad");
        reportItemGraphic.setStyle("-fx-font-family: la-solid-900");
        reportItem = new MenuItem("Comment/Report", reportItemGraphic);



        menuBar = new MenuBar(exerciseMenu, assignmentMenu, previousExerciseMenu, nextExerciseMenu, goToExerciseMenu, assignmentCommentMenu, printMenu, helpMenu);
        exerciseMenu.getItems().addAll(saveExerciseItem, saveAsExerciseItem, openExerciseItem, clearExerciseItem, closeExerciseItem, printExerciseItem, exportToPDFExerciseItem, createRevisedExerciseItem, createNewExerciseItem);
        assignmentMenu.getItems().addAll(saveAssignmentItem, saveAsAssignmentItem, openAssignmentItem, closeAssignmentItem, printAssignmentItem, exportAssignmentToPDFItem, createRevisedAssignmentItem, createNewAssignmentItem);
        printMenu.getItems().addAll(printExerciseItemPM, exportExerciseToPDFItemPM, printAssignmentItemPM, exportAssignmentToPDFItemPM, exportSetupItem, pageSetupItem, scaleSetupItem);
        helpMenu.getItems().addAll(quickStartItem, slappEditorItem, verticalTreeItem, truthTableItem, horizontalTreeItem, derivationItem, instructorInfoItem, commonElementsTextItem, contextualTextItem, keyboardShortcutsItem, aboutItem, updateItem, reportItem);
/*
        if (EditorMain.os.startsWith("Mac")) {
            menuBar.setUseSystemMenuBar(true);
        }
 */

        //toolbar items

        zoomLabel = new Label(" Zoom ");
        zoomSpinner = new Spinner(25, 500, 100, 5);
        zoomSpinner.setPrefSize(65,25);
        zoomSpinner.setTooltip(new Tooltip("Window zoom as percentage of normal"));
        zoomSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = zoomSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = zoomSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
            updateZoom(nv);
        });

        saveButton = new Button("\uf0c7");  //LineAwesome.SAVE
        saveButton.getStyleClass().add("lasolid-icon");
        saveButton.setTooltip(new Tooltip("Save assignment if an assignment is open, and otherwise save open exercise"));

        if (EditorMain.secondaryCopy) {
            saveButton.setDisable(true);
            saveExerciseItem.setDisable(true);
            saveAsExerciseItem.setDisable(true);
            saveAssignmentItem.setDisable(true);
            saveAsAssignmentItem.setDisable(true);
        }

        horizontalSizeSpinner = new Spinner<>(0.0, 999.0, 0.0, 5.0);
        horizontalSizeSpinner.setPrefWidth(65);
        horizontalSizeSpinner.setTooltip(new Tooltip("Width as % of selected paper"));
        horizontalSizeSpinner.setDisable(true);

        verticalSizeSpinner = new Spinner<>(0.0, 999.0, 0.0, 5.0);
        verticalSizeSpinner.setPrefWidth(65);
        verticalSizeSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        verticalSizeSpinner.setDisable(true);

        txtHeightIndicator = new TextField("100");
        txtHeightIndicator.setPrefWidth(40);
        txtHeightIndicator.setDisable(true);

        progressLabel = new Label("");
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefWidth(25);
        progressIndicator.setPrefHeight(25);
        progressIndicator.setVisible(false);

        sizeToolBar.setStyle("-fx-spacing: 10");
        sizeToolBar.getItems().addAll(zoomLabel, zoomSpinner, new Label(" T Ht:"), txtHeightIndicator,  new Label("V Sz:"), verticalSizeSpinner,
                new Label("H Sz:"), horizontalSizeSpinner, saveButton, progressIndicator);
        sizeToolBar.setPrefHeight(38);


        //center

        centerBox = new VBox();
        centerBox.setSpacing(3);

        /*
        The natural way to have scrolling (together with the ability to scale) is to have content including the RTA wrapped
        in a group and then the group in a scroll pane.  This causes exceptions in the RTA:

        When the centerPane has the new Group(centerHBox) as member, zoom works as one would expect - the scroll pane
        responds to the layout bounds of the scaled group.  But, in this case, RTA crashes two ways: Typing any char
        results in ConcurrentModificationException (from ParagraphTile 352). And if the window is dragged with
        HSize Win checked there is a NullPointerException (from ParagraphTile 202).  There are no exceptions when
        the scroll pane has just center box -- but then zoom doesn't generate properly scroll bars.

        So: Take a group containing a 'dummy' pane whose height and width are bound to those of content including the RTA as a
        member; put both the group and the content onto an AnchorPane, and the anchor pane into the scroll pane.  Scaling
        applies equally to the dummy pane and to the content. Then the scroll pane can go into the center of a border pane
        or whatever.  This seems to work!
     */

        spacerPane = new Pane();
        spacerPane.prefHeightProperty().bind(centerBox.heightProperty());
        spacerPane.prefWidthProperty().bind(centerBox.widthProperty());
        Group group = new Group(spacerPane);
        AnchorPane comboPane = new AnchorPane(group, centerBox);

        centerPane = new ScrollPane(comboPane);

        borderPane.setCenter(centerPane);
        borderPane.setMargin(centerPane, new Insets(10,0,0,0));
        borderPane.getCenter().setStyle("-fx-background-color: transparent");

        //bottom

        statusBar = new VBox(5);
        upperStatusBox = new HBox(40);
        lowerStatusPane = new FlowPane();
        lowerStatusPane.setHgap(40);
        statusBar.setPadding(new Insets(0,20,20,20));

        statusBar.getChildren().addAll(upperStatusBox, lowerStatusPane);
        borderPane.setBottom(statusBar);

        //sides

        borderPane.setLeft(leftControlNode);
        borderPane.setRight(rightControlNode);

        //window

        mainScene = new Scene(borderPane);
        mainScene.getStylesheets().add(DecoratedRTA.class.getClassLoader().getResource("slappEditor.css").toExternalForm());

        stage.setScene(mainScene);
        stage.setTitle("SLAPP Editor");
        stage.setMinWidth(minStageWidth);
 //       stage.setWidth(minStageWidth);

        Rectangle2D mainBounds = Screen.getPrimary().getVisualBounds();
        double mainWindowX = Math.max(0.0, (mainBounds.getMaxX() - mainBounds.getMinX())/8);
        double mainWindowY = 40;
        stage.setX(mainWindowX);
        stage.setY(mainWindowY);

        stage.setHeight(860); //this added to keep SlappLogoView on screen (adjust with actual logo) ok in general? 800
        stage.setOnCloseRequest(e -> {
            e.consume();
            closeWindow();
        });

        stage.show();
    }

    /**
     * Set the size of the dummy window as a function of the scale and paper size
     */
    void setupDummyWindowRTASize() {
        dummyRTA.prefHeightProperty().bind(Bindings.multiply(scalePageHeight, 5.0));
        dummyRTA.prefWidthProperty().bind(scalePageWidth);
    }

    /*
     * The dummy window contains an RTA upon which text may be "measured"
     */
    private void setUpDummyWindow() {
        dummyRTA = new RichTextArea(EditorMain.mainStage);
        dummyRTA.getStylesheets().add("slappTextArea.css");



        dummyRoot = new Group();
        dummyRoot.getChildren().addAll(dummyRTA);
        dummyScene = new Scene(dummyRoot);
        dummyStage = new Stage();
        dummyStage.setScene(dummyScene);

        dummyStage.initStyle(StageStyle.UTILITY);
        dummyStage.initOwner(EditorMain.mainStage);
        dummyStage.getIcons().add(new Image(EditorMain.class.getResourceAsStream("/icon32x32.png")));
        dummyStage.getIcons().add(new Image(EditorMain.class.getResourceAsStream("/icon16x16.png")));

        dummyStage.toBack();
        dummyStage.setOnCloseRequest(e -> {e.consume();});
        dummyStage.setOpacity(0);
        dummyStage.show();
        dummyRTASkin = ((RichTextAreaSkin) dummyRTA.getSkin());
    }

    /**
     * Use the dummy window to extract RTA text height
     *
     * @param rta the RichTextArea
     *
     * @return height of text in RTA
     */
    public double getRTATextHeight(RichTextArea rta) {
        rta.getActionFactory().saveNow().execute(new ActionEvent());
        Document doc = rta.getDocument();
        dummyRTA.getActionFactory().open(doc).execute(new ActionEvent());
        dummyRoot.applyCss();
        dummyRoot.layout();
        double height = dummyRTASkin.getComputedHeight();
        double scaledHeight = 5.0 +  100 * (height / scalePageHeight.get());
        String strHeight = String.valueOf(Math.round(scaledHeight));
        txtHeightIndicator.setText(strHeight);
        return height;
    }

    /**
     * Set (reset) the left control of the border pane
     *
     * @param leftControl the left control node for an exercise
     */
    public void setUpLeftControl(Node leftControl) {
        borderPane.setLeft(leftControl);
    }



    public static Rectangle2D getCurrentScreenBounds() {
        Screen screen = Screen.getPrimary();
        for (Screen s : Screen.getScreensForRectangle(EditorMain.mainStage.getX(), EditorMain.mainStage.getY(), EditorMain.mainStage.getWidth(), EditorMain.mainStage.getHeight())) {
            if (s.getBounds().getMinX() > screen.getBounds().getMinX()) screen = s;
        }
        return screen.getVisualBounds();
    }


    /**
     * Set an exercise on the main view
     */
    public void setupExercise() {
        this.currentExerciseView = (ExerciseView) mainWindow.getCurrentExercise().getExerciseView();
        this.statementNode = currentExerciseView.getExerciseStatementNode();
        this.contentNode = currentExerciseView.getExerciseContentNode();
        this.commentDecoratedRTA = currentExerciseView.getExerciseComment();
        this.commentNode = commentDecoratedRTA.getEditor();

        this.leftControlNode = currentExerciseView.getExerciseControl();
        this.rightControlNode = currentExerciseView.getRightControl();

        centerBox.getChildren().clear();
        centerBox.getChildren().addAll(commentNode, statementNode, contentNode);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        upperStatusBox.getChildren().clear();
        lowerStatusPane.getChildren().clear();

        upperStatusBox.getChildren().add(new Label("Exercise: " + ((ExerciseModel) mainWindow.getCurrentExercise().getExerciseModel()).getExerciseName()));
        lowerStatusPane.getChildren().add(new Label("Date: " + dtf.format(LocalDateTime.now())));

        borderPane.setLeft(leftControlNode);
        borderPane.setRight(rightControlNode);

        centerBox.layout();
        Platform.runLater(() -> contentNode.requestFocus());
    }

    /**
     * Udate (reset) size spinners for active window
     *
     * @param height the height spinner
     * @param width the width spinner
     */
    public void updateSizeSpinners(Spinner<Double> height, Spinner<Double> width) {
        sizeToolBar.getItems().remove(5);
        sizeToolBar.getItems().add(5, height);
        sizeToolBar.getItems().remove(7);
        sizeToolBar.getItems().add(7, width);
    }

    /**
     * Deactivate (hide) the progress indicator
     */
    public static void deactivateProgressIndicator() {
        progressLabel.setVisible(false);
        progressIndicator.setVisible(false);
    }

    /**
     * Activate (show) the progress indicator
     *
     * @param text to indicate progress item
     */
    public static void activateProgressIndicator(String text) {
        progressLabel.setText(text);
        progressIndicator.setVisible(true);
        progressLabel.setVisible(true);
    }

    /**
     * Update lower assignment bar based on current exercise and/or assignment
     */
    void setUpLowerAssignmentBar() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        upperStatusBox.getChildren().clear();
        lowerStatusPane.getChildren().clear();

        AssignmentHeader header = mainWindow.getCurrentAssignment().getHeader();
        int exerciseNum = mainWindow.getAssignmentIndex() + 1;

        upperStatusBox.getChildren().addAll(new Label("Student Name: " + header.getStudentName()),
                new Label("Assignment: " + header.getAssignmentName()),
                new Label("Exercise: " + ((ExerciseModel) mainWindow.getCurrentExercise().getExerciseModel()).getExerciseName() + " (" + exerciseNum + "/" + mainWindow.getCurrentAssignment().getExerciseModels().size() + ")"),
                new Label("ID: " + header.getCreationID() + "-" + header.getWorkingID()) );
        lowerStatusPane.getChildren().add(new Label("Date: " + dtf.format(LocalDateTime.now())));
        for (int i = 0; i < header.getInstructorItems().size(); i++) {
            AssignmentHeaderItem headerItem = header.getInstructorItems().get(i);
            lowerStatusPane.getChildren().add(new Label(headerItem.getLabel() + ": " + headerItem.getValue()));
        }
        for (int i = 0; i < header.getStudentItems().size(); i++) {
            AssignmentHeaderItem headerItem = header.getStudentItems().get(i);
            lowerStatusPane.getChildren().add(new Label( headerItem.getLabel() + ": " + headerItem.getValue()));
        }
    }


    /*
     * Apply zoom setting
     *
     * @param zoom integer percentage
     */
    private void updateZoom(int zoom) {
        scale = (double)zoom/100.0;

        KeyboardDiagram keyboardDiagram = KeyboardDiagram.getInstance();
        keyboardDiagram.updateFontSize(scale);
        keyboardDiagram.initialize(lastFocusedDRTA);
        keyboardDiagram.update();

        centerBox.getTransforms().clear();
        centerBox.getTransforms().add(new Scale(scale, scale));
        spacerPane.getTransforms().clear();
        spacerPane.getTransforms().add(new Scale(scale, scale));
    }


    /**
     * Set toolbars with controls for currently focused RTA
     *
     * @param decoratedRTA the focused RTA
     * @param control The {@link slapp.editor.main_window.ControlType} for this RTA instance
     */
    public void editorInFocus(DecoratedRTA decoratedRTA, ControlType control) {

        lastFocusedDRTA = decoratedRTA;
        KeyboardDiagram keyboardDiagram = KeyboardDiagram.getInstance();
        keyboardDiagram.initialize(decoratedRTA);
        if (keyboardDiagram.isShowing()) {
            keyboardDiagram.updateAndShow();
        }

        paragraphToolbar = decoratedRTA.getParagraphToolbar();
        paragraphToolbar.setMinWidth(minStageWidth - 15);

        paraToolbarBox = new HBox(paragraphToolbar);
        HBox.setHgrow(paragraphToolbar, Priority.ALWAYS);

        fontsToolbar = decoratedRTA.getFontsToolbar();
        fontsToolbar.setMinWidth(520);


        editToolbar = decoratedRTA.getEditToolbar();
        editToolbar.setMinWidth(300);

        kbdSelectorToolBar = decoratedRTA.getKbdSelectorToolbar();
        kbdDiaToolBar = decoratedRTA.getKbdDiaToolbar();


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


        fontsAndEditBox = new HBox(fontsToolbar, editToolbar);
        HBox kbdBox = new HBox(kbdSelectorToolBar, kbdDiaToolBar, sizeToolBar);

        fontsAndEditBox.setHgrow(editToolbar, Priority.ALWAYS);
        kbdBox.setHgrow(sizeToolBar, Priority.ALWAYS);

        topBox = new VBox(menuBar, paraToolbarBox, fontsAndEditBox, kbdBox);

   //     topBox = new VBox(menuBar, paragraphToolbar, fontsAndEditBox, kbdBox);
        topBox.layout();

        borderPane.topProperty().setValue(topBox);
    }

    /**
     * If a non-RTA text field is in focus, disable all but the kbdDiaToolBar
     */
    public void textFieldInFocus() {
            editorInFocus(dummyDRTA, ControlType.STATEMENT);
    }

    /*
     * Close the main window view
     */
    private void closeWindow() {
        if (mainWindow.checkCloseWindow()) {
            KeyboardDiagram.getInstance().close();
            dummyStage.close();
            mainWindow.closeVideoHelp();
//            if (mainWindow.getMediaViewer() != null) {  mainWindow.getMediaViewer().stopPlay();    }
            stage.close();
            System.exit(0);
        }
    }

    /**
     * Get print/export assignment header
     *
     * @return VBox header node
     */
    VBox getAssignmentHeader() {
        VBox headerBox = new VBox(10);
        AssignmentHeader header = mainWindow.getCurrentAssignment().getHeader();

        Label studentNameLabel = new Label(header.getStudentName());
        studentNameLabel.setStyle("-fx-font-weight: bold;");
        HBox nameBox = new HBox(studentNameLabel);
        nameBox.setAlignment(Pos.CENTER);

        VBox leftBox = new VBox(0);
        leftBox.getChildren().add(new Label("Assignment: " + header.getAssignmentName()));
        for (AssignmentHeaderItem item : header.getStudentItems()) {
            leftBox.getChildren().add(new Label(item.getLabel() + ": " + item.getValue()));
        }
        leftBox.setAlignment(Pos.TOP_LEFT);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        VBox rightBox = new VBox(0);
        rightBox.getChildren().add(new Label("Date: " + dtf.format(LocalDateTime.now())));
        for (AssignmentHeaderItem item : header.getInstructorItems()) {
            rightBox.getChildren().add(new Label(item.getLabel() + ": " + item.getValue()));
        }
        rightBox.setAlignment(Pos.TOP_LEFT);

        Region spacer = new Region();
        HBox itemsBox = new HBox(leftBox, spacer, rightBox);
        itemsBox.setHgrow(spacer, Priority.ALWAYS);

        Separator separator = new Separator();
        separator.setOrientation(Orientation.HORIZONTAL);

        RichTextArea commentArea = new RichTextArea(EditorMain.mainStage);
        commentArea.getActionFactory().open(header.getComment()).execute(new ActionEvent());
        commentArea.setContentAreaWidth(PrintUtilities.getPageWidth());
        commentArea.setPrefWidth(PrintUtilities.getPageWidth());
        commentArea.setPrefHeight(header.getCommentTextHeight() + 35);

        headerBox.getChildren().addAll(nameBox,itemsBox, separator, commentArea );
        headerBox.setPadding(new Insets(0,0,20,0));
        return headerBox;
    }


     /**
     * Button to save exercise/assignment
     *
     * @return the button
     */
    Button getSaveButton() {
        return saveButton;
    }

    /**
     * Menu item to save current exercise
     *
     * @return the menu item
     */
    MenuItem getSaveExerciseItem() {
        return saveExerciseItem;
    }

    /**
     * Menu item to save current exercise as
     *
     * @return the menu item
     */
    MenuItem getSaveAsExerciseItem() {
        return saveAsExerciseItem;
    }

    /**
     * Menu item to save current assignment
     *
     * @return the menu item
     */
    MenuItem getSaveAssignmentItem() {
        return saveAssignmentItem;
    }

    /**
     * Menu item to save current assignment as
     *
     * @return the menu item
     */
    MenuItem getSaveAsAssignmentItem() {
        return saveAsAssignmentItem;
    }

    /**
     * Menu item to open an exercise
     *
     * @return the menu item
     */
    MenuItem getOpenExerciseItem() {
        return openExerciseItem;
    }

    /**
     * Menu item to create a new assignment
     *
     * @return the menu item
     */
    MenuItem getCreateNewAssignmentItem() {
        return createNewAssignmentItem;
    }

    /**
     * Menu item to open an assignment
     *
     * @return the menu item
     */
    MenuItem getOpenAssignmentItem() {
        return openAssignmentItem;
    }

    /**
     * Menu item to clear an exercise
     *
     * @return the menu item
     */
    MenuItem getClearExerciseItem() {
        return clearExerciseItem;
    }

    /**
     * Menu item to create a new exercise
     *
     * @return the menu item
     */
    MenuItem getCreateNewExerciseItem() {
        return createNewExerciseItem;
    }

    /**
     * Menu item to create a revised exercise
     *
     * @return the menu item
     */
    MenuItem getCreateRevisedExerciseItem() {
        return createRevisedExerciseItem;
    }

    /**
     * Menu item to print the current exercise
     *
     * @return the menu item
     */
    MenuItem getPrintExerciseItem() {
        return printExerciseItem;
    }

    /**
     * Menu item to export current exercise to PDF
     *
     * @return the menu item
     */
    MenuItem getExportToPDFExerciseItem() {
        return exportToPDFExerciseItem;
    }

    /**
     * Menu item to close current exercise
     *
     * @return the menu item
     */
    MenuItem getCloseExerciseItem() {
        return closeExerciseItem;
    }

    /**
     * Menu item to open page setup window
     *
     * @return the menu item
     */
    MenuItem getPageSetupItem() {
        return pageSetupItem;
    }

    /**
     * Menu item to print exercise (from print menu)
     *
     * @return the menu item
     */
    MenuItem getPrintExerciseItemPM() {
        return printExerciseItemPM;
    }

    /**
     * Menu item to export exercise to PDF (from print menu)
     *
     * @return the menu item
     */
    MenuItem getExportExerciseToPDFItemPM() {
        return exportExerciseToPDFItemPM;
    }

    /**
     * Menu item to setup the export printer
     *
     * @return the menu item
     */
    MenuItem getExportSetupItem() {
        return exportSetupItem;
    }

    /**
     * Menu item to open print scale popup
     *
     * @return the menu item
     */
    MenuItem getScaleSetupItem() {
        return scaleSetupItem;
    }

    /**
     * Menu item to close current assignment
     *
     * @return the menu item
     */
    MenuItem getCloseAssignmentItem() {
        return closeAssignmentItem;
    }

    /**
     * Menu item to print current assignment
     *
     * @return the menu item
     */
    MenuItem getPrintAssignmentItem() {
        return printAssignmentItem;
    }

    /**
     * Menu item to create revise assignment
     *
     * @return the menu item
     */
    MenuItem getCreateRevisedAssignmentItem() {
        return createRevisedAssignmentItem;
    }

    /**
     * Menu item to export current assignment to PDF
     *
     * @return the menu item
     */
    MenuItem getExportAssignmentToPDFItem() {
        return exportAssignmentToPDFItem;
    }

    /**
     * Menu item to print current assignment (from print menu)
     *
     * @return the menu item
     */
    MenuItem getPrintAssignmentItemPM() {
        return printAssignmentItemPM;
    }

    /**
     * Menu item to
     *
     * @return the menu item export current assignment to PDF (from print menu)
     */
    MenuItem getExportAssignmentToPDFItemPM() {
        return exportAssignmentToPDFItemPM;
    }

    /**
     * Menu item to open 'general info' help item
     *
     * @return the menu item
     */
    MenuItem getCommonElementsTextItem() {
        return commonElementsTextItem;
    }

    /*
    MenuItem getKeyboardDiagramItem() {
        return keyboardDiagramItem;
    }
    
     */

    MenuItem getKeyboardShortcutsItem() {
        return keyboardShortcutsItem;
    }

    /**
     * Menu item to
     *
     * @return the menu item open contextual help
     */
    MenuItem getContextualTextItem() {
        return contextualTextItem;
    }


    MenuItem getUpdateItem() {return updateItem; }


    /**
     * Menu item to open 'about' help window
     *
     * @return the menu item
     */
    MenuItem getAboutItem() {
        return aboutItem;
    }

    /**
     * Menu item to open quick start viedeo
     *
     * @return the menu item
     */
    MenuItem getQuickStartItem() {return quickStartItem; }

    /**
     * Menu item to open the SLAPP editor help video
     *
     * @return the menu item
     */
    MenuItem getSlappEditorItem() {
        return slappEditorItem;
    }

    /**
     * Menu item to open the vertical tree help video
     *
     * @return the menu item
     */
    MenuItem getVerticalTreeItem() {
        return verticalTreeItem;
    }

    /**
     * Menu item to open the horizontal tree help video
     *
     * @return the menu item
     */
    MenuItem getHorizontalTreeItem() {
        return horizontalTreeItem;
    }

    /**
     * Menu item to open the truth table help video
     *
     * @return the menu item
     */
    MenuItem getTruthTableItem() {
        return truthTableItem;
    }

    /**
     * Menu item to open the derivation help video
     *
     * @return the menu item
     */
    MenuItem getDerivationItem() {
        return derivationItem;
    }

    /**
     * Menu item to open the instructor info help video
     *
     * @return the menu item
     */
    MenuItem getInstructorInfoItem() { return instructorInfoItem; }

    /**
     * Menu item to activate the 'comment/report' item
     *
     * @return the menu item
     */
    MenuItem getReportItem() {
        return reportItem;
    }

    /**
     * Empty menu to move to previous exercise
     *
     * @return the menu
     */
    Menu getPreviousExerciseMenu() {
        return previousExerciseMenu;
    }

    /**
     * Empty menu to move to next exercise
     *
     * @return the menu
     */
    Menu getNextExerciseMenu() {
        return nextExerciseMenu;
    }

    /**
     * Empty menu to jump to exercise
     *
     * @return the menu
     */
    Menu getGoToExerciseMenu() {
        return goToExerciseMenu;
    }

    /**
     * Empty menu to open assignment comment window
     *
     * @return the menu
     */
    Menu getAssignmentCommentMenu() {
        return assignmentCommentMenu;
    }

    /**
     * Scene for the main window view
     *
     * @return tghe main scene
     */

    public Scene getMainScene() {
        return mainScene;
    }

    /**
     * Minimum width for the main window view
     *
     * @return the width
     */
    public double getMinStageWidth() {
        return minStageWidth;
    }

    /**
     * The scale page height is a function of selected paper and scale
     *
     * @return the height value
     */
    public double getScalePageHeight() {   return scalePageHeight.get(); }

    /**
     * The scale page height is a function of selected paper and scale
     *
     * @return the height property
     */
    public DoubleProperty scalePageHeightProperty() {   return scalePageHeight;  }

    /**
     * The scale page width is a function of selected paper and scale
     *
     * @return the width value
     */
    public double getScalePageWidth() {   return scalePageWidth.get();   }

    /**
     * The scale page width is a function of selected paper and scale
     *
     * @return the scale property
     */
    public DoubleProperty scalePageWidthProperty() {    return scalePageWidth;  }

    DecoratedRTA getLastFocusedDRTA() {
        return lastFocusedDRTA;
    }

    public VBox getTopBox() {return topBox;}

    public HBox getParaToolbarBox() { return paraToolbarBox; }

    public HBox getFontsAndEditBox() {return fontsAndEditBox; }

    public Spinner getZoomSpinner() {return zoomSpinner; }




}
