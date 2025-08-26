package com.sfr.tokyo.sfr_backend.assertions;

import com.sfr.tokyo.sfr_backend.dto.PostDto;
import com.sfr.tokyo.sfr_backend.entity.PostEntity;
// BasePairAssert による共通化

public class PostAssertions {

    public static PostPairAssert assertThatPair(PostEntity entity, PostDto dto) {
        return new PostPairAssert(entity, dto);
    }

    public static class PostPairAssert extends BasePairAssert<PostPairAssert, PostEntity, PostDto> {

        public PostPairAssert(PostEntity actual, PostDto dto) {
            super(actual, dto, PostPairAssert.class);
        }

        public PostPairAssert hasSameCoreFields() {
            isNotNull();
            requireDto();
            compare("title", actual.getTitle(), dto.getTitle());
            compare("description", actual.getDescription(), dto.getDescription());
            compare("fileUrl", actual.getFileUrl(), dto.getFileUrl());
            return this;
        }
    }
}
