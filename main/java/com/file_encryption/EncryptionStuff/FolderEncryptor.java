package com.file_encryption.EncryptionStuff;

import com.file_encryption.Exceptions.BadKeyException;
import com.file_encryption.Exceptions.IllegalEncryptionMode;
import com.file_encryption.Exceptions.InvalidFileName;
import com.file_encryption.Utils.GlobalVariables;
import com.file_encryption.Utils.Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class FolderEncryptor {

    public static byte[] folderSignature = new byte[]{-128,-106,58,-76,80};

    public static File encryptFolder(File folder,File errorFolder,byte[] key) throws IOException {
        File[] files = folder.listFiles();

        if (files == null) return new File("");

        for (File f:files) {
            if (f.isDirectory()) {
                encryptFolder(f,errorFolder,key);
            } else {
                encryptFile(f,errorFolder,key);
            }
        }
        File outputFile = new File(folder.getAbsolutePath()+".folder");
        int count = 0;
        while (outputFile.isFile()) {
            outputFile = new File(folder.getAbsolutePath()+" ("+(++count)+").folder");
        }
        if (!outputFile.createNewFile()) throw new IOException();

        files = folder.listFiles();
        try (RandomAccessFile file = new RandomAccessFile(outputFile,"rw")) {
            for (File fName:files) {
                file.write(generateFileHeader(fName));
                try (RandomAccessFile f = new RandomAccessFile(fName,"r")) {
                    byte[] buffer = new byte[8192];
                    long bytesTransferred = 0;
                    long totalBytes = f.length();
                    while ((bytesTransferred+=8192)<totalBytes) {
                        f.read(buffer);
                        file.write(buffer);
                    }
                    bytesTransferred-=8192;
                    buffer = new byte[(int)(totalBytes-bytesTransferred)];
                    f.read(buffer);
                    file.write(buffer);
                }
                fName.delete();
            }
            file.write(new byte[]{-1});
        } catch (NullPointerException ignored) {}

        folder.delete();
        return encryptFile(outputFile,errorFolder,key);
    }

    private static File encryptFile(File file,File errorFolder, byte[] key) {
        try {
            Encryptor encryptor = new Encryptor(file, new GlobalVariables());
            return encryptor.init(key,"encrypt");
        } catch (BadKeyException | IOException | InvalidAlgorithmParameterException | InvalidFileName | NoSuchPaddingException | IllegalEncryptionMode | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeySpecException | InvalidKeyException ioe) {
            try {
                Files.move(file.toPath(), Paths.get(errorFolder.getAbsolutePath() + file.getName()));
            } catch (IOException e) {
                System.out.println("\u001B[31merror encrypting file '"+file.getAbsolutePath()+"'\u001B[0m");
            }
            return null;
        }
    }

    private static byte[] generateFileHeader(File fileName) throws IOException {
        byte[] fileSize;
        try (RandomAccessFile file = new RandomAccessFile(fileName,"r")) {
            fileSize = Utils.convertToBytes(file.length());
        }
        byte[] fileNameBytes = fileName.getName().getBytes(StandardCharsets.UTF_8);

        byte[] output = new byte[fileNameBytes.length+11];
        output[5] = (byte)(fileNameBytes.length-128);

        System.arraycopy(folderSignature,0,output,0,5);
        System.arraycopy(fileNameBytes,0,output,6,fileNameBytes.length);
        System.arraycopy(fileSize, 0,output,fileNameBytes.length+6,5);

        return output;
    }

    public static File decryptFolder(File encryptedFolder,byte[] key) throws IOException, BadKeyException {
        if (!Utils.verifyFolder(encryptedFolder)&&Utils.verifyFile(encryptedFolder)) {
            return decryptFile(encryptedFolder,key);
        }

        File decryptedFolder = new File(encryptedFolder.getAbsolutePath().split("\\.")[0]);
        decryptedFolder.mkdir();

        try (RandomAccessFile file = new RandomAccessFile(encryptedFolder,"r")) {

            byte[] signature = new byte[5];
            byte[] filename;
            byte[] fileSize = new byte[5];
            byte[] fileNameLength = new byte[1];
            long size;

            file.read(signature);
            while (signature[0]!=-1&&Arrays.equals(signature, GlobalVariables.folderSignature)) {
                file.read(fileNameLength);
                filename = new byte[(int)fileNameLength[0]+128];
                file.read(filename);
                File f = new File(decryptedFolder.getAbsolutePath()+"\\"+new String(filename,StandardCharsets.UTF_8));
                f.createNewFile();
                file.read(fileSize);
                size = Utils.convertToLong(fileSize);
                try (RandomAccessFile subFile = new RandomAccessFile(f,"rw")) {
                    byte[] buffer = new byte[(int) Math.max(16, Math.min(file.length() / 256 * 16, 8192))];
                    long totalWrittenBytes = 0;

                    while (totalWrittenBytes + buffer.length < size) {
                        file.read(buffer);
                        subFile.write(buffer);
                        totalWrittenBytes += buffer.length;
                    }

                    buffer = new byte[(int) (size - totalWrittenBytes)];
                    file.read(buffer);
                    subFile.write(buffer);
                }
                Encryptor encryptor = new Encryptor(f,new GlobalVariables());
                encryptor.init(key,"decrypt");
                file.read(signature);
            }
        }catch (InvalidAlgorithmParameterException | InvalidFileName | IllegalEncryptionMode | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeySpecException | InvalidKeyException e) {e.printStackTrace();}
        encryptedFolder.delete();
        return decryptedFolder;
    }

    private static File decryptFile(File file, byte[] key) throws BadKeyException {
        try {
            Encryptor encryptor = new Encryptor(file, new GlobalVariables());
            return encryptor.init(key,"decrypt");
        } catch (IOException | InvalidAlgorithmParameterException | InvalidFileName | NoSuchPaddingException | IllegalEncryptionMode | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeySpecException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }
}
