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

package slapp.editor.abefg_explain;

import java.io.Serializable;

/**
 * Supplement to ABEFG model to support the choice fields
 */
public class ABEFGmodelExtra implements Serializable {
    private static final long serialVersionUID = 100L;
    private String leaderAB = "";
    private String promptA = "";
    private boolean valueA = false;
    private String promptB = "";
    private boolean valueB = false;
    private String leaderEFG ="";
    private String promptE = "";
    private boolean valueE = false;
    private String promptF = "";
    private boolean valueF = false;
    private String promptG = "";
    private boolean valueG = false;


    /**
     * Create ABmodelExtra with empty defaults
     */
   public ABEFGmodelExtra() {}

    /**
     * Create ABEFG with parameters
     * @param leaderAB the AB choice leaeder
     * @param promptA prompt for the A choice
     * @param valueA value of the A choice
     * @param promptB prompt for the B choice
     * @param valueB value of the B choice
     * @param leaderEFG the EFG choice leader
     * @param promptE prompt for the E choice
     * @param valueE value of the E choice
     * @param promptF prompt for the F choice
     * @param valueF value of the F choice
     * @param promptG prompt for the G choice
     * @param valueG value of the G choice
     */
    public ABEFGmodelExtra(String leaderAB, String promptA, boolean valueA, String promptB, boolean valueB, String leaderEFG, String promptE, boolean valueE, String promptF, boolean valueF, String promptG, boolean valueG) {
        this();
        this.leaderAB = leaderAB;
        this.promptA = promptA;
        this.valueA = valueA;
        this.promptB = promptB;
        this.valueB = valueB;
        this.leaderEFG = leaderEFG;
        this.promptE = promptE;
        this.valueE = valueE;
        this.promptF = promptF;
        this.valueF = valueF;
        this.promptG = promptG;
        this.valueG = valueG;
    }

    /**
     * Leader to appear prior to the AB choice fields
     * @return the string leader
     */
    String getLeaderAB() {
        return leaderAB;
    }

    /**
     * Leader to appear prior to the EFG choice fields
     * @return the string leader
     */
    String getLeaderEFG() { return leaderEFG; }

    /**
     * The prompt for the A choice
     * @return The string prompt
     */
    String getPromptA() {
        return promptA;
    }

    /**
     * The value of the A check box
     * @return true if selected and otherwise false
     */
    boolean getValueA() {
        return valueA;
    }

    /**
     * The prompt for the B choice
     * @return the string prompt
     */
    String getPromptB() {
        return promptB;
    }

    /**
     * The value of the B check box
     * @return true if selected and otherwise false
     */
    boolean getValueB() {
        return valueB;
    }

    /**
     * The prompt for the E choice
     * @return the string prompt
     */
    String getPromptE() {
        return promptE;
    }

    /**
     * The value of the E check box
     * @return true if selected and otherwise false
     */
    boolean getValueE() {
        return valueE;
    }

    /**
     * The prompt for the F check box
     * @return the string prompt
     */
    public String getPromptF() {
        return promptF;
    }

    /**
     * The value of the F check box
     * @return true if selected and otherwise false
     */
    boolean getValueF() {
        return valueF;
    }

    /**
     * The prompt for the G check box
     * @return the string prompt
     */
    String getPromptG() {
        return promptG;
    }

    /**
     * The value of the G check box
     * @return true if selected and otherwise false
     */
    boolean getValueG() {
        return valueG;
    }
}
