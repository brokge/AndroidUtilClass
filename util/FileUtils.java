package com.dxy.android.statistics.util;

import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.dxy.android.statistics.DXYStatisticsConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.zip.Checksum;

/**
 * Utils for dealing with files.
 * chenlw@dxyer.com
 * Created by chenlw on 2015/6/9.
 */
public class FileUtils {
    public static final int _1KB = 1024;
    public static final int _1MB = _1KB * _1KB;
    public static final int _1GB = _1KB * _1MB;

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }


    public static byte[] readBytes(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);
        return IoUtils.readAllBytesAndClose(is);
    }

    public static void writeBytes(File file, byte[] content) throws IOException {
        OutputStream out = new FileOutputStream(file);
        try {
            out.write(content);
        } finally {
            IoUtils.safeClose(out);
        }
    }

    public static String readUtf8(File file) throws IOException {
        return readChars(file, "UTF-8");
    }

    public static String readChars(File file, String charset) throws IOException {
        Reader reader = new InputStreamReader(new FileInputStream(file), charset);
        return IoUtils.readAllCharsAndClose(reader);
    }

    public static void writeUtf8(File file, CharSequence text) throws IOException {
        writeChars(file, "UTF-8", text);
    }

    public static void writeChars(File file, String charset, CharSequence text) throws IOException {
        Writer writer = new OutputStreamWriter(new FileOutputStream(file), charset);
        IoUtils.writeAllCharsAndClose(writer, text);
    }

    /**
     * Copies a file to another location.
     */
    public static void copyFile(File from, File to) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(from));
        try {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(to));
            try {
                IoUtils.copyAllBytes(in, out);
            } finally {
                IoUtils.safeClose(out);
            }
        } finally {
            IoUtils.safeClose(in);
        }
    }

    /**
     * Copies a file to another location.
     */
    public static void copyFile(String fromFilename, String toFilename) throws IOException {
        copyFile(new File(fromFilename), new File(toFilename));
    }

    /**
     * To read an object in a quick & dirty way. Prepare to handle failures when object serialization changes!
     */
    public static Object readObject(File file) throws IOException,
            ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(file);
        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(fileIn));
        try {
            return in.readObject();
        } finally {
            IoUtils.safeClose(in);
        }
    }

    /**
     * To store an object in a quick & dirty way.
     */
    public static void writeObject(File file, Object object) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(file);
        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(fileOut));
        try {
            out.writeObject(object);
            out.flush();
            // Force sync
            fileOut.getFD().sync();
        } finally {
            IoUtils.safeClose(out);
        }
    }

    /**
     * @return MD5 digest (32 characters).
     */
    public static String getMd5(File file) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            return IoUtils.getMd5(in);
        } finally {
            IoUtils.safeClose(in);
        }
    }

    /**
     * @return SHA-1 digest (40 characters).
     */
    public static String getSha1(File file) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            return IoUtils.getSha1(in);
        } finally {
            IoUtils.safeClose(in);
        }
    }

    public static void updateChecksum(File file, Checksum checksum) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            IoUtils.updateChecksum(in, checksum);
        } finally {
            IoUtils.safeClose(in);
        }
    }


    /*
    对文件进行操作
     */

    /**
     * 创建文件
     *
     * @param path 文件路径包括文件名后后缀名
     * @return 返回是否成功 true 表示成功 false 表示失败
     * @throws IOException
     */
    public static Boolean createFile(String path) throws IOException {
        File file = new File(path);
        return file.createNewFile();// 建立文件


    }

    /**
     * 创建文件
     *
     * @param path 文件路径包括文件名后后缀名
     * @throws IOException
     */
    public static File createNewFile(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 创建文件夹
     *
     * @param path 创建文件夹的路径
     * @return 是否成功
     */
    public static Boolean createMkdir(String path) {
        File file = new File(path);
        return file.mkdir();
    }


    public static boolean checkIsEmpty(String path) {
        if (isDirectory(path)) {
            File[] files = fileList(path);
            return files.length <= 0;
        } else {
            return false;
        }
    }

    /**
     * 判断是不是文件夹
     *
     * @param path 需要检查的文件路径
     * @return 是否是文件夹
     */
    public static Boolean isDirectory(String path) {
        File file = new File(path);
        return file.isDirectory();
    }

    /**
     * 列出文件夹下的所有文件和文件夹名
     *
     * @param path 需要列出的父目录
     * @return File[]集合
     */
    public static File[] fileList(String path) {
        File file = new File(path);
        File[] files = file.listFiles(); // 列出文件夹下的所有文件和文件夹名

        return files;
    }

    /**
     * 更改文件或文件夹名
     *
     * @param oldPath 旧文件名的路径
     * @param newFile
     * @return
     */
    public static Boolean reName(String oldPath, File newFile) {
        File file = new File(oldPath);
        return file.renameTo(newFile);
    }

    /**
     * 获得文件或文件夹的父目录
     *
     * @param f File文件的实例对象
     * @return
     */
    public String getparentPath(File f) {
        String parentPath = f.getParent(); // 获得文件或文件夹的父目录
        return parentPath;
    }

    /**
     * 获取文件字符串中的文件名
     *
     * @param url
     * @return
     */
    private static String getFileName(String url) {
        String extension = getFileExtension(url);
        String fileName = url.substring(url.lastIndexOf("/"), url.lastIndexOf("."));
        return (new StringBuilder()).append(fileName).append(extension).toString();
    }

    /**
     * 获取文件名
     *
     * @param file
     * @return
     */
    private static String getFileName(File file) {
        return file.exists() ? file.getName() : "";
    }

    /**
     * 获取文件的后缀名
     */
    public static String getFileExtension(String fileString) {
        return fileString.substring(fileString.lastIndexOf("."), fileString.length()).toLowerCase();
    }

    public static String getMimeType(String type) {
        if (type == null) {
            return null;
        }
        type = type.trim().toLowerCase(Locale.US);
        final int semicolonIndex = type.indexOf(';');
        if (semicolonIndex != -1) {
            type = type.substring(0, semicolonIndex);
        }
        return type;
    }

    /**
     * 格式化文件大小(xxx.xx B/KB/MB/GB)
     */
    public static String formatFileSize(long size) {
        if (size <= 0) return "0B";
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (size < _1KB)
            fileSizeString = df.format((double) size) + "B";
        else if (size < _1MB)
            fileSizeString = df.format((double) size / _1KB) + "K";
        else if (size < _1GB)
            fileSizeString = df.format((double) size / _1MB) + "M";
        else
            fileSizeString = df.format((double) size / _1GB) + "G";
        return fileSizeString;
    }

    /**
     * 清空文件夹
     */
    public static void clearFolder(File folder) {
        for (File file : folder.listFiles()) file.delete();
    }

    /**
     * 清空文件夹
     *
     * @param folderPath 文件夹路径
     */
    public static void clearFolder(String folderPath) {
        File folder = new File(folderPath);
        for (File file : folder.listFiles()) file.delete();
    }

    /**
     * 删除文件
     *
     * @param localPath 文件路径
     */

    public static void deleteFile(String localPath) {
        File file = new File(localPath.substring(0, localPath.length() - 4));
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 判断是否存在此文件
     *
     * @param localPath 本地文件路径
     * @return
     */
    public static boolean isExitFile(String localPath) {
        File file = new File(localPath.substring(0, localPath.length()));
        return file.exists();
    }


}
