package com.sfr.tokyo.sfr_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sfr.tokyo.sfr_backend.entity.Character;

// キャラクターエンティティのデータベース操作を担うリポジトリインターフェース
@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {
    // 特定のユーザーIDに紐づくすべてのキャラクターを取得
    List<Character> findByUserId(Long userId);
    
    // 特定のIDとユーザーIDでキャラクターを検索
    Optional<Character> findByIdAndUserId(Long id, Long userId);

    // キャラクター名とユーザーIDでキャラクターを検索
    Character findByNameAndUserId(String name, Long userId);
}
