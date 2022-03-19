package com.lf.playground.producer.processor;

import com.lf.playground.producer.downloader.ImageDownloader;
import com.lf.playground.producer.domain.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class ShopItemProcessor implements ItemProcessor<User, User> {

    private ImageDownloader imageDownloader;

    @Override
    public User process(User user) {
        if (isValid(user)) {

            try {
                final var path = this.imageDownloader.downloadImage(user);

                return new User(user.getId(),
                        user.getTenantId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getAvatarUrl(), path);

            } catch (IOException e) {
                e.printStackTrace();
                return null; // PLEASE DON't DO THAT :)
            }
        }
        log.error("Invalid shop item");
        return null;
    }

    private boolean isValid(User user) {

        return user != null && StringUtils.hasText(user.getFirstName()) &&
                StringUtils.hasText(user.getLastName()) &&
                StringUtils.hasText(user.getAvatarUrl());
    }
}
