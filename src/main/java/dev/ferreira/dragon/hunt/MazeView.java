package dev.ferreira.dragon.hunt;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Tela principal do Dragon Hunt: o herói precisa atravessar as cavernas,
 * desviar das rochas desabadas e alcançar o covil do dragão. O jogador
 * esculpe o labirinto clicando nas células, escolhe o algoritmo de busca
 * e, ao fim da caçada, é levado para uma tela de resultado com a opção de
 * voltar a jogar ou encerrar.
 */
public final class MazeView extends BorderPane {

    private static final int ROWS = 15;
    private static final int COLS = 15;
    private static final double CELL_SIZE = 40.0;
    private static final double CELL_GAP = 1.0;

    // Paleta temática: pedra de caverna + brasa de dragão.
    private static final String STONE_BG = "#1c1613";
    private static final String PANEL_BG = "#241d19";
    private static final String PANEL_BORDER = "#3a2f27";
    private static final String TEXT_PRIMARY = "#f1e7d0";
    private static final String TEXT_MUTED = "#b8a888";
    private static final String ACCENT_FIRE = "#d9622b";
    private static final String ACCENT_GOLD = "#c9a13b";

    private static final Color FLOOR_COLOR = Color.web("#e8ddc7");
    private static final Color WALL_COLOR = Color.web("#241a16");
    private static final Color HERO_COLOR = Color.web("#4c8c5d");
    private static final Color DRAGON_COLOR = Color.web("#8c2f2f");
    private static final Color EXPLORED_COLOR = Color.web("#5b7fa6");
    private static final Color PATH_COLOR = Color.web("#e8b74d");

    private final MazeGrid maze = new MazeGrid(ROWS, COLS);
    private final AStarPathfinder aStarPathfinder = new AStarPathfinder();
    private final GreedyBestFirstPathfinder greedyPathfinder = new GreedyBestFirstPathfinder();

    private final GridPane gridPane = new GridPane();

    private final ToggleButton wallTool = new ToggleButton("Rochas");
    private final ToggleButton startTool = new ToggleButton("Herói");
    private final ToggleButton goalTool = new ToggleButton("Dragão");
    private final ToggleButton aStarAlgorithmTool = new ToggleButton("A*");
    private final ToggleButton greedyAlgorithmTool = new ToggleButton("Greedy Best-First");
    private final Button solveButton = new Button("Iniciar caçada");

    private final Rectangle[][] cells = new Rectangle[ROWS][COLS];

    private Node header;
    private Node gridContainer;

    private boolean animating;
    private long searchStartNanos;

    public MazeView() {
        setStyle("-fx-background-color: " + STONE_BG + ";");
        setPadding(new Insets(18));

        header = createHeader();
        gridContainer = createGridContainer();

        setTop(header);
        setCenter(gridContainer);

        createGrid();
        refreshAll();
    }

    private Node createHeader() {
        Label title = new Label("Dragon Hunt");
        title.setStyle(
                "-fx-font-size: 30px; -fx-font-weight: bold; " +
                "-fx-text-fill: " + ACCENT_GOLD + "; -fx-font-family: Georgia;"
        );

        Label subtitle = new Label(
                "Esculpa as cavernas, poste o herói e o covil do dragão, e veja o algoritmo " +
                "de busca traçar uma trilha entre as rochas desabadas."
        );
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px;");

        ToggleGroup drawingGroup = new ToggleGroup();
        wallTool.setToggleGroup(drawingGroup);
        startTool.setToggleGroup(drawingGroup);
        goalTool.setToggleGroup(drawingGroup);
        wallTool.setSelected(true);
        styleToggle(wallTool);
        styleToggle(startTool);
        styleToggle(goalTool);

        ToggleGroup algorithmGroup = new ToggleGroup();
        aStarAlgorithmTool.setToggleGroup(algorithmGroup);
        greedyAlgorithmTool.setToggleGroup(algorithmGroup);
        aStarAlgorithmTool.setSelected(true);
        styleToggle(aStarAlgorithmTool);
        styleToggle(greedyAlgorithmTool);

        // Impede que o algoritmo selecionado fique "sem dono" ao clicar de novo nele.
        algorithmGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                oldToggle.setSelected(true);
            }
        });

        Button clearWallsButton = new Button("Desmoronar rochas");
        Button resetButton = new Button("Nova caverna");
        styleRestingAction(solveButton);
        styleRestingAction(clearWallsButton);
        styleRestingAction(resetButton);

        // O botão só acende em laranja enquanto o usuário o mantém pressionado.
        solveButton.pressedProperty().addListener((observable, wasPressed, isPressed) ->
                solveButton.setStyle(actionStyle(isPressed)));

        solveButton.setOnAction(event -> solve());
        clearWallsButton.setOnAction(event -> {
            if (!animating) {
                maze.clearWalls();
                refreshAll();
            }
        });
        resetButton.setOnAction(event -> {
            if (!animating) {
                maze.reset();
                refreshAll();
            }
        });

        HBox drawingTools = new HBox(8, wallTool, startTool, goalTool);
        drawingTools.setAlignment(Pos.CENTER_LEFT);

        HBox algorithmTools = new HBox(8, mutedLabel("Algoritmo:"), aStarAlgorithmTool, greedyAlgorithmTool);
        algorithmTools.setAlignment(Pos.CENTER_LEFT);

        HBox actionTools = new HBox(8, solveButton, clearWallsButton, resetButton);
        actionTools.setAlignment(Pos.CENTER_LEFT);

        HBox tools = new HBox(
                12,
                drawingTools,
                separator(),
                algorithmTools,
                separator(),
                actionTools
        );
        tools.setAlignment(Pos.CENTER_LEFT);

        VBox headerBox = new VBox(10, title, subtitle, tools);
        headerBox.setPadding(new Insets(0, 0, 14, 0));
        return headerBox;
    }

    private Node createGridContainer() {
        gridPane.setHgap(CELL_GAP);
        gridPane.setVgap(CELL_GAP);
        gridPane.setAlignment(Pos.CENTER);

        BorderPane container = new BorderPane(gridPane);
        container.setStyle(
                "-fx-background-color: " + PANEL_BG + "; " +
                "-fx-border-color: " + PANEL_BORDER + "; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 12;"
        );
        return container;
    }

    private void createGrid() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
                rect.setArcWidth(4);
                rect.setArcHeight(4);
                rect.setStroke(Color.web("#cbb98f"));
                rect.setFill(FLOOR_COLOR);

                final int r = row;
                final int c = col;

                rect.setOnMouseClicked(event -> {
                    if (animating) {
                        return;
                    }

                    Cell cell = new Cell(r, c);

                    if (wallTool.isSelected()) {
                        maze.setWall(cell, !maze.isWall(cell));
                        refreshCell(cell);
                    } else if (startTool.isSelected()) {
                        maze.setStart(cell);
                        refreshAll();
                    } else if (goalTool.isSelected()) {
                        maze.setGoal(cell);
                        refreshAll();
                    }
                });

                cells[row][col] = rect;
                gridPane.add(rect, col, row);
            }
        }
    }

    private void solve() {
        if (animating) {
            return;
        }

        refreshAll();

        String algorithmName = currentAlgorithmName();
        searchStartNanos = System.nanoTime();

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
                rect.setFill(EXPLORED_COLOR);
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
                rect.setFill(PATH_COLOR);
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
            double elapsedSeconds = (System.nanoTime() - searchStartNanos) / 1_000_000_000.0;
            showResultsScreen(algorithmName, result, countWalls(), elapsedSeconds);
        });

        sequence.play();
    }

    /**
     * Substitui toda a tela pelo resultado da caçada. O jogador só volta a
     * ver o labirinto clicando em "Continuar caçando".
     */
    private void showResultsScreen(String algorithmName, PathfindingResult result, int wallsCount, double elapsedSeconds) {
        Label title = new Label(result.found() ? "O dragão foi encurralado!" : "O dragão escapou...");
        title.setStyle(
                "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + ACCENT_GOLD + "; " +
                "-fx-font-family: Georgia;"
        );

        Label subtitle = new Label(
                result.found()
                        ? "O herói encontrou uma trilha segura até o covil."
                        : "Não existe caminho até o covil com as rochas atuais."
        );
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 14px;");

        VBox statsBox = new VBox(
                10,
                resultRow("Algoritmo", algorithmName),
                resultRow("Células exploradas", String.valueOf(result.exploredOrder().size())),
                resultRow("Passos no caminho", String.valueOf(Math.max(0, result.path().size() - 1))),
                resultRow("Rochas no mapa", String.valueOf(wallsCount)),
                resultRow("Tempo de busca", String.format(Locale.US, "%.1fs", elapsedSeconds))
        );
        statsBox.setPadding(new Insets(18));
        statsBox.setStyle(
                "-fx-background-color: " + PANEL_BG + "; " +
                "-fx-border-color: " + PANEL_BORDER + "; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10;"
        );
        statsBox.setMaxWidth(360);

        Button continueButton = new Button("Continuar caçando");
        Button quitButton = new Button("Encerrar jogo");
        styleRestingAction(continueButton);
        styleRestingAction(quitButton);

        continueButton.setOnAction(event -> {
            setTop(header);
            setCenter(gridContainer);
        });
        quitButton.setOnAction(event -> Platform.exit());

        HBox buttons = new HBox(12, continueButton, quitButton);
        buttons.setAlignment(Pos.CENTER);

        VBox resultsScreen = new VBox(20, title, subtitle, statsBox, buttons);
        resultsScreen.setAlignment(Pos.CENTER);
        resultsScreen.setPadding(new Insets(40));
        resultsScreen.setStyle("-fx-background-color: " + STONE_BG + ";");

        setTop(null);
        setCenter(resultsScreen);
    }

    private HBox resultRow(String label, String value) {
        Label caption = mutedLabel(label);
        Label valueLabel = new Label(value);
        valueLabel.setStyle(
                "-fx-text-fill: " + ACCENT_GOLD + "; -fx-font-weight: bold; -fx-font-size: 14px;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(caption, spacer, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
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
            rect.setFill(HERO_COLOR);
        } else if (cell.equals(maze.goal())) {
            rect.setFill(DRAGON_COLOR);
        } else if (maze.isWall(cell)) {
            rect.setFill(WALL_COLOR);
        } else {
            rect.setFill(FLOOR_COLOR);
        }
    }

    private int countWalls() {
        int count = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (maze.isWall(new Cell(row, col))) {
                    count++;
                }
            }
        }
        return count;
    }

    private String currentAlgorithmName() {
        return aStarAlgorithmTool.isSelected() ? "A*" : "Greedy Best-First";
    }

    private Label mutedLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px;");
        return label;
    }

    private Separator separator() {
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: " + PANEL_BORDER + ";");
        return separator;
    }

    private void styleToggle(ToggleButton button) {
        // Usa o estado atual (pode já vir selecionado, ex.: ferramenta padrão)
        // em vez de assumir "não selecionado" — evita o botão nascer apagado.
        button.setStyle(toggleStyle(button.isSelected()));
        button.selectedProperty().addListener((observable, wasSelected, isSelected) ->
                button.setStyle(toggleStyle(isSelected)));
    }

    private String toggleStyle(boolean selected) {
        String background = selected ? ACCENT_FIRE : PANEL_BG;
        String textColor = selected ? "#241a16" : TEXT_PRIMARY;
        String fontWeight = selected ? "bold" : "normal";
        return "-fx-background-color: " + background + "; " +
                "-fx-text-fill: " + textColor + "; " +
                "-fx-font-weight: " + fontWeight + "; " +
                "-fx-background-radius: 6; " +
                "-fx-border-color: " + PANEL_BORDER + "; " +
                "-fx-border-radius: 6; " +
                "-fx-padding: 6 12;";
    }

    /** Estilo padrão "em repouso" para botões de ação (sem laranja). */
    private void styleRestingAction(Button button) {
        button.setStyle(actionStyle(false));
    }

    private String actionStyle(boolean pressed) {
        String background = pressed ? ACCENT_FIRE : PANEL_BG;
        String textColor = pressed ? "#241a16" : TEXT_PRIMARY;
        return "-fx-background-color: " + background + "; " +
                "-fx-text-fill: " + textColor + "; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 6; " +
                "-fx-border-color: " + PANEL_BORDER + "; " +
                "-fx-border-radius: 6; " +
                "-fx-padding: 6 14;";
    }
}