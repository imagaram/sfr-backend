package com.sfr.tokyo.sfr_backend.assertions;

import com.sfr.tokyo.sfr_backend.entity.Comment;
import com.sfr.tokyo.sfr_backend.dto.CommentDto;
// BasePairAssert で compare 共通化

public class CommentAssertions {

    public static CommentPairAssert assertThatPair(Comment entity, CommentDto dto) {
        return new CommentPairAssert(entity, dto);
    }

    public static class CommentPairAssert extends BasePairAssert<CommentPairAssert, Comment, CommentDto> {
        public CommentPairAssert(Comment actual, CommentDto dto) {
            super(actual, dto, CommentPairAssert.class);
        }
        public CommentPairAssert hasSameCoreFields() {
            isNotNull();
            requireDto();
            compare("content", actual.getContent(), dto.getContent());
            return this;
        }
    }
}
