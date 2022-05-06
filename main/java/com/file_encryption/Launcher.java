package com.file_encryption;

import com.file_encryption.EncryptionStuff.CommandLineStart;
import com.file_encryption.WindowClasses.Main;

public class Launcher {

    public static void main(String[] args) {
        if (args.length>0)
            CommandLineStart.start(args);
        else
            Main.main();
    }
}
