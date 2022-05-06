package com.file_encryption.WindowClasses;

import com.file_encryption.EncryptionStuff.Encryptor;
import com.file_encryption.Utils.GlobalVariables;
import com.file_encryption.Utils.ScreenUnits;
import com.file_encryption.Utils.Utils;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class EncryptionKeyWindow {

    private static final ScreenUnits su = new ScreenUnits();

    public static byte[] getKeyWindow(boolean encryptMode, File currentlySelectedFile, GlobalVariables global) {
        final Stage window = new Stage();
        window.setTitle("enter key");
        final GridPane root = new GridPane();

        final short minPasswordLength = 2;

        final Label stringKey = new Label("enter password key: ");
        final PasswordField stringKeyInput = new PasswordField();
        stringKeyInput.setPromptText("password");
        final Button selectAsPassword = new Button((encryptMode?"encrypt":"decrypt")+" file");
        selectAsPassword.setDisable(true);

        final File[] keySaveLocation = new File[1];
        final Label useKeyFile = new Label("select key file: ");
        final TextField keyLocation = new TextField();
        keyLocation.setPromptText("key file location");
        final FileChooser keyChooser = new FileChooser();
        keyChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("KEY","*.key"));
        keyChooser.setTitle("select key file");
        Button browseKeyLocation = new Button("browse");

        final byte[][] key = new byte[1][];
        final Button encryptWithKeyFile = new Button((encryptMode?"encrypt":"decrypt")+" file");
        encryptWithKeyFile.setDisable(true);

        if (encryptMode) {
            final Label createKeyFileLabel = new Label("create new key file: ");
            final TextField keyFileLocationInput = new TextField();
            keyFileLocationInput.setPromptText("key file location");

            final Button createKeyFile = new Button("create key file");
            createKeyFile.setDisable(true);

            final DirectoryChooser keyFileLocation = new DirectoryChooser();
            final File[] fileLocation = new File[1];
            final Button browseKeyFileLocation = new Button("browse");
            browseKeyFileLocation.setOnAction(e -> {
                fileLocation[0] = keyFileLocation.showDialog(window);
                if (fileLocation[0] != null) {
                    keyFileLocationInput.setText(fileLocation[0].getAbsolutePath());
                    createKeyFile.setDisable(false);
                }
            });

            createKeyFile.setOnAction(e -> {
                key[0] = Encryptor.generateKey();
                System.out.println(Arrays.toString(key[0]));
                File file = new File(""+keyFileLocationInput.getText() + "/"+ Utils.replaceFileExtension(currentlySelectedFile.getName(),"key"));
                try {
                    int count = 0;
                    while (!file.createNewFile()) file = new File(keyFileLocationInput.getText() + "/("+(++count)+")"+Utils.replaceFileExtension(currentlySelectedFile.getName(),"key"));
                    try (RandomAccessFile writer = new RandomAccessFile(file,"rw")) {
                        writer.write(key[0]);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                window.close();
            });
            keyFileLocationInput.setOnKeyTyped(e -> {
                if (e.getCode()==KeyCode.ENTER) {
                    createKeyFile.fire();
                    e.consume();
                }
                createKeyFile.setDisable(!new File(keyFileLocationInput.getText()).isDirectory());
            });

            root.add(createKeyFileLabel, 1, 6);
            root.add(keyFileLocationInput, 2, 6);
            root.add(browseKeyFileLocation, 3, 6);
            root.add(createKeyFile, 4, 6);
        }

        selectAsPassword.setOnAction(e->{
            if (stringKeyInput.getLength()<minPasswordLength) {
                MessageBox.showError("password too short",false,global);
                return;
            }
            key[0] = stringKeyInput.getText().getBytes(StandardCharsets.UTF_8);
            window.close();
        });

        encryptWithKeyFile.setOnAction(e->{
            if (keySaveLocation[0]==null) {
                MessageBox.showError("no key file selected",false,global);
                return;
            }
            try (RandomAccessFile file = new RandomAccessFile(keySaveLocation[0],"r")) {
                key[0] = new byte[32];
                file.read(key[0]);
            } catch (IOException ignored) {
                MessageBox.showError("invalid key file",false,global);
            }
            window.close();
        });

        browseKeyLocation.setOnAction(e->{
            keySaveLocation[0] = keyChooser.showOpenDialog(window);
            if (keySaveLocation[0]==null) keyLocation.setText("");
            else {
                keyLocation.setText(keySaveLocation[0].getAbsolutePath());
                encryptWithKeyFile.setDisable(false);
            }
        });

        keyLocation.setOnKeyReleased(e -> {
            if (e.getCode()==KeyCode.ENTER) {
                encryptWithKeyFile.fire();
                e.consume();
            }
        });
        keyLocation.setOnKeyTyped(e -> {
            keySaveLocation[0] = new File(keyLocation.getText());
            if (keySaveLocation[0].isFile())
                encryptWithKeyFile.setDisable(false);
            else {
                keySaveLocation[0] = null;
                encryptWithKeyFile.setDisable(true);
            }
        });
        stringKeyInput.setOnKeyReleased(e -> {
            if (e.getCode()==KeyCode.ENTER) {
                selectAsPassword.fire();
                e.consume();
            }
        });
        stringKeyInput.setOnKeyTyped(e -> selectAsPassword.setDisable(stringKeyInput.getText().length()<minPasswordLength));


        root.setVgap(5);
        root.setHgap(5);

        root.add(stringKey,1,1);
        root.add(stringKeyInput,2,1);
        root.add(selectAsPassword,3,1,2,1);

        root.add(useKeyFile,1,4);
        root.add(keyLocation,2,4);
        root.add(browseKeyLocation,3,4);
        root.add(encryptWithKeyFile,4,4);

        Scene scene = new Scene(root,su.sw*40,su.sh*35);

        stringKeyInput.requestFocus();

        window.setOnCloseRequest(e->window.close());
        window.setScene(scene);
        window.setX(su.sw*30);
        window.setY(su.sh*32.5);
        window.showAndWait();
        return key[0];
    }
}
