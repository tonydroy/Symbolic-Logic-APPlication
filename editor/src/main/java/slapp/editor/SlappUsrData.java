package slapp.editor;

import java.io.File;
import java.io.Serializable;

public class SlappUsrData implements Serializable {
    private static final long serialVersionUID = 100L;

    private boolean instructorCheck;
    private double mainWindowX;
    private double mainWindowY;
    private double mainWindowWidth;
    private double mainWindowHeight;
    private int zoom;
    private LimitedQueue<File> recentExercises;
    private LimitedQueue<File> recentAssignments;



    public SlappUsrData() {
        instructorCheck = false;
        mainWindowX = -1;
        mainWindowY = -1;
        mainWindowWidth = -1;
        mainWindowHeight = -1;
        zoom = 100;
        recentExercises = new LimitedQueue(5);
        recentAssignments = new LimitedQueue(5);
    }

    public boolean isInstructorCheck() {
        return instructorCheck;
    }

    public void setInstructorCheck(boolean insCheck) {
        instructorCheck = insCheck;
    }

    public double getMainWindowX() {
        return mainWindowX;
    }

    public void setMainWindowX(double mainWindowX) {
        this.mainWindowX = mainWindowX;
    }

    public double getMainWindowY() {
        return mainWindowY;
    }

    public void setMainWindowY(double mainWindowY) {
        this.mainWindowY = mainWindowY;
    }

    public double getMainWindowWidth() {
        return mainWindowWidth;
    }

    public void setMainWindowWidth(double mainWindowWidth) {
        this.mainWindowWidth = mainWindowWidth;
    }

    public double getMainWindowHeight() {
        return mainWindowHeight;
    }

    public void setMainWindowHeight(double mainWindowHeight) {
        this.mainWindowHeight = mainWindowHeight;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public LimitedQueue<File> getRecentExercises() {
        return recentExercises;
    }

    public void setRecentExercises(LimitedQueue<File> recentExercises) {
        this.recentExercises = recentExercises;
    }

    public LimitedQueue<File> getRecentAssignments() {
        return recentAssignments;
    }

    public void setRecentAssignments(LimitedQueue<File> recentAssignments) {
        this.recentAssignments = recentAssignments;
    }
}
