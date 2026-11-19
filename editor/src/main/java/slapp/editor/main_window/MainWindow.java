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




import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.navigation.event.NavigationFinished;
import com.teamdev.jxbrowser.net.*;
import com.teamdev.jxbrowser.view.javafx.BrowserView;

import com.teamdev.jxbrowser.net.callback.InterceptUrlRequestCallback;
import com.teamdev.jxbrowser.navigation.callback.StartNavigationCallback;

import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.event.Subscription;
import com.teamdev.jxbrowser.navigation.event.LoadFinished;
import com.teamdev.jxbrowser.view.javafx.BrowserView;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.util.Duration;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.List;


import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.print.PageLayout;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import slapp.editor.*;
import slapp.editor.free_form.FreeFormExercise;
import slapp.editor.front_page.FrontPageExercise;
import slapp.editor.main_window.assignment.*;
//import slapp.editor.simpleTests.EncodeDecode;
//import slapp.editor.simpleTests.Encoder;
//import slapp.editor.simpleTests.Encoder;
import slapp.editor.simple_edit.SimpleEditExercise;
import slapp.editor.simple_edit.SimpleEditModel;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Optional;


import static com.teamdev.jxbrowser.engine.RenderingMode.OFF_SCREEN;
import static javafx.scene.control.ButtonType.OK;
import static slapp.editor.EditorMain.mainStage;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;





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
    private boolean isExerciseOpen = false;
    private DoubleProperty baseScale = new SimpleDoubleProperty(1.0);
    private boolean fitToPage = false;
    public static Engine engine;
    private Browser videoBrowser;
    private Browser createHlpBrowser;
    private Browser textHlpBrowser;

    private BrowserView view;

    private static Stage videoStage;

    private SlappProgData slappProgData = new SlappProgData();
    private SlappUsrData slappUsrData = new SlappUsrData();
    private boolean instructorFunctions;
    private int assignmentPointsEarned;
    private int assignmentPointsPossible;

    private EventHandler mainSceneClickFilter;

    private final static int GCM_IV_LENGTH = 12;
    String keyString = "no key";

    private static String styleHeader;
    private static String headTop;

    // ---> MEMORY LEAK FIX: Declare popups globally so we don't recreate them 100 times.
    private Popup jumpPopup;
    private ListView<ExerciseModel> jumpListView;
    private boolean ignoreJumpListener = false;
    private Popup scaleSetupPopup;
    private CheckBox fitToPageCheck;
    private Spinner<Integer> baseScaleSpinner;

    // Define a flag somewhere in your class (set to false for production releases)
    private static final boolean DEBUG_MEMORY_LEAKS = false;

    private List<Exercise> pendingPrintCleanups = new ArrayList<>();


    /**
     * Create main SLAPP main window
     */
    public MainWindow(String startFileName) {

        SlappProgData progData = DiskUtilities.openProgDataFile();
        if (progData != null) {
            slappProgData = progData;
        } else {
            System.out.println("no prog data file");
        }

        SlappUsrData usrData = DiskUtilities.openUsrDataFile();
        if (usrData != null) {
            slappUsrData = usrData;
        } else {
            System.out.println("no usr data file");
        }

        mainWindow = this;

        //get JxBrowser key
        try {
            keyString = decrypt(slappProgData.getJxcipher(), slappProgData.getJxsecretKey(), slappProgData.getJxdata());
        } catch (Exception e) {
            EditorAlerts.fleetingRedPopup("Failed to obtain JxBrowser key.");
        }

        //re-encode JxBrowserkey
        encoder();

        // ---> REMOVED jxBrowserInit() from here so it doesn't race with UI startup

        focusListener = (ob, ov, nv) -> {
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

        instructorFunctions = false;

        //messy code to show splash screen while verifying license
        if (slappUsrData.isInstructorCheck()) {
            Label label = new Label("Verifying Instructor License . . .");
            label.setAlignment(Pos.CENTER);
            label.setFont(new Font("NotoSeriefCombo", 18));
            VBox splashBox = new VBox(label);
            splashBox.setStyle("-fx-background-color: whitesmoke");  //ghostwhite
            splashBox.setAlignment(Pos.CENTER);
            Scene scene = new Scene(splashBox);
            mainStage.setScene(scene);
            mainView = new MainWindowView(this);
            mainView.initializeStage();

            mainStage.setOnShown(e -> {
                PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
                delay.setOnFinished(event -> {
                    checkForInstructor();
                    mainView.setupWindow();
                    startMainView();
                    setupMainWindow();
                    mainView.getInstructorCheck().setSelected(slappUsrData.isInstructorCheck());
                    setUpExercise(new FrontPageExercise(this));
                    mainView.updateZoom(slappUsrData.getZoom());

                    Platform.runLater(() -> {
                        autoOpen(startFileName);
                        jxBrowserInit();
                    });

                });
                delay.play();
            });
            mainStage.show();
        }
        else {
            mainView = new MainWindowView(this);
            mainView.setupWindow();
            setupMainWindow();
            mainView.getInstructorCheck().setSelected(slappUsrData.isInstructorCheck());
            startMainView();
            mainView.initializeStage();
            mainStage.show();
            setUpExercise(new FrontPageExercise(this));
            mainView.updateZoom(slappUsrData.getZoom());

            autoOpen(startFileName);
            jxBrowserInit();
        }

        Platform.runLater(() -> {
            PrintUtilities printUtilities = new PrintUtilities();
            if (!slappUsrData.getPdfPrinterName().equals(""))
                PrintUtilities.setPdfPrinter(slappUsrData.getPdfPrinterName());

     //       UpdateScheduleRegistry.setUpdateSchedule(UpdateSchedule.WEEKLY);  this stops the crash but file never opens with instructor checked
      //      autoOpen(startFileName);


        });


        //install4j
        UpdateScheduleRegistry.setUpdateSchedule(UpdateSchedule.WEEKLY);   //WEEKLY  //ON_EVERY_START  //DAILY
//        autoOpen(startFileName);
    }

    private void startMainView() {
        mainView.getMainScene().addEventFilter(MouseEvent.MOUSE_PRESSED, mainSceneClickFilter);
        mainView.getInstructorCheck().setSelected(slappUsrData.isInstructorCheck());
        mainView.getMainScene().focusOwnerProperty().addListener(focusListener);

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
    }

    private String decrypt(byte[] cipherMessage, SecretKey secretKey, byte[] associatedData) throws Exception {
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        AlgorithmParameterSpec gcmIv = new GCMParameterSpec(128, cipherMessage, 0, GCM_IV_LENGTH);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmIv);

        if (associatedData != null) {
            cipher.updateAAD(associatedData);
        }
        byte[] plainText = cipher.doFinal(cipherMessage, GCM_IV_LENGTH, cipherMessage.length - GCM_IV_LENGTH);

        return new String(plainText, StandardCharsets.UTF_8);
    }

    private void encoder() {
        return;        //out for set code otherwise in

   /*     in set code otherwise out
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[16];
        secureRandom.nextBytes(key);
        slappProgData.setJxsecretKey(new SecretKeySpec(key, "AES"));
        slappProgData.setJxdata("ProtocolVersion1".getBytes(StandardCharsets.UTF_8));

        Encoder encoder = new Encoder();

        try {
            slappProgData.setJxcipher(encoder.encrypt(slappProgData.getJxsecretKey(), slappProgData.getJxdata()));
        }
        catch (Exception e) {
            EditorAlerts.fleetingRedPopup("Could not encode JxKey.");
        }

    */

    }

    private void jxBrowserInit() {
        // 1. Force MacFontFinder_getFontData to complete synchronously on Thread 0
        //    before any JxBrowser native libraries are loaded.
        Font.getFamilies();

        // 2. Wait briefly for initial stage rendering, PrintUtilities, and autoOpen
        //    to settle so Thread 0 is idle when Chromium loads libtoolkit.dylib.
        PauseTransition browserStartDelay = new PauseTransition(Duration.millis(300));
        browserStartDelay.setOnFinished(ev -> {
            Task<Void> engineInitTask = new Task<>() {
                @Override
                protected Void call() {
                    try {
                        InterceptUrlRequestCallback fontInterceptor = params -> {
                            String url = params.urlRequest().url();

                            if (url.endsWith("/fonts/NotoSerifCombo-Regular.ttf")) {
                                try (InputStream inputStream = getClass().getResourceAsStream("/fonts/NotoSerifCombo-Regular.ttf")) {
                                    if (inputStream != null) {
                                        byte[] fontBytes = inputStream.readAllBytes();
                                        UrlRequestJob job = params.newUrlRequestJob(
                                                UrlRequestJob.Options.newBuilder(HttpStatus.OK)
                                                        .addHttpHeader(HttpHeader.of("Content-Type", "font/ttf"))
                                                        .addHttpHeader(HttpHeader.of("Access-Control-Allow-Origin", "*"))
                                                        .build()
                                        );
                                        job.write(fontBytes);
                                        job.complete();
                                        return InterceptUrlRequestCallback.Response.intercept(job);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            return InterceptUrlRequestCallback.Response.proceed();
                        };

                        EngineOptions options = EngineOptions.newBuilder(OFF_SCREEN)
                                .licenseKey(keyString)
                                .addSwitch("--disable-features=NativeNotifications")
                                .addScheme(Scheme.of("https"), fontInterceptor)
                                .addScheme(Scheme.of("http"), fontInterceptor)
                                .build();

                        engine = Engine.newInstance(options);

                        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                            if (engine != null && !engine.isClosed()) {
                                engine.close();
                            }
                        }, "JxBrowser-Shutdown-Hook"));

                        videoBrowser = engine.newBrowser();
                        textHlpBrowser = engine.newBrowser();
                        createHlpBrowser = engine.newBrowser();

                        videoBrowser.navigation().loadHtml("<html><body style='background-color:black;margin:0;'></body></html>");

                        textHlpBrowser.navigation().set(StartNavigationCallback.class, params -> {
                            String url = params.url();
                            if (url.startsWith("http") && !url.contains("local")) {
                                try {
                                    if (java.awt.Desktop.isDesktopSupported()) {
                                        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return StartNavigationCallback.Response.ignore();
                            }
                            return StartNavigationCallback.Response.start();
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> EditorAlerts.fleetingRedPopup("Failed to initialize browser."));
                    }
                    return null;
                }
            };
            Thread initThread = new Thread(engineInitTask, "JxBrowser-Init-Thread");
            initThread.setDaemon(true);
            initThread.start();
        });
        browserStartDelay.play();
    }








    private void checkForUpdates() {
        String os = EditorMain.os;
        String applicationID = "";
        if (os.startsWith("win")) applicationID = "182";
        if (os.startsWith("mac")) applicationID = "91";

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

    private void setupMainWindow() {
        mainView.scalePageHeightProperty().bind(Bindings.divide(PrintUtilities.pageHeightProperty(), mainWindow.baseScaleProperty()));
        mainView.scalePageWidthProperty().bind(Bindings.divide(PrintUtilities.pageWidthProperty(), mainWindow.baseScaleProperty()));


        mainView.getCreateNewExerciseItem().setOnAction(e -> createNewExercise());
        mainView.getCreateRevisedExerciseItem().setOnAction(e -> createRevisedExercise());
        mainView.getSaveExerciseItem().setOnAction(e -> saveExercise(false));
        mainView.getSaveAsExerciseItem().setOnAction(e -> saveExercise(true));
        mainView.getOpenExerciseItem().setOnAction(e -> openExercise(null));
        mainView.getClearExerciseItem().setOnAction(e -> resetExercise());
        mainView.getCloseExerciseItem().setOnAction(e -> closeExercise());
        mainView.getPrintExerciseItem().setOnAction(e -> printExercise());
        mainView.getExportToPDFExerciseItem().setOnAction(e -> exportExerciseToPDF());
        if (EditorMain.os.startsWith("mac")) mainView.getExportToPDFExerciseItem().setDisable(true);

        mainView.getSaveAssignmentItem().setOnAction(e -> saveAssignment(false));
        mainView.getSaveAsAssignmentItem().setOnAction(e -> saveAssignment(true));
        mainView.getOpenAssignmentItem().setOnAction(e -> openAssignment(null));
        mainView.getCloseAssignmentItem().setOnAction(e -> closeAssignment());
        mainView.getPrintAssignmentItem().setOnAction(e -> printAssignment());
        mainView.getCreateRevisedAssignmentItem().setOnAction(e -> createRevisedAssignment());
        mainView.getCreateNewAssignmentItem().setOnAction(e -> createNewAssignment());
        mainView.getExportAssignmentToPDFItem().setOnAction(e -> exportAssignment());
        if (EditorMain.os.startsWith("mac")) mainView.getExportAssignmentToPDFItem().setDisable(true);


        mainView.getPrintExerciseItemPM().setOnAction(e -> printExercise());
        mainView.getExportExerciseToPDFItemPM().setOnAction(e -> exportExerciseToPDF());
        if (EditorMain.os.startsWith("mac")) mainView.getExportExerciseToPDFItemPM().setDisable(true);
        mainView.getPrintAssignmentItemPM().setOnAction(e -> printAssignment());
        mainView.getExportAssignmentToPDFItemPM().setOnAction(e -> exportAssignment());
        if (EditorMain.os.startsWith("mac")) mainView.getExportAssignmentToPDFItemPM().setDisable(true);
        mainView.getPageSetupItem().setOnAction(e -> pageSetup());
        mainView.getExportSetupItem().setOnAction(e -> exportSetup());
        if (EditorMain.os.startsWith("mac")) mainView.getExportSetupItem().setDisable(true);
        mainView.getScaleSetupItem().setOnAction(e -> scaleSetup());

        mainView.getQuickStartItem().setOnAction(e -> videoHelp("https://www.slappservices.net/quick_start/quick_start_player1.html", 900, 584));
        mainView.getSlappEditorItem().setOnAction(e -> videoHelp("https://www.slappservices.net/slapp_editor/slapp_editor_player1.html", 900, 671));
        mainView.getFormMapItem().setOnAction(e -> videoHelp("https://www.slappservices.net/form_maps/form_maps_player1.html",900, 759));
        mainView.getVerticalTreeItem().setOnAction(e -> videoHelp("https://www.slappservices.net/vertical_trees/vertical_trees_player1.html",900, 885));
        mainView.getHorizontalTreeItem().setOnAction(e -> videoHelp("https://www.slappservices.net/horizontal_trees/horizontal_trees_player1.html", 900, 833));
        mainView.getTranslationItem().setOnAction(e -> videoHelp("https://www.slappservices.net/translations/translations_player1.html", 900, 747));
        mainView.getTruthTableItem().setOnAction(e -> videoHelp("https://www.slappservices.net/truth_tables/truth_tables_player1.html", 900, 767));
        mainView.getDerivationItem().setOnAction(e -> videoHelp("https://www.slappservices.net/derivations/derivations_player1.html", 900, 755));

        mainView.getInstructorInfoItem().setOnAction(e -> mainView.showInstructorInfo());


        mainView.getCommonElementsTextItem().setOnAction(e -> generalTextHelp());
        mainView.getAboutItem().setOnAction(e -> aboutTextHelp());
        mainView.getContextualTextItem().setOnAction(e -> contextualTextHelp());
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
                e.consume();
            }
        });
        mainView.getPreviousExerciseMenu().setGraphic(previousExerciseLabel);

        Label nextExerciseLabel = new Label("Next");
        nextExerciseLabel.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                nextExercise();
                e.consume();
            }
        });
        mainView.getNextExerciseMenu().setGraphic(nextExerciseLabel);

        Label goToExerciseLabel = new Label("Jump");
        goToExerciseLabel.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                goToExercise();
                e.consume();
            }
        });
        mainView.getGoToExerciseMenu().setGraphic(goToExerciseLabel);


        Label assignmentCommentLabel = new Label("Comment");
        assignmentCommentLabel.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                assignmentComment();
                e.consume();
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
        flushPrintCleanups();
        mainView.getMainScene().focusOwnerProperty().removeListener(focusListener);

        // ---> MEMORY LEAK FIX: Clean up old UI and free the focus anchor
        if (currentExercise != null && currentExercise != exercise) {
            mainView.clearSizeSpinners();
            currentExercise.cleanup();
        }
        lastFocusOwner = null;
        // -----------------------------------------------------------------

        currentExercise = exercise;

        boolean isFrontPage = currentExercise instanceof FrontPageExercise;
        mainView.setupExercise(isFrontPage);

        mainView.getMainScene().focusOwnerProperty().addListener(focusListener);
    }

    private void createNewExercise() {
        if (checkContinueAssignment("Confirm Create", "This assignment appears to have unsaved changes, and will be overwritten in the create process.\n\nContinue to create exercise?")) {
            if (checkContinueExercise("Confirm Create", "This exercise appears to have unsaved changes, and will be overwritten by the new one.\n\nContinue to create exercise?")) {
                if (currentExercise != null) {
                    currentExercise.clearStandingPopups();
                }

                ExerciseType exerciseType = ExerciseTypePopup.getType(mainView);
                if (exerciseType != null) {
                    // ---> MEMORY LEAK FIX
              //      if (currentExercise != null) currentExercise.cleanup();

                    TypeSelectorFactories typeFactories = new TypeSelectorFactories(this);
                    typeFactories.createExerciseOfType(exerciseType);
                    isExerciseOpen = false;
                    currentAssignment = null;
                }
            }
        }
    }

    private void createRevisedExercise() {
        if (checkContinueAssignment("Confirm Create", "This assignment appears to have unsaved changes, and will be overwritten in the create process.\n\nContinue to create exercise?")) {
            if (checkContinueExercise("Confirm Create", "This exercise appears to have unsaved changes, and will be overwritten by the new one.\n\nContinue to create exercise?")) {
                if (currentExercise != null) {
                    currentExercise.clearStandingPopups();
                }

                Object exerciseModelObject = DiskUtilities.openExerciseModelObject();
                if (exerciseModelObject != null) {
                    // ---> MEMORY LEAK FIX
                 //   if (currentExercise != null) currentExercise.cleanup();

                    updateRecentExercises();
                    TypeSelectorFactories typeFactories = new TypeSelectorFactories(this);
                    typeFactories.createRevisedExerciseFromModelObject(exerciseModelObject);
                    isExerciseOpen = false;
                    currentAssignment = null;
                }
            }
        }
    }

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
    }

    private void saveExercise(boolean saveAs) {
        if (currentExercise != null) {
            if (!((ExerciseModel) currentExercise.getExerciseModel()).getExerciseName().isEmpty()) {
                currentExercise.saveExercise(saveAs);
            }
            else EditorAlerts.fleetingRedPopup("No named exercise to save.");
        }
        else EditorAlerts.fleetingRedPopup("No named exercise to save.");
    }

    private void resetExercise() {
        Alert confirm = EditorAlerts.confirmationAlert("Confirm Reset", "This will undo all your work on this exercise.  Continue to reset?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == OK) {
            if (currentExercise != null) currentExercise.clearStandingPopups();

            Exercise clearExercise = currentExercise.resetExercise();
            // Note: setUpExercise automatically calls cleanup() on the old exercise!
            setUpExercise(clearExercise);
            setAuxExercise();
            if (currentAssignment != null) mainView.setUpLowerAssignmentBar();
        }
    }

    private Exercise getEmptyExercise() {
        SimpleEditModel emptyModel = new SimpleEditModel("", "");
        emptyModel.setResponsePrefHeight(400);
        SimpleEditExercise emptyExercise = new SimpleEditExercise(emptyModel, mainWindow);
        emptyExercise.getExerciseView().getExerciseComment().getEditor().setPromptText("");
        emptyExercise.getExerciseView().setRightControlNode(null);
        return emptyExercise;
    }

    public void closeExercise() {
        if (currentAssignment == null) {
            if (checkContinueExercise("Confirm Close", "This exercise appears to have unsaved changes.\n\nContinue to close exercise?")) {
                if (currentExercise != null) {
                    currentExercise.clearStandingPopups();
                }

                Exercise oldExercise = currentExercise;  //************

                // Note: setUpExercise automatically calls cleanup() on the old exercise!
                setUpExercise(getEmptyExercise());
                isExerciseOpen = false;

                //**********
                if (DEBUG_MEMORY_LEAKS && oldExercise != null) {
                    checkMemoryLeak(oldExercise);
                }
                //***********
            }
        } else {
            EditorAlerts.showSimpleAlert("Cannot Close", "There is an open assignment.  Closing the assignment closes its member exercises.");
        }
    }

    public void openExercise(File exerciseFile){
        Object exerciseModelObject = null;
        if (checkContinueAssignment("Confirm Open", "This assignment appears to have unsaved changes, and will be overwritten by the new exercise.  Continue to open exercise?")) {
            if (checkContinueExercise("Confirm Open", "This exercise appears to have unsaved changes, and will be overwritten by the new one.  Continue to open exercise?")) {

                if (currentExercise != null) {
                    currentExercise.clearStandingPopups();
                }

                if (exerciseFile == null)
                    exerciseModelObject = DiskUtilities.openExerciseModelObject();
                else
                    exerciseModelObject = DiskUtilities.openExerciseObjectFromFile(exerciseFile);

                if (exerciseModelObject != null) {
                    updateRecentExercises();
                    TypeSelectorFactories typeFactories = new TypeSelectorFactories(this);
                    Exercise exercise = typeFactories.getExerciseFromModelObject(exerciseModelObject);
                    if (exercise != null) {
                        // Note: setUpExercise automatically calls cleanup() on the old exercise!
                        setUpExercise(exercise);
                        isExerciseOpen = true;
                        currentAssignment = null;
                    }
                }
            }
        }
    }

    public void flushPrintCleanups() {
        if (!pendingPrintCleanups.isEmpty()) {
            for (Exercise ex : pendingPrintCleanups) {
                try {
                    if (ex != null) ex.cleanup();
                } catch (Exception e) {}
            }
            pendingPrintCleanups.clear();
        }
    }

    private void exportExerciseToPDF() {
        processExerciseForOutput(true);
    }

    private void printExercise() {
        processExerciseForOutput(false);
    }

    private void processExerciseForOutput(boolean isExport) {
        if (PrintUtilities.isPrintInProgress()) {
            return;
        }

        flushPrintCleanups();

        if (currentExercise == null || ((ExerciseModel) currentExercise.getExerciseModel()).getExerciseName().isEmpty()) {
            String action = isExport ? "export." : "print.";
            EditorAlerts.fleetingRedPopup("Cannot find named exercise to " + action);
            return;
        }

        PrintUtilities.setPrintInProgress(true);
        mainView.activateProgressIndicator("");

        // Let one frame render the progress indicator before building and measuring the exercise nodes
        AnimationTimer timer = new AnimationTimer() {
            private int frames = 0;

            @Override
            public void handle(long now) {
                if (++frames < 2) {
                    return;
                }
                stop();

                Platform.runLater(() -> {
                    try {
                        boolean heightGood = true;

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

                        // Queue cleanup for later so the exercise survives the print job
                        pendingPrintCleanups.add(printExercise);

                        if (!heightGood && !fitToPage) {
                            mainView.deactivateProgressIndicator();

                            String actionPrompt = isExport ? "Continue export?" : "Continue to print?";
                            String exerciseName = ((ExerciseModel) currentExercise.getExerciseModel()).getExerciseName();
                            String message = "Fit page not selected and exercise " + exerciseName +
                                    " includes at least one block that takes up more than a page.  " +
                                    "Content exceeding page bounds will be cropped.\n\n " + actionPrompt;

                            Alert confirm = EditorAlerts.confirmationAlert("Page Problem:", message);
                            Optional<ButtonType> result = confirm.showAndWait();
                            if (result.isPresent() && result.get() == OK) {
                                heightGood = true;
                            }
                        }

                        if (heightGood) {
                            if (!fitToPage) PrintUtilities.resetPrintBufferScale();
                            if (isExport) {
                                PrintUtilities.sendBufferToPDF(null);
                            } else {
                                PrintUtilities.sendBufferToPrint(null);
                            }
                        } else {
                            PrintUtilities.clearPrintBuffer();
                            PrintUtilities.setPrintInProgress(false);
                            mainView.deactivateProgressIndicator();
                        }
                    } catch (Exception ex) {
                        PrintUtilities.clearPrintBuffer();
                        PrintUtilities.setPrintInProgress(false);
                        mainView.deactivateProgressIndicator();
                        ex.printStackTrace();
                    }
                });
            }
        };
        timer.start();
    }

    private boolean checkContinueExercise(String title, String content) {
        boolean okContinue = true;
        if (isExerciseOpen && currentExercise.isExerciseModified()) {
            Alert confirm = EditorAlerts.confirmationAlert(title, content);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() != OK) okContinue = false;
        }
        return okContinue;
    }

    private boolean checkContinueAssignment(String title, String content) {
        boolean okContinue = true;
        if (currentAssignment != null && (currentExercise.isExerciseModified() || assignmentContentModified)) {
            Alert confirm = EditorAlerts.confirmationAlert(title, content);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() != OK) okContinue = false;
        }
        return okContinue;
    }

    private void exportSetup() { PrintUtilities.exportSetup(); }

    private void pageSetup() { PrintUtilities.updatePageLayout(); }

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

    private void saveAssignment(boolean saveAs){
        if (currentAssignment == null) {
            EditorAlerts.fleetingRedPopup("There is no open assignment to save.");
        } else {
            if (currentExercise.isExerciseModified())  assignmentContentModified = true;

            ExerciseModel model = currentExercise.getExerciseModelFromView();
            currentAssignment.replaceExerciseModel(assignmentIndex, model);
            currentExercise.setExerciseModified(false);

            currentAssignment.setBaseScale(getBaseScale());
            currentAssignment.setFitToPage(fitToPage);
            currentAssignment.setPageLayout(PrintUtilities.getPageLayout());
            boolean saved = DiskUtilities.saveAssignment(saveAs, currentAssignment);

            if (saved) assignmentContentModified = false;
        }
    }

    public void openAssignment(File assignmentFile){
        Assignment assignment = null;
        if (checkContinueAssignment("Confirm Open", "The current assignment appears to have unsaved changes, and will be overwritten by the new one.\n\nContinue to open assignment?")) {
            if (checkContinueExercise("Confirm Open", "The current exercise appears to have unsaved changes, and will be overwritten by the new assignment.\n\nContinue to open assignment?")) {

                if (currentExercise != null) {
                    currentExercise.clearStandingPopups();
                    // Notice we removed currentExercise.cleanup() from here!
                    // If the user cancels the file dialog below, their current exercise is kept safe.
                }

                if (assignmentFile == null)
                    assignment = DiskUtilities.openAssignment();
                else
                    assignment = DiskUtilities.openAssignmentFromFile(assignmentFile);

                if (assignment != null) {
                    isExerciseOpen = false;
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

                        PageLayout pl = currentAssignment.getPageLayout();
                        PrintUtilities.setPageLayout(currentAssignment.getPageLayout());

                        // ---> MEMORY LEAK FIX: Use the wrapper!
                        // Safely cleans up the old exercise now that the file is loaded!
                        if (currentAssignment.getExerciseModels().size() != 0) {
                            Exercise firstEx = typeFactory.getExerciseFromModelObject(currentAssignment.getExerciseModels().get(assignmentIndex));
                            setUpExercise(firstEx);
                        }
                        else {
                            EditorAlerts.fleetingRedPopup("Assignment is empty - has no exercises.");
                        }

                        setAuxExercise();
                        mainView.setUpLowerAssignmentBar();
                        assignmentContentModified = false;
                    }
                }
            }
        }
    }


    private void closeAssignment() {
        if (currentAssignment == null) {
            EditorAlerts.fleetingRedPopup("There is no open assignment to close.");
        } else {
            if (checkContinueAssignment("Confirm Close", "This assignment appears to have unsaved changes.\n\nContinue to close assignment?")) {

                if (currentExercise != null) {
                    currentExercise.clearStandingPopups();
                }

                // Note: setUpExercise automatically calls cleanup() on the old exercise!
                setUpExercise(getEmptyExercise());
                isExerciseOpen = false;
                currentAssignment = null;
                assignmentContentModified = false;
            }
        }
    }

    private void printAssignment() {
        processAssignmentForOutput(false);
    }

    private void exportAssignment() {
        processAssignmentForOutput(true);
    }
    private void processAssignmentForOutput(boolean isExport) {
        if (PrintUtilities.isPrintInProgress()) {
    //       EditorAlerts.fleetingRedPopup("Print/Export already in progress.");
            return;
        }

        flushPrintCleanups();

        if (currentAssignment == null) {
            String action = isExport ? "export." : "print.";
            EditorAlerts.fleetingRedPopup("There is no open assignment to " + action);
            return;
        }

        PrintUtilities.setPrintInProgress(true);
        mainView.activateProgressIndicator("");
   //     EditorAlerts.fleetingPopup(isExport ? "Processing for export." : "Processing for print.");

        PrintUtilities.resetPrintBuffer(getBaseScale());

        if (currentExercise.isExerciseModified()) assignmentContentModified = true;
        ExerciseModel currentModel = currentExercise.getExerciseModelFromView();
        currentAssignment.replaceExerciseModel(assignmentIndex, currentModel);

        assignmentPointsEarned = 0;
        assignmentPointsPossible = 0;

        List<ExerciseModel> exerciseModelList = currentAssignment.getExerciseModels();
        List<String> badExerciseList = new ArrayList<>();
        final boolean[] heightGood = { true };

        AnimationTimer timer = new AnimationTimer() {
            private int index = 0;
            private int pulseCount = 0;

            @Override
            public void handle(long now) {
                // Run 3 lightweight animation pulses between each heavy exercise build
                // so the ProgressIndicator spins smoothly at ~60fps in between exercises.
                if (++pulseCount % 4 != 0) {
                    return;
                }

                try {
                    if (index < exerciseModelList.size()) {
                        ExerciseModel model = exerciseModelList.get(index++);

                        assignmentPointsEarned += model.getPointsEarned();
                        assignmentPointsPossible += model.getPointsPossible();

                        TypeSelectorFactories typeFactory = new TypeSelectorFactories(MainWindow.this);
                        Exercise exercise = typeFactory.getExerciseFromModelObject(model);
                        List<Node> exerciseNodes = exercise.getPrintNodes(model);

                        for (Node node : exerciseNodes) {
                            if (!PrintUtilities.processPrintNode(node) && !fitToPage) {
                                heightGood[0] = false;
                                badExerciseList.add(((ExerciseModel) exercise.getExerciseModel()).getExerciseName());
                            }
                        }

                        pendingPrintCleanups.add(exercise);
                    } else {
                        stop();
                        Platform.runLater(() -> {
                            try {
                                finishAssignmentPreparation(isExport, heightGood[0], badExerciseList);
                            } catch (Exception ex) {
                                PrintUtilities.clearPrintBuffer();
                                PrintUtilities.setPrintInProgress(false);
                                mainView.deactivateProgressIndicator();
                                ex.printStackTrace();
                            }
                        });
                    }
                } catch (Exception ex) {
                    stop();
                    PrintUtilities.clearPrintBuffer();
                    PrintUtilities.setPrintInProgress(false);
                    mainView.deactivateProgressIndicator();
                    ex.printStackTrace();
                }
            }
        };

        timer.start();
    }

    private void finishAssignmentPreparation(boolean isExport, boolean heightGood, List<String> badExerciseList) {
        if (!badExerciseList.isEmpty()) {
            mainView.deactivateProgressIndicator();

            StringBuilder sb = new StringBuilder(badExerciseList.get(0));
            for (int i = 1; i < badExerciseList.size(); i++) {
                sb.append(", ").append(badExerciseList.get(i));
            }
            String actionPrompt = isExport ? "Continue export?" : "Continue to print?";
            String message = "Fit page not selected and " + sb.toString() +
                    " include(s) at least one block that takes up more than a page.  " +
                    "Content exceeding page bounds will be cropped.\n\n " + actionPrompt;

            Alert confirm = EditorAlerts.confirmationAlert("Page Problem:", message);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == OK) {
                heightGood = true;
            }
        }

        PrintUtilities.setTopBox(mainView.getAssignmentHeader(assignmentPointsPossible, assignmentPointsEarned));

        if (heightGood) {
            if (!fitToPage) PrintUtilities.resetPrintBufferScale();
            String infoString = currentAssignment.getHeader().getCreationID() + "-" + currentAssignment.getHeader().getWorkingID();
            if (isExport) {
                PrintUtilities.sendBufferToPDF(infoString);
            } else {
                PrintUtilities.sendBufferToPrint(infoString);
            }
        } else {
            PrintUtilities.clearPrintBuffer();
            PrintUtilities.setPrintInProgress(false);
            mainView.deactivateProgressIndicator();
        }
    }



    private void createRevisedAssignment() {
        if (checkContinueAssignment("Confirm Create", "The current assignment appears to have unsaved changes, and will be overwritten in the creation process.\n\nContinue to create assignment?")) {
            if (checkContinueExercise("Confirm Create", "The current exercise appears to have unsaved changes, and will be overwritten in the creation process.\n\nContinue to create assignment?")) {
                if (currentExercise != null) {
                    currentExercise.clearStandingPopups();
                    // ---> MEMORY LEAK FIX
               //     currentExercise.cleanup();
                }

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

    private void createNewAssignment(){
        if (checkContinueAssignment("Confirm Create", "The current assignment appears to have unsaved changes, and will be overwritten in the creation process.\n\nContinue to create assignment?")) {
            currentAssignment = null;
            if (checkContinueExercise("Confirm Create", "The current exercise appears to have unsaved changes, and will be overwritten in the creation process.\n\n Continue to create assignment?")) {
                if (currentExercise != null) {
                    currentExercise.clearStandingPopups();
                    // ---> MEMORY LEAK FIX
               //     currentExercise.cleanup();
                }

                isExerciseOpen = false;
                new CreateAssignment(new Assignment(), this);
            }
        }
    }

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
                Exercise prevEx = typeFactory.getExerciseFromModelObject(currentAssignment.getExerciseModels().get(assignmentIndex));

                setUpExercise(prevEx); // Automatically cleans up the old one and resets lastFocusOwner!
                setAuxExercise();
                mainView.setUpLowerAssignmentBar();

                /*
                // ---> MEMORY LEAK FIX: Clean up AFTER grabbing the model, but BEFORE overwriting the variable!
                if (currentExercise != null) currentExercise.cleanup();

                assignmentIndex = prevIndex;
                TypeSelectorFactories typeFactory = new TypeSelectorFactories(this);

                currentExercise = typeFactory.getExerciseFromModelObject(currentAssignment.getExerciseModels().get(assignmentIndex));
                setAuxExercise();
                mainView.setupExercise(false);
                mainView.setUpLowerAssignmentBar();

                 */
            }
        }
    }



    private void nextExercise() {
        // 1. Early return eliminates the need for the large 'else' block
        if (currentAssignment == null) {
            EditorAlerts.fleetingRedPopup("Cannot Advance. There is no open assignment.");
            return;
        }

        // 2. Consistent null checking to prevent NPEs
        if (currentExercise != null && currentExercise.isExerciseModified()) {
            assignmentContentModified = true;
        }

        int nextIndex = assignmentIndex + 1;
        if (nextIndex >= currentAssignment.getExerciseModels().size()) {
            return; // Reached the end of the assignment, exit early
        }

        // Capture the old exercise before we advance for the leak test
        Exercise oldExercise = currentExercise;

        if (currentExercise != null) {
            currentExercise.clearStandingPopups();
            ExerciseModel model = currentExercise.getExerciseModelFromView();
            currentAssignment.replaceExerciseModel(assignmentIndex, model);
        }

        assignmentIndex = nextIndex;
        TypeSelectorFactories typeFactory = new TypeSelectorFactories(this);
        Exercise nextEx = typeFactory.getExerciseFromModelObject(currentAssignment.getExerciseModels().get(assignmentIndex));

        // NOTE: This method MUST overwrite 'this.currentExercise = nextEx' internally for GC to work!
        setUpExercise(nextEx);
        setAuxExercise();
        mainView.setUpLowerAssignmentBar();

        // 3. Run memory leak test in a separate, isolated method
        if (DEBUG_MEMORY_LEAKS && oldExercise != null) {
            checkMemoryLeak(oldExercise);
        }
    }

    /**
     * Utility method to verify JavaFX objects are safely garbage collected.
     */
    private void checkMemoryLeak(Exercise exerciseToDiscard) {
        String className = exerciseToDiscard.getClass().getSimpleName();
        java.lang.ref.WeakReference<Exercise> leakTestRef = new java.lang.ref.WeakReference<>(exerciseToDiscard);

        // Wait 2 seconds for JavaFX to detach the UI from the Scene graph before testing
        javafx.animation.PauseTransition gcDelay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(2000));
        gcDelay.setOnFinished(gcEvent -> {
            System.gc(); // Suggest Garbage Collection to the JVM

            Exercise leakedExercise = leakTestRef.get();
            if (leakedExercise != null) {
                System.err.println("⚠️ MEMORY LEAK: " + className + " " + leakedExercise + " is still in memory!");
                System.err.println("   Check for lingering Event Listeners, Property Bindings, or Scene Graph references.");
            } else {
                System.out.println("✅ SUCCESS: Previous " + className + " was safely destroyed.");
            }
        });
        gcDelay.play();
    }

    /*
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

                // --- MEMORY LEAK TEST SETUP ---
                // Capture the exercise we are about to discard in a WeakReference
                Exercise aboutToDiscard = currentExercise;
                java.lang.ref.WeakReference<Exercise> leakTestRef = new java.lang.ref.WeakReference<>(aboutToDiscard);
                aboutToDiscard = null; // Clear local strong reference immediately
                // ------------------------------

                assignmentIndex = nextIndex;
                TypeSelectorFactories typeFactory = new TypeSelectorFactories(this);
                Exercise nextEx = typeFactory.getExerciseFromModelObject(currentAssignment.getExerciseModels().get(assignmentIndex));

                setUpExercise(nextEx); // Automatically cleans up the old one and resets lastFocusOwner!
                setAuxExercise();
                mainView.setUpLowerAssignmentBar();

                // --- MEMORY LEAK VERIFICATION ---
                // Wait 250ms for JavaFX to detach the UI from the Scene graph before testing
                javafx.animation.PauseTransition gcDelay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(2000));
                gcDelay.setOnFinished(gcEvent -> {
                    System.gc(); // Force Garbage Collection

                    Exercise leakedExercise = leakTestRef.get();
                    if (leakedExercise != null) {
                        System.out.println("MEMORY LEAK: " + leakedExercise.getClass().getSimpleName() + " " + leakedExercise + " is still in memory!");
                    } else {
                        System.out.println("SUCCESS: Previous Exercise was safely destroyed.");
                    }
                });
                gcDelay.play();
                // --------------------------------
            }
        }
    }

     */

    /*
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
                Exercise nextEx = typeFactory.getExerciseFromModelObject(currentAssignment.getExerciseModels().get(assignmentIndex));

                setUpExercise(nextEx); // Automatically cleans up the old one and resets lastFocusOwner!
                setAuxExercise();
                mainView.setUpLowerAssignmentBar();

            }
        }
    }

     */

    private void goToExercise() {
        if (currentAssignment == null) {
            EditorAlerts.fleetingRedPopup("Cannot Jump.  There is no open assignment.");
            return;
        }

        if (currentExercise.isExerciseModified()) assignmentContentModified = true;

        if (jumpPopup == null) {
            jumpPopup = new Popup();
            jumpListView = new ListView<>();
            jumpListView.setPadding(new Insets(5));
            jumpListView.setStyle("-fx-border-color: white; -fx-background-color: white; -fx-border-width: 0");

            jumpListView.setCellFactory(new Callback<ListView<ExerciseModel>, ListCell<ExerciseModel>>() {
                public ListCell<ExerciseModel> call(ListView<ExerciseModel> param) {
                    return new ListCell<ExerciseModel>() {
                        @Override
                        protected void updateItem(ExerciseModel item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(item == null || empty ? null : (jumpListView.getItems().indexOf(item) + 1) + ".  " + item);
                        }
                    };
                }
            });

            jumpListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ExerciseModel>() {
                @Override
                public void changed(ObservableValue<? extends ExerciseModel> ob, ExerciseModel ov, ExerciseModel nv) {
                    if (nv != null && !ignoreJumpListener) {
                        if (currentExercise != null) currentExercise.clearStandingPopups();

                        ExerciseModel model = currentExercise.getExerciseModelFromView();
                        currentAssignment.replaceExerciseModel(assignmentIndex, model);
                        assignmentIndex = jumpListView.getItems().indexOf(nv);

                        TypeSelectorFactories typeFactory = new TypeSelectorFactories(mainWindow);

                        // ---> MEMORY LEAK FIX: Use the wrapper!
                        // This automatically cleans up the old exercise, releases focus,
                        // assigns currentExercise, and sets up the Main View for the new one!
                        Exercise nextEx = typeFactory.getExerciseFromModelObject(currentAssignment.getExerciseModels().get(assignmentIndex));
                        setUpExercise(nextEx);

                        setAuxExercise();
                        mainView.setUpLowerAssignmentBar();
                        jumpPopup.hide();
                    }
                }
            });

            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> jumpPopup.hide());

            HBox buttonBox = new HBox(closeButton);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setStyle("-fx-background-color: white; -fx-border-width: 1 0 0 0; -fx-border-color: lightblue;");

            VBox jumpBox = new VBox(0, jumpListView, buttonBox);
            jumpPopup.getContent().add(jumpBox);
            jumpBox.setStyle("-fx-border-color: lightblue; -fx-border-width: 5; -fx-opacity: 1.0");
        }

        ignoreJumpListener = true;
        jumpListView.getItems().setAll(currentAssignment.getExerciseModels());
        jumpListView.getSelectionModel().select(assignmentIndex);
        ignoreJumpListener = false;

        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        double popupX = EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth() * .55;
        double popupY = EditorMain.mainStage.getY() + EditorMain.mainStage.getHeight() * .2;

        jumpPopup.show(mainStage, popupX, popupY);
    }


    public void setAuxExercise() {
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

    private void scaleSetup() {
        // ---> MEMORY LEAK FIX: Lazy initialization. Only build this window once!
        if (scaleSetupPopup == null) {
            scaleSetupPopup = new Popup();

            Button okButton = new Button("OK");

            fitToPageCheck = new CheckBox("Fit to Page");
            fitToPageCheck.setTooltip(new Tooltip("Fit oversize nodes to page"));
            fitToPageCheck.setPrefHeight(24);

            baseScaleSpinner = new Spinner<>(50, 150, 100, 1);
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
            mainBox.setStyle("-fx-background-color: white; -fx-border-color: lightblue; -fx-border-width: 5; -fx-opacity: 1.0");
            mainBox.setPadding(new Insets(10));
            scaleSetupPopup.getContent().add(mainBox);

            okButton.setOnAction(e -> {
                setBaseScale(baseScaleSpinner.getValue() / 100.0);
                fitToPage = fitToPageCheck.isSelected();
                scaleSetupPopup.hide();
            });
        }

        fitToPageCheck.setSelected(fitToPage);
        baseScaleSpinner.getValueFactory().setValue((int) Math.round(getBaseScale() * 100));
        scaleSetupPopup.show(mainStage);
    }

    boolean checkCloseWindow() {
        boolean continueClose = false;
        if (checkContinueAssignment("Confirm Close", "The current assignment appears to have unsaved changes.\n\nContinue to close?")) {
            if (checkContinueExercise("Confirm Close", "The current exercise appears to have unsaved changes.\n\nContinue to close exercise?")) {
                continueClose = true;
            }
        }
        return continueClose;
    }

    public static void restoreMacFocus(Stage stage) {
        if (stage == null) return;
        String osName = System.getProperty("os.name").toLowerCase();
        if (!osName.contains("mac")) {
            return;
        }

        Platform.runLater(() ->
                Platform.runLater(() -> {
                    stage.requestFocus();
                    if (stage.getScene() != null && stage.getScene().getRoot() != null) {
                        stage.getScene().getRoot().requestFocus();
                    }
                    EditorAlerts.fleetingStage();
                })
        );
    }

    private void aboutTextHelp() {
        TextHelpPopup.helpAbout();
    }

    private void keyboardShortcutsTextHelp() {TextHelpPopup.helpKeyboardShortcut(); }

    public void videoHelp(String urlString, double width, double height) {


        if (videoStage != null) videoStage.close();
        if (TextHelpPopup.getHelpStage() != null) TextHelpPopup.getHelpStage().close();

        if (videoBrowser != null) {
            if (view == null) view = BrowserView.newInstance(videoBrowser);

            videoBrowser.navigation().loadUrl(urlString);

            VBox root = new VBox(view);
            root.setStyle("-fx-background-color: white");
            root.setVgrow(view, Priority.ALWAYS);
            Scene scene = new Scene(root, Color.WHITE);
      //      Scene scene = new Scene(root);

            videoStage = new Stage();
            videoStage.setScene(scene);
            videoStage.setTitle("SLAPP Video Help");
            videoStage.initModality(Modality.NONE);
            videoStage.getIcons().addAll(EditorMain.icons);
            Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
            videoStage.setX(Math.max(5, Math.min(mainStage.getX() + mainStage.getWidth(), bounds.getMaxX() - (width + 20))));
            videoStage.setY(Math.max(5, Math.min(mainStage.getY() + 20, bounds.getMaxY() - (height + 20))));
            videoStage.setWidth(width);
            videoStage.setHeight(height);

            videoStage.setOnCloseRequest(e -> {
                videoBrowser.navigation().loadHtml("");
                videoStage.close();
            });

            videoStage.show();
        }
        else {
            EditorAlerts.fleetingRedPopup("Problem initializing JxBrowser.");
        }
    }

    void closeVideoHelp() {
        if (videoStage != null) {
            videoStage.close();
        }
        if (engine != null && !engine.isClosed()) {
            engine.close();
        }
    }

    private void generalTextHelp() {
        TextHelpPopup.helpCommonElements();
    }

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

    private void makeReport() {
        String message = "Please be as specific as you can about your concern; if you are reporting an error, include information about the version of SLAPP and of your operating system, along with (if possible) the relevant file and whether and how the problem may be repeated (ok to delete this line).";
        MailHelper.generate("messaging@slappservices.net", "SLAPP: (your issue)", message);
    }

    public MainWindowView getMainView() { return mainView; }
    public Assignment getCurrentAssignment() { return currentAssignment; }
    public Exercise getCurrentExercise() { return currentExercise; }
    public int getAssignmentIndex() { return assignmentIndex; }
    public Node getLastFocusOwner() { return lastFocusOwner; }
    public double getBaseScale() { return baseScale.get(); }
    public DoubleProperty baseScaleProperty() { return baseScale; }
    public void setBaseScale(double baseScale) { this.baseScale.set(baseScale); }
    public static String getStyleHeader() { return styleHeader; }
    public static String getHeadTop() { return headTop; }
    public Browser getTextHlpBrowser() { return textHlpBrowser; }
    public Browser getCreateHlpBrowser() { return createHlpBrowser; }
    public static Stage getVideoStage() { return videoStage; }
}



