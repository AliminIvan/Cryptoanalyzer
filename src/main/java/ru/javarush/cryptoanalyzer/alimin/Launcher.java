package ru.javarush.cryptoanalyzer.alimin;

import ru.javarush.cryptoanalyzer.alimin.controller.CryptoController;

public class Launcher {
    public static void main(String[] args) {
        CryptoController controller = new CryptoController();
        controller.run();
    }
}
