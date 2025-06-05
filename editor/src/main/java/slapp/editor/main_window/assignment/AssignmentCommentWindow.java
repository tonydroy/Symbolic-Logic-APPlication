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

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import slapp.editor.EditorMain;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.MainWindowView;

/**
 * Window to update of comment in assignment header
 */
public class AssignmentCommentWindow {

    private AssignmentHeader header;
    private MainWindowView mainView;
    private DecoratedRTA commentDRTA;
    private RichTextArea commentRTA;
    private double scale = 1.0;
    private Scene scene;
    private Stage stage;
    private TextArea helpArea;
    private VBox centerBox;
    private SimpleDoubleProperty centerHeightProperty;
    private int pointsPossible;
    private int pointsEarned;

    /**
     * Creates (but does not show) window to update comment in the assignment header.
     * @param header the current header
     * @param mainView the current MainWindowView
     */
    public AssignmentCommentWindow(AssignmentHeader header, MainWindowView mainView, int assignmentPointsPossible, int assignmentPointsEarned) {
        this.header = header;
        this.mainView = mainView;
        this.pointsPossible = assignmentPointsPossible;
        this.pointsEarned = assignmentPointsEarned;
        setUpWindow();
    }

    /**
     * Show assignment comment window, and return header with updated comment field
     *
     * @return header with updated comment
     */
    public AssignmentHeader getAssignmentHeader() {
        stage.showAndWait();
        return header;
    }

    /*
     * Create (but don't show) stage for comment window.
     */
    private void setUpWindow() {
        BorderPane borderPane = new BorderPane();

        Menu helpMenu = new Menu("");
        MenuBar menuBar = new MenuBar(helpMenu);
        menuBar.setStyle("-fx-background-color: aliceblue; -fx-border-color: white;");

        commentDRTA = new DecoratedRTA();
        commentRTA = commentDRTA.getEditor();
        commentRTA.getActionFactory().open(header.getComment()).execute(new ActionEvent());
        commentRTA.setPromptText("Assignment Comment:");
        commentRTA.getStylesheets().add("slappTextArea.css");
        commentRTA.setPrefWidth(PrintUtilities.getPageWidth() + 20);
        commentRTA.setContentAreaWidth(PrintUtilities.getPageWidth());
        commentRTA.setPrefHeight(200);
        commentRTA.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            header.setCommentTextHeight(mainView.getRTATextHeight(commentRTA));
        });

        Node commentNode;
        if (pointsPossible > 0) {
            int percent = (int) Math.round((double) pointsEarned / pointsPossible * 100);
            Label pointsLabel = new Label(Integer.toString(pointsEarned) + "/" + Integer.toString(pointsPossible) + " = " + Integer.toString(percent) + "/100");
            AnchorPane anchorPane = new AnchorPane(commentRTA, pointsLabel);
            anchorPane.setTopAnchor(commentRTA, 0.0);
            anchorPane.setLeftAnchor(commentRTA, 0.0);
            anchorPane.setBottomAnchor(pointsLabel, 3.0);
            anchorPane.setRightAnchor(pointsLabel, 3.0);
            commentNode = anchorPane;
        }
        else commentNode = commentRTA;



        String helpText = "You may comment on the assignment as a whole.  Comment will not show on main screen, but does appear at top of your printed assignment.";

        helpArea = new TextArea(helpText);
        helpArea.setWrapText(true);
        helpArea.setPrefHeight(45);
        helpArea.setEditable(false);
        helpArea.setFocusTraversable(false);
        helpArea.setMouseTransparent(true);
        helpArea.setStyle("-fx-text-fill: mediumslateblue");

        centerBox = new VBox(10, commentNode, helpArea);
        centerBox.setPadding(new Insets(20,0,0,0));
        Group centerGroup = new Group(centerBox);
        borderPane.setCenter(centerGroup);

        Button updateButton = new Button("Update");
        updateButton.setPrefWidth(55);
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(55);
        updateButton.setOnAction(e -> {
            updateHeaderFromWindow();
            closeWindow();
        });
        cancelButton.setOnAction(e ->  closeWindow() );
        HBox buttonBox = new HBox(updateButton, cancelButton);
        buttonBox.setSpacing(80);
        buttonBox.setPadding(new Insets(20,50,20,50));
        buttonBox.setAlignment(Pos.BASELINE_CENTER);
        borderPane.setBottom(buttonBox);
        Label zoomLabel = new Label(" Zoom ");
        Spinner<Integer> zoomSpinner = new Spinner(25, 500, 100, 5);
        zoomSpinner.setPrefSize(65,25);
        zoomSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = zoomSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = zoomSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);

            scale = (double) nv/100;
            updateZoom();
            scene.getWindow().setWidth(Math.max(860, PrintUtilities.getPageWidth() * scale + 55));
            setCenterVgrow();

        });


        ToolBar sizeToolBar = new ToolBar();
        sizeToolBar.setPrefHeight(38);
        sizeToolBar.getItems().addAll(zoomLabel, zoomSpinner, new Label("     "));

        /*

        ToolBar editToolbar = commentDRTA.getKbdSelectorToolbar();
        ToolBar fontsToolbar = commentDRTA.getEditToolbar();
        ToolBar paragraphToolbar = commentDRTA.getParagraphToolbar();
        ToolBar kbdDiaToolBar = commentDRTA.getKbdDiaToolbar();
        kbdDiaToolBar.setPrefHeight(38);



        HBox editAndKbdBox = new HBox(editToolbar, sizeToolBar, kbdDiaToolBar);
        editAndKbdBox.setHgrow(kbdDiaToolBar, Priority.ALWAYS);


         */

        ToolBar paragraphToolbar = commentDRTA.getParagraphToolbar();
        paragraphToolbar.setMinWidth(870);

        ToolBar fontsToolbar = commentDRTA.getFontsToolbar();
        fontsToolbar.setMinWidth(520);

        ToolBar editToolbar = commentDRTA.getEditToolbar();
        editToolbar.setMinWidth(300);

        ToolBar kbdSelectorToolBar = commentDRTA.getKbdSelectorToolbar();
        ToolBar kbdDiaToolBar = commentDRTA.getKbdDiaToolbar();


        editToolbar.setPrefHeight(38);
        fontsToolbar.setPrefHeight(38);
        kbdDiaToolBar.setPrefHeight(38);

        HBox fontsAndEditBox = new HBox(fontsToolbar, editToolbar);
        HBox kbdBox = new HBox(kbdSelectorToolBar, kbdDiaToolBar, sizeToolBar);

        fontsAndEditBox.setHgrow(editToolbar, Priority.ALWAYS);
        kbdBox.setHgrow(sizeToolBar, Priority.ALWAYS);

        VBox topBox = new VBox(menuBar, paragraphToolbar, fontsAndEditBox, kbdBox);

 //       VBox topBox = new VBox(menuBar, paragraphToolbar, fontsToolbar, editAndKbdBox);
        borderPane.topProperty().setValue(topBox);

        scene = new Scene(borderPane);
        scene.getStylesheets().add(DecoratedRTA.class.getClassLoader().getResource("slappEditor.css").toExternalForm());

        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Update Assignment Comment:");
        stage.getIcons().addAll(EditorMain.icons);

        stage.setWidth(860);
        stage.setHeight(520);

        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        stage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 860));
        stage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 520));

//        stage.initOwner(EditorMain.mainStage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setOnCloseRequest(e-> {
            e.consume();
            closeWindow();
        });
    }

    /*
     * Update zoom for the center box (comment and help fields)
     */
    private void updateZoom() {
        centerBox.setScaleX(scale);
        centerBox.setScaleY(scale);
        scene.getWindow().setWidth(Math.max(860, PrintUtilities.getPageWidth() * scale + 55));
        setCenterVgrow();
    }

    /*
     * Update height of comment field by dragging stage size.
     */
    private void setCenterVgrow() {
        double fixedHeight = helpArea.getHeight() * scale + 280;
        DoubleProperty fixedValueProperty = new SimpleDoubleProperty(fixedHeight);
        DoubleProperty maximumHeightProperty = new SimpleDoubleProperty(PrintUtilities.getPageHeight() );
        DoubleProperty scaleProperty = new SimpleDoubleProperty(scale);
        centerHeightProperty = new SimpleDoubleProperty();
        centerHeightProperty.bind(Bindings.min(maximumHeightProperty, (stage.heightProperty().subtract(fixedValueProperty)).divide(scaleProperty)));
        commentRTA.prefHeightProperty().bind(centerHeightProperty);
    }


    /*
     * Update the header with the current value of the comment field.
     */
    private void updateHeaderFromWindow() {
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document statementDocument = commentRTA.getDocument();
        header.setComment(statementDocument);

    }

    private void closeWindow() { stage.close(); }


}
