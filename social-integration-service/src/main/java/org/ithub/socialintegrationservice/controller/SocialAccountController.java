package org.ithub.socialintegrationservice.controller;

import lombok.RequiredArgsConstructor;
import org.ithub.socialintegrationservice.dto.LinkSocialAccountRequest;
import org.ithub.socialintegrationservice.model.SocialAccount;
import org.ithub.socialintegrationservice.service.SocialAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/social")
@RequiredArgsConstructor
public class SocialAccountController {
    private final SocialAccountService service;

    @PostMapping("/link")
    public ResponseEntity<SocialAccount> linkAccount(@RequestBody LinkSocialAccountRequest request) {
        return ResponseEntity.ok(service.linkAccount(request));
    }

    @GetMapping("/{userId}/{platform}")
    public ResponseEntity<SocialAccount> getAccount(@PathVariable Long userId, @PathVariable String platform) {
        return ResponseEntity.ok(service.getAccount(userId, platform));
    }

    @DeleteMapping("/{userId}/{platform}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long userId, @PathVariable String platform) {
        service.deleteAccount(userId, platform);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active/{userId}")
    public ResponseEntity<List<SocialAccount>> getActiveAccounts(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getActiveAccountsForUser(userId));
    }

    @GetMapping("/platform/{userId}/{platform}")
    public ResponseEntity<List<SocialAccount>> getAccountsByPlatform(
            @PathVariable Long userId,
            @PathVariable String platform) {
        return ResponseEntity.ok(service.getAccountsByPlatform(userId, platform));
    }
}
