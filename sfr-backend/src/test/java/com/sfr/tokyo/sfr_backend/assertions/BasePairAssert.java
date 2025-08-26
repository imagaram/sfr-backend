package com.sfr.tokyo.sfr_backend.assertions;

import org.assertj.core.api.AbstractAssert;

import java.util.Objects;

/**
 * 共通ペア比較アサーション基底クラス。
 * 各 PairAssert が (entity, dto) 間のフィールド比較で利用する compare / dto null チェックを集約。
 *
 * @param <SELF>  具象アサート型 (Fluent API 用)
 * @param <A>     Entity / Actual 型
 * @param <D>     Dto 型
 */
public abstract class BasePairAssert<SELF extends BasePairAssert<SELF, A, D>, A, D>
        extends AbstractAssert<SELF, A> {

    protected final D dto;

    protected BasePairAssert(A actual, D dto, Class<SELF> selfType) {
        super(actual, selfType);
        this.dto = dto;
    }

    /** DTO が null でないことを検証 */
    protected void requireDto() {
        if (dto == null) failWithMessage("Expected dto not to be null");
    }

    /** 単一フィールドの等価比較 (Objects.equals) */
    protected void compare(String field, Object a, Object b) {
        if (!Objects.equals(a, b)) {
            failWithMessage("Field %s mismatch: entity=%s dto=%s", field, a, b);
        }
    }
}
