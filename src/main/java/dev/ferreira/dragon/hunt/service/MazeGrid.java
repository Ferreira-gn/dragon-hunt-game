package dev.ferreira.dragon.hunt.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import dev.ferreira.dragon.hunt.dtos.Cell;

public final class MazeGrid {
    private final int rows;
    private final int cols;
    private final Set<Cell> walls = new HashSet<>();

    private Cell start;
    private Cell goal;

    public MazeGrid(int rows, int cols) {
        if (rows < 2 || cols < 2) {
            throw new IllegalArgumentException("Grid must have at least 2 rows and 2 columns.");
        }

        this.rows = rows;
        this.cols = cols;
        this.start = new Cell(1, 1);
        this.goal = new Cell(rows - 2, cols - 2);
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    public Cell start() {
        return start;
    }

    public Cell goal() {
        return goal;
    }

    public Set<Cell> walls() {
        return Collections.unmodifiableSet(walls);
    }

    public boolean contains(Cell cell) {
        return cell.row() >= 0 && cell.row() < rows
                && cell.col() >= 0 && cell.col() < cols;
    }

    public boolean isWall(Cell cell) {
        return walls.contains(cell);
    }

    public void toggleWall(Cell cell) {
        requireInside(cell);

        if (cell.equals(start) || cell.equals(goal)) {
            return;
        }

        if (!walls.add(cell)) {
            walls.remove(cell);
        }
    }

    public void setWall(Cell cell, boolean wall) {
        requireInside(cell);

        if (cell.equals(start) || cell.equals(goal)) {
            return;
        }

        if (wall) {
            walls.add(cell);
        } else {
            walls.remove(cell);
        }
    }

    public void setStart(Cell cell) {
        requireInside(cell);

        if (cell.equals(goal)) {
            return;
        }

        walls.remove(cell);
        start = cell;
    }

    public void setGoal(Cell cell) {
        requireInside(cell);

        if (cell.equals(start)) {
            return;
        }

        walls.remove(cell);
        goal = cell;
    }

    public void clearWalls() {
        walls.clear();
    }

    public void reset() {
        walls.clear();
        start = new Cell(1, 1);
        goal = new Cell(rows - 2, cols - 2);
    }

    private void requireInside(Cell cell) {
        if (!contains(cell)) {
            throw new IllegalArgumentException("Cell outside grid: " + cell);
        }
    }
}
