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

package slapp.editor;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import slapp.editor.main_window.MainWindowView;

import java.util.List;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.List;

/**
 * Utility class to show pop-up alerts
 */

public class EditorAlerts {

    static boolean showPreliminary = false;
    static String prelimMessage = "";
    static boolean popupShowing = false;

    private static java.lang.ref.WeakReference<Stage> lastFocusedStageRef = new java.lang.ref.WeakReference<>(null);

    static {
        // Track whichever primary/editor Stage most recently held focus
        Window.getWindows().addListener((javafx.collections.ListChangeListener<Window>) change -> {
            while (change.next()) {
                for (Window w : change.getAddedSubList()) {
                    attachFocusTracker(w);
                }
                for (Window w : change.getRemoved()) {
                    if (w == lastFocusedStageRef.get()) {
                        lastFocusedStageRef.clear();
                    }
                }
            }
        });
        for (Window w : Window.getWindows()) {
            attachFocusTracker(w);
        }
    }

    private static void attachFocusTracker(Window window) {
        if (window instanceof Stage stage) {
            if (isAppStage(stage) && stage.isFocused()) {
                lastFocusedStageRef = new java.lang.ref.WeakReference<>(stage);
            }
            javafx.beans.value.ChangeListener<Boolean> focusListener = (obs, wasFocused, isNowFocused) -> {
                if (Boolean.TRUE.equals(isNowFocused) && isAppStage(stage)) {
                    lastFocusedStageRef = new java.lang.ref.WeakReference<>(stage);
                }
            };
            stage.focusedProperty().addListener(focusListener);

            // Clean up listener when the stage closes
            stage.addEventHandler(javafx.stage.WindowEvent.WINDOW_HIDDEN, e ->
                    stage.focusedProperty().removeListener(focusListener)
            );
        }
    }

    /**
     * Returns true for Main Window (Modality.NONE) and Create Windows (Modality.WINDOW_MODAL or NONE),
     * while excluding UTILITY stages and Alert/Dialog stages (Modality.APPLICATION_MODAL / DialogPane).
     */
    private static boolean isAppStage(Window window) {
        return window instanceof Stage stage
                && stage.isShowing()
                && stage.getStyle() != StageStyle.UTILITY
                && stage.getModality() != Modality.APPLICATION_MODAL
                && !(stage.getScene() != null && stage.getScene().getRoot() instanceof javafx.scene.control.DialogPane);
    }

    /**
     * If a Stage is currently blocked by a showing WINDOW_MODAL child (such as a Create window
     * owned by mainStage), always prefer the modal child on top.
     */
    private static Stage redirectIfBlockedByModalChild(Stage stage) {
        if (stage == null) return null;
        for (Window window : Window.getWindows()) {
            if (window instanceof Stage child
                    && child.isShowing()
                    && child.getOwner() == stage
                    && child.getModality() == Modality.WINDOW_MODAL
                    && isAppStage(child)) {
                return child;
            }
        }
        return stage;
    }

    private static Stage getFocusedStage() {
        for (Window window : Window.getWindows()) {
            if (isAppStage(window) && window.isFocused()) {
                Stage stage = redirectIfBlockedByModalChild((Stage) window);
                lastFocusedStageRef = new java.lang.ref.WeakReference<>(stage);
                return stage;
            }
        }
        return null;
    }

    /**
     * Dynamically locates the active Stage (Create Window or Main Window)
     * without requiring callers like DiskUtilities to pass a Window reference.
     */
    public static Stage getActiveStage() {
        // 1. Prefer the currently focused non-utility, non-Alert Stage
        Stage focused = getFocusedStage();
        if (focused != null) {
            return focused;
        }

        // 2. If focus is momentarily in transit after a native dialog,
        // prefer the Stage that most recently held focus
        Stage lastFocused = lastFocusedStageRef.get();
        if (lastFocused != null && isAppStage(lastFocused)) {
            return redirectIfBlockedByModalChild(lastFocused);
        }

        // 3. Fallback for macOS right after a native FileChooser closes
        // (if EditorAlerts was not yet loaded prior to the dialog opening)
        for (Window window : Window.getWindows()) {
            if (isAppStage(window) && window != EditorMain.mainStage) {
                return (Stage) window;
            }
        }

        // 4. Default to mainStage (or its WINDOW_MODAL Create child if one is open)
        return redirectIfBlockedByModalChild(EditorMain.mainStage);
    }

    /**
     * Optional helper for Create stages: blocks input to mainStage while createStage is open,
     * while leaving both stages independent at the OS level so either window can be brought to the front.
     * Automatically cleans up all filters and listeners when createStage closes.
     */
    private static boolean hasShowingPopup(Stage owner) {
        for (Window w : Window.getWindows()) {
            if (w instanceof javafx.stage.PopupWindow popup
                    && popup.isShowing()
                    && popup.getOwnerWindow() == owner) {
                return true;
            }
        }
        return false;
    }

    public static void makeIndependentBlockingStage(Stage createStage) {
        Stage mainStage = EditorMain.mainStage;
        if (mainStage == null) return;

        boolean isWindows = System.getProperty("os.name", "").toLowerCase().startsWith("win");

        // Releases alwaysOnTop 60ms after Ctrl+C or after the ContextMenu has completely closed
        PauseTransition releaseTopmostDelay = new PauseTransition(Duration.millis(60));
        releaseTopmostDelay.setOnFinished(e -> {
            if (createStage.isShowing() && !hasShowingPopup(createStage)) {
                createStage.setAlwaysOnTop(false);
            }
        });

        Runnable shieldCreateOnTop = () -> {
            if (isWindows && createStage.isShowing() && !mainStage.isFocused()) {
                if (!createStage.isAlwaysOnTop()) {
                    createStage.setAlwaysOnTop(true);
                }
                releaseTopmostDelay.playFromStart();
            }
        };

        // 1. Only activate the 60ms shield on Ctrl/Shortcut key combos (e.g., Ctrl+C)
        javafx.event.EventHandler<javafx.scene.input.KeyEvent> keyFilter = e -> {
            if (e.isControlDown() || e.isShortcutDown()) {
                shieldCreateOnTop.run();
            }
        };

        // 2. Right-click turns on the shield BEFORE the ContextMenu opens;
        //    Left-click synchronously restacks createStage without leaving alwaysOnTop active.
        javafx.event.EventHandler<javafx.scene.input.MouseEvent> mouseFilter = e -> {
            if (!isWindows || !createStage.isShowing() || mainStage.isFocused()) return;

            if (e.isSecondaryButtonDown() || e.isPopupTrigger()) {
                shieldCreateOnTop.run();
            } else if (!hasShowingPopup(createStage)) {
                boolean wasTop = createStage.isAlwaysOnTop();
                createStage.setAlwaysOnTop(true);
                createStage.setAlwaysOnTop(wasTop);
                createStage.toFront();
            }
        };

        javafx.event.EventHandler<javafx.scene.input.ContextMenuEvent> contextMenuFilter = e -> {
            shieldCreateOnTop.run();
        };

        // 3. When the ContextMenu finishes closing, hold the shield for 60ms and then release it
        javafx.collections.ListChangeListener<Window> popupTracker = change -> {
            if (!isWindows) return;
            while (change.next()) {
                for (Window w : change.getAddedSubList()) {
                    if (w instanceof javafx.stage.PopupWindow popup && popup.getOwnerWindow() == createStage) {
                        // Keep alwaysOnTop steady while the menu is open; do not let the timer expire mid-menu
                        releaseTopmostDelay.stop();
                        if (!mainStage.isFocused() && !createStage.isAlwaysOnTop()) {
                            createStage.setAlwaysOnTop(true);
                        }
                    }
                }
                for (Window w : change.getRemoved()) {
                    if (w instanceof javafx.stage.PopupWindow popup && popup.getOwnerWindow() == createStage) {
                        shieldCreateOnTop.run();
                    }
                }
            }
        };

        // 4. If createStage loses focus to an Alert, FileChooser, or mainStage, drop alwaysOnTop immediately
        javafx.beans.value.ChangeListener<Boolean> createFocusListener = (obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && !hasShowingPopup(createStage)) {
                releaseTopmostDelay.stop();
                createStage.setAlwaysOnTop(false);
            }
        };

        javafx.event.EventHandler<javafx.scene.input.InputEvent> blockInput = e -> {
            if (e instanceof javafx.scene.input.MouseEvent me
                    && me.getEventType() == javafx.scene.input.MouseEvent.MOUSE_PRESSED) {
                releaseTopmostDelay.stop();
                createStage.setAlwaysOnTop(false);
            }
            e.consume();
        };

        javafx.beans.value.ChangeListener<Boolean> mainFocusListener = (obs, wasFocused, isNowFocused) -> {
            if (Boolean.TRUE.equals(isNowFocused)) {
                releaseTopmostDelay.stop();
                createStage.setAlwaysOnTop(false);
            }
        };

        javafx.event.EventHandler<javafx.stage.WindowEvent> blockClose = e -> {
            e.consume();
            if (isWindows) {
                createStage.setAlwaysOnTop(true);
                createStage.setAlwaysOnTop(false);
            }
            createStage.toFront();
            createStage.requestFocus();
        };

        javafx.beans.value.ChangeListener<Boolean> mainShowingListener = (obs, wasShowing, isNowShowing) -> {
            if (!isNowShowing && createStage.isShowing()) {
                createStage.close();
            }
        };

        createStage.addEventHandler(javafx.stage.WindowEvent.WINDOW_SHOWN, e -> {
            createStage.addEventFilter(javafx.scene.input.KeyEvent.ANY, keyFilter);
            createStage.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, mouseFilter);
            createStage.addEventFilter(javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED, contextMenuFilter);
            createStage.focusedProperty().addListener(createFocusListener);
            Window.getWindows().addListener(popupTracker);
            mainStage.addEventFilter(javafx.scene.input.InputEvent.ANY, blockInput);
            mainStage.addEventFilter(javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST, blockClose);
            mainStage.focusedProperty().addListener(mainFocusListener);
            mainStage.showingProperty().addListener(mainShowingListener);
        });

        createStage.addEventHandler(javafx.stage.WindowEvent.WINDOW_HIDDEN, e -> {
            releaseTopmostDelay.stop();
            createStage.setAlwaysOnTop(false);
            createStage.removeEventFilter(javafx.scene.input.KeyEvent.ANY, keyFilter);
            createStage.removeEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, mouseFilter);
            createStage.removeEventFilter(javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED, contextMenuFilter);
            createStage.focusedProperty().removeListener(createFocusListener);
            Window.getWindows().removeListener(popupTracker);
            mainStage.removeEventFilter(javafx.scene.input.InputEvent.ANY, blockInput);
            mainStage.removeEventFilter(javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST, blockClose);
            mainStage.focusedProperty().removeListener(mainFocusListener);
            mainStage.showingProperty().removeListener(mainShowingListener);
        });
    }

    /**
     * Information alert with 'OK' to close
     *
     * @param header  alert header string
     * @param message alert message
     */
    public static void showSimpleAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(header);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);

        Stage owner = getActiveStage();
        if (owner != null) {
            alert.initOwner(owner);
            alert.setX(owner.getX() + owner.getWidth() / 3);
            alert.setY(owner.getY() + owner.getHeight() / 3);
        }

        alert.showAndWait();
    }

    /**
     * Information alert with 'OK' to close
     *
     * @param header      alert header string
     * @param messageTxts list of texts for message
     */
    public static void showSimpleTxtListAlert(String header, List<Text> messageTxts) {
        if (showPreliminary) {
            fleetingRedPopup(prelimMessage);
        }

        TextFlow textFlow = new TextFlow();
        if (messageTxts != null) {
            Text[] resultArray = new Text[messageTxts.size()];
            messageTxts.toArray(resultArray);
            textFlow = new TextFlow(resultArray);
        }
        EditorAlerts.showSimpleTxtFlowAlert(header, textFlow);
    }

    /**
     * Information alert with 'OK' to close
     *
     * @param header  alert header string
     * @param message alert message
     */
    public static void showSimpleTxtFlowAlert(String header, TextFlow message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(header);
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(message);
        alert.getDialogPane().setGraphic(null);
        alert.initModality(Modality.APPLICATION_MODAL);

        Stage owner = getActiveStage();
        message.setPrefWidth(400);
        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();

        if (owner != null) {
            alert.initOwner(owner);
            alert.setX(Math.min(owner.getX() + owner.getWidth(), bounds.getMaxX() - 420));
            alert.setY(owner.getY() + owner.getHeight() / 4);
        }

        alert.showAndWait();
    }

    /**
     * Self-closing information alert
     *
     * @param header  alert header string
     * @param message alert message
     */
    public static void showFleetingAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(header);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);

        Stage owner = getActiveStage();
        if (owner != null) {
            alert.initOwner(owner);
            alert.setX(owner.getX() + owner.getWidth() / 3);
            alert.setY(owner.getY() + owner.getHeight() / 3);
        }

        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> alert.hide());
        delay.play();
        alert.showAndWait();
    }

    public static void fleetingStage() {
        VBox vBox = new VBox();
        Scene fScene = new Scene(vBox);
        Stage fStage = new Stage();
        fStage.setScene(fScene);
        fStage.initModality(Modality.NONE);

        fStage.initStyle(StageStyle.UTILITY);
        fStage.toBack();
        fStage.setOpacity(0);

        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> fStage.close());
        delay.play();

        fStage.show();
        vBox.requestFocus();
    }

    /**
     * Confirmation alert with OK/Cancel options. The alert is returned, and must be shown by caller
     *
     * @param title   alert header string
     * @param content alert message
     * @return the alert
     */
    public static Alert confirmationAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        Stage owner = getActiveStage();
        if (owner != null) {
            alert.initOwner(owner);
            alert.setX(owner.getX() + owner.getWidth() / 3);
            alert.setY(owner.getY() + owner.getHeight() / 3);
        }
        return alert;
    }

    /**
     * Self-closing popup message
     *
     * @param message the string (text) message
     */
    public static void fleetingPopup(String message) {
        showStyledPopup(
                message,
                "-fx-background-color: aliceblue; -fx-border-color: blue;",
                Duration.seconds(2),
                30
        );
    }

    public static void fleetingRedPopup(String message) {
        showStyledPopup(
                message,
                "-fx-background-color: palevioletred; -fx-border-color: darkred;",
                Duration.seconds(4),
                150
        );
    }

    private static void showStyledPopup(String message, String style, Duration duration, double yOffset) {
        Popup popup = new Popup();
        Label label = new Label(message);
        label.setAlignment(Pos.CENTER);
        label.setStyle(style);

        Text text = new Text(message);
        new Scene(new Group(text));
        text.applyCss();
        double width = Math.max(200, text.getLayoutBounds().getWidth());

        label.setMinWidth(width + 20);
        label.setMaxWidth(1000);
        label.setMinHeight(100);
        popup.getContent().add(label);

        showPopupOnActiveStage(popup, duration, yOffset);
    }

    /**
     * Self-closing popup with label (node) message area
     *
     * @param label
     */
    public static void fleetingPopup(Label label) {
        Popup popup = new Popup();
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-background-color: aliceblue; -fx-border-color: blue;");

        new Scene(new Group(label));
        label.applyCss();
        double width = Math.max(200, label.getLayoutBounds().getWidth());

        label.setMinWidth(width + 20);
        label.setMaxWidth(1000);
        label.setMinHeight(100);
        popup.getContent().add(label);

        showPopupOnActiveStage(popup, Duration.seconds(2), 0);
    }

    private static void showPopupOnActiveStage(Popup popup, Duration duration, double yOffset) {
        PauseTransition delay = new PauseTransition(duration);
        delay.setOnFinished(e -> popup.hide());

        // Capture focused stage synchronously in case a modal Alert is opened
        // on the very next line (e.g., in showSimpleTxtListAlert)
        Stage initialFocusedStage = getFocusedStage();

        java.util.function.Consumer<Stage> displayOnStage = (Stage owner) -> {
            if (owner != null && owner.isShowing()) {
                popup.show(owner);
                if (yOffset != 0) {
                    popup.setY(popup.getY() + yOffset);
                }
                delay.playFromStart();
            }
        };

        Platform.runLater(() -> {
            if (initialFocusedStage != null && initialFocusedStage.isShowing()) {
                // Stage already had focus when popup was requested; show immediately
                displayOnStage.accept(initialFocusedStage);
                return;
            }

            Stage currentlyFocused = getFocusedStage();
            if (currentlyFocused != null) {
                displayOnStage.accept(currentlyFocused);
            } else {
                // Native dialog (Print / FileChooser) just closed on macOS and focus is in transit.
                // Wait for macOS to hand focus back to the owning Stage, then show the popup.
                waitForStageFocus(displayOnStage);
            }
        });
    }

    private static void waitForStageFocus(java.util.function.Consumer<Stage> onStageReady) {
        java.util.List<Stage> candidates = new java.util.ArrayList<>();
        for (Window window : Window.getWindows()) {
            if (isAppStage(window)) {
                candidates.add((Stage) window);
            }
        }

        boolean[] handled = { false };
        PauseTransition fallbackTimeout = new PauseTransition(Duration.millis(400));

        javafx.beans.value.ChangeListener<Boolean> focusListener = new javafx.beans.value.ChangeListener<>() {
            @Override
            public void changed(javafx.beans.value.ObservableValue<? extends Boolean> obs, Boolean wasFocused, Boolean isNowFocused) {
                if (Boolean.TRUE.equals(isNowFocused) && !handled[0]) {
                    handled[0] = true;
                    fallbackTimeout.stop();
                    for (Stage s : candidates) {
                        s.focusedProperty().removeListener(this);
                    }
                    Stage newlyFocused = getFocusedStage();
                    // Give macOS AppKit a brief beat to finish ordering the Stage to the front
                    PauseTransition settle = new PauseTransition(Duration.millis(50));
                    settle.setOnFinished(e -> onStageReady.accept(newlyFocused != null ? newlyFocused : getActiveStage()));
                    settle.play();
                }
            }
        };

        for (Stage s : candidates) {
            s.focusedProperty().addListener(focusListener);
        }

        // Fallback in case the user Cmd-Tabbed to another app while the dialog was closing
        fallbackTimeout.setOnFinished(e -> {
            if (!handled[0]) {
                handled[0] = true;
                for (Stage s : candidates) {
                    s.focusedProperty().removeListener(focusListener);
                }
                onStageReady.accept(getActiveStage());
            }
        });
        fallbackTimeout.play();
    }

    public static void setShowPreliminary(boolean showPrelim, String messageString) {
        showPreliminary = showPrelim;
        prelimMessage = messageString;
    }
}