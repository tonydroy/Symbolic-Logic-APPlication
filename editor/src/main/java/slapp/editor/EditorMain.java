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

//RTA v. 1.2.5

package slapp.editor;

import com.install4j.api.launcher.StartupNotification;
import com.teamdev.jxbrowser.engine.ChromiumBinaries;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.*;
import javafx.application.*;
import slapp.editor.main_window.MainWindow;
//import slapp.editor.simpleTests.TransEquivTest;

import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.teamdev.jxbrowser.VersionInfo;


/**
 * Includes the main and start methods for the SLAPP editor.
 */
public class EditorMain extends Application {

    /** Stage for the main window   */
    public static Stage mainStage;

    public static double RTA_SCREEN_BMP = 18;  //17
    public static double RTA_PRINT_BMP = 18;    //was 25
    public static double RTA_WIDTH_BMP = 35;

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

    public static MainWindow mainWindow;




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

        System.setProperty("javafx.dnd.deserializeObjectGraph", "true");

        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            java.nio.file.Path appDir = java.nio.file.Paths.get(System.getProperty("user.dir"));
            java.nio.file.Path bundledDjlCache = appDir.resolve("djl_cache");
            if (!java.nio.file.Files.isDirectory(bundledDjlCache)) {
                bundledDjlCache = appDir.resolve("SLAPPfiles").resolve("djl_cache");
            }
            if (!java.nio.file.Files.isDirectory(bundledDjlCache) && System.getProperty("java.home") != null) {
                bundledDjlCache = java.nio.file.Paths.get(System.getProperty("java.home")).getParent().resolve("djl_cache");
            }
            if (java.nio.file.Files.isDirectory(bundledDjlCache)) {
                System.setProperty("ENGINE_CACHE_DIR", bundledDjlCache.toAbsolutePath().toString());
            }
        }

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
        os = System.getProperty("os.name").toLowerCase();

        if (os.contains("mac")) {
            Path macHelpersDir = Paths.get(
                    System.getProperty("user.home"),
                    "Library", "Application Support", "SLAPP", "Helpers",
                    VersionInfo.version()
            );
            System.setProperty("jxbrowser.chromium.dir", macHelpersDir.toString());
        }

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

  //      Platform.runLater(() -> {

           mainWindow = new MainWindow(fileToOpen);


   //     new TransEquivTest(mainStage);


     //   new MParserMain(mainStage);
      //     new ParserMain(mainStage);
     //      new RegexTest(mainStage);
      //    new SimpleEditor(mainStage);
     //   new EncodeDecode();

     //   });



    }


    private static void openFile(String parameters) {

        Platform.runLater(() -> {
            mainWindow.autoOpen(parameters);
        });
    }



}


