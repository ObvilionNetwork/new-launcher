package ru.obvilion.launcher.fx;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.obvilion.launcher.Vars;
import ru.obvilion.launcher.config.Global;
import ru.obvilion.launcher.utils.FileUtil;
import ru.obvilion.launcher.utils.Log;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * TODO: Проверка через HEAD запрос?
 *       Проверять сначала изменения, потом уже перезаписывать?
 */
public class CachingImageLoader {
    public static final List<CachingImageLoader> queue = new ArrayList<>();

    public static Image LOADING_GIF = null;

    private long lifetime = 1000 * 60 * 60 * 24 * 3; // 3 дня
    private double requestedWidth = 0;
    private double requestedHeight = 0;
    private boolean preserveRatio = false;
    private boolean smooth = false;

    private ImageView intoView = null;
    private String url = null;
    private File toFile = null;
    private boolean useLoading = false;
    private Callback callback =
            img -> this.intoView.setImage(img);

    public CachingImageLoader() {

    }

    public CachingImageLoader load(String url) {
        this.url = url;

        String r_url = url.replaceAll(" |:|\\\\|\\*|\\?|\\||\"|<|>|\\/", "_");
        this.toFile = new File(Global.LAUNCHER_CACHE, r_url);

        return this;
    }

    public CachingImageLoader into(ImageView imageView) {
        this.intoView = imageView;
        return this;
    }

    public CachingImageLoader useLoadingGif(boolean use) {
        this.useLoading = use;
        return this;
    }

    public CachingImageLoader setCallback(Callback cb) {
        this.callback = cb;
        return this;
    }

    public CachingImageLoader setRequestedSize(double requestedWidth, double requestedHeight) {
        this.requestedHeight = requestedHeight;
        this.requestedWidth = requestedWidth;
        return this;
    }

    public CachingImageLoader setPreserveRatio(boolean preserveRatio) {
        this.preserveRatio = preserveRatio;
        return this;
    }

    public CachingImageLoader setSmooth(boolean smooth) {
        this.smooth = smooth;
        return this;
    }

    public CachingImageLoader setLifetime(long lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    public void runRequest() {
        queue.add(this);

        if (queue.size() > 1) {
            return;
        }

        start();
    }

    private void start() {
        Thread request = new Thread(this::logic, "ImageView loader");

        request.setDaemon(true);
        request.start();
    }

    private void next() {
        synchronized (queue) {
            queue.remove(this);

            if (queue.size() > 0) {
                queue.get(0).start();
            }
        }
    }

    private void logic() {
        long delta = System.currentTimeMillis() - this.toFile.lastModified();

        if (this.toFile.exists()) {
            if (delta < this.lifetime) {
                Image img = new Image("file:///" + this.toFile.getAbsolutePath(), this.requestedWidth, this.requestedHeight, this.preserveRatio, this.smooth, false);

                Platform.runLater(() -> callback.run(img));

                next();
                return;
            }

            Log.debug("Image lifetime is expired for " + this.toFile.getName());
        }

        if (this.useLoading && Vars.useAnimations) {
            if (LOADING_GIF == null) {
                LOADING_GIF = new Image("loading.gif", this.requestedWidth, this.requestedHeight, this.preserveRatio, true, false);
            }

            Platform.runLater(() -> callback.run(LOADING_GIF));
        }

        try {
            this.toFile.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Image img = new Image(this.url, this.requestedWidth, this.requestedHeight, this.preserveRatio, this.smooth, false);

        try {
            saveImage(img);
            Log.debug("Cached image " + this.toFile.getName());
        } catch (Exception e) {
            Log.err("Error on load image " + this.toFile.getName() + ": ");
            e.printStackTrace();
        }

        Platform.runLater(() -> callback.run(img));

        next();
    }

    private void saveImage(Image image) {
        BufferedImage img = FXUtils.fromFXImage(image, null);

        String ext = FileUtil.getExtension(this.toFile);

        if (!ext.equals("png") && !ext.equals("jpg") && !ext.equals("jpeg")) {
            ext = "png";
        }

        try {
            assert img != null;
            ImageIO.write(img, ext, this.toFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public interface Callback {
        void run(Image img);
    }

}
