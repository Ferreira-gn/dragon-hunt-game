package dev.ferreira.dragon.hunt.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public final class HomeScreen extends VBox {

    public HomeScreen(Runnable onPlayDragonHunt, Runnable onWatchHunt) {
        setAlignment(Pos.CENTER);
        setSpacing(22);
        setPadding(new Insets(40));
        setStyle("-fx-background-color: " + Theme.STONE_BG + ";");

        Label title = Theme.heading("Dragon Hunt", 42);

        Label description = Theme.subtitle(
                "Nas profundezas da caverna vive um dragão adormecido. Esculpa a rocha, " +
                "posicione o herói e trace uma trilha segura entre os desabamentos até o covil — " +
                "ou acompanhe caçadas já registradas por outros aventureiros."
        );
        description.setMaxWidth(460);
        description.setStyle(description.getStyle() + " -fx-font-size: 14px;");
        description.setAlignment(Pos.CENTER);
        description.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button playButton = new Button("Caçar o Dragão");
        Button watchButton = new Button("Visualizar uma Caçada");
        Theme.styleRestingAction(playButton);
        Theme.styleRestingAction(watchButton);

        playButton.setOnAction(event -> onPlayDragonHunt.run());
        watchButton.setOnAction(event -> onWatchHunt.run());

        HBox buttons = new HBox(16, playButton, watchButton);
        buttons.setAlignment(Pos.CENTER);

        Label watchHint = Theme.mutedLabel("A visualização de caçadas alheias chega em breve.");
        watchHint.setStyle(watchHint.getStyle() + " -fx-font-style: italic;");

        getChildren().addAll(title, description, buttons, watchHint);
    }
}