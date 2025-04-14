package org.ithub.socialintegrationservice.controller;

import lombok.RequiredArgsConstructor;
import org.ithub.socialintegrationservice.dto.LinkSocialAccountRequest;
import org.ithub.socialintegrationservice.model.SocialAccount;
import org.ithub.socialintegrationservice.service.SocialAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/social")
@RequiredArgsConstructor
public class SocialAccountController {
    private final SocialAccountService socialAccountService;

    @PostMapping("/link")
    public ResponseEntity<SocialAccount> linkAccount(@RequestBody LinkSocialAccountRequest request) {
        return ResponseEntity.ok(socialAccountService.linkAccount(request));
    }

    @GetMapping("/{userId}/{platform}")
    public ResponseEntity<SocialAccount> getAccount(@PathVariable Long userId, @PathVariable String platform) {
        return ResponseEntity.ok(socialAccountService.getAccount(userId, platform));
    }

    @DeleteMapping("/{userId}/{platform}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long userId, @PathVariable String platform) {
        socialAccountService.deleteAccount(userId, platform);
        return ResponseEntity.noContent().build();
    }
}
