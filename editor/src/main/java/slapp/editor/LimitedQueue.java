package slapp.editor;

import java.io.Serializable;
import java.util.LinkedList;

public class LimitedQueue<E> extends LinkedList<E> implements Serializable {

    private int limit;

    public LimitedQueue(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean add(E element) {
        super.add(element); // Add the element
        while (size() > limit) {
            super.remove(); // Remove the oldest element if the limit is exceeded
        }
        return true;
    }

}
