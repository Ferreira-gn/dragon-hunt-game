package dev.ferreira.dragon.hunt.ui;

import dev.ferreira.dragon.hunt.Cell;

import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.function.Consumer;

final class MazeBoard extends BorderPane {

    static final int ROWS = 15;
    static final int COLS = 15;

    private static final double CELL_SIZE = 40.0;
    private static final double CELL_GAP = 1.0;

    private final Rectangle[][] cells = new Rectangle[ROWS][COLS];

    /** Tabuleiro apenas para exibição (cliques ignorados). */
    MazeBoard() {
        this(cell -> { });
    }

    MazeBoard(Consumer<Cell> onCellClicked) {
        GridPane grid = new GridPane();
        grid.setHgap(CELL_GAP);
        grid.setVgap(CELL_GAP);
        grid.setAlignment(Pos.CENTER);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
                rect.setArcWidth(4);
                rect.setArcHeight(4);
                rect.setStroke(Color.web("#cbb98f"));
                rect.setFill(Theme.FLOOR_COLOR);

                Cell cell = new Cell(row, col);
                rect.setOnMouseClicked(event -> onCellClicked.accept(cell));

                cells[row][col] = rect;
                grid.add(rect, col, row);
            }
        }

        setCenter(grid);
        setStyle(
                "-fx-background-color: " + Theme.PANEL_BG + "; " +
                "-fx-border-color: " + Theme.PANEL_BORDER + "; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 12;"
        );
    }

    void paint(Cell cell, Color color) {
        cells[cell.row()][cell.col()].setFill(color);
    }
}
