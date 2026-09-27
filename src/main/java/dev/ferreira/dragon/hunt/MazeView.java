package dev.ferreira.dragon.hunt;

import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public final class MazeView extends BorderPane {

    private static final int ROWS = 15;
    private static final int COLS = 15;
    private static final double CELL_SIZE = 40.0;
    private static final double CELL_GAP = 1.0;

    private final MazeGrid maze = new MazeGrid(ROWS, COLS);
    private final AStarPathfinder aStarPathfinder = new AStarPathfinder();
    private final GreedyBestFirstPathfinder greedyPathfinder = new GreedyBestFirstPathfinder();

    private final GridPane gridPane = new GridPane();
    private final Label statusLabel = new Label();
    private final ToggleButton wallTool = new ToggleButton("Parede");
    private final ToggleButton startTool = new ToggleButton("Início");
    private final ToggleButton goalTool = new ToggleButton("Saída");
    private final ToggleButton aStarAlgorithmTool = new ToggleButton("A*");
    private final ToggleButton greedyAlgorithmTool = new ToggleButton("Greedy Best-First");

    private final Rectangle[][] cells = new Rectangle[ROWS][COLS];

    private boolean mouseDown;
    private Cell lastEditedCell;
    private boolean editingWallValue;
    private boolean animating;

    public MazeView() {
        setPadding(new Insets(18));
        setTop(createHeader());
        setCenter(createGridContainer());
        setBottom(createFooter());

        createGrid();
        refreshAll();
    }

    private Node createHeader() {
        Label title = new Label("A* Maze Solver");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        Label subtitle = new Label(
                "Desenhe as paredes, defina o início e a saída e visualize o caminho encontrado pelo algoritmo escolhido."
        );
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");

        ToggleGroup drawingGroup = new ToggleGroup();

        wallTool.setToggleGroup(drawingGroup);
        startTool.setToggleGroup(drawingGroup);
        goalTool.setToggleGroup(drawingGroup);
        wallTool.setSelected(true);

        ToggleGroup algorithmGroup = new ToggleGroup();

        aStarAlgorithmTool.setToggleGroup(algorithmGroup);
        greedyAlgorithmTool.setToggleGroup(algorithmGroup);
        aStarAlgorithmTool.setSelected(true);

        // Impede que o usuário desmarque o algoritmo selecionado sem escolher outro.
        algorithmGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                oldToggle.setSelected(true);
            }
        });

        Button solveButton = new Button("Encontrar caminho");
        Button clearWallsButton = new Button("Limpar paredes");
        Button resetButton = new Button("Novo labirinto");

        solveButton.setOnAction(event -> solve());
        clearWallsButton.setOnAction(event -> {
            if (!animating) {
                maze.clearWalls();
                refreshAll();
                updateStatus("Paredes limpas.");
            }
        });
        resetButton.setOnAction(event -> {
            if (!animating) {
                maze.reset();
                refreshAll();
                updateStatus("Labirinto restaurado.");
            }
        });

        HBox drawingTools = new HBox(8, wallTool, startTool, goalTool);
        drawingTools.setAlignment(Pos.CENTER_LEFT);

        HBox algorithmTools = new HBox(8, new Label("Algoritmo:"), aStarAlgorithmTool, greedyAlgorithmTool);
        algorithmTools.setAlignment(Pos.CENTER_LEFT);

        HBox actionTools = new HBox(8, solveButton, clearWallsButton, resetButton);
        actionTools.setAlignment(Pos.CENTER_LEFT);

        HBox tools = new HBox(
                12,
                drawingTools,
                new Separator(),
                algorithmTools,
                new Separator(),
                actionTools
        );
        tools.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(10, title, subtitle, tools);
        header.setPadding(new Insets(0, 0, 14, 0));
        return header;
    }

    private Node createGridContainer() {
        gridPane.setHgap(CELL_GAP);
        gridPane.setVgap(CELL_GAP);
        gridPane.setAlignment(Pos.CENTER);

        BorderPane container = new BorderPane(gridPane);
        container.setStyle(
                "-fx-background-color: #e9e9e9; " +
                "-fx-border-color: #c8c8c8; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8;"
        );
        return container;
    }

    private Node createFooter() {
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

        Label legend = new Label("Início = verde   Saída = vermelho   Parede = escuro   Explorado = azul claro   Caminho = amarelo");
        legend.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        VBox footer = new VBox(5, statusLabel, legend);
        footer.setPadding(new Insets(12, 0, 0, 0));
        return footer;
    }

    private void createGrid() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
                rect.setStroke(Color.web("#d0d0d0"));
                rect.setFill(Color.WHITE);

                final int r = row;
                final int c = col;

                rect.setOnMousePressed(event -> {
                    if (animating) {
                        return;
                    }

                    mouseDown = true;
                    lastEditedCell = null;

                    Cell cell = new Cell(r, c);

                    if (wallTool.isSelected()) {
                        editingWallValue = !maze.isWall(cell);
                        applyWallEdit(cell);
                    } else if (startTool.isSelected()) {
                        maze.setStart(cell);
                        refreshAll();
                    } else if (goalTool.isSelected()) {
                        maze.setGoal(cell);
                        refreshAll();
                    }
                });

                rect.setOnMouseEntered(event -> {
                    if (animating || !mouseDown || !wallTool.isSelected()) {
                        return;
                    }

                    applyWallEdit(new Cell(r, c));
                });

                cells[row][col] = rect;
                gridPane.add(rect, col, row);
            }
        }

        gridPane.setOnMouseReleased(event -> {
            mouseDown = false;
            lastEditedCell = null;
        });
    }

    private void applyWallEdit(Cell cell) {
        if (cell.equals(lastEditedCell)) {
            return;
        }

        lastEditedCell = cell;
        maze.setWall(cell, editingWallValue);
        refreshCell(cell);
    }

    private void solve() {
        if (animating) {
            return;
        }

        refreshAll();

        String algorithmName = aStarAlgorithmTool.isSelected() ? "A*" : "Greedy Best-First";
        updateStatus("Executando " + algorithmName + "...");

        PathfindingResult result = aStarAlgorithmTool.isSelected()
                ? aStarPathfinder.findPath(maze)
                : greedyPathfinder.findPath(maze);

        animating = true;

        List<KeyFrame> frames = new ArrayList<>();

        int index = 0;
        for (Cell cell : result.exploredOrder()) {
            if (cell.equals(maze.start()) || cell.equals(maze.goal())) {
                continue;
            }

            int delay = index * 18;
            frames.add(new KeyFrame(Duration.millis(delay), event -> {
                Rectangle rect = cells[cell.row()][cell.col()];
                rect.setFill(Color.web("#9ecae1"));
            }));
            index++;
        }

        int pathStart = index * 18 + 60;
        int pathIndex = 0;

        for (Cell cell : result.path()) {
            if (cell.equals(maze.start()) || cell.equals(maze.goal())) {
                continue;
            }

            int delay = pathStart + pathIndex * 45;
            frames.add(new KeyFrame(Duration.millis(delay), event -> {
                Rectangle rect = cells[cell.row()][cell.col()];
                rect.setFill(Color.web("#f3c969"));
            }));
            pathIndex++;
        }

        SequentialTransition sequence = new SequentialTransition();

        if (!frames.isEmpty()) {
            Timeline timeline = new Timeline(frames.toArray(new KeyFrame[0]));
            sequence.getChildren().add(timeline);
        } else {
            sequence.getChildren().add(new PauseTransition(Duration.millis(250)));
        }

        sequence.setOnFinished(event -> {
            animating = false;

            if (result.found()) {
                updateStatus(
                        algorithmName + " encontrou o caminho. " +
                        "Passos: " + Math.max(0, result.path().size() - 1) +
                        " | Nós explorados: " + result.exploredOrder().size()
                );
            } else {
                updateStatus(
                        algorithmName + " não conseguiu alcançar a saída. " +
                        "Nós explorados: " + result.exploredOrder().size()
                );
            }
        });

        sequence.play();
    }

    private void refreshAll() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                refreshCell(new Cell(row, col));
            }
        }
    }

    private void refreshCell(Cell cell) {
        Rectangle rect = cells[cell.row()][cell.col()];

        if (cell.equals(maze.start())) {
            rect.setFill(Color.web("#63c174"));
        } else if (cell.equals(maze.goal())) {
            rect.setFill(Color.web("#e06464"));
        } else if (maze.isWall(cell)) {
            rect.setFill(Color.web("#303238"));
        } else {
            rect.setFill(Color.WHITE);
        }
    }

    private void updateStatus(String text) {
        statusLabel.setText(text);
    }
}