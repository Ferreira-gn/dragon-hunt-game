package dev.ferreira.dragon.hunt;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Main extends Application {

  @Override
  public void start(Stage stage) {
    MazeView view = new MazeView();

    Scene scene = new Scene(view, 980, 760);
    stage.setTitle("A* Maze Solver");
    stage.setMinWidth(820);
    stage.setMinHeight(650);
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
