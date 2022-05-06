package com.file_encryption.WindowClasses;

import com.file_encryption.Exceptions.BadKeyException;
import com.file_encryption.Utils.GlobalVariables;
import com.file_encryption.Utils.Testing;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private final GlobalVariables global = new GlobalVariables();

    public static void main() {
        launch();
    }

    @Override
    public void start(final Stage window) throws BadKeyException, IOException {
        //Testing.test();
        //if (true) Platform.exit();
        window.setTitle("file encrypter");

        final GridPane root = new GridPane();

        final GridPane sideMenu = new GridPane();
        SideMenu.createSideMenu(sideMenu, window,global);

        root.add(sideMenu, 0, 0, 10, 10);

        final GridPane fileViewer = new GridPane();
        createFileViewer(fileViewer, window);

        global.root.getChildren().add(root);

        final Scene scene = new Scene(global.root, global.su.sw * 40, global.su.sh * 40);

        window.setX(global.su.sw * 30);
        window.setY(global.su.sh * 25);
        window.setScene(scene);
        window.show();
    }

    private void createFileViewer(final GridPane fileViewer, final Stage window) {

    }
}
