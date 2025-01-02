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



import com.gluonhq.richtextarea.RichTextAreaSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import slapp.editor.EditorMain;
import slapp.editor.main_window.MainWindowView;

import java.util.HashMap;
import java.util.Map;

/**
 * Keyboard diagram for currently selected keyboards and font size
 */
public class KeyboardDiagram {
    private DecoratedRTA decoratedRTA;
    private Map<Character, Text> keyTypedTextMap = new HashMap<>();
    private Map<KeyCombination, Text> keyPressedTextMap = new HashMap<>();
    private double mapFontSize = 11;
    private Font textFont;
    private Font symbolFont;
    private Font iconFont;
    private Text title1;
    private GridPane normalBoard;
    private Text title2;
    private GridPane shiftBoard;
    private Text title3;
    private GridPane altBoard;
    private Text title4;
    private GridPane shiftAltBoard;
    private Text title5;
    private GridPane ctrlShiftAltBoard;
    private Text ctrlChars;
    private boolean shown = false;
    private static KeyboardDiagram uniqueKeyboardDiagram;
    private static Font titleFont;
    private static int baseKeyWidth;  //the width of a standard key is 4 of these units
    private static VBox boardsBox;
    private static ScrollPane scrollPane;
    private static Scene scene;
    private static Stage stage;

    /**
     * Set up window
     */
    static {
        titleFont = Font.font("Noto Sans", FontWeight.BOLD, FontPosture.REGULAR, 20);
        baseKeyWidth = 10;
        boardsBox = new VBox();
        boardsBox.setAlignment(Pos.CENTER_LEFT);
        boardsBox.setSpacing(20.0);
        scrollPane = new ScrollPane(boardsBox);
        scrollPane.setPadding(new Insets(10,10,10,10));
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scene = new Scene(scrollPane);
        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Keyboard Diagrams");

        stage.initOwner(EditorMain.mainStage);
        stage.initModality(Modality.NONE);
        stage.getIcons().addAll(EditorMain.icons);

    }

    /*
     * Singleton class has private constructor
     */
    private KeyboardDiagram(){}

    /**
     * Get instance of singleton class
     *
     * @return the unique keyboard diagram
     */
    public static KeyboardDiagram getInstance() {
        if (uniqueKeyboardDiagram == null) {
            uniqueKeyboardDiagram = new KeyboardDiagram();
        }
        return uniqueKeyboardDiagram;
    }


    /**
     * Update the font size for characters in the map depending on selected font size and scale value
     * @param scale the scale
     */
    public void updateFontSize(Double scale) {
        this.mapFontSize = decoratedRTA.getPrimaryFontSize() * scale;
    }

    /**
     * Initialize the diagram based on the DecoratedRTA with its keyboard selections
     *
     * @param decoratedRTA the decorated RTA
     */
    public void initialize(DecoratedRTA decoratedRTA) {
        this.decoratedRTA = decoratedRTA;
        textFont = Font.font("Noto Serif Combo", FontWeight.NORMAL, FontPosture.REGULAR, mapFontSize);
        symbolFont = Font.font("Noto Serif Combo", FontWeight.NORMAL, FontPosture.REGULAR, mapFontSize);
        iconFont = Font.font("la-solid-900", FontWeight.NORMAL, FontPosture.REGULAR, 14);

        stage.setOnCloseRequest(e -> {
            e.consume();
            hide();
        });

        //initialize blank text maps
        Map<Character, String> keyTypedCharMap = ((RichTextAreaSkin) decoratedRTA.getEditor().getSkin()).getKeyTypedCharMap();
        Map<KeyCodeCombination, String> keyPressedCharMap = ((RichTextAreaSkin) decoratedRTA.getEditor().getSkin()).getKeyPressedCharMap();
        for (char key : keyTypedCharMap.keySet()) {
        keyTypedTextMap.put(key, new Text());
        }
        for (KeyCombination key : keyPressedCharMap.keySet()) {
        keyPressedTextMap.put(key, new Text());
        }

        //initialize keyboards
        title1 = new Text("Normal");
        title1.setFont(titleFont);

        normalBoard = new GridPane();
        normalBoard.add(getTypedKey('`',4),0,0, 4, 1);
        normalBoard.add(getTypedKey('1',4),4,0,4,1);
        normalBoard.add(getTypedKey('2',4),8,0,4,1);
        normalBoard.add(getTypedKey('3',4),12,0,4,1);
        normalBoard.add(getTypedKey('4',4),16,0,4,1);
        normalBoard.add(getTypedKey('5',4),20,0,4,1);
        normalBoard.add(getTypedKey('6',4),24,0,4,1);
        normalBoard.add(getTypedKey('7',4),28,0,4,1);
        normalBoard.add(getTypedKey('8',4),32,0,4,1);
        normalBoard.add(getTypedKey('9',4),36,0,4,1);
        normalBoard.add(getTypedKey('0',4),40,0,4,1);
        normalBoard.add(getTypedKey('-',4),44,0,4,1);
        normalBoard.add(getTypedKey('=',4),48,0,4,1);
        normalBoard.add(getControlKey("BackDel", 8),52,0,8,1);

        normalBoard.add(getControlKey("Tab",6),0,1,6,1);
        normalBoard.add(getTypedKey('q',4),6,1,4,1);
        normalBoard.add(getTypedKey('w',4),10,1,4,1);
        normalBoard.add(getTypedKey('e',4),14,1,4,1);
        normalBoard.add(getTypedKey('r',4),18,1,4,1);
        normalBoard.add(getTypedKey('t',4),22,1,4,1);
        normalBoard.add(getTypedKey('y',4),26,1,4,1);
        normalBoard.add(getTypedKey('u',4),30,1,4,1);
        normalBoard.add(getTypedKey('i',4),34,1,4,1);
        normalBoard.add(getTypedKey('o',4),38,1,4,1);
        normalBoard.add(getTypedKey('p',4),42,1,4,1);
        normalBoard.add(getTypedKey('[',4),46,1,4,1);
        normalBoard.add(getTypedKey(']',4),50,1,4,1);
        normalBoard.add(getTypedKey('\\',6),54,1,6,1);

        normalBoard.add(getControlKey("Cap Lock",7),0,2,7,1);
        normalBoard.add(getTypedKey('a',4),7,2,4,1);
        normalBoard.add(getTypedKey('s',4),11,2,4,1);
        normalBoard.add(getTypedKey('d',4),15,2,4,1);
        normalBoard.add(getTypedKey('f',4),19,2,4,1);
        normalBoard.add(getTypedKey('g',4),23,2,4,1);
        normalBoard.add(getTypedKey('h',4),27,2,4,1);
        normalBoard.add(getTypedKey('j',4),31,2,4,1);
        normalBoard.add(getTypedKey('k',4),35,2,4,1);
        normalBoard.add(getTypedKey('l',4),39,2,4,1);
        normalBoard.add(getTypedKey(';',4),43,2,4,1);
        normalBoard.add(getTypedKey('\'',4),47,2,4,1);
        normalBoard.add(getControlKey("Enter",9),51,2,9,1);

        normalBoard.add(getControlKey("Shift",9),0,3,9,1);
        normalBoard.add(getTypedKey('z',4),9,3,4,1);
        normalBoard.add(getTypedKey('x',4),13,3,4,1);
        normalBoard.add(getTypedKey('c',4),17,3,4,1);
        normalBoard.add(getTypedKey('v',4),21,3,4,1);
        normalBoard.add(getTypedKey('b',4),25,3,4,1);
        normalBoard.add(getTypedKey('n',4),29,3,4,1);
        normalBoard.add(getTypedKey('m',4),33,3,4,1);
        normalBoard.add(getTypedKey(',',4),37,3,4,1);
        normalBoard.add(getTypedKey('.',4),41,3,4,1);
        normalBoard.add(getTypedKey('/',4),45,3,4,1);
        normalBoard.add(getControlKey("Shift",11),49,3,11,1);

        normalBoard.add(getControlKey("control/\n command",10),0,4,10,1);
        normalBoard.add(getControlKey("alternate/\n option",9),10,4,9,1);
        normalBoard.add(getControlKey("",24),19,4,24,1);
        normalBoard.add(getControlKey("alternate/\n option",8),43,4,8,1);
        normalBoard.add(getControlKey("control/\n command",9),51,4,9,1);

        title2 = new Text("Shift");
        title2.setFont(titleFont);

        shiftBoard = new GridPane();
        shiftBoard.add(getTypedKey('~',4),0,0, 4, 1);
        shiftBoard.add(getTypedKey('!',4),4,0,4,1);
        shiftBoard.add(getTypedKey('@',4),8,0,4,1);
        shiftBoard.add(getTypedKey('#',4),12,0,4,1);
        shiftBoard.add(getTypedKey('$',4),16,0,4,1);
        shiftBoard.add(getTypedKey('%',4),20,0,4,1);
        shiftBoard.add(getTypedKey('^',4),24,0,4,1);
        shiftBoard.add(getTypedKey('&',4),28,0,4,1);
        shiftBoard.add(getTypedKey('*',4),32,0,4,1);
        shiftBoard.add(getTypedKey('(',4),36,0,4,1);
        shiftBoard.add(getTypedKey(')',4),40,0,4,1);
        shiftBoard.add(getTypedKey('_',4),44,0,4,1);
        shiftBoard.add(getTypedKey('+',4),48,0,4,1);
        shiftBoard.add(getControlKey("BackDel", 8),52,0,8,1);

        shiftBoard.add(getControlKey("Tab",6),0,1,6,1);
        shiftBoard.add(getTypedKey('Q',4),6,1,4,1);
        shiftBoard.add(getTypedKey('W',4),10,1,4,1);
        shiftBoard.add(getTypedKey('E',4),14,1,4,1);
        shiftBoard.add(getTypedKey('R',4),18,1,4,1);
        shiftBoard.add(getTypedKey('T',4),22,1,4,1);
        shiftBoard.add(getTypedKey('Y',4),26,1,4,1);
        shiftBoard.add(getTypedKey('U',4),30,1,4,1);
        shiftBoard.add(getTypedKey('I',4),34,1,4,1);
        shiftBoard.add(getTypedKey('O',4),38,1,4,1);
        shiftBoard.add(getTypedKey('P',4),42,1,4,1);
        shiftBoard.add(getTypedKey('{',4),46,1,4,1);
        shiftBoard.add(getTypedKey('}',4),50,1,4,1);
        shiftBoard.add(getTypedKey('|',6),54,1,6,1);

        shiftBoard.add(getControlKey("Cap Lock",7),0,2,7,1);
        shiftBoard.add(getTypedKey('A',4),7,2,4,1);
        shiftBoard.add(getTypedKey('S',4),11,2,4,1);
        shiftBoard.add(getTypedKey('D',4),15,2,4,1);
        shiftBoard.add(getTypedKey('F',4),19,2,4,1);
        shiftBoard.add(getTypedKey('G',4),23,2,4,1);
        shiftBoard.add(getTypedKey('H',4),27,2,4,1);
        shiftBoard.add(getTypedKey('J',4),31,2,4,1);
        shiftBoard.add(getTypedKey('K',4),35,2,4,1);
        shiftBoard.add(getTypedKey('L',4),39,2,4,1);
        shiftBoard.add(getTypedKey(':',4),43,2,4,1);
        shiftBoard.add(getTypedKey('\"',4),47,2,4,1);
        shiftBoard.add(getControlKey("Enter",9),51,2,9,1);

        shiftBoard.add(getControlKey("Shift",9),0,3,9,1);
        shiftBoard.add(getTypedKey('Z',4),9,3,4,1);
        shiftBoard.add(getTypedKey('X',4),13,3,4,1);
        shiftBoard.add(getTypedKey('C',4),17,3,4,1);
        shiftBoard.add(getTypedKey('V',4),21,3,4,1);
        shiftBoard.add(getTypedKey('B',4),25,3,4,1);
        shiftBoard.add(getTypedKey('N',4),29,3,4,1);
        shiftBoard.add(getTypedKey('M',4),33,3,4,1);
        shiftBoard.add(getTypedKey('<',4),37,3,4,1);
        shiftBoard.add(getTypedKey('>',4),41,3,4,1);
        shiftBoard.add(getTypedKey('?',4),45,3,4,1);
        shiftBoard.add(getControlKey("Shift",11),49,3,11,1);

        shiftBoard.add(getControlKey("control/\n command",10),0,4,10,1);
        shiftBoard.add(getControlKey("alternate/\n option",9),10,4,9,1);
        shiftBoard.add(getControlKey("",24),19,4,24,1);
        shiftBoard.add(getControlKey("alternate/\n option",8),43,4,8,1);
        shiftBoard.add(getControlKey("control/\n command",9),51,4,9,1);

        title3 = new Text("Alt (Option)");
        title3.setFont(titleFont);

        altBoard = new GridPane();
        altBoard.add(getAltKey(KeyCode.BACK_QUOTE,4),0,0, 4, 1);
        altBoard.add(getAltKey(KeyCode.DIGIT1,4),4,0,4,1);
        altBoard.add(getAltKey(KeyCode.DIGIT2,4),8,0,4,1);
        altBoard.add(getAltKey(KeyCode.DIGIT3,4),12,0,4,1);
        altBoard.add(getAltKey(KeyCode.DIGIT4,4),16,0,4,1);
        altBoard.add(getAltKey(KeyCode.DIGIT5,4),20,0,4,1);
        altBoard.add(getAltKey(KeyCode.DIGIT6,4),24,0,4,1);
        altBoard.add(getAltKey(KeyCode.DIGIT7,4),28,0,4,1);
        altBoard.add(getAltKey(KeyCode.DIGIT8,4),32,0,4,1);
        altBoard.add(getAltKey(KeyCode.DIGIT9,4),36,0,4,1);
        altBoard.add(getAltKey(KeyCode.DIGIT0,4),40,0,4,1);
        altBoard.add(getAltKey(KeyCode.MINUS,4),44,0,4,1);
        altBoard.add(getAltKey(KeyCode.EQUALS,4),48,0,4,1);
        altBoard.add(getControlKey("BackDel", 8),52,0,8,1);

        altBoard.add(getControlKey("Tab",6),0,1,6,1);
        altBoard.add(getAltKey(KeyCode.Q,4),6,1,4,1);
        altBoard.add(getAltKey(KeyCode.W,4),10,1,4,1);
        altBoard.add(getAltKey(KeyCode.E,4),14,1,4,1);
        altBoard.add(getAltKey(KeyCode.R,4),18,1,4,1);
        altBoard.add(getAltKey(KeyCode.T,4),22,1,4,1);
        altBoard.add(getAltKey(KeyCode.Y,4),26,1,4,1);
        altBoard.add(getAltKey(KeyCode.U,4),30,1,4,1);
        altBoard.add(getAltKey(KeyCode.I,4),34,1,4,1);
        altBoard.add(getAltKey(KeyCode.O,4),38,1,4,1);
        altBoard.add(getAltKey(KeyCode.P,4),42,1,4,1);
        altBoard.add(getAltKey(KeyCode.OPEN_BRACKET,4),46,1,4,1);
        altBoard.add(getAltKey(KeyCode.CLOSE_BRACKET,4),50,1,4,1);
        altBoard.add(getAltKey(KeyCode.BACK_SLASH,6),54,1,6,1);

        altBoard.add(getControlKey("Cap Lock",7),0,2,7,1);
        altBoard.add(getAltKey(KeyCode.A,4),7,2,4,1);
        altBoard.add(getAltKey(KeyCode.S,4),11,2,4,1);
        altBoard.add(getAltKey(KeyCode.D,4),15,2,4,1);
        altBoard.add(getAltKey(KeyCode.F,4),19,2,4,1);
        altBoard.add(getAltKey(KeyCode.G,4),23,2,4,1);
        altBoard.add(getAltKey(KeyCode.H,4),27,2,4,1);
        altBoard.add(getAltKey(KeyCode.J,4),31,2,4,1);
        altBoard.add(getAltKey(KeyCode.K,4),35,2,4,1);
        altBoard.add(getAltKey(KeyCode.L,4),39,2,4,1);
        altBoard.add(getAltKey(KeyCode.SEMICOLON,4),43,2,4,1);
        altBoard.add(getAltKey(KeyCode.QUOTE,4),47,2,4,1);
        altBoard.add(getControlKey("Enter",9),51,2,9,1);

        altBoard.add(getControlKey("Shift",9),0,3,9,1);
        altBoard.add(getAltKey(KeyCode.Z,4),9,3,4,1);
        altBoard.add(getAltKey(KeyCode.X,4),13,3,4,1);
        altBoard.add(getAltKey(KeyCode.C,4),17,3,4,1);
        altBoard.add(getAltKey(KeyCode.V,4),21,3,4,1);
        altBoard.add(getAltKey(KeyCode.B,4),25,3,4,1);
        altBoard.add(getAltKey(KeyCode.N,4),29,3,4,1);
        altBoard.add(getAltKey(KeyCode.M,4),33,3,4,1);
        altBoard.add(getAltKey(KeyCode.COMMA,4),37,3,4,1);
        altBoard.add(getAltKey(KeyCode.PERIOD,4),41,3,4,1);
        altBoard.add(getAltKey(KeyCode.SLASH,4),45,3,4,1);
        altBoard.add(getControlKey("Shift",11),49,3,11,1);

        altBoard.add(getControlKey("control/\n command",10),0,4,10,1);
        altBoard.add(getControlKey("alternate/\n option",9),10,4,9,1);
        altBoard.add(getControlKey("",24),19,4,24,1);
        altBoard.add(getControlKey("alternate/\n option",8),43,4,8,1);
        altBoard.add(getControlKey("control/\n command",9),51,4,9,1);

        title4 = new Text("Shift-Alt (Shift-Option)");
        title4.setFont(titleFont);

        shiftAltBoard = new GridPane();
        shiftAltBoard.add(getShiftAltKey(KeyCode.BACK_QUOTE,4),0,0, 4, 1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.DIGIT1,4),4,0,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.DIGIT2,4),8,0,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.DIGIT3,4),12,0,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.DIGIT4,4),16,0,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.DIGIT5,4),20,0,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.DIGIT6,4),24,0,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.DIGIT7,4),28,0,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.DIGIT8,4),32,0,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.DIGIT9,4),36,0,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.DIGIT0,4),40,0,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.MINUS,4),44,0,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.EQUALS,4),48,0,4,1);
        shiftAltBoard.add(getControlKey("BackDel", 8),52,0,8,1);

        shiftAltBoard.add(getControlKey("Tab",6),0,1,6,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.Q,4),6,1,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.W,4),10,1,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.E,4),14,1,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.R,4),18,1,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.T,4),22,1,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.Y,4),26,1,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.U,4),30,1,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.I,4),34,1,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.O,4),38,1,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.P,4),42,1,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.OPEN_BRACKET,4),46,1,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.CLOSE_BRACKET,4),50,1,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.BACK_SLASH,6),54,1,6,1);

        shiftAltBoard.add(getControlKey("Cap Lock",7),0,2,7,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.A,4),7,2,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.S,4),11,2,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.D,4),15,2,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.F,4),19,2,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.G,4),23,2,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.H,4),27,2,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.J,4),31,2,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.K,4),35,2,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.L,4),39,2,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.SEMICOLON,4),43,2,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.QUOTE,4),47,2,4,1);
        shiftAltBoard.add(getControlKey("Enter",9),51,2,9,1);

        shiftAltBoard.add(getControlKey("Shift",9),0,3,9,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.Z,4),9,3,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.X,4),13,3,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.C,4),17,3,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.V,4),21,3,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.B,4),25,3,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.N,4),29,3,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.M,4),33,3,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.COMMA,4),37,3,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.PERIOD,4),41,3,4,1);
        shiftAltBoard.add(getShiftAltKey(KeyCode.SLASH,4),45,3,4,1);
        shiftAltBoard.add(getControlKey("Shift",11),49,3,11,1);

        shiftAltBoard.add(getControlKey("control/\n command",10),0,4,10,1);
        shiftAltBoard.add(getControlKey("alternate/\n option",9),10,4,9,1);
        shiftAltBoard.add(getControlKey("",24),19,4,24,1);
        shiftAltBoard.add(getControlKey("alternate/\n option",8),43,4,8,1);
        shiftAltBoard.add(getControlKey("control/\n command",9),51,4,9,1);

        title5 = new Text("Ctrl-Shift-Alt (Command-Shift-Option)");
        title5.setFont(titleFont);

        ctrlShiftAltBoard = new GridPane();
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.BACK_QUOTE,4),0,0, 4, 1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.DIGIT1,4),4,0,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.DIGIT2,4),8,0,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.DIGIT3,4),12,0,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.DIGIT4,4),16,0,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.DIGIT5,4),20,0,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.DIGIT6,4),24,0,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.DIGIT7,4),28,0,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.DIGIT8,4),32,0,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.DIGIT9,4),36,0,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.DIGIT0,4),40,0,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.MINUS,4),44,0,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.EQUALS,4),48,0,4,1);

        ctrlShiftAltBoard.add(getControlKey("BackDel", 8),52,0,8,1);
        ctrlShiftAltBoard.add(getControlKey("Tab",6),0,1,6,1);

        ctrlShiftAltBoard.add(getIconKey("\uf05e",4),6,1,4,1);
   //     ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.Q,4),6,1,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.W,4),10,1,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.E,4),14,1,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.R,4),18,1,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.T,4),22,1,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.Y,4),26,1,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.U,4),30,1,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.I,4),34,1,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.O,4),38,1,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.P,4),42,1,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.OPEN_BRACKET,4),46,1,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.CLOSE_BRACKET,4),50,1,4,1);
        ctrlShiftAltBoard.add(getControlKey("NoBreak\nSpace", 6),54,1,6,1);

        ctrlShiftAltBoard.add(getControlKey("Cap Lock",7),0,2,7,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.A,4),7,2,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.S,4),11,2,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.D,4),15,2,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.F,4),19,2,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.G,4),23,2,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.H,4),27,2,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.J,4),31,2,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.K,4),35,2,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.L,4),39,2,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.SEMICOLON,4),43,2,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.QUOTE,4),47,2,4,1);
        ctrlShiftAltBoard.add(getControlKey("Enter",9),51,2,9,1);

        ctrlShiftAltBoard.add(getControlKey("Shift",9),0,3,9,1);
        //these are "hard coded" - need to fix for modified assignments.
        ctrlShiftAltBoard.add(getFixedCharKey("\ue06e",4),9,3,4,1);
        ctrlShiftAltBoard.add(getFixedCharKey("\ue27f",4),13,3,4,1);
        ctrlShiftAltBoard.add(getFixedCharKey("\ue4ea",4),17,3,4,1);
        ctrlShiftAltBoard.add(getFixedCharKey("\ue69a",4),21,3,4,1);
        ctrlShiftAltBoard.add(getFixedCharKey("\ue871",4),25,3,4,1);
        //



        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.N,4),29,3,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.M,4),33,3,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.COMMA,4),37,3,4,1);
        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.PERIOD,4),41,3,4,1);

        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.SLASH,4),45,3,4,1);

//        ctrlShiftAltBoard.add(getCtrlShiftAltKey(KeyCode.SLASH,4),45,3,4,1);
        ctrlShiftAltBoard.add(getControlKey("Shift",11),49,3,11,1);

        ctrlShiftAltBoard.add(getControlKey("control/\n command",10),0,4,10,1);
        ctrlShiftAltBoard.add(getControlKey("alternate/\n option",9),10,4,9,1);
        ctrlShiftAltBoard.add(getControlKey("",24),19,4,24,1);
        ctrlShiftAltBoard.add(getControlKey("alternate/\n option",8),43,4,8,1);
        ctrlShiftAltBoard.add(getControlKey("control/\n command",9),51,4,9,1);

        ctrlChars = new Text("\ud83e\udc46 F1 - F8: select keyboards (same order as dropdown); F9 toggles to previous\n" +
        "    page-up: toggle superscript (+shift, shift back)\n" +
        "    page-down: toggle subscript (+shift, shift back)\n" +
        "    Ctrl:B bold, :I italic, :U underline, :0 (zero) overline\n" +
        "    Ctrl:A select all, :C copy, :X cut, :V paste, :Z undo, :Z(+shift) redo\n\n");

        ctrlChars.setFont(textFont);
    }

    /*
     * Get key with text name (as 'shift' or 'enter')
     *
     * @param name key name
     * @param key width units
     * @return StackPane key
     */
    private StackPane getControlKey(String name, int width) {
        Text text = new Text(name);
        text.setFont(new Font("NotoSans", mapFontSize * 2/3));
        TextFlow flow = new TextFlow(text);
        flow.setTextAlignment(TextAlignment.CENTER);
        StackPane pane = new StackPane(flow);
        pane.setBorder(new Border(new BorderStroke(Color.GREY, BorderStrokeStyle.SOLID, new CornerRadii(3),new BorderWidths(2.0))));
        pane.setAlignment(Pos.CENTER);
        pane.setMinWidth(baseKeyWidth * width);
        return pane;
    }


    /*
     * Get char with fixed symbol (used for combining characters)
     *
      * @param name the character string
     * @param key width units
     * @return StackPane key
     */
    private StackPane getFixedCharKey(String name, int width) {
        Text text = new Text(name);
        text.setFont(symbolFont);
        TextFlow flow = new TextFlow(text);
        flow.setTextAlignment(TextAlignment.CENTER);
        StackPane pane = new StackPane(flow);
        pane.setBorder(new Border(new BorderStroke(Color.GREY, BorderStrokeStyle.SOLID, new CornerRadii(3),new BorderWidths(2.0))));
        pane.setAlignment(Pos.CENTER);
        pane.setMinWidth(baseKeyWidth * width);
        return pane;
    }

    /*
     * Get key from icon font (used just for 'do not go' character)
     * @param name the character strint
     * @param key with units
     * @return StackPane key
     */
    private StackPane getIconKey(String name, int width) {
        Text text = new Text(name);
        text.setFont(iconFont);
        text.setFill(Color.RED);
        TextFlow flow = new TextFlow(text);
        flow.setPadding(new Insets(4,0,0,0));
        flow.setTextAlignment(TextAlignment.CENTER);
        StackPane pane = new StackPane(flow);
        pane.setBorder(new Border(new BorderStroke(Color.GREY, BorderStrokeStyle.SOLID, new CornerRadii(3),new BorderWidths(2.0))));
        pane.setAlignment(Pos.CENTER);
        pane.setMinWidth(baseKeyWidth * width);
        Tooltip t = new Tooltip("Do not type: Key initiates logout on Mac!");
        Tooltip.install(pane, t);

        return pane;
    }

    /*
     * Get key from the keyTypedTextMap
     * @param key the key Character
     * @param key width units
     * @return StackPane key
     */
    private StackPane getTypedKey(Character key, int width) {
        Text text = keyTypedTextMap.get(key);
        text.setFont(textFont);
        TextFlow flow = new TextFlow(text);
        flow.setTextAlignment(TextAlignment.CENTER);
        StackPane pane = new StackPane(flow);
        pane.setBorder(new Border(new BorderStroke(Color.GREY, BorderStrokeStyle.SOLID, new CornerRadii(3),new BorderWidths(2.0))));
        pane.setAlignment(Pos.CENTER);
        pane.setMinWidth(baseKeyWidth * width);
        return pane;
    }

    /*
     *Get key from keyPressedTextMap given KeyCode and alt down
     * @param code the KeyCode
     * @param width key width units
     * @return StackPane key
     */
    private StackPane getAltKey(KeyCode code, int width) {
        Text text = keyPressedTextMap.get(new KeyCodeCombination(code, KeyCombination.ALT_DOWN));
        text.setFont(symbolFont);
        TextFlow flow = new TextFlow(text);
        flow.setTextAlignment(TextAlignment.CENTER);
        StackPane pane = new StackPane(flow);
        pane.setBorder(new Border(new BorderStroke(Color.GREY, BorderStrokeStyle.SOLID, new CornerRadii(3),new BorderWidths(2.0))));
        pane.setAlignment(Pos.CENTER);
        pane.setMinWidth(baseKeyWidth * width);
        return pane;
    }

    /*
     *Get key from keyPressedTextMap given KeyCode with shift and alt down
     * @param code the KeyCode
     * @param width key width units
     * @return StackPane key
     */
    private StackPane getShiftAltKey(KeyCode code, int width) {
        Text text = keyPressedTextMap.get(new KeyCodeCombination(code, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN));
        text.setFont(symbolFont);
        TextFlow flow = new TextFlow(text);
        flow.setTextAlignment(TextAlignment.CENTER);
        StackPane pane = new StackPane(flow);
        pane.setBorder(new Border(new BorderStroke(Color.GREY, BorderStrokeStyle.SOLID, new CornerRadii(3),new BorderWidths(2.0))));
        pane.setAlignment(Pos.CENTER);
        pane.setMinWidth(baseKeyWidth * width);
        return pane;
    }

    /*
     *Get key from keyPressedTextMap given KeyCode with control/command, shift, and alt down
     * @param code the KeyCode
     * @param width key width units
     * @return StackPane key
     */
    private StackPane getCtrlShiftAltKey(KeyCode code, int width) {
        Text text = keyPressedTextMap.get(new KeyCodeCombination(code, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN,KeyCombination.SHORTCUT_DOWN));
        text.setFont(symbolFont);
        TextFlow flow = new TextFlow(text);
        flow.setTextAlignment(TextAlignment.CENTER);
        StackPane pane = new StackPane(flow);
        pane.setBorder(new Border(new BorderStroke(Color.GREY, BorderStrokeStyle.SOLID, new CornerRadii(3),new BorderWidths(2.0))));
        pane.setAlignment(Pos.CENTER);
        pane.setMinWidth(baseKeyWidth * width);
        return pane;
    }

    /**
     * Update the diagram based on maps from the selected DecoratedRTA
     */

    /*

     */
    public void update() {
        RichTextAreaSkin skin = (RichTextAreaSkin) decoratedRTA.getEditor().getSkin();

        Map<Character, String> keyTypedCharMap = skin.getKeyTypedCharMap();
        Map<KeyCodeCombination, String> keyPressedCharMap = skin.getKeyPressedCharMap();

        for (Map.Entry<Character, Text> entry : keyTypedTextMap.entrySet()) {
            entry.getValue().setText(skin.getModifiedChar(keyTypedCharMap.get(entry.getKey())));
        }
        for (Map.Entry<KeyCombination, Text> entry : keyPressedTextMap.entrySet()) {
            entry.getValue().setText(skin.getModifiedChar(keyPressedCharMap.get(entry.getKey())));
        }
        boardsBox.getChildren().clear();
        boardsBox.getChildren().addAll(title1, normalBoard, title2, shiftBoard, title3, altBoard, title4, shiftAltBoard, title5, ctrlShiftAltBoard, ctrlChars);
    }

    /**
     * Update the keyboard diagram and show if not already showing.
     */
    public void updateAndShow() {
        update();
        if (!shown) {
            stage.setWidth(650);
            stage.setHeight(970);

            Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
            stage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 650));
            stage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 970));

            shown = true;
        }


        stage.show();
    }



    /**
     * Hide the stage.  Hide and close are the same.  However conceptually, this method is for closing the diagram
     * while the program is running and the stage continues to exist in this static class.
     */
    public void hide() {
        stage.hide();
        decoratedRTA.getEditor().requestFocus();
    }

    /**
     * Close the stage.  Hide and close are the same.  However conceptually, this method is for closing the diagram
     * when the main program closes and so the stage and this class cease to exist.
     */
    public void close() {
        hide();
        stage.close();
    }


    /**
     * The static keyboard window class may or may not have a showing diagram.
     *
     * @return true if the diagram is showing and otherwise false.
     */
    public boolean isShowing() {
        return stage.isShowing();
    }

}





