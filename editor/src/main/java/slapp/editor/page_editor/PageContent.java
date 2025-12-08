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

package slapp.editor.page_editor;

import com.gluonhq.richtextarea.model.Document;

import java.io.Serializable;

/**
 * Element corresponding to a single page of Pagination
 */
public class PageContent implements Serializable {
    private static final long serialVersionUID = 100L;
    Document pageDoc;
    double textHeight;


    /**
     * The content consists of a Document and a height value
     * @param pageDoc the Document
     * @param textHeight the height value
     */
    public PageContent(Document pageDoc, double textHeight) {
        this.pageDoc = pageDoc;
        this.textHeight = textHeight;
    }

    /**
     * Document for this page
     * @return the document
     */
    public Document getPageDoc() {
        return pageDoc;
    }

    /**
     * The text height for this page (may not be same as window size).
     * @return the height
     */
    public double getTextHeight() {
        return textHeight;
    }

    /**
     * The text height for this page (may not be same as window size).
     * @param textHeight
     */
    public void setTextHeight(double textHeight) {
        this.textHeight = textHeight;
    }
}
