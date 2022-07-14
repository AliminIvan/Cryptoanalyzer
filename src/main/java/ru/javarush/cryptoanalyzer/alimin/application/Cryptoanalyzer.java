package ru.javarush.cryptoanalyzer.alimin.application;

import ru.javarush.cryptoanalyzer.alimin.constants.Alphabet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.nio.file.StandardOpenOption.APPEND;

public class Cryptoanalyzer {
    private final List<Path> paths = new ArrayList<>();

    public void encrypt(Path path, int key) {
        key = keyVerification(key);
        try {
            Path encryptedFile = createNewPath(path, true);
            Files.createFile(encryptedFile);
            for (String line : readLinesFromFile(path)) {
                String encryptedString = toCipher(line, key);
                Files.writeString(encryptedFile, encryptedString + "\n", APPEND);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void decrypt(Path path, int key) {
        key = keyVerification(key);
        try {
            Path decryptedFile = createNewPath(path, false);
            Files.createFile(decryptedFile);
            for (String line : readLinesFromFile(path)) {
                String decryptedString = toDecipher(line, key);
                System.out.println(decryptedString);
                Files.writeString(decryptedFile, decryptedString + "\n", APPEND);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void bruteForceHack(Path path) {
        List<String> encryptedLines = readLinesFromFile(path);
        Set<String> decryptedWords;
        Map<Integer, Integer> possibleKeysCounter = new HashMap<>();
        int counter = 0;
        int key = 0;
        for (int i = 1; i < Alphabet.ALPHABET_LENGTH; i++) {
            decryptedWords = getDecryptedWords(encryptedLines, i);
            for (int j = 0; j < Alphabet.VOCABULARY.length; j++) {
                if (decryptedWords.contains(Alphabet.VOCABULARY[j])) {
                    counter++;
                }
            }
            possibleKeysCounter.put(i, counter);
            counter = 0;
        }

        Map.Entry<Integer, Integer> maxEntry = null;
        for (Map.Entry<Integer, Integer> entry : possibleKeysCounter.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }
        if (maxEntry != null) {
            key = maxEntry.getKey();
        }
        decrypt(path, key);
        System.out.println("*".repeat(100));
        System.out.println("Ключ к зашифрованному файлу: " + key);
    }

    public void frequencyHack(Path path) {
        int key = 0;
        Map.Entry<Character, Integer> entry = maxRepeatedChar(path);
        char mostRepeatedCharInFile = entry.getKey();
        List<String> encryptedLines = readLinesFromFile(path);
        Set<String> decryptedWords;
        for (int i = 0; i < Alphabet.MOST_POPULAR_CHARS.length; i++) {
            char mostPopularChar = Alphabet.MOST_POPULAR_CHARS[i];
            if (mostRepeatedCharInFile > mostPopularChar) {
                key = Math.abs(Alphabet.LOWERCASE_LETTERS.indexOf(mostPopularChar) - Alphabet.LOWERCASE_LETTERS.indexOf(mostRepeatedCharInFile));
            } else {
                key = Math.abs(Alphabet.LOWERCASE_LETTERS.indexOf(mostPopularChar) - Alphabet.LOWERCASE_LETTERS.indexOf('я') -
                        Alphabet.LOWERCASE_LETTERS.indexOf(mostRepeatedCharInFile) - 1);
            }
            decryptedWords = getDecryptedWords(encryptedLines, key);
            if (fileIsDecrypted(decryptedWords)) {
                break;
            }
        }
        decrypt(path, key);
        System.out.println("*".repeat(100));
        System.out.println("Ключ к зашифрованному файлу: " + key);
    }

    public void statisticalAnalysisHack(Path textExample, Path encryptedFile) {
        Path decryptedFile = createNewPath(encryptedFile, false);
        try {
            Files.createFile(decryptedFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<String> exampleLines = readLinesFromFile(textExample);
        List<String> encryptedLines = readLinesFromFile(encryptedFile);
        Map<Character, Integer> exampleLettersRepeatCount = countLettersRepeats(exampleLines);
        Map<Character, Integer> encryptedLettersRepeatCount = countLettersRepeats(encryptedLines);
        Map<Character, Character> decoder = new HashMap<>();
        int exampleLettersQuantity = countAllLettersInFile(exampleLettersRepeatCount);
        int encryptedLettersQuantity = countAllLettersInFile(encryptedLettersRepeatCount);

        Map<Double, Character> exampleLettersRepeatPercent = getLettersRepeatPercent(exampleLettersRepeatCount, exampleLettersQuantity);
        Map<Double, Character> encryptedLettersRepeatPercent = getLettersRepeatPercent(encryptedLettersRepeatCount, encryptedLettersQuantity);
        List<Character> values = new ArrayList<>(encryptedLettersRepeatPercent.values());

        char key;
        char value;
        int i = 0;
        for (Map.Entry<Double, Character> entry : exampleLettersRepeatPercent.entrySet()) {
            key = values.get(i);
            value = entry.getValue();
            decoder.put(key, value);
            i++;
        }

        StringBuilder decryptedLine = new StringBuilder();
        String encryptedLine;
        for (String line : encryptedLines) {
            encryptedLine = line;
            for (int k = 0; k < encryptedLine.length(); k++) {
                char letter = encryptedLine.charAt(k);
                if (decoder.containsKey(Character.toLowerCase(letter))) {
                    if (Character.isLowerCase(letter)) {
                        decryptedLine.append(decoder.get(letter));
                    } else {
                        decryptedLine.append(Character.toUpperCase(decoder.get(Character.toLowerCase(letter))));
                    }
                } else {
                    decryptedLine.append(letter);
                }
            }
            System.out.println(decryptedLine);
            try {
                Files.writeString(decryptedFile, decryptedLine + "\n", APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            decryptedLine.delete(0, decryptedLine.length());
        }
    }

    public void printAllPossibleOptions(Path path) {
        for (int i = 0; i < Alphabet.ALPHABET_LENGTH; i++) {
            System.out.println("Result for key " + i + ":");
            decrypt(path, i);
            System.out.println("*".repeat(100));
        }
    }



    //методы-утилиты

    private Map<Double, Character> getLettersRepeatPercent(Map<Character, Integer> map, int allLettersCount) {
        Map<Double, Character> fileLettersRepeatPercent = new TreeMap<>(Collections.reverseOrder());
        double key;
        char value;
        for (Map.Entry<Character, Integer> entry : map.entrySet()) {
            key = ((double) entry.getValue() / (double) allLettersCount) * 100.0;
            value = entry.getKey();
            fileLettersRepeatPercent.put(key, value);
        }
        return fileLettersRepeatPercent;
    }

    private int countAllLettersInFile(Map<Character, Integer> map) {
        int lettersCount = 0;
        for (Map.Entry<Character, Integer> entry : map.entrySet()) {
            lettersCount += entry.getValue();
        }
        return lettersCount;
    }

    private Set<String> getDecryptedWords(List<String> encryptedLines, int key) {
        Set<String> decryptedWords = new HashSet<>();
        for (String encryptedLine : encryptedLines) {
            String line = toDecipher(encryptedLine, key).replaceAll("[^A-Za-zА-Яа-я ]", " ");
            String[] words = line.split(" ");
            for (String word : words) {
                decryptedWords.add(word.toLowerCase());
            }
        }
        return decryptedWords;
    }

    private boolean fileIsDecrypted(Set<String> decryptedWords) {
        boolean result = false;
        for (int i = 0; i < Alphabet.VOCABULARY.length; i++) {
            if (decryptedWords.contains(Alphabet.VOCABULARY[i])) {
                result = true;
                break;
            }
        }
        return result;
    }

    private int keyVerification(int key) {
        if (key < 0) {
            throw new IllegalArgumentException("Key can`t be < 0");
        }
        if (key > Alphabet.ALPHABET_LENGTH) {
            key = key % Alphabet.ALPHABET_LENGTH;
        }
        return key;
    }

    private Path createNewPath(Path path, boolean encrypting) {
        String cipherMode;
        Path lastPart;
        if (encrypting) {
            cipherMode = "Encrypted";
            lastPart = Path.of(cipherMode + path.getFileName());
        } else {
            cipherMode = "Decrypted";
            if (path.getFileName().toString().contains("Encrypted")) {
                lastPart = Path.of(path.getFileName().toString().replace("Encrypted", cipherMode));
            } else {
                lastPart = Path.of(cipherMode + path.getFileName());
            }
        }
        Path newPath = path.getParent().resolve(lastPart);
        if (paths.contains(newPath)) {
            int count = 1;
            while (paths.contains(newPath)) {
                Path uniquePath = Path.of(cipherMode + count + path.getFileName().toString().replace("Encrypted", ""));
                count++;
                newPath = path.getParent().resolve(uniquePath);
            }
        }
        paths.add(newPath);
        return newPath;
    }

    private List<String> readLinesFromFile(Path path) {
        try {
            return new ArrayList<>(Files.readAllLines(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String toCipher(String line, int key) {
        int punctuationKey = key;
        if (key == Alphabet.ALPHABET_LENGTH) {
            punctuationKey = Alphabet.PUNCTUATION.length();
        } else if (key > Alphabet.PUNCTUATION.length()) {
            punctuationKey = key % Alphabet.PUNCTUATION.length();
        }
        StringBuilder builder = new StringBuilder(line.length());
        String encryptUppercaseLetters = Alphabet.UPPERCASE_LETTERS.substring(key) + Alphabet.UPPERCASE_LETTERS.substring(0, key);
        String encryptLowercaseLetters = Alphabet.LOWERCASE_LETTERS.substring(key) + Alphabet.LOWERCASE_LETTERS.substring(0, key);
        String encryptPunctuation = Alphabet.PUNCTUATION.substring(punctuationKey) + Alphabet.PUNCTUATION.substring(0, punctuationKey);

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (Character.isLetter(ch) && Character.isLowerCase(ch) && contains(Alphabet.LOWERCASE_LETTERS, ch)) {
                int position = Alphabet.LOWERCASE_LETTERS.indexOf(ch);
                builder.append(encryptLowercaseLetters.charAt(position));
            } else if (Character.isLetter(ch) && Character.isUpperCase(ch) && contains(Alphabet.UPPERCASE_LETTERS, ch)) {
                int position = Alphabet.UPPERCASE_LETTERS.indexOf(ch);
                builder.append(encryptUppercaseLetters.charAt(position));
            } else if (contains(Alphabet.PUNCTUATION, ch)) {
                int position = Alphabet.PUNCTUATION.indexOf(ch);
                builder.append(encryptPunctuation.charAt(position));
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    private String toDecipher(String line, int key) {
        int punctuationKey = key;
        if (key > Alphabet.PUNCTUATION.length()) {
            punctuationKey = key % Alphabet.PUNCTUATION.length();
        }
        StringBuilder builder = new StringBuilder();
        String encryptUppercaseLetters = Alphabet.UPPERCASE_LETTERS.substring(key) + Alphabet.UPPERCASE_LETTERS.substring(0, key);
        String encryptLowercaseLetters = Alphabet.LOWERCASE_LETTERS.substring(key) + Alphabet.LOWERCASE_LETTERS.substring(0, key);
        String encryptPunctuation = Alphabet.PUNCTUATION.substring(punctuationKey) + Alphabet.PUNCTUATION.substring(0, punctuationKey);

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (Character.isLetter(ch) && Character.isLowerCase(ch) && contains(Alphabet.LOWERCASE_LETTERS, ch)) {
                int position = encryptLowercaseLetters.indexOf(ch);
                builder.append(Alphabet.LOWERCASE_LETTERS.charAt(position));
            } else if (Character.isLetter(ch) && Character.isUpperCase(ch) && contains(Alphabet.UPPERCASE_LETTERS, ch)) {
                int position = encryptUppercaseLetters.indexOf(ch);
                builder.append(Alphabet.UPPERCASE_LETTERS.charAt(position));
            } else if (contains(Alphabet.PUNCTUATION, ch)) {
                int position = encryptPunctuation.indexOf(ch);
                builder.append(Alphabet.PUNCTUATION.charAt(position));
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    private boolean contains(String line, char ch) {
        return line.indexOf(ch) != -1;
    }

    private Map.Entry<Character, Integer> maxRepeatedChar(Path path) {
        List<String> allLines = readLinesFromFile(path);
        Map<Character, Integer> lettersRepeatCount = countLettersRepeats(allLines);
        Map.Entry<Character, Integer> maxEntry = null;
        for (Map.Entry<Character, Integer> entry : lettersRepeatCount.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }
        return maxEntry;
    }

    private Map<Character, Integer> countLettersRepeats(List<String> allLines) {
        TreeMap<Character, Integer> lettersRepeatCount = new TreeMap<>();
        int repeatCount;
        for (String line : allLines) {
            for (int j = 0; j < line.length(); j++) {
                repeatCount = 0;
                char letter = Character.toLowerCase(line.charAt(j));
                if (contains(Alphabet.LOWERCASE_LETTERS, letter)) {
                    if (lettersRepeatCount.containsKey(letter)) {
                        repeatCount = lettersRepeatCount.get(letter);
                    }
                    repeatCount++;
                    lettersRepeatCount.put(letter, repeatCount);
                }
            }
        }
        return lettersRepeatCount;
    }
}
