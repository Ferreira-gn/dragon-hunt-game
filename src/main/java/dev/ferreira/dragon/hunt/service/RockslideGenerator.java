package dev.ferreira.dragon.hunt.service;

import java.util.Random;

import dev.ferreira.dragon.hunt.dtos.Cell;
import dev.ferreira.dragon.hunt.dtos.Pathfinder;


public final class RockslideGenerator {

    public static final double DEFAULT_DENSITY = 0.32;
    public static final int DEFAULT_MAX_ATTEMPTS = 40;

    private final Pathfinder validator;
    private final Random random;
    private final double density;
    private final int maxAttempts;

    public RockslideGenerator(Pathfinder validator) {
        this(validator, new Random(), DEFAULT_DENSITY, DEFAULT_MAX_ATTEMPTS);
    }

    public RockslideGenerator(Pathfinder validator, Random random, double density, int maxAttempts) {
        this.validator = validator;
        this.random = random;
        this.density = density;
        this.maxAttempts = maxAttempts;
    }

    public void generate(MazeGrid maze, int rows, int cols) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            scatter(maze, rows, cols);
            if (validator.findPath(maze).found()) {
                return;
            }
        }
    }

    private void scatter(MazeGrid maze, int rows, int cols) {
        maze.clearWalls();

        Cell start = maze.start();
        Cell goal = maze.goal();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Cell cell = new Cell(row, col);
                boolean endpoint = cell.equals(start) || cell.equals(goal);
                if (!endpoint && random.nextDouble() < density) {
                    maze.setWall(cell, true);
                }
            }
        }
    }
}
