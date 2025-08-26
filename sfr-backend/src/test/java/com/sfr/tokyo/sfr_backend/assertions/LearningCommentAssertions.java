package com.sfr.tokyo.sfr_backend.assertions;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningComment;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningCommentDto;
// BasePairAssert による重複削減

public class LearningCommentAssertions {

    public static LearningCommentPairAssert assertThatPair(LearningComment entity, LearningCommentDto dto) {
        return new LearningCommentPairAssert(entity, dto);
    }

    public static class LearningCommentPairAssert extends BasePairAssert<LearningCommentPairAssert, LearningComment, LearningCommentDto> {

        public LearningCommentPairAssert(LearningComment actual, LearningCommentDto dto) {
            super(actual, dto, LearningCommentPairAssert.class);
        }

        public LearningCommentPairAssert hasSameCoreFields() {
            isNotNull();
            requireDto();
            compare("topicId", actual.getTopicId(), dto.getTopicId());
            compare("authorId", actual.getAuthorId(), dto.getAuthorId());
            compare("content", actual.getContent(), dto.getContent());
            compare("commentType", actual.getCommentType(), dto.getCommentType());
            compare("commentStatus", actual.getCommentStatus(), dto.getCommentStatus());
            return this;
        }
    }
}
