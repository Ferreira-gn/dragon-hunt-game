package dev.ferreira.dragon.hunt.ui;

import dev.ferreira.dragon.hunt.AStarPathfinder;
import dev.ferreira.dragon.hunt.Cell;
import dev.ferreira.dragon.hunt.GreedyBestFirstPathfinder;
import dev.ferreira.dragon.hunt.MazeGrid;
import dev.ferreira.dragon.hunt.PathfindingResult;
import dev.ferreira.dragon.hunt.RockslideGenerator;

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
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public final class DragonHuntScreen extends BorderPane {

    private static final int ROWS = MazeBoard.ROWS;
    private static final int COLS = MazeBoard.COLS;

    // Ritmo da animação de busca, em milissegundos.
    private static final int EXPLORE_FRAME_MILLIS = 18;
    private static final int PATH_FRAME_MILLIS = 45;
    private static final int PATH_START_GAP_MILLIS = 60;

    private final Runnable onBackToHome;

    private final MazeGrid maze = new MazeGrid(ROWS, COLS);
    private final AStarPathfinder aStarPathfinder = new AStarPathfinder();
    private final GreedyBestFirstPathfinder greedyPathfinder = new GreedyBestFirstPathfinder();
    private final RockslideGenerator rockslideGenerator = new RockslideGenerator(aStarPathfinder);

    private final MazeBoard board = new MazeBoard(this::handleCellClick);

    private final ToggleButton wallTool = new ToggleButton("Rochas");
    private final ToggleButton startTool = new ToggleButton("Herói");
    private final ToggleButton goalTool = new ToggleButton("Dragão");
    private final ToggleButton aStarAlgorithmTool = new ToggleButton("A*");
    private final ToggleButton greedyAlgorithmTool = new ToggleButton("Greedy Best-First");
    private final Button solveButton = new Button("Iniciar caçada");
    private final Button rockslideButton = new Button("Deslizamento de rochas");

    private Node header;
    private Node gridContainer;

    private boolean animating;
    private long searchStartNanos;

    public DragonHuntScreen(Runnable onBackToHome) {
        this.onBackToHome = onBackToHome;

        setStyle("-fx-background-color: " + Theme.STONE_BG + ";");
        setPadding(new Insets(18));

        header = createHeader();
        gridContainer = board;

        setTop(header);
        setCenter(gridContainer);

        refreshAll();
    }

    // Cabeçalho: título, navegação, ferramentas de desenho, algoritmo e ações
    private Node createHeader() {
        VBox headerBox = new VBox(10, createTopBar(), createToolbar());
        headerBox.setPadding(new Insets(0, 0, 14, 0));
        return headerBox;
    }

    private Node createTopBar() {
        VBox titleBlock = new VBox(4, Theme.heading("Dragon Hunt", 30), createSubtitle());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button menuButton = new Button("Menu principal");
        Theme.styleRestingAction(menuButton);
        menuButton.setOnAction(event -> runIfIdle(onBackToHome));

        HBox topBar = new HBox(12, titleBlock, spacer, menuButton);
        topBar.setAlignment(Pos.TOP_LEFT);
        return topBar;
    }

    private Label createSubtitle() {
        return Theme.subtitle(
                "Esculpa as cavernas, poste o herói e o covil do dragão, e veja o algoritmo " +
                "de busca traçar uma trilha entre as rochas desabadas."
        );
    }

    private HBox createToolbar() {
        HBox tools = new HBox(
                12,
                createDrawingTools(),
                Theme.separator(),
                createAlgorithmTools(),
                Theme.separator(),
                createActionTools()
        );
        tools.setAlignment(Pos.CENTER_LEFT);
        return tools;
    }

    private HBox createDrawingTools() {
        ToggleGroup drawingGroup = new ToggleGroup();
        wallTool.setToggleGroup(drawingGroup);
        startTool.setToggleGroup(drawingGroup);
        goalTool.setToggleGroup(drawingGroup);
        wallTool.setSelected(true);

        Theme.styleToggle(wallTool);
        Theme.styleToggle(startTool);
        Theme.styleToggle(goalTool);

        HBox box = new HBox(8, wallTool, startTool, goalTool);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private HBox createAlgorithmTools() {
        ToggleGroup algorithmGroup = new ToggleGroup();
        aStarAlgorithmTool.setToggleGroup(algorithmGroup);
        greedyAlgorithmTool.setToggleGroup(algorithmGroup);
        aStarAlgorithmTool.setSelected(true);

        Theme.styleToggle(aStarAlgorithmTool);
        Theme.styleToggle(greedyAlgorithmTool);

        // Impede que o algoritmo selecionado fique "sem dono" ao clicar de novo nele.
        algorithmGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                oldToggle.setSelected(true);
            }
        });

        HBox box = new HBox(8, Theme.mutedLabel("Algoritmo:"), aStarAlgorithmTool, greedyAlgorithmTool);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private HBox createActionTools() {
        Button resetButton = new Button("Nova caverna");

        Theme.styleRestingAction(solveButton);
        Theme.styleRestingAction(rockslideButton);
        Theme.styleRestingAction(resetButton);

        // O botão de caçada só acende em laranja enquanto pressionado.
        solveButton.pressedProperty().addListener((observable, wasPressed, isPressed) ->
                solveButton.setStyle(Theme.actionStyle(isPressed)));

        solveButton.setOnAction(event -> solve());
        rockslideButton.setOnAction(event -> runIfIdle(this::triggerRockslide));
        resetButton.setOnAction(event -> runIfIdle(() -> {
            maze.reset();
            refreshAll();
        }));

        HBox box = new HBox(8, solveButton, rockslideButton, resetButton);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private void runIfIdle(Runnable action) {
        if (!animating) {
            action.run();
        }
    }

    // Grade do labirinto

    private void handleCellClick(Cell cell) {
        if (animating) {
            return;
        }

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
    }

    // Deslizamento de rochas (gera um novo padrão de paredes aleatório)
    private void triggerRockslide() {
        rockslideGenerator.generate(maze, ROWS, COLS);
        refreshAll();
    }

    // Busca e animação
    private void solve() {
        if (animating) {
            return;
        }

        refreshAll();

        String algorithmName = currentAlgorithmName();
        searchStartNanos = System.nanoTime();
        PathfindingResult result = runPathfinder();

        animating = true;
        playAnimation(buildAnimationFrames(result), () -> {
            animating = false;
            double elapsedSeconds = (System.nanoTime() - searchStartNanos) / 1_000_000_000.0;
            showResultsScreen(algorithmName, result, countWalls(), elapsedSeconds);
        });
    }

    private PathfindingResult runPathfinder() {
        return aStarAlgorithmTool.isSelected()
                ? aStarPathfinder.findPath(maze)
                : greedyPathfinder.findPath(maze);
    }

    private List<KeyFrame> buildAnimationFrames(PathfindingResult result) {
        List<KeyFrame> frames = new ArrayList<>();

        int exploredCount = 0;
        for (Cell cell : result.exploredOrder()) {
            if (isEndpoint(cell)) {
                continue;
            }
            frames.add(colorFrame(exploredCount * EXPLORE_FRAME_MILLIS, cell, Theme.EXPLORED_COLOR));
            exploredCount++;
        }

        int pathStart = exploredCount * EXPLORE_FRAME_MILLIS + PATH_START_GAP_MILLIS;
        int pathIndex = 0;
        for (Cell cell : result.path()) {
            if (isEndpoint(cell)) {
                continue;
            }
            frames.add(colorFrame(pathStart + pathIndex * PATH_FRAME_MILLIS, cell, Theme.PATH_COLOR));
            pathIndex++;
        }

        return frames;
    }

    private KeyFrame colorFrame(int delayMillis, Cell cell, Color color) {
        return new KeyFrame(
                Duration.millis(delayMillis),
                event -> board.paint(cell, color)
        );
    }

    private void playAnimation(List<KeyFrame> frames, Runnable onFinished) {
        SequentialTransition sequence = new SequentialTransition();

        if (frames.isEmpty()) {
            sequence.getChildren().add(new PauseTransition(Duration.millis(250)));
        } else {
            sequence.getChildren().add(new Timeline(frames.toArray(new KeyFrame[0])));
        }

        sequence.setOnFinished(event -> onFinished.run());
        sequence.play();
    }

    private boolean isEndpoint(Cell cell) {
        return cell.equals(maze.start()) || cell.equals(maze.goal());
    }

    // Tela de resultado

    /**
     * Substitui toda a tela pelo resultado da caçada. O jogador escolhe
     * continuar no mesmo labirinto, voltar ao menu principal ou encerrar.
     */
    private void showResultsScreen(String algorithmName, PathfindingResult result, int wallsCount, double elapsedSeconds) {
        VBox resultsScreen = new VBox(
                20,
                createResultTitle(result),
                createResultSubtitle(result),
                createResultStats(algorithmName, result, wallsCount, elapsedSeconds),
                createResultButtons()
        );
        resultsScreen.setAlignment(Pos.CENTER);
        resultsScreen.setPadding(new Insets(40));
        resultsScreen.setStyle("-fx-background-color: " + Theme.STONE_BG + ";");

        setTop(null);
        setCenter(resultsScreen);
    }

    private Label createResultTitle(PathfindingResult result) {
        return Theme.heading(result.found() ? "O dragão foi encurralado!" : "O dragão escapou...", 32);
    }

    private Label createResultSubtitle(PathfindingResult result) {
        return Theme.subtitle(
                result.found()
                        ? "O herói encontrou uma trilha segura até o covil."
                        : "Não existe caminho até o covil com as rochas atuais."
        );
    }

    private VBox createResultStats(String algorithmName, PathfindingResult result, int wallsCount, double elapsedSeconds) {
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
                "-fx-background-color: " + Theme.PANEL_BG + "; " +
                "-fx-border-color: " + Theme.PANEL_BORDER + "; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10;"
        );
        statsBox.setMaxWidth(360);
        return statsBox;
    }

    private HBox createResultButtons() {
        Button continueButton = new Button("Continuar caçando");
        Button menuButton = new Button("Menu principal");
        Button quitButton = new Button("Encerrar jogo");
        Theme.styleRestingAction(continueButton);
        Theme.styleRestingAction(menuButton);
        Theme.styleRestingAction(quitButton);

        continueButton.setOnAction(event -> {
            setTop(header);
            setCenter(gridContainer);
        });
        menuButton.setOnAction(event -> onBackToHome.run());
        quitButton.setOnAction(event -> Platform.exit());

        HBox buttons = new HBox(12, continueButton, menuButton, quitButton);
        buttons.setAlignment(Pos.CENTER);
        return buttons;
    }

    private HBox resultRow(String label, String value) {
        Label caption = Theme.mutedLabel(label);
        Label valueLabel = new Label(value);
        valueLabel.setStyle(
                "-fx-text-fill: " + Theme.ACCENT_GOLD + "; -fx-font-weight: bold; -fx-font-size: 14px;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(caption, spacer, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // Renderização da grade
    private void refreshAll() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                refreshCell(new Cell(row, col));
            }
        }
    }

    private void refreshCell(Cell cell) {
        if (cell.equals(maze.start())) {
            board.paint(cell, Theme.HERO_COLOR);
        } else if (cell.equals(maze.goal())) {
            board.paint(cell, Theme.DRAGON_COLOR);
        } else if (maze.isWall(cell)) {
            board.paint(cell, Theme.WALL_COLOR);
        } else {
            board.paint(cell, Theme.FLOOR_COLOR);
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
}
