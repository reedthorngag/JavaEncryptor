package com.file_encryption.Utils;

import com.file_encryption.Exceptions.InvalidFileName;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Utils {

    public static byte[] getFileExtension(String file) throws InvalidFileName {
        char[] name = file.toCharArray();
        for (int i = name.length;i>0;i--) if (name[i-1]==".".charAt(0)) return file.substring(i,name.length).getBytes(StandardCharsets.UTF_8);
        return new byte[]{};
    }

    public static byte[] addFileExtensionAndPad(byte[] extension,long lengthOfDataToPad,int bufferSize) {
        lengthOfDataToPad += extension.length+3;
        int amountToAdd = (int)(bufferSize-(lengthOfDataToPad%bufferSize))+extension.length+3;
        byte[] data = new byte[amountToAdd];
        data[data.length-3] = (byte)(extension.length-129);
        System.arraycopy(convertToBytes(amountToAdd),0,data,data.length-2,2);
        System.arraycopy(extension,0,data,data.length-extension.length-3,extension.length);
        return data;
    }

    public static String insertBeforeFileName(File file, String insert) {
        return file.getAbsolutePath().replace(file.getName(),insert+file.getName());
    }

    public static String replaceFileExtension(String filePath,String newExtension) {
        char[] path = filePath.toCharArray();
        for (int i = path.length;i>0;i--) if (path[i-1]==".".charAt(0)) return filePath.substring(0,i)+newExtension;
        return "";
    }

    public static byte[] convertToBytes(int num) {
        return new byte[]{(byte)(num%256-128),(byte)((num/256)%256-128)};
    }

    public static int convertToInt(byte[] num) {
        return num[0]+128+((num[1]+128)*256);
    }

    public static byte[] convertToBytes(long num) {
        return new byte[]{(byte)(num%256-128),(byte)((num/256)%256-128),(byte)((num/65536)%256-128),(byte)((num/16777216)%256-128),(byte)(num/4294967296L-128)};
    }

    public static long convertToLong(byte[] num) {
        return num[0]+128+((num[1]+128)*256)+((num[2]+128)*256*256)+(((long)num[3]+128)*256*256*256)+(((long)num[4]+128)*256*256*256*256);
    }

    public static int workOutBestBufferSize(File fileName) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(fileName,"r")) {
            return (int) Math.max(16,Math.min(file.length()/256*16,8192));
        }
    }

    public static boolean verifyFile(File fileName) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(fileName,"r")) {
            byte[] fileStuff = new byte[6];
            file.seek(file.length()-10);
            file.read(fileStuff);
            if (Arrays.equals(GlobalVariables.fileSignature,fileStuff)) return true;
        } catch (IOException ignored) {}
        return false;
    }

    public static boolean verifyFolder(File fileName) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(fileName,"r")) {
            byte[] fileStuff = new byte[5];
            file.read(fileStuff);
            if (Arrays.equals(GlobalVariables.folderSignature,fileStuff)) return true;
        } catch (IOException ignored) {}
        return false;
    }

    public static boolean checkVersionCompatibility(File fileName) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(fileName,"r")) {
            byte[] fileStuff = new byte[2];
            file.seek(file.length()-4);
            file.read(fileStuff);
            if (Arrays.equals(GlobalVariables.fileSignature,GlobalVariables.compatibilityVersion)) return true;
        } catch (IOException ignored) {}
        return false;
    }

    public static int getBufferSize(File fileName) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(fileName,"r")) {
            byte[] bufferSize = new byte[2];
            file.seek(file.length()-2);
            file.read(bufferSize);
            return convertToInt(bufferSize);
        }
    }

    public static String workOutMode(File file) {
        try {
            return file.isDirectory()?"encrypt folder":verifyFile(file)?"decrypt file":"encrypt file";
        } catch (IOException e) {
            return "error";
        }
    }

    public static String stripFileNumber(String filename) {
        if (filename.startsWith("(")) {
            try {
                for (int n = 2; n < filename.length(); n++) {
                    if (filename.charAt(n)==")".charAt(0)) {
                        System.out.println(filename.substring(1,n));
                        System.out.println(filename.substring(n+1));
                        Integer.parseInt(filename.substring(1,n));
                        return filename.substring(n+1);
                    }
                }
            } catch (NumberFormatException e) {return filename;}
        }
        return filename;
    }
}
