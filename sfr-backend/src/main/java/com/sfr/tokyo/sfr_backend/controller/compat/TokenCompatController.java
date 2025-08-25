package com.sfr.tokyo.sfr_backend.controller.compat;

import com.sfr.tokyo.sfr_backend.dto.crypto.TokenDto;
import com.sfr.tokyo.sfr_backend.dto.crypto.api.ApiBalanceHistoryDtos.*;
import com.sfr.tokyo.sfr_backend.dto.crypto.api.ApiTransferDtos.*;
import com.sfr.tokyo.sfr_backend.dto.crypto.api.ApiUserBalanceDto;
import com.sfr.tokyo.sfr_backend.entity.crypto.BalanceHistory;
import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance;
import com.sfr.tokyo.sfr_backend.service.crypto.BalanceHistoryService;
import com.sfr.tokyo.sfr_backend.service.crypto.UserBalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import com.sfr.tokyo.sfr_backend.exception.*;
import java.util.stream.Collectors;
import static com.sfr.tokyo.sfr_backend.compat.CompatApiConstants.*;

@RestController
@RequestMapping("/api/v1/sfr")
@RequiredArgsConstructor
@Slf4j
public class TokenCompatController {

    private final UserBalanceService userBalanceService;
    private final BalanceHistoryService balanceHistoryService;

    private static String fmt8(BigDecimal v) {
        return v == null ? "0.00000000" : v.setScale(8, RoundingMode.DOWN).toPlainString();
    }

    // GET /sfr/balance/{user_id}
    @GetMapping("/balance/{user_id}")
    public ResponseEntity<ApiUserBalanceDto> getUserBalance(@PathVariable("user_id") String userId) {
        Long spaceId = 1L;
        return userBalanceService.getUserBalance(userId, spaceId)
                .map(ub -> ResponseEntity.ok(toApiUserBalance(ub)))
                .orElseThrow(() -> new ResourceNotFoundException("User balance not found"));
    }

    // GET /sfr/balance/{user_id}/history?page&page_size (deprecated: page_index,
    // limit)
    @GetMapping("/balance/{user_id}/history")
    public ResponseEntity<BalanceHistoryResponseDto> getHistory(
            @PathVariable("user_id") String userId,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "page_index", required = false) Integer pageIndexDeprecated,
            @RequestParam(name = "page_size", required = false) Integer pageSize,
            @RequestParam(name = "limit", required = false) Integer limitDeprecated) {
        // 優先順位: page(1-based) > page_index(0-based, deprecated)
        int resolvedPageIndex;
        if (page != null) {
            if (page < 1) {
                throw new InvalidRequestException("page must be >= 1");
            }
            resolvedPageIndex = page - 1;
        } else if (pageIndexDeprecated != null) {
            if (pageIndexDeprecated < 0) {
                throw new InvalidRequestException("page_index must be >= 0");
            }
            resolvedPageIndex = pageIndexDeprecated;
        } else {
            resolvedPageIndex = 0; // default first page
        }

        int resolvedPageSize;
        if (pageSize != null) {
            resolvedPageSize = pageSize;
        } else if (limitDeprecated != null) {
            resolvedPageSize = limitDeprecated; // deprecated alias
        } else {
            resolvedPageSize = DEFAULT_PAGE_SIZE; // default
        }

        if (resolvedPageSize < 1 || resolvedPageSize > MAX_PAGE_SIZE) {
            throw new InvalidRequestException("page_size must be between 1 and 100");
        }

        Page<BalanceHistory> historyPage = balanceHistoryService.getUserBalanceHistory(userId, resolvedPageIndex,
                resolvedPageSize);
        List<BalanceHistoryItemDto> items = historyPage.getContent().stream()
                .map(this::toApiHistoryItem)
                .collect(Collectors.toList());
        BalanceHistoryResponseDto resp = BalanceHistoryResponseDto.builder()
                .data(items)
                .pagination(PaginationInfoDto.builder()
                        .page(resolvedPageIndex + 1) // 外部提示は1始まり
                        .limit(resolvedPageSize)
                        .total_pages(historyPage.getTotalPages())
                        .total_count((int) historyPage.getTotalElements())
                        .has_next(historyPage.hasNext())
                        .has_previous(historyPage.hasPrevious())
                        .build())
                .build();
        return ResponseEntity.ok(resp);
    }

    // POST /sfr/transfer
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDto> transfer(
            @Valid @RequestBody TransferRequestDto request,
            Authentication authentication) {
        String authUser = authentication != null ? authentication.getName() : null;
        String sender = (request.getFrom_user_id() != null && !request.getFrom_user_id().isEmpty())
                ? request.getFrom_user_id()
                : authUser;
        if (sender == null || sender.isEmpty()) {
            throw new UnauthorizedOperationException("Sender not resolved");
        }

        // 既存のTokenControllerロジックに合わせるため、内部DTOへ変換
        TokenDto.TransferRequest internal = TokenDto.TransferRequest.builder()
                .recipientId(request.getTo_user_id())
                .amount(new BigDecimal(request.getAmount()))
                .message(request.getNote() != null ? request.getNote() : request.getReason())
                .build();

        if (internal.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Amount must be greater than 0");
        }

        // 転送実行のためにサービス群を使用
        Long spaceId = 1L;
        // 送金者残高
        UserBalance senderBalance = userBalanceService.getUserBalance(sender, spaceId)
                .orElseThrow(() -> new RuntimeException("Sender balance not found"));
        if (senderBalance.getCurrentBalance().compareTo(internal.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
        // 受取人残高（なければ作成）
        UserBalance recipientBalance = userBalanceService.getUserBalance(internal.getRecipientId(), spaceId)
                .orElseGet(() -> userBalanceService.createUserBalance(internal.getRecipientId(), spaceId,
                        BigDecimal.ZERO));

        String txId = UUID.randomUUID().toString();
        // 残高更新
        BigDecimal newSenderBalance = senderBalance.getCurrentBalance().subtract(internal.getAmount());
        userBalanceService.updateUserBalance(sender, spaceId, newSenderBalance);
        BigDecimal newRecipientBalance = recipientBalance.getCurrentBalance().add(internal.getAmount());
        userBalanceService.updateUserBalance(internal.getRecipientId(), spaceId, newRecipientBalance);
        // 履歴
        balanceHistoryService.recordTransfer(sender, senderBalance.getCurrentBalance(), internal.getAmount(), true,
                txId);
        balanceHistoryService.recordTransfer(internal.getRecipientId(), recipientBalance.getCurrentBalance(),
                internal.getAmount(), false, txId);

        TransferResponseDto resp = TransferResponseDto.builder()
                .transfer_id(txId)
                .from_user_id(sender)
                .to_user_id(internal.getRecipientId())
                .amount(fmt8(internal.getAmount()))
                .from_balance_after(fmt8(newSenderBalance))
                .to_balance_after(fmt8(newRecipientBalance))
                .processed_at(java.time.LocalDateTime.now())
                .build();
        return ResponseEntity.ok(resp);
    }

    private ApiUserBalanceDto toApiUserBalance(UserBalance ub) {
        return ApiUserBalanceDto.builder()
                .user_id(ub.getUserId())
                .current_balance(fmt8(ub.getCurrentBalance()))
                .total_earned(fmt8(ub.getTotalEarned()))
                .total_spent(fmt8(ub.getTotalSpent()))
                .total_collected(fmt8(ub.getTotalCollected()))
                .last_collection_date(ub.getLastCollectionDate())
                .collection_exempt(Boolean.TRUE.equals(ub.getCollectionExempt()))
                .frozen(Boolean.TRUE.equals(ub.getFrozen()))
                .updated_at(ub.getUpdatedAt())
                .build();
    }

    private BalanceHistoryItemDto toApiHistoryItem(BalanceHistory h) {
        return BalanceHistoryItemDto.builder()
                .history_id(h.getHistoryId())
                .user_id(h.getUserId().toString())
                .transaction_type(h.getTransactionType().name())
                .amount(fmt8(h.getAmount()))
                .balance_before(fmt8(h.getBalanceBefore()))
                .balance_after(fmt8(h.getBalanceAfter()))
                .reason(h.getReason())
                .reference_id(h.getReferenceId())
                .created_at(h.getCreatedAt())
                .build();
    }
}
