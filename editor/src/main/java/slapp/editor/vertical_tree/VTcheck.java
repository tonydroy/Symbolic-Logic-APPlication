package slapp.editor.vertical_tree;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import slapp.editor.EditorAlerts;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ParseUtilities;
import slapp.editor.vertical_tree.drag_drop.ClickableNodeLink;
import slapp.editor.vertical_tree.drag_drop.MapFormulaBox;
import slapp.editor.vertical_tree.drag_drop.TreeFormulaBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VTcheck {
    private VerticalTreeExercise vtExercise;
    private VerticalTreeView vtView;
    private VerticalTreeModel vtModel;
    private VTcheckSetup checkSetup;

    private int checkMax;
    private int checkTries;
    private boolean checkFinal;
    private boolean checkSuccess;

    List<Node> checkNodeList;
    private List<List<TreeNode>> formulaTree;
    private List<List<String>> lineJustifications;
    private double equalsEpsilon = 10.0;
    private boolean isCheckJustification = true;
    private List<TreeNode> connectedNodes;

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
            vtView.setCheckMessage("Full Tree");
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
            checkFormulaTree();
        });
    }

    private void setChecksCounter() {
        if (checkMax != -1 && checkTries >= checkMax && !vtExercise.getMainWindow().isInstructorFunctions()) {
            vtView.getCheckButton().setDisable(true);
        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";

        vtView.getCheckTriesLabel().setText(checkString);

    }

    private void checkFormulaTree() {
        checkSuccess = false;
        vtView.deactivateBigCheck();
        if (!vtExercise.getMainWindow().isInstructorFunctions()) {
            checkTries++;
            setChecksCounter();
        }


        formulaTree = new ArrayList<>();
        lineJustifications = new ArrayList<>();

        checkNodeList = vtView.getRootLayout().getMainPane().getChildren();
        if (checkNodeList.isEmpty()) {
            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Nothing to evaluate.")));
            return;
        }

        List<Node> sortedFormulaBoxes = getSortedFormulaBoxes();
        if (sortedFormulaBoxes.isEmpty()) {
            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("No tree to evaluate.")));
            return;
        }

        if (!populateTreeLists(sortedFormulaBoxes)) return;
        if (!setParentsAndChildren()) return;
        if (!checkTrueTree()) return;


        checkSuccess = true;
        vtView.activateBigCheck();
    }

    private boolean checkTrueTree() {

        //check one root
        List<TreeNode> bottomRow = formulaTree.get(formulaTree.size() - 1);
        if (bottomRow.size() > 1 ) {
            for (TreeNode treeNode : bottomRow) {
                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
            }
            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("A tree can have only one bottom (root) node.")));
            for (TreeNode treeNode : bottomRow) {
                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
            }
            return false;
        }

        //check nodes have at most one child
        for (List<TreeNode> row : formulaTree) {
            for (TreeNode treeNode : row) {
                List<TreeNode> children = treeNode.getChildren();
                if (children.size() > 1) {
                    treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                    EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("A node can be linked to at most one child (lower) node.")));
                    treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                    return false;
                }
            }
        }

        //check all nodes connected to root
        connectedNodes = new ArrayList<>();
        populateConnectedNodes(formulaTree.get(formulaTree.size() - 1).get(0));
        for (List<TreeNode> row : formulaTree) {
            for (TreeNode treeNode : row) {
                if (!connectedNodes.contains(treeNode)) {
                    treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                    EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node is not an an ancestor of the root node.")));
                    treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                    return false;
                }
            }
        }

        return true;
    }

    private void populateConnectedNodes(TreeNode node) {
        connectedNodes.add(node);
        for (TreeNode parent : node.getParents()) {
            populateConnectedNodes(parent);
        }
    }

    private boolean setParentsAndChildren() {
        for (List<TreeNode> rowList : formulaTree) {
            for (TreeNode node : rowList) {
                node.setParents(new ArrayList<>());
                node.setChildren(new ArrayList<>());
                TreeFormulaBox mainTBox = node.getMainTreeBox();
                String formulaID = mainTBox.getId();
                String relativeID = "";
                for (Node link : checkNodeList ) {
                    relativeID = "";
                    if (link instanceof ClickableNodeLink) {
                        ClickableNodeLink nodeLink = (ClickableNodeLink) link;
                        if (nodeLink.getSourceId().equals(formulaID)) {
                            relativeID = nodeLink.getTargetId();
                        }
                        if (nodeLink.getTargetId().equals(formulaID)) {
                            relativeID = nodeLink.getSourceId();
                        }
                    }
                    if (!relativeID.equals("")) {
                        for (List<TreeNode> rList : formulaTree) {
                            for (TreeNode tNode : rList) {
                                TreeFormulaBox tBox = tNode.getMainTreeBox();
                                if (tBox.getId().equals(relativeID)) {
                                    if (tBox.getLayoutY() - mainTBox.getLayoutY() > equalsEpsilon)
                                        node.getChildren().add(tNode);
                                    else if (mainTBox.getLayoutY() - tBox.getLayoutY() > equalsEpsilon)
                                        node.getParents().add(tNode);
                                    else {
                                        mainTBox.getFormulaBox().setVTtreeBoxHighlight();
                                        tBox.getFormulaBox().setVTtreeBoxHighlight();
                                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Cannot have connected nodes in same row.")));
                                        mainTBox.getFormulaBox().resetVTtreeBoxHighlight();
                                        tBox.getFormulaBox().resetVTtreeBoxHighlight();
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (List<TreeNode> rowList : formulaTree) {
            for (TreeNode node : rowList) {
                node.getParents().sort(Comparator.comparingDouble(parent -> parent.getMainTreeBox().getLayoutX()));
            }
        }

        return true;
    }

    private boolean populateTreeLists(List<Node> sortedFormulaBoxes) {
        int j = 0;
        List<TreeNode> treeList = new ArrayList<>();
        List<MapFormulaBox> justificationList = new ArrayList<>();
        lineJustifications = new ArrayList<>();
        for (int i = 0; i < sortedFormulaBoxes.size(); i++) {
            if (sortedFormulaBoxes.get(i) instanceof TreeFormulaBox) treeList.add(new TreeNode((TreeFormulaBox) sortedFormulaBoxes.get(i)));
            if (sortedFormulaBoxes.get(i) instanceof MapFormulaBox) {justificationList.add((MapFormulaBox) sortedFormulaBoxes.get(i));}

            if ((i + 1 < sortedFormulaBoxes.size() && Math.abs(sortedFormulaBoxes.get(i + 1).getLayoutY() - sortedFormulaBoxes.get(i).getLayoutY()) > equalsEpsilon) || i + 1 >= sortedFormulaBoxes.size()) {

                treeList.sort(Comparator.comparingDouble((treeBox -> treeBox.getMainTreeBox().getLayoutX())));
                formulaTree.add(treeList);

                if (isCheckJustification)  {
                    if (justificationList.size() == 0) {
                        for (TreeNode treeNode : treeList) {
                            treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                        }
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Missing justification on row.")));
                        for (TreeNode treeNode : treeList) {
                            treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                        }
                        return false;
                    }
                    else if (justificationList.size() > 1) {
                        for (MapFormulaBox justificationBox : justificationList) {
                            justificationBox.getFormulaBox().setVTmapBoxHighlight();
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Only one justification field for each row.")));
                        for (MapFormulaBox justificationBox : justificationList) {
                            justificationBox.getFormulaBox().resetVTmapBoxHighlight();
                        }
                        return false;
                    }
                    else {
                        BoxedDRTA bdrta = justificationList.get(0).getFormulaBox();
                        RichTextArea rta = bdrta.getRTA();
                        rta.getActionFactory().saveNow().execute(new ActionEvent());
                        String boxText = rta.getDocument().getText();
                        List<String> missingPrimes = getMissingPrime(boxText);
                        if (missingPrimes.size() > 0) {
                            bdrta.setVTmapBoxHighlight();
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Do not forget prime on justification for abbreviating rules, as FR\u2032(\u2227).")));
                            bdrta.resetVTmapBoxHighlight();
                            return false;
                        }

                        List<String> justifications = getJustificationStrings(boxText);
                        if (justifications.size() != treeList.size()) {
                            bdrta.setVTmapBoxHighlight();
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Should be one justification, as FR(\u223c), for each member of row.")));
                            bdrta.resetVTmapBoxHighlight();
                            return false;
                        }
                        lineJustifications.add(justifications);
                    }
                }
                j++;
                treeList = new ArrayList<>();
                justificationList = new ArrayList<>();
            }
        }
        return true;
    }

    List<String> getMissingPrime(String inputString) {
        String regexTarget = "FR\\(\\u2227\\)|FR\\(\\u2228\\)|FR\\(\\u2194\\)|FR\\(\\u2203\\)";
        Pattern pattern = Pattern.compile(regexTarget);
        Matcher matcher = pattern.matcher(inputString);

        List<String> justifications = new ArrayList<>();
        while (matcher.find()) {
            justifications.add(matcher.group());
        }
        return justifications;
    }

    List<String> getJustificationStrings(String inputString) {

        String regexTarget = "FR\\(s\\)|FR\\(r\\)|FR\\(\\u223c\\)|FR\\(\\u2192\\)|FR\\u2032\\(\\u2227\\)|FR\\u2032\\(\\u2228\\)|FR\\u2032\\(\\u2194\\)|FR\\(\\u2200\\)|FR\\u2032\\(\\u2203\\)";
        Pattern pattern = Pattern.compile(regexTarget);
        Matcher matcher = pattern.matcher(inputString);

        List<String> justifications = new ArrayList<>();
        while (matcher.find()) {
            justifications.add(matcher.group());
        }
        return justifications;
    }


    private List<Node> getSortedFormulaBoxes() {
        List<Node> sortedFormulaBoxes = new ArrayList<>();
        for (Node node : checkNodeList) {
            if (node instanceof TreeFormulaBox || node instanceof MapFormulaBox) sortedFormulaBoxes.add(node);
        }
        sortedFormulaBoxes.sort(Comparator.comparingDouble(node -> node.getLayoutY()));
        return sortedFormulaBoxes;
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
