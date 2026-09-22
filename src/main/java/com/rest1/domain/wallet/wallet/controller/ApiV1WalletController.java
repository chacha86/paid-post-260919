package com.rest1.domain.wallet.wallet.controller;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.wallet.wallet.dto.WalletDto;
import com.rest1.domain.wallet.wallet.entity.Wallet;
import com.rest1.domain.wallet.wallet.service.WalletService;
import com.rest1.global.rq.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallets")
@Tag(name = "ApiV1WalletController", description = "지갑 API")
@SecurityRequirement(name = "bearerAuth")
public class ApiV1WalletController {

    private final WalletService walletService;
    private final Rq rq;

    @GetMapping("/me")
    @Transactional(readOnly = true)
    @Operation(summary = "내 지갑 조회 (잔액 + 원장)")
    public WalletDto getMine() {
        Member actor = rq.getActor();
        Wallet wallet = walletService.findByMemberId(actor.getId()).get();

        return new WalletDto(wallet, walletService.findLedgers(wallet));
    }
}
