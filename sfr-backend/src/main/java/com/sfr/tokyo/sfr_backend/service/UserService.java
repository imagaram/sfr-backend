package com.sfr.tokyo.sfr_backend.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sfr.tokyo.sfr_backend.dto.UserDto;
import com.sfr.tokyo.sfr_backend.mapper.UserMapper;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.user.User;

import lombok.RequiredArgsConstructor;

// UserDetailsServiceを実装し、ユーザーの詳細情報をロードするサービス
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * ユーザー名（ここではメールアドレス）でユーザーの詳細をロードする
     * 
     * @param username ユーザー名（メールアドレス）
     * @return ユーザーの詳細情報
     * @throws UsernameNotFoundException 指定されたユーザーが見つからない場合
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // リポジトリを使用して、メールアドレスでユーザーを検索
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりませんでした。"));
    }

    /**
     * ユーザー詳細取得
     */
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserById(UUID userId) {
        return userRepository.findById(userId)
                .map(userMapper::toDto);
    }

    /**
     * メールアドレスでユーザー取得
     */
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toDto);
    }

    /**
     * ユーザー情報更新
     */
    @Transactional
    public Optional<UserDto> updateUser(UUID userId, UserDto userDto) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setFirstname(userDto.getFirstname());
                    user.setLastname(userDto.getLastname());
                    // emailの変更は認証に影響するため、別途検討が必要

                    User updatedUser = userRepository.save(user);
                    return userMapper.toDto(updatedUser);
                });
    }

    /**
     * 本人確認フラグの更新
     */
    @Transactional
    public Optional<UserDto> updateVerificationStatus(UUID userId, boolean idVerified, boolean myNumberVerified) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setIdVerified(idVerified);
                    user.setMyNumberVerified(myNumberVerified);

                    User updatedUser = userRepository.save(user);
                    return userMapper.toDto(updatedUser);
                });
    }
}
