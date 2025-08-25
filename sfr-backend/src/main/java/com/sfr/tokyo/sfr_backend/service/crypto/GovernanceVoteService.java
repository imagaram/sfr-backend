package com.sfr.tokyo.sfr_backend.service.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.GovernanceVote;
import com.sfr.tokyo.sfr_backend.repository.crypto.GovernanceVoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.Builder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ガバナンス投票サービス
 * 投票の記録、管理、統計分析を担当
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GovernanceVoteService {

    private final GovernanceVoteRepository governanceVoteRepository;

    /**
     * 投票を記録
     */
    @Transactional
    public GovernanceVote castVote(Long proposalId, UUID voterId, GovernanceVote.VoteType voteType,
            BigDecimal tokenBalance, BigDecimal reputationScore,
            String reason, Integer confidence) {
        log.info("Casting vote for proposal: {}, voter: {}, type: {}", proposalId, voterId, voteType);

        // 既存投票チェック
        List<GovernanceVote> existingVotes = governanceVoteRepository.findByVoterId(voterId)
                .stream()
                .filter(vote -> vote.getProposalId().equals(proposalId))
                .toList();

        if (!existingVotes.isEmpty()) {
            throw new RuntimeException("User has already voted on this proposal");
        }

        GovernanceVote vote = GovernanceVote.createStandardVote(
                proposalId, voterId, voteType, tokenBalance, reputationScore, reason, confidence);

        GovernanceVote saved = governanceVoteRepository.save(vote);
        log.info("Vote cast successfully with ID: {}", saved.getId());
        return saved;
    }

    /**
     * 委任投票を記録
     */
    @Transactional
    public GovernanceVote castDelegateVote(Long proposalId, UUID delegateId, UUID delegatorId,
            GovernanceVote.VoteType voteType, BigDecimal delegatedPower,
            BigDecimal delegateReputation, String reason) {
        log.info("Casting delegate vote for proposal: {}, delegate: {}, delegator: {}",
                proposalId, delegateId, delegatorId);

        GovernanceVote vote = GovernanceVote.createDelegateVote(
                proposalId, delegateId, delegatorId, voteType, delegatedPower, delegateReputation, reason);

        GovernanceVote saved = governanceVoteRepository.save(vote);
        log.info("Delegate vote cast successfully with ID: {}", saved.getId());
        return saved;
    }

    /**
     * 投票を変更
     */
    @Transactional
    public GovernanceVote changeVote(Long voteId, GovernanceVote.VoteType newVoteType, String changeReason) {
        log.info("Changing vote ID: {} to type: {}", voteId, newVoteType);

        GovernanceVote vote = getVote(voteId);
        vote.changeVote(newVoteType, changeReason);

        GovernanceVote updated = governanceVoteRepository.save(vote);
        log.info("Vote changed successfully");
        return updated;
    }

    /**
     * 投票取得
     */
    @Transactional(readOnly = true)
    public GovernanceVote getVote(Long id) {
        return governanceVoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vote not found with ID: " + id));
    }

    /**
     * 提案別投票取得
     */
    @Transactional(readOnly = true)
    public Page<GovernanceVote> getVotesByProposal(Long proposalId, int page, int size) {
        log.info("Getting votes for proposal: {}, page: {}, size: {}", proposalId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("votedAt").descending());
        return governanceVoteRepository.findByProposalId(proposalId, pageable);
    }

    /**
     * ユーザー別投票履歴取得
     */
    @Transactional(readOnly = true)
    public Page<GovernanceVote> getUserVotes(UUID userId, int page, int size) {
        log.info("Getting votes for user: {}, page: {}, size: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("votedAt").descending());
        return governanceVoteRepository.findByVoterId(userId, pageable);
    }

    /**
     * 投票タイプ別取得
     */
    @Transactional(readOnly = true)
    public List<GovernanceVote> getVotesByType(GovernanceVote.VoteType voteType) {
        log.info("Getting votes by type: {}", voteType);
        return governanceVoteRepository.findByVoteType(voteType);
    }

    /**
     * 期間内投票取得
     */
    @Transactional(readOnly = true)
    public Page<GovernanceVote> getVotesByPeriod(LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        log.info("Getting votes from {} to {}, page: {}, size: {}", startDate, endDate, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("votedAt").descending());
        return governanceVoteRepository.findByVotedAtBetween(startDate, endDate, pageable);
    }

    /**
     * 提案の投票統計取得
     */
    @Transactional(readOnly = true)
    public ProposalVoteStatistics getProposalVoteStatistics(Long proposalId) {
        log.info("Getting vote statistics for proposal: {}", proposalId);

        List<GovernanceVote> votes = governanceVoteRepository.findByProposalId(proposalId);

        if (votes.isEmpty()) {
            return ProposalVoteStatistics.builder()
                    .proposalId(proposalId)
                    .totalVotes(0L)
                    .forVotes(0L)
                    .againstVotes(0L)
                    .abstainVotes(0L)
                    .totalVotingPower(BigDecimal.ZERO)
                    .forVotingPower(BigDecimal.ZERO)
                    .againstVotingPower(BigDecimal.ZERO)
                    .abstainVotingPower(BigDecimal.ZERO)
                    .participationRate(BigDecimal.ZERO)
                    .averageConfidence(BigDecimal.ZERO)
                    .uniqueVoters(0L)
                    .delegateVotes(0L)
                    .changedVotes(0L)
                    .build();
        }

        long totalVotes = votes.size();
        long forVotes = votes.stream().mapToLong(v -> v.getVoteType() == GovernanceVote.VoteType.FOR ? 1 : 0).sum();
        long againstVotes = votes.stream().mapToLong(v -> v.getVoteType() == GovernanceVote.VoteType.AGAINST ? 1 : 0)
                .sum();
        long abstainVotes = votes.stream().mapToLong(v -> v.getVoteType() == GovernanceVote.VoteType.ABSTAIN ? 1 : 0)
                .sum();

        BigDecimal totalVotingPower = votes.stream()
                .map(GovernanceVote::getVotingPower)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal forVotingPower = votes.stream()
                .filter(v -> v.getVoteType() == GovernanceVote.VoteType.FOR)
                .map(GovernanceVote::getVotingPower)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal againstVotingPower = votes.stream()
                .filter(v -> v.getVoteType() == GovernanceVote.VoteType.AGAINST)
                .map(GovernanceVote::getVotingPower)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal abstainVotingPower = votes.stream()
                .filter(v -> v.getVoteType() == GovernanceVote.VoteType.ABSTAIN)
                .map(GovernanceVote::getVotingPower)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 参加率（仮に全体投票権を1000として計算）
        BigDecimal totalPossiblePower = new BigDecimal("1000");
        BigDecimal participationRate = totalVotingPower.divide(totalPossiblePower, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        BigDecimal averageConfidence = votes.stream()
                .filter(v -> v.getConfidenceLevel() != null)
                .map(v -> new BigDecimal(v.getConfidenceLevel()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(1, votes.stream()
                        .mapToInt(v -> v.getConfidenceLevel() != null ? 1 : 0).sum())), 4, RoundingMode.HALF_UP);

        long uniqueVoters = votes.stream()
                .map(GovernanceVote::getVoterId)
                .distinct()
                .count();

        long delegateVotes = votes.stream()
                .mapToLong(v -> v.isDelegateVote() ? 1 : 0)
                .sum();

        long changedVotes = votes.stream()
                .mapToLong(v -> v.hasBeenChanged() ? 1 : 0)
                .sum();

        return ProposalVoteStatistics.builder()
                .proposalId(proposalId)
                .totalVotes(totalVotes)
                .forVotes(forVotes)
                .againstVotes(againstVotes)
                .abstainVotes(abstainVotes)
                .totalVotingPower(totalVotingPower)
                .forVotingPower(forVotingPower)
                .againstVotingPower(againstVotingPower)
                .abstainVotingPower(abstainVotingPower)
                .participationRate(participationRate)
                .averageConfidence(averageConfidence)
                .uniqueVoters(uniqueVoters)
                .delegateVotes(delegateVotes)
                .changedVotes(changedVotes)
                .build();
    }

    /**
     * ユーザー投票統計取得
     */
    @Transactional(readOnly = true)
    public UserVoteStatistics getUserVoteStatistics(UUID userId) {
        log.info("Getting vote statistics for user: {}", userId);

        List<GovernanceVote> votes = governanceVoteRepository.findByVoterId(userId);

        if (votes.isEmpty()) {
            return UserVoteStatistics.builder()
                    .userId(userId)
                    .totalVotes(0L)
                    .forVotes(0L)
                    .againstVotes(0L)
                    .abstainVotes(0L)
                    .totalVotingPower(BigDecimal.ZERO)
                    .averageVotingPower(BigDecimal.ZERO)
                    .averageConfidence(BigDecimal.ZERO)
                    .delegateVotes(0L)
                    .changedVotes(0L)
                    .averageReputationScore(BigDecimal.ZERO)
                    .mostRecentVote(null)
                    .build();
        }

        long totalVotes = votes.size();
        long forVotes = votes.stream().mapToLong(v -> v.getVoteType() == GovernanceVote.VoteType.FOR ? 1 : 0).sum();
        long againstVotes = votes.stream().mapToLong(v -> v.getVoteType() == GovernanceVote.VoteType.AGAINST ? 1 : 0)
                .sum();
        long abstainVotes = votes.stream().mapToLong(v -> v.getVoteType() == GovernanceVote.VoteType.ABSTAIN ? 1 : 0)
                .sum();

        BigDecimal totalVotingPower = votes.stream()
                .map(GovernanceVote::getVotingPower)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageVotingPower = totalVotingPower.divide(BigDecimal.valueOf(totalVotes), 4,
                RoundingMode.HALF_UP);

        BigDecimal averageConfidence = votes.stream()
                .filter(v -> v.getConfidenceLevel() != null)
                .map(v -> new BigDecimal(v.getConfidenceLevel()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(1, votes.stream()
                        .mapToInt(v -> v.getConfidenceLevel() != null ? 1 : 0).sum())), 4, RoundingMode.HALF_UP);

        long delegateVotes = votes.stream().mapToLong(v -> v.isDelegateVote() ? 1 : 0).sum();
        long changedVotes = votes.stream().mapToLong(v -> v.hasBeenChanged() ? 1 : 0).sum();

        BigDecimal averageReputationScore = votes.stream()
                .map(GovernanceVote::getReputationScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(totalVotes), 4, RoundingMode.HALF_UP);

        LocalDateTime mostRecentVote = votes.stream()
                .map(GovernanceVote::getVotedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return UserVoteStatistics.builder()
                .userId(userId)
                .totalVotes(totalVotes)
                .forVotes(forVotes)
                .againstVotes(againstVotes)
                .abstainVotes(abstainVotes)
                .totalVotingPower(totalVotingPower)
                .averageVotingPower(averageVotingPower)
                .averageConfidence(averageConfidence)
                .delegateVotes(delegateVotes)
                .changedVotes(changedVotes)
                .averageReputationScore(averageReputationScore)
                .mostRecentVote(mostRecentVote)
                .build();
    }

    /**
     * 投票パターン分析
     */
    @Transactional(readOnly = true)
    public VotingPatternAnalysis analyzeVotingPatterns(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Analyzing voting patterns from {} to {}", startDate, endDate);

        List<GovernanceVote> votes = governanceVoteRepository.findByVotedAtBetween(startDate, endDate,
                PageRequest.of(0, Integer.MAX_VALUE)).getContent();

        if (votes.isEmpty()) {
            return VotingPatternAnalysis.builder()
                    .totalVotes(0L)
                    .averageParticipation(BigDecimal.ZERO)
                    .mostActiveVoters(List.of())
                    .votingTrends(Map.of())
                    .confidenceTrends(Map.of())
                    .delegationRate(BigDecimal.ZERO)
                    .voteChangeRate(BigDecimal.ZERO)
                    .build();
        }

        long totalVotes = votes.size();

        // アクティブ投票者分析
        Map<UUID, Long> voterCounts = votes.stream()
                .collect(Collectors.groupingBy(
                        GovernanceVote::getVoterId,
                        Collectors.counting()));

        List<VoterActivity> mostActiveVoters = voterCounts.entrySet().stream()
                .map(entry -> VoterActivity.builder()
                        .voterId(entry.getKey())
                        .voteCount(entry.getValue())
                        .build())
                .sorted((a, b) -> b.getVoteCount().compareTo(a.getVoteCount()))
                .limit(10)
                .toList();

        // 投票トレンド分析（日別）
        Map<String, Long> votingTrends = votes.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getVotedAt().toLocalDate().toString(),
                        Collectors.counting()));

        // 確信度トレンド分析
        Map<String, BigDecimal> confidenceTrends = votes.stream()
                .filter(v -> v.getConfidenceLevel() != null)
                .collect(Collectors.groupingBy(
                        v -> v.getVotedAt().toLocalDate().toString(),
                        Collectors.averagingInt(GovernanceVote::getConfidenceLevel)))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> BigDecimal.valueOf(entry.getValue()).setScale(2, RoundingMode.HALF_UP)));

        BigDecimal delegationRate = BigDecimal.valueOf(votes.stream().mapToLong(v -> v.isDelegateVote() ? 1 : 0).sum())
                .divide(BigDecimal.valueOf(totalVotes), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        BigDecimal voteChangeRate = BigDecimal.valueOf(votes.stream().mapToLong(v -> v.hasBeenChanged() ? 1 : 0).sum())
                .divide(BigDecimal.valueOf(totalVotes), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        BigDecimal averageParticipation = BigDecimal.valueOf(totalVotes)
                .divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP) // 仮の総ユーザー数
                .multiply(BigDecimal.valueOf(100));

        return VotingPatternAnalysis.builder()
                .totalVotes(totalVotes)
                .averageParticipation(averageParticipation)
                .mostActiveVoters(mostActiveVoters)
                .votingTrends(votingTrends)
                .confidenceTrends(confidenceTrends)
                .delegationRate(delegationRate)
                .voteChangeRate(voteChangeRate)
                .build();
    }

    /**
     * 提案別投票権重分析
     */
    @Transactional(readOnly = true)
    public VotingPowerAnalysis analyzeVotingPower(Long proposalId) {
        log.info("Analyzing voting power for proposal: {}", proposalId);

        List<GovernanceVote> votes = governanceVoteRepository.findByProposalId(proposalId);

        if (votes.isEmpty()) {
            return VotingPowerAnalysis.builder()
                    .proposalId(proposalId)
                    .totalVotingPower(BigDecimal.ZERO)
                    .powerDistribution(Map.of())
                    .topInfluencers(List.of())
                    .powerConcentration(BigDecimal.ZERO)
                    .averagePowerPerVote(BigDecimal.ZERO)
                    .medianVotingPower(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal totalVotingPower = votes.stream()
                .map(GovernanceVote::getVotingPower)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 投票権分布分析
        Map<String, BigDecimal> powerDistribution = votes.stream()
                .collect(Collectors.groupingBy(
                        v -> {
                            BigDecimal power = v.getVotingPower();
                            if (power.compareTo(BigDecimal.valueOf(10)) < 0)
                                return "Small (0-10)";
                            if (power.compareTo(BigDecimal.valueOf(100)) < 0)
                                return "Medium (10-100)";
                            if (power.compareTo(BigDecimal.valueOf(1000)) < 0)
                                return "Large (100-1000)";
                            return "Whale (1000+)";
                        },
                        Collectors.reducing(BigDecimal.ZERO, GovernanceVote::getVotingPower, BigDecimal::add)));

        // 影響力上位者
        List<VoterInfluence> topInfluencers = votes.stream()
                .map(vote -> VoterInfluence.builder()
                        .voterId(vote.getVoterId())
                        .votingPower(vote.getVotingPower())
                        .influenceScore(vote.getInfluenceScore())
                        .voteType(vote.getVoteType())
                        .build())
                .sorted((a, b) -> b.getVotingPower().compareTo(a.getVotingPower()))
                .limit(10)
                .toList();

        // パワー集中度（上位10%が全体の何%を占めるか）
        int topCount = Math.max(1, votes.size() / 10);
        BigDecimal topPower = votes.stream()
                .sorted((a, b) -> b.getVotingPower().compareTo(a.getVotingPower()))
                .limit(topCount)
                .map(GovernanceVote::getVotingPower)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal powerConcentration = totalVotingPower.compareTo(BigDecimal.ZERO) > 0
                ? topPower.divide(totalVotingPower, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        BigDecimal averagePowerPerVote = totalVotingPower.divide(BigDecimal.valueOf(votes.size()), 4,
                RoundingMode.HALF_UP);

        // 中央値計算
        List<BigDecimal> sortedPowers = votes.stream()
                .map(GovernanceVote::getVotingPower)
                .sorted()
                .toList();
        BigDecimal medianVotingPower = sortedPowers.get(sortedPowers.size() / 2);

        return VotingPowerAnalysis.builder()
                .proposalId(proposalId)
                .totalVotingPower(totalVotingPower)
                .powerDistribution(powerDistribution)
                .topInfluencers(topInfluencers)
                .powerConcentration(powerConcentration)
                .averagePowerPerVote(averagePowerPerVote)
                .medianVotingPower(medianVotingPower)
                .build();
    }

    // === 内部DTOクラス ===

    @Data
    @Builder
    public static class ProposalVoteStatistics {
        private Long proposalId;
        private Long totalVotes;
        private Long forVotes;
        private Long againstVotes;
        private Long abstainVotes;
        private BigDecimal totalVotingPower;
        private BigDecimal forVotingPower;
        private BigDecimal againstVotingPower;
        private BigDecimal abstainVotingPower;
        private BigDecimal participationRate;
        private BigDecimal averageConfidence;
        private Long uniqueVoters;
        private Long delegateVotes;
        private Long changedVotes;
    }

    @Data
    @Builder
    public static class UserVoteStatistics {
        private UUID userId;
        private Long totalVotes;
        private Long forVotes;
        private Long againstVotes;
        private Long abstainVotes;
        private BigDecimal totalVotingPower;
        private BigDecimal averageVotingPower;
        private BigDecimal averageConfidence;
        private Long delegateVotes;
        private Long changedVotes;
        private BigDecimal averageReputationScore;
        private LocalDateTime mostRecentVote;
    }

    @Data
    @Builder
    public static class VotingPatternAnalysis {
        private Long totalVotes;
        private BigDecimal averageParticipation;
        private List<VoterActivity> mostActiveVoters;
        private Map<String, Long> votingTrends;
        private Map<String, BigDecimal> confidenceTrends;
        private BigDecimal delegationRate;
        private BigDecimal voteChangeRate;
    }

    @Data
    @Builder
    public static class VoterActivity {
        private UUID voterId;
        private Long voteCount;
    }

    @Data
    @Builder
    public static class VotingPowerAnalysis {
        private Long proposalId;
        private BigDecimal totalVotingPower;
        private Map<String, BigDecimal> powerDistribution;
        private List<VoterInfluence> topInfluencers;
        private BigDecimal powerConcentration;
        private BigDecimal averagePowerPerVote;
        private BigDecimal medianVotingPower;
    }

    @Data
    @Builder
    public static class VoterInfluence {
        private UUID voterId;
        private BigDecimal votingPower;
        private BigDecimal influenceScore;
        private GovernanceVote.VoteType voteType;
    }
}
