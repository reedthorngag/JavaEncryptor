package com.file_encryption.WindowClasses;

import com.file_encryption.EncryptionStuff.Encryptor;
import com.file_encryption.Exceptions.BadKeyException;
import com.file_encryption.Exceptions.IllegalEncryptionMode;
import com.file_encryption.Exceptions.InvalidFileName;
import com.file_encryption.Utils.GlobalVariables;
import com.file_encryption.Utils.Utils;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

public class SideMenu {

    public static void createSideMenu(final GridPane sideMenu, final Stage window, final GlobalVariables global) {
        sideMenu.setHgap(5);
        sideMenu.setVgap(5);

        final Button mode = new Button("encrypt mode");
        final int[] modes = new int[]{1, 2, 0};
        final HashMap<Integer, String> modeTextKey = new HashMap<>();
        modeTextKey.put(1, "encrypt");
        modeTextKey.put(2, "decrypt");
        modeTextKey.put(0, "auto detect");

        final TextField selectedFile = new TextField();

        final Button browseFolders = new Button("browse folders");

        final Button encryptDecryptFile = new Button("encrypt file");
        encryptDecryptFile.setDisable(true);

        mode.setOnAction(e -> {
            global.mode.set(modes[global.mode.get()]);
            mode.setText(modeTextKey.get(global.mode.get()) + " mode");
            if (global.currentlySelectedFile == null) encryptDecryptFile.setDisable(true);
            browseFolders.setDisable(global.mode.get() == 2);
            File file = new File(selectedFile.getText());
            if (global.mode.get()==1)
                encryptDecryptFile.setText("encrypt "+Utils.workOutMode(file).split(" ")[1]);
            else if (global.mode.get()==0)
                encryptDecryptFile.setText("encrypt "+Utils.workOutMode(file).split(" ")[1]);
            else
                encryptDecryptFile.setText(Utils.workOutMode(file));
            if (file.exists()) {
                global.currentlySelectedFile = file;
                encryptDecryptFile.setDisable(false);
                try {
                    if (mode.getText().equals("decrypt mode") && !Utils.verifyFile(file))
                        encryptDecryptFile.setDisable(true);
                } catch (IOException ignored) {encryptDecryptFile.setDisable(true);}
            } else encryptDecryptFile.setDisable(true);
        });

        encryptDecryptFile.setOnAction(e -> {
            try {
                boolean encryptMode = false;
                boolean fileEncrypted = Utils.verifyFile(global.currentlySelectedFile);
                if (global.mode.get() == 0) {
                    encryptMode = fileEncrypted;
                } else if (global.mode.get()==1) encryptMode = true;
                else if (!Utils.verifyFile(global.currentlySelectedFile)) {
                    MessageBox.showError("this file isn't encrypted!",false,global);
                    return;
                }
                byte[] key = EncryptionKeyWindow.getKeyWindow(encryptMode, global.currentlySelectedFile, global);
                if (key==null) return;
                Encryptor encryptor = new Encryptor(global.currentlySelectedFile,global);
                global.currentlySelectedFile = encryptor.init(key,encryptMode?"encrypt":"decrypt");
                selectedFile.setText(global.currentlySelectedFile.getAbsolutePath());
                if (encryptMode) selectedFile.setText(Utils.replaceFileExtension(selectedFile.getText(),"secure"));
                else selectedFile.setText(Utils.replaceFileExtension(selectedFile.getText(),global.currentlySelectedFile.getName().split("\\.")[1]));
                encryptDecryptFile.setText(Utils.workOutMode(global.currentlySelectedFile));
                if (global.mode.get()!=0) {
                    mode.setText(Utils.workOutMode(global.currentlySelectedFile).split(" ")[0] + " mode");
                    global.mode.set(Utils.workOutMode(global.currentlySelectedFile).split(" ")[0].equals("encrypt") ? 1 : 2);
                }
                browseFolders.setDisable(global.mode.get() == 2);
                System.out.println(Utils.workOutMode(global.currentlySelectedFile).split(" ")[0].equals("encrypt"));
            } catch (IOException ignored) {
                MessageBox.showError("error "+mode.getText().split(" ")[0]+"ing file",false,global);
            } catch (InvalidKeyException | BadKeyException ignored) {
                MessageBox.showError("invalid key",false,global);
            } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | BadPaddingException | InvalidKeySpecException | InvalidFileName | IllegalEncryptionMode | NoSuchPaddingException | IllegalBlockSizeException ex) {
                ex.printStackTrace();
            }
        });

        final Label selectFile = new Label("select file or folder: ");

        selectedFile.setOnKeyTyped(e -> {
            File file = new File(selectedFile.getText());
            if (file.exists()) {
                global.currentlySelectedFile = file;
                if (global.mode.get()==0)
                    encryptDecryptFile.setText(Utils.workOutMode(file));
                encryptDecryptFile.setDisable(false);
            } else {
                encryptDecryptFile.setDisable(true);
            }
        });

        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("select file to encrypt/decrypt");
        final Button browseFiles = new Button("browse files");
        browseFiles.setOnAction(e -> {
            if (global.currentlySelectedFile!=null)
                fileChooser.setInitialDirectory(global.currentlySelectedFile.getParentFile());
            if (global.mode.get() < 2) fileChooser.getExtensionFilters().clear();
            else fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SECURE", "*.secure"));
            File file = fileChooser.showOpenDialog(window);
            if (file==null) return;
            global.currentlySelectedFile = file;
            selectedFile.setText(global.currentlySelectedFile.getAbsolutePath());
            if (file.exists()) {
                if (global.mode.get()==0)
                    encryptDecryptFile.setText(Utils.workOutMode(file));
                encryptDecryptFile.setDisable(false);
            }
        });

        final DirectoryChooser folderChooser = new DirectoryChooser();
        folderChooser.setTitle("select folder to encrypt");
        browseFolders.setOnAction(e -> {
            if (global.currentlySelectedFile!=null)
                fileChooser.setInitialDirectory(global.currentlySelectedFile.getParentFile());
            File folder = folderChooser.showDialog(window);
            if (folder==null) return;
            global.currentlySelectedFile = folder;
            selectedFile.setText(global.currentlySelectedFile.getAbsolutePath());
            if (folder.exists()) {
                if (global.mode.get()==0)
                    encryptDecryptFile.setText(Utils.workOutMode(folder));
                encryptDecryptFile.setDisable(false);
            }
        });

        sideMenu.add(mode, 2, 2);
        sideMenu.add(selectFile, 1, 5);
        sideMenu.add(selectedFile, 2, 5);
        sideMenu.add(browseFiles, 3, 5);
        sideMenu.add(browseFolders,4,5);
        sideMenu.add(encryptDecryptFile, 3, 6,2,1);
    }
}
