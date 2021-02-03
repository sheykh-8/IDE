package sheykh;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;

public class FXRun extends Application {


    private static Stage stage;
    private static Font font;
    public static String pathPreFix = !System.getProperty("os.name").equals("Linux") ? ".." : "";

    @Override
    public void start(Stage primaryStage) throws Exception {
        Font.loadFont(getClass().getResourceAsStream("/fonts/Ubuntu-Regular.ttf"), 15);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Ubuntu-Bold.ttf"), 15);
        stage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource(pathPreFix + File.separator + "mainWindow.fxml"));
        primaryStage.setTitle("Text Editor");
        Scene scene = new Scene(root, 800, 500);
        scene.getStylesheets().add(Controller.class.getResource("/keywords-highlight.css").toExternalForm());
        scene.getStylesheets().add(Controller.class.getResource("/mainWindow.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
//        primaryStage.setMaximized(true);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(550);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }


    public static void changeTitle(String str) {
        stage.setTitle(str);
    }

    public static Font getFont() {
        return font;
    }
}
