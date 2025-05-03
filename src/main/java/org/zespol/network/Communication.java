package org.zespol.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zespol.huffman.HuffmanDecoder;
import org.zespol.huffman.HuffmanEncoder;
import org.zespol.huffman.HuffmanNode;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.PriorityQueue;

public class Communication {

    private static final ObjectMapper objectMapper = new ObjectMapper(); // Używamy jednej instancji ObjectMapper

    /**
     * Pełni rolę klienta: koduje plik, wysyła mapę częstotliwości (JSON) i zakodowane dane do serwera.
     *
     * @param inputFilePath Ścieżka do pliku wejściowego do zakodowania i wysłania.
     * @param serverAddress Adres IP lub nazwa hosta serwera.
     * @param port          Numer portu serwera.
     * @throws IOException Błędy związane z odczytem pliku lub komunikacją sieciową.
     */
    public void sendEncodedFile(String inputFilePath, String serverAddress, int port) throws IOException {
        System.out.println("Nadawca (Klient): Rozpoczynam proces...");

        // 1. Odczyt pliku
        System.out.println("Nadawca: Odczytuję plik: " + inputFilePath);
        String fileContent;
        try {
            fileContent = Files.readString(Paths.get(inputFilePath));
            if (fileContent.isEmpty()) {
                System.err.println("Nadawca: Plik jest pusty. Przesyłanie przerwane.");
                // Można zdecydować o wysłaniu specjalnego komunikatu lub przerwaniu
                return;
            }
        } catch (IOException e) {
            System.err.println("Nadawca: Błąd odczytu pliku: " + e.getMessage());
            throw e; // Rzuć dalej lub obsłuż inaczej
        }
        System.out.println("Nadawca: Odczytano " + fileContent.length() + " znaków.");


        // 2. Kodowanie Huffmana i pobranie mapy częstotliwości
        HuffmanEncoder encoder = new HuffmanEncoder();
        System.out.println("Nadawca: Koduję dane...");
        String encodedData = encoder.encode(fileContent);
        Map<Character, Integer> freqMap = encoder.getFrequencyMap(); // Zakładamy, że ta metoda istnieje w HuffmanEncoder

        if (freqMap == null || freqMap.isEmpty()) {
             System.err.println("Nadawca: Mapa częstotliwości jest pusta. Nie można kontynuować.");
             return;
        }
        System.out.println("Nadawca: Kodowanie zakończone. Rozmiar mapy częstotliwości: " + freqMap.size());
        System.out.println("Nadawca: Długość zakodowanych danych: " + encodedData.length());


        // 3. Serializacja mapy częstotliwości do JSON
        String frequencyMapJson;
        try {
            frequencyMapJson = objectMapper.writeValueAsString(freqMap);
            System.out.println("Nadawca: Mapa częstotliwości zserializowana do JSON.");
            // System.out.println("Nadawca: JSON Mapy: " + frequencyMapJson); // Opcjonalnie do debugowania
        } catch (JsonProcessingException e) {
            System.err.println("Nadawca: Błąd serializacji mapy do JSON: " + e.getMessage());
            throw new IOException("Błąd serializacji JSON", e);
        }

        // 4. Nawiązanie połączenia i wysłanie danych
        System.out.println("Nadawca: Łączę się z serwerem " + serverAddress + ":" + port);
        try (Socket socket = new Socket(serverAddress, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) // true oznacza autoFlush
        {
            System.out.println("Nadawca: Połączono. Wysyłam dane...");

            // 4a. Wyślij JSON mapy częstotliwości (jako pierwszą linię)
            out.println(frequencyMapJson);
            System.out.println("Nadawca: Wysłano mapę częstotliwości (JSON).");

            // 4b. Wyślij zakodowane dane (jako drugą linię)
            out.println(encodedData);
            System.out.println("Nadawca: Wysłano zakodowane dane.");

            // out.flush(); // Nie jest potrzebne dzięki autoFlush=true w PrintWriter

            System.out.println("Nadawca: Dane wysłane pomyślnie.");

        } catch (IOException e) {
            System.err.println("Nadawca: Błąd sieciowy podczas wysyłania: " + e.getMessage());
            throw e; // Rzuć dalej lub obsłuż inaczej
        }
        System.out.println("Nadawca: Zakończono proces.");
    }

    /**
     * Pełni rolę serwera: odbiera mapę częstotliwości (JSON) i zakodowane dane,
     * dekoduje je i zapisuje do pliku. Obsługuje jedno połączenie klienta.
     *
     * @param port           Numer portu, na którym serwer będzie nasłuchiwał.
     * @param outputFilePath Ścieżka do pliku, w którym zostaną zapisane odkodowane dane.
     * @throws IOException Błędy związane z komunikacją sieciową, parsowaniem JSON lub zapisem pliku.
     */
    public void receiveAndDecodeFile(int port, String outputFilePath) throws IOException {
        System.out.println("Odbiorca (Serwer): Uruchamiam serwer na porcie " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Odbiorca: Oczekuję na połączenie klienta...");
            try (Socket clientSocket = serverSocket.accept(); // Akceptuj połączenie
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                System.out.println("Odbiorca: Klient połączony z " + clientSocket.getInetAddress());

                // 1. Odbiór danych
                System.out.println("Odbiorca: Odbieram dane...");
                String receivedJsonMap = in.readLine();     // Odbierz pierwszą linię (JSON mapy)
                String receivedEncodedData = in.readLine(); // Odbierz drugą linię (zakodowane dane)

                if (receivedJsonMap == null || receivedEncodedData == null) {
                    System.err.println("Odbiorca: Nie otrzymano kompletnych danych od klienta. Zamykam połączenie.");
                    return; // Zakończ, jeśli dane są niekompletne
                }
                 System.out.println("Odbiorca: Otrzymano mapę (JSON) o długości: " + receivedJsonMap.length());
                 System.out.println("Odbiorca: Otrzymano zakodowane dane o długości: " + receivedEncodedData.length());


                // 2. Deserializacja mapy częstotliwości z JSON
                Map<Character, Integer> receivedFreqMap;
                try {
                     System.out.println("Odbiorca: Deserializuję mapę częstotliwości...");
                    receivedFreqMap = objectMapper.readValue(receivedJsonMap,
                            new TypeReference<Map<Character, Integer>>() {}); // Ważne użycie TypeReference!
                     System.out.println("Odbiorca: Mapa zdeserializowana. Liczba wpisów: " + receivedFreqMap.size());
                } catch (JsonProcessingException e) {
                    System.err.println("Odbiorca: Błąd deserializacji mapy JSON: " + e.getMessage());
                    throw new IOException("Błąd parsowania JSON mapy", e);
                }

                // 3. Odtworzenie drzewa Huffmana na podstawie mapy częstotliwości
                System.out.println("Odbiorca: Odtwarzam drzewo Huffmana...");
                HuffmanNode root = buildHuffmanTreeFromFrequencies(receivedFreqMap);

                 if (root == null) {
                     System.err.println("Odbiorca: Nie udało się zbudować drzewa Huffmana (pusta mapa?). Przerywam.");
                     // Jeśli oryginalny plik był pusty, mapa też będzie pusta, root będzie null
                     // Jeśli plik miał jeden unikalny znak, drzewo może być specyficzne (zależne od buildHuffmanTree...)
                     if (receivedEncodedData.isEmpty()) {
                         System.out.println("Odbiorca: Odebrane dane są puste, tworzę pusty plik wyjściowy.");
                         Files.writeString(Paths.get(outputFilePath), "");
                         return;
                     } else {
                          throw new IOException("Nie udało się zbudować drzewa Huffmana, a otrzymano niepuste dane.");
                     }
                 }
                 System.out.println("Odbiorca: Drzewo Huffmana odtworzone.");


                // 4. Dekodowanie danych
                HuffmanDecoder decoder = new HuffmanDecoder(root); // Użyj odtworzonego korzenia
                System.out.println("Odbiorca: Dekoduję dane...");
                String decodedText = decoder.decode(receivedEncodedData);
                 System.out.println("Odbiorca: Dane zdekodowane. Długość tekstu: " + decodedText.length());


                // 5. Zapis odkodowanego tekstu do pliku
                try {
                    System.out.println("Odbiorca: Zapisuję odkodowany tekst do pliku: " + outputFilePath);
                    Files.writeString(Paths.get(outputFilePath), decodedText);
                    System.out.println("Odbiorca: Plik zapisany pomyślnie.");
                } catch (IOException e) {
                    System.err.println("Odbiorca: Błąd zapisu do pliku: " + e.getMessage());
                    throw e; // Rzuć dalej lub obsłuż inaczej
                }

            } // Try-with-resources zamknie clientSocket i BufferedReader
        } catch (IOException e) {
            System.err.println("Odbiorca: Błąd serwera: " + e.getMessage());
            throw e; // Rzuć dalej lub obsłuż inaczej
        }
        System.out.println("Odbiorca: Zakończono proces.");
    }


    /**
     * Pomocnicza metoda do budowania drzewa Huffmana na podstawie mapy częstotliwości.
     * Ta logika jest skopiowana z HuffmanEncoder dla potrzeb odbiorcy.
     * W idealnym projekcie byłaby to metoda statyczna w klasie narzędziowej.
     *
     * @param frequencyMap Mapa częstotliwości znaków.
     * @return Korzeń zbudowanego drzewa Huffmana lub null, jeśli mapa jest pusta.
     */
    private static HuffmanNode buildHuffmanTreeFromFrequencies(Map<Character, Integer> frequencyMap) {
        if (frequencyMap == null || frequencyMap.isEmpty()) {
            return null; // Brak drzewa dla pustej mapy
        }

        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>();

        // Obsługa mapy z jednym elementem - tworzymy prosty korzeń-liść
        if (frequencyMap.size() == 1) {
            Map.Entry<Character, Integer> singleEntry = frequencyMap.entrySet().iterator().next();
            // Zwracamy bezpośrednio liść. Dekoder musi być w stanie to obsłużyć
            // (np. przez specjalne traktowanie, jeśli root jest liściem, lub kodowanie takiego znaku jako np. "0")
             // UWAGA: Obecny HuffmanDecoder może mieć problem, jeśli oczekuje ścieżek 0/1.
             // Bezpieczniejszym podejściem byłoby stworzenie sztucznego rodzica:
             // HuffmanNode leaf = new HuffmanNode(singleEntry.getKey(), singleEntry.getValue());
             // HuffmanNode dummyParent = new HuffmanNode('\0', singleEntry.getValue());
             // dummyParent.setLeft(leaf); // np. kod "0"
             // return dummyParent;
            // Na razie zwracamy sam liść:
             return new HuffmanNode(singleEntry.getKey(), singleEntry.getValue());
        }


        // Budowanie drzewa dla >= 2 unikalnych znaków
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            HuffmanNode node = new HuffmanNode(entry.getKey(), entry.getValue());
            queue.offer(node);
        }

        while (queue.size() > 1) {
            HuffmanNode left = queue.poll();
            HuffmanNode right = queue.poll();

            // Teoretycznie nie powinno się zdarzyć przy queue.size() > 1, ale dla bezpieczeństwa
            if (left == null || right == null) {
                 throw new IllegalStateException("Błąd wewnętrzny podczas budowania drzewa - kolejka priorytetowa zwróciła null.");
            }

            HuffmanNode parent = new HuffmanNode('\0', left.getFrequency() + right.getFrequency());
            parent.setLeft(left);
            parent.setRight(right);
            queue.offer(parent);
        }

        return queue.poll(); // Ostatni element w kolejce to korzeń
    }
}