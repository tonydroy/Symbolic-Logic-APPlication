package slapp.editor.trans_uline;

import javafx.beans.property.DoubleProperty;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ControlType;

public interface ULBoxContainer {

    void editorInFocus(DecoratedRTA decoratedRTA, ControlType control);

    void setExerciseModified(boolean modified);

    void pushUndoRedo();

    DoubleProperty getScalePageWidth();
}
