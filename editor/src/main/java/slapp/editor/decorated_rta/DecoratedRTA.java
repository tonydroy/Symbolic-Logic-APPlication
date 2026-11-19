/*
Copyright (c) 2024 Tony Roy

This file is part of the Symbolic Logic APPlication (SLAPP).

SLAPP is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

SLAPP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with SLAPP.
If not, see <https://www.gnu.org/licenses/>.
 */

package slapp.editor.decorated_rta;


import com.gluonhq.emoji.Emoji;
import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.action.Action;
import com.gluonhq.richtextarea.action.DecorateAction;
import com.gluonhq.richtextarea.action.ParagraphDecorateAction;
import com.gluonhq.richtextarea.action.TextDecorateAction;
import com.gluonhq.richtextarea.model.*;
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import slapp.editor.EditorMain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.gluonhq.richtextarea.model.ParagraphDecoration.GraphicType.*;
import static javafx.scene.text.FontPosture.ITALIC;
import static javafx.scene.text.FontPosture.REGULAR;
import static javafx.scene.text.FontWeight.BOLD;
import static javafx.scene.text.FontWeight.NORMAL;

import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Rich Text area outfitted with toolbar controls (based on the Gluon "Full Featured Demo")
 */

public class DecoratedRTA {

    static {
        // 1. Grab the absolute Root Logger (the boss of all Java console output)
        Logger rootLogger = Logger.getLogger("");

        // 2. Loop through all output handlers (usually just the ConsoleHandler)
        for (Handler handler : rootLogger.getHandlers()) {

            // Keep a reference to any existing filter so we don't accidentally break other rules
            Filter existingFilter = handler.getFilter();

            // 3. Attach our custom interceptor directly to the output handler
            handler.setFilter(record -> {
                // If the message contains our annoying string, SILENCE IT (return false)
                if (record.getMessage() != null && record.getMessage().contains("index exceeds maxCellCount")) {
                    return false;
                }

                // Otherwise, let it pass through normally
                return existingFilter == null || existingFilter.isLoggable(record);
            });
        }
    }





    private static Stage mainStage;
    private final RichTextArea editor;
    private final DecoratedRTA decoratedRTA;
    private double primaryFontSize = 11.0;  //see corresponding value in TextDecoration.java
    private ToolBar kbdSelectorToolbar;
    private ToolBar editToolbar;
    private ToolBar paragraphToolbar;
    private ToolBar kbdDiaToolbar;
    private ToolBar fontsToolbar;
    private ToggleButton overlineButton;
    private TextField unicodeField;


    private Button keyboardDiagramButton;
    ChoiceBox<RichTextAreaSkin.KeyMapValue> keyboardSelector;

    private Button cutButton;
    private Button copyButton;
    private Button pasteButton;
    private Button undoButton;
    private Button redoButton;

    private ComboBox<Double> fontSize;

    private ChangeListener<RichTextAreaSkin.KeyMapValue> kbdSelectorListener;
    private ChangeListener<Object> skinModificationListener;

    private RichTextAreaSkin boundSkin;

    private ChangeListener<Skin<?>> skinNullListener;

    /**
     * Create the decoratedRTA with RTA editor as component.
     */
    public DecoratedRTA() {
        decoratedRTA = this;
        this.mainStage = EditorMain.mainStage;
        this.editor = new RichTextArea(mainStage);
        setup();
    }

    public void setBoxHighlight() {
        RichTextArea rta = this.getEditor();
        rta.getStylesheets().clear();
        rta.getStylesheets().add("slappHighlightTextArea.css");
    }

    public void resetBoxHighlight() {
        RichTextArea rta = this.getEditor();
        rta.getStylesheets().clear();
        rta.getStylesheets().add("slappTextArea.css");
    }

    /*
     * Set up the DRTA controls
     */
    private void setup() {

        //this prevents (strange) scrolling of scrollpanes whenever space is typed.
        editor.addEventFilter( KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                if (event.getEventType() == KeyEvent.KEY_PRESSED){
                    // Consume Event before Bubbling Phase, -> otherwise Scrollpane scrolls
                    if ( event.getCode() == KeyCode.SPACE ){
                        event.consume();
                    }
                }
            }
        });

        //presets combo box (not currently part of SLAPP)
        ComboBox<Presets> presets = new ComboBox<>();
        presets.setTooltip(new Tooltip("Heading Level"));
        presets.getItems().setAll(Presets.values());
        presets.setValue(Presets.DEFAULT);
        presets.setPrefWidth(120);
        presets.setConverter(new StringConverter<>() {
            @Override
            public String toString(Presets presets) {
                return presets.getName();
            }
            @Override
            public Presets fromString(String s) {
                return Presets.valueOf(s.replaceAll(" ", ""));
            }
        });
        presets.getSelectionModel().selectedItemProperty().addListener((observableValue, ov, nv) -> {
            // presets should define all decoration attributes.
            // For now just they set only font size and weight for text, and alignment for paragraphs,
            // so the rest of attributes come for the Builder::presets
            editor.getActionFactory()
                    .decorate(TextDecoration.builder().presets().fontSize(nv.getFontSize()).fontWeight(nv.getWeight()).build(),
                            ParagraphDecoration.builder().presets().alignment(nv.getTextAlignment()).build()).execute(new ActionEvent());
            editor.requestFocus();
        });

        //font size box
        fontSize = new ComboBox<>();
        fontSize.setEditable(true);
        fontSize.setPrefWidth(60);
        fontSize.setTooltip(new Tooltip("Font Size"));
        fontSize.getItems().addAll(IntStream.range(1, 41)
                .filter(i -> i % 2 == 0 || i < 18)
                .asDoubleStream().boxed().collect(Collectors.toList()));
        new TextDecorateAction<>(editor, fontSize.valueProperty(), TextDecoration::getFontSize, (builder, a) -> builder.fontSize(a).build());
        fontSize.setConverter(new StringConverter<>() {
            @Override
            public String toString(Double aDouble) {
                return Integer.toString(aDouble.intValue());
            }

            @Override
            public Double fromString(String s) {
                return Double.parseDouble(s);
            }
        });
        fontSize.setValue(primaryFontSize);
        //this is to make the dropdown open to the selected item's scroll position
        fontSize.setOnShowing(e -> {
            ListView list = (ListView) ((ComboBoxListViewSkin<Double>) fontSize.getSkin()).getPopupContent();
            int listIndex = fontSize.getSelectionModel().getSelectedIndex()- 3;
            list.scrollTo(Math.max(0, listIndex));
        });
        fontSize.setOnAction(e -> primaryFontSize = fontSize.getValue());

        //color pickers
        final ColorPicker textForeground = new ColorPicker();
        textForeground.setTooltip(new Tooltip("Foreground Color"));
        textForeground.getStyleClass().add("foreground");
        new TextDecorateAction<>(editor, textForeground.valueProperty(), (TextDecoration textDecoration1) -> Color.web(textDecoration1.getForeground()), (builder, a) -> builder.foreground(toHexString(a)).build());
        textForeground.setValue(Color.BLACK);

        final ColorPicker textBackground = new ColorPicker();
        textBackground.setTooltip(new Tooltip("Background Color"));
        textBackground.getStyleClass().add("background");
        new TextDecorateAction<>(editor, textBackground.valueProperty(), (TextDecoration textDecoration) -> Color.web(textDecoration.getBackground()), (builder, a) -> builder.background(toHexString(a)).build());
        textBackground.setValue(Color.TRANSPARENT);

        //overline button
        overlineButton = new ToggleButton();
        overlineButton.setTooltip(new Tooltip("Overline (Ctrl/Cmd-0 (zero))"));
        Font overlineButtonFont = Font.font("Noto Serif Combo", FontWeight.LIGHT, FontPosture.REGULAR, 18);
        Text overlineButtonText = new Text("\ue03e");
        overlineButtonText.setFont(overlineButtonFont);
        TextFlow overlineButtonTextFlow = new TextFlow(overlineButtonText);
        overlineButtonTextFlow.setMaxHeight(0.0);
        overlineButton.setGraphic(overlineButtonTextFlow);
        overlineButton.setAlignment(Pos.CENTER);
        overlineButton.setPadding(new Insets(-10,0,-10,11));
        overlineButton.setMaxHeight(28);
        overlineButton.setPrefSize(34,28);
        overlineButton.setFocusTraversable(false);

        //keyboardDiagramButton
        keyboardDiagramButton = new Button("\uf11c");  //line awesome KEYBOARD
        keyboardDiagramButton.getStyleClass().add("lasolid-icon");
        /*
        the LineAwesomeSolid package works fine for me in IntelliJ.  However, I have been unable to include it in
        a jar file without runtime errors.  There is significant activity about this on the web -- as
        https://github.com/kordamp/ikonli/issues/11.  However I have been unable to make their solutions work for me.
        LineAwesomeSolid includes a font, la-solid-900 for its icons.  There is no problem about the jar when
        characters are inserted as such.
         */
        keyboardDiagramButton.setTooltip(new Tooltip("Show Keyboard Diagram"));
        KeyboardDiagram keyboardDiagram = KeyboardDiagram.getInstance();
        keyboardDiagramButton.setOnAction(e -> {
            if (!keyboardDiagram.isShowing()) {
                keyboardDiagram.initialize(this);
                keyboardDiagram.updateAndShow();
            }
            else {
                keyboardDiagram.hide();
            }
        });




        //unicode field
        unicodeField = new TextField();
        unicodeField.setTooltip(new Tooltip("Unicode (x/hex, #/decimal)"));
        unicodeField.setPrefWidth(90);
        unicodeField.setPromptText("unicode value");
        unicodeField.setOnAction(e -> {
            String text = unicodeField.getText();
            editor.getActionFactory().insertUnicode(text).execute(e);
        });



        //keyboard selector
        keyboardSelector = new ChoiceBox<>();
        //      keyboardSelector.getItems().setAll(RichTextAreaSkin.KeyMapValue.values());

        RichTextAreaSkin.KeyMapValue excluded = RichTextAreaSkin.KeyMapValue.BASE_AND_SANS_ITALIC;

        keyboardSelector.getItems().addAll(
                Arrays.stream(RichTextAreaSkin.KeyMapValue.values())
                        .filter(value -> value != excluded)
                        .collect(Collectors.toList())
        );


        keyboardSelector.setValue(RichTextAreaSkin.KeyMapValue.BASE);
        keyboardSelector.setTooltip(new Tooltip("Select Keyboard (F1, F2, F3, F4, shift-F4, F5, F6, F7, F8, same order as dropdown)"));
        keyboardSelector.setPrefWidth(120);
        keyboardSelector.setFocusTraversable(false);

        //action buttons
        cutButton = actionButton("\uf0c4", "Cut (Ctrl/Cmd-X)",   editor.getActionFactory().cut());  //LineAwesomeSolid.CUT
        copyButton = actionButton("\uf0c5", "Copy (Ctrl/Cmd-C)",  editor.getActionFactory().copy()); //LineAwesomeSolid.COPY
        pasteButton = actionButton("\uf0ea", "Paste (Ctrl/Cmd-V)", editor.getActionFactory().paste()); //LineAwesomeSolid.PASTE
        undoButton = actionButton("\uf0e2", "Undo (Ctrl/Cmd-Z)",  editor.getActionFactory().undo());  //LineAwesomeSolid.UNDO
        redoButton = actionButton("\uf01e", "Redo (Shift-Ctrl/Cmd-Z)",  editor.getActionFactory().redo());  //LineAwesomeLolid.REDO

        //toolbars

        paragraphToolbar = new ToolBar();
        paragraphToolbar.setStyle("-fx-spacing: 10");
        paragraphToolbar.getItems().setAll(
                createToggleButton("\uf036", "Align Left", property -> new ParagraphDecorateAction<>(editor, property, d -> d.getAlignment() == TextAlignment.LEFT, (builder, a) -> builder.alignment(TextAlignment.LEFT).build())),
                createToggleButton("\uf037", "Align Center", property -> new ParagraphDecorateAction<>(editor, property, d -> d.getAlignment() == TextAlignment.CENTER, (builder, a) -> builder.alignment(a ? TextAlignment.CENTER : TextAlignment.LEFT).build())),
                createToggleButton("\uf038", "Align Right", property -> new ParagraphDecorateAction<>(editor, property, d -> d.getAlignment() == TextAlignment.RIGHT, (builder, a) -> builder.alignment(a ? TextAlignment.RIGHT : TextAlignment.LEFT).build())),
                createToggleButton("\uf039", "Justify", property -> new ParagraphDecorateAction<>(editor, property, d -> d.getAlignment() == TextAlignment.JUSTIFY, (builder, a) -> builder.alignment(a ? TextAlignment.JUSTIFY : TextAlignment.LEFT).build())),

                wideSeparator(.5),

                createJumpSpinner("Wrp", "Space for wrapped lines (point value)", p -> new ParagraphDecorateAction<>(editor, p, v -> (int) v.getSpacing(), (builder, a) -> builder.spacing(a).build())),

                createJumpSpinner("Par", "Extra space after paragraph (point value)",  p -> new ParagraphDecorateAction<>(editor, p, v -> (int) v.getBottomInset(), (builder, a) -> builder.bottomInset(a).build())),

                createSpinner("Ind", "Indent Level", p -> new ParagraphDecorateAction<>(editor, p, ParagraphDecoration::getIndentationLevel, (builder, a) -> builder.indentationLevel(a).build())),
                new Label("Size"),
                fontSize,

                wideSeparator(.5),

                createToggleButton("\uf0cb", "Simple Numbered List", property -> new ParagraphDecorateAction<>(editor, property, d -> d.getGraphicType() == NUMBERED_LIST, (builder, a) -> builder.graphicType(a ? NUMBERED_LIST : NONE).build())),
                createToggleButton("\uf0ca", "Bulleted List (shapes set by indent level)", property -> new ParagraphDecorateAction<>(editor, property, d -> d.getGraphicType() == BULLETED_LIST, (builder, a) -> builder.graphicType(a ? BULLETED_LIST : NONE).build())),
          //      actionImage("\uf03e", "Insert Image"), //taken out because pictures upset RTA scrolling
                actionEmoji("Insert Emoji"),
                actionHyperlink("\uf0c1", "Insert Hyperlink"),
                actionTable("\uf0ce", "Insert Table", td -> editor.getActionFactory().insertTable(td))
        );

        fontsToolbar = new ToolBar();
        fontsToolbar.setStyle("-fx-spacing: 12");
        fontsToolbar.getItems().setAll(
                textForeground,
                textBackground,

                wideSeparator(0),

                createToggleButton("\uf12b", "Superscript (PgUp)", property -> new TextDecorateAction<>(editor, property, TextDecoration::isSuperscript, (builder, a) -> builder.superscript(a).subscript(false).transSuperscript(false).transSubscript(false).build())),
                createColoredToggleButton("\uf12b", "Shifted superscript (Shift-PgUp)", property -> new TextDecorateAction<>(editor, property, TextDecoration::isTransSuperscript, (builder, a) -> builder.transSuperscript(a).transSubscript(false).subscript(false).superscript(false).build())),
                createToggleButton("\uf12c", "Subscript (PgDn)", property -> new TextDecorateAction<>(editor, property, TextDecoration::isSubscript, (builder, a) -> builder.subscript(a).superscript(false).transSuperscript(false).transSubscript(false).build())),
                createColoredToggleButton("\uf12c", "Shifted Subscript (Shift-PgDn)", property -> new TextDecorateAction<>(editor, property, TextDecoration::isTransSubscript, (builder, a) -> builder.transSubscript(a).transSuperscript(false).superscript(false).subscript(false).build())),

                wideSeparator(0),

                createToggleButton("\uf032", "Bold - not for symbol fonts (Ctrl/Cmd-B)", property -> new TextDecorateAction<>(editor, property, d -> d.getFontWeight() == BOLD, (builder, a) -> builder.fontWeight(a ? BOLD : NORMAL).build())),
                createToggleButton("\uf033", "Italic - not for symbol fonts (Ctrl/Cmd-I)", property -> new TextDecorateAction<>(editor, property, d -> d.getFontPosture() == ITALIC, (builder, a) -> builder.fontPosture(a ? ITALIC : REGULAR).build())),
                createToggleButton("\uf0cc", "Strikethrough", property -> new TextDecorateAction<>(editor, property, TextDecoration::isStrikethrough, (builder, a) -> builder.strikethrough(a).build())),
                createToggleButton("\uf0cd", "Underline (Ctrl/Cmd-U)", property -> new TextDecorateAction<>(editor, property, TextDecoration::isUnderline, (builder, a) -> builder.underline(a).build()))




        );


        editToolbar = new ToolBar();
        editToolbar.setStyle("-fx-spacing: 12");
        editToolbar.getItems().setAll(
                overlineButton,

                wideSeparator(0),

                cutButton,
                copyButton,
                pasteButton,
                undoButton,
                redoButton
        );




        kbdSelectorToolbar = new ToolBar();
        kbdSelectorToolbar.setStyle("-fx-spacing: 10");
        kbdSelectorToolbar.setPrefHeight(38);
        kbdSelectorToolbar.getItems().setAll(
                keyboardSelector,
                unicodeField,

                wideSeparator(1)
        );

        kbdDiaToolbar = new ToolBar();
        kbdDiaToolbar.setStyle("-fx-spacing: 10");
        kbdDiaToolbar.getItems().setAll(keyboardDiagramButton, new Label("  "));



        setRtaListeners();

    }

    /*
     * Color to hex string
     * @param value the color
     * @return the string
     */
    private String toHexString(Color value) {
        return String.format("#%02X%02X%02X%02X", (int) Math.round(value.getRed() * 255),
                (int) Math.round(value.getGreen() * 255),
                (int) Math.round(value.getBlue() * 255),
                (int) Math.round(value.getOpacity() * 255));
    }

    /**
     * Bind overline button and keyboard selector to Skin properties, and listen for changes.
     */
    private void setRtaListeners() {
        BorderPane tempPane = new BorderPane(editor);
        Scene tempScene = new Scene(tempPane);
        editor.applyCss();
        editor.layout();
        boundSkin = (RichTextAreaSkin) editor.getSkin();
        if (boundSkin == null) return;

        overlineButton.selectedProperty().bindBidirectional(boundSkin.overlineOnProperty());

        keyboardSelector.valueProperty().bindBidirectional(boundSkin.keyMapStateProperty());
        kbdSelectorListener = (v, ov, nv) -> {
            if (editor.getSkin() == null) return;
            ((RichTextAreaSkin) editor.getSkin()).setMaps(nv);
            KeyboardDiagram keyboardDiagram = KeyboardDiagram.getInstance();
            if (keyboardDiagram.isShowing()) {
                keyboardDiagram.initialize(this);
                keyboardDiagram.update();
            }
            editor.requestFocus();
        };
        keyboardSelector.getSelectionModel().selectedItemProperty().addListener(kbdSelectorListener);

        skinModificationListener = (v, ov, nv) -> {
            if (editor.getSkin() == null) return;
            KeyboardDiagram keyboardDiagram = KeyboardDiagram.getInstance();
            if (keyboardDiagram.isShowing()) {
                keyboardDiagram.initialize(this);
                keyboardDiagram.update();
            }
        };
        boundSkin.modificationProperty().addListener(skinModificationListener);

        // Auto-clean whenever the Skin Hammer sets editor.setSkin(null) anywhere in SLAPP
        skinNullListener = (obs, oldSkin, newSkin) -> {
            if (newSkin == null) {
                cleanup();
            }
        };
        editor.skinProperty().addListener(skinNullListener);
    }

    /*
     * Separator for use in toolbar with variable width
     * @param inset the width
     * @return the separator
     */
    private Separator wideSeparator(double inset) {
        Separator separator = new Separator(Orientation.VERTICAL);
        separator.setPadding(new Insets(0, inset,0, inset));
        return separator;
    }

    /*
     * Get button to perform an action
     * @param ikon the button ikon
     * @param tooltip the button tooltip
     * @param action the button action
     * @return the button
     */
    private Button actionButton(String ikon, String tooltip, Action action) {
        Button button = new Button(ikon);
        button.getStyleClass().add("lasolid-icon");
        button.setTooltip(new Tooltip(tooltip));
        button.disableProperty().bind(action.disabledProperty());
        button.setOnAction(action::execute);
        button.setFocusTraversable(false);
        return button;
    }

    /*
     * Get toggle button
     * @param ikon the button ikon
     * @param tooltip the button tooltip
     * @param function the button function
     * @return the button
     */
    private ToggleButton createToggleButton(String ikon, String tooltip, Function<ObjectProperty<Boolean>, DecorateAction<Boolean>> function) {
        final ToggleButton toggleButton = new ToggleButton(ikon);
        toggleButton.getStyleClass().add("lasolid-icon");
        toggleButton.setTooltip(new Tooltip(tooltip));
        function.apply(toggleButton.selectedProperty().asObject());
        return toggleButton;
    }

    /*
     * Create the "green outline" toggle button (for shifted super- and subscript)
     * @param ikon the button ikon
     * @param tooltip the button tooltip
     * @param function the button function
     * @return the button
     */
    private ToggleButton createColoredToggleButton(String ikon, String tooltip, Function<ObjectProperty<Boolean>, DecorateAction<Boolean>> function) {
        final ToggleButton toggleButton = new ToggleButton(ikon);
        toggleButton.getStyleClass().add("lasolid-icon");
        toggleButton.setTooltip(new Tooltip(tooltip));
        toggleButton.setStyle("-fx-border-color:LIGHTGREEN;-fx-border-radius:3;-fx-border-width:1;");
        function.apply(toggleButton.selectedProperty().asObject());
        return toggleButton;
    }


    /*
     * Create spinner that steps by 1
     * @param text the spinner label
     * @param tooltip the spinner tooltip
     * @param function the spinner function
     * @return the spinner HBox
     */
    private HBox createSpinner(String text, String tooltip, Function<ObjectProperty<Integer>, DecorateAction<Integer>> function) {
        Spinner<Integer> spinner = new Spinner<>();
        //
        //this is a kludge because the spinners do not stop on mouse release.  This behavior only appeared after I started
        //switching headers on focus.  It doesn't happen to a spinner by itself, but only in combination with some application.
        //this "fix" prevents mouse down spinning, but that will be ok for me. I absolutly do not understand how this happens!
        //see https://stackoverflow.com/questions/55933513/javafx-spinner-keeps-going-after-removed-from-scene
        /*
        spinner.valueProperty().addListener((obs, ov, nv) -> {
           Node increment = spinner.lookup(".increment-arrow-button");
           if (increment != null) increment.getOnMouseReleased().handle(null);
           Node decrement = spinner.lookup(".decrement-arrow-button");
           if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });
         */
        //
        spinner.setTooltip(new Tooltip(tooltip));
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20);
        spinner.setValueFactory(valueFactory);
        spinner.setPrefWidth(55);
        spinner.setEditable(false);
        function.apply(valueFactory.valueProperty());
        HBox spinnerBox = new HBox(5, new Label(text), spinner);
        spinnerBox.setAlignment(Pos.CENTER);
        return spinnerBox;
    }

    /*
     * Create spinner that steps by 2
     * @param text the spinner label
     * @param tooltip the spinner tooltip
     * @param function the spinner function
     * @return the spinner HBox
     */
    //TODO This code duplicates createSpinner except for the amount to step by
    private HBox createJumpSpinner(String text, String tooltip, Function<ObjectProperty<Integer>, DecorateAction<Integer>> function) {
        Spinner<Integer> spinner = new Spinner<>();
        //
        //this is a kludge because the spinners do not stop on mouse release.  This behavior only appeared after I started
        //switching headers on focus.  It doesn't happen to a spinner by itself, but only in combination with RTA logic.
        //this "fix" prevents mouse down spinning, but that will be ok for me. I absolutly do not understand how this happens!

        /*
        spinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = spinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = spinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });
         */
        //
        spinner.setTooltip(new Tooltip(tooltip));
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20,0,2);
        spinner.setValueFactory(valueFactory);
        spinner.setPrefWidth(55);
        spinner.setEditable(false);
        function.apply(valueFactory.valueProperty());
        HBox spinnerBox = new HBox(5, new Label(text), spinner);
        spinnerBox.setAlignment(Pos.CENTER);
        return spinnerBox;
    }

    //start at 2
    private HBox createJumpSpinner2(String text, String tooltip, Function<ObjectProperty<Integer>, DecorateAction<Integer>> function) {
        Spinner<Integer> spinner = new Spinner<>();
        //
        //this is a kludge because the spinners do not stop on mouse release.  This behavior only appeared after I started
        //switching headers on focus.  It doesn't happen to a spinner by itself, but only in combination with RTA logic.
        //this "fix" prevents mouse down spinning, but that will be ok for me. I absolutly do not understand how this happens!
        /*
        spinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = spinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = spinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

         */
        //
        spinner.setTooltip(new Tooltip(tooltip));
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20,2,2);
        spinner.setValueFactory(valueFactory);
        spinner.setPrefWidth(55);
        spinner.setEditable(false);
        function.apply(valueFactory.valueProperty());
        HBox spinnerBox = new HBox(5, new Label(text), spinner);
        spinnerBox.setAlignment(Pos.CENTER);
        return spinnerBox;
    }


    /*
     * Button to insert image
     * @param ikon
     * @param tooltip
     * @return the button
     */

    private Button actionImage(String ikon, String tooltip) {
        Button button = new Button(ikon);
        button.getStyleClass().add("lasolid-icon");
        button.setTooltip(new Tooltip(tooltip));
        button.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();

            File homeDir = new File(System.getProperty("user.home"));
            fileChooser.setInitialDirectory(homeDir);

            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File file = fileChooser.showOpenDialog(button.getScene().getWindow());
            if (file != null) {
                String url = file.toURI().toString();
                Image image = new Image(url);
                if (!image.isError() && image.getWidth() > 0) {
                    Dialog<ImageDecoration> imageDialog = createImageDialog(url, image);
                    Optional<ImageDecoration> result = imageDialog.showAndWait();
                    result.ifPresent(imageDecoration -> {
                        RichTextAreaViewModel viewModel = ((RichTextAreaSkin) editor.getSkin()).getViewModel();
                        if (viewModel.getCaretPosition() < 0) {
                            viewModel.setCaretPosition(viewModel.getTextLength());
                        }
                        editor.getActionFactory().decorate(imageDecoration).execute(e);
                    });
                }
            }
        });
        return button;
    }


    private Dialog<ImageDecoration> createImageDialog(String url, Image image) {
        Dialog<ImageDecoration> dialog = new Dialog<>();
        dialog.setTitle("Insert Image");

        ButtonType insertButtonType = new ButtonType("Insert", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(insertButtonType, ButtonType.CANCEL);

        double origWidth = Math.max(1.0, image.getWidth());
        double origHeight = Math.max(1.0, image.getHeight());

        // If the image is larger than 350px, start at a sensible initial scale; otherwise 100%
        double maxDim = Math.max(origWidth, origHeight);
        double initialPercent = maxDim > 350.0
                ? Math.max(5.0, Math.round((350.0 / maxDim) * 100.0))
                : 100.0;

        ImageView previewView = new ImageView(image);
        previewView.setPreserveRatio(true);
        previewView.setSmooth(true);

        StackPane imageHolder = new StackPane(previewView);
        imageHolder.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(imageHolder);
        scrollPane.setPrefViewportWidth(450);
        scrollPane.setPrefViewportHeight(300);
        scrollPane.setFitToWidth( true );
        scrollPane.setFitToHeight( true );

        Slider scaleSlider = new Slider(5, 150, initialPercent);
        scaleSlider.setShowTickMarks(true);
        scaleSlider.setShowTickLabels(true);
        scaleSlider.setMajorTickUnit(25);
        scaleSlider.setPrefWidth(320);

        Label sizeLabel = new Label();

        Runnable updatePreview = () -> {
            double scale = scaleSlider.getValue() / 100.0;
            int w = Math.max(1, (int) Math.round(origWidth * scale));
            int h = Math.max(1, (int) Math.round(origHeight * scale));
            previewView.setFitWidth(w);
            previewView.setFitHeight(h);
            sizeLabel.setText(String.format("Scale: %d%%  (%d × %d px)",
                    (int) Math.round(scaleSlider.getValue()), w, h));
        };

        scaleSlider.valueProperty().addListener((obs, ov, nv) -> updatePreview.run());
        updatePreview.run();

        VBox content = new VBox(10, scrollPane, sizeLabel, scaleSlider);
        content.setPadding(new Insets(15));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == insertButtonType) {
                double scale = scaleSlider.getValue() / 100.0;
                int w = Math.max(1, (int) Math.round(origWidth * scale));
                int h = Math.max(1, (int) Math.round(origHeight * scale));
                return new ImageDecoration(url, w, h);
            }
            return null;
        });

        dialog.setX(mainStage.getX() + mainStage.getWidth() / 4);
        dialog.setY(mainStage.getY() + mainStage.getHeight() / 4);

        return dialog;
    }

    /*
     * Button to insert emoji
     * @param tooltip the button tooltip
     * @return the emoji button
     */
    private Button actionEmoji(String tooltip) {
        Region region = new Region();
        region.getStyleClass().addAll("icon", "emoji-outline");
        Button emojiButton = new Button(null, region);
        emojiButton.setTooltip(new Tooltip("Insert Emoji"));
        emojiButton.getStyleClass().add("emoji-button");
        emojiButton.setOnAction(e -> {
            EmojiPopup emojiPopup = new EmojiPopup();
            emojiPopup.setSkinTone(editor.getSkinTone());
            editor.skinToneProperty().bindBidirectional(emojiPopup.skinToneProperty());
            emojiPopup.setOnAction(ev -> {
                Emoji emoji = (Emoji) ev.getSource();
                editor.getActionFactory().insertEmoji(emoji).execute(new ActionEvent());
            });
            emojiPopup.show(emojiButton);
        });
        return emojiButton;
    }

    /*
     * Button to insert hyperlink
     * @param ikon the button ikon
     * @param tooltip the button tooltip
     * @return the button
     */
    private Button actionHyperlink(String ikon, String tooltip) {
        Button button = new Button(ikon);
        button.getStyleClass().add("lasolid-icon");
        button.setTooltip(new Tooltip(tooltip));
        button.setOnAction(e -> {
            RichTextAreaViewModel viewModel = ((RichTextAreaSkin) editor.getSkin()).getViewModel();
            Selection selection = viewModel.getSelection();

            final String selectedText;
            if (selection.isDefined()) {
                int start = Math.min(selection.getStart(), selection.getEnd());
                int end = Math.max(selection.getStart(), selection.getEnd());
                selectedText = viewModel.getTextBuffer().getText(start, end).replace("\ufeff", "");
            } else {
                selectedText = "";
            }

            Decoration dec = viewModel.getDecorationAtCaret();
            final String existingUrl = (dec instanceof TextDecoration && ((TextDecoration) dec).getURL() != null)
                    ? ((TextDecoration) dec).getURL()
                    : "";

            final Dialog< javafx.util.Pair<String, String> > hyperlinkDialog = createHyperlinkDialog(selectedText, existingUrl);
            Optional< javafx.util.Pair<String, String> > result = hyperlinkDialog.showAndWait();

            result.ifPresent(pair -> {
                String linkText = pair.getKey();
                String textURL = pair.getValue();
                TextDecoration linkDecoration = TextDecoration.builder().url(textURL).build();

                if (!selection.isDefined() || !linkText.equals(selectedText)) {
                    if (!selection.isDefined() && viewModel.getCaretPosition() < 0) {
                        viewModel.setCaretPosition(viewModel.getTextLength());
                    }
                    int start = selection.isDefined()
                            ? Math.min(selection.getStart(), selection.getEnd())
                            : viewModel.getCaretPosition();

                    editor.getActionFactory().insertText(linkText).execute(e);

                    int end = start + linkText.length();
                    if (end > start) {
                        editor.getActionFactory().selectAndDecorate(new Selection(start, end), linkDecoration).execute(e);
                    }
                } else {
                    // Selection existed and display text was unchanged: decorate existing selection
                    editor.getActionFactory().decorate(linkDecoration).execute(e);
                }
            });
        });
        return button;
    }

    /*
     * Create the insert hyperlink dialog
     * @return the dialog
     */

    private Dialog< javafx.util.Pair<String, String> > createHyperlinkDialog(String initialText, String initialUrl) {
        Dialog< javafx.util.Pair<String, String> > dialog = new Dialog<>();
        dialog.setTitle("Hyperlink");

        // Set the button types (Create + Cancel)
        ButtonType createButtonType = new ButtonType("Create", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the text and url labels and fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField displayText = new TextField(initialText != null ? initialText : "");
        displayText.setPrefWidth(300);
        displayText.setPromptText("Text to display");

        TextField url = new TextField(initialUrl != null ? initialUrl : "");
        url.setPrefWidth(300);
        url.setPromptText("URL");

        grid.add(new Label("Text:"), 0, 0);
        grid.add(displayText, 1, 0);
        grid.add(new Label("URL:"), 0, 1);
        grid.add(url, 1, 1);

        Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);
        createButton.disableProperty().bind(url.textProperty().isEmpty());

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String urlStr = url.getText().trim();
                String textStr = displayText.getText();
                if (textStr == null || textStr.isEmpty()) {
                    textStr = urlStr; // Fallback to URL if display text was left blank
                }
                return new javafx.util.Pair<>(textStr, urlStr);
            }
            return null;
        });

        dialog.setX(mainStage.getX() + mainStage.getWidth() / 3);
        dialog.setY(mainStage.getY() + mainStage.getHeight() / 3);

        // Focus URL if text was already selected; otherwise focus Text field first
        dialog.setOnShown(e -> Platform.runLater(() -> {
            if (initialText != null && !initialText.isEmpty()) {
                url.requestFocus();
            } else {
                displayText.requestFocus();
            }
        }));

        return dialog;
    }



    /*
     * Button to insert table
     * @param ikon the button ikon
     * @param tooltip the button tooltip
     * @param actionFunction the button function
     * @return the button
     */
    private Button actionTable(String ikon, String tooltip, Function<TableDecoration, Action> actionFunction) {
        Button button = new Button(ikon);
        button.getStyleClass().add("lasolid-icon");
        button.setTooltip(new Tooltip(tooltip));
        button.disableProperty().bind(actionFunction.apply(null).disabledProperty());
        button.setOnAction(e -> {
            final Dialog<TableDecoration> tableDialog = insertTableDialog();
            Optional<TableDecoration> result = tableDialog.showAndWait();
            result.ifPresent(td -> actionFunction.apply(td).execute(e));
        });
        return button;
    }

    /**
     * Dialog to insert table
     * @return the dialog
     */
    private Dialog<TableDecoration> insertTableDialog() {
        Dialog<TableDecoration> dialog = new Dialog<>();
        dialog.setTitle("Insert table");

        // Set the button types
        ButtonType textButtonType = new ButtonType("Create", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(textButtonType);

        // Create the text and rows labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField rows = new TextField();
        rows.setPromptText("Rows ");

        grid.add(new Label("Rows:"), 0, 1);
        grid.add(rows, 1, 1);

        TextField cols = new TextField();
        cols.setPromptText("Columns ");

        grid.add(new Label("Columns:"), 0, 2);
        grid.add(cols, 1, 2);

        Node tableButton = dialog.getDialogPane().lookupButton(textButtonType);
        tableButton.setDisable(true);
        tableButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> {
                    if (rows.getText().isEmpty() || cols.getText().isEmpty()) {
                        return true;
                    }
                    try {
                        Integer.parseInt(rows.getText());
                        Integer.parseInt(cols.getText());
                    } catch (NumberFormatException nfe) {
                        return true;
                    }
                    return false;
                },
                rows.textProperty(), cols.textProperty()));

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == textButtonType) {
                int r = Integer.parseInt(rows.getText());
                int c = Integer.parseInt(cols.getText());
                return new TableDecoration(r, c); // TODO: add cell alignment
            }
            return null;
        });

        dialog.setX(mainStage.getX() + mainStage.getWidth()/3);
        dialog.setY(mainStage.getY() + mainStage.getHeight()/3);

        // Request focus on the username field by default.
        dialog.setOnShown(e -> Platform.runLater(rows::requestFocus));

        return dialog;
    }

    /*
     * Presets enum (used for presets combo box not currently in SLAPP
     */
    private enum Presets {

        DEFAULT("Default",11, NORMAL, TextAlignment.LEFT),
        HEADER1("Header 1", 32, BOLD, TextAlignment.CENTER),
        HEADER2("Header 2", 24, BOLD, TextAlignment.LEFT),
        HEADER3("Header 3", 19, BOLD, TextAlignment.LEFT);

        private final String name;
        private final int fontSize;
        private final FontWeight weight;
        private final TextAlignment textAlignment;

        Presets(String name, int fontSize, FontWeight weight, TextAlignment textAlignment) {
            this.name = name;
            this.fontSize = fontSize;
            this.weight = weight;
            this.textAlignment = textAlignment;
        }

        public String getName() {
            return name;
        }

        public int getFontSize() {
            return fontSize;
        }

        public FontWeight getWeight() {
            return weight;
        }

        public TextAlignment getTextAlignment() {
            return textAlignment;
        }
    }

    public void cleanup() {
        try {
            if (skinNullListener != null) {
                editor.skinProperty().removeListener(skinNullListener);
                skinNullListener = null;
            }
        } catch (Exception e) {}

        try {
            if (boundSkin != null) {
                if (overlineButton != null) {
                    overlineButton.selectedProperty().unbindBidirectional(boundSkin.overlineOnProperty());
                }
                if (keyboardSelector != null) {
                    keyboardSelector.valueProperty().unbindBidirectional(boundSkin.keyMapStateProperty());
                }
                if (skinModificationListener != null) {
                    boundSkin.modificationProperty().removeListener(skinModificationListener);
                }
                boundSkin = null;
            }
        } catch (Exception e) {}

        try {
            if (keyboardSelector != null && kbdSelectorListener != null) {
                keyboardSelector.getSelectionModel().selectedItemProperty().removeListener(kbdSelectorListener);
            }
        } catch (Exception e) {}

        try {
            if (cutButton != null) { cutButton.disableProperty().unbind(); cutButton.setOnAction(null); }
            if (copyButton != null) { copyButton.disableProperty().unbind(); copyButton.setOnAction(null); }
            if (pasteButton != null) { pasteButton.disableProperty().unbind(); pasteButton.setOnAction(null); }
            if (undoButton != null) { undoButton.disableProperty().unbind(); undoButton.setOnAction(null); }
            if (redoButton != null) { redoButton.disableProperty().unbind(); redoButton.setOnAction(null); }
            if (keyboardDiagramButton != null) keyboardDiagramButton.setOnAction(null);
            if (unicodeField != null) unicodeField.setOnAction(null);
            if (fontSize != null) { fontSize.setOnShowing(null); fontSize.setOnAction(null); }
        } catch (Exception e) {}

        kbdSelectorListener = null;
        skinModificationListener = null;
    }


    /**
     * The RichTextArea editor
     * @return the RTA
     */
    public RichTextArea getEditor() {
        return editor;
    }

    /**
     * The RTA font size
     * @return the font size
     */
    public double getPrimaryFontSize() {
        return primaryFontSize;
    }

    /**
     * The kbdSelectorToolbar has just the keyboard selector dropdown
     * @return the toolbar
     */
    public ToolBar getKbdSelectorToolbar() {
        return kbdSelectorToolbar;
    }

    /**
     * The Edit toolbar has multiple edit commands (for the SLAPP middle toolbar row)
     * @return the toolbar
     */
    public ToolBar getEditToolbar() {
        return editToolbar;
    }

    /**
     * The paragraphtoolbar contains multiple commands relevant to paragraphs (for the SLAPP top toolbar row)
     * @return the toolbar
     */
    public ToolBar getParagraphToolbar() {
        return paragraphToolbar;
    }

    /**
     * The keyboard selector dropdown gives keyboard combination options
     * @return the choice box
     */
    public ChoiceBox<RichTextAreaSkin.KeyMapValue> getKeyboardSelector() {
        return keyboardSelector;
    }

    /**
     * The keyboard diagram toolbar has just the keyboard diagram button
     * @return the toolbar
     */
    public ToolBar getKbdDiaToolbar() {
        return kbdDiaToolbar;
    }

    public ToolBar getFontsToolbar() { return fontsToolbar; }

    public TextField getUnicodeField() {return unicodeField; }

    public Button getKeyboardDiagramButton() {return keyboardDiagramButton; }

    public void setPrimaryFontSize(double size) {
        primaryFontSize = size;
        fontSize.setValue(primaryFontSize);

    }


}
