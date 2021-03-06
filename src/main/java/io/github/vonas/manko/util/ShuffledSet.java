package io.github.vonas.manko.util;

import io.github.vonas.manko.util.exceptions.EmptySetException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

public class ShuffledSet<E> extends BaseSet<E> implements Serializable {

    /**
     * Keeps track if an element that was previously added
     * to the elementList is not part of the set anymore.
     */
    private final HashSet<E> removedElements = new HashSet<>();

    /**
     * Elements are added to this set for the sake of random access.
     * When an element is removed it is not removed immediately for performance reasons.
     * Before using an element from this list, check if it's not in removedElements.
     */
    private final ArrayList<E> elementList = new ArrayList<>();

    private transient Random random;

    public ShuffledSet() {
        super(new HashSet<>());
        initTransient();
    }

    public ShuffledSet(Set<E> elements) {
        this();
        this.elements.addAll(elements);
        elementList.addAll(elements);
    }

    @Override
    public boolean add(E element) {
        if (contains(element))
            return false;

        if (!removedElements.contains(element))
            elementList.add(element);

        removedElements.remove(element);
        return super.add(element);
    }

    @Override
    public boolean remove(E element) {
        if (!contains(element))
            return false;

        super.remove(element);
        removedElements.add(element);
        return true;
    }

    public E removeRandom() throws EmptySetException {

        while (!elementList.isEmpty()) {
            E popped = popRandomListElement();
            if (!removedElements.contains(popped)) {
                super.remove(popped);
                return popped;
            }
        }

        throw new EmptySetException();
    }

    @Override
    public void clear() {
        super.clear();
        elementList.clear();
        removedElements.clear();
    }

    private E popRandomListElement() {
        int index = random.nextInt(elementList.size());
        int lastIndex = elementList.size() - 1;

        E declared = elementList.get(index);
        elementList.set(index, elementList.get(lastIndex));
        elementList.remove(lastIndex);

        return declared;
    }

    private void initTransient() {
        random = new Random();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        initTransient();
    }
}
