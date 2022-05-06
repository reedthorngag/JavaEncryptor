package com.file_encryption.Utils;

import javafx.stage.Screen;

public class ScreenUnits {

    public int sw;
    public int sh;
    public int av;

    {
        sw = (int)Math.round(Screen.getPrimary().getBounds().getWidth()/100);
        sh = (int)Math.round(Screen.getPrimary().getBounds().getHeight()/100);
        av = (int)Math.round((sw+sh)/(double)2);
    }

    protected void updateScreenUnits() {
        sw = (int)Math.round(Screen.getPrimary().getBounds().getWidth()/100);
        sh = (int)Math.round(Screen.getPrimary().getBounds().getHeight()/100);
        av = (int)Math.round((sw+sh)/(double)2);
    }
}
