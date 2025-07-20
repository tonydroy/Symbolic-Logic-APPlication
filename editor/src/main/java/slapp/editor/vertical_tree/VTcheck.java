package slapp.editor.vertical_tree;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.event.ActionEvent;
import slapp.editor.EditorAlerts;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ParseUtilities;
import slapp.editor.vertical_tree.drag_drop.TreeFormulaBox;

import java.util.List;

public class VTcheck {
    private VerticalTreeExercise vtExercise;
    private VerticalTreeView vtView;
    private VerticalTreeModel vtModel;
    private VTcheckSetup checkSetup;

    private int checkMax;
    private int checkTries;
    private boolean checkFinal;
    private boolean checkSuccess;

    VTcheck(VerticalTreeExercise vtExercise) {
        this.vtExercise = vtExercise;
        this.vtView = vtExercise.getExerciseView();
        this.vtModel = vtExercise.getExerciseModel();
        setRightControlBox();

        checkSetup = vtExercise.getExerciseModel().getCheckSetup();
        if (checkSetup == null) { checkSetup = new VTcheckSetup(); }
        checkSuccess = checkSetup.isCheckSuccess();
        checkFinal = checkSetup.isCheckFinal();
        if (checkFinal) {
            vtView.setCheckColor(Color.LAWNGREEN);
            vtView.setCheckElementsColor(Color.GREEN);
            vtView.setCheckMessage("Tree");
        }
        else {
            vtView.setCheckColor(Color.ORCHID);
            vtView.setCheckElementsColor(Color.PURPLE);
            vtView.setCheckMessage("Nodes: Good");
        }
        if (checkSuccess) vtView.activateBigCheck();
        else vtView.deactivateBigCheck();

        checkMax = checkSetup.getCheckMax();
        checkTries = checkSetup.getCheckTries();
        updateCheckCounter();
    }



    private void setRightControlBox() {

        vtView.getCheckButton().setOnAction(e -> {
            System.out.println("check");
        });

//        vtView.getCheckProgButton().setOnAction(e -> {
//            System.out.println("check progress");
//        });

        vtView.getstaticHelpButton().setDisable(!vtModel.getCheckSetup().isStaticHelpButton());

        vtView.getstaticHelpButton().setOnAction(e -> {
            AnchorPane mainPane =   vtView.getRootLayout().getMainPane();
            ObservableList<Node> nodes = mainPane.getChildren();
            for (Node node : nodes) {
                if (node instanceof TreeFormulaBox) {
                    TreeFormulaBox treeBox = (TreeFormulaBox) node;
                    if (treeBox.isCircled()) {
                        List<Expression> expressions = getCircleText(treeBox);

                        int size = expressions.size();
                        System.out.println("size: " + size);
                        if (size > 0)  {
                            System.out.println("type: " + expressions.get(size -1).getType());
                            EditorAlerts.showSimpleTxtListAlert("circle text:", expressions.get(size - 1).toTextList());
                        }
                    }
                }
            }
        });


    }

    private List<Expression> getCircleText(TreeFormulaBox treeFormulaBox) {
        RichTextArea rta = treeFormulaBox.getFormulaBox().getRTA();
        rta.getActionFactory().saveNow().execute(new ActionEvent());
        Document doc = rta.getDocument();
        String text = doc.getText();

        int[] indexes = treeFormulaBox.getCircleIndexes();
        int startIndex = Math.min(indexes[0], indexes[1]);
        if (0 < startIndex && startIndex + 1 < text.length() && text.codePointCount(startIndex -1, startIndex +1) == 1) startIndex++;

        int endIndex = Math.max(indexes[0], indexes[1]);
        if (0 < endIndex && endIndex + 1 < text.length() && text.codePointCount(endIndex -1, endIndex +1) == 1) endIndex++;

        return ParseUtilities.parseSubDoc(doc, startIndex, endIndex, checkSetup.getObjLangName());

    }

    private void updateCheckCounter() {
        if (checkMax != -1 && checkTries >= checkMax && !vtExercise.getMainWindow().isInstructorFunctions()) {
            vtView.getCheckButton().setDisable(true);
            vtView.getCheckProgButton().setDisable(true);
        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";

        vtView.getCheckTriesLabel().setText(checkString);
    }

}
