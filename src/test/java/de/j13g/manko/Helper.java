package de.j13g.manko;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Helper {

    /**
     * Asserts that the supplier supplies all expected elements.
     * The supplier is called repeatedly until a certain threshold exceeds
     * @param expectedElements The expected elements.
     * @param supplier Supplier that should supply all expected elements.
     * @param iterations The number of iterations.
     * @param <T> The element type.
     */
    public static <T> void assertSuppliesAll(Collection<T> expectedElements, Supplier<T> supplier, int iterations) {
        HashSet<T> encountered = new HashSet<>();

        for (int i = 0; i <= iterations; ++i) {
            encountered.add(supplier.get());
            if (encountered.equals(expectedElements))
                return; // Encountered all expected elements.
            if (encountered.size() > expectedElements.size())
                break; // Got more than the expected elements.
        }

        assert false;
    }

    /**
     * @see Helper#assertSuppliesAll(Collection, Supplier, int)
     */
    public static <T> void assertSuppliesAll(Collection<T> expectedElements, Supplier<T> supplier) {
        int iterations = expectedElements.size() * 100;
        assertSuppliesAll(expectedElements, supplier, iterations);
    }

    public static <T extends Serializable> void assertSerializable(T o, Class<T> type) {

        try {
            T result = serializeDeserialize(o, type);
            assertEquals(o, result);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static <T extends Serializable> T serializeDeserialize(T o, Class<T> type)
        throws IOException, ClassNotFoundException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outputStream);
        out.writeObject(o);
        out.close();
        byte[] bytes = outputStream.toByteArray();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(inputStream);
        Object d = in.readObject();
        in.close();

        if (!type.isInstance(d))
            throw new RuntimeException();

        return type.cast(d);
    }
}
