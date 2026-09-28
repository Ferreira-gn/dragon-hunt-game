package dev.ferreira.dragon.hunt.dtos;

import java.util.List;

public record PathfindingResult(List<Cell> path, List<Cell> exploredOrder, boolean found) {
} 