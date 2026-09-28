package dev.ferreira.dragon.hunt.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;


final class Theme {

    static final String STONE_BG = "#1c1613";
    static final String PANEL_BG = "#241d19";
    static final String PANEL_BORDER = "#3a2f27";
    static final String TEXT_PRIMARY = "#f1e7d0";
    static final String TEXT_MUTED = "#b8a888";
    static final String ACCENT_FIRE = "#d9622b";
    static final String ACCENT_GOLD = "#c9a13b";

    static final Color FLOOR_COLOR = Color.web("#e8ddc7");
    static final Color WALL_COLOR = Color.web("#241a16");
    static final Color HERO_COLOR = Color.web("#4c8c5d");
    static final Color DRAGON_COLOR = Color.web("#8c2f2f");
    static final Color EXPLORED_COLOR = Color.web("#5b7fa6");
    static final Color PATH_COLOR = Color.web("#e8b74d");
    static final Color HUNTER_TRAIL_COLOR = Color.web("#d9a5a0");
    static final Color PREY_TRAIL_COLOR = Color.web("#a9cdb0");

    private Theme() {
    }

    static Label heading(String text, double fontSize) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-font-size: " + fontSize + "px; -fx-font-weight: bold; " +
                "-fx-text-fill: " + ACCENT_GOLD + "; -fx-font-family: Georgia;"
        );
        return label;
    }

    static Label subtitle(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px;");
        return label;
    }

    static Label mutedLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px;");
        return label;
    }

    static Separator separator() {
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: " + PANEL_BORDER + ";");
        return separator;
    }

    /** Aplica o estilo de toggle já refletindo o estado atual de seleção. */
    static void styleToggle(ToggleButton button) {
        button.setStyle(toggleStyle(button.isSelected()));
        button.selectedProperty().addListener((observable, wasSelected, isSelected) ->
                button.setStyle(toggleStyle(isSelected)));
    }

    static String toggleStyle(boolean selected) {
        return buttonStyle(selected, false, "6 12");
    }

    /** Aplica o estilo "em repouso" de um botão de ação (sem laranja). */
    static void styleRestingAction(Button button) {
        button.setStyle(actionStyle(false));
    }

    static String actionStyle(boolean highlighted) {
        return buttonStyle(highlighted, true, "6 14");
    }

    /**
     * Estilo compartilhado por toggles e botões de ação: laranja quando
     * destacado (selecionado ou pressionado), tom neutro em repouso.
     * {@code boldWhenIdle} controla se o texto fica em negrito mesmo fora
     * do estado destacado (usado pelos botões de ação).
     */
    private static String buttonStyle(boolean highlighted, boolean boldWhenIdle, String padding) {
        String background = highlighted ? ACCENT_FIRE : PANEL_BG;
        String textColor = highlighted ? "#241a16" : TEXT_PRIMARY;
        String fontWeight = highlighted || boldWhenIdle ? "bold" : "normal";
        return "-fx-background-color: " + background + "; " +
                "-fx-text-fill: " + textColor + "; " +
                "-fx-font-weight: " + fontWeight + "; " +
                "-fx-background-radius: 6; " +
                "-fx-border-color: " + PANEL_BORDER + "; " +
                "-fx-border-radius: 6; " +
                "-fx-padding: " + padding + ";";
    }
}
