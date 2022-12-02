package com.vaadin.kriss.views.sheet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;


@PageTitle("Sheet")
@Route(value = "spread")
@RouteAlias(value = "")
public class SheetView extends VerticalLayout {

    private static final Logger LOG = LoggerFactory.getLogger(SheetView.class);

    private final Spreadsheet spreadsheet;
    private final CellStyle solidCellStyle;

    private CellRangeAddress gameRange;
    byte[][] data;

    public SheetView() {
        setSizeFull();

        spreadsheet = new Spreadsheet();
        solidCellStyle = spreadsheet.getWorkbook().createCellStyle();
        solidCellStyle.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
        solidCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        addSelectionListnerToDrawFigures();

        UI.getCurrent().setPollInterval(1000);
        UI.getCurrent().addPollListener(pollEvent -> {
            if (gameRange == null || data == null) {
                return;
            }

            final int rows = gameRange.getLastRow() - gameRange.getFirstRow() + 1;
            final int column = gameRange.getLastColumn() - gameRange.getFirstColumn() + 1;

            data = nextGeneration(data, rows, column);
            renderGame();
        });

        final MenuBar menuBar = createMenuBar();

        add(menuBar, spreadsheet);
    }

    private MenuBar createMenuBar() {
        final AtomicReference<CellRangeAddress> selectedCells = new AtomicReference<>();
        spreadsheet.addSelectionChangeListener(e ->
            selectedCells.set(e.getCellRangeAddresses().stream().findFirst().orElse(null))
        );

        final MenuBar menuBar = new MenuBar();
        menuBar.addItem("Create Border", event -> {
            if (selectedCells.get() != null && selectedCells.get().getNumberOfCells() > 1) {
                updateBorders(selectedCells.get());
            }
        });
        menuBar.addItem("Start", event ->
            calculateData()
        );

        return menuBar;
    }

    private void addSelectionListnerToDrawFigures() {
        spreadsheet.addSelectionChangeListener(e -> {
            if (gameRange == null) {
                return;
            }

            final var optionalCellAddresses = e.getCellRangeAddresses().stream().findFirst();
            if (optionalCellAddresses.isEmpty()) {
                tryToDrawSingleCell(e);
                return;
            }
            final CellRangeAddress cellAddresses = optionalCellAddresses.get();
            if (cellAddresses == gameRange) {
                return;
            }

            tryToDrawCellRange(cellAddresses);
        });
    }

    private void tryToDrawCellRange(final CellRangeAddress cellAddresses) {
        final List<Cell> listOfCells = new ArrayList<>();
        for (final CellAddress cellAddress : cellAddresses) {
            final CellReference cellRef = new CellReference(spreadsheet.getActiveSheet().getSheetName(), cellAddress.getRow(),
                    cellAddress.getColumn(), false, false);
            if (gameRange.containsColumn(cellRef.getCol()) && gameRange.containsRow(cellRef.getRow())) {
                final Cell cell = drawCell(cellRef);
                listOfCells.add(cell);
            }
        }

        spreadsheet.refreshCells(listOfCells);
    }

    private Cell drawCell(final CellReference cellRef) {
        final Cell cell = getOrCreateCell(cellRef);
        if (cell.getCellStyle().equals(solidCellStyle)) {
            cell.setCellStyle(spreadsheet.getWorkbook().createCellStyle());
            cell.setCellValue("");
        } else {
            cell.setCellStyle(solidCellStyle);
            cell.setCellValue("x");
        }
        return cell;
    }

    private void tryToDrawSingleCell(final Spreadsheet.SelectionChangeEvent e) {
        final CellReference cellRef = e.getSelectedCellReference();
        if (cellRef != null && gameRange.containsColumn(cellRef.getCol()) && gameRange.containsRow(cellRef.getRow())) {
            final Cell cell = drawCell(cellRef);
            spreadsheet.refreshCells(cell);
        }
    }

    private void updateBorders(final CellRangeAddress cellAddresses) {
        cleanUpGameRange();
        gameRange = cellAddresses;
        setBorderOfRegion(gameRange);
    }

    private void cleanUpGameRange() {
        if(gameRange == null) {
            return;
        }

        final List<Cell> cells = new ArrayList<>();
        for (final CellAddress cellAddress : gameRange) {
            final CellReference cellRef = new CellReference(spreadsheet.getActiveSheet().getSheetName(), cellAddress.getRow(),
                    cellAddress.getColumn(), false, false);
            if (gameRange.containsColumn(cellRef.getCol()) && gameRange.containsRow(cellRef.getRow())) {
                final Cell cell = getOrCreateCell(cellRef);
                final CellStyle cellStyle = spreadsheet.getWorkbook().createCellStyle();
                cellStyle.setBorderBottom(BorderStyle.NONE);
                cellStyle.setBorderRight(BorderStyle.NONE);
                cellStyle.setBorderTop(BorderStyle.NONE);
                cellStyle.setBorderLeft(BorderStyle.NONE);
                cell.setCellStyle(cellStyle);
                cell.setCellValue("");
                cells.add(cell);
            }
        }
        spreadsheet.refreshCells(cells);
    }

    private void setBorderOfRegion(final CellRangeAddress cellAddresses) {
        final List<Cell> listOfCells = new ArrayList<>();
        for (final CellAddress cellAddress : cellAddresses) {
            final CellReference cellRef = new CellReference(spreadsheet.getActiveSheet().getSheetName(), cellAddress.getRow(),
                    cellAddress.getColumn(), false, false);
            final Cell cell = getOrCreateCell(cellRef);
            final CellStyle cellStyle = spreadsheet.getWorkbook().createCellStyle();
            drawBorderInRange(cell, cellStyle, cellAddresses);
            listOfCells.add(cell);
        }
        spreadsheet.refreshCells(listOfCells);
    }

    private void calculateData() {
        if (gameRange == null) {
            return;
        }
        final int rows = gameRange.getLastRow() - gameRange.getFirstRow() + 1;
        final int column = gameRange.getLastColumn() - gameRange.getFirstColumn() + 1;
        data = new byte[rows][column];

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                final Cell cellFromGame = getCellFromGame(i, j);
                if ("x".equals(cellFromGame.getStringCellValue())) {
                    data[i][j] = 1;
                } else {
                    data[i][j] = 0;
                }
            }
        }
    }

    private Cell getCellFromGame(final int i, final int j) {
        final int row = gameRange.getFirstRow() + i;
        final int column = gameRange.getFirstColumn() + j;
        final CellReference cellRef = new CellReference(spreadsheet.getActiveSheet().getSheetName(),
                row, column, false, false);
        return getOrCreateCell(cellRef);
    }

    private void renderGame() {
        final List<Cell> cells = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                final Cell cell = getCellFromGame(i, j);
                final CellStyle cellStyle = spreadsheet.getWorkbook().createCellStyle();
                if (data[i][j] == 1) {
                    //alive
                    cellStyle.cloneStyleFrom(solidCellStyle);
                    cell.setCellStyle(cellStyle);
                    drawBorderInRange(cell, cellStyle, gameRange);
                    cell.setCellValue("X");
                } else {
                    //ded
                    drawBorderInRange(cell, cellStyle, gameRange);
                    cell.setCellValue("");
                }

                cells.add(cell);
            }
        }
        spreadsheet.refreshCells(cells);
    }

    private void drawBorderInRange(final Cell cell, final CellStyle cellStyle, final CellRangeAddress gameRange) {
        if (cell.getRow().getRowNum() == gameRange.getFirstRow()) {
            cellStyle.setBorderTop(BorderStyle.THICK);
            cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        } else if (cell.getRow().getRowNum() == gameRange.getLastRow()) {
            cellStyle.setBorderBottom(BorderStyle.THICK);
            cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        }

        if (cell.getColumnIndex() == gameRange.getFirstColumn()) {
            cellStyle.setBorderLeft(BorderStyle.THICK);
            cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        } else if (cell.getColumnIndex() == gameRange.getLastColumn()) {
            cellStyle.setBorderRight(BorderStyle.THICK);
            cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        }
        cell.setCellStyle(cellStyle);
    }

    //TODO take style data directly
    //Copied logic from https://stackoverflow.com/a/70869260
    public static byte[][] nextGeneration(final byte[][] inputGrid, final int i, final int j) {
        final byte[][] future = new byte[i][j];

        // iterate over each row (start with 0 because an array index is 0 based)
        for (int x = 0; x < i; x++) {
            // iterate over each column (start with 0 as well)
            for (int y = 0; y < j; y++) {
                final int aliveNeighbours = getAliveNeighbours(inputGrid, i, j, x, y);

                // simplified logic to remove unnecessary conditions
                // any cell with three neighbors is alive (past value doesn't matter)
                if (aliveNeighbours == 3) {
                    future[x][y] = 1;
                }
                // any cell with fewer than two live neighbors is dead (past value doesn't matter)
                else if (aliveNeighbours < 2) {
                    future[x][y] = 0;
                }
                // any cell with more than three neighbors is dead (past value doesn't matter)
                else if (aliveNeighbours >= 4) {
                    future[x][y] = 0;
                }
                // any cell with two neighbors remains in its present state (regardless of what the past value was)
                else {
                    future[x][y] = inputGrid[x][y];
                }
            }
        }
        return future;
    }

    private static int getAliveNeighbours(final byte[][] inputGrid, final int i, final int j, final int x, final int y) {
        int aliveNeighbours = 0;
        // the row above is x-1 but never less than 0 because that row doesn't exist
        final int rowAbove = Math.max(x - 1, 0);
        // the row below is never greater than the last row in the array (i - 1)
        final int rowBelow = Math.min(x + 1, i - 1);
        // go to the left one column, unless we are at the edge, then don't go past 0
        final int colLeft = Math.max(y - 1, 0);
        // ... continuing the same logic as above
        final int colRight = Math.min(y + 1, j - 1);
        for (int rowToCheck = rowAbove; rowToCheck <= rowBelow; rowToCheck++) {
            for (int colToCheck = colLeft; colToCheck <= colRight; colToCheck++) {
                aliveNeighbours += inputGrid[rowToCheck][colToCheck];
            }
        }

        // remove the cell being evaluated from the neighbors count
        aliveNeighbours -= inputGrid[x][y];
        return aliveNeighbours;
    }

    private Cell getOrCreateCell(final CellReference cellRef) {
        Cell cell = spreadsheet.getCell(cellRef.getRow(), cellRef.getCol());
        if (cell == null) {
            cell = spreadsheet.createCell(cellRef.getRow(), cellRef.getCol(), "");
        }
        return cell;
    }
}
