package slapp.editor.map_abexplain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.util.Pair;
import slapp.editor.EditorAlerts;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MapABExpCheck {
    private MapABExpExercise mapExercise;
    private MapABExpView mapView;
    private MapABExpModel mapModel;
    private MapCheckSetup checkSetup;

    String objLangName;
    private MapABExpFormulaBox metaBox;
    private MapABExpFormulaBox objectBox;
    private Document metaDocument;
    private Document objectDocument;
    private List<MapCheckLink> checkLinks = new ArrayList<>();


    public MapABExpCheck(MapABExpExercise mapExercise) {
        this.mapExercise = mapExercise;
        this.mapView = mapExercise.getExerciseView();
        this.mapModel = mapExercise.getExerciseModel();

        checkSetup = mapModel.getCheckSetup();
        if (checkSetup == null) {
            checkSetup = new MapCheckSetup();
            mapModel.setCheckSetup(checkSetup);
        }
        objLangName = checkSetup.getObjLangName();


        setRightControlBox();
    }

    private void setRightControlBox() {
        mapView.getCheckButton().setOnAction(e -> {
            checkMap();
        });
    }

    public boolean checkMap() {

        setMetaAndObjBoxes();
        populateCheckLinks();

        boolean checkOK = false;

        if (metaLinkToBracket()) checkOK = false;
        else if (crossedLinks()) checkOK = false;
        else if (emptyLink()) checkOK = false;

        System.out.println("checkOK: " + checkOK);
        return checkOK;
    }

    private boolean emptyLink() {
        boolean emptyLink = false;
        for (MapCheckLink link : checkLinks) {
            if (link.getMetaExpressions().isEmpty() || link.getObjectExpressions().isEmpty()) {
                link.getLink().setMapLinkHighlight();
                EditorAlerts.showSimpleTxtListAlert("Link Issue:", Collections.singletonList(ParseUtilities.newRegularText("Link goes to blank space.")));
                link.getLink().resetMapLinkHighlight();
                emptyLink = true;
                break;
            }
        }
        return emptyLink;
    }

    private boolean crossedLinks() {
        boolean crossed = false;

        for (int i = 0; i < checkLinks.size(); i++) {
            MapCheckLink link = checkLinks.get(i);
            if (i + 1 < checkLinks.size()) {
                if (link.getMetaStartIndex().equals(checkLinks.get(i + 1).getMetaStartIndex())) {
                    checkLinks.get(i + 1).getLink().setMapLinkHighlight();
                    EditorAlerts.showSimpleTxtListAlert("Link Issue:", Collections.singletonList(ParseUtilities.newRegularText("In a good map links do not cross or meet.")));
                    checkLinks.get(i + 1).getLink().resetMapLinkHighlight();
                    crossed = true;
                    return crossed;
                }
            }
        }

        List<Pair<Integer, MapABExpClickableMapLink>> objectIndexes = new ArrayList<>();
        for (MapCheckLink link : checkLinks) {
            objectIndexes.add(new Pair(link.getObjectStartIndex(), link.getLink()));
            if (link.getObjectEndIndex() != null) objectIndexes.add(new Pair(link.getObjectEndIndex(), link.getLink()));
        }

        for (int i = 0; i < objectIndexes.size(); i++) {
            if (i + 1 < objectIndexes.size()) {
                if (objectIndexes.get(i).getKey() >= objectIndexes.get(i + 1).getKey()) {
                    objectIndexes.get(i + 1).getValue().setMapLinkHighlight();
                    EditorAlerts.showSimpleTxtListAlert("Link Issue:", Collections.singletonList(ParseUtilities.newRegularText("In a good map links do not cross or meet.")));
                    objectIndexes.get(i + 1).getValue().resetMapLinkHighlight();
                    crossed = true;
                    break;
                }
            }
        }
        return crossed;
    }

    private boolean metaLinkToBracket() {
        boolean bracket = false;
        for (MapCheckLink link : checkLinks) {
            if (link.getMetaEndIndex() != null) {
                link.getLink().setMapLinkHighlight();
                EditorAlerts.showSimpleTxtListAlert("Link Issue: ", Collections.singletonList(ParseUtilities.newRegularText("A good map does not include \"bracket\" links on the metalinguistic expression.")));
                link.getLink().resetMapLinkHighlight();
                bracket = true;
                break;
            }
        }
        return bracket;
    }

    private void populateCheckLinks() {

        checkLinks = new ArrayList<>();

        List<MapIndexItem> metaIndexItems = metaBox.getMapIndexItems();
        List<MapIndexItem> objectIndexItems = objectBox.getMapIndexItems();
        for (MapIndexItem metaIndexItem : metaIndexItems) {
            for (MapIndexItem objectIndexItem : objectIndexItems) {
                if (metaIndexItem.getLinkID().equals(objectIndexItem.getLinkID())) {
                    String linkID = metaIndexItem.getLinkID();
                    MapCheckLink checkLink = new MapCheckLink();
                    checkLink.setMetaStartIndex(metaIndexItem.getStartIndex());
                    checkLink.setMetaEndIndex(metaIndexItem.getEndIndex());
                    checkLink.setObjectStartIndex(objectIndexItem.getStartIndex());
                    checkLink.setObjectEndIndex(objectIndexItem.getEndIndex());
                    checkLink.setLink(getClickableMapLink(linkID));
                    checkLinks.add(checkLink);
                    break;
                }
            }
        }

        //sort on meta start indexes
        checkLinks.sort(Comparator.comparingInt(MapCheckLink::getMetaStartIndex));

        //populate expression lists
        for (int i = 0; i < checkLinks.size(); i++) {
            MapCheckLink checkLink = checkLinks.get(i);
            int metaStartIndex = checkLink.getMetaStartIndex();
            int metaEndIndex = 0;
            if (checkLink.isMetaGroup()) {
                metaEndIndex = checkLink.getMetaEndIndex();
            }
            else if (i + 1 < checkLinks.size()) {
                metaEndIndex = checkLinks.get(i + 1).getMetaStartIndex();
            }
            else metaEndIndex = metaDocument.getText().length();

            int objectStartIndex = checkLink.getObjectStartIndex();
            int objectEndIndex = 0;
            if (checkLink.isObjectGroup()) {
                objectEndIndex = checkLink.getObjectEndIndex();
            }
            else if (i + 1 < checkLinks.size()) {
                objectEndIndex = checkLinks.get(i + 1).getObjectStartIndex();
            }
            else objectEndIndex = objectDocument.getText().length();

            checkLink.setMetaExpressions(ParseUtilities.parseSubDoc(metaDocument, metaStartIndex, metaEndIndex, "LM Meta"));
            checkLink.setObjectExpressions(ParseUtilities.parseSubDoc(objectDocument, objectStartIndex, objectEndIndex, objLangName));

            System.out.println(checkLink.toString());

        }


    }

    private void setMetaAndObjBoxes() {
        List<Node> nodes = mapView.getRootLayout().getMain_pane().getChildren();
        for (Node node : nodes) {
            if (node instanceof MapABExpFormulaBox) {
                MapABExpFormulaBox box = (MapABExpFormulaBox) node;
                if (box.isMetaBox()) {
                    metaBox = box;
                    RichTextArea metaRTA = metaBox.getFormulaBox().getRTA();
                    if (metaRTA.isModified()) mapExercise.setExerciseModified(true);
                    metaRTA.getActionFactory().saveNow().execute(new ActionEvent());
                    metaDocument = metaRTA.getDocument();
                } else {
                    objectBox = box;
                    RichTextArea objectRTA = objectBox.getFormulaBox().getRTA();
                    if (objectRTA.isModified()) mapExercise.setExerciseModified(true);
                    objectRTA.getActionFactory().saveNow().execute(new ActionEvent());
                    objectDocument = objectRTA.getDocument();
                }
            }
        }
    }

    private MapABExpClickableMapLink getClickableMapLink(String idString) {
        List<Node> nodes = mapView.getRootLayout().getMain_pane().getChildren();
        MapABExpClickableMapLink clickableMapLink = null;
        for (Node node : nodes) {
            if (node instanceof MapABExpClickableMapLink) {
                MapABExpClickableMapLink link = (MapABExpClickableMapLink) node;
                if (link.getIdString().equals(idString)) {
                    clickableMapLink = link;
                }
            }
        }
        return clickableMapLink;
    }




    public boolean isCheckSuccess() {
        return false;
    }

    public boolean isChoiceSuccess() {
        return false;
    }

    public boolean isCheckFinal() {
        return false;
    }

    public int getCheckTries() {
        return 0;
    }

}
