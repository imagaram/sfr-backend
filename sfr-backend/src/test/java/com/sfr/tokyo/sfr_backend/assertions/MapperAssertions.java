package com.sfr.tokyo.sfr_backend.assertions;

import java.util.Objects;

// BasePairAssert を利用して compare 重複を排除

/**
 * 汎用 Mapper アサーションヘルパ。
 * 片方向 DTO/Entity でフィールド差異を比較する際の重複削減。
 */
public class MapperAssertions {

    public static UserPairAssert assertThatPair(com.sfr.tokyo.sfr_backend.user.User entity,
                                                com.sfr.tokyo.sfr_backend.dto.UserDto dto) {
        return new UserPairAssert(entity, dto);
    }

    public static class UserPairAssert extends BasePairAssert<UserPairAssert, com.sfr.tokyo.sfr_backend.user.User, com.sfr.tokyo.sfr_backend.dto.UserDto> {

        public UserPairAssert(com.sfr.tokyo.sfr_backend.user.User actual, com.sfr.tokyo.sfr_backend.dto.UserDto dto) {
            super(actual, dto, UserPairAssert.class);
        }

        public UserPairAssert hasSameCoreFields() {
            isNotNull();
            requireDto();
            if (actual.getId() != null && dto.getId() != null) {
                if (!Objects.equals(actual.getId(), dto.getId())) {
                    failWithMessage("Expected id to match but was entity=%s dto=%s", actual.getId(), dto.getId());
                }
            }
            compare("firstname", actual.getFirstname(), dto.getFirstname());
            compare("lastname", actual.getLastname(), dto.getLastname());
            compare("email", actual.getEmail(), dto.getEmail());
            return this;
        }
    }
}
