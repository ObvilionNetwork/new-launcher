package ru.obvilion.launcher.utils;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;

public class MD5 {
    public static String getChecksum(File f) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            try (InputStream is = Files.newInputStream(f.toPath())) {
                byte[] buffer = new byte[8192];
                int read = 0;
                while ((read = is.read(buffer)) > 0) {
                    md.update(buffer, 0, read);
                }
            }

            String res = new BigInteger(1, md.digest()).toString(16);

            short len = (short) (32 - res.length());
            for (short i = 0; i < len; i++) {
                res = "0" + res;
            }

            return res;
        } catch (Exception e) {
            Log.err("Error on checking MD5:");
            e.printStackTrace();

            return "";
        }
    }
}
