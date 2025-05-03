package org.zespol.huffman;

import org.junit.Test;

import static org.junit.Assert.*;

public class HuffmanTest {
    @Test
    public void encodeAndDecode() {
        String originalMessage = "Hello World!";

        // 1. Kodowanie i uzyskanie potrzebnych danych
        HuffmanEncoder encoder = new HuffmanEncoder();
        String encodedMessage = encoder.encode(originalMessage); // Kodujemy
        HuffmanNode root = encoder.getRoot(); // Pobieramy korzeń drzewa

        String expectedEncoded = "0110001101011111000101110111100001101";
        assertEquals(expectedEncoded, encodedMessage);
        System.out.println("Encoded: " + encodedMessage);

        // 2. Dekodowanie przy użyciu prawidłowego korzenia
        assertNotNull("Root node should not be null after encoding", root); // Dodatkowe sprawdzenie
        HuffmanDecoder decoder = new HuffmanDecoder(root); // Używamy korzenia z encodera
        String decodedMessage = decoder.decode(encodedMessage);

        // 3. Weryfikacja
        assertEquals(originalMessage, decodedMessage);
        System.out.println("Decoded: " + decodedMessage);
    }

}
