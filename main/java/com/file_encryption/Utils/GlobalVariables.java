package com.file_encryption.Utils;

import javafx.scene.layout.Pane;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalVariables {

    public final ScreenUnits su = new ScreenUnits();
    public final AtomicInteger mode = new AtomicInteger(1); // 1 == encrypt, 2 == decrypt, 3 == autodetect
    public File currentlySelectedFile = null;
    public final Pane root = new Pane();
    public final HashMap<String, Boolean[]> mouseHovering = new HashMap<>();
    public final Pane infoBox = new Pane();
    public final Pane errorBox = new Pane();
    public boolean errorShowing = false;
    public static final byte[] fileSignature = new byte[]{76,-66,-54,49,77,-22};
    public static byte[] folderSignature = new byte[]{-128,-106,58,-76,80};
    public static byte[] compatibilityVersion = new byte[]{-128,-128};// byte range: -128 - 127
    public static final String version = "0.0.01";
}
