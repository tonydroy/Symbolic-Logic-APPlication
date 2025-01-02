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

package slapp.editor;

import com.install4j.api.launcher.StartupNotification;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.*;
import javafx.application.*;
import slapp.editor.main_window.MainWindow;

import java.awt.*;
import java.util.List;

/**
 * Includes the main and start methods for the SLAPP editor.
 */
public class EditorMain extends Application {

    /** Stage for the main window   */
    public static Stage mainStage;

    /** Window icons     */
    public static ObservableList<Image> icons;

    /** Non-null (but small and blank) image     */
    public static Image emptyImage;

    /** True if this is a second instance of SLAPP   */
    public static boolean secondaryCopy = false;

    /** The operating system     */
    public static String os;

 //   private static String fileToOpen = null;
    private static String fileToOpen = "";

    private static MainWindow mainWindow;


    /**
     * The main entry point to the SLAPP editor
     *
     * @param args - not used
     */
    public static void main(String[] args) {

        // register startup notification on AWT before JavaFX is initialized (required as per Ingo K)
        EventQueue.invokeLater(() -> {
            StartupNotification.registerStartupListener(parameters -> {
                        try {
                            openFile(parameters);   //this has no effect on Mac startup
                            fileToOpen = parameters;
                        } catch (IllegalStateException ex) {
                            fileToOpen = parameters;
                        }
                    }
            );
        });

        launch(args);
    }



    /**
     * The JavaFX entry point to the SLAPP editor.  Sets static variables, and creates the main window.
     *
     * @param stage the primary stage for this application, onto which
     * the application scene can be set.
     */
    @Override
    public void start(Stage stage) {
        this.mainStage = stage;

        secondaryCopy = JustOneLock.isAppActive();
        os = System.getProperty("os.name");

        if (!secondaryCopy) {
            stage.getIcons().add(new Image(EditorMain.class.getResourceAsStream("/icon32x32.png")));
            stage.getIcons().add(new Image(EditorMain.class.getResourceAsStream("/icon16x16.png")));
        }
        else {
            stage.getIcons().add(new Image(EditorMain.class.getResourceAsStream("/icon_purple32x32.png")));
            stage.getIcons().add(new Image(EditorMain.class.getResourceAsStream("/icon_purple16x16.png")));
        }
        icons = stage.getIcons();

        emptyImage = new Image(EditorMain.class.getResourceAsStream("/emptyImage.png"));

        Font.loadFont(EditorMain.class.getResource("/fonts/la-solid-900.ttf").toExternalForm(),11);
        Font.loadFont(EditorMain.class.getResource("/fonts/NotoSans-Bold.ttf").toExternalForm(),11);
        Font.loadFont(EditorMain.class.getResource("/fonts/NotoSans-BoldItalic.ttf").toExternalForm(),11);
        Font.loadFont(EditorMain.class.getResource("/fonts/NotoSans-Italic.ttf").toExternalForm(),11);
        Font.loadFont(EditorMain.class.getResource("/fonts/NotoSans-Regular.ttf").toExternalForm(),11);
        Font.loadFont(EditorMain.class.getResource("/fonts/NotoSerifCombo-Bold.ttf").toExternalForm(),11);
        Font.loadFont(EditorMain.class.getResource("/fonts/NotoSerifCombo-BoldItalic.ttf").toExternalForm(),11);
        Font.loadFont(EditorMain.class.getResource("/fonts/NotoSerifCombo-Italic.ttf").toExternalForm(),11);
        Font.loadFont(EditorMain.class.getResource("/fonts/NotoSerifCombo-Regular.ttf").toExternalForm(),11);

        //file to open is empty on Windows startup, but can get application parameters
        if (fileToOpen.isEmpty()) {
            Parameters params = getParameters();
            List<String> paramList = params.getRaw();
            if (paramList.size() > 0) {
                fileToOpen = paramList.get(0);
            }
        }

        Platform.runLater(() -> {
            mainWindow = new MainWindow(fileToOpen);
        });
    }

    private static void openFile(String parameters) {

        Platform.runLater(() -> {
            mainWindow.autoOpen(parameters);
        });
    }

}


