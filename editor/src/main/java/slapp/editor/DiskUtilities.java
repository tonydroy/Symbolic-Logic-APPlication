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
import slapp.editor.main_window.ExerciseModel;
import slapp.editor.main_window.assignment.Assignment;

import java.io.*;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;

public class DiskUtilities {

    private static File exerciseDirectory = null;
    private static File assignmentDirectory = null;
    private static LimitedQueue<File> recentExerciseFiles = new LimitedQueue<>(5);
    private static LimitedQueue<File> recentAssignmentFiles = new LimitedQueue<>(5);
    private static final File userHomeFile = new File(System.getProperty("user.home"));
    private static final String slappProgDataFile = "SLAPPprog.dta";
    private static final String slappUsrDataFile = "SLAPPusr.dta";

    /**
     * Ensures the initial directory passed to FileChooser/DirectoryChooser still exists on disk;
     * falls back to user.home to prevent JavaFX IllegalArgumentException if a folder was moved/deleted.
     */
    private static File getValidDirectory(File preferredDir) {
        return (preferredDir != null && preferredDir.exists() && preferredDir.isDirectory())
                ? preferredDir
                : userHomeFile;
    }

    public static boolean saveUsrDataFile(SlappUsrData data) {
        File dataFile = new File(userHomeFile, slappUsrDataFile);

        // FIXED: Using try-with-resources and BufferedOutputStream
        try (FileOutputStream fos = new FileOutputStream(dataFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {

            oos.writeObject(data);
            return true;
        } catch (IOException e) {
            EditorAlerts.showSimpleAlert("Error saving usr file", e.getClass().getCanonicalName());
            return false;
        }
    }

    public static SlappUsrData openUsrDataFile() {
        File dataFile = new File(userHomeFile, slappUsrDataFile);
        if (!dataFile.exists()) return null;

        // FIXED: Using try-with-resources and BufferedInputStream
        try (FileInputStream fis = new FileInputStream(dataFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ObjectInputStream ois = new ObjectInputStream(bis)) {

            return (SlappUsrData) ois.readObject();
        } catch (Exception e) {
            EditorAlerts.showSimpleAlert("Error opening user data file", e.getClass().getCanonicalName());
            return null;
        }
    }

    public static boolean saveProgDataFile(SlappProgData data) {
        File dataFile = new File(userHomeFile, slappProgDataFile);

        // FIXED: Using try-with-resources and BufferedOutputStream
        try (FileOutputStream fos = new FileOutputStream(dataFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {

            oos.writeObject(data);
            return true;
        } catch (IOException e) {
            EditorAlerts.showSimpleAlert("Error saving program data file", e.getClass().getCanonicalName());
            return false;
        }
    }

    public static SlappProgData openProgDataFile() {
        File dataFile = new File(userHomeFile, slappProgDataFile);
        if (!dataFile.exists()) return null;

        // FIXED: Using try-with-resources and BufferedInputStream
        try (FileInputStream fis = new FileInputStream(dataFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ObjectInputStream ois = new ObjectInputStream(bis)) {

            return (SlappProgData) ois.readObject();
        } catch (Exception e) {
            EditorAlerts.showSimpleAlert("Error opening program data file", e.getClass().getCanonicalName());
            return null;
        }
    }

    public static boolean saveExercise(boolean saveAs, ExerciseModel exerciseModel) {
        File fileToSave;
        if (saveAs || exerciseDirectory == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SLAPP exercise (*.sle)", "*.sle"));

            fileChooser.setInitialDirectory(getValidDirectory(exerciseDirectory));
            fileChooser.setInitialFileName(exerciseModel.getExerciseName());

            fileToSave = fileChooser.showSaveDialog(EditorAlerts.getActiveStage());
            if (fileToSave == null) return false;
        } else {
            fileToSave = new File(exerciseDirectory, exerciseModel.getExerciseName() + ".sle");
        }

        exerciseDirectory = fileToSave.getParentFile();

        // FIXED: Removed redundant createNewFile() and added BufferedOutputStream
        try (FileOutputStream fs = new FileOutputStream(fileToSave, false);
             BufferedOutputStream bs = new BufferedOutputStream(fs);
             ObjectOutputStream os = new ObjectOutputStream(bs)) {

            os.writeObject(exerciseModel);

            String locationString = fileToSave.getParent() != null ? "\n\nin " + fileToSave.getParent() + "." : ".";
            EditorAlerts.fleetingPopup(fileToSave.getName() + " saved" + locationString);
            return true;
        } catch (IOException e) {
            EditorAlerts.showSimpleAlert("Error Saving", e.getClass().getCanonicalName());
            return false;
        }
    }

    public static boolean saveAssignment(boolean saveAs, Assignment assignment) {
        File fileToSave;
        if (saveAs || assignmentDirectory == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SLAPP assignment (*.sla)", "*.sla"));

            fileChooser.setInitialDirectory(getValidDirectory(assignmentDirectory));
            fileChooser.setInitialFileName(assignment.getHeader().getAssignmentName());

            fileToSave = fileChooser.showSaveDialog(EditorAlerts.getActiveStage());
            if (fileToSave == null) return false;
        } else {
            fileToSave = new File(assignmentDirectory, assignment.getHeader().getAssignmentName() + ".sla");
        }

        assignmentDirectory = fileToSave.getParentFile();

        // FIXED: Removed redundant createNewFile() and added BufferedOutputStream
        try (FileOutputStream fs = new FileOutputStream(fileToSave, false);
             BufferedOutputStream bs = new BufferedOutputStream(fs);
             ObjectOutputStream os = new ObjectOutputStream(bs)) {

            os.writeObject(assignment);

            String locationString = fileToSave.getParent() != null ? "\n\nin " + fileToSave.getParent() + "." : ".";
            EditorAlerts.fleetingPopup(fileToSave.getName() + " saved" + locationString);
            return true;
        } catch (IOException e) {
            EditorAlerts.showSimpleAlert("Error Saving", e.getClass().getCanonicalName());
            return false;
        }
    }

    public static Object openExerciseModelObject() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SLAPP exercise (*.sle)", "*.sle"));
        fileChooser.setInitialDirectory(getValidDirectory(exerciseDirectory));
        File fileToOpen = fileChooser.showOpenDialog(EditorAlerts.getActiveStage());

        return openExerciseObjectFromFile(fileToOpen);
    }

    public static Object openExerciseObjectFromFile(File fileToOpen) {
        if (fileToOpen != null) {
            if (fileToOpen.exists() && !fileToOpen.isDirectory()) {
                exerciseDirectory = fileToOpen.getParentFile();

                // FIXED: Added BufferedInputStream
                try (FileInputStream fi = new FileInputStream(fileToOpen);
                     BufferedInputStream bi = new BufferedInputStream(fi);
                     ObjectInputStream oi = new ObjectInputStream(bi)) {

                    Object exerciseModelObject = oi.readObject();

                    if (recentExerciseFiles.size() == 0 || !recentExerciseFiles.get(recentExerciseFiles.size() - 1).getPath().equals(fileToOpen.getPath())) {
                        recentExerciseFiles.add(fileToOpen);
                    }
                    return exerciseModelObject;
                } catch (IOException | ClassNotFoundException e) {
                    EditorAlerts.showSimpleAlert("Error opening file", "Error opening " + fileToOpen.getPath() + ".");
                }
            } else {
                EditorAlerts.fleetingRedPopup("Cannot find " + fileToOpen.getPath() + ".");
            }
        }
        return null;
    }

    public static Assignment openAssignment() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SLAPP assignment (*.sla)", "*.sla"));
        fileChooser.setInitialDirectory(getValidDirectory(assignmentDirectory));
        File fileToOpen = fileChooser.showOpenDialog(EditorAlerts.getActiveStage());

        return openAssignmentFromFile(fileToOpen);
    }

    public static Assignment openAssignmentFromFile(File fileToOpen) {
        if (fileToOpen != null) {
            if (fileToOpen.exists() && !fileToOpen.isDirectory()) {
                assignmentDirectory = fileToOpen.getParentFile();

                // FIXED: Added BufferedInputStream
                try (FileInputStream fi = new FileInputStream(fileToOpen);
                     BufferedInputStream bi = new BufferedInputStream(fi);
                     ObjectInputStream oi = new ObjectInputStream(bi)) {

                    Assignment assignment = (Assignment) oi.readObject();

                    if (recentAssignmentFiles.size() == 0 || !recentAssignmentFiles.get(recentAssignmentFiles.size() - 1).getPath().equals(fileToOpen.getPath())) {
                        recentAssignmentFiles.add(fileToOpen);
                    }
                    return assignment;
                } catch (IOException | ClassNotFoundException e) {
                    EditorAlerts.showSimpleAlert("Error opening file", "Error opening " + fileToOpen.getPath() + ".");
                }
            } else {
                EditorAlerts.fleetingRedPopup("Cannot find " + fileToOpen.getPath() + ".");
            }
        }
        return null;
    }

    public static ExerciseModel getExerciseModelFromFile(File fileToOpen) {
        if (fileToOpen != null) {
            // FIXED: Added BufferedInputStream
            try (FileInputStream fi = new FileInputStream(fileToOpen);
                 BufferedInputStream bi = new BufferedInputStream(fi);
                 ObjectInputStream oi = new ObjectInputStream(bi)) {

                return (ExerciseModel) oi.readObject();
            } catch (IOException | ClassNotFoundException e) {
                EditorAlerts.showSimpleAlert("Error opening file", e.getClass().getCanonicalName() + ", " + fileToOpen.getName());
            }
        }
        return null;
    }

    public static File getDirectory(DirType type) {
        File startingDir = userHomeFile;
        if (type == DirType.EXERCISE && exerciseDirectory != null) startingDir = exerciseDirectory;
        if (type == DirType.ASSIGNMENT && assignmentDirectory != null) startingDir = assignmentDirectory;

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(getValidDirectory(startingDir));
        File directory = directoryChooser.showDialog(EditorAlerts.getActiveStage());

        if (directory != null) {
            if (type == DirType.EXERCISE) exerciseDirectory = directory;
            if (type == DirType.ASSIGNMENT) assignmentDirectory = directory;
        }
        return directory;
    }

    // FIXED: Safely typed to prevent unchecked casting warnings, and safely checks for null directory arrays
    public static ObservableList<File> getFileListFromDir(File directory, String extension) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) return FXCollections.observableArrayList();

        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(extension));
        if (files == null) return FXCollections.observableArrayList();

        ObservableList<File> list = FXCollections.observableArrayList(files);
        Collections.sort(list, new AlphanumFileComparator());

        return list;
    }

    public enum DirType {
        EXERCISE,
        ASSIGNMENT;
    }

    public static LimitedQueue<File> getRecentExerciseFiles() { return recentExerciseFiles; }
    public static LimitedQueue<File> getRecentAssignmentFiles() { return recentAssignmentFiles; }
    public static void setRecentExerciseFiles(LimitedQueue<File> recentExerciseFiles) { DiskUtilities.recentExerciseFiles = recentExerciseFiles; }
    public static void setRecentAssignmentFiles(LimitedQueue<File> recentAssignmentFiles) { DiskUtilities.recentAssignmentFiles = recentAssignmentFiles; }
}