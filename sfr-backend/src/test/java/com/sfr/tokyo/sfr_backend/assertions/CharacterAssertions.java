package com.sfr.tokyo.sfr_backend.assertions;

import com.sfr.tokyo.sfr_backend.dto.CharacterDto;
import com.sfr.tokyo.sfr_backend.entity.CharacterLifecycle;

// BasePairAssert による compare 共有

public class CharacterAssertions {

    public static CharacterPairAssert assertThatPair(CharacterLifecycle entity, CharacterDto dto) {
        return new CharacterPairAssert(entity, dto);
    }

    public static class CharacterPairAssert extends BasePairAssert<CharacterPairAssert, CharacterLifecycle, CharacterDto> {

        public CharacterPairAssert(CharacterLifecycle actual, CharacterDto dto) {
            super(actual, dto, CharacterPairAssert.class);
        }

        public CharacterPairAssert hasSameCoreFields() {
            isNotNull();
            requireDto();
            compare("name", actual.getName(), dto.getName());
            compare("profile", actual.getProfile(), dto.getProfile());
            compare("imageUrl", actual.getImageUrl(), dto.getImageUrl());
            compare("lifespanPoints", actual.getLifespanPoints(), dto.getLifespanPoints());
            compare("status", actual.getStatus(), dto.getStatus());
            return this;
        }
    }
}
