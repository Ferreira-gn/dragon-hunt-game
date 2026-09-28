package dev.ferreira.dragon.hunt.dtos;

import dev.ferreira.dragon.hunt.service.MazeGrid;

public interface Pathfinder {
    PathfindingResult findPath(MazeGrid maze);
}
