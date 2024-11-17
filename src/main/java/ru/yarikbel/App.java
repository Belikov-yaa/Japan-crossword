package ru.yarikbel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 *
 */
public class App {
    public static void main(String[] args) {

        JapCross cross;
        List<List<Integer>> columnBlocksList = new ArrayList<>();
        List<List<Integer>> rowBlocksList = new ArrayList<>();

        if (args.length == 0 || args[0].isBlank()) {
            printHelp();
            return;
        }

        List<String> allLineFromFile = readCrossDataFileToStrngList(args[0]);
        getCrossColAndRowBlockData(allLineFromFile, columnBlocksList, rowBlocksList);
        if (!checkColoredCellsSum(columnBlocksList, rowBlocksList)) {
            throw new RuntimeException("Wrong file data: Сумма закрашенных ячеек по столбцам не равна сумме сумме закрашенных ячеек по строкам");
        }

        cross = new JapCross(columnBlocksList, rowBlocksList);
/*
        OptionalInt maxCol = columnBlocksList.stream().mapToInt(List::size).max();
        OptionalInt maxRow = rowBlocksList.stream().mapToInt(List::size).max();
*/
        cross.run(0, 0);
//        cross.printSolution();
    }

    private static boolean checkColoredCellsSum(List<List<Integer>> columnBlocksList, List<List<Integer>> rowBlocksList) {
        int coloredCellsFromCol = columnBlocksList.stream()
                .flatMap(Collection::stream)
                .mapToInt(Integer::intValue)
                .sum();
        int coloredCellsFromRow = rowBlocksList.stream()
                .flatMap(Collection::stream)
                .mapToInt(Integer::intValue)
                .sum();
        return coloredCellsFromCol == coloredCellsFromRow;
    }

    private static void printHelp() {
        System.out.println("Отсутствует обязательный параметр запуска - имя файла с данными кроссворда.");
        System.out.println("Файл должен содержать в первой строке целое число N - количество строк кроссворда");
        System.out.println("Последующие N строк количества непрерывно закрашенных блоков в очередной строке, разделенные пробелом");
        System.out.println("В строке N+2 - целое число M - количество столбцов в кроссворде ");
        System.out.println("Последующие M строк количества непрерывно закрашенных блоков в очередном столбце, разделенные пробелом");
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
