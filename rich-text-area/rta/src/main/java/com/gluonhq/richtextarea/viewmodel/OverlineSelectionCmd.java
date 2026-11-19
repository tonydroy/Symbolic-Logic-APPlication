package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.model.Decoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import com.gluonhq.richtextarea.model.Unit;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class OverlineSelectionCmd extends AbstractEditCmd {

    private final Map<String, String> forwardMap;
    private final Map<String, String> reverseMap;
    private int pieceTableOpsCount = 0;

    private static class FragmentRun {
        String text;
        TextDecoration decoration;

        FragmentRun(String text, TextDecoration decoration) {
            this.text = text;
            this.decoration = decoration;
        }
    }

    public OverlineSelectionCmd(Map<String, String> forwardMap) {
        this.forwardMap = Objects.requireNonNull(forwardMap);
        this.reverseMap = new HashMap<>(forwardMap.size() + 1);
        for (Map.Entry<String, String> entry : forwardMap.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            String existingKey = this.reverseMap.get(val);
            if (existingKey == null || isPrivateUse(existingKey.codePointAt(0))) {
                this.reverseMap.put(val, key);
            }
        }
        this.reverseMap.putIfAbsent("\ue06f", " "); // backwards-compatibility for old files
    }

    private static boolean isPrivateUse(int cp) {
        return cp >= 0xE000 && cp <= 0xF8FF;
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        pieceTableOpsCount = 0;
        Selection sel = viewModel.getSelection();
        if (!sel.isDefined()) {
            return;
        }

        int start = Math.min(sel.getStart(), sel.getEnd());
        int end = Math.max(sel.getStart(), sel.getEnd());
        if (start >= end) {
            return;
        }

        TextDecoration defaultTDec = TextDecoration.builder().presets().build();
        List<FragmentRun> rawRuns = new ArrayList<>();

        // 1. Collect existing fragments and their TextDecorations in the selection
        viewModel.walkFragments((Unit unit, Decoration decoration) -> {
            if (unit == null || unit.isEmpty()) {
                return;
            }
            String unitText = unit.getText();
            if (unitText == null || unitText.isEmpty()) {
                return;
            }
            TextDecoration tDec = (decoration instanceof TextDecoration)
                    ? (TextDecoration) decoration : defaultTDec;

            if (!rawRuns.isEmpty() && Objects.equals(rawRuns.get(rawRuns.size() - 1).decoration, tDec)) {
                rawRuns.get(rawRuns.size() - 1).text += unitText;
            } else {
                rawRuns.add(new FragmentRun(unitText, tDec));
            }
        }, start, end);

        if (rawRuns.isEmpty()) {
            return;
        }

        // 2. Toggle off if all transformable characters in the selection are already overlined
        boolean shouldRemoveOverline = isSelectionAlreadyOverlined(rawRuns);
        Map<String, String> activeMap = shouldRemoveOverline ? reverseMap : forwardMap;

        // 3. Transform each fragment code-point by code-point and strip Bold/Italic
        StringBuilder fullTransformedText = new StringBuilder();
        List<FragmentRun> transformedRuns = new ArrayList<>(rawRuns.size());

        for (FragmentRun run : rawRuns) {
            String converted = transformCodePoints(run.text, activeMap);
            if (!converted.isEmpty()) {
                fullTransformedText.append(converted);
                TextDecoration cleanDec = stripBoldAndItalic(run.decoration);
                transformedRuns.add(new FragmentRun(converted, cleanDec));
            }
        }

        if (fullTransformedText.length() == 0) {
            return;
        }

        // 4. Replace the selection in TextBuffer and apply cleaned fragment decorations
        viewModel.clearSelection();
        viewModel.setCaretPosition(-1);

        viewModel.getTextBuffer().delete(start, end - start);
        pieceTableOpsCount++;

        String newText = fullTransformedText.toString();
        if (start >= viewModel.getTextLength()) {
            viewModel.getTextBuffer().append(newText);
        } else {
            viewModel.getTextBuffer().insert(newText, start);
        }
        pieceTableOpsCount++;

        int cursor = start;
        for (FragmentRun run : transformedRuns) {
            int runLen = run.text.length();
            if (runLen > 0) {
                viewModel.getTextBuffer().decorate(cursor, cursor + runLen, run.decoration);
                pieceTableOpsCount++;
                cursor += runLen;
            }
        }

        // 5. Keep the newly transformed range selected and force UI cell refresh
        int newEnd = start + newText.length();
        viewModel.setCaretPosition(newEnd);
        viewModel.setSelection(new Selection(start, newEnd));
        viewModel.fireUiRefresh();
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        Objects.requireNonNull(viewModel);
        for (int i = 0; i < pieceTableOpsCount; i++) {
            viewModel.undo();
        }
    }

    private TextDecoration stripBoldAndItalic(TextDecoration td) {
        return TextDecoration.builder()
                .fromDecoration(td)
                .fontWeight(FontWeight.NORMAL)
                .fontPosture(FontPosture.REGULAR)
                .build();
    }

    private boolean isSelectionAlreadyOverlined(List<FragmentRun> runs) {
        boolean foundOverlined = false;
        for (FragmentRun run : runs) {
            int i = 0;
            while (i < run.text.length()) {
                int cp = run.text.codePointAt(i);
                i += Character.charCount(cp);
                String ch = new String(Character.toChars(cp));
                if (forwardMap.containsKey(ch)) {
                    return false;
                }
                if (reverseMap.containsKey(ch)) {
                    foundOverlined = true;
                }
            }
        }
        return foundOverlined;
    }

    private String transformCodePoints(String input, Map<String, String> map) {
        StringBuilder sb = new StringBuilder(input.length());
        int i = 0;
        while (i < input.length()) {
            int cp = input.codePointAt(i);
            i += Character.charCount(cp);
            String ch = new String(Character.toChars(cp));
            if ("\n".equals(ch)) {
                sb.append(ch);
            } else {
                sb.append(map.getOrDefault(ch, ch));
            }
        }
        return sb.toString();
    }
}