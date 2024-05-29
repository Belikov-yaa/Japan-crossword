package ru.yarikbel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 *
 */
public class App {
    public static void main(String[] args) {
//        System.out.println("\u001B[35mHello \u001B[34mWorld\u001B[43m!\u001B[0m");

        JapCross cross;
        List<List<Integer>> columnBlocksList = new ArrayList<>();
        List<List<Integer>> rowBlocksList = new ArrayList<>();

        if (args.length > 0 && !args[0].isBlank()) {
            List<String> allLineFromFile = readCrossDataFileToStrngList(args[0]);
            getCrossColAndRowBlockData(allLineFromFile, columnBlocksList, rowBlocksList);
        }
        cross = new JapCross(columnBlocksList, rowBlocksList);
        OptionalInt maxCol = columnBlocksList.stream().mapToInt(List::size).max();
        OptionalInt maxRow = rowBlocksList.stream().mapToInt(List::size).max();
//        System.out.println(maxCol.getAsInt() + " " + maxRow.getAsInt());
//        System.out.println("###");
//        columnBlocksList.forEach(System.out::println);
//        System.out.println("###");
//        rowBlocksList.forEach(System.out::println);
        cross.run();
//        cross.printSolution();
    }

    private static void getCrossColAndRowBlockData(List<String> lines,
                                                   List<List<Integer>> columnBlocksList,
                                                   List<List<Integer>> rowBlocksList) {
        int rowAmount = Integer.parseInt(lines.get(0));
        for (int i = 1; i <= rowAmount; i++) {
            rowBlocksList.add(parseLineToIntegerList(lines.get(i)));
        }
        int colAmount = Integer.parseInt(lines.get(rowAmount + 1));
        for (int i = rowAmount + 2; i <= colAmount + rowAmount + 1; i++) {
            columnBlocksList.add(parseLineToIntegerList(lines.get(i)));
        }
    }

    private static ArrayList<Integer> parseLineToIntegerList(String s) {
        ArrayList<Integer> result = new ArrayList<>();
        String[] numbers = s.split(" ");
        for (String number : numbers) {
            int block = Integer.parseInt(number);
            if (block < 0) throw new RuntimeException("Wrong file data");
            result.add(block);
        }
        return result;
    }

    public static List<String> readCrossDataFileToStrngList(String fileName) {
        try (Scanner input = new Scanner(new File(fileName))) {
            ArrayList<String> result = new ArrayList<>();
            while (input.hasNext()) {
                result.add(input.nextLine());
            }
            return result;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
