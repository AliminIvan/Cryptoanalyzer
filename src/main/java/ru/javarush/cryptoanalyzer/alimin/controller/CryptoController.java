package ru.javarush.cryptoanalyzer.alimin.controller;

import ru.javarush.cryptoanalyzer.alimin.application.Cryptoanalyzer;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Scanner;

public class CryptoController {
    private final Scanner scanner = new Scanner(System.in);
    private final Cryptoanalyzer cryptoanalyzer = new Cryptoanalyzer();

    public void run() {
        Path path;
        String choice;
        String menu = """
                Пожалуйста, выберите режим работы криптоанализатора:
                1 - Шифрование / расшифровка.
                2 - Криптоанализ (для взлома шифра в случае отсутствия ключа)
                Или введите "exit" для завершения программы.
                 """;
        System.out.println(menu);
        choice = scanner.next();
        while (!choice.equals("exit")) {
            switch (choice) {
                case "1" -> {
                    while (!choice.equals("q")) {
                        System.out.println("""
                                Пожалуйста, выберите что необходимо сделать с файлом:
                                1 - Зашифровать
                                2 - Расшифровать
                                q - вернуться в главное меню
                                """);
                        choice = scanner.next();
                        switch (choice) {
                            case "1" -> {
                                while (true) {
                                    System.out.println("Пожалуйста введите путь к файлу, который необходимо зашифровать или введите q для возврата в главное меню: ");
                                    choice = scanner.next();
                                    if (choice.equals("q")) {
                                        break;
                                    }
                                    try {
                                        path = Path.of(choice);
                                        System.out.println("Пожалуйста введите любое неотрицательное целочисленное значение в качестве ключа шифрования или введите q для возврата: ");
                                        String key;
                                        key = scanner.next();
                                        if (key.equals("q")) {
                                            break;
                                        }
                                        cryptoanalyzer.encrypt(path, Integer.parseInt(key));
                                        System.out.println("Файл успешно зашифрован");
                                    } catch (NumberFormatException e) {
                                        System.out.println("Вы ввели недопустимое значение для ключа шифрования, пожалуйста введите любое неотрицательное целочисленное значение");
                                        throw new RuntimeException(e);
                                    } catch (InvalidPathException e) {
                                        System.out.println("Неверное указан путь");
                                    }
                                }
                            }
                            case "2" -> {
                                while (true) {
                                    System.out.println("Пожалуйста введите путь к файлу, который необходимо расшифровать или введите q для возврата в главное меню: ");
                                    choice = scanner.next();
                                    if (choice.equals("q")) {
                                        break;
                                    }
                                    path = Path.of(choice);
                                    System.out.println("Пожалуйста введите ключ, использованный при шифровании файла или нажмите q и воспользуйтесь криптоанализом, если забыли ключ: ");
                                    String key = scanner.next();
                                    if (key.equals("q")) {
                                        break;
                                    }
                                    cryptoanalyzer.decrypt(path, Integer.parseInt(key));
                                    System.out.println("Файл успешно расшифрован");
                                }
                            }
                        }
                    }
                }
                case "2" -> {
                    while (true) {
                        System.out.println("""
                                Пожалуйста, выберите метод криптоанализа:
                                1 - метод грубой силы (brute force)
                                2 - метод частотного анализа
                                3 - метод статистического анализа
                                4 - показать все возможные варианты расшифровки (для самостоятельного анализа)
                                q - вернуться в главное меню
                                """);
                        choice = scanner.next();
                        if (choice.equals("q")) {
                            break;
                        }
                        switch (choice) {
                            case "1" -> {
                                System.out.println("Пожалуйста введите путь к зашифрованному файлу, который необходимо взломать или введите q для возврата: ");
                                choice = scanner.next();
                                if (choice.equals("q")) {
                                    break;
                                }
                                path = Path.of(choice);
                                cryptoanalyzer.bruteForceHack(path);
                                System.out.println();
                            }

                            case "2" -> {
                                System.out.println("Пожалуйста введите путь к зашифрованному файлу, который необходимо взломать или введите q для возврата: ");
                                choice = scanner.next();
                                if (choice.equals("q")) {
                                    break;
                                }
                                path = Path.of(choice);
                                cryptoanalyzer.frequencyHack(path);
                                System.out.println();
                            }

                            case "3" -> {
                                System.out.println("Пожалуйста введите путь к файлу с текстом который является примером текста что был зашифрован (например другой труд того же автора) или введите q для возврата: ");
                                choice = scanner.next();
                                if (choice.equals("q")) {
                                    break;
                                }
                                Path exampleTextPath = Path.of(choice);
                                System.out.println("Пожалуйста введите путь к зашифрованному файлу, который необходимо взломать или введите q для возврата: ");
                                choice = scanner.next();
                                if (choice.equals("q")) {
                                    break;
                                }
                                path = Path.of(choice);
                                cryptoanalyzer.statisticalAnalysisHack(exampleTextPath, path);
                                System.out.println();
                            }

                            case "4" -> {
                                System.out.println("Пожалуйста введите путь к зашифрованному файлу, который необходимо взломать или введите q для возврата: ");
                                choice = scanner.next();
                                if (choice.equals("q")) {
                                    break;
                                }
                                path = Path.of(choice);
                                cryptoanalyzer.printAllPossibleOptions(path);
                                System.out.println();
                            }
                        }
                    }
                }
                default -> {
                    System.out.println(menu);
                    choice = scanner.next();
                }
            }
        }
    }
}
