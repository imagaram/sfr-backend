package com.sfr.tokyo.sfr_backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sfr.tokyo.sfr_backend.user.Role;
import com.sfr.tokyo.sfr_backend.user.Status;
import com.sfr.tokyo.sfr_backend.user.User;

/**
 * ユーザー情報を管理するリポジトリ
 * インデックス：email (UNIQUE)
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * メールアドレスからユーザーを検索
     * ログインやユーザー検索に使用
     */
    Optional<User> findByEmail(String email);

    /**
     * ロールでユーザーを検索
     */
    List<User> findByRole(Role role);

    /**
     * ステータスでユーザーを検索
     */
    List<User> findByStatus(Status status);

    /**
     * 名前（姓または名）で部分一致検索
     */
    @Query("SELECT u FROM User u WHERE u.firstname LIKE %:name% OR u.lastname LIKE %:name%")
    List<User> findByName(@Param("name") String name);

    /**
     * 認証状態でユーザーを検索
     */
    List<User> findByIdVerifiedTrue();

    /**
     * マイナンバー認証状態でユーザーを検索
     */
    List<User> findByMyNumberVerifiedTrue();
}
