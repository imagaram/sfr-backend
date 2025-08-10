package com.sfr.tokyo.sfr_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sfr.tokyo.sfr_backend.user.User;

// UserRepositoryはUserエンティティを扱うためのJPAリポジトリ
@Repository // このインターフェースがリポジトリであることを示す
public interface UserRepository extends JpaRepository<User, Long> {

    // emailでユーザーを検索するメソッド
    // Spring Data JPAがこのメソッド名から自動的にクエリを生成する
    Optional<User> findByEmail(String email);
    
}
