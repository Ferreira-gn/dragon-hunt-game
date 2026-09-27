package dev.ferreira.dragon.hunt;

import java.util.List;

public record PathfindingResult(List<Cell> path, List<Cell> exploredOrder, boolean found) {
} 