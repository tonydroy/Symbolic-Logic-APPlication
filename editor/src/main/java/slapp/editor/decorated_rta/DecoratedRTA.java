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
import com.gluonhq.richtextarea.action.Action;
import com.gluonhq.richtextarea.action.DecorateAction;
import com.gluonhq.richtextarea.action.ParagraphDecorateAction;
import com.gluonhq.richtextarea.action.TextDecorateAction;
import com.gluonhq.richtextarea.model.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
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

/**
 * Rich Text area outfitted with toolbar controls (based on the Gluon "Full Featured Demo")
 */
public class DecoratedRTA {


    static {
        try (InputStream resourceAsStream = DecoratedRTA.class.getResourceAsStream("/logging.properties")) {
            if (resourceAsStream != null) {
                LogManager.getLogManager().readConfiguration(resourceAsStream);
            }
        } catch (IOException ex) {
            Logger.getLogger(DecoratedRTA.class.getName()).log(Level.SEVERE, "Error opening logging.properties file", ex);
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

    /**
     * Create the decoratedRTA with RTA editor as component.
     */
    public DecoratedRTA() {
        decoratedRTA = this;
        this.mainStage = EditorMain.mainStage;
        this.editor = new RichTextArea(mainStage);
        setup();
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
        fontSize.getItems().addAll(IntStream.range(1, 100)
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
        keyboardSelector.getItems().setAll(RichTextAreaSkin.KeyMapValue.values());
        keyboardSelector.setValue(RichTextAreaSkin.KeyMapValue.BASE);
        keyboardSelector.setTooltip(new Tooltip("Select Keyboard (F1 - F8, same order as dropdown)"));
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

                wideSeparator(2),

                createJumpSpinner("Spacing", "Space for wrapped lines (point value)", p -> new ParagraphDecorateAction<>(editor, p, v -> (int) v.getSpacing(), (builder, a) -> builder.spacing(a).build())),
                createSpinner("Indent", "Indent Level", p -> new ParagraphDecorateAction<>(editor, p, ParagraphDecoration::getIndentationLevel, (builder, a) -> builder.indentationLevel(a).build())),
                new Label("Size"),
                fontSize,

                wideSeparator(3),

                createToggleButton("\uf0cb", "Numbered List", property -> new ParagraphDecorateAction<>(editor, property, d -> d.getGraphicType() == NUMBERED_LIST, (builder, a) -> builder.graphicType(a ? NUMBERED_LIST : NONE).build())),
                createToggleButton("\uf0ca", "Unordered List/Outline", property -> new ParagraphDecorateAction<>(editor, property, d -> d.getGraphicType() == BULLETED_LIST, (builder, a) -> builder.graphicType(a ? BULLETED_LIST : NONE).build())),
                actionImage("\uf03e", "Insert Image"),
                actionEmoji("Insert Emoji"),
                actionHyperlink("\uf0c1", "Insert Hyperlink on selected text"),
                actionTable("\uf0ce", "Insert Table", td -> editor.getActionFactory().insertTable(td))
        );

        fontsToolbar = new ToolBar();
        fontsToolbar.setStyle("-fx-spacing: 12");
        fontsToolbar.getItems().setAll(
                textForeground,
                textBackground,

                createToggleButton("\uf12b", "Superscript (PgUp)", property -> new TextDecorateAction<>(editor, property, TextDecoration::isSuperscript, (builder, a) -> builder.superscript(a).subscript(false).transSuperscript(false).transSubscript(false).build())),
                createColoredToggleButton("\uf12b", "Shifted superscript (Shift-PgUp)", property -> new TextDecorateAction<>(editor, property, TextDecoration::isTransSuperscript, (builder, a) -> builder.transSuperscript(a).transSubscript(false).subscript(false).superscript(false).build())),
                createToggleButton("\uf12c", "Subscript (PgDn)", property -> new TextDecorateAction<>(editor, property, TextDecoration::isSubscript, (builder, a) -> builder.subscript(a).superscript(false).transSuperscript(false).transSubscript(false).build())),
                createColoredToggleButton("\uf12c", "Shifted Subscript (Shift-PgDn)", property -> new TextDecorateAction<>(editor, property, TextDecoration::isTransSubscript, (builder, a) -> builder.transSubscript(a).transSuperscript(false).superscript(false).subscript(false).build())),
                createToggleButton("\uf032", "Bold - not for symbol fonts (Ctrl/Cmd-B)", property -> new TextDecorateAction<>(editor, property, d -> d.getFontWeight() == BOLD, (builder, a) -> builder.fontWeight(a ? BOLD : NORMAL).build())),
                createToggleButton("\uf033", "Italic - not for symbol fonts (Ctrl/Cmd-I)", property -> new TextDecorateAction<>(editor, property, d -> d.getFontPosture() == ITALIC, (builder, a) -> builder.fontPosture(a ? ITALIC : REGULAR).build())),
                createToggleButton("\uf0cc", "Strikethrough", property -> new TextDecorateAction<>(editor, property, TextDecoration::isStrikethrough, (builder, a) -> builder.strikethrough(a).build())),
                createToggleButton("\uf0cd", "Underline (Ctrl/Cmd-U)", property -> new TextDecorateAction<>(editor, property, TextDecoration::isUnderline, (builder, a) -> builder.underline(a).build()))




        );


        editToolbar = new ToolBar();
        editToolbar.setStyle("-fx-spacing: 12");
        editToolbar.getItems().setAll(
                overlineButton,

                wideSeparator(1),

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
        overlineButton.selectedProperty().bindBidirectional(((RichTextAreaSkin) editor.getSkin()).overlineOnProperty());
//        overlineButton.selectedProperty().addListener((v, ov, nv) -> editor.requestFocus());

        keyboardSelector.valueProperty().bindBidirectional(((RichTextAreaSkin) editor.getSkin()).keyMapStateProperty());
        keyboardSelector.getSelectionModel().selectedItemProperty().addListener((v, ov, nv) -> {
            ((RichTextAreaSkin) editor.getSkin()).setMaps(nv);
            KeyboardDiagram keyboardDiagram = KeyboardDiagram.getInstance();
            if (keyboardDiagram.isShowing()) {
                keyboardDiagram.initialize(this);
                keyboardDiagram.update();
            }
            editor.requestFocus();   //have not been able to find way to stop keyboard window from stealing focus see https://stackoverflow.com/questions/33151460/javafx-stop-new-window-stealing-focus
        });

        ((RichTextAreaSkin) editor.getSkin()).modificationProperty().addListener((v, ov, nv) -> {
            KeyboardDiagram keyboardDiagram = KeyboardDiagram.getInstance();
            if (keyboardDiagram.isShowing()) {
                keyboardDiagram.initialize(this);
                keyboardDiagram.update();
            }
        });
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
        spinner.valueProperty().addListener((obs, ov, nv) -> {
           Node increment = spinner.lookup(".increment-arrow-button");
           if (increment != null) increment.getOnMouseReleased().handle(null);
           Node decrement = spinner.lookup(".decrement-arrow-button");
           if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });
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
        spinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = spinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = spinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });
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

            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image", "*.png", ".jpeg", ".gif"));
            File file = fileChooser.showOpenDialog(button.getScene().getWindow());
            if (file != null) {
                String url = file.toURI().toString();
                editor.getActionFactory().decorate(new ImageDecoration(url)).execute(e);
            }
        });
        return button;
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
            final Dialog<String> hyperlinkDialog = createHyperlinkDialog();
            Optional<String> result = hyperlinkDialog.showAndWait();
            result.ifPresent(textURL -> {
                editor.getActionFactory().decorate(TextDecoration.builder().url(textURL).build()).execute(e);
            });
        });
        return button;
    }

    /*
     * Create the insert hyperlink dialog
     * @return the dialog
     */
    private Dialog<String> createHyperlinkDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Hyperlink");

        // Set the button types
        ButtonType textButtonType = new ButtonType("Create", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(textButtonType);

        // Create the text and url labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField url = new TextField();
        url.setPromptText("URL");

        grid.add(new Label("URL:"), 0, 1);
        grid.add(url, 1, 1);

        Node loginButton = dialog.getDialogPane().lookupButton(textButtonType);
        loginButton.setDisable(true);
        loginButton.disableProperty().bind(url.textProperty().isEmpty());

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == textButtonType) {
                return url.getText();
            }
            return null;
        });

        dialog.setX(mainStage.getX() + mainStage.getWidth()/3);
        dialog.setY(mainStage.getY() + mainStage.getHeight()/3);

        // Request focus on the username field by default.
        dialog.setOnShown(e -> Platform.runLater(url::requestFocus));

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
