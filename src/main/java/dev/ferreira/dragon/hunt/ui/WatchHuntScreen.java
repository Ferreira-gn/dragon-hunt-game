package dev.ferreira.dragon.hunt.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;


public final class WatchHuntScreen extends VBox {

    public WatchHuntScreen(Runnable onBackToHome) {
        setAlignment(Pos.CENTER);
        setSpacing(18);
        setPadding(new Insets(40));
        setStyle("-fx-background-color: " + Theme.STONE_BG + ";");

        Label title = Theme.heading("Em construção", 28);

        Label description = Theme.subtitle(
                "A visualização de caçadas registradas por outros aventureiros ainda está sendo " +
                "forjada nas forjas do reino. Volte em breve."
        );
        description.setMaxWidth(420);
        description.setAlignment(Pos.CENTER);
        description.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button backButton = new Button("Voltar ao menu");
        Theme.styleRestingAction(backButton);
        backButton.setOnAction(event -> onBackToHome.run());

        getChildren().addAll(title, description, backButton);
    }
}