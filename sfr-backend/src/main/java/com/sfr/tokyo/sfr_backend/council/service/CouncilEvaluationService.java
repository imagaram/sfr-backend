package com.sfr.tokyo.sfr_backend.council.service;

import com.sfr.tokyo.sfr_backend.council.dto.CouncilEvaluationSummaryDto;
import com.sfr.tokyo.sfr_backend.entity.council.*;
import com.sfr.tokyo.sfr_backend.exception.BusinessException;
import com.sfr.tokyo.sfr_backend.exception.ErrorCode;
import com.sfr.tokyo.sfr_backend.repository.council.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouncilEvaluationService {

    private final CouncilUserEvaluationRepository userRepo;
    private final CouncilPeerEvaluationRepository peerRepo;
    private final CouncilAdminEvaluationRepository adminRepo;

    // 登録 (ユーザー)
    @Transactional
    public void submitUserEvaluation(UUID councilMemberId, UUID userId, int score, String comment) {
        validateScore(score);
        if (userRepo.existsByCouncilMemberIdAndUserId(councilMemberId, userId)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Already evaluated by user");
        }
        userRepo.save(CouncilUserEvaluation.builder()
                .councilMemberId(councilMemberId)
                .userId(userId)
                .score(score)
                .comment(comment)
                .build());
    }

    // 登録 (同僚評議員)
    @Transactional
    public void submitPeerEvaluation(UUID councilMemberId, UUID evaluatorId, int score, String comment) {
        validateScore(score);
        if (peerRepo.existsByCouncilMemberIdAndEvaluatorId(councilMemberId, evaluatorId)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Already peer-evaluated");
        }
        peerRepo.save(CouncilPeerEvaluation.builder()
                .councilMemberId(councilMemberId)
                .evaluatorId(evaluatorId)
                .score(score)
                .comment(comment)
                .build());
    }

    // 登録 (運営) 単一
    @Transactional
    public void submitAdminEvaluation(UUID councilMemberId, int score, String comment) {
        validateScore(score);
        adminRepo.findByCouncilMemberId(councilMemberId).ifPresent(e -> {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Admin evaluation already exists");
        });
        adminRepo.save(CouncilAdminEvaluation.builder()
                .councilMemberId(councilMemberId)
                .score(score)
                .comment(comment)
                .build());
    }

    public CouncilEvaluationSummaryDto summarize(UUID councilMemberId) {
        double userAvg = userRepo.averageScore(councilMemberId);
        double peerAvg = peerRepo.averageScore(councilMemberId);
        Integer adminScore = adminRepo.findByCouncilMemberId(councilMemberId).map(CouncilAdminEvaluation::getScore).orElse(null);
    long userCount = userRepo.countByCouncilMemberId(councilMemberId);
    long peerCount = peerRepo.countByCouncilMemberId(councilMemberId);
        double weighted = computeWeighted(userAvg, peerAvg, adminScore);
        return CouncilEvaluationSummaryDto.builder()
                .userEvaluations(userCount)
                .userScoreAvg(round1(userAvg))
                .peerEvaluations(peerCount)
                .peerScoreAvg(round1(peerAvg))
                .adminScore(adminScore)
                .weightedScore(round1(weighted))
                .build();
    }

    private double computeWeighted(double user, double peer, Integer admin) {
        double adminVal = admin == null ? 0 : admin;
        // 仕様: user 0.4 peer 0.3 admin 0.3 (admin 未登録ならその比率を残りに再配分せず単純に 0 として扱う)
        return user * 0.4 + peer * 0.3 + adminVal * 0.3;
    }

    private void validateScore(int score) {
        if (score < 0 || score > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Score out of range 0-100");
        }
    }

    private double round1(double v) { return Math.round(v * 10.0) / 10.0; }
}
