package org.ithub.socialintegrationservice.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.ithub.socialintegrationservice.dto.LinkSocialAccountRequest;
import org.ithub.socialintegrationservice.model.SocialAccount;
import org.ithub.socialintegrationservice.service.SocialAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
    public ResponseEntity<Object> getAccount(@PathVariable Long userId, @PathVariable String platform) {
        try {
            SocialAccount account = service.getAccount(userId, platform);
            return ResponseEntity.ok(Objects.requireNonNullElse(account, Collections.emptyList()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }



    @DeleteMapping("/{userId}/{platform}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long userId, @PathVariable String platform) {
        service.deleteAccount(userId, platform);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/{platform}/toggle")
    public ResponseEntity<SocialAccount> toggleAccountStatus(@PathVariable Long userId, @PathVariable String platform) {
        return ResponseEntity.ok(service.toggleAccount(userId, platform));
    }

    @GetMapping("/active/{userId}")
    public ResponseEntity<List<SocialAccount>> getActiveAccount(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getActiveAccountsForUser(userId));
    }

    @GetMapping("/platform/{userId}/{platform}")
    public ResponseEntity<List<SocialAccount>> getAccountsByPlatform(
            @PathVariable Long userId,
            @PathVariable String platform) {
        return ResponseEntity.ok(service.getAccountsByPlatform(userId, platform));
    }
}
