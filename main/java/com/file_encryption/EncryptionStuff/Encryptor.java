package com.file_encryption.EncryptionStuff;

import com.file_encryption.Exceptions.BadKeyException;
import com.file_encryption.Exceptions.IllegalEncryptionMode;
import com.file_encryption.Exceptions.InvalidFileName;
import com.file_encryption.Utils.GlobalVariables;
import com.file_encryption.Utils.Utils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.HashMap;

public class Encryptor {

    private static final byte[] salt = "fg78ehs0rgb4".getBytes(StandardCharsets.UTF_8);
    private static final byte[] iv = new byte[]{-79,71,121,47,-104,-6,17,8,13,-121,91,38,-57,-71,-21,-82};
    private static final HashMap<String, Integer> modeKey = new HashMap<>();
    private final File file;
    private Cipher cipher;
    private int bufferSize;
    private int originalBufferSize;
    private final GlobalVariables global;

    static {
        modeKey.put("encrypt",0);
        modeKey.put("Encrypt",0);
        modeKey.put("e",0);
        modeKey.put("E",0);
        modeKey.put("decrypt",1);
        modeKey.put("Decrypt",1);
        modeKey.put("d",1);
        modeKey.put("D",1);
    }

    public Encryptor(File file,GlobalVariables global) throws IOException {
        this.file = file;
        this.bufferSize = Utils.workOutBestBufferSize(file);
        this.global = global;
    }

    public Encryptor(File file, int bufferSize, GlobalVariables global) {
        this.file = file;
        this.bufferSize = bufferSize;
        originalBufferSize = bufferSize;
        this.global = global;
    }

    public File init(byte[] key,String mode) throws IOException, InvalidFileName, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException, IllegalEncryptionMode, BadKeyException {
        if (modeKey.get(mode) == 0) {
            return encryptFile(file, key);
        }
        else if (modeKey.get(mode) == 1) {
            this.bufferSize = Utils.getBufferSize(this.file);
            return decryptFile(file, key);
        }
        else throw new IllegalEncryptionMode();
    }

    private File encryptFile(File fileToEncrypt,byte[] key) throws IOException, InvalidFileName, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        System.out.println("encrypting "+fileToEncrypt.getAbsolutePath());
        RandomAccessFile fileReader = new RandomAccessFile(fileToEncrypt.getAbsolutePath(), "rw");
        RandomAccessFile fileWriter = new RandomAccessFile(fileToEncrypt.getAbsolutePath(), "rw");
        byte[] buffer = new byte[bufferSize];
        initializeEncryptor(key);
        byte[] appendData = Utils.addFileExtensionAndPad(Utils.getFileExtension(fileToEncrypt.getName()),file.length(),bufferSize);
        long a = appendData.length;
        fileWriter.seek(file.length());
        fileWriter.write(appendData);
        fileReader.seek(0);
        fileWriter.seek(0);
        long totalReadBytes = 0;
        int oldProgress = 0;
        int progress;
        long fileSize = fileReader.length();
        while (totalReadBytes+bufferSize<=fileSize) {
            fileReader.read(buffer);
            fileWriter.write(cipher.update(buffer));
            totalReadBytes += bufferSize;
            if ((progress = (int)Math.round(totalReadBytes/(double)fileSize*100))!=oldProgress) {
                oldProgress = progress;
                System.out.print("\r"+progress+"%");
            }
        }
        System.out.print("\n");
        fileWriter.write(cipher.doFinal());
        fileWriter.write(GlobalVariables.fileSignature);
        fileWriter.write(GlobalVariables.compatibilityVersion);
        fileWriter.write(Utils.convertToBytes(bufferSize));
        fileReader.close();
        fileWriter.close();
        File newFileName = new File(Utils.replaceFileExtension(fileToEncrypt.getAbsolutePath(),"secure"));
        File oldFileName = new File(fileToEncrypt.getAbsolutePath());
        int count = 0;
        while (!oldFileName.renameTo(newFileName)) {
            newFileName = new File(Utils.replaceFileExtension(Utils.insertBeforeFileName(fileToEncrypt,"("+(++count)+")"), "secure"));
        }
        global.currentlySelectedFile = newFileName;
        return newFileName;
    }

    private File decryptFile(File fileToDecrypt,byte[] key) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, BadKeyException {
        System.out.println("decrypting "+fileToDecrypt.getAbsolutePath());
        RandomAccessFile fileReader = new RandomAccessFile(fileToDecrypt.getAbsolutePath(), "r");
        RandomAccessFile fileWriter = new RandomAccessFile(fileToDecrypt.getAbsolutePath(), "rw");
        byte[] buffer = new byte[bufferSize];
        initializeDecrypter(key);
        fileWriter.setLength(fileReader.length()-10);
        fileReader.seek(0);
        fileWriter.seek(0);
        long totalReadBytes = 0;
        int oldProgress = 0;
        int progress;
        long fileSize = fileReader.length();
        while (totalReadBytes+bufferSize<=fileSize) {
            fileReader.read(buffer);
            fileWriter.write(cipher.update(buffer));
            totalReadBytes += bufferSize;
            if ((progress = (int)Math.round(totalReadBytes/(double)fileSize*100))!=oldProgress) {
                oldProgress = progress;
                System.out.print("\r"+progress+"%");
            }
        }
        System.out.print("\n");
        fileWriter.write(cipher.doFinal());
        byte[] fileData = new byte[3];
        fileReader.seek(fileReader.length()-3);
        fileReader.read(fileData);
        byte[] fileExtensionRaw = new byte[fileData[0]+129];
        fileReader.seek(fileReader.length()-(fileData[0]+129)-3);
        fileReader.read(fileExtensionRaw);
        fileWriter.setLength(fileReader.length()-Utils.convertToInt(Arrays.copyOfRange(fileData,1,3)));
        String fileExtension = new String(fileExtensionRaw,StandardCharsets.UTF_8);
        fileReader.close();
        fileWriter.close();
        File newFileName = new File(fileToDecrypt.getParent()+"\\"+Utils.stripFileNumber(Utils.replaceFileExtension(fileToDecrypt.getName(),fileExtension)));
        File oldFileName = new File(fileToDecrypt.getAbsolutePath());
        int count = 0;
        while (!oldFileName.renameTo(newFileName)) {
            newFileName = new File(Utils.replaceFileExtension(Utils.insertBeforeFileName(fileToDecrypt,"("+(++count)+")"), fileExtension));
        }
        if (originalBufferSize!=bufferSize) bufferSize = originalBufferSize;
        global.currentlySelectedFile = newFileName;
        if (fileExtension.equals("folder")) return FolderEncryptor.decryptFolder(newFileName,key);
        return newFileName;
    }

    public static byte[] generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            return keyGen.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }

    private void initializeEncryptor(byte[] key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec((new String(key)).toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(iv));
    }

    private void initializeDecrypter(byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec((new String(key)).toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        cipher = Cipher.getInstance("AES/CBC/NoPadding"); // /PKCS5Padding
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
    }
}
