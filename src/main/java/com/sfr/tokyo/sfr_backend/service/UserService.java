package com.sfr.tokyo.sfr_backend.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sfr.tokyo.sfr_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

// UserDetailsServiceを実装し、ユーザーの詳細情報をロードするサービス
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * ユーザー名（ここではメールアドレス）でユーザーの詳細をロードする
     * @param username ユーザー名（メールアドレス）
     * @return ユーザーの詳細情報
     * @throws UsernameNotFoundException 指定されたユーザーが見つからない場合
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // リポジトリを使用して、メールアドレスでユーザーを検索
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりませんでした。"));
    }
}
