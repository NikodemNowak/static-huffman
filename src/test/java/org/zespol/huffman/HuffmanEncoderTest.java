package org.zespol.huffman;

import org.junit.Test;

import static org.junit.Assert.*;

public class HuffmanEncoderTest {
    @Test
    public void encode() {
        HuffmanEncoder encoder = new HuffmanEncoder();
        String encoded = encoder.encode("Hello World!");
        assertEquals("0110001101011111000101110111100001101", encoded);
        System.out.println(encoded);
    }
}
