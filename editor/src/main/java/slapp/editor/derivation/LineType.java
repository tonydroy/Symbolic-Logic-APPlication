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

package slapp.editor.derivation;

/**
 * Enum for types of derivation line
 */
public enum LineType {
    MAIN_CONTENT_LINE,
    PREMISE_LINE,
    CONCLUSION_LINE,
    SETUP_SHELF_LINE,
    SETUP_GAP_LINE,

    SHELF_LINE,
    GAP_LINE;


    public static boolean isContentLine(LineType type) {
        return (type == MAIN_CONTENT_LINE || type == PREMISE_LINE || type == CONCLUSION_LINE);
    }

    public static boolean isSetupLine(LineType type) {
        return (type == PREMISE_LINE || type == CONCLUSION_LINE || type == SETUP_SHELF_LINE || type == SETUP_GAP_LINE);
    }

    public static boolean isShelfLine(LineType type) {
        return (type == SHELF_LINE || type == SETUP_SHELF_LINE);
    }

    public static boolean isGapLine(LineType type) {
        return (type == GAP_LINE || type == SETUP_GAP_LINE);
    }

}
