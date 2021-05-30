package ru.obvilion.launcher.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtil {
    public static void downloadFromURL(String url, File target) {
        try {
            URL website = new URL(url);
            try (InputStream in = website.openStream()) {
                Log.debug("Downloading file: " + url);
                Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void threadedDownload(String url, File target, long size, int threads_count) throws Exception {
        final int stock_buffer = 4096 * threads_count;
        final Range range = new Range(0, size, threads_count);
        final List< Thread > threadList = new ArrayList<>();
        final String path = target.getPath();

        for (int i = 0; i < threads_count; i++) {
            final int diap_i = i;
            Thread t = new Thread(() -> {
                byte[] buffer = new byte[stock_buffer];
                int bytesRead = -1;

                try {
                    HttpURLConnection conn = null;
                    RandomAccessFile randomAccessFile = null;
                    try {
                        conn = (HttpURLConnection) new URL(url).openConnection();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    conn.setDoInput(true);
                    conn.setConnectTimeout(100000);
                    conn.setRequestProperty("Range", "bytes=" + range.getRangeString(diap_i));
                    randomAccessFile = new RandomAccessFile(path, "rw");
                    randomAccessFile.seek(range.getFirstRangeInt(diap_i));
                    conn.connect();

                    InputStream inputStream = conn.getInputStream();
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        randomAccessFile.write(buffer, 0, bytesRead);
                    }
                    randomAccessFile.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            threadList.add(t);
        }

        for (Thread thread : threadList) {
            thread.start();
        }

        for (Thread thread : threadList) {
            thread.join();
        }
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                length += file.length();
            } else {
                length += folderSize(file);
            }
        }
        return length;
    }

    public static long getSize(File file) {
        if (file.isDirectory()) {
            return folderSize(file);
        }

        return file.length();
    }

    public static void downloadAndUnpackFromURL(String url, Path target) {
        try {
            URL website = new URL(url);
            try (InputStream in = website.openStream()) {
                System.out.println("Downloading file: " + url);
                Files.copy(in, new File(target.toFile(), "temp.zip").toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Unzip file: " + url);
            unzip(new File(target.toFile(), "temp.zip"), target.toFile());
        }
    }

    public static void unzip(File zip, File dest) {
        ZipFile zipFile = null;

        try {

            zipFile = new ZipFile(zip);
            Enumeration<? extends ZipEntry> e = zipFile.entries();

            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                File destinationPath = new File(dest, entry.getName());
                destinationPath.getParentFile().mkdirs();

                if (!entry.isDirectory()) {
                    System.out.println("Extracting file: " + destinationPath);

                    BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                    int b;
                    byte buffer[] = new byte[8192];

                    FileOutputStream fos = new FileOutputStream(destinationPath);
                    BufferedOutputStream bos = new BufferedOutputStream(fos, 8192);

                    while ((b = bis.read(buffer, 0, 8192)) != -1) {
                        bos.write(buffer, 0, b);
                    }

                    bos.close();
                    bis.close();
                }
            }
        } catch (IOException ioe) {
            System.out.println("Error opening zip file" + ioe);
        } finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (IOException ioe) {
                System.out.println("Error while closing zip file" + ioe);
            }
        }
    }

    public static void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                File f = new File(dir, children[i]);
                deleteDirectory(f);
            }
        }

        dir.delete();
    }
}
