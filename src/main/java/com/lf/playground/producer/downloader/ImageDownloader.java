package com.lf.playground.producer.downloader;

import com.lf.playground.producer.domain.User;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Service
public class ImageDownloader {

    @Value("${app.img.path}")
    private String DOWNLOAD_PATH;
    private final int CONNECT_TIMEOUT = 10000;
    private final int READ_TIMEOUT = 10000;

    public String downloadImage(User user) throws IOException {
        final var downloadPath = FilenameUtils.concat(DOWNLOAD_PATH, UUID.randomUUID().toString());
        FileUtils.copyURLToFile(
                new URL(user.getAvatarUrl()),
                new File(downloadPath),
                CONNECT_TIMEOUT,
                READ_TIMEOUT);

        return downloadPath;
    }

}
