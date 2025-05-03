package org.zespol.huffman;

import java.util.*;

public class HuffmanEncoder {
    private Map<Character, String> encodingMap;
    private HuffmanNode root;

    public HuffmanEncoder() {
        this.encodingMap = new HashMap<>();
    }

    public String encode(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        Map<Character, Integer> frequencyMap = createFrequencyMap(message);
        buildTree(frequencyMap);
        createEncodingMap(root, "");

        StringBuilder encoded = new StringBuilder();
        for (char c : message.toCharArray()) {
            encoded.append(encodingMap.get(c));
        }
        return encoded.toString();
    }

    private Map<Character, Integer> createFrequencyMap(String message) {
        Map<Character, Integer> frequencyMap = new HashMap<>();
        for (char c : message.toCharArray()) {
            frequencyMap.merge(c, 1, Integer::sum);
        }
        return frequencyMap;
    }

    private void buildTree(Map<Character, Integer> frequencyMap) {
        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>();

        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            HuffmanNode node = new HuffmanNode(entry.getKey(), entry.getValue());
            queue.offer(node);
        }

        while (queue.size() > 1) {
            HuffmanNode left = queue.poll();
            HuffmanNode right = queue.poll();
            HuffmanNode parent = new HuffmanNode('\0', left.getFrequency() + right.getFrequency());
            parent.setLeft(left);
            parent.setRight(right);
            queue.offer(parent);
        }

        root = queue.poll();
    }

    private void createEncodingMap(HuffmanNode node, String code) {
        if (node == null) {
            return;
        }

        if (node.isLeaf()) {
            encodingMap.put(node.getSymbol(), code);
            return;
        }

        createEncodingMap(node.getLeft(), code + "0");
        createEncodingMap(node.getRight(), code + "1");
    }
}
