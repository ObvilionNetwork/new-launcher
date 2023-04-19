package ru.obvilion.launcher.client;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.util.Duration;
import ru.obvilion.json.JSONArray;
import ru.obvilion.json.JSONObject;
import ru.obvilion.launcher.Vars;
import ru.obvilion.launcher.config.Config;
import ru.obvilion.launcher.config.Global;
import ru.obvilion.launcher.api.Request;
import ru.obvilion.launcher.utils.DesktopUtil;
import ru.obvilion.launcher.utils.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Downloader {
    public static final String api = Global.API_LINK + "files/?path=";
    public static Downloader INSTANCE = null;

    private final String name;
    private final int id;
    private final File client_dir;

    private Thread animation_thread = null;
    private boolean animation_runned = false;

    public AtomicLong bytes_per_second = new AtomicLong(0);
    private List<DownloadItem> all_items = new ArrayList<>();
    private List<DownloadItem> check_items = new ArrayList<>();
    private List<DownloadItem> download_queue = new ArrayList<>();

    private JSONObject client_data = null;
    private long size = 0;
    public boolean skip = false;

    public Downloader(String name, int id) {
        this.id = id;
        this.name = name;

        this.client_dir = new File(Global.LAUNCHER_CLIENTS, name);
        this.client_dir.mkdir();

        INSTANCE = this;
    }

    public Client loadAll() {
        Platform.runLater(() -> Vars.frameController.STATUS.setText("Получение списка файлов с сервера..."));

        // Запрашиваем список файлов с сервера, формируем списки к загрузке
        fetchData();

        // Считаем общий размер файлов к скачиванию
        this.size = calculate();

        // Запускаем анимацию загрузки (статусбар)
        startAnimationThread();

        // Скачиваем клиент
        Client client = download();

        // Проверяем лишние файлы, если что удаляем
        checkUnnecessaryFiles();

        // Останавлиает анимацию статусбара загрузки
        stopAnimationThread();

        return client;
    }


    private void addClientMods(JSONArray optional) {
        if (!Vars.clientMods.has(this.id + "")) {
            return;
        }

        Vars.optionalMods.clear();

        for (Object o : Vars.clientMods.getJSONArray(this.id + "")) {
            int find_id = (int) o;
            JSONObject ok = null;

            for (Object category : optional) {
                for (Object _mod : ((JSONObject)category).getJSONArray("mods")) {
                    JSONObject mod = (JSONObject) _mod;

                    if (mod.getInt("id") == find_id) {
                        ok = mod;
                        break;
                    }
                }

                if (ok != null) break;
            }

            if (ok == null) {
                Log.warn("Client mod with id {0} not allowed on server", find_id);
                continue;
            }

            Vars.optionalMods.add(ok);
        }

        for (JSONObject mod : Vars.optionalMods) {
            String path = mod.getString("path");

            // Получаем название файла
            String[] paths = path.split("/");
            String fileName = paths[paths.length - 1];
            File file = new File(this.client_dir, "mods/" + fileName);

            Log.debug("Added {0} client mod", mod.getString("path"));

            DownloadItem it = new DownloadItem(path, file, mod.getLong("fileSize"));
            it.setHashCode(mod.getString("fileHash"));

            this.all_items.add(it);
        }
    }

    /**
     * Метод загружает все данные с сервера и подготавливает их к загрузке
     */
    public void fetchData() {
        Log.info("Checking client {0} - {1}...", name, id);
        all_items.clear();

        JSONObject client = (JSONObject) getJsonFromAPI("clients/" + 1 + "/files");
        JSONObject default_client = (JSONObject) getJsonFromAPI(
                "clients/default/files?version=" + client.getString("version"));

        this.client_data = client;
        Vars.clientVersion = client.getString("version");

        JSONObject configs = (JSONObject) getJsonFromAPI("clients/" + this.id + "/configs");
        JSONObject assets = (JSONObject) getJsonFromAPI("clients/assets?version=" + Vars.clientVersion);

        addClientItems(client, true);
        addClientItems(default_client, false);

        addClientMods(client.getJSONArray("optionalMods"));

        check_items = new ArrayList<>(all_items);

        addItems(configs, client_dir);
        addItems(assets, new File(Global.LAUNCHER_CLIENTS, "assets/" + Vars.clientVersion), () -> {
            Platform.runLater(() -> Vars.frameController.SKIP.setVisible(true));
        });

        // Загрузка Java
        if (!System.getProperty("java.version").startsWith("1.8")) {
            String need = client.getString("java");
            String os = DesktopUtil.getOs();

            JSONArray javaFiles = (JSONArray) getJsonFromAPI("launcher/java/download/?os=" + os + "&version=" + need);

            Log.info("Java version {0} added to download queue...", need);
            Vars.useCustomJRE = true;

            JSONObject first = javaFiles.getJSONObject(0);
            String prefix = first.getString("link").replace(first.getString("path"), "");

            JSONObject java = new JSONObject();
            java.put("prefix", prefix);
            java.put("files", javaFiles);

            addItems(java, new File(Global.LAUNCHER_HOME, "java/" + need));
        }
    }

    /**
     * Добавляет в очередь не модульные файлы с включенными директориями
     * @param items_data Ответ от сервера
     * @param out В какую директорию сохранять
     */
    private void addItems(JSONObject items_data, File out) {
        addItems(items_data, out, null);
    }

    /**
     * Добавляет в очередь не модульные файлы с включенными директориями
     * @param items_data Ответ от сервера
     * @param out В какую директорию сохранять
     * @param callback Функция, которая будет выполняться при начале загрузки категории
     */
    private void addItems(JSONObject items_data, File out, Runnable callback) {

        JSONArray items = items_data.getJSONArray("files");
        String apiFilesPrefix = items_data.getString("prefix");

        boolean first = true;

        for (Object it : items) {
            JSONObject item = (JSONObject) it;

            File file = new File(out, item.getString("path"));
            String link = apiFilesPrefix + item.getString("path");

            DownloadItem downloadItem = new DownloadItem(link, file, item.getLong("fileSize"));

            if (first) {
                first = false;
                downloadItem.setOnDownloadCallback(callback);
            }

            all_items.add(downloadItem);
        }
    }

    /**
     * Включает в очередь все модульные файлы клиента
     * @param client клиент из /clients/:id/files
     * @param include_core если true - ядро включается в очередь загрузки
     */
    private void addClientItems(JSONObject client, boolean include_core) {
        if (include_core) {
            downloadItemTo(client.getJSONObject("coreMod"), new File(client_dir, "forge.jar"));
        }

        addClientCategory(client, "libraries");
        addClientCategory(client, "natives");
        addClientCategory(client, "mods");
    }

    /**
     * Включает в очередь категорию файлов из клиента,
     * т.е. mods, natives или libraries
     * @param client Клиент, с которого будет браться категория
     * @param name Название категории для скачивония
     */
    private void addClientCategory(JSONObject client, String name) {
        JSONArray arr = client.getJSONArray(name);

        for (Object object : arr) {
            addCategoryItem((JSONObject) object, new File(client_dir, name));
        }
    }

    /**
     * Включает указанный модуль в очередь загрузки,
     * рекурсивность папок не учитывается
     * @param item Файл для загрузки
     * @param dir Родительская папка
     */
    private void addCategoryItem(JSONObject item, File dir) {
        String path = item.getString("path");

        // Получаем название файла
        String[] paths = path.split("/");
        String fileName = paths[paths.length - 1];

        File file = new File(dir, fileName);

        // Проверяем, есть ли повторения
        for (DownloadItem item1 : all_items) {
            String it_path = item1.save_to.getPath();

            if (it_path.equals(file.getPath())) {
                Log.err("Download item " + item1.save_to.getPath() + " already in queue!");
                return;
            }
        }

        DownloadItem downloadItem = new DownloadItem(path, file, item.getLong("fileSize"));
        downloadItem.setHashCode(item.getString("fileHash"));

        all_items.add(downloadItem);
    }

    private void downloadItemTo(JSONObject item, File to) {
        String path = item.getString("path");

        DownloadItem downloadItem = new DownloadItem(path, to, item.getLong("fileSize"));
        all_items.add(downloadItem);
    }

    /**
     * Получает JSON Обьект по пути к Obvilion API
     * @param path Путь для скачивания
     * @return JSON обьект из data
     */
    public Object getJsonFromAPI(String path) {
        Request r = new Request(Global.API_LINK + path);
        JSONObject serv = r.connectAndGetJSON();

        if (serv == null) {
            throw new RuntimeException("Error getting info client info (" + path + "): response is null");
        }

        if (serv.has("error")) {
            throw new RuntimeException("Error getting info client info (" + path + "): " + serv.getString("error"));
        }

        if (serv.isNull("data")) {
            throw new RuntimeException("Error getting info client info (" + path + "): data is null");
        }

        return serv.get("data");
    }

    private File[] getFiles(String category) {
        return new File(client_dir, category).listFiles();
    }

    public void checkUnnecessaryFiles() {
        for (File f : getFiles("mods")) {
            if (f.isDirectory()) {
                if (!f.getName().equals("1.7.10")) {
                    continue;
                }

                for (File sf : getFiles("mods/" + f.getName())) {
                    boolean ok = false;

                    for (DownloadItem i : this.check_items) {
                        if (i.save_to.getName().equals(sf.getName())) {
                            ok = true;
                            break;
                        }
                    }

                    if (!ok && sf.delete()) {
                        Log.warn("File {0} deleted!", sf.getPath());
                    }
                }

                //f.delete();
                continue;
            }

            boolean ok = false;

            for (DownloadItem i : this.check_items) {
                if (i.save_to.getAbsolutePath().equals(f.getAbsolutePath())) {
                    ok = true;
                    break;
                }
            }

            if (!ok && f.delete()) {
                Log.warn("File {0} deleted!", f.getPath());
            }
        }
    }

    /**
     * Подсчитывает файлы для загрузки, их размер и
     * загружает их
     * @return Клиент для запуска
     */
    public Client download() {
        if (Vars.richPresence != null) {
            Vars.richPresence.updateState("Сервер " + this.name);
            Vars.richPresence.updateDescription("Скачивает файлы игры");
            Vars.richPresence.updateInvite();
        }

        Log.info("The download is starting, you need to download {0} MB", this.size / 1024 / 1024);

        Iterator<DownloadItem> iterator = download_queue.iterator();
        List<Thread> current_threads = new ArrayList<>();

        while (iterator.hasNext()) {
            if (skip) break;
            DownloadItem item = iterator.next();

            if (item.size / 1024 > 4096) {
                item.threads = 8;
            } else if (item.size / 1024 > 800) {
                item.threads = 4;
            } else if (item.size / 1024 > 128) {
                item.threads = 2;
            } else {
                item.threads = 1;
            }

            if (current_threads.size() == 2) {
                try {
                    current_threads.get(0).join();
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }

                current_threads.remove(0);
            }

            Thread th = new Thread(item::download);
            th.start();

            current_threads.add(th);
        }

        for (Thread thread : current_threads) {
            try {
                thread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Log.info("Download ended. Starting client...");

        if (Vars.richPresence != null) {
            Vars.richPresence.updateState("Сервер " + id);
            Vars.richPresence.updateDescription("Игрок " + Config.getValue("login"));
            Vars.richPresence.updateInvite();
        }

        skip = false;
        return new Client(this.client_data);
    }

    /**
     * Подсчитывает, сколько байт необходимо скачать в итоге
     * @return Количество байт к загрузке
     */
    public long calculate() {
        this.download_queue = checkInvalid();
        long out = 0;

        for (DownloadItem item : download_queue) {
            out += item.size;
        }

        return out;
    }

    /**
     * Получает количество уже загруженных байт
     * @return Количество загружденных байт
     */
    public long downloadStatus() {
        long out = 0;

        for (DownloadItem item : download_queue) {
            out += item.save_to.length();
        }

        return out;
    }

    /**
     * Возращает список файлов к обновлению
     * @return Файлы, которые необходимо скачать еще раз
     */
    public List<DownloadItem> checkInvalid() {
        List<DownloadItem> out = new ArrayList<>();

        for (DownloadItem item : all_items) {
            if (!item.check()) {
                out.add(item);
            }
        }

        return out;
    }

    private void stopAnimationThread() {
        if (this.animation_thread == null) {
            return;
        }

        this.animation_thread.interrupt();
        this.animation_thread = null;

        this.animation_runned = false;
    }

    private void startAnimationThread() {
        if (this.animation_thread != null) {
            this.animation_thread.interrupt();
        }

        this.animation_runned = true;

        long finalSize = this.size;

        this.animation_thread = new Thread(() -> {
            float tec_percent = 0;
            float last_percent = 0;

            while (animation_runned) {
                if (this.skip && this.bytes_per_second.get() == 0) {
                    break;
                }

                long old_speed = this.bytes_per_second.getAndSet(0);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                double current = this.bytes_per_second.get();

                last_percent = tec_percent;
                tec_percent = (float) (downloadStatus() / (double) finalSize);

                if (tec_percent > 1) {
                    tec_percent = 1;
                }

                float f_tec_percent = tec_percent;
                float f_last_percent = last_percent;
                final Animation animation = new Transition() {
                    {
                        setCycleDuration(Duration.millis(1000));
                    }

                    protected void interpolate(double f) {
                        double current_bytes = old_speed - (old_speed - current) * f;
                        float current_megabytes = (int) (current_bytes / 1024f / 1024f * 10f) / 10f;

                        float delta = (float) (f_last_percent - (f_last_percent - f_tec_percent) * f);

                        Vars.frameController.SPEED.setText(current_megabytes + " МБ/C");
                        Vars.frameController.PERSENT.setText(((int) (delta * 1000)) / 10f + "%");
                        Vars.frameController.STATUS_L.setPrefWidth(1040 * delta * Vars.frameController.root.getWidth() / 1165);
                    }
                };

                animation.play();
            }
        }, "Loading animation Thread");

        this.animation_thread.start();
    }
}
