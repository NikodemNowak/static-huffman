package org.zespol.huffman;

import org.junit.Test;

public class HuffmanEncoderTest {
    @Test
    public void encode() {
        HuffmanEncoder encoder = new HuffmanEncoder();
        String encoded = encoder.encode("Hello World!");
        System.out.println(encoded);
    }
}
