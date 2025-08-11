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

import com.gluonhq.richtextarea.model.Document;
import com.install4j.api.launcher.ApplicationLauncher;
import com.install4j.api.update.UpdateSchedule;
import com.install4j.api.update.UpdateScheduleRegistry;
import com.license4j.License;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import slapp.editor.*;
import slapp.editor.free_form.FreeFormExercise;
import slapp.editor.front_page.FrontPageExercise;
import slapp.editor.main_window.assignment.*;
import slapp.editor.main_window.media_player.MediaViewer;
import slapp.editor.simple_edit.SimpleEditExercise;
import slapp.editor.simple_edit.SimpleEditModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import static javafx.scene.control.ButtonType.OK;

/**
 * Controller for the SLAPP main window
 */
public class MainWindow {
    private MainWindow mainWindow;
    private MainWindowView mainView;
    private Exercise currentExercise;
    private Assignment currentAssignment = null;
    private int assignmentIndex = 0;
    private boolean assignmentContentModified = false;
    private ChangeListener<Node> focusListener;
    private Node lastFocusOwner;
    private MediaViewer mediaViewer = new MediaViewer();
    private boolean isExerciseOpen = false;
    private DoubleProperty baseScale = new SimpleDoubleProperty(1.0);
    private boolean fitToPage = false;
    private WebEngine videoWebEngine;
    private WebView videoWebView;
    private Stage videoStage;
    private SlappProgData slappProgData = new SlappProgData();
    private SlappUsrData slappUsrData = new SlappUsrData();
    private boolean instructorFunctions;
    private int assignmentPointsEarned;
    private int assignmentPointsPossible;

    private EventHandler mainSceneClickFilter;




    /**
     * Create main SLAPP main window
     */
    public MainWindow(String startFileName) {

        SlappProgData progData = DiskUtilities.openProgDataFile();
        if (progData != null) {
            slappProgData = progData;
        }
        else {
            System.out.println("no prog data file");
        }

        SlappUsrData usrData = DiskUtilities.openUsrDataFile();
        if (usrData != null) {
            slappUsrData = usrData;
        }
        else {
            System.out.println("no usr data file");
        }

        mainWindow = this;
        mainView = new MainWindowView(this);
        setupMainWindow();
        mainView.setupDummyWindowRTASize();

        videoWebView = new WebView();

        focusListener = (ob, ov, nv) ->  {
            if (nv != null) {
                if (ov != lastFocusOwner) {
                    lastFocusOwner = ov;
                }
            }
        };

        mainSceneClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Platform.runLater(() -> {
                    currentExercise.handleMouseEvent(event);
                });

            }
        };
        mainView.getMainScene().addEventFilter(MouseEvent.MOUSE_PRESSED, mainSceneClickFilter);

        setUpExercise(new FrontPageExercise(this));
        mainView.updateZoom(slappUsrData.getZoom());
        mainView.getMainScene().focusOwnerProperty().addListener(focusListener);
        instructorFunctions = false;

        mainView.getInstructorCheck().setSelected(slappUsrData.isInstructorCheck());
        if (slappUsrData.isInstructorCheck()) {
            checkForInstructor();
        }

        if (slappUsrData.getRecentExercises() != null) {
            LimitedQueue<File> recents = slappUsrData.getRecentExercises();
            DiskUtilities.setRecentExerciseFiles(recents);
            updateRecentExercises();
        }
        if (slappUsrData.getRecentAssignments() != null) {
            LimitedQueue<File> recents = slappUsrData.getRecentAssignments();
            DiskUtilities.setRecentAssignmentFiles(recents);
            updateRecentAssignments();
        }
        if (!slappUsrData.getPdfPrinterName().equals("")) PrintUtilities.setPdfPrinter(slappUsrData.getPdfPrinterName());


        //install4j
        UpdateScheduleRegistry.setUpdateSchedule(UpdateSchedule.WEEKLY);
        autoOpen(startFileName);
    }


    private void checkForUpdates() {

        String os = EditorMain.os;
        String applicationID = "";
        if (os.startsWith("Win")) applicationID = "182";
        if (os.startsWith("Mac")) applicationID = "91";

        try {
            ApplicationLauncher.launchApplication(applicationID, null, true, new ApplicationLauncher.Callback() {
                public void exited(int exitValue) {    }
                public void prepareShutdown() {      }
            });
        }
        catch (IOException e) {System.err.println("check update exception" + e); }
    }


    public void autoOpen(String startFileName) {

        if (!startFileName.isEmpty()) {

            startFileName = startFileName.replace("\"", "");

            if (startFileName.endsWith(".sla")) {
                File assignmentFile = new File(startFileName);
                openAssignment(assignmentFile);
            } else if (startFileName.endsWith(".sle")) {
                File exerciseFile = new File(startFileName);
                openExercise(exerciseFile);
            } else {
                EditorAlerts.showSimpleAlert("Error", "SLAPP cannot open file of type, " + startFileName);
            }
        }
    }

    /*
     * Initialize main window controls
     */
    private void setupMainWindow() {



        mainView.scalePageHeightProperty().bind(Bindings.divide(PrintUtilities.pageHeightProperty(), mainWindow.baseScaleProperty()));
        mainView.scalePageWidthProperty().bind(Bindings.divide(PrintUtilities.pageWidthProperty(), mainWindow.baseScaleProperty()));

        mainView.getCreateNewExerciseItem().setOnAction(e -> createNewExercise());
//        mainView.getCreateNewExerciseItem().addEventFilter(KeyEvent.ANY, e -> {if (e.getCode() == KeyCode.ENTER) createNewExercise();});


        mainView.getCreateRevisedExerciseItem().setOnAction(e -> createRevisedExercise());
        mainView.getSaveExerciseItem().setOnAction(e -> saveExercise(false));
        mainView.getSaveAsExerciseItem().setOnAction(e -> saveExercise(true));
        mainView.getOpenExerciseItem().setOnAction(e -> openExercise(null));
        mainView.getClearExerciseItem().setOnAction(e -> resetExercise());
        mainView.getCloseExerciseItem().setOnAction(e -> closeExercise());
        mainView.getPrintExerciseItem().setOnAction(e -> printExercise());
        mainView.getExportToPDFExerciseItem().setOnAction(e -> exportExerciseToPDF());
        if (EditorMain.os.startsWith("Mac")) mainView.getExportToPDFExerciseItem().setDisable(true);

        mainView.getSaveAssignmentItem().setOnAction(e -> saveAssignment(false));
        mainView.getSaveAsAssignmentItem().setOnAction(e -> saveAssignment(true));
        mainView.getOpenAssignmentItem().setOnAction(e -> openAssignment(null));
        mainView.getCloseAssignmentItem().setOnAction(e -> closeAssignment());
        mainView.getPrintAssignmentItem().setOnAction(e -> printAssignment());
        mainView.getCreateRevisedAssignmentItem().setOnAction(e -> createRevisedAssignment());
        mainView.getCreateNewAssignmentItem().setOnAction(e -> createNewAssignment());
        mainView.getExportAssignmentToPDFItem().setOnAction(e -> exportAssignment());
        if (EditorMain.os.startsWith("Mac")) mainView.getExportAssignmentToPDFItem().setDisable(true);


        mainView.getPrintExerciseItemPM().setOnAction(e -> printExercise());
        mainView.getExportExerciseToPDFItemPM().setOnAction(e -> exportExerciseToPDF());
        if (EditorMain.os.startsWith("Mac")) mainView.getExportExerciseToPDFItemPM().setDisable(true);
        mainView.getPrintAssignmentItemPM().setOnAction(e -> printAssignment());
        mainView.getExportAssignmentToPDFItemPM().setOnAction(e -> exportAssignment());
        if (EditorMain.os.startsWith("Mac")) mainView.getExportAssignmentToPDFItemPM().setDisable(true);
        mainView.getPageSetupItem().setOnAction(e -> pageSetup());
        mainView.getExportSetupItem().setOnAction(e -> exportSetup());
        if (EditorMain.os.startsWith("Mac")) mainView.getExportSetupItem().setDisable(true);
        mainView.getScaleSetupItem().setOnAction(e -> scaleSetup());



        mainView.getQuickStartItem().setOnAction(e -> videoHelp("https://www.slappservices.net/quick_start/quick_start_player.html", 900, 631));
        mainView.getSlappEditorItem().setOnAction(e -> videoHelp("https://www.slappservices.net/slapp_editor/slapp_editor_player.html", 800, 677));
        mainView.getVerticalTreeItem().setOnAction(e -> videoHelp("https://www.slappservices.net/vertical_trees/vertical_trees_player.html",650, 783));
        mainView.getHorizontalTreeItem().setOnAction(e -> videoHelp("https://www.slappservices.net/horizontal_trees/horizontal_trees_player.html", 600, 835));
        mainView.getTruthTableItem().setOnAction(e -> videoHelp("https://www.slappservices.net/truth_tables/truth_tables_player.html", 600, 746));
        mainView.getDerivationItem().setOnAction(e -> videoHelp("https://www.slappservices.net/derivations/derivations_player.html", 700, 587));
        mainView.getInstructorInfoItem().setOnAction(e -> mainView.showInstructorInfo());
     //   mainView.getInstructorInfoItem().setOnAction(e -> videoHelp("https://www.slappservices.net/instructor/instructor_player.html", 900, 537));

        mainView.getCommonElementsTextItem().setOnAction(e -> generalTextHelp());
        mainView.getAboutItem().setOnAction(e -> aboutTextHelp());
        mainView.getContextualTextItem().setOnAction(e -> contextualTextHelp());
 //       mainView.getKeyboardDiagramItem().setOnAction(e -> keyboardDiagramHelp());
        mainView.getKeyboardShortcutsItem().setOnAction(e -> keyboardShortcutsTextHelp());

        mainView.getUpdateItem().setOnAction(e -> {
            saveAction();
            checkForUpdates();
        });

        mainView.getReportItem().setOnAction(e -> makeReport());
        mainView.getSaveButton().setOnAction(e -> saveAction());

        mainView.getInstructorCheck().selectedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                slappUsrData.setInstructorCheck(true);
                checkForInstructor();
            }
            else {
                slappUsrData.setInstructorCheck(false);
                instructorFunctions = false;
                mainView.setInstructorFunctions(false);
                mainView.disableExerAssItems();
            }
        });


        Label previousExerciseLabel = new Label("Previous");
        previousExerciseLabel.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                previousExercise();
            }
        });
        mainView.getPreviousExerciseMenu().setGraphic(previousExerciseLabel);



        Label nextExerciseLabel = new Label("Next");
        nextExerciseLabel.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                nextExercise();
            }
        });
        mainView.getNextExerciseMenu().setGraphic(nextExerciseLabel);

        Label goToExerciseLabel = new Label("Jump");
        goToExerciseLabel.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                goToExercise();
            }
        });
        mainView.getGoToExerciseMenu().setGraphic(goToExerciseLabel);


        Label assignmentCommentLabel = new Label("Comment");
        assignmentCommentLabel.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                assignmentComment();
            }
        });
        mainView.getAssignmentCommentMenu().setGraphic(assignmentCommentLabel);

    }

    private void updateRecentExercises() {
        Menu recentExerciseMenu = mainView.getRecentExerciseMenu();
        recentExerciseMenu.getItems().clear();
        for (int i = DiskUtilities.getRecentExerciseFiles().size() - 1; i >= 0; i--) {
            File file = DiskUtilities.getRecentExerciseFiles().get(i);
            MenuItem newMenuItem = new MenuItem(file.getAbsolutePath());
            newMenuItem.setMnemonicParsing(false);
            newMenuItem.setOnAction(e -> mainWindow.openExercise(file));
            recentExerciseMenu.getItems().add(newMenuItem);
        }
    }

    private void updateRecentAssignments() {
        Menu recentAssignmentMenu = mainView.getRecentAssignmentMenu();
        recentAssignmentMenu.getItems().clear();
        for (int i = DiskUtilities.getRecentAssignmentFiles().size() - 1; i >= 0; i--) {
            File file = DiskUtilities.getRecentAssignmentFiles().get(i);
            MenuItem newMenuItem = new MenuItem(file.getAbsolutePath());
            newMenuItem.setMnemonicParsing(false);
            newMenuItem.setOnAction(e -> mainWindow.openAssignment(file));
            recentAssignmentMenu.getItems().add(newMenuItem);
        }
    }




    /**
     * Update currentExercise, and then the mainView.
     * @param exercise the new exercise
     */
    public void setUpExercise(Exercise exercise) {
        mainView.getMainScene().focusOwnerProperty().removeListener(focusListener);
        currentExercise = exercise;
        mainView.setupExercise();
        mainView.getMainScene().focusOwnerProperty().addListener(focusListener);
    }

    /*
     * Open open window to get type, and then window to create exercise.
     */
    private void createNewExercise() {
        if (checkContinueAssignment("Confirm Create", "This assignment appears to have unsaved changes, and will be overwritten in the create process.\n\nContinue to create exercise?")) {
            if (checkContinueExercise("Confirm Create", "This exercise appears to have unsaved changes, and will be overwritten by the new one.\n\nContinue to create exercise?")) {
                if (currentExercise != null) currentExercise.clearStandingPopups();

                ExerciseType exerciseType = ExerciseTypePopup.getType(mainView);
                if (exerciseType != null) {
                    TypeSelectorFactories typeFactories = new TypeSelectorFactories(this);
                    typeFactories.createExerciseOfType(exerciseType);
                    isExerciseOpen = false;
                    currentAssignment = null;
                }
            }
        }
    }

    /*
     * Open exercise in create window for revision.  This gets blocked (at TypeSelectorFactories) if the exercise has been even partially worked.
     */
    private void createRevisedExercise() {
        if (checkContinueAssignment("Confirm Create", "This assignment appears to have unsaved changes, and will be overwritten in the create process.\n\nContinue to create exercise?")) {
            if (checkContinueExercise("Confirm Create", "This exercise appears to have unsaved changes, and will be overwritten by the new one.\n\nContinue to create exercise?")) {
                if (currentExercise != null) currentExercise.clearStandingPopups();

                Object exerciseModelObject = DiskUtilities.openExerciseModelObject();
                if (exerciseModelObject != null) {
                    updateRecentExercises();
                    TypeSelectorFactories typeFactories = new TypeSelectorFactories(this);
                    typeFactories.createRevisedExerciseFromModelObject(exerciseModelObject);
                    isExerciseOpen = false;
                    currentAssignment = null;
                }
            }
        }
    }

    /*
     * If named assignment is open, save assignment.  Else if named exercise is open save exercise.
     */
    private void saveAction(){
        if (currentAssignment != null) {
            if (!currentAssignment.getHeader().getAssignmentName().isEmpty()) {
                saveAssignment(false);
            }
        }
        else if (isExerciseOpen && currentExercise != null) {
            if (!((ExerciseModel) currentExercise.getExerciseModel()).getExerciseName().isEmpty()) {
                currentExercise.saveExercise(false);
            }
        }
   //     else EditorAlerts.fleetingRedPopup("No named assignment or exercise to save.");
    }

    /*
     * Save named exercise
     *
     * @param saveAs if true activate 'save as' else current exerrcise directory
     */
    private void saveExercise(boolean saveAs) {
        if (currentExercise != null) {
            if (!((ExerciseModel) currentExercise.getExerciseModel()).getExerciseName().isEmpty()) {
                currentExercise.saveExercise(saveAs);
            }
        }
        else EditorAlerts.fleetingRedPopup("No named exercise to save.");
    }

    /*
     * Reset current exercise to its original state
     */
    private void resetExercise() {
        Alert confirm = EditorAlerts.confirmationAlert("Confirm Reset", "This will undo all your work on this exercise.  Continue to reset?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.get() == OK) {
            if (currentExercise != null) currentExercise.clearStandingPopups();

            Exercise clearExercise = currentExercise.resetExercise();
            setUpExercise(clearExercise);
            setAuxExercise();
            if (currentAssignment != null) mainView.setUpLowerAssignmentBar();
        }
    }

    /*
     * Revert to empty simple edit exercise
     *
     * @return the empty exercise
     */
    private Exercise getEmptyExercise() {
        //revert to empty simple edit exercise
        SimpleEditModel emptyModel = new SimpleEditModel("", "");
        emptyModel.setResponsePrefHeight(400);
        SimpleEditExercise emptyExercise = new SimpleEditExercise(emptyModel, mainWindow);
        return emptyExercise;
    }

    /*
     * Close exercise that is open as such (without assignment)
     */
    public void closeExercise() {
        if (currentAssignment == null) {
            if (checkContinueExercise("Confirm Close", "This exercise appears to have unsaved changes.\n\nContinue to close exercise?")) {
                if (currentExercise != null) currentExercise.clearStandingPopups();

                setUpExercise(getEmptyExercise());
                isExerciseOpen = false;
            }
        } else {
            EditorAlerts.showSimpleAlert("Cannot Close", "There is an open assignment.  Closing the assignment closes its member exercises.");
        }
    }

    /*
     * Open exercise from disk
     */
    public void openExercise(File exerciseFile){
        Object exerciseModelObject = null;
        if (checkContinueAssignment("Confirm Open", "This assignment appears to have unsaved changes, and will be overwritten by the new exercise.  Continue to open exercise?")) {
            if (checkContinueExercise("Confirm Open", "This exercise appears to have unsaved changes, and will be overwritten by the new one.  Continue to open exercise?")) {

                if (currentExercise != null) currentExercise.clearStandingPopups();
                if (exerciseFile == null)
                    exerciseModelObject = DiskUtilities.openExerciseModelObject();
                else
                    exerciseModelObject = DiskUtilities.openExerciseObjectFromFile(exerciseFile);


                if (exerciseModelObject != null) {
                    updateRecentExercises();
                    TypeSelectorFactories typeFactories = new TypeSelectorFactories(this);
                    Exercise exercise = typeFactories.getExerciseFromModelObject(exerciseModelObject);
                    if (exercise != null) {
                        setUpExercise(exercise);
                        isExerciseOpen = true;
                        currentAssignment = null;
                    }
                }
            }
        }
    }

    /*
     * Export current exercise to PDF
     */
    //TODO this and printExercise are mostly duplicated code.  Refactor?
    private void exportExerciseToPDF() {
        if (currentExercise != null && !((ExerciseModel) currentExercise.getExerciseModel()).getExerciseName().isEmpty()) {
            boolean heightGood = true;

            //isolate printExercise from currentExercise
            ExerciseModel printModel = currentExercise.getExerciseModelFromView();
            TypeSelectorFactories typeFactory = new TypeSelectorFactories(mainWindow);
            Exercise printExercise = typeFactory.getExerciseFromModelObject(printModel);

            List<Node> printNodes = printExercise.getPrintNodes(printModel);
            PrintUtilities.resetPrintBuffer(getBaseScale());
            for (Node node : printNodes) {
                if (!PrintUtilities.processPrintNode(node) && !fitToPage) {
                    heightGood = false;
                }
            }
            if (!heightGood && !fitToPage) {
                String message = "Fit page not selected and exercise " + ((ExerciseModel) currentExercise.getExerciseModel()).getExerciseName() + " includes at least one block that takes up more than a page.  Content exceeding page bounds will be cropped.\n\n Continue export?";
                Alert confirm = EditorAlerts.confirmationAlert("Page Problem:", message);
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.get() == OK) heightGood = true;
            }
            if (heightGood) {
                if (!fitToPage) PrintUtilities.resetPrintBufferScale();
                PrintUtilities.sendBufferToPDF(null);
            }
        }
        else EditorAlerts.fleetingRedPopup("Cannot find named exercise to export.");

    }

    /*
     * Send current exercise to printer
     */
    private void printExercise() {
        if (currentExercise != null && !((ExerciseModel) currentExercise.getExerciseModel()).getExerciseName().isEmpty()) {
            boolean heightGood = true;

            //isolate printExercise from currentExercise
            ExerciseModel printModel = currentExercise.getExerciseModelFromView();
            TypeSelectorFactories typeFactory = new TypeSelectorFactories(mainWindow);
            Exercise printExercise = typeFactory.getExerciseFromModelObject(printModel);

            List<Node> printNodes = printExercise.getPrintNodes(printModel);
            PrintUtilities.resetPrintBuffer(getBaseScale());
            for (Node node : printNodes) {
                if (!PrintUtilities.processPrintNode(node) && !fitToPage) {
                    heightGood = false;
                }
            }
            if (!heightGood && !fitToPage) {
                String message = "Fit page not selected and exercise " + ((ExerciseModel) currentExercise.getExerciseModel()).getExerciseName() + " includes at least one block that takes up more than a page.  Content exceeding page bounds will be cropped.\n\n Continue to print?";
                Alert confirm = EditorAlerts.confirmationAlert("Page Problem:", message);
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.get() == OK) heightGood = true;
            }
            if (heightGood) {
                if (!fitToPage) PrintUtilities.resetPrintBufferScale();
                PrintUtilities.sendBufferToPrint(null);
            }
        }
        else EditorAlerts.fleetingRedPopup("Cannot find named exercise to print.");
    }

    /*
     * Return true if exercise is unmodified or modified and user says ok
     *
     * @param title title of confirmation box
     * @param content content of confirmation box
     * @return true if ok to continue, and otherwise false
     */
    private boolean checkContinueExercise(String title, String content) {
        boolean okContinue = true;
        if (isExerciseOpen && currentExercise.isExerciseModified()) {
            Alert confirm = EditorAlerts.confirmationAlert(title, content);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.get() != OK) okContinue = false;
        }
        return okContinue;
    }

    /*
     * Return true if assignment is unmodified or modified and user says ok
     *
     * @param title title of confirmation box
     * @param content content of confirmation box
     * @return true if ok to continue, and otherwise false
     */
    private boolean checkContinueAssignment(String title, String content) {
        boolean okContinue = true;
        if (currentAssignment != null && (currentExercise.isExerciseModified() || assignmentContentModified)) {
            Alert confirm = EditorAlerts.confirmationAlert(title, content);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.get() != OK) okContinue = false;
        }
        return okContinue;
    }

    /*
     * Run export setup
     */
    private void exportSetup() { PrintUtilities.exportSetup(); }

    /*
     * Run page setup
     */
    private void pageSetup() {
        PrintUtilities.updatePageLayout();
    }

    /*
     * Update assignment header with assignment comment
     */
    private void assignmentComment() {
        if (currentAssignment == null) {
            EditorAlerts.fleetingRedPopup("There is no open assignment on which to comment.");
        } else {

            if (currentExercise.isExerciseModified()) assignmentContentModified = true;
            ExerciseModel currentModel = currentExercise.getExerciseModelFromView();
            currentAssignment.replaceExerciseModel(assignmentIndex, currentModel);

            assignmentPointsEarned = 0;  assignmentPointsPossible = 0;
            List<ExerciseModel> exerciseModelList = currentAssignment.getExerciseModels();
            for (int i = 0; i < exerciseModelList.size(); i++) {
                ExerciseModel model = exerciseModelList.get(i);
                assignmentPointsEarned += model.getPointsEarned();
                assignmentPointsPossible += model.getPointsPossible();
            }


            AssignmentCommentWindow commentWindow = new AssignmentCommentWindow(currentAssignment.getHeader(), mainView, assignmentPointsPossible, assignmentPointsEarned);
            AssignmentHeader header = commentWindow.getAssignmentHeader();
            if (!(header.getComment().equals(new Document()))) assignmentContentModified = true;
            currentAssignment.setHeader(header);
        }
    }

    /*
     * If necessary, update assignment from current exercise and save
     *
     * @param saveAs true if activate 'save as' otherwise false
     */
    private void saveAssignment(boolean saveAs){
        if (currentAssignment == null) {
            EditorAlerts.fleetingRedPopup("There is no open assignment to save.");
        } else {
                //run later to allow derivation justifications to set ok???
         //       Platform.runLater(() -> {
                if (currentExercise.isExerciseModified())  assignmentContentModified = true;
                
                ExerciseModel model = currentExercise.getExerciseModelFromView();
                currentAssignment.replaceExerciseModel(assignmentIndex, model);
                currentExercise.setExerciseModified(false);

                currentAssignment.setBaseScale(getBaseScale());
                currentAssignment.setFitToPage(fitToPage);
                currentAssignment.setPageLayout(PrintUtilities.getPageLayout());
                boolean saved = DiskUtilities.saveAssignment(saveAs, currentAssignment);

                if (saved) assignmentContentModified = false;
         //   });
        }
    }

    /*
     * Open assignment from disk
     */
    public void openAssignment(File assignmentFile){
        Assignment assignment = null;
        if (checkContinueAssignment("Confirm Open", "The current assignment appears to have unsaved changes, and will be overwritten by the new one.\n\nContinue to open assignment?")) {
            if (checkContinueExercise("Confirm Open", "The current exercise appears to have unsaved changes, and will be overwritten by the new assignment.\n\nContinue to open assignment?")) {
                if (currentExercise != null) currentExercise.clearStandingPopups();

                isExerciseOpen = false;
                if (assignmentFile == null)
                    assignment = DiskUtilities.openAssignment();
                else
                    assignment = DiskUtilities.openAssignmentFromFile(assignmentFile);

                if (assignment != null) {
                    updateRecentAssignments();
                    if (!assignment.hasCompletedHeader()) {
                        UpdateAssignmentHeader headerUpdater = new UpdateAssignmentHeader(assignment.getHeader());
                        assignment.setHeader(headerUpdater.updateHeader());
                    }
                    if (assignment.hasCompletedHeader()) {
                        currentAssignment = assignment;
                        TypeSelectorFactories typeFactory = new TypeSelectorFactories(this);
                        assignmentIndex = 0;
                        setBaseScale(currentAssignment.getBaseScale());
                        fitToPage = currentAssignment.isFitToPage();
                        PrintUtilities.setPageLayout(currentAssignment.getPageLayout());

                        if (currentExercise != null) currentExercise.clearStandingPopups();
                        currentExercise = typeFactory.getExerciseFromModelObject(currentAssignment.getExerciseModels().get(assignmentIndex));
                        setAuxExercise();

                        mainView.setupExercise();
                        mainView.setUpLowerAssignmentBar();
                        assignmentContentModified = false;
                    }
                }
            }
        }
    }



    /*
     * Close open assignment
     */
    private void closeAssignment() {
        if (currentAssignment == null) {
            EditorAlerts.fleetingRedPopup("There is no open assignment to close.");
        } else {
            if (checkContinueAssignment("Confirm Close", "This assignment appears to have unsaved changes.\n\nContinue to close assignment?")) {

                if (currentExercise != null) currentExercise.clearStandingPopups();
                setUpExercise(getEmptyExercise());
                isExerciseOpen = false;
                currentAssignment = null;
                assignmentContentModified = false;
            }
        }
    }

    /*
     * Send open assignment to the printer
     */
    //TODO This and exportAssignment are mostly duplicated code.  Refactor?
    private void printAssignment() {
        if (currentAssignment == null) {
            EditorAlerts.fleetingRedPopup("There is no open assignment to print.");
        } else {
            mainView.activateProgressIndicator("");
            EditorAlerts.showFleetingAlert("Notice", "Processing for print." );

            boolean heightGood = true;
            List<String> badExerciseList = new ArrayList<>();
            PrintUtilities.resetPrintBuffer(getBaseScale());


            if (currentExercise.isExerciseModified()) assignmentContentModified = true;
            ExerciseModel currentModel = currentExercise.getExerciseModelFromView();
            currentAssignment.replaceExerciseModel(assignmentIndex, currentModel);

            assignmentPointsEarned = 0;  assignmentPointsPossible = 0;
            List<ExerciseModel> exerciseModelList = currentAssignment.getExerciseModels();
            for (int i = 0; i < exerciseModelList.size(); i++) {
                ExerciseModel model = exerciseModelList.get(i);

                assignmentPointsEarned += model.getPointsEarned();
                assignmentPointsPossible += model.getPointsPossible();

                TypeSelectorFactories typeFactory = new TypeSelectorFactories(this);
                Exercise exercise = typeFactory.getExerciseFromModelObject(model);
                List<Node> exerciseNodes = exercise.getPrintNodes(model);

                for (Node node : exerciseNodes) {
                    if (!PrintUtilities.processPrintNode(node) && !fitToPage) {
                        heightGood = false;
                        badExerciseList.add(((ExerciseModel) exercise.getExerciseModel()).getExerciseName());
                    }
                }
            }

            if (!badExerciseList.isEmpty()) {
                StringBuilder sb = new StringBuilder(badExerciseList.get(0));
                for (int i = 1; i < badExerciseList.size(); i++) {
                    sb.append(", ");
                    sb.append(badExerciseList.get(i));
                }
                String message = "Fit page not selected and " + sb.toString() + " include(s) at least one block that takes up more than a page.  Content exceeding page bounds will be cropped.\n\n Continue to print?";
                Alert confirm = EditorAlerts.confirmationAlert("Page Problem:", message);
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.get() == OK) heightGood = true;
            }

            PrintUtilities.setTopBox(mainView.getAssignmentHeader(assignmentPointsPossible, assignmentPointsEarned));

            if (heightGood) {
                if (!fitToPage) PrintUtilities.resetPrintBufferScale();
                String infoString = currentAssignment.getHeader().getCreationID() + "-" + currentAssignment.getHeader().getWorkingID();
                PrintUtilities.sendBufferToPrint(infoString);
            }
        }
    }

    /*
     * Send open assignment to PDF
     */
    private void exportAssignment() {
        if (currentAssignment == null) {
            EditorAlerts.fleetingRedPopup("There is no open assignment to export.");
        } else {
            mainView.activateProgressIndicator("");
            EditorAlerts.showFleetingAlert("Notice", "Processing for export." );

            boolean heightGood = true;
            List<String> badExerciseList = new ArrayList<>();
            PrintUtilities.resetPrintBuffer(getBaseScale());


            if (currentExercise.isExerciseModified()) assignmentContentModified = true;
            ExerciseModel currentModel = currentExercise.getExerciseModelFromView();
            currentAssignment.replaceExerciseModel(assignmentIndex, currentModel);

            assignmentPointsEarned = 0;  assignmentPointsPossible = 0;
            List<ExerciseModel> exerciseModelList = currentAssignment.getExerciseModels();
            for (int i = 0; i < exerciseModelList.size(); i++) {
                ExerciseModel model = exerciseModelList.get(i);
                TypeSelectorFactories typeFactory = new TypeSelectorFactories(mainWindow);
                Exercise exercise = typeFactory.getExerciseFromModelObject(model);

                List<Node> exerciseNodes = exercise.getPrintNodes(model);

                assignmentPointsEarned += model.getPointsEarned();
                assignmentPointsPossible += model.getPointsPossible();

                for (Node node : exerciseNodes) {
                    if (!PrintUtilities.processPrintNode(node) && !fitToPage) {
                        heightGood = false;
                        badExerciseList.add(((ExerciseModel) exercise.getExerciseModel()).getExerciseName());
                    }
                }
            }
            mainView.deactivateProgressIndicator();

            if (!badExerciseList.isEmpty()) {
                StringBuilder sb = new StringBuilder(badExerciseList.get(0));
                for (int i = 1; i < badExerciseList.size(); i++) {
                    sb.append(", ");
                    sb.append(badExerciseList.get(i));
                }
                String message = "Fit page not selected and " + sb.toString() + " include(s) at least one block that takes up more than a page.  Content exceeding page bounds will be cropped.\n\n Continue export?";
                Alert confirm = EditorAlerts.confirmationAlert("Page Problem:", message);
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.get() == OK) heightGood = true;
            }

            PrintUtilities.setTopBox(mainView.getAssignmentHeader(assignmentPointsPossible, assignmentPointsEarned));

            if (heightGood) {

                    if (!fitToPage) PrintUtilities.resetPrintBufferScale();
                    String infoString = currentAssignment.getHeader().getCreationID() + "-" + currentAssignment.getHeader().getWorkingID();
                    PrintUtilities.sendBufferToPDF(infoString);
            }
        }
    }

    /*
     * If assignment is not started (if it does not have a completed header), open for revision in create window
     */
    private void createRevisedAssignment() {
        if (checkContinueAssignment("Confirm Create", "The current assignment appears to have unsaved changes, and will be overwritten in the creation process.\n\nContinue to create assignment?")) {
             if (checkContinueExercise("Confirm Create", "The current exercise appears to have unsaved changes, and will be overwritten in the creation process.\n\nContinue to create assignment?")) {
                 if (currentExercise != null) currentExercise.clearStandingPopups();

                 isExerciseOpen = false;
                Assignment assignment = DiskUtilities.openAssignment();
                if (assignment != null) {
                    updateRecentAssignments();
                    if (!assignment.hasCompletedHeader()) {
                        currentAssignment = null;
                        new CreateAssignment(assignment, this);
                    } else {
                        EditorAlerts.showSimpleAlert("Cannot Modify", "This assignment appears to have been started.  Cannot modify in create window.");
                    }
                }
            }
        }
    }

    /*
     * Open window to create a new assignment
     */
    private void createNewAssignment(){
        if (checkContinueAssignment("Confirm Create", "The current assignment appears to have unsaved changes, and will be overwritten in the creation process.\n\nContinue to create assignment?")) {
            currentAssignment = null;
            if (checkContinueExercise("Confirm Create", "The current exercise appears to have unsaved changes, and will be overwritten in the creation process.\n\n Continue to create assignment?")) {
                if (currentExercise != null) currentExercise.clearStandingPopups();

                isExerciseOpen = false;
                new CreateAssignment(new Assignment(), this);
            }
        }
    }

    /*
     * Update assignment with current exercise, and move to the previous exercise
     */
    private void previousExercise() {
        if (currentAssignment == null) {
            EditorAlerts.fleetingRedPopup("Cannot Advance.  There is no open assignment.");
        } else {
            if (currentExercise.isExerciseModified()) assignmentContentModified = true;
            int prevIndex = assignmentIndex - 1;
            if (prevIndex >= 0) {
                if (currentExercise != null) currentExercise.clearStandingPopups();

                ExerciseModel model = currentExercise.getExerciseModelFromView();
                currentAssignment.replaceExerciseModel(assignmentIndex, model);

                assignmentIndex = prevIndex;
                TypeSelectorFactories typeFactory = new TypeSelectorFactories(this);

                currentExercise = typeFactory.getExerciseFromModelObject(currentAssignment.getExerciseModels().get(assignmentIndex));
                setAuxExercise();
                mainView.setupExercise();
                mainView.setUpLowerAssignmentBar();
            }
        }
    }

    /*
     * Update assignment with current exercise, and move to next
     */
    private void nextExercise() {
        if (currentAssignment == null) {
            EditorAlerts.fleetingRedPopup("Cannot Advance.  There is no open assignment.");
        } else {
            if (currentExercise.isExerciseModified()) {assignmentContentModified = true; }
            int nextIndex = assignmentIndex + 1;
            if (nextIndex < currentAssignment.getExerciseModels().size()) {
                if (currentExercise != null) currentExercise.clearStandingPopups();

                ExerciseModel model = currentExercise.getExerciseModelFromView();
                currentAssignment.replaceExerciseModel(assignmentIndex, model);
                assignmentIndex = nextIndex;
                TypeSelectorFactories typeFactory = new TypeSelectorFactories(this);
                currentExercise = typeFactory.getExerciseFromModelObject(currentAssignment.getExerciseModels().get(assignmentIndex));
                setAuxExercise();
                mainView.setupExercise();
                mainView.setUpLowerAssignmentBar();
            }
        }
     }

    /*
     * Open popup for user to select an exercise from assignment.  Save current exercise to assignment and go to selected.
     */
    private void goToExercise() {
        if (currentAssignment == null) {
            EditorAlerts.fleetingRedPopup("Cannot Jump.  There is no open assignment.");
        } else {
            if (currentExercise.isExerciseModified()) assignmentContentModified = true;

            Popup exercisePopup = new Popup();
            ListView exerciseList = new ListView();
            exerciseList.setPadding(new Insets(5));
            exerciseList.setStyle("-fx-border-color: white; -fx-background-color: white; -fx-border-width: 0");

            exerciseList.getItems().addAll(currentAssignment.getExerciseModels());

            exerciseList.getSelectionModel().select(assignmentIndex);

            exerciseList.setCellFactory(new Callback<ListView<ExerciseModel>, ListCell<ExerciseModel>>() {
                public ListCell<ExerciseModel> call(ListView<ExerciseModel> param) {
                    return new ListCell<ExerciseModel>() {
                        @Override
                        protected void updateItem(ExerciseModel item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(item == null || empty ? null : (exerciseList.getItems().indexOf(item) + 1) + ".  " + item);
                        }
                    };
                }
            });
            exerciseList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ExerciseModel>() {
                @Override
                public void changed(ObservableValue ob, ExerciseModel ov, ExerciseModel nv) {
                    if (nv != null) {
                        if (currentExercise != null) currentExercise.clearStandingPopups();

                        ExerciseModel model = currentExercise.getExerciseModelFromView();
                        currentAssignment.replaceExerciseModel(assignmentIndex, model);
                        assignmentIndex = exerciseList.getItems().indexOf(nv);
                        TypeSelectorFactories typeFactory = new TypeSelectorFactories(mainWindow);
                        currentExercise = typeFactory.getExerciseFromModelObject(currentAssignment.getExerciseModels().get(assignmentIndex));
                        setAuxExercise();
                        mainView.setupExercise();
                        mainView.setUpLowerAssignmentBar();
                        exercisePopup.hide();
                    }
                }
            });

            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> exercisePopup.hide());

            HBox buttonBox = new HBox(closeButton);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setStyle("-fx-background-color: white; -fx-border-width: 1 0 0 0; -fx-border-color: lightblue;");

            VBox jumpBox = new VBox(0, exerciseList, buttonBox);
            exercisePopup.getContent().add(jumpBox);
            jumpBox.setStyle("-fx-border-color: lightblue; -fx-border-width: 5; -fx-opacity: 1.0");
            exercisePopup.show(EditorMain.mainStage);
        }
    }

    private void setAuxExercise() {
        if (currentAssignment != null) {
            List<ExerciseModel> exerModels = currentAssignment.getExerciseModels();
            for (ExerciseModel exerModel : exerModels) {
                if (exerModel.getExerciseName().equals(currentExercise.getAuxExerName())) {
                    ExerciseModel auxModel = exerModel;
                    TypeSelectorFactories typeFactory = new TypeSelectorFactories(this);
                    Exercise auxExerA = typeFactory.getExerciseFromModelObject(auxModel);
                    currentExercise.setAuxExerA(auxExerA);
                    Exercise auxExerB = typeFactory.getExerciseFromModelObject(auxModel);
                    currentExercise.setAuxExerB(auxExerB);
                }
            }
        }
    }

    /*
     * Open popup for user to select baseScale and/or fit to page
     */
    private void scaleSetup() {
        Popup scaleSetupPopup = new Popup();

        Button okButton = new Button("OK");

        CheckBox fitToPageCheck = new CheckBox("Fit to Page");
        fitToPageCheck.setTooltip(new Tooltip("Fit oversize nodes to page"));
        fitToPageCheck.setPrefHeight(24);
        fitToPageCheck.setSelected(fitToPage);

        int spinnerInitialValue = (int) Math.round(getBaseScale() * 100);
        Spinner<Integer> baseScaleSpinner = new Spinner(50, 150, spinnerInitialValue, 1);
        baseScaleSpinner.setTooltip(new Tooltip("Print at selected percentage of full size"));
        baseScaleSpinner.setPrefWidth(75);
        Label baseScaleLabel = new Label("Base Scale");
        HBox spinnerBox = new HBox(10, baseScaleSpinner, baseScaleLabel);
        VBox choicesBox = new VBox(15, spinnerBox, fitToPageCheck);
        choicesBox.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(okButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10,10,0,10));
        buttonBox.setStyle("-fx-background-color: white; -fx-border-width: 1 0 0 0; -fx-border-color: lightblue;");

        VBox mainBox = new VBox(10, choicesBox, buttonBox);
        scaleSetupPopup.getContent().add(mainBox);
        mainBox.setStyle("-fx-background-color: white; -fx-border-color: lightblue; -fx-border-width: 5; -fx-opacity: 1.0");
        mainBox.setPadding(new Insets(10));
        scaleSetupPopup.show(EditorMain.mainStage);

        okButton.setOnAction(e -> {
            setBaseScale(baseScaleSpinner.getValue() / 100.0);
            fitToPage = fitToPageCheck.isSelected();
            scaleSetupPopup.hide();
        });

    }

    /**
     * True if exercise/assignment not modified, or if modified and user selects ok
     *
     * @return true if ok to continue
     */
    boolean checkCloseWindow() {
        boolean continueClose = false;
        if (checkContinueAssignment("Confirm Close", "The current assignment appears to have unsaved changes.\n\nContinue to close?")) {
            if (checkContinueExercise("Confirm Close", "The current exercise appears to have unsaved changes.\n\nContinue to close exercise?")) {
                continueClose = true;
            }
        }
        return continueClose;
    }

    /*
     * Open "about" text help popup
     */
    private void aboutTextHelp() {
        TextHelpPopup.helpAbout();
    }

    private void keyboardShortcutsTextHelp() {TextHelpPopup.helpKeyboardShortcut(); }

    /*
     * Open help video
     * @param urlString - video location
     * @param width - widith of view window
     * @param height - height of view window
     */

    /*
    private void videoHelp(String urlString, double width, double height) {
        if (mediaViewer == null) mediaViewer = new MediaViewer();
        else mediaViewer.stopPlay();
        mediaViewer.play(urlString, width, height);
    }
     */

    public void videoHelp(String urlString, double width, double height) {
        closeVideoHelp();

        videoWebEngine = videoWebView.getEngine();
        videoWebEngine.setUserStyleSheetLocation("data:, body {font: 16px Noto Serif Combo; }");

        videoWebEngine.getLoadWorker().stateProperty().addListener((ob, ov, nv) -> {
            if (nv == Worker.State.FAILED) {
                EditorAlerts.showSimpleAlert("Connection Error", "Error connecting to https://www.slappservices.net.");
                closeVideoHelp();
            }
        });

        videoWebEngine.load(urlString);

        VBox root = new VBox(videoWebView);
        root.setVgrow(videoWebView, Priority.ALWAYS);
        Scene scene = new Scene(root);

        videoStage = new Stage();
        videoStage.setScene(scene);
        videoStage.setTitle("SLAPP Video Help");
        videoStage.initModality(Modality.NONE);
        videoStage.getIcons().addAll(EditorMain.icons);
//        stage.initOwner(EditorMain.mainStage);
        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        videoStage.setX(Math.max(5, Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - (width + 20))));
        videoStage.setY(Math.max(5, Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - (height + 20))));
        videoStage.setWidth(width);
        videoStage.setHeight(height);

        videoStage.setOnCloseRequest(e -> { closeVideoHelp(); });

        videoStage.show();
    }

    void closeVideoHelp() {
        if (videoWebEngine != null) videoWebEngine.load(null);
        if (videoStage != null) videoStage.close();
    }

    /*
     * Open 'general info' text help item
     */
    private void generalTextHelp() {
        TextHelpPopup.helpCommonElements();
    }

    /*
     * Depending on current exercise, open contextual help
     */
    private void contextualTextHelp() {
        ExerciseModel model = ((ExerciseModel) currentExercise.getExerciseModel());
        if (model.getExerciseType() == ExerciseType.FREE_FORM) {
            Exercise activeExer = ((FreeFormExercise) currentExercise).getActiveExercise();
            model = ((ExerciseModel) activeExer.getExerciseModel());
        }
        TextHelpPopup.helpContextual(model.getExerciseType());
    }


    private void checkForInstructor() {

        License license = License.getInstance();

        license.getBuilder()
              .product("8B4A2B871E73E19C931F2D599719FFB7")
              .build();

        license.validate();
        if (license.getStatus().isValid()) {
            instructorFunctions = true;
            mainView.enableExerAssItems();
        }
        else {
            mainView.getInstructorCheck().setSelected(false);
            instructorFunctions = false;
            slappUsrData.setInstructorCheck(false);
            EditorAlerts.fleetingRedPopup("No Valid license.  Valid license required for instructor functions.");
        }
        mainView.setInstructorFunctions(instructorFunctions);
    }

    public boolean isInstructorFunctions() {
        return instructorFunctions;
    }

    public void setInstructorFunctions(boolean instructorFunctions) {
        this.instructorFunctions = instructorFunctions;
    }

    public SlappProgData getSlappProgData() {
        return slappProgData;
    }

    public SlappUsrData getSlappUsrData() {
        return slappUsrData;
    }

    /*
     * Open native mail client to report an issue
     */
    private void makeReport() {
        String message = "Please be as specific as you can about your concern; if you are reporting an error, include information about the version of SLAPP and of your operating system, along with (if possible) the relevant file and whether and how the problem may be repeated (ok to delete this line).";
        MailHelper.generate("messaging@slappservices.net", "SLAPP: (your issue)", message);
    }

    MediaViewer getMediaViewer() {     return mediaViewer;  }

    /**
     * The MainWindowView is the main SLAPP window
     *
     * @return the mainView
     */
    public MainWindowView getMainView() { return mainView; }

    /**
     * The current assignment is the assignment (if any) currently open
     *
     * @return the current assignment
     */
    public Assignment getCurrentAssignment() { return currentAssignment; }

    /**
     * The current exercise is the exercise currently open (whether or not it is part of an assignment)
     *
     * @return the current exercise
     */
    public Exercise getCurrentExercise() {    return currentExercise;  }

    /**
     * The assignment index is the position of the current exercise in the assignment's list of ExerciseModels
     * @return
     */
    public int getAssignmentIndex() {     return assignmentIndex;   }

    /**
     * The last focus owner.  Required when a control depends on the particular item with focus before the
     * control was selected.
     *
     * @return last focussed node
     */
    public Node getLastFocusOwner() {     return lastFocusOwner;   }

    /**
     * The base scale is the scale at which nodes are exported or printed (unless 'fit to page' is selected).
     *
     * @return the base scale (1.0 = 100% of normal)
     */
    public double getBaseScale() { return baseScale.get(); }

    /**
     * The base scale is the scale at which nodes are exported or printed (unless 'fit to page' is selected).
     *
     * @return the base scale property (1.0 = 100% of normal)
     */
    public DoubleProperty baseScaleProperty() {   return baseScale; }

    /**
     * The base scale is the scale at which nodes are exported or printed (unless 'fit to page' is selected).
     *
     * @param baseScale the base scale value (1.0 = 100% of normal)
     */
    public void setBaseScale(double baseScale) { this.baseScale.set(baseScale); }
}
