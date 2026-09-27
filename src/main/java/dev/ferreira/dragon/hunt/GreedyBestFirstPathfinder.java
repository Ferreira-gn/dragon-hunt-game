
package dev.ferreira.dragon.hunt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;


public final class GreedyBestFirstPathfinder {

    public PathfindingResult findPath(MazeGrid maze) {
        Cell start = maze.start();
        Cell goal = maze.goal();

        PriorityQueue<Node> open = new PriorityQueue<>(
                Comparator.comparingInt(Node::h)
        );

        Map<Cell, Cell> cameFrom = new HashMap<>();
        Set<Cell> visited = new HashSet<>();
        Set<Cell> closed = new HashSet<>();
        List<Cell> exploredOrder = new ArrayList<>();

        visited.add(start);
        open.add(new Node(start, heuristic(start, goal)));

        while (!open.isEmpty()) {
            Node currentNode = open.poll();
            Cell current = currentNode.cell();

            if (closed.contains(current)) {
                continue;
            }

            closed.add(current);
            exploredOrder.add(current);

            if (current.equals(goal)) {
                return new PathfindingResult(reconstructPath(cameFrom, goal), exploredOrder, true);
            }

            for (Cell neighbor : neighbors(current, maze)) {
                if (visited.contains(neighbor)) {
                    continue;
                }

                visited.add(neighbor);
                cameFrom.put(neighbor, current);
                open.add(new Node(neighbor, heuristic(neighbor, goal)));
            }
        }

        return new PathfindingResult(List.of(), exploredOrder, false);
    }

    private List<Cell> reconstructPath(Map<Cell, Cell> cameFrom, Cell goal) {
        List<Cell> path = new ArrayList<>();
        Cell current = goal;
        path.add(current);

        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(current);
        }

        java.util.Collections.reverse(path);
        return path;
    }

    private List<Cell> neighbors(Cell cell, MazeGrid maze) {
        int row = cell.row();
        int col = cell.col();

        Cell[] candidates = {
                new Cell(row - 1, col),
                new Cell(row + 1, col),
                new Cell(row, col - 1),
                new Cell(row, col + 1)
        };

        List<Cell> result = new ArrayList<>(4);
        for (Cell candidate : candidates) {
            if (maze.contains(candidate) && !maze.isWall(candidate)) {
                result.add(candidate);
            }
        }
        return result;
    }

    private int heuristic(Cell a, Cell b) {
        return Math.abs(a.row() - b.row()) + Math.abs(a.col() - b.col());
    }

    private record Node(Cell cell, int h) {
    }
}