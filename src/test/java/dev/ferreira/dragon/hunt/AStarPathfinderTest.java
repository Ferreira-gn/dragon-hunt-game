package dev.ferreira.dragon.hunt;

import org.junit.jupiter.api.Test;

import dev.ferreira.dragon.hunt.AStarPathfinder;
import dev.ferreira.dragon.hunt.Cell;
import dev.ferreira.dragon.hunt.MazeGrid;

import static org.junit.jupiter.api.Assertions.*;

public class AStarPathfinderTest {

    @Test
    void findsSimplePath() {
        MazeGrid maze = new MazeGrid(5, 5);
        maze.setStart(new Cell(0, 0));
        maze.setGoal(new Cell(0, 4));

        AStarPathfinder.Result result = new AStarPathfinder().findPath(maze);

        assertTrue(result.found());
        assertEquals(5, result.path().size());
        assertEquals(new Cell(0, 0), result.path().getFirst());
        assertEquals(new Cell(0, 4), result.path().getLast());
    }

    @Test
    void avoidsWalls() {
        MazeGrid maze = new MazeGrid(5, 5);
        maze.setStart(new Cell(2, 0));
        maze.setGoal(new Cell(2, 4));

        maze.setWall(new Cell(2, 1), true);
        maze.setWall(new Cell(2, 2), true);
        maze.setWall(new Cell(2, 3), true);

        AStarPathfinder.Result result = new AStarPathfinder().findPath(maze);

        assertTrue(result.found());
        assertTrue(result.path().size() > 5);
        assertTrue(result.path().stream().noneMatch(maze::isWall));
    }

    @Test
    void reportsNoPath() {
        MazeGrid maze = new MazeGrid(3, 3);
        maze.setStart(new Cell(1, 0));
        maze.setGoal(new Cell(1, 2));

        maze.setWall(new Cell(0, 1), true);
        maze.setWall(new Cell(1, 1), true);
        maze.setWall(new Cell(2, 1), true);

        AStarPathfinder.Result result = new AStarPathfinder().findPath(maze);

        assertFalse(result.found());
        assertTrue(result.path().isEmpty());
    }
}
