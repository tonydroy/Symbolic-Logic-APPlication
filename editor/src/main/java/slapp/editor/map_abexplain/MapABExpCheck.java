package slapp.editor.map_abexplain;

public class MapABExpCheck {
    MapABExpExercise mapExercise;
    MapABExpView mapView;
    MapABExpModel mapModel;


    public MapABExpCheck(MapABExpExercise mapExercise) {
        this.mapExercise = mapExercise;
        this.mapView = mapExercise.getExerciseView();
        this.mapModel = mapExercise.getExerciseModel();

        setRightControlBox();
    }

    private void setRightControlBox() {
        mapView.getCheckButton().setOnAction(e -> {
            checkMap();
        });
    }

    public boolean checkMap() {



        return false;
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
