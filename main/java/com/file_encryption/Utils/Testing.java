package com.file_encryption.Utils;

import com.file_encryption.EncryptionStuff.FolderEncryptor;
import com.file_encryption.Exceptions.BadKeyException;

import java.io.File;
import java.io.IOException;

public class Testing {
    public static void main(String[] args) throws IOException, BadKeyException {
    }

    public static void test() throws IOException, BadKeyException {
        File folder = new File("C:\\Code\\proguard-7.2.1");
        File errorFolder = new File("c:\\code\\errorsfolder");
        errorFolder.mkdir();
        File output = FolderEncryptor.encryptFolder(folder,errorFolder,new byte[]{23,19,-23,87});
        //FolderEncryptor.decryptFolder(output,new byte[]{23,19,-23,87});
    }
}