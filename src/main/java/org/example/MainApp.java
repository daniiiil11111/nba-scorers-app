package org.example;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    private ToggleGroup featureGroup;
    private TextField inputField;
    private ComboBox<String> teamFilter;
    private ListView<Player> listView;
    private Label resultCount;
    private Button darkModeButton;
    private Scene scene;
    private boolean darkMode = false;
    private List<Player> top5;

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        top5 = Features.getTop5();

        HBox header = buildHeader();
        HBox tabBar = buildTabBar();
        HBox filterRow = buildFilterRow();
        listView = buildListView();
        resultCount = new Label(Features.getFullList().size() + " results");
        resultCount.getStyleClass().add("text-muted");

        VBox.setVgrow(listView, Priority.ALWAYS);
        VBox card = new VBox(18, tabBar, filterRow, listView, resultCount);
        card.setPadding(new Insets(28));
        card.getStyleClass().add("card");
        VBox.setVgrow(card, Priority.ALWAYS);

        VBox root = new VBox(26, header, card);
        root.setPadding(new Insets(36));

        scene = new Scene(root, 780, 680);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Scorers");
        stage.show();
    }

    private HBox buildHeader() {
        Label eyebrow = new Label("2025\u201326 SEASON");
        eyebrow.getStyleClass().add("eyebrow");
        Label title = new Label("Scorers");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Browse, search, and rank this season's top scorers");
        subtitle.getStyleClass().add("text-muted");
        VBox left = new VBox(6, eyebrow, title, subtitle);

        darkModeButton = new Button("\uD83C\uDF19");
        darkModeButton.getStyleClass().add("star-button");
        darkModeButton.setOnAction(e -> toggleDarkMode());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(left, spacer, darkModeButton);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private void toggleDarkMode() {
        darkMode = !darkMode;
        scene.getStylesheets().clear();
        if (darkMode) {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            scene.getStylesheets().add(getClass().getResource("/style-dark.css").toExternalForm());
            darkModeButton.setText("\u2600");
        } else {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            darkModeButton.setText("\uD83C\uDF19");
        }
        listView.refresh();
    }

    private HBox buildTabBar() {
        featureGroup = new ToggleGroup();
        String[] options = {"All", "Top 5", "Search", "By Rank", "Favorites"};
        HBox box = new HBox(28);
        box.getStyleClass().add("tab-bar");
        for (String opt : options) {
            ToggleButton tb = new ToggleButton(opt);
            tb.setToggleGroup(featureGroup);
            tb.setUserData(opt);
            box.getChildren().add(tb);
        }
        ((ToggleButton) box.getChildren().get(0)).setSelected(true);
        featureGroup.selectedToggleProperty().addListener((obs, old, val) -> onFeatureChanged());
        return box;
    }

    private HBox buildFilterRow() {
        inputField = new TextField();
        inputField.setPromptText("Player name");
        inputField.setDisable(true);
        HBox.setHgrow(inputField, Priority.ALWAYS);

        teamFilter = new ComboBox<>();
        teamFilter.getItems().add("All Teams");
        teamFilter.getItems().addAll(Features.getDistinctTeams());
        teamFilter.setValue("All Teams");
        teamFilter.setOnAction(e -> runQuery());

        Button runButton = new Button("Search");
        runButton.getStyleClass().add("primary-button");
        runButton.setDefaultButton(true);
        runButton.setOnAction(e -> runQuery());

        HBox row = new HBox(10, inputField, teamFilter, runButton);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private ListView<Player> buildListView() {
        ListView<Player> lv = new ListView<>();
        lv.setPlaceholder(new Label("No results found."));
        lv.setCellFactory(list -> new PlayerCell(this::runQuery, this::showProfile));
        lv.setItems(FXCollections.observableArrayList(Features.getFullList()));
        return lv;
    }

    private void onFeatureChanged() {
        String choice = (String) featureGroup.getSelectedToggle().getUserData();
        boolean needsInput = choice.equals("Search") || choice.equals("By Rank");
        inputField.setDisable(!needsInput);
        inputField.clear();
        inputField.setPromptText(choice.equals("By Rank")
                ? "Rank (1-" + Features.getFullList().size() + ")"
                : "Player name");
        runQuery();
    }

    private void runQuery() {
        String choice = (String) featureGroup.getSelectedToggle().getUserData();
        List<Player> result;
        switch (choice) {
            case "All": result = Features.getFullList(); break;
            case "Top 5": result = Features.getTop5(); break;
            case "Search": result = Features.searchForPlayer(inputField.getText()); break;
            case "By Rank":
                try {
                    result = Features.getRankedPlayer(Integer.parseInt(inputField.getText().trim()));
                } catch (NumberFormatException ex) {
                    result = List.of();
                }
                break;
            case "Favorites": result = Features.getFavorites(FavoritesManager.all()); break;
            default: result = List.of();
        }

        String team = teamFilter.getValue();
        if (!"By Rank".equals(choice) && team != null && !team.equals("All Teams")) {
            List<Player> filtered = new ArrayList<>();
            for (Player p : result) {
                if (p.getTeam().equals(team)) filtered.add(p);
            }
            result = filtered;
        }

        listView.setItems(FXCollections.observableArrayList(result));
        resultCount.setText(result.size() + (result.size() == 1 ? " result" : " results"));
    }

    private void showProfile(Player player) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle(player.getName());

        ImageView imageView = new ImageView();
        imageView.setFitWidth(220);
        imageView.setFitHeight(220);
        imageView.setPreserveRatio(true);
        boolean hasImage = false;
        try {
            var stream = getClass().getResourceAsStream(PlayerProfiles.imagePath(player));
            if (stream != null) {
                imageView.setImage(new Image(stream));
                hasImage = true;
            }
        } catch (Exception ignored) {
        }

        Region imageBox;
        if (hasImage) {
            imageBox = new StackPane(imageView);
        } else {
            Label initials = new Label(initialsFor(player.getName()));
            initials.getStyleClass().add("avatar-text");
            initials.setStyle("-fx-font-size: 48px;");
            StackPane circle = new StackPane(initials);
            circle.getStyleClass().add("avatar");
            circle.setMinSize(160, 160);
            circle.setMaxSize(160, 160);
            imageBox = circle;
        }

        Label name = new Label(player.getName());
        name.getStyleClass().add("page-title");
        name.setStyle("-fx-font-size: 20px;");

        Label rank = new Label("Rank #" + Features.getRankOf(player) + " in scoring this season");
        rank.getStyleClass().add("text-muted");

        Label stats = new Label(player.getTeam() + "  \u00b7  " + player.getGames() + " games  \u00b7  "
                + player.getPoints() + " points  \u00b7  "
                + String.format("%.1f", player.getPoints() / (double) player.getGames()) + " PPG");
        stats.getStyleClass().add("player-meta");

        VBox content = new VBox(14, imageBox, name, rank, stats);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));
        content.getStyleClass().add("card");

        Scene popupScene = new Scene(content, 320, 420);
        popupScene.getStylesheets().add(getClass().getResource(
                darkMode ? "/style-dark.css" : "/style.css").toExternalForm());
        popup.setScene(popupScene);
        popup.showAndWait();
    }

    private String initialsFor(String name) {
        String[] parts = name.trim().split("\\s+");
        String initials = parts.length > 1
                ? ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0))
                : parts[0].substring(0, Math.min(2, parts[0].length()));
        return initials.toUpperCase();
    }

    private class PlayerCell extends ListCell<Player> {
        private final HBox root;
        private final Label avatarText = new Label();
        private final StackPane avatar = new StackPane(avatarText);
        private final Label nameLabel = new Label();
        private final Label metaLabel = new Label();
        private final Label pointsLabel = new Label();
        private final Label ptsCaption = new Label("PTS");
        private final Button starButton = new Button();
        private final Hyperlink profileLink = new Hyperlink("Profile");
        private final Runnable onFavoriteToggled;
        private final java.util.function.Consumer<Player> onProfileClick;

        PlayerCell(Runnable onFavoriteToggled, java.util.function.Consumer<Player> onProfileClick) {
            this.onFavoriteToggled = onFavoriteToggled;
            this.onProfileClick = onProfileClick;

            avatar.getStyleClass().add("avatar");
            avatarText.getStyleClass().add("avatar-text");
            nameLabel.getStyleClass().add("player-name");
            metaLabel.getStyleClass().add("player-meta");
            profileLink.getStyleClass().add("profile-link");
            VBox nameBox = new VBox(3, nameLabel, metaLabel, profileLink);

            pointsLabel.getStyleClass().add("points-value");
            ptsCaption.getStyleClass().add("points-caption");
            VBox statsBox = new VBox(2, pointsLabel, ptsCaption);
            statsBox.setAlignment(Pos.CENTER_RIGHT);

            starButton.getStyleClass().add("star-button");
            starButton.setOnAction(e -> {
                Player p = getItem();
                if (p != null) {
                    FavoritesManager.toggle(p.getName());
                    updateStar(p);
                    onFavoriteToggled.run();
                }
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            root = new HBox(16, avatar, nameBox, spacer, statsBox, starButton);
            root.setAlignment(Pos.CENTER_LEFT);
            root.getStyleClass().add("player-row");
        }

        private void updateStar(Player p) {
            starButton.setText(FavoritesManager.isFavorite(p.getName()) ? "\u2605" : "\u2606");
        }

        @Override
        protected void updateItem(Player player, boolean empty) {
            super.updateItem(player, empty);
            if (empty || player == null) {
                setGraphic(null);
            } else {
                avatarText.setText(initialsFor(player.getName()));
                nameLabel.setText(player.getName());
                metaLabel.setText(player.getTeam() + "   \u00b7   " + player.getGames() + " GP");
                pointsLabel.setText(String.valueOf(player.getPoints()));
                updateStar(player);
                profileLink.setVisible(top5.contains(player));
                profileLink.setManaged(top5.contains(player));
                profileLink.setOnAction(e -> onProfileClick.accept(player));
                setGraphic(root);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}