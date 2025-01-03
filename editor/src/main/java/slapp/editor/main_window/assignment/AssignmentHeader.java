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

package slapp.editor.main_window.assignment;

import com.gluonhq.richtextarea.model.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model for assignment header.  Contains ID information, assignment comment, and optional items added from student and instructor.
 */
public class AssignmentHeader implements Serializable {
    private static final long serialVersionUID = 100L;
    private String creationID = "";
    private String workingID = "";
    private String assignmentName = "";
    private String studentName = "";
    private Document comment = new Document();
    private double commentTextHeight = 0;
    private List<AssignmentHeaderItem> instructorItems = new ArrayList<>();
    private List<AssignmentHeaderItem> studentItems = new ArrayList<>();

    /**
     * Create header object with default (empty) field values.
     */
    public AssignmentHeader(){ }


    /**
     * The creation ID number is a 10-digit randomly assigned number which tracks the (instructor)
     * origin of the assignment.
     *
     * @return the ID number.
     */
    public String getCreationID() {
        return creationID;
    }

    /**
     * The creation ID number is a 10-digit randomly assigned number which tracks the (instructor)
     * origin of the assignment assignment.
     *
     * @param creationID the ID number.
     */
    public void setCreationID(String creationID) {
        this.creationID = creationID;
    }

    /**
     * The working ID is a 10-digit randomly assigned number set when a student first opens an assignment and
     * creates the header.
     *
     * @return the ID number.
     */
    public String getWorkingID() {
        return workingID;
    }


    /**
     * The working ID is a 10-digit randomly asigned number set when a student first opens an assignment and
     * creates the header.
     *
     * @param workingID the ID number
     */
    public void setWorkingID(String workingID) {
        this.workingID = workingID;
    }

    /**
     * The assignment name.  Also appears as file name.
     *
     * @return the assignment name
     */
    public String getAssignmentName() {
        return assignmentName;
    }

    /**
     * The assignment name.  Also appears as file name
     *
     * @param assignmentName the assignment name
     */
    public void setAssignmentName(String assignmentName) {
        this.assignmentName = assignmentName;
    }

    /**
     * Student name string
     *
     * @return student name
     */
    public String getStudentName() {
        return studentName;
    }

    /**
     * Student name string
     *
     * @param studentName student name
     */
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    /**
     * List of optional instructor header items
     *
     * @return instructor items list
     */
    public List<AssignmentHeaderItem> getInstructorItems() {
        return instructorItems;
    }

    /**
     * List of optional instructor header items
     *
     * @param instructorItems instructor items list
     */
    public void setInstructorItems(List<AssignmentHeaderItem> instructorItems) {
        this.instructorItems = instructorItems;
    }

    /**
     * List of optional student header items
     *
     * @return student items list
     */
    public List<AssignmentHeaderItem> getStudentItems() {
        return studentItems;
    }

    /**
     * List of optional student header items
     *
     * @param studentItems student items list
     */
    public void setStudentItems(List<AssignmentHeaderItem> studentItems) {
        this.studentItems = studentItems;
    }

    /**
     * Comment on assignment
     *
     * @return comment document
     */
    public Document getComment() {
        return comment;
    }

    /**
     * Comment on assignment
     *
     * @param comment comment document
     */
    public void setComment(Document comment) {
        this.comment = comment;
    }

    /**
     * Comment text height (not same as window height)
     *
     * @return text height value
     */
    public double getCommentTextHeight() {     return commentTextHeight;  }

    /**
     * Comment text height (not same as window height)
     *
     * @param commentTextHeight text height value
     */
    public void setCommentTextHeight(double commentTextHeight) {     this.commentTextHeight = commentTextHeight;  }
}
