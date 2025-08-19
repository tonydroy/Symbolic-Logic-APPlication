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

import slapp.editor.EditorAlerts;
import slapp.editor.ab_explain.ABcreate;
import slapp.editor.ab_explain.ABexercise;
import slapp.editor.ab_explain.ABmodel;
import slapp.editor.abefg_explain.ABEFGcreate;
import slapp.editor.abefg_explain.ABEFGexercise;
import slapp.editor.abefg_explain.ABEFGmodel;
import slapp.editor.derivation.DerivationCreate;
import slapp.editor.derivation.DerivationExercise;
import slapp.editor.derivation.DerivationModel;
import slapp.editor.derivation_explain.DrvtnExpCreate;
import slapp.editor.derivation_explain.DrvtnExpExercise;
import slapp.editor.derivation_explain.DrvtnExpModel;
import slapp.editor.free_form.FreeFormCreate;
import slapp.editor.free_form.FreeFormExercise;
import slapp.editor.free_form.FreeFormModel;
import slapp.editor.horizontal_tree.HorizontalTreeCreate;
import slapp.editor.horizontal_tree.HorizontalTreeExercise;
import slapp.editor.horizontal_tree.HorizontalTreeModel;
import slapp.editor.map_abexplain.MapABExpCreate;
import slapp.editor.map_abexplain.MapABExpExercise;
import slapp.editor.map_abexplain.MapABExpModel;
import slapp.editor.page_editor.PageEditCreate;
import slapp.editor.page_editor.PageEditExercise;
import slapp.editor.page_editor.PageEditModel;
import slapp.editor.simple_edit.SimpleEditCreate;
import slapp.editor.simple_edit.SimpleEditExercise;
import slapp.editor.simple_edit.SimpleEditModel;
import slapp.editor.simple_trans.SimpleTransCreate;
import slapp.editor.simple_trans.SimpleTransExercise;
import slapp.editor.simple_trans.SimpleTransModel;
import slapp.editor.truth_table.TruthTableCreate;
import slapp.editor.truth_table.TruthTableExercise;
import slapp.editor.truth_table.TruthTableModel;
import slapp.editor.truth_table_explain.TruthTableExpCreate;
import slapp.editor.truth_table_explain.TruthTableExpExercise;
import slapp.editor.truth_table_explain.TruthTableExpModel;
import slapp.editor.truth_table_generate.TruthTableGenCreate;
import slapp.editor.truth_table_generate.TruthTableGenExercise;
import slapp.editor.truth_table_generate.TruthTableGenModel;
import slapp.editor.vert_tree_abefexplain.VerticalTreeABEFExpCreate;
import slapp.editor.vert_tree_abefexplain.VerticalTreeABEFExpExercise;
import slapp.editor.vert_tree_abefexplain.VerticalTreeABEFExpModel;
import slapp.editor.vert_tree_abexplain.VerticalTreeABExpCreate;
import slapp.editor.vert_tree_abexplain.VerticalTreeABExpExercise;
import slapp.editor.vert_tree_abexplain.VerticalTreeABExpModel;
import slapp.editor.vert_tree_explain.VerticalTreeExpCreate;
import slapp.editor.vert_tree_explain.VerticalTreeExpExercise;
import slapp.editor.vert_tree_explain.VerticalTreeExpModel;
import slapp.editor.vertical_tree.VerticalTreeCreate;
import slapp.editor.vertical_tree.VerticalTreeExercise;
import slapp.editor.vertical_tree.VerticalTreeModel;

/**
 * Class with switches depending on the model class or exercise type
 */
public class TypeSelectorFactories {
    private MainWindow mainWindow;

    /**
     * Create the factories
     *
     * @param mainWindow the main window
     */
    public TypeSelectorFactories(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    /**
     * Starting with model object, return an exercise of its type
     *
     * @param objectModel the model object
     *
     * @return the exercise
     */
    public Exercise getExerciseFromModelObject(Object objectModel) {
        String modelClassName = objectModel.getClass().getSimpleName();

        switch (modelClassName) {

            case "SimpleEditModel": {
                SimpleEditModel editModel = (SimpleEditModel) objectModel;
                return new SimpleEditExercise(editModel, mainWindow);
            }
            case "SimpleTransModel": {
                SimpleTransModel transModel = (SimpleTransModel) objectModel;
                return new SimpleTransExercise(transModel, mainWindow);
            }
            case "PageEditModel": {
                PageEditModel editModel = (PageEditModel) objectModel;
                return new PageEditExercise(editModel, mainWindow);
            }
            case "ABmodel": {
                ABmodel abModel = (ABmodel) objectModel;
                return new ABexercise(abModel, mainWindow);
            }
            case "ABEFGmodel": {
                ABEFGmodel abefgModel = (ABEFGmodel) objectModel;
                return new ABEFGexercise(abefgModel, mainWindow);
            }
            case "DerivationModel": {
                DerivationModel derivationModel = (DerivationModel) objectModel;
                return new DerivationExercise(derivationModel, mainWindow);
            }
            case "DrvtnExpModel": {
                DrvtnExpModel drvtnExpModel = (DrvtnExpModel) objectModel;
                return new DrvtnExpExercise(drvtnExpModel, mainWindow);
            }
            case "TruthTableModel": {
                TruthTableModel truthTableModel = (TruthTableModel) objectModel;
                return new TruthTableExercise(truthTableModel, mainWindow);
            }
            case "TruthTableExpModel": {
                TruthTableExpModel truthTableExpModel = (TruthTableExpModel) objectModel;
                return new TruthTableExpExercise(truthTableExpModel, mainWindow);
            }
            case "TruthTableGenModel": {
                TruthTableGenModel truthTableGenModel = (TruthTableGenModel) objectModel;
                return new TruthTableGenExercise(truthTableGenModel, mainWindow);
            }
            case "MapABExpModel": {
                MapABExpModel mapABExpModel = (MapABExpModel) objectModel;
                return new MapABExpExercise(mapABExpModel, mainWindow);
            }
            case "VerticalTreeModel": {
                VerticalTreeModel verticalTreeModel = (VerticalTreeModel) objectModel;
                return new VerticalTreeExercise(verticalTreeModel, mainWindow);
            }
            case "VerticalTreeExpModel": {
                VerticalTreeExpModel verticalTreeExpModel = (VerticalTreeExpModel) objectModel;
                return new VerticalTreeExpExercise(verticalTreeExpModel, mainWindow);
            }
            case "VerticalTreeABExpModel": {
                VerticalTreeABExpModel verticalTreeABExpModel = (VerticalTreeABExpModel) objectModel;
                return new VerticalTreeABExpExercise(verticalTreeABExpModel, mainWindow);
            }
            case "VerticalTreeABEFExpModel": {
                VerticalTreeABEFExpModel verticalTreeABEFExpModel = (VerticalTreeABEFExpModel) objectModel;
                return new VerticalTreeABEFExpExercise(verticalTreeABEFExpModel, mainWindow);
            }
            case "HorizontalTreeModel": {
                HorizontalTreeModel horizontalTreeModel = (HorizontalTreeModel) objectModel;
                return new HorizontalTreeExercise(horizontalTreeModel, mainWindow);
            }
            case "FreeFormModel": {
                FreeFormModel freeFormModel = (FreeFormModel) objectModel;
                return new FreeFormExercise(freeFormModel, mainWindow);
            }

            default: {
                EditorAlerts.showSimpleAlert("Cannot Open", "I do not recognize this as a SLAPP exercise file");
                return null;
            }
        }
    }

    /**
     * From model object, if not started, open create window for (revision of) exercise of that type.
     *
     * @param objectModel the model object
     */
    public void createRevisedExerciseFromModelObject(Object objectModel) {
        String modelClassName = objectModel.getClass().getSimpleName();
        switch (modelClassName) {

            case "SimpleEditModel": {
                SimpleEditModel editModel = (SimpleEditModel) objectModel;
                if (editModel.isStarted()) {
                    EditorAlerts.showSimpleAlert("Cannot Open", "This exercise appears to have the content area modified.  Cannot open in create window.");
                    break;
                }
                SimpleEditCreate simpleEditCreate = new SimpleEditCreate(mainWindow, editModel);
                break;
            }
            case "SimpleTransModel": {
                SimpleTransModel transModel = (SimpleTransModel) objectModel;
                if (transModel.isStarted()) {
                    EditorAlerts.showSimpleAlert("Cannot Open", "This exercise appears to have the content area modified.  Cannot open in create window.");
                    break;
                }
                SimpleTransCreate simpleTransCreate = new SimpleTransCreate(mainWindow, transModel);
                break;
            }
            case "PageEditModel": {
                PageEditModel editModel = (PageEditModel) objectModel;
                if (editModel.isStarted()) {
                    EditorAlerts.showSimpleAlert("Cannot Open", "This exercise appears to have the content area modified.  Cannot open in create window.");
                    break;
                }
                PageEditCreate pageEditCreate = new PageEditCreate(mainWindow, editModel);
                break;
            }
            case "ABmodel": {
                ABmodel abModel = (ABmodel) objectModel;
                if (abModel.isStarted()) {
                    EditorAlerts.showSimpleAlert("Cannot Open", "This exercise appears to have the content area modified.  Cannot open in create window.");
                    break;
                }
                ABcreate abCreate = new ABcreate(mainWindow, abModel);
                break;
            }
            case "ABEFGmodel": {
                ABEFGmodel abefgModel = (ABEFGmodel) objectModel;
                if (abefgModel.isStarted()) {
                    EditorAlerts.showSimpleAlert("Cannot Open", "This exercise appears to have the content area modified.  Cannot open in create window.");
                    break;
                }
                ABEFGcreate abefgCreate = new ABEFGcreate(mainWindow, abefgModel);
                break;
            }
            case "DerivationModel": {
                DerivationModel derivationModel = (DerivationModel) objectModel;
                if (derivationModel.isStarted()) {
                    EditorAlerts.showSimpleAlert("Cannot Open", "This exercise appears to have the content area modified.  Cannot open in create window.");
                    break;
                }
                DerivationCreate derivationCreate = new DerivationCreate(mainWindow, derivationModel);
                break;
            }
            case "DrvtnExpModel": {
                DrvtnExpModel drvtnExpModel = (DrvtnExpModel) objectModel;
                if (drvtnExpModel.isStarted()) {
                    EditorAlerts.showSimpleAlert("Cannot Open", "This exercise appears to have the content area modified.  Cannot open in create window.");
                    break;
                }
                DrvtnExpCreate drvtnExpCreate = new DrvtnExpCreate(mainWindow, drvtnExpModel);
                break;
            }
            case "TruthTableModel": {
                TruthTableModel truthTableModel = (TruthTableModel) objectModel;
                if (truthTableModel.isStarted()) {
                    EditorAlerts.showSimpleAlert("Cannot Open", "This exercise appears to have the content area modified.  Cannot open in create window.");
                    break;
                }
                TruthTableCreate truthTableCreate = new TruthTableCreate(mainWindow, truthTableModel);
                break;
            }
            case "TruthTableExpModel": {
                TruthTableExpModel truthTableExpModel = (TruthTableExpModel) objectModel;
                if (truthTableExpModel.isStarted()) {
                    EditorAlerts.showSimpleAlert("Cannot Open", "This exercise appears to have the content area modified.  Cannot open in create window.");
                    break;
                }
                TruthTableExpCreate truthTableExpCreate = new TruthTableExpCreate(mainWindow, truthTableExpModel);
                break;
            }
            case "TruthTableGenModel": {
                TruthTableGenModel truthTableGenModel = (TruthTableGenModel) objectModel;
                if (truthTableGenModel.isStarted()) {
                    EditorAlerts.showSimpleAlert("Cannot Open", "This exercise appears to have the content area modified.  Cannot open in create window.");
                    break;
                }
                TruthTableGenCreate truthTableGenCreate = new TruthTableGenCreate(mainWindow, truthTableGenModel);
                break;
            }
            case "MapABExpModel": {
                MapABExpModel mapABExpModel = (MapABExpModel) objectModel;
                if (mapABExpModel.isStarted()) {
                    EditorAlerts.showSimpleAlert("Cannot Open", "This exercise appears to have the content area modified.  Cannot open in create window.");
                    break;
                }
                MapABExpCreate mapABExpCreate = new MapABExpCreate(mainWindow, mapABExpModel);
                break;
            }
            case "VerticalTreeModel": {
                VerticalTreeModel verticalTreeModel = (VerticalTreeModel) objectModel;
                if (verticalTreeModel.isStarted()) {
                    EditorAlerts.showSimpleAlert("Cannot Open", "This exercise appears to have the content area modified.  Cannot open in create window.");
                    break;
                }
                VerticalTreeCreate verticalTreeCreate = new VerticalTreeCreate(mainWindow, verticalTreeModel);
                break;
            }
            case "VerticalTreeExpModel": {
                VerticalTreeExpModel verticalTreeExpModel = (VerticalTreeExpModel) objectModel;
                if (verticalTreeExpModel.isStarted()) {
                    EditorAlerts.showSimpleAlert("Cannot Open", "This exercise appears to have the content area modified.  Cannot open in create window.");
                    break;
                }
                VerticalTreeExpCreate verticalTreeExpCreate = new VerticalTreeExpCreate(mainWindow, verticalTreeExpModel);
                break;
            }
            case "VerticalTreeABExpModel": {
                VerticalTreeABExpModel verticalTreeABExpModel = (VerticalTreeABExpModel) objectModel;
                if (verticalTreeABExpModel.isStarted()) {
                    EditorAlerts.showSimpleAlert("Cannot Open", "This exercise appears to have the content area modified.  Cannot open in create window.");
                    break;
                }
                VerticalTreeABExpCreate verticalTreeABExpCreate = new VerticalTreeABExpCreate(mainWindow, verticalTreeABExpModel);
                break;
            }
            case "VerticalTreeABEFExpModel": {
                VerticalTreeABEFExpModel verticalTreeABEFExpModel = (VerticalTreeABEFExpModel) objectModel;
                if (verticalTreeABEFExpModel.isStarted()) {
                    EditorAlerts.showSimpleAlert("Cannot Open", "This exercise appears to have the content area modified.  Cannot open in create window.");
                    break;
                }
                VerticalTreeABEFExpCreate verticalTreeABEFExpCreate = new VerticalTreeABEFExpCreate(mainWindow, verticalTreeABEFExpModel);
                break;
            }
            case "HorizontalTreeModel": {
                HorizontalTreeModel horizontalTreeModel = (HorizontalTreeModel) objectModel;
                if (horizontalTreeModel.isStarted()) {
                    EditorAlerts.showSimpleAlert("Cannot Open", "This exercise appears to have the content area modified.  Cannot open in create window.");
                    break;
                }
                HorizontalTreeCreate horizontalTreeCreate = new HorizontalTreeCreate(mainWindow, horizontalTreeModel);
                break;
            }
            case "FreeFormModel": {
                FreeFormModel freeFormModel = (FreeFormModel) objectModel;
                if (freeFormModel.isStarted()) {
                    EditorAlerts.showSimpleAlert("Cannot Open", "This exercise appears to have the content area modified.  Cannot open in create window.");
                    break;
                }
                FreeFormCreate freeFormCreate = new FreeFormCreate(mainWindow, freeFormModel);
                break;
            }

            default: {
                EditorAlerts.showSimpleAlert("Cannot Open", "I do not recognize this as a SLAPP exercise file.");
            }
        }
    }


    /**
     * From exercise type, open create window for (new) exercise of that type.
     *
     * @param type the exercise type
     */
    public void createExerciseOfType(ExerciseType type) {

        switch(type) {

            case AB_EXPLAIN: {
                ABcreate abCreate = new ABcreate(mainWindow);
                break;
            }
            case ABEFG_EXPLAIN: {
                ABEFGcreate abefgCreate = new ABEFGcreate(mainWindow);
                break;
            }
            case DERIVATION: {
                DerivationCreate derivationCreate = new DerivationCreate(mainWindow);
                break;
            }
            case DRVTN_EXP: {
                DrvtnExpCreate drvtnExpCreate = new DrvtnExpCreate(mainWindow);
                break;
            }
            case HORIZONTAL_TREE: {
               HorizontalTreeCreate horizontalTreeCreate = new HorizontalTreeCreate(mainWindow);
                break;
            }
            case PAGE_EDIT: {
                PageEditCreate pageEditCreate = new PageEditCreate(mainWindow);
                break;
            }
            case SIMPLE_EDIT: {
                SimpleEditCreate simpleEditCreate = new SimpleEditCreate(mainWindow);
                break;
            }
            case SIMPLE_TRANS: {
                SimpleTransCreate simpleTransCreate = new SimpleTransCreate(mainWindow);
                break;
            }
            case TRUTH_TABLE: {
                TruthTableCreate truthTableCreate = new TruthTableCreate(mainWindow);
                break;
            }
            case TRUTH_TABLE_ABEXP: {
                TruthTableExpCreate truthTableExpCreate = new TruthTableExpCreate(mainWindow);
                break;
            }
            case TRUTH_TABLE_GENERATE: {
                TruthTableGenCreate truthTableGenCreate = new TruthTableGenCreate(mainWindow);
                break;
            }
            case MAP_AB_EXPLAIN: {
                MapABExpCreate mapABExpCreate = new MapABExpCreate(mainWindow);
                break;
            }
            case VERTICAL_TREE: {
                VerticalTreeCreate verticalTreeCreate = new VerticalTreeCreate(mainWindow);
                break;
            }
            case VERTICAL_TREE_EXP: {
                VerticalTreeExpCreate verticalTreeExpCreate = new VerticalTreeExpCreate(mainWindow);
                break;
            }
            case VERTICAL_TREE_ABEXP: {
                VerticalTreeABExpCreate verticalTreeABExpCreate = new VerticalTreeABExpCreate(mainWindow);
                break;
            }
            case VERTICAL_TREE_ABEFEXP: {
                VerticalTreeABEFExpCreate verticalTreeABEFExpCreate = new VerticalTreeABEFExpCreate(mainWindow);
                break;
            }
            case FREE_FORM: {
                FreeFormCreate freeFormCreate = new FreeFormCreate(mainWindow);
                break;
            }


        }
    }




}
