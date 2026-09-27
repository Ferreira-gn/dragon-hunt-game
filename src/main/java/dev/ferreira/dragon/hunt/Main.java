package dev.ferreira.dragon.hunt;

import dev.ferreira.dragon.hunt.ui.DragonHuntScreen;
import dev.ferreira.dragon.hunt.ui.HomeScreen;
import dev.ferreira.dragon.hunt.ui.WatchHuntScreen;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Ponto de entrada da aplicação. Mantém um único {@link BorderPane} como
 * "casca" da janela e troca o seu centro entre as três telas (Home, Dragon
 * Hunt e Visualizar a Caçada) conforme a navegação do jogador.
 */
public final class Main extends Application {

    private static final double WINDOW_WIDTH = 960;
    private static final double WINDOW_HEIGHT = 720;

    private final BorderPane root = new BorderPane();

    @Override
    public void start(Stage primaryStage) {
        showHome();

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setTitle("Dragon Hunt");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showHome() {
        root.setCenter(new HomeScreen(this::showDragonHunt, this::showWatchHunt));
    }

    private void showDragonHunt() {
        root.setCenter(new DragonHuntScreen(this::showHome));
    }

    private void showWatchHunt() {
        root.setCenter(new WatchHuntScreen(this::showHome));
    }

    public static void main(String[] args) {
        launch(args);
    }
}