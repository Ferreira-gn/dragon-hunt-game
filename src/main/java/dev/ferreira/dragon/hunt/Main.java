package dev.ferreira.dragon.hunt;

import dev.ferreira.dragon.hunt.ui.DragonHuntScreen;
import dev.ferreira.dragon.hunt.ui.HomeScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


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
    root.setCenter(new HomeScreen(this::showDragonHunt));
  }

  private void showDragonHunt() {
    root.setCenter(new DragonHuntScreen(this::showHome));
  }

  public static void main(String[] args) {
    launch(args);
  }
}
