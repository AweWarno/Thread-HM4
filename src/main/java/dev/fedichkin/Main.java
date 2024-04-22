package dev.fedichkin;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {
    private static final int countLineString = 10_000;
    private static final int sizeText = 100_000;
    private static BlockingQueue<String> queueToA = new ArrayBlockingQueue<>(100);
    private static BlockingQueue<String> queueToB = new ArrayBlockingQueue<>(100);
    private static BlockingQueue<String> queueToC = new ArrayBlockingQueue<>(100);

    private static String maxA = "";
    private static int countMaxA = 0;
    private static String maxB = "";
    private static int countMaxB = 0;
    private static String maxC = "";
    private static int countMaxC = 0;

    public static void main(String[] args) {
        Thread root = new Thread(() -> {
            String temp = generateText("abc", sizeText);
            maxA = temp;
            countMaxA = sizeText - temp.replaceAll("a", "").length();
            maxB = temp;
            countMaxB = sizeText - temp.replaceAll("b", "").length();
            maxC = temp;
            countMaxC = sizeText - temp.replaceAll("c", "").length();

            for (int i = 0; i < countLineString; i++) {
                String newLine = generateText("abc", sizeText);
                try {
                    queueToA.put(newLine);
                    queueToB.put(newLine);
                    queueToC.put(newLine);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        root.start();

        Thread threadA = new Thread(() -> {
            try {
                incrementCount(queueToA, "a", countMaxA, maxA);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Thread threadB = new Thread(() -> {
            try {
                incrementCount(queueToB, "b", countMaxB, maxB);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Thread threadC = new Thread(() -> {
            try {
                incrementCount(queueToC, "c", countMaxC, maxC);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        threadA.start();
        threadB.start();
        threadC.start();

        try {
            root.join();
            threadA.join();
            threadB.join();
            threadC.join();

            //
            System.out.println("maxA: " + maxA);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void incrementCount(BlockingQueue<String> queueTo, String regex, int countMax, String max) throws InterruptedException {
        for (int i = countLineString; i > 0; i--) {
            String line = queueTo.take();
            int currentMaxA = line.replaceAll(regex, "").length();
            if (countMax < currentMaxA) {
                max = line;
                countMax = currentMaxA;
            }
        }
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
}