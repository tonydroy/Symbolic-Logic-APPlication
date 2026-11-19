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

package slapp.editor.front_page;

import javafx.scene.Node;
import javafx.scene.control.Spinner;
import slapp.editor.main_window.Exercise;
import slapp.editor.main_window.ExerciseModel;
import slapp.editor.main_window.MainWindow;
import java.util.List;


/**
 * Dummy exercise to fill the empty opening screen.
 */
public class FrontPageExercise implements Exercise<FrontPageModel, FrontPageView> {
    private FrontPageView view;
    private FrontPageModel model;
    private MainWindow mainWindow;

    // SCORCHED EARTH RULE 2: Kill Switch for asynchronous event interception during teardown
    private boolean isTornDown = false;

    public FrontPageExercise(MainWindow mainWindow) {
        this.view = new FrontPageView(mainWindow.getMainView());
        this.model = new FrontPageModel();
        this.mainWindow = mainWindow;
    }

    @Override
    public Node getFFViewNode() {return null;}
    @Override
    public Spinner getFFHeightSpinner() {return null;}
    @Override
    public Spinner getFFWidthSpinner() {return null;}
    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) {return null;}
    @Override
    public FrontPageModel getExerciseModel() {return model; }
    @Override
    public FrontPageView getExerciseView() { return view; }
    @Override
    public void saveExercise(boolean saveAs) { }

    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) { return null; }
    @Override
    public Exercise<FrontPageModel, FrontPageView> resetExercise() { return new FrontPageExercise(mainWindow); }
    @Override
    public boolean isExerciseModified() { return false; }
    @Override
    public void setExerciseModified(boolean modified) { }

    @Override
    public ExerciseModel<FrontPageModel> getExerciseModelFromView() { return (ExerciseModel) new FrontPageModel(); }

    // SCORCHED EARTH RULE 1 & 5: Cleanup cascading and isolation
    @Override
    public void cleanup() {
        // SCORCHED EARTH RULE 2: Engage Kill Switch
        isTornDown = true;

        // SCORCHED EARTH RULE 1: Cascade cleanup to view
        try { if (view != null) view.cleanup(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Final Nullification
        try { this.view = null; } catch (Exception e) {}
        try { this.model = null; } catch (Exception e) {}
        try { this.mainWindow = null; } catch (Exception e) {}
    }
}