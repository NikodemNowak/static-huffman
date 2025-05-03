package org.zespol;

import org.zespol.network.Communication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    // Stałe definiujące domyślne wartości
    private static final int DEFAULT_PORT = 12345; // Port używany do komunikacji
    private static final String DEFAULT_OUTPUT_FILE = "otrzymany_plik.txt"; // Domyślna nazwa pliku wynikowego dla serwera
    private static final String DEFAULT_INPUT_FILE = "src/main/java/org/zespol/data/file.txt"; // Domyślny plik wejściowy dla klienta

    public static void main(String[] args) {
        // Sprawdzenie, czy podano jakikolwiek argument (tryb pracy)
        if (args.length == 0) {
            printUsage(); // Wyświetlenie instrukcji użycia, jeśli brak argumentów
            return;
        }

        // Pierwszy argument określa tryb: "server" lub "client"
        String mode = args[0].toLowerCase();
        // Utworzenie obiektu obsługującego komunikację
        Communication communication = new Communication();

        // Wybór akcji w zależności od trybu
        switch (mode) {
            case "server":
                runServer(communication, args); // Uruchomienie logiki serwera
                break;
            case "client":
                runClient(communication, args); // Uruchomienie logiki klienta
                break;
            default:
                // Jeśli podano nieznany tryb
                System.err.println("Nieznany tryb: " + mode);
                printUsage(); // Wyświetlenie instrukcji
        }
    }

    /**
     * Uruchamia logikę serwera.
     * @param communication Instancja klasy Communication.
     * @param args Argumenty linii poleceń.
     */
    private static void runServer(Communication communication, String[] args) {
        // Użycie domyślnych wartości, które mogą zostać nadpisane przez argumenty
        int port = DEFAULT_PORT;
        String outputFile = DEFAULT_OUTPUT_FILE;

        // Proste parsowanie opcjonalnych argumentów dla serwera
        // Argument 1: Port (opcjonalny)
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]); // Spróbuj sparsować numer portu
            } catch (NumberFormatException e) {
                System.err.println("Nieprawidłowy numer portu dla serwera: '" + args[1] + "'. Używam domyślnego: " + DEFAULT_PORT);
            }
        }
        // Argument 2: Ścieżka pliku wyjściowego (opcjonalna)
        if (args.length > 2) {
            outputFile = args[2];
        }

        try {
            System.out.println("--- Tryb Serwera ---");
            System.out.println("Nasłuchiwanie na porcie: " + port);
            System.out.println("Zapis odkodowanych danych do: " + outputFile);
            // Wywołanie metody serwera z klasy Communication
            communication.receiveAndDecodeFile(port, outputFile);
            System.out.println("Serwer zakończył odbieranie i dekodowanie pliku.");
        } catch (IOException e) {
            // Obsługa błędów, które mogły wystąpić podczas działania serwera
            System.err.println("Wystąpił błąd podczas działania serwera: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Uruchamia logikę klienta.
     * @param communication Instancja klasy Communication.
     * @param args Argumenty linii poleceń.
     */
    private static void runClient(Communication communication, String[] args) {
        // Sprawdzenie, czy podano wystarczającą liczbę argumentów dla klienta
        if (args.length < 3) {
            System.err.println("Błąd: Brak wystarczających argumentów dla trybu klienta.");
            System.err.println("Oczekiwano: client <plik_wejściowy> <adres_serwera> [port]");
            printUsage();
            return;
        }

        // Argument 1: Ścieżka do pliku wejściowego
        String inputFile = args[1];
        // Argument 2: Adres IP lub nazwa hosta serwera
        String serverAddress = args[2];
        // Domyślny port, może zostać nadpisany
        int port = DEFAULT_PORT;

        // Sprawdzenie, czy plik wejściowy istnieje
        if (!Files.exists(Paths.get(inputFile))) {
             System.err.println("Błąd: Plik wejściowy '" + inputFile + "' nie istnieje.");
             return;
        }


        // Argument 3: Port (opcjonalny)
        if (args.length > 3) {
            try {
                port = Integer.parseInt(args[3]); // Spróbuj sparsować numer portu
            } catch (NumberFormatException e) {
                System.err.println("Nieprawidłowy numer portu dla klienta: '" + args[3] + "'. Używam domyślnego: " + DEFAULT_PORT);
            }
        }

        try {
            System.out.println("--- Tryb Klienta ---");
            System.out.println("Plik do wysłania: " + inputFile);
            System.out.println("Adres serwera: " + serverAddress + ":" + port);
            // Wywołanie metody klienta z klasy Communication
            communication.sendEncodedFile(inputFile, serverAddress, port);
            System.out.println("Klient zakończył wysyłanie pliku.");
        } catch (IOException e) {
            // Obsługa błędów, które mogły wystąpić podczas działania klienta
            System.err.println("Wystąpił błąd podczas działania klienta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Wyświetla instrukcję użycia programu.
     */
    private static void printUsage() {
        System.out.println("\nINSTRUKCJA UŻYCIA:");
        System.out.println("  Uruchomienie jako serwer:");
        System.out.println("    java -cp <ścieżka_do_jar_lub_klas> org.zespol.Main server [port] [plik_wyjściowy]");
        System.out.println("      [port] - opcjonalny numer portu (domyślnie: " + DEFAULT_PORT + ")");
        System.out.println("      [plik_wyjściowy] - opcjonalna nazwa pliku do zapisu (domyślnie: " + DEFAULT_OUTPUT_FILE + ")");
        System.out.println("\n  Uruchomienie jako klient:");
        System.out.println("    java -cp <ścieżka_do_jar_lub_klas> org.zespol.Main client <plik_wejściowy> <adres_serwera> [port]");
        System.out.println("      <plik_wejściowy> - wymagana ścieżka do pliku do wysłania");
        System.out.println("      <adres_serwera> - wymagany adres IP lub nazwa hosta serwera");
        System.out.println("      [port] - opcjonalny numer portu (domyślnie: " + DEFAULT_PORT + ")");
        System.out.println("\nPRZYKŁADY:");
        System.out.println("  # Uruchom serwer na domyślnym porcie, zapis do domyślnego pliku");
        System.out.println("  java -cp target/static-huffman-1.0-SNAPSHOT.jar org.zespol.Main server");
        System.out.println("\n  # Uruchom klienta, wyślij plik.txt do serwera na localhost na domyślnym porcie");
        System.out.println("  java -cp target/static-huffman-1.0-SNAPSHOT.jar org.zespol.Main client " + DEFAULT_INPUT_FILE + " 127.0.0.1");
        System.out.println("\n  # Uruchom serwer na porcie 5000, zapis do pliku 'odebrane.txt'");
        System.out.println("  java -cp target/static-huffman-1.0-SNAPSHOT.jar org.zespol.Main server 5000 odebrane.txt");
        System.out.println("\n  # Uruchom klienta, wyślij inny_plik.log do serwera 192.168.1.100 na porcie 5000");
        System.out.println("  java -cp target/static-huffman-1.0-SNAPSHOT.jar org.zespol.Main client sciezka/do/inny_plik.log 192.168.1.100 5000");
        System.out.println("\n(Pamiętaj o zastąpieniu <ścieżka_do_jar_lub_klas> odpowiednią wartością, np. 'target/static-huffman-1.0-SNAPSHOT.jar' po zbudowaniu projektu Mavenem)");
    }
}