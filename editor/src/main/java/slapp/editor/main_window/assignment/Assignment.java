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

package slapp.editor.main_window.assignment;

import com.gluonhq.richtextarea.model.Document;
import javafx.print.PageLayout;
import javafx.print.Paper;
import javafx.print.Printer;
import slapp.editor.PrintUtilities;
import slapp.editor.main_window.ExerciseModel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Assignment model.  Contains assignment header, exercises, and print setup info.
 */
public class Assignment implements Serializable {
    private static final long serialVersionUID = 100L;
    private AssignmentHeader header = new AssignmentHeader();
    private List<ExerciseModel> exerciseModels = new ArrayList<>();
    private double baseScale = 1.0;
    private boolean fitToPage = false;
    private PageLayoutValues pageLayoutValues;
    private transient PageLayout pageLayout = PrintUtilities.getPageLayout();


    /**
     * Construct assignment with default field values.
     */
    public Assignment(){}

    //Paper from PageLayout is not serializable.  Save the rest.
    private void writeObject (ObjectOutputStream stream) throws IOException, ClassNotFoundException {
        if (pageLayout != null)
            pageLayoutValues = new PageLayoutValues(pageLayout.getPageOrientation(), pageLayout.getLeftMargin(), pageLayout.getRightMargin(), pageLayout.getTopMargin(), pageLayout.getBottomMargin());
        stream.defaultWriteObject();
    }

    //Paper from PageLayout is not serializable.  Read the rest, accept current printer's default paper.
    private void readObject (ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        Printer printer = PrintUtilities.getPrinter();
        if (printer != null && pageLayoutValues != null) {
            Paper paper = printer.getPrinterAttributes().getDefaultPaper();
            pageLayout = printer.createPageLayout(paper, pageLayoutValues.getOrientation(), pageLayoutValues.getLeftMargin(), pageLayoutValues.getRightMargin(), pageLayoutValues.getTopMargin(), pageLayoutValues.getBottomMargin());
        }
    }

    /**
     * Get the {@link slapp.editor.main_window.ExerciseModel} at index from list
     *
     * @param index
     * @return ExerciseModel at index
     */
    public ExerciseModel getExercise(int index) {
        return exerciseModels.get(index);
    }

    /**
     * Replace the {@link slapp.editor.main_window.ExerciseModel} at index from list
     *
     * @param index index of model to be replaced
     * @param exerciseModel  replacing model
     */
    public void replaceExerciseModel(int index, ExerciseModel exerciseModel) {
        exerciseModels.set(index, exerciseModel);
    }

    /**
     * Add {@link slapp.editor.main_window.ExerciseModel} at index (pushing other members down)
     *
     * @param index index in list of ExerciseModels at which to insert
     * @param exerciseModel inserted model
     */
    public void addExerciseModel(int index, ExerciseModel exerciseModel) {
        exerciseModels.add(index, exerciseModel);
    }

    /**
     * Tests if header for this assignment has been created (with a non-empty name field).
     *
     * @return true if the assignment header has been created, and otherwise false.
     */
    public boolean hasCompletedHeader() {
        return !getHeader().getStudentName().isEmpty();
    }

    /**
     * {@link slapp.editor.main_window.assignment.AssignmentHeader} includes identifying info, assignment
     * comment, and optional items added by student or instructor.
     *
     * @return header object
     */
    public AssignmentHeader getHeader() { return header; }

    /**
     *  {@link slapp.editor.main_window.assignment.AssignmentHeader } includes identifying info, assignment
     *  comment, and optional items added by student or instructor.
     *
     * @param header header object
     */
    public void setHeader(AssignmentHeader header) { this.header = header; }


    /**
     * A List of {@link slapp.editor.main_window.ExerciseModel} that are the main contents of
     * this assignment.
     *
     * @return the list of exercise models.
     */
    public List<ExerciseModel> getExerciseModels() {
        return exerciseModels;
    }

    /**
     * A list of {@link slapp.editor.main_window.ExerciseModel} that are the main contents of
     * this asssignment
     *
     * @param exerciseModels the list of exercise models
     */
    public void setExerciseModels(List<ExerciseModel> exerciseModels) {
        this.exerciseModels = exerciseModels;
    }

    /**
     * The base print scale associated with this assignment
     *
     * @return the base scale (where 1 = 100%).
     */
    public double getBaseScale() { return baseScale; }

    /**
     * The base print scale associated with this assignment
     *
     * @param baseScale the base scale (where 1 = 100%)
     */
    public void setBaseScale(double baseScale) { this.baseScale = baseScale; }

    /**
     * Tests whether oversize nodes should be fit to page at print
     *
     * @return true if should fit, and otherwise false
     */
    public boolean isFitToPage() {    return fitToPage;  }

    /**
     * Set whether oversize nodes should be fit to page at print
     *
     * @param fitToPage true if should fit, and otherwise false
     */
    public void setFitToPage(boolean fitToPage) {   this.fitToPage = fitToPage;  }

    /**
     * {@link javafx.print.PageLayout} stored with this assignment.
     *
     * @return the page layout
     */
    public PageLayout getPageLayout() {     return pageLayout;  }

    /**
     * {@link javafx.print.PageLayout} to be stored with this assignment
     *
     * @param pageLayout  the page layout
     */
    public void setPageLayout(PageLayout pageLayout) {     this.pageLayout = pageLayout;  }
}
