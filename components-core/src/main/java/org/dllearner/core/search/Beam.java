package org.dllearner.core.search;

import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * A priority queue implementation with a fixed size based on a {@link PriorityQueue}.
 * The number of elements in the queue will be at most {@code maxSize}.
 * Once the number of elements in the queue reaches {@code maxSize}, trying to add a new element
 * will remove the greatest element in the queue if the new element is less than or equal to
 * the current greatest element. The queue will not be modified otherwise.
 */
public class Beam<E> extends PriorityQueue<E> {
    //        private final PriorityQueue<E> priorityQueue; /* backing data structure */
    private final Comparator<? super E> comparator;
    private final int maxSize;

    /**
     * Constructs a {@link Beam} with the specified {@code maxSize}
     * and {@code comparator}.
     *
     * @param maxSize    - The maximum size the queue can reach, must be a positive integer.
     * @param comparator - The comparator to be used to compare the elements in the queue, must be non-null.
     */
    public Beam(final int maxSize, final Comparator<? super E> comparator) {
        super(comparator);
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize = " + maxSize + "; expected a positive integer.");
        }
        Objects.requireNonNull(comparator, "Comparator is null.");
//            this.priorityQueue = new PriorityQueue<>(comparator);
        this.comparator = comparator;
        this.maxSize = maxSize;
    }

    /**
     * Adds an element to the queue. If the queue contains {@code maxSize} elements, {@code e} will
     * be compared to the lowest element in the queue using {@code comparator}.
     * If {@code e} is greater than or equal to the lowest element, that element will be removed and
     * {@code e} will be added instead. Otherwise, the queue will not be modified
     * and {@code e} will not be added.
     *
     * @param e - Element to be added, must be non-null.
     */
    @Override
    public boolean add(final E e) {
        Objects.requireNonNull(e, "e is null.");
        if (maxSize <= super.size()) {
            final E firstElm = super.peek();
            if (comparator.compare(e, firstElm) < 1) {
                return false;
            } else {
                super.poll();
            }
        }
        return super.add(e);
    }

}