package ru.yarikbel;

import java.util.Arrays;
import java.util.List;

import static java.lang.Math.max;

public class JapCross {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";

    // текущему состоянию клеток: 0 -- пока что неизвестно, 1 -- зарисованная, 2 -- гарантированно не зарисованная,
    private final byte[][] gameField;
    private final byte[][] gameFieldColor;
    private final int rowAmount;
    private final int columnAmount;
    private final boolean[] needRefreshCol;
    private final boolean[] needRefreshRow;
    private final boolean[] canBeFilled;
    private final boolean[] canBeEmpty;


    private final List<List<Integer>> columnBlocksListsArray;
    private final List<List<Integer>> rowBlocksListsArray;
    private int errorLevel;

    public JapCross(List<List<Integer>> columnBlocksListsArray, List<List<Integer>> rowBlocksListsArray) {
        this.rowAmount = rowBlocksListsArray.size();
        this.columnAmount = columnBlocksListsArray.size();
        this.gameField = new byte[rowAmount][columnAmount];
        this.gameFieldColor = new byte[rowAmount][columnAmount];
        this.columnBlocksListsArray = columnBlocksListsArray;
        this.rowBlocksListsArray = rowBlocksListsArray;
        this.needRefreshCol = new boolean[columnAmount];
        this.needRefreshRow = new boolean[rowAmount];
        Arrays.fill(needRefreshCol, true);
        Arrays.fill(needRefreshRow, true);

        this.canBeFilled = new boolean[max(columnAmount, rowAmount)];
        this.canBeEmpty = new boolean[max(columnAmount, rowAmount)];
    }

    public void iterateLineLook() {
        boolean findedCellsState;
        int iteration = 1;
        do {
            findedCellsState = false;
            for (int i = 0; i < rowAmount; i++) {
                if (needRefreshRow[i])
                    findedCellsState = analyzeLine(true, i) || findedCellsState;
            }

            if (findedCellsState) {
                System.out.println("row search iteration = " + iteration);
                printSolution();
                clearColorInfo();
            }

            for (int i = 0; i < columnAmount; i++) {
                if (needRefreshCol[i]) findedCellsState = analyzeLine(false, i) || findedCellsState;
            }
            if (findedCellsState) {
                System.out.println("column search");
                printSolution();
                clearColorInfo();
            }
            iteration++;
        } while (findedCellsState);
    }

    private void clearColorInfo() {
        for (int i = 0; i < rowAmount; i++) {
            Arrays.fill(gameFieldColor[i], (byte) 0);
        }
    }

    public void run() {
        errorLevel = 0;
        iterateLineLook();
    }

    public void printSolution() {
        System.out.println("=============");
        for (int i = 0; i < rowAmount; i++) {
            byte previousColor = 0;
            for (int j = 0; j < columnAmount; j++) {
                if (previousColor != gameFieldColor[i][j]) {
                    previousColor = gameFieldColor[i][j];
                    if (gameFieldColor[i][j] == 0) {
                        System.out.print(ANSI_RESET);
                    } else {
                        System.out.print(ANSI_RED);
                    }
                }
                if (gameField[i][j] == 1)
                    System.out.print("\u2588\u258a");
                else if (gameField[i][j] == 2)
                    System.out.print("\u2591\u2591");
                else System.out.print("\u2595\u258f");
            }
            System.out.println(ANSI_RESET);
        }
        System.out.println("=============");
    }

    private boolean tryBlock(int blockNumber,
                             int blockStartIndex,
                             byte[] cells,
                             List<Integer> blocksLen,
                             boolean[] canBeFilled,
                             boolean[] canBeEmpty) {
        int nextBlockStartIndex;
        int blockLen = blockNumber == -1 ? 0 : blocksLen.get(blockNumber);
        boolean result;
        // проверяем все клеточки блока, если хоть одна из них гарантированно не зарисованная, то размещение блока в этой позиции не возможно
        if (blockNumber > -1) {
            for (int i = blockStartIndex; i < blockStartIndex + blockLen; i++) {
                if (cells[i] == 2) {
                    return false;
                }
            }
        }
        // если блок не последний в последовательности, то проверяем непротиворечивость размещения следующих за ним блоков
        if (blockNumber + 1 < blocksLen.size()) {
            result = false;
            int nextBlockStartRightIndexLimit = getStartIndexLimit(blockNumber + 1, blocksLen);
            for (nextBlockStartIndex = blockStartIndex + blockLen + 1;
                 nextBlockStartIndex <= cells.length - nextBlockStartRightIndexLimit;
//                 nextBlockStartIndex <= cells.length - blocksLen.get(blockNumber + 1) + 1;
                 nextBlockStartIndex++) {
                if (nextBlockStartIndex > 0 && cells[nextBlockStartIndex - 1] == 1) { // возможно нужно добавить вначале условие "blockNumber > -1 &&"
                    break;
                }
                if (tryBlock(blockNumber + 1, nextBlockStartIndex, cells, blocksLen, canBeFilled, canBeEmpty)) {
                    result = true;
                    //(*какое-то непротиворечивое размещение дальнейших блоков существует*)
                    for (int i = blockStartIndex; i < blockStartIndex + blockLen; i++) {
                        canBeFilled[i] = true; // помечаем предполагаемые занятые блоком клетки
                    }
                    for (int i = blockStartIndex + blockLen; i < nextBlockStartIndex; i++) {
                        if (i > -1)
                            canBeEmpty[i] = true; // помечаем клетки между текущим блоком и последующим как возможно пустые
                    }
                }
            }
            return result;
        } else { // если блок последний, то проверяем, что нет зарисованных блоков после него
            for (int i = blockStartIndex + blockLen; i < cells.length; i++) {
                if (cells[i] == 1) {
                    return false;
                }
            }
            // (*данное размещение последнего блока непротиворечиво*)
            for (int i = blockStartIndex; i < blockStartIndex + blockLen; i++) {
                canBeFilled[i] = true; // помечаем предполагаемые занятые блоком клетки
            }
            for (int i = blockStartIndex + blockLen; i < cells.length; i++) {
                if (i > -1)
                    canBeEmpty[i] = true; // помечаем клетки от текущим блоком до конца линии как возможно пустые
            }
            return true;
        }
    }

    private int getStartIndexLimit(int blockNumber, List<Integer> blocksLen) {
        int sum = 0;
        int spaces = 0;
        for (int i = blockNumber; i < blocksLen.size(); i++) {
            sum += blocksLen.get(i);
            spaces++;
        }
        return sum + spaces - 1;
    }

    private boolean analyzeLine(boolean isAnalyzeRow, int lineNumber) {
        boolean result = false;
        byte[] cells;
        List<Integer> blocksLen;
        if (isAnalyzeRow) {
            needRefreshRow[lineNumber] = false;
            cells = getCellsFromRow(lineNumber);
            blocksLen = rowBlocksListsArray.get(lineNumber);
        } else {
            needRefreshCol[lineNumber] = false;
            cells = getCellsFromColumn(lineNumber);
            blocksLen = columnBlocksListsArray.get(lineNumber);
        }

        Arrays.fill(canBeFilled, false);
        Arrays.fill(canBeEmpty, false);

        if (tryBlock(-1, -1, cells, blocksLen, canBeFilled, canBeEmpty)) {
            for (int i = 0; i < cells.length; i++) {
                if (cells[i] == 0 && (canBeFilled[i] ^ canBeEmpty[i])) {
                    result = true;
                    if (isAnalyzeRow) {
                        needRefreshCol[i] = true;
                    } else {
                        needRefreshRow[i] = true;
                    }
                    if (canBeFilled[i]) {
                        cells[i] = 1;
                    } else {
                        cells[i] = 2;
                    }
                    if (isAnalyzeRow) {
                        gameField[lineNumber][i] = cells[i];
                        gameFieldColor[lineNumber][i] = 1;
                    } else {
                        gameField[i][lineNumber] = cells[i];
                        gameFieldColor[i][lineNumber] = 1;
                    }
                }
            }
        } else {
            errorLevel = 1;
        }
        return result;
    }

    private byte[] getCellsFromColumn(int lineNumber) {
        byte[] cells = new byte[rowAmount];
        for (int i = 0; i < rowAmount; i++) {
            cells[i] = gameField[i][lineNumber];
        }
        return cells;
    }

    private byte[] getCellsFromRow(int lineNumber) {
        byte[] cells = new byte[columnAmount];
        System.arraycopy(gameField[lineNumber], 0, cells, 0, columnAmount);
        return cells;
    }

    public byte[][] getGameField() {
        return gameField;
    }

    public int getColumnAmount() {
        return columnAmount;
    }

    public int getRowAmount() {
        return rowAmount;
    }

    public List<List<Integer>> getColumnBlocksListsArray() {
        return columnBlocksListsArray;
    }

    public List<List<Integer>> getRowBlocksListsArray() {
        return rowBlocksListsArray;
    }

}
