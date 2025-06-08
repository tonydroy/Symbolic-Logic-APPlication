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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import slapp.editor.main_window.assignment.Assignment;
import slapp.editor.main_window.ExerciseModel;

import java.io.*;
import java.util.Collections;

/**
 * Utility methods for disk operations
 */
public class DiskUtilities {

    private static File exerciseDirectory = null;
    private static File assignmentDirectory = null;
    private static LimitedQueue<File> recentExerciseFiles = new LimitedQueue<>(5);
    private static LimitedQueue<File> recentAssignmentFiles = new LimitedQueue<>(5);
    private static File userHomeFile = new File(System.getProperty("user.home"));
    private static String slappProgDataFile = "SLAPPprog.dta";
    private static String slappUsrDataFile = "SLAPPusr.dta";

    public static boolean saveUsrDataFile(SlappUsrData data) {
        boolean success = false;
        try {
            File dataFile = new File(userHomeFile, slappUsrDataFile);
            FileOutputStream fileOutputStream = new FileOutputStream(dataFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(data);
            objectOutputStream.close();
            success = true;
        }
        catch (IOException e) {
            //     e.printStackTrace();
            EditorAlerts.showSimpleAlert("Error saving usr file", e.getClass().getCanonicalName());
            return success;
        }
        return success;
    }

    public static SlappUsrData openUsrDataFile() {

        try {
            File dataFile = new File(userHomeFile, slappUsrDataFile);
            if (dataFile.exists()) {
                FileInputStream fileInputStream = new FileInputStream(dataFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                SlappUsrData data = (SlappUsrData) objectInputStream.readObject();
                objectInputStream.close();
                return data;
            }
            else return null;
        }
        catch (Exception e) {
            //     e.printStackTrace();
            EditorAlerts.showSimpleAlert("Error opening user data file", e.getClass().getCanonicalName());
            return null;
        }
    }



    public static boolean saveProgDataFile(SlappProgData data) {
        boolean success = false;
        try {
            File dataFile = new File(userHomeFile, slappProgDataFile);
            FileOutputStream fileOutputStream = new FileOutputStream(dataFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(data);
            objectOutputStream.close();
            success = true;
        }
        catch (IOException e) {
       //     e.printStackTrace();
            EditorAlerts.showSimpleAlert("Error saving program data file", e.getClass().getCanonicalName());
            return success;
        }
        return success;
    }

    public static SlappProgData openProgDataFile() {

        try {
            File dataFile = new File(userHomeFile, slappProgDataFile);
            if (dataFile.exists()) {
                FileInputStream fileInputStream = new FileInputStream(dataFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                SlappProgData data = (SlappProgData) objectInputStream.readObject();
                objectInputStream.close();
                return data;
            }
            else return null;
        }
        catch (Exception e) {
       //     e.printStackTrace();
            EditorAlerts.showSimpleAlert("Error opening program data file", e.getClass().getCanonicalName());
            return null;
        }
    }

    /**
     * Save exercise
     *
     * @param saveAs if true or exercise directrory is null save as, otherwise save to current exercise directory
     * @param exerciseModel the model to be saved
     * @return true if successful and otherwise false
     */
    public static boolean saveExercise(boolean saveAs, ExerciseModel exerciseModel) {
        boolean success = false;
        File fileToSave;
        if (saveAs || exerciseDirectory == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SLAPP exercise (*.sle)", "*.sle"));
            if (exerciseDirectory != null)
                fileChooser.setInitialDirectory(exerciseDirectory);
            else
                fileChooser.setInitialDirectory(userHomeFile);
            fileChooser.setInitialFileName(exerciseModel.getExerciseName() + ".sle");
            fileToSave = fileChooser.showSaveDialog(EditorMain.mainStage);
            if (fileToSave == null)
                return success;
        }
        else fileToSave = new File(exerciseDirectory, exerciseModel.getExerciseName() + ".sle");

        try {
            fileToSave.createNewFile();
        }
        catch (IOException e) {
            EditorAlerts.showSimpleAlert("Cannot Save", "No access to save " + fileToSave.getPath());
            return success;
        }
        //legit fileToSave
        exerciseDirectory = fileToSave.getParentFile();

        try (FileOutputStream fs = new FileOutputStream(fileToSave, false); ObjectOutputStream os = new ObjectOutputStream(fs);) {
            os.writeObject(exerciseModel);

            String locationString = ".";
            if (fileToSave.getParent() != null) locationString = "\n\nin " + fileToSave.getParent() +".";
            EditorAlerts.fleetingPopup(fileToSave.getName() + " saved" + locationString);
            success = true;
        }
        catch (IOException e) {
//            e.printStackTrace();
            EditorAlerts.showSimpleAlert("Error Saving", e.getClass().getCanonicalName());
            return success;
        }
        return success;
    }

    /**
     * Save assignment
     *
     * @param saveAs if true or assignment directory is null save as, otherwise save in current assignment directory
     * @param assignment the assignment to save
     * @return true if successful and otherwise false
     */
    public static boolean saveAssignment(boolean saveAs, Assignment assignment) {
        boolean success = false;
        File fileToSave;
        if (saveAs || assignmentDirectory == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SLAPP assignment (*.sla)", "*.sla"));
            if (assignmentDirectory != null)
                fileChooser.setInitialDirectory(assignmentDirectory);
            else
                fileChooser.setInitialDirectory(userHomeFile);
            fileChooser.setInitialFileName(assignment.getHeader().getAssignmentName() + ".sla");
            fileToSave = fileChooser.showSaveDialog(EditorMain.mainStage);
            if (fileToSave == null)
                return success;
        }
        else fileToSave = new File(assignmentDirectory, assignment.getHeader().getAssignmentName() + ".sla");

        try {
            fileToSave.createNewFile();
        }
        catch (IOException e) {
            EditorAlerts.showSimpleAlert("Cannot Save", "No access to save " + fileToSave.getPath());
            return success;
        }
        //legit fileToSave
        assignmentDirectory = fileToSave.getParentFile();

        try (FileOutputStream fs = new FileOutputStream(fileToSave, false); ObjectOutputStream os = new ObjectOutputStream(fs);) {
            os.writeObject(assignment);

            String locationString = ".";
            if (fileToSave.getParent() != null) locationString = "in\n\n" + fileToSave.getParent() +".";
            EditorAlerts.fleetingPopup(fileToSave.getName() + " saved" + locationString);
            success = true;
        }
        catch (IOException e) {
            e.printStackTrace();
            EditorAlerts.showSimpleAlert("Error Saving", e.getClass().getCanonicalName());
            return success;
        }
        return success;
    }

    /**
     * Choose and open exercise model (as object) from disk
     *
     * @return the exercise model object
     */
    public static Object openExerciseModelObject() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SLAPP exercise (*.sle)", "*.sle"));
        if (exerciseDirectory != null)
            fileChooser.setInitialDirectory(exerciseDirectory);
        else
            fileChooser.setInitialDirectory(userHomeFile);
        File fileToOpen = fileChooser.showOpenDialog(EditorMain.mainStage);

        return openExerciseObjectFromFile(fileToOpen);
    }

    public static Object openExerciseObjectFromFile(File fileToOpen) {
        Object exerciseModelObject = null;

        if (fileToOpen != null) {
            if (fileToOpen.exists() && !fileToOpen.isDirectory()) {
                exerciseDirectory = fileToOpen.getParentFile();
                try (FileInputStream fi = new FileInputStream(fileToOpen); ObjectInputStream oi = new ObjectInputStream(fi);) {
                    exerciseModelObject = oi.readObject();
                    recentExerciseFiles.add(fileToOpen);

                } catch (IOException | ClassNotFoundException e) {
                    //                            e.printStackTrace();
                    EditorAlerts.showSimpleAlert("Error opening file", fileToOpen.getPath() + " is not compatible with this version of SLAPP.\nCannot open.");
                }
            }
            else EditorAlerts.fleetingRedPopup("Cannot find " + fileToOpen.getPath() + ".");
        }

        return exerciseModelObject;
    }

    /**
     * Choose and open assignment from disk
     *
     * @return the assignment
     */
    public static Assignment openAssignment() {
 //       Assignment assignment = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SLAPP assignment (*.sla)", "*.sla"));
        if (assignmentDirectory != null)
            fileChooser.setInitialDirectory(assignmentDirectory);
        else
            fileChooser.setInitialDirectory(userHomeFile);
        File fileToOpen = fileChooser.showOpenDialog(EditorMain.mainStage);

        return openAssignmentFromFile(fileToOpen);
    }

    public static Assignment openAssignmentFromFile(File fileToOpen) {
        Assignment assignment = null;

        if (fileToOpen != null) {
            if (fileToOpen.exists() && !fileToOpen.isDirectory()) {
                assignmentDirectory = fileToOpen.getParentFile();
                try (FileInputStream fi = new FileInputStream(fileToOpen); ObjectInputStream oi = new ObjectInputStream(fi);) {
                    assignment = (Assignment) oi.readObject();
                    recentAssignmentFiles.add(fileToOpen);
                } catch (IOException | ClassNotFoundException e) {
//                e.printStackTrace();
                    EditorAlerts.showSimpleAlert("Error opening file", fileToOpen.getPath() + " is not compatible with this version of SLAPP.\nCannot open.");
                }
            }
            else EditorAlerts.fleetingRedPopup("Cannot find " + fileToOpen.getPath() + ".");
        }

        return assignment;
    }

    /**
     * Get exercise model from disk file
     *
     * @param fileToOpen the file
     * @return the exercise model
     */
    public static ExerciseModel getExerciseModelFromFile(File fileToOpen) {
        ExerciseModel exerciseModel = null;
        if (fileToOpen != null) {
            try (FileInputStream fi = new FileInputStream(fileToOpen); ObjectInputStream oi = new ObjectInputStream(fi);) {
                exerciseModel = (ExerciseModel) oi.readObject();
            } catch (IOException | ClassNotFoundException e) {
 //               e.printStackTrace();
                EditorAlerts.showSimpleAlert("Error opening file", e.getClass().getCanonicalName() + ", " + fileToOpen.getName());
            }
        }
        return exerciseModel;
    }

    /**
     * Get a directory from disk
     *
     * @param type the type EXERCISE/ASSIGNMENT of directory to open
     * @return the directory
     */
    public static File getDirectory(DirType type) {
        File startingDir = userHomeFile;
        if (type == DirType.EXERCISE && exerciseDirectory != null) startingDir = exerciseDirectory;
        if (type == DirType.ASSIGNMENT && assignmentDirectory != null) startingDir = assignmentDirectory;
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(startingDir);
        File directory = directoryChooser.showDialog(EditorMain.mainStage);
        if (directory != null && type == DirType.EXERCISE) exerciseDirectory = directory;
        if (directory != null && type == DirType.ASSIGNMENT) assignmentDirectory = directory;
        return directory;
    }


    /**
     * Get sorted list of files (of some type) from directory
     *
     * @param directory the directory File
     * @param extension the type extgension
     * @return the File list
     */
    public static ObservableList<File> getFileListFromDir(File directory, String extension) {
        ObservableList list = null;
        FilenameFilter filenameFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(extension);
            }
        };
        File[] files = directory.listFiles(filenameFilter);
        list = FXCollections.observableArrayList(files);

        Collections.sort(list, new AlphanumFileComparator());

        return list;
    }


    /**
     * Enum to differentiate exercise/assignment directory types
     */
    public enum DirType {
        EXERCISE,
        ASSIGNMENT;

    }

    public static LimitedQueue<File> getRecentExerciseFiles() {
        return recentExerciseFiles;
    }

    public static LimitedQueue<File> getRecentAssignmentFiles() {
        return recentAssignmentFiles;
    }

    public static void setRecentExerciseFiles(LimitedQueue<File> recentExerciseFiles) {
        DiskUtilities.recentExerciseFiles = recentExerciseFiles;
    }

    public static void setRecentAssignmentFiles(LimitedQueue<File> recentAssignmentFiles) {
        DiskUtilities.recentAssignmentFiles = recentAssignmentFiles;
    }
}
