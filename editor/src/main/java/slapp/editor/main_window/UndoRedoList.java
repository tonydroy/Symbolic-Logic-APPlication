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

import java.util.LinkedList;

/**
 * Linked list to support undo/redo operations.  Works by pushing and popping "snapshots" of exercise.
 *
 * @param <T> The type of items included in the list (usually exercise models)
 */
public class UndoRedoList<T> extends LinkedList<T> {

    /*
    The currentIndex is a pointer to the current snapshot.  Undo increments the pointer down the list,
    redo decrements it back up.  Push cleans off items up to the pointer, and places a new item on the top.  If a
    push increases size beyond the maximum, the bottom element is removed.
     */
    private int currentIndex = 0;
    private int maxSize;


    /**
     * Constructor for list with parameter for maximum size.
     *
     * @param size the maximum size of the list
     */
    public UndoRedoList(int size) {
        super();
        this.maxSize = size;
    }

    /**
     * Push element onto list.
     *
     * @param element the element to push
     */
    @Override
    public void push(T element) {
        for (int i = 0; i < currentIndex; i++) {
            this.removeFirst();
        }
        currentIndex = 0;
        this.addFirst(element);

        while (this.size() >= maxSize) {
            this.removeLast();
        }
      //  System.out.println("push: " + this + " " + currentIndex);
    }

    /**
     * Undo element is next element down the list
     *
     * @return undo element
     */
    public T getUndoElement() {
        T element = null;
        if (currentIndex + 1 < this.size()) {
            currentIndex++;
            element = this.get(currentIndex);
        }
    //    System.out.println("undo: " + element + " " + currentIndex);

        return element;
    }

    /**
     * Redo element is previous item in the list
     *
     * @return redo element
     */
    public T getRedoElement() {
        T element = null;
        if (currentIndex > 0) {
            currentIndex--;
            element = this.get(currentIndex);
        }
 //       System.out.println("redo: " + element + " " + currentIndex);
        return element;
    }

    /**
     * canRedo if there is a previous item in the list
     *
     * @return true if canRedo and otherwise false
     */
    public boolean canRedo() {
        boolean canRedo = true;
        if (currentIndex <= 0) canRedo = false;
        return canRedo;
    }

    /**
     * canUndo if there is a next item in the list
     *
     * @return true if canUndo and otherwise false
     */
    public boolean canUndo() {
        boolean canUndo = false;
        if (currentIndex + 1 < this.size()) canUndo = true;
        return canUndo;
    }

}
