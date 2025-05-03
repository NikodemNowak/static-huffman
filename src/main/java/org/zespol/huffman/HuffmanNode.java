package org.zespol.huffman;

public class HuffmanNode implements Comparable<HuffmanNode>{
    private char symbol;
    private int frequency;
    private HuffmanNode left;
    private HuffmanNode right;

    public HuffmanNode(char c, int i) {
        this.symbol = c;
        this.frequency = i;
    }

    @Override
    public int compareTo(HuffmanNode o) {
        return Integer.compare(this.frequency, o.frequency);
    }

    public char getSymbol() {
        return symbol;
    }

    public int getFrequency() {
        return frequency;
    }

    public HuffmanNode getLeft() {
        return left;
    }

    public HuffmanNode getRight() {
        return right;
    }

    public void setLeft(HuffmanNode left) {
        this.left = left;
    }

    public void setRight(HuffmanNode right) {
        this.right = right;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }
}
