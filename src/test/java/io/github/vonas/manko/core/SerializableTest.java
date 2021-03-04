package io.github.vonas.manko.core;

import io.github.vonas.manko.fabric.Player;
import io.github.vonas.manko.util.TestEntrant;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializableTest {

    // TODO: use UUIDs with a specific seed instead of randomizing and maintaining a Set
    private final Player player1 = new Player(UUID.randomUUID(), "Player1");
    private final Player player2 = new Player(UUID.randomUUID(), "Player2");

    @Test
    void playerSerializable() {
        assertSerializable(player1, Player.class);
    }

    @Test
    void pairingSerializable() {
        assertSerializable(new Pairing(player1, player2), Pairing.class);
    }

    @Test
    void dynamicRoundSerializable() {
        DynamicRound round = new DynamicRound();
        round.addEntrant(player1);
        round.addEntrant(player2);
        round.addEntrant(TestEntrant.createUnique());
        round.addEntrant(TestEntrant.createUnique());

        assertDoesNotThrow(() -> {
            Pairing pairing = round.nextPairing();
            round.declareWinner(pairing.getEntrant1());
        });

        assertSerializable(round, DynamicRound.class);
    }

    private <T extends Serializable> void assertSerializable(T o, Class<T> type) {

        try {
            assertEquals(o, serializeDeserialize(o, type));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private <T extends Serializable> T serializeDeserialize(T o, Class<T> type)
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
