package slapp.editor;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import slapp.editor.main_window.MainWindowView;

import java.util.List;

public class ExerciseHelpPopup {
    Stage helpStage;

    public ExerciseHelpPopup(List<Text> messageTxts) {
        TextFlow textFlow = new TextFlow();
        if (messageTxts != null) {
            Text[] resultArray = new Text[messageTxts.size()];
            messageTxts.toArray(resultArray);
            textFlow = new TextFlow(resultArray);
            textFlow.setPrefWidth(400);
            textFlow.setPrefHeight(150);
            textFlow.setMinHeight(175);
            textFlow.setPadding(new Insets(10, 10, 10, 10));

            Scene scene = new Scene(textFlow);
            helpStage = new Stage();
            helpStage.setScene(scene);
            helpStage.setTitle("Contextual Help");
            helpStage.initModality(Modality.NONE);
            helpStage.getIcons().addAll(EditorMain.icons);
            Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();


            helpStage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 420));
            helpStage.setY(EditorMain.mainStage.getY() + EditorMain.mainStage.getHeight()/4);
     //       helpStage.initOwner(EditorMain.mainStage);
        }


    }

    public Stage getHelpStage() {
        return helpStage;
    }
}
