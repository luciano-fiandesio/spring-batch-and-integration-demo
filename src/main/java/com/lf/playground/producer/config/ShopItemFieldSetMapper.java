package com.lf.playground.producer.config;

import com.lf.playground.producer.domain.User;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

import java.util.UUID;

public class ShopItemFieldSetMapper implements FieldSetMapper<User> {
    @Override
    public User mapFieldSet(FieldSet fieldSet) {
        final String first = fieldSet.readRawString("first_name");
        final String last = fieldSet.readRawString("last_name");
        final String imageUrl = fieldSet.readRawString("avatar_url");
        final Long tenantId = fieldSet.readLong("tenant_id");
        return new User(UUID.randomUUID(), tenantId, first, last, imageUrl, null);
    }
}
