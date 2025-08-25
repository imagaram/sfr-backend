package com.sfr.tokyo.sfr_backend.test.integration;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.BeforeEach;

/**
 * Repository統合テスト用のベースクラス
 * H2データベースを使用した高速なテスト環境を提供
 */
@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public abstract class BaseRepositoryIntegrationTest {

    @Autowired
    protected TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        // テストデータベースのクリアと初期化
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * エンティティを永続化して即座にフラッシュ
     */
    protected <T> T persistAndFlush(T entity) {
        entityManager.persistAndFlush(entity);
        return entity;
    }

    /**
     * エンティティマネージャーをクリア
     */
    protected void clearEntityManager() {
        entityManager.clear();
    }
}
