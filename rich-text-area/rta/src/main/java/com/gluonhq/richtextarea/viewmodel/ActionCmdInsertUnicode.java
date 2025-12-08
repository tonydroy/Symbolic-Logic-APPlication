package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.Alerts;
import com.gluonhq.richtextarea.RichTextArea;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;


import java.util.Objects;
public class ActionCmdInsertUnicode implements ActionCmd {
    private final String codeString;

    public ActionCmdInsertUnicode(String codeString) {
        this.codeString = codeString;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        String content = "";
        if (!codeString.isEmpty()) {
            int codeNum = 0;
            if (codeString.charAt(0) == 'x') {
                //convert hex string to decimal set codeNum
                String numString = codeString.substring(1);
                try {
                    codeNum = Integer.valueOf(numString, 16);
                } catch (NumberFormatException e) {
                    RichTextArea.showSimpleAlert("Entry problem", "I do not recognize '" + numString + "' as a representation of a hexidecimal numer.");
                    return;
                }
            } else if (codeString.charAt(0) == '#') {
                //convert decimal string, set codeNum
                String numString = codeString.substring(1);
                try {
                    codeNum = Integer.valueOf(numString);
                } catch (NumberFormatException e) {
                    RichTextArea.showSimpleAlert("Entry problem", "I do not recognize '" + numString + "' as a representation of a decimal numer.");
                    return;
                }
            } else {
                RichTextArea.showSimpleAlert("Entry problem", "Begin decimal with '#' and hexidecimal with 'x'.");
                return;
            }
            if (codeNum > 1114111) {
                RichTextArea.showSimpleAlert("Entry problem", "'" + codeString + "' does not fall within the unicode codeNum points.");
               return;
            }

            char[] chars = Character.toChars(codeNum);
            content = new String(chars);
        }

        if (viewModel.isEditable()) {
            String text;
            if (Objects.requireNonNull(viewModel).getDecorationAtParagraph() != null &&
                    viewModel.getDecorationAtParagraph().hasTableDecoration()) {
                text = content.replace("\n", "");
            } else {
                text = content;
            }
            if (!text.isEmpty()) {
                viewModel.getCommandManager().execute(new InsertCmd(text));
            }
        }
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return viewModel.editableProperty().not();
    }


}
