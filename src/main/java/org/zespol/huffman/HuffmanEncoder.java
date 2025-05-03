package org.zespol.huffman;

import java.util.*;

public class HuffmanEncoder {
    private Map<Character, String> encodingMap;
    private Map<Character, Integer> frequencyMap;
    private HuffmanNode root;

    public Map<Character, Integer> getFrequencyMap() {
        return frequencyMap;
    }

    public HuffmanNode getRoot() {
        return root;
    }

    public HuffmanEncoder() {
        this.encodingMap = new HashMap<>();
    }

    public String encode(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        createFrequencyMap(message);
        buildTree(frequencyMap);
        createEncodingMap(root, "");

        StringBuilder encoded = new StringBuilder();
        for (char c : message.toCharArray()) {
            encoded.append(encodingMap.get(c));
        }
        return encoded.toString();
    }

    private void createFrequencyMap(String message) {
        Map<Character, Integer> frequencyMap = new HashMap<>();
        // Iterujemy po każdym znaku w wiadomości i aktualizujemy jego częstotliwość
        for (char c : message.toCharArray()) {
            // Używamy metody merge:
            // c to klucz,
            // 1 to wartość która będzie ustawiona dla nowego znaku lub połączona z istniejącą,
            // Integer::sum to funkcja łącząca, która mówiem, że jak taki znak już istnieje to dodaj do jego wartości 1
            frequencyMap.merge(c, 1, Integer::sum);
        }
        this.frequencyMap = frequencyMap;
    }

    private void buildTree(Map<Character, Integer> frequencyMap) {
        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>();

        // frequencyMap.entrySet() - zwraca zbiór par klucz-wartość
        // entry - to pojedyncza para klucz-wartość
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            // Tworzymy nowy węzeł Huffmana z kluczem i wartością
            HuffmanNode node = new HuffmanNode(entry.getKey(), entry.getValue());
            // Dodajemy węzeł do kolejki priorytetowej, priorytetowa kolejka sortuje węzły na podstawie ich częstotliwości, czyli wartości
            queue.offer(node);
        }

        while (queue.size() > 1) {
            // Pobieramy dwa węzły o najmniejszej częstotliwości
            HuffmanNode left = queue.poll();
            HuffmanNode right = queue.poll();

            // Tworzymy nowy węzeł, który będzie rodzicem tych dwóch węzłów
            // Jego znak to '\0' (brak znaku), a częstotliwość to suma częstotliwości obu węzłów
            HuffmanNode parent = new HuffmanNode('\0', left.getFrequency() + right.getFrequency());
            // Ustawiamy lewego i prawego syna dla rodzica
            parent.setLeft(left);
            parent.setRight(right);

            // Dodajemy rodzica do kolejki
            queue.offer(parent);
        }

        // Na końcu w kolejce zostaje tylko jeden węzeł, który jest korzeniem drzewa Huffmana
        root = queue.poll();
    }

    // Rekurencyjna metoda do tworzenia mapy kodowania, czyli przypisania kodów do symboli np A -> 00, B -> 01 itd.
    // Zaczynamy od korzenia i pustego kodu
    private void createEncodingMap(HuffmanNode node, String code) {
        if (node == null) {
            return;
        }

        // Jeśli węzeł jest liściem - brak left i right
        if (node.isLeaf()) {
            // Przypisujemy kod do symbolu
            encodingMap.put(node.getSymbol(), code);
            return;
        }

        createEncodingMap(node.getLeft(), code + "0");
        createEncodingMap(node.getRight(), code + "1");
    }
}
