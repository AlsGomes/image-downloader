package br.com.agdev;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageDownloader {

    public static void main(String[] args) throws IOException {
        int pages = 2719;
        for (int i = 2680; i <= pages; i++) {
            String page = String.format("%04d", i);
            String prePageFileName = "";
            String postPageFileName = "-sem-t-tulo";
            String extension = ".png";
            String filename = prePageFileName + page + postPageFileName + extension;
            String urlBase = "https://leitordemanga.com/wp-content/uploads/WP-manga/data/manga_65d48dc6127d0/";
            String urlMid = "10a8d603bf10953153301f6112565ad8/";
            String url = urlBase + urlMid + filename;
            String folder = "boruto-tbb-cap3";

            Path pathWithoutFile = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", folder);
            if (Files.notExists(pathWithoutFile)) {
                Files.createDirectories(pathWithoutFile);
            }

            Path path = pathWithoutFile.resolve(filename);
            String pathString = path.toString();

            extractImage(url, pathString);

            if (extension.equals(".webp")) {
                convert(pathString, pathString.substring(0, pathString.lastIndexOf('.') + 1) + "png");
                Files.deleteIfExists(path);
            }
        }
    }

    private static void extractImage(String url, String path) {
        try {
            URL imageUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
            connection.setRequestMethod("GET");

            InputStream inputStream = connection.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(path);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
            connection.disconnect();

            System.out.println("Image downloaded successfully: " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void convert(String webpPath, String pngPath) {
        String executable = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "libwebp-0.4.1-linux-x86-64", "bin", "dwebp").toString();
        String[] args = new String[]{executable, webpPath, "-o", pngPath};

        try {
            Process exec = Runtime.getRuntime().exec(args);
            exec.waitFor();
            System.out.println("Image converted successfully: " + pngPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}