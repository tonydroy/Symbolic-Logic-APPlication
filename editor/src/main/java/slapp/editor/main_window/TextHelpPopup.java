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

package slapp.editor.main_window;

import javafx.concurrent.Worker;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import slapp.editor.EditorAlerts;
import slapp.editor.EditorMain;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.awt.Desktop;

/**
 * Show text help in popup window
 *
 * Static help strings set at initialization of class
 */
public class TextHelpPopup {

    private static String about;
    private static String keyboardShortcuts;
    private static String commonElements;
    private static String simpleEdit;
    private static String formMaps;
    private static String verticalTrees;
    private static String horizontalTrees;
    private static String truthTables;
    private static String derivations;
    private static String metalanguage;


static {

    about = "<body style=\"margin-left:10; margin-right: 20\">"+
            "<h2><p style=\"text-align: center;\">Symbolic Logic Application (SLAPP)</p></h2>" +
            "<p style = \"text-align: center;\">Version 3.2</p>" +
            "<p style=\"text-align: center;\">Copyright (c) 2025, Tony Roy</p>" +
            "<p>This program (SLAPP) is open-source and free software.  An executable install package may be downloaded from <a href=\"https://tonyroyphilosophy.net/slapp\">tonyroyphilosophy.net/slapp</a>.  Source code is available on <a href=\"https://www.github.com/tonydroy/Symbolic-Logic-APPlication\">GitHub</a>.  You may redistribute the software and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.  For the license, see <a href=\"https://www.gnu.org/licenses/licenses.html\">www.gnu.org/licenses.html</a>.</p>" +
            "<p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. </p>" +
            "In this preliminary version, every user becomes an evaluator and tester.  Comments are much appreciated.  You may submit comments and error reports by email <a href=\"mailto:messaging@slappservices.net?subject=SLAPP: (your issue)&body=Please be as specific as you can about your concern; if you are reporting an error, include information about the version of SLAPP and of your operating system, and (if possible) whether and how the problem may be repeated (ok to delete this line).\"> here </a>and by the 'comment / report' help menu item.  Those who are technically adept may submit through the <a href=\"https://www.github.com/tonydroy/Symbolic-Logic-APPlication\">GitHub</a> issues section.  Reviews and other items that deserve public discussion may be submitted through the <a href=\"https://tonyroyphilosophy.net/textbook-blog/\">Symbolic Logic Blog</a>.</p>"
            ;

    commonElements = "<body style=\"margin-left:10; margin-right: 20\">" +
            "<p><h3>Common Functions:</h3></p>" +
            "<p>The SLAPP file structure consists of exercises, and then assignments made up of exercises.  You can open and work an exercise as such.  However, in the usual case, you begin with an assignment.</p>" +
            "<ol><li><h4>Top Menu Bar:</li>" +
            "<ul><li>The assignment dropdown has the usual operations: open, open recent, save, save as, close, print, and export to PDF.  (The export option is disabled on Mac: to create a PDF on Mac, select the regular SLAPP print option and then the PDF/Save as PDF from the Mac printer window.)  The 'create' options have application for instructors.<br><br>" +
            "In addition to the menu options, double clicking a SLAPP file will start the app and/or open the file in the program.<br><br></li>" +
            "<li>The first time you open an assignment you will be asked to provide header information.  The name field is required.  Then the '+' and '-' buttons add and remove fields for optional header items (as student number or course section).  You will not be able to return to this screen without restarting the assignment from scratch; so be sure that the information is complete and correct as you enter it.  <br><br></li>" +
            "<li>Once you have an open assignment, move among exercises by the 'previous', 'next' and 'jump' items from the menu bar.  The 'comment' item opens a window to add a comment on the assignment as a whole; the comment appears at the top of a printed assignment.  The 'print' menu item duplicates print and export commands from the assignment and exercise items, and adds options for printer and page setup. <br><br></li>" +
            "<li>The 'Help' menu item includes help videos on the different exercise types.  In addition to this text page, the 'Contextual' help item pops up text help relevant to whatever exercise is currently open.<br><br></li>" +
            "<li>In this version, SLAPP does not make use of the regular Mac menu bar -- the menu bar is rather a Windows-style bar across the top of the SLAPP window.</li>" +
            "</ul>" +
            "<li><h4>Edit Commands:</li>" +
            "<ul><li>The button with the small keyboard icon pops up a keyboard diagram.  The diagram lists normal, shift, alt, shift-alt, and ctrl-shift-alt boards (mac: normal, shift, option, shift-option and command-shift-option). The content of these boards is modified by the keyboard selector dropdown.  The keyboard selector shifts base alphabet characters, while special characters remain the same.  Keyboard options are meant to group together characters you will need in a given context.<br><br>" +
            "The bottom of the keyboard diagram lists keyboard shortcuts. There is also a list of shortcuts from the Help dropdown.   Note that there is no 'right-click' context menu so that copy, paste, and such come only by buttons at the top or by these keyboard shortcuts. <br><br>" +
            "Also: You will not need all the characters from all the boards at once!  You should be able to go through a good part of <i>Symbolic Logic</i> using nothing but the few logic characters sprinkled into the alt- and shift-alt boards, and just the Base/Italic and Italic/Sans keyboard selections.<br><br></li>" +
            "<li>In the unlikely event that you need a symbol not on the keyboard diagrams, you can insert any Unicode character by the unicode field.  You may enter either a decimal (preceded by '#') or hexadecimal (preceded by 'x') code.  Unicode includes over 100,000 character codes. You will be able to display a Unicode character so long as that character exists in a font on your computer.  If you enter a code for which there is no representation, you usually see an empty box.<br><br></li> " +
            "<li>In the underlying file, the ordinary representation for a character is a single 16-bit word.  But Unicode has more characters than can be represented in 16 bits.  A standard solution is to use just one 16-bit word in the ordinary case, but two words for character codes that fall outside of the 16-bit range.  This manifests itself in SLAPP especially when deleting characters -- the delete takes out one word at a time, where the second word typically does not represent anything, and so shows as an empty box.  If you see this, the program is not broken -- it is only that you have half the representation of a character in the underlying file and you can just delete again.<br><br></li>" +
            "<li>Many of the edit buttons: cut, copy, paste, and such are what you expect.  A couple call for special comment:<br><br></li>" +
            "<ul><li>Superscript and subscript have 'regular' and green 'shifted' versions (and work also by shifted or unshifted page up and page down keys).  The regular versions work in the usual way.  Sometimes, however, you want a superscript over a subscript.  The shifted versions back up one space before adding the character.  This is not a complete solution to putting superscripts over subscripts, as it only moves back one space.  But in many cases this is just what you need.  Note that the shift applies to any text after it is applied and text reverts to its normal position after the shift is removed -- a result is that you always end up with a space after a shifted super- or sub-script -- as the text reverts to the place it would have been without the shift.<br><br></li> " +
            "<li>The save button saves an open exercise in the current (most recent) exercise directory, or an assignment in the current assignment directory.  If an exercise is open as such, it saves the exercise.  If an assignment is open, it saves the assignment.<br><br></li></ul>" +
            "<li>Keys in the bottom row of the keyboard diagram with a 'dotted circle' are for 'two-stroke' characters (they are 'dead keys'): typing a circle key followed by a regular character results in the character modified by the symbol from the circle key (overline, bar, arrow, hat, slash).  This works so long as the combined character exists in the SLAPP font.  Excluding the use of bold and italic buttons, overline works with any character that can be typed from a regular SLAPP keyboard; bar, arrow, and hat work with alphanumeric characters; slash adds a negating slash to relations whose negation is not already on the keyboard, along with some miscellaneous (seldom used) characters.  In each case the keyboard diagram reflects the modified characters when the dead key is pressed. With the overlaine button depressed, all typed characters are overlined.<br><br>" +
            "Note that overline does not work in the same way as ordinary underline and strikethrough.  The latter overlay a line on top of characters from a font.  Overline takes characters from the font each of which has a built-in overline -- such that the lines appear as continuous when the characters are typed together.<br><br>" +
            "</ul>" +
            "<li><h4>Page Management:</h4>" +
            "Many SLAPP exercises involve graphical elements.  While there is no problem in the vast majority of cases, it is possible for such elements to extend beyond regular page boundaries.  SLAPP breaks printed pages only at the boundaries of its boxes.  If the content of a box extends beyond one page, print will clip whatever is beyond the page border.  This means that you have to manage page breaks.<br><br> </li>" +
            "<ul><li> The 'V Sz' and 'H Sz' spinners reflect the horizontal and vertical size of a current content area (window) as a percentage of the selected paper size.  In some cases you can use the counters to change an area's vertical or horizontal size, and in others the counters simply reflect a size automatically set.<br><br></li>" +
            "<li>Windows which take multiple lines of text show scroll bars as text exceeds window size.  As you type in such a window, the 'T Ht' label shows the total height of the text in that window as a percentage of selected paper size.<br><br></li>" +
            "<li>In case content exceeds the selected paper size, there are different options:<br><br></li>" +
            "<ul><li>In many cases, you can solve the problem by changing the page setup -- the selected paper, the page orientation, or the margins.  Page setup options apply to an entire print  job, and so to all the exercises in a printed assignment and not just to a member that is giving you trouble.<br><br></li>" +
            "<li>In addition, from 'print/scale setup' the 'base scale' increases or decreases print size for an assignment -- you may actually prefer the look of a reduced scale (and save some trees while you are at it) as the normal SLAPP print layouts are relatively spacious.  With 'fit to page' selected, SLAPP reduces oversize nodes to fit on the selected paper.  This is sufficient to print any exercise.  Some experimentation should reveal a range where you are comfortable with the reduced size.<br><br></li>" +
            "<li>If an electronic copy will do, it may be convenient to submit just a PDF file -- where PDF accommodates any paper size your system will allow.  Or you might be able to submit a SLAPP file directly, in which case there are no size limitations (and comment fields on assignments and exercises remain live).<br><br></li></ul>" +
            "</ul>" +
            "<li><h4>Technical Matters:</h4>" +
            "<ul><li><i>Hot Keys:</i> SLAPP uses multiple key combinations.  These are not 'hot keys' of the sort that work even when an app is not in the active window.  However other apps may use the same key combinations as hot keys.  If hotkey combinations do overlap with combinations used by SLAPP it is likely that the hotkey functions will fire when the combination is typed.  SLAPP does its best to avoid standard Mac and PC combinations.  If there is overlap, most apps have a means of changing their hot-key combinations. <br><br>" +
            "<li>There can be a related problem with system <i>dead keys</i>.  As for ones in SLAPP, these place an accent on the next (or previous) typed character, and are a function of your system's keyboard map.  You want system dead keys off.<br><br> " +
            "<ul><li>On PC you will be fine with (the usual default) English (United States) and the US QWERTY board.  If you have a different default, from Settings / Time & language / Language & region, you can add English (United States) and from its language options, the US QWERTY board. Then you can use the windows key and space bar to cycle among installed boards.<br><br></li>" +
            "<li>For Mac, from System Settings / Keyboard / Input Sources, select 'show input menu in menu bar' (this will let you select among keyboard maps from a dropdown on the menu bar). Then you can choose which keyboards to show in the menu by the '+' and '-' buttons. From '+', select 'other' at the bottom of the language list, and add 'Unicode Hex Input'.  <br><br></li></ul>" +
            "<li><i>Function Keys:</i> SLAPP makes use of the function keys F1 - F12.  On many computers these keys are assigned to special functions (for volume and the like).  If this is so, there are generally methods of changing the default behavior between regular F-key and the special assignments (different on different systems and keyboards).  Whichever is selected, holding down the Fn-key at the same time as you type a function key gives the non-default behavior.  As Examples, <br><br>" +
            "<ul><li>A PC with the Logitech MX keyboard toggles function keys by Fn-Esc.<br><br></li>" +
            "<li>On Mac OS Sequoia, there are controls from System Settings / Keyboard / Keyboard Shortcuts / Function Keys, and then again from System Settings / Desktop & Dock / Shortcuts / Keyboard & Mouse Shortcuts.<br><br></li></ul>" +
            "<li><i>Mouse Right-Click:</i> SLAPP makes use of right-click on the mouse.  On a PC, this is most always enabled.  There are different means of performing right-click on a Mac, including modifications from System Settings / Mouse (or Trackpad) / Secondary Click.<br><br></li>" +
            "<li><i>Rich Text Area:</i> SLAPP makes use of the <a href=\"https://github.com/gluonhq/rich-text-area\">Rich Text Area</a> Java control for most text editing purposes.  This is a wonderful tool insofar as it (is open-source and) in a modified form makes possible special characters and the like required for SLAPP.  However the Rich Text Area (RTA) is itself a work in progress.  Recent improvements have made it vastly more stable.  However, there remain a couple of contexts where it can give trouble.  <br><br></li>" +
            "<ul>" +
            "<li>In its current version, the RTA may give an error in case you attempt to copy and paste an <i>indented</i> line or paragraph.  In this case, you should see a popup warning: save your work, then close and restart SLAPP.  You should not have lost anything.<br><br></li>" +
            "<li>Some of the RTA insert options (lists, pictures, and such) are fairly primitive -- lacking features you might expect.  This is so especially for tables (which strangely when indented).<br><br></li>" +
            "<li>RTA scrollbars \"jump\" when you are adding text at the very end of a field.  Adding a final word such as 'END' (which you can delete later) and inserting prior to it removes the problem." +
            "</ul>" +
            "</ol>"
            ;

    simpleEdit = "<body style=\"margin-left:10; margin-right: 20\">" +
            "<p><h3>Edit Exercises</h3></p>" +
            "<p>An edit exercise includes one or more windows for text entry.  These exercises may range from short answer to multiple pages.  Edit exercises come in two main forms: In the first form, you see buttons on the left, the second has its buttons on the right." +
            "<ol><li>The simplest form of the edit exercise has a text field with the ability to add and remove additional pages by 'insert page' and 'remove page' buttons on the left.<br><br>" +
            "<ul><li>The 'Insert' button adds a page after the current page.  'Remove' deletes the current page.  You will not be able to have less than one page.<br><br></li>" +
            "<li>Pages are inserted 'horizontally' and you move from one page to another by the control at the bottom.<br><br></li>" +
            "<li>This form of the edit exercise is often combined with some simple choice checkboxes with the edit field for an explanation after.<br><br></li></ul></li>" +
            "<li>The second form of the edit exercise also begins with a text field.  In this case, fields are added 'vertically' as manipulated by controls on the right.<br><br>" +
            "<ul><li>You can insert a new edit field below the current one by the 'simple edit' button.  The active field may exchanged with the one above or below, removed, and restored, by the relevant buttons.<br><br></li>" +
            "<li>This form of the exercise is not limited to edit fields.  As you progress, there may be options to insert objects including tree diagrams, truth tables, and derivations.  When an object is active, its own special controls appear on the left.  These work just as in dedicated exercises for the diagrams, tables, and derivations (you should not encounter these options until you have already encountered them in their stand-alone form).<br><br></li>" +
            "<li> The right-hand indent and outdent buttons do not apply to edit fields (which have their own internal indent mechanism).  Rather they are used to indent and outdent boxes for objects, such as tables and derivations, that are without internal indent mechanisms.<br><br></li>" +
            "</ul></li>" +
            "</ol>" +
            "<ul><li>Because of its 'one page per window' setup and relatively limited editing capacities (compared to, say, Word), SLAPP is less than ideal for multiple-page essays.  All the same, you <em>can</em> produce multiple page documents in SLAPP.  Then you retain editing features unique to SLAPP -- special characters and the like.  And with the second form of the edit exercise, it is easy to include into your documents graphical elements including trees, tables and derivations. </li></ul>"
    ;

    formMaps = "<body style=\"margin-left:10; margin-right: 20\">" +
            "<p><h3>Form Maps</h3></p>" +
            "<p>Form map exercises appear in chapter 2 of <i>Symbolic Logic</i>.  Controls are straightforward (and anticipate ones for \"vertical trees\", also encountered in chapter 2).</p>" +
            "<ol><li>"+
            "<p>A map exercise starts with \"upper\" and \"lower\" expression boxes.  The aim is to \"map\" elements from the upper box to the lower.  Each box has a gray popup button on its left that can be used to drag the box around in the work area. </p>" +
            "<ul>"+
            "<li><p>To start, click into one of the boxes.  Then F10 adds a marker at the current cursor position.  Move the cursor, and F10 again adds a second marker.  Pressing F10 a third time removes markers, and the cycle begins again. And similarly for the other box.  Right click on a box removes any markers. </li>" +
            "<li><p>With at least one marker in each box, F11 connects them by a line.  If a field has two markers, the line reaches to a bracket whose leftmost end is at the left marker, and rightmost point at the right marker.  If there is a single marker, the line reaches to the marked character.  It is possible to 'grab' the center of a line with the mouse; then right-click deletes it.</p></li>" +
            "<li><p>With at least one marker in a selected formula field, F12 adds '?' above.  If there are two markers, the question mark attaches to a bracket whose leftmost end is at the left marker, and rightmost point at the right marker.  If there is a single marker, the question mark attaches to the marked character.  It is possible to grab the question mark with the mouse; then right-click deletes it.</p></li>" +
            "<li><p>There should be no need to alter expressions in the boxes.  However it is worth noting that, although map elements are first set relative to formula characters, they are linked to positions on the box.  A result is that changes to the formula may leave links incorrectly positioned.  So if an expression changes, it is likely that mapping will need to be redone.</p></li>" +
            "</ul></li>" +
            "<li><p>The check function evaluates whether you have produced a good map.  'Check Progress' evaluates existing links for correctness.  'Check Map' adds the requirement that expressions are <i>completely</i> mapped.</p>"+
            "<p>Observe that Check Map must fail in case the correct response is that there is no map -- and so that the expression is not of the given form.  It remains that any links you make will be evaluated by Check Progress -- where these may help focus an explanation of why there is no complete map.</p></li>" +
            "</li>"+
            "</ol>"
            ;

    verticalTrees = "<body style=\"margin-left:10; margin-right: 20\">" +
            "<p><h3>VerticalTrees</h3></p>" +
            "<p>Vertical trees appear in a variety of contexts and in a variety of forms, as sprinkled through chapters 2, 4, and 5 of <i>Symbolic Logic</i>.  Such exercises have a variety of controls, where different exercises may include different combinations of them.</p>" +

            "<ol>" +
            "<li><p>A vertical tree exercise always  has a bar over the main work area containing items that may be dragged into the work area.  Among the possibilities are a blue formula field, a green formula field, a gray vertical bracket, and/or a gray dotted horizontal line.</p>" +
            "<ul>" +
            "<li><p>Use the blue box for \"nodes\" of your tree.  Use the green box for justification fields (usually appearing down the right).</p></li>" +
            "<li><p>Once placed in the work area, the formula fields grow as you type in them; bracket and line may be sized from the far end (the right or bottom) by the mouse.  Each of the items has small popup buttons at the start (the left or top) -- gray to drag the object around in the work area, black to delete.</p></li>" +
            "<li><p>The initial size of the work area is on the small side.  However the space adjusts its size to accommodate its contents.  </p>" +
            "<p>This works well so long as the work space fits within the main SLAPP window.  To obtain a larger space you can increase the size of the SLAPP window, or alternatively \"push\" a content beyond the window bounds -- then the work area will expand (and scroll) to match.  (The vertical bracket and horizontal line work well for this, as they can be extended to push the area over their entire length.)   </p></li>" +
            "</li></ul>" +

            "<li>In addition to its move and delete buttons, a blue formula field has popup grey buttons above and below the center of the field.  Grabbing a lower (upper) button of one field and dragging to an upper (lower) button of another results in  a line between the two. This line is linked to the formula fields and moves with them as the fields move or grow. It is possible to 'grab' the center of a line with the mouse; then right-click deletes it.  Removing a box removes any lines to which it is connected. In addition, the blue formula field may have any of the following controls on the left: <br><br></li>" +

            "<ul><li>With 'box' selected, left-click adds a solid outline around the formula space.  Right-click removes the outline.<br><br></li>" +
            "<li>With 'star' selected, left-click adds a solid star at the upper right-hand corner of the formula space.  Right-click removes the star.<br><br></li>" +
            "<li>With 'annotation' (the stacked boxes) selected, left-click adds a small annotation field at the upper right-hand corner of the formula space.  Right-click removes the field.  The small '+' button adds annotation fields to each formula space, and '-' removes them all.  Star and annotation options exclude one another. <br><br></li>" +
            "<li> With 'circle' selected, F10 adds a marker at the current cursor position.  Move the cursor, and F10 again adds a second marker.  Pressing F10 a third time removes markers, and the cycle begins again.  With two markers, F11 adds a circle whose leftmost point is at the left marker and rightmost point is at the right marker.  Right-click removes a circle and any markers.  In order for the circle not to 'walk' on characters in the formula space, you can insert a space on either side of the circled item(s), and the markers just before the spaces.<br><br></li>" +
            "<li> Underlines work very much like circle.  With 'underlines' (horizontal bars) selected, F10 adds a marker at the current cursor position.  Move the cursor, and F10 again adds a second marker.  Pressing F10 a third time removes markers and the cycle begins again.  With two markers, F11 adds an underline whose leftmost point is at the left marker, and rightmost point at the right marker.  Right-click removes all underlines and any markers on the field.  A new underline always rests just above any underlines beneath it -- if necessary, 'pushing' existing lines down.  This will be what you want so long as you begin with longer lines (for main operators) first, and come with shorter ones 'contained' within them after.<br><br></li>" +
            "</ul>" +
            "<p>These elements attach to the <i>formula box</i>.  This is just what you want for the box, star, and annotation options.  However, although circle and underline are first set relative to formula characters, they too are linked to the boxes.  A result is that changes to the formula may leave these incorrectly positioned.  So it is best to add circle and underline only after the formula is finalized. </p>" +

            "<li><p>The check function checks your tree for correctness.  Depending on how an exercise is set up it may check the tree, justifications, and/or markup.</p></li>" +
            "<ul>" +
            "<li><p>For a given row use a single justification field.  This one field may include justifications for multiple nodes of the tree -- just list them in order from left to right.  (SLAPP looks just for the rule that justifies the node, as 'FR(\u2192)' or the like, other content is ignored.)  </p></li>" +
            "<li><p>Checking for some tree exercises depends upon a previously completed tree.  In this case there is a thumbnail on the left which you can click to pop up your previous tree.  This checking works only in the context of an assignment including the previous tree. </p></li>" +
            "</ul>" +
            "</li>" +
            "<li>It is likely that some vertical trees will overrun the margins of a standard printed page (especially ones in chapter 5 of <em>Symbolic Logic</em>).  In this case, it will often make sense to modify page and/or scale settings from the 'print' dropdown.</li>" +
            "</ul>"
            ;

    horizontalTrees = "<body style=\"margin-left:10; margin-right: 20\">"+
            "<body style=\"margin-left:10; margin-right: 20\">" +
            "<p><h3>HorizontalTrees</h3></p>" +
            "<p>Horizontal trees appear especially in the quantificational portions of chapters 4 and 5 of <i>Symbolic Logic</i>.  Though there are a number of controls, their application is reasonably intuitive.</p>" +

            "<ol><li>A horizontal tree always begins by placing a 'root' formula box in the main work area.  Branches 'grow' from a root.   Select the box control on the left, and click on the work area to add the box.  To have more than one tree in a work area, add more than one root.  With the box control selected, right click on a box removes it together with any branches it may have.  As for vertical trees, a formula box sizes as you type in it. A root formula box, together with its branches, may be moved in the work area by the grey popup button to its left. <br><br></li>" +

            "<li>Other controls down the left apply to growing trees.<br><br></li>" +
            "<ul><li>One, two, or three branches may be added to a formula node by selecting the relevant control, and clicking on the formula box. Branching for formulas adds a field for the branching operator, and then fields for the formula branches.  In fact, there is no limit to the number of branches a node may have -- as another click adds one, two, or three branches again (but it would be unusual to require more than two or three branches).  For a node with one or more branches, it is possible to indicate that the branches continue indefinitely, by adding a 'dots' branch with the control showing dots underneath a bar.<br><br></li>" +
            "<li>If a break is required to separate a formula from its terms, a vertical dotted line may be added by selecting the relevant control and clicking on the formula box.  With the control selected, right-click removes the break.<br><br></li>" +
            "<li>Branching for terms works by selecting the relevant control and clicking on a node.  Such branching looks like that for vertical trees except rotated clockwise by 90&#176;. Again, there is no limit to the number of branches a node may have -- as another click adds one or two branches again (but it would be unusual to require more than one or two branches).  It is not possible to add a formula branch to a term node; and it is not possible for the immediate branches of any node to include both formula and term nodes.<br><br></li>" +
            "<li>By the control with vertical tick marks across a line, it is possible to add a 'ruler' over the main work area.  There is no special scale to these marks.  They are meant merely to locate positions in the work area, in order to aid references to one portion of a tree or another.<br><br></li>" +
            "<li>As for vertical trees, selecting the control with the stacked boxes adds (left click) or removes (right click) an annotation box in the upper right corner of formula and term boxes.  The small '+' button adds annotation fields to them all; the small '-' button removes them from all.</li></ull></ol>" +
            "<ul><li>It is likely that some horizontal trees will overrun the margins of a standard page.  Very often it will make sense to modify page and/or scale settings from the 'print' dropdown.</li></ul>"
            ;

    truthTables = "<body style=\"margin-left:10; margin-right: 20\">" +
            "<p><h3>Truth Table Exercises</h3></p>" +
            "<p>There are two basic contexts for constructing truth tables.   </p>" +
            "<ol>" +
            "<li><p>In the first and most basic case, you begin with some controls on the left, and table formulas over a line at the top of the work area.</p></li>" +
            "<ul>" +
            "<li><p>To start, enter the setup information on the left:  '+' and '-' add and remove fields for basic sentences.  Enter the basic sentences, and then the number of table rows.  Press 'setup'.  This creates the table.</p></li>" +
            "<li><p>Then it is a straightforward matter to enter table values. A given cell holds a single uppercase character (usually 'T' or 'F').  Automatic cursor movement is down.</p></li>" +
            "<li><p>Each row includes a short comment field at the right.  In most cases the comment will either be blank or a simple marker on the row. </p></li>" +
            "<li><p>Pressing a circular button highlights its column.  Highlighting may be helpful to 'direct the eye' as you work the table and, in the end, to mark main columns.</p></li>" +
            "</ul>" +
            "<li><p>The second context is like the first, except that there is a work space for you to supply an interpretation function and/or translation, and then fields on the left to enter the table formulas.  After that, table construction is just as in the simple case.</p></li>" +
            "<li><p>Either context may include choice boxes at the bottom, along with a field to explain your choice.</p>" +
            "<p>And either context may include check and help functions on the right:</p></li>" +
            "<ul>" +
            "<li><p>If enabled, Static Help pops up a fixed message associated with that particular exercise.</p></li>" +
            "<li><p>Check Table checks table values and, if the exercise includes a choice option, usually checks the choice.</p></li>" +
            "<li><p>In some cases it is possible to \"shortcut\" a full table without doing all the rows.  If Short Table is selected check applies to the short table.  In this case, feedback is somewhat different, applying row-by-row to rows with (some) completed cells, rather than to the whole table and to cells individually. </p></li>" +
            "</ul>" +
            "</ol>" +
            "<ul><li>Though there is no problem in most cases, some truth table exercises overrun a standard page size.  The problem is usually solved by modifying print or scale settings.</li></ul>"
            ;

    derivations = "<body style=\"margin-left:10; margin-right: 20\">" +
            "<p><h3>Derivation Exercises</h3></p>" +
            "<p>A derivation exercise typically begins with premise(s) at the top, and a conclusion at the bottom.  In the usual case, you will not be able to modify the concluding formula and not be able to modify either a premise or its justification.</p> " +
            "<ol><li>There is a vertical slider bar to adjust the width of formula fields.  In addition you can change the width of a derivation window by its width spinner.  Move within a derivation by the mouse and/or Enter/Return, Ctrl/Cmd-up, &#8209;down, &#8209;right, &#8209;left (Ctrl/Cmd optional for &#8209;up, &#8209;down). Exercises conducted in the metalanguage may show a button with script \u2133 for metalanguage help.<br><br></li>" +
            "<li>After that, buttons on the left are reasonably straightforward:<br><br></li>" +
            "<ul><li>The 'insert line' button inserts a line above the current line (at current scope depth), and 'delete line' removes the current line.<br><br></li>" +
            "<li>The 'insert subder' button adds lines for a new subderivation just above the current line, and 'insert subders' adds lines for a pair of subderivations above the current line (as for &#x2194;I or &#x2228;E). <br><br></li>" +
            "<li>It is possible to modify or generate suberivations 'by hand' using the 'indent line', 'outdent line', 'add shelf' and 'add gap' buttons.  The indent button increases the scope of the current line by one; outdent reduces it by one.  Add shelf inserts a small shelf beneath the current line, and add gap inserts a gap just below the current line.  Removing a line removes a shelf or gap beneath it.<br><br></li>" +
            "<li>Once you enter line numbers for a justification, the numbers in the justification are automatically adjusted as you insert and delete lines above.<br><br>" +
            "<i>*It is good practice to enter justifications of lines that depend on ones above as soon as the justifying lines exist.</i> For example, if a line <i>L</i> is to be justified by a suberivation, you can enter the justification for <i>L</i> when that suberivation is first set up, rather than waiting until the suberivation is complete -- and let SLAPP manage justification line numbers as lines are inserted or deleted.<br><br></li></ul>" +
            "<li>Controls for derivation checking and help appear on the right (where different controls may be active for different exercises).  Optionally, the number of check or help tries may be limited as part of the exercise.<br><br></li>" +
            "<ul> " +
            "<li>The check buttons initiate a correctness check.  The two functions are alike except that the Progress check skips over unjustified lines (so you can see how you are doing so far), where the Final check requires that every nonempty line be correctly justified.<br><br></li>" +
            "<li>Contextual help offers suggestions for \"what to do next\" -- always relative to a selected goal formula -- its suggestions are for how to reach <em>that</em>.  (The help suggestions mirror \"strategies\" developed in <em>Symbolic Logic</em> chapter 6.)  <br><br></li>" +
            "<li>Static help pops up a fixed message associated with that particular exercise.<br><br></li>" +
            "</ul>" +
            "<li>SLAPP will attempt to link and adjust any number that is part of a justification.  In the vast majority of cases, this is just what you want.  But, in some cases, you want a justification to include a fixed number (as for a theorm or axiom number, as T6.15 or A8).  Then the fixed number must be distinct from that of any line.  Since line numbers are always whole numbers, this happens automatically if the justification number contains a 'point'.  If there is no natural point in the justification number, you can distinguish the justification number from the line numbers by adding a leading point (as A.8).</li>" +
            "</ol>";

    keyboardShortcuts = "<body style=\"margin-left:10; margin-right: 20\">" +
            "<p><h3>Editor Keyboard Shortcuts:</h3></p>" +
            "<ul>" +
            "<li>F1 Base/Italic keyboard     </li>" +
            "<li>F2 Base/Script keyboard    </li>" +
            "<li>F3 Base/Sans keyboard    </li>" +
            "<li>F4 Italic/Sans keyboard     </li>" +
            "<li>F5 Italic/Blackboard keyboard     </li>" +
            "<li>F6 Script/Italic keyboard     </li>" +
            "<li>F7 Script/Sans keyboard     </li>" +
            "<li>F8 Greek/Fraktur keyboard     </li>" +
            "<li>F9 Toggle to previous keyboard     </li>" +
            "<li>PgUp Toggle superscript on/off    </li>" +
            "<li>Shift-PgUp Toggle shifted superscript on/off    </li>" +
            "<li>PgDn Toggle subscript on/off    </li>" +
            "<li>Shift-PgDn Toggle shifted subscript on/off    </li>" +
            "<li>Ctrl/Cmd-B Toggle bold on/off   </li>" +
            "<li>Ctrl/Cmd-I Toggle italic on/off    </li>" +
            "<li>Ctrl/Cmd-U Toggle underline on/off   </li>" +
            "<li>Ctrl/Cmd-0 (zero) Toggle overline on/off    </li>" +
            "<li>Ctrl/Cmd-A Select all    </li>" +
            "<li>Ctrl/Cmd-C Copy    </li>" +
            "<li>Ctrl/Cmd-X Cut    </li>" +
            "<li>Ctrl/Cmd-V Paste    </li>" +
            "<li>Ctrl/Cmd-Z Undo    </li>" +
            "<li>Shift-Ctrl/Cmd-Z Redo    </li>" +
            "</ul>"
            ;

    metalanguage = "<body style=\"margin-left:10; margin-right: 20\">" +
            "<p><h3>Metalanguage</h3></p>" +

            "<p>A metalanguage is informally described in <em>Symbolic Logic</em>.  For the most part, this should be intuitive.  But SLAPP adds wrinkles.  Here are some details:* </p>" +
            "<ul>" +
            "<li><h4>Vocabulary</h4></li>" +
                "<ul>" +
                "<li><p>Punctuation: ( ) &nbsp [ ] &nbsp : &nbsp ,</p></li>" +
                "<li><p>Operator: \u223c, \u2192, \u2194, \u2227, \u2228, \u2191, \u2193, \u2200, \u2203 </p></li>" +
                "<li><p>Variables: script &#x1d4ca;. . . &#x1d4cf; and italic &#x1d44e; . . . &#x1d467 with or without positive integer subscripts. </p></li>" +
                "<li><p>Constants:  \ue886 and script &#x1d4b6; . . . \u212f with or without positive integer subscripts.</p></li>" +
                "<li><p>Function symbols: &#x1d446;, \ue8b8, \ue8ba, and for positive <em>n:</em>, script &#x1d4bb;<sup><em>n</em></sup>, \u210a<sup><em>n</em></sup>, and &#x1d4bd;<sup><em>n</em></sup> with or without positive integer subscripts.   </p></li>" +
                "<li><p>Sentence Letters: \u22a5 and script &#x1d4ae; with or without positive integer subscripts. </p></li>" +
                "<li><p>Relation Symbols: \ue8ac, \ue8a4, \ue8a6, \ue8ad, \ue8a5, \ue8a7 and for positive <em>n,</em> script \u211b<sup><em>n</em></sup> with or without integer subscripts. </p></li>" +
                "<li><p>Term Symbols: script &#x1d4c2; . . . &#x1d4c9;.</p></li>" +
                "<li><p>Formula Symbols: script &#x1d49c; . . . &#x1d4ac;.  </p></li>" +
                "</ul>" +
            "<li><h4>Special Notations:</h4></li>" +
                "<ul>" +
                "<li><p>As described at the end of <em>Symbolic Logic</em> section 6.3.3, &#x1d4ac;(&#x1d4cd;), &#x1d4ac;(&#x1d4cd;, &#x1d4ce;) and such indicate that &#x1d4ac; " +
                "may have instances of the indicated variables free -- and, in context, &#x1d4ac; without the parenthetical notation that the variables are not free." +
                "Then &#x1d4ac;(&#x1d4c9;) is &#x1d4ac; with &#x1d4cd; replaced by &#x1d4c9;.</p></li>" +
                "<li><p>In SLAPP, &#x1d4c9;<sup>&#x1d4cd;</sup> (with superscript variable) indicates that term &#x1d4c9; does not include that variable.  And &#x1d4c9;&#8194<sup>\u22c6</sup> (superscript star, not asterisk) behaves as if it is variable free " +
                "-- so that it is sure to satisfy \"free for\" constraints.  You should not use these designations unless the conditions are stated as a condition of the exercise.</p></li> "+
                "</ul>" +

            "</ul>" +
            "<p>*Please ignore empty box characters (which result from a Java bug).  Will change this view if the problem persists.</p>"

            ;



}

    /**
     * Show the about help item.
     */
    public static void helpAbout() {
       showHelp(about);
    }

    /**
     * Show the common elements help item.
     */
    public static void helpCommonElements() {
        showHelp(commonElements);
    }

    public static void helpKeyboardShortcut() {showHelp(keyboardShortcuts); }

    public static void helpMetalanguage() {showHelp(metalanguage); }

    /**
     * Show help relevant to current exercise type.
     *
     * @param type the exercise type
     */
    //switch "falls through" to help item relevant to members of a type grouping.
    public static void helpContextual(ExerciseType type) {

        if (type == null) {
            EditorAlerts.fleetingRedPopup("Open exercise to obtain contextual help.");
            return;
        }

        switch(type) {
            case FREE_FORM: {}
            case SIMPLE_EDIT: {}
            case SIMPLE_TRANS: {}
            case AB_EXPLAIN: { }
            case ABEFG_EXPLAIN: { }
            case PAGE_EDIT: {
                showHelp(simpleEdit);
                break;
            }

            case DRVTN_EXP: {}
            case DERIVATION: {
                showHelp(derivations);
                break;
            }

            case TRUTH_TABLE_ABEXP: { }
            case TRUTH_TABLE_GENERATE: {}
            case TRUTH_TABLE: {
                showHelp(truthTables);
                break;
            }

            case MAP_AB_EXPLAIN: {
                showHelp(formMaps);
                break;
            }

            case VERTICAL_TREE: {}
            case VERTICAL_TREE_EXP: {}
            case VERTICAL_TREE_ABEXP: {}
            case VERTICAL_TREE_ABEFEXP: {
                showHelp(verticalTrees);
                break;
            }

           case HORIZONTAL_TREE: {
                showHelp(horizontalTrees);
                break;
            }

            default: {
            }
        }

    }

    /**
     * Create WebView and place on new Stage
     *
     * @param helpString the html string to be displayed.
     */
    private static void showHelp(String helpString) {

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        webEngine.setUserStyleSheetLocation("data:, body {font: 16px Noto Serif Combo; }");
        webEngine.loadContent(helpString);

        //open links in native browser
        webEngine.getLoadWorker().stateProperty().addListener((ob, ov, nv) -> {
            if (nv == Worker.State.SUCCEEDED) {
                Document document = webEngine.getDocument();
                NodeList nodeList = document.getElementsByTagName("a");
                for (int i = 0; i < nodeList.getLength(); i++)
                {
                    Node node= nodeList.item(i);
                    EventTarget eventTarget = (EventTarget) node;
                    eventTarget.addEventListener("click", new EventListener()
                    {
                        @Override
                        public void handleEvent(Event evt)
                        {
                            EventTarget target = evt.getCurrentTarget();
                            HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
                            String href = anchorElement.getHref();
                            //handle opening URL outside JavaFX WebView
                            System.out.println(href);

                            try {
                                Desktop.getDesktop().browse(new URI(href));
                            } catch (URISyntaxException e) {
                                System.out.println("URISyntaxException (textHelpPopup)");
                            } catch (IOException e) {
                                System.out.println("IOException (textHelpPopup)");
                            }
                            evt.preventDefault();
                        }
                    }, false);
                }
            }
        });

        VBox root = new VBox(webView);
        root.setVgrow(webView, Priority.ALWAYS);
        Scene scene = new Scene(root);

        Stage stage = new Stage();
        stage.setWidth(800);
        stage.setHeight(600);
        stage.setScene(scene);
        stage.setTitle("SLAPP Text Help");
        stage.initModality(Modality.NONE);
        stage.getIcons().addAll(EditorMain.icons);
        stage.initOwner(EditorMain.mainStage);
        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
        stage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 860));
        stage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 850));

        stage.show();
    }

}
