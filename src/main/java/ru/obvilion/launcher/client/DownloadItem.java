package ru.obvilion.launcher.client;

import javafx.application.Platform;
import ru.obvilion.launcher.Vars;
import ru.obvilion.launcher.utils.FileUtil;
import ru.obvilion.launcher.utils.Log;
import ru.obvilion.launcher.utils.MD5;

import java.io.File;
import java.net.URLEncoder;

public class DownloadItem {
    public File save_to;
    public final String link;
    public final long size;
    public short threads;
    private Runnable callback = null;
    private short _try = 0;
    private String hash = null;
    private String version = null;

    // TODO: hash

    public DownloadItem(String link, File save_to, long size) {
        this.link = link;
        this.save_to = save_to;
        this.size = size;
    }

    public void setHashCode(String hashCode) {
        this.hash = hashCode;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setOnDownloadCallback(Runnable runnable) {
        this.callback = runnable;
    }

    public void download() {
        if (callback != null) {
            callback.run();
        }

        this.save_to.getParentFile().mkdirs();

        boolean deleted = this.save_to.delete();

        try {
            Log.debug("Downloading file {0} ({1}KB){2}", link, size / 1024, deleted ? " [exists, try download]" : "");

            Platform.runLater(() -> Vars.frameController.STATUS.setText("Загрузка файла " + this.link));

            FileUtil.threadedDownload(Downloader.api + URLEncoder.encode(link, "UTF-8").replaceAll("%2F", "/"), save_to, size, threads);

            FileVersions.setValue(save_to.getAbsolutePath(), version);
        } catch (Exception e) {
            if (_try > 2) {
                Log.err("Error on download file " + link);
                e.printStackTrace();
                return;
            }

            _try++;
        }
    }

    public boolean check() {
        if (!save_to.exists() && save_to.getParentFile().getName().equals("mods")) {
            File other = new File(save_to.getParentFile(), Vars.clientVersion + "/" + save_to.getName());

            if (other.exists()) {
                save_to = other;
            }
        }

        if (size == 0) {
            try {
                save_to.createNewFile();
                return true;
            } catch (Exception e) {
                Log.err("I can't create a file " + save_to);
                e.printStackTrace();
            }
        }

        if (version != null) {
            String vers = FileVersions.getValue(save_to.getAbsolutePath());

            if (vers == null || !vers.equals(version)) {
                Log.debug("  - Detected invalid version ({0} / {1}) - {2}", version, String.valueOf(vers), this.save_to.getName());
                return false;
            }

            return true;
        }

        if (save_to.exists() && save_to.length() != size) {
            Log.debug("  - Detected invalid size ({0} / {1}) - {2}", save_to.length(), size, this.save_to.getName());
            return false;
        }

        if (save_to.exists() && save_to.length() == size && this.hash != null) {
            boolean out = MD5.getChecksum(this.save_to).equals(this.hash);
            if (!out) {
                Log.debug("  - Detected invalid hash! {1} | {2} - {0}", this.save_to.getName(), MD5.getChecksum(this.save_to), this.hash);
            }

            return out;
        }

        // Hash here
        return save_to.exists() && save_to.length() == size;
    }
}
