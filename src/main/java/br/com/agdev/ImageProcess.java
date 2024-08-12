package br.com.agdev;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ImageProcess {

    private static final Logger LOG = LogManager.getLogger(ImageProcess.class);

    public static void main(String[] args) throws IOException {
        int times = 10;
        LOG.info("Initiating process with a single thread [{}] times...", times);

        List<Summary> summaryList = new ArrayList<>();

        for (int i = 1; i <= times; i++) {
            LOG.info("Initiating process [#{}] with a single thread...", i);
            Summary summary = execute();
            LOG.info("Process [#{}] finished!", i);

            summaryList.add(summary);
        }

        SummaryGenerator.generateSummary(summaryList);
    }

    private static Summary execute() throws IOException {
        Instant initialTime = Instant.now();
        int pages = 41;
        for (int i = 1; i <= pages; i++) {
            String page = String.format("%04d", i);
            String prePageFileName = "Boruto-Two-Blue-Vortex-Capitulo-4_page-";
            String postPageFileName = "";
            String extension = ".webp";
            String filename = prePageFileName + page + postPageFileName + extension;
            String urlBase = "https://leitordemanga.com/wp-content/uploads/WP-manga/data/manga_65d48dc6127d0/";
            String urlMid = "2d98fedc0da277a0af0818e42a2f6621/";
            String url = urlBase + urlMid + filename;
            String folder = "boruto-tbb-cap4-single-thread";

            Path pathWithoutFile = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", folder);
            if (Files.notExists(pathWithoutFile)) {
                Files.createDirectories(pathWithoutFile);
            }

            Path path = pathWithoutFile.resolve(filename);

            boolean convert = extension.equals(".webp");

            ImageInfo imageInfo = new ImageInfo(url, path, convert);

            LOG.info("Extracting image [{}]...", imageInfo);
            extractImage(imageInfo);
            LOG.info("Image extracted successfully: [{}]", imageInfo);

            if (imageInfo.convert()) {
                LOG.info("Converting image [{}]...", imageInfo);
                String pathString = imageInfo.path().toString();
                convert(pathString, pathString.substring(0, pathString.lastIndexOf('.') + 1) + "png");
                LOG.info("Converted image [{}] successfully", imageInfo);

                LOG.info("Deleting image [{}]...", imageInfo);
                Files.deleteIfExists(path);
                LOG.info("Deleted image [{}] successfully", imageInfo);
            }
        }

        Instant finalTime = Instant.now();
        long elapsedTime = ChronoUnit.SECONDS.between(initialTime, finalTime);

        LOG.info("Process finalized. The process took [{}] seconds to complete", elapsedTime);
        return new Summary("Single thread", elapsedTime, 1);
    }

    private static void extractImage(ImageInfo imageInfo) {
        try {
            URL imageUrl = URL.of(URI.create(imageInfo.url()), null);
            HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
            connection.setRequestMethod("GET");

            InputStream inputStream = connection.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(imageInfo.path().toString());

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
            connection.disconnect();
        } catch (Exception e) {
            LOG.error("Error while extracting image [{}]", imageInfo, e);
        }
    }

    private static void convert(String webpPath, String pngPath) {
        String executable = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "libwebp-0.4.1-linux-x86-64", "bin", "dwebp").toString();
        String[] args = new String[]{executable, webpPath, "-o", pngPath};

        try {
            Process exec = Runtime.getRuntime().exec(args);
            exec.waitFor();
        } catch (Exception e) {
            LOG.error("Error while converting [{}]", webpPath, e);
        }
    }
}
