package com.sfr.tokyo.sfr_backend.entity.crypto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * UserBalanceエンティティの複合主キークラス
 * userIdとspaceIdの組み合わせでユニーク性を保証
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBalanceId implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ユーザーID
     */
    private String userId;

    /**
     * スペースID
     */
    private Long spaceId;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        UserBalanceId that = (UserBalanceId) obj;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(spaceId, that.spaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, spaceId);
    }
}
