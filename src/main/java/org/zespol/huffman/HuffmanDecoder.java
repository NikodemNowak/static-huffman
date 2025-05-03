package org.zespol.huffman;

public class HuffmanDecoder {
    private final HuffmanNode root;

    public HuffmanDecoder(HuffmanNode root) {
        this.root = root;
    }

    public String decode(String encodedMessage) {
        if (encodedMessage == null || encodedMessage.isEmpty()) {
            return "";
        }

        StringBuilder decoded = new StringBuilder();
        HuffmanNode current = root;

        for (char bit : encodedMessage.toCharArray()) {
            if (bit == '0') {
                current = current.getLeft();
            } else if (bit == '1') {
                current = current.getRight();
            }

            if (current.isLeaf()) {
                decoded.append(current.getSymbol());
                current = root;
            }
        }

        return decoded.toString();
    }
}
