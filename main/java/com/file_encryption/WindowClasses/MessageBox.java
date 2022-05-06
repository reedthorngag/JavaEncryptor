package com.file_encryption.WindowClasses;

import com.file_encryption.Utils.GlobalVariables;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MessageBox extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        showError("incorrect parameters",false,new GlobalVariables());
    }

    protected static void showErrorBox(Node node, String error, GlobalVariables global) {
        if (global.errorShowing) MessageBox.hideLocalErrorBox(global);
        final Bounds nodePosition = node.localToScene(node.getBoundsInLocal());
        global.errorShowing = true;
        MessageBox.showLocalErrorBox(error, nodePosition.getMinX(), nodePosition.getMaxY(),global);
        PauseTransition pause = new PauseTransition(Duration.millis(3000));
        pause.setOnFinished(f -> {
            if (global.errorShowing) {
                MessageBox.hideLocalErrorBox(global);
                global.errorShowing = false;
            }
        });
        pause.play();
    }

    protected static void addInfoMessageOnHover(Node node, String infoMessage, GlobalVariables global){
        final String nodeID = node.toString();
        global.mouseHovering.put(nodeID,new Boolean[]{false,false});
        final Bounds nodePosition = node.localToScene(node.getBoundsInLocal());
        final PauseTransition pause = new PauseTransition(Duration.millis(700));
        pause.setOnFinished(e->{
            if (global.mouseHovering.get(nodeID)[0]) {
                global.mouseHovering.get(nodeID)[1]=true;
                showInfoBox(infoMessage,nodePosition.getMinX(),nodePosition.getMaxY(),global);
            }
        });
        node.setOnMouseEntered(e->{
            global.mouseHovering.get(nodeID)[0]=true;
            pause.play();
        });
        node.setOnMouseExited(e->{
            if (global.mouseHovering.get(nodeID)[1]) hideInfoBox(global);
            global.mouseHovering.get(nodeID)[0]=false;
            global.mouseHovering.get(nodeID)[1]=false;
        });
        node.setOnMouseClicked(e->{
            if (global.mouseHovering.get(nodeID)[1]) hideInfoBox(global);
            global.mouseHovering.get(nodeID)[0]=false;
            global.mouseHovering.get(nodeID)[1]=false;
        });
    }

    public static boolean showError(String errorMessage, boolean giveOption, GlobalVariables global) {
        Stage errorWindow = new Stage();
        errorWindow.setTitle("Error");
        GridPane root = new GridPane();
        root.setHgap(global.su.sw*2);
        root.setVgap(global.su.sh*2);
        Font buttonTextFont = new Font(global.su.av*1.5);
        Label title = new Label("Error");
        title.setFont(new Font(global.su.av*3));
        Label error = new Label(errorMessage);
        error.setFont(new Font(global.su.av*1.5));
        error.setTextFill(Paint.valueOf("red"));
        error.setMaxWidth(global.su.sw*23);
        error.setWrapText(true);
        ScrollPane errorView = new ScrollPane();
        errorView.setPrefSize(global.su.sw*30,global.su.sh*28);
        errorView.setPadding(new Insets(global.su.sh*1.5,global.su.sw*1.5,global.su.sh*1.5,global.su.sw*1.5));
        errorView.setStyle("-fx-border-color:rgb(150,150,150);");
        errorView.setContent(error);
        Button ok = new Button("ok");
        ok.setFont(buttonTextFont);
        ok.setPadding(new Insets(global.su.sh,global.su.sh*2,global.su.sh,global.su.sh*2));
        ok.setOnAction(e->errorWindow.close());
        Boolean[] returnOnClose = new Boolean[]{true};
        Button close = new Button("cancel");
        close.setFont(buttonTextFont);
        close.setOnAction(e->{
            returnOnClose[0] = false;
            errorWindow.close();
        } );
        Button ignore = new Button("continue");
        ignore.setOnAction(e->errorWindow.close());
        ignore.setFont(buttonTextFont);
        Label padding = new Label();
        padding.setPadding(new Insets(0,0,0,global.su.sh*10));
        root.add(title,3,1);
        root.add(errorView,2,2,4,1);
        if (giveOption) {
            root.add(close,3,3);
            root.add(padding,4,3);
            root.add(ignore,5,3);
        } else {
            root.add(ok, 3, 3);
        }
        errorWindow.setScene(new Scene(root,global.su.sw*40,global.su.sh*48));
        errorWindow.setX(global.su.sw*28.5);
        errorWindow.setY(global.su.sh*26);
        if (giveOption) errorWindow.showAndWait();
        else errorWindow.show();
        return returnOnClose[0];
    }

    protected static void showInfoBox(String info,double x,double y, GlobalVariables global) {
        global.infoBox.setLayoutX(x);
        global.infoBox.setLayoutY(y);
        Label text = new Label(info);
        text.setPadding(new Insets(3,0,0,3));
        global.infoBox.getChildren().add(text);
        FadeTransition fadeIn = new FadeTransition();
        fadeIn.setToValue(1);
        fadeIn.setFromValue(0);
        fadeIn.durationProperty().setValue(Duration.millis(200));
        fadeIn.setNode(global.infoBox);
        fadeIn.play();
    }

    protected static void showLocalErrorBox(String error, double x, double y,GlobalVariables global) {
        global.errorBox.setLayoutX(x);
        global.errorBox.setLayoutY(y);
        Label text = new Label(error);
        text.setPadding(new Insets(3,0,0,3));
        text.setTextFill(Paint.valueOf("red"));
        global.errorBox.getChildren().clear();
        global.errorBox.getChildren().add(text);
        FadeTransition fadeIn = new FadeTransition();
        fadeIn.setToValue(1);
        fadeIn.setFromValue(0);
        fadeIn.durationProperty().setValue(Duration.millis(200));
        fadeIn.setNode(global.errorBox);
        fadeIn.play();
    }

    protected static void hideInfoBox(GlobalVariables global) {
        FadeTransition fadeOut = new FadeTransition();
        fadeOut.setToValue(0);
        fadeOut.setFromValue(1);
        fadeOut.durationProperty().setValue(Duration.millis(200));
        fadeOut.setNode(global.infoBox);
        fadeOut.play();
        fadeOut.setOnFinished(e->global.infoBox.getChildren().clear());
    }

    protected static void hideLocalErrorBox(GlobalVariables global) {
        FadeTransition fadeOut = new FadeTransition();
        fadeOut.setToValue(0);
        fadeOut.setFromValue(1);
        fadeOut.durationProperty().setValue(Duration.millis(200));
        fadeOut.setNode(global.errorBox);
        fadeOut.play();
    }
}
