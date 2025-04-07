package org.ithub.socialintegrationservice.controller;

import lombok.RequiredArgsConstructor;
import org.ithub.socialintegrationservice.dto.LinkSocialAccountRequest;
import org.ithub.socialintegrationservice.dto.SendMessageRequest;
import org.ithub.socialintegrationservice.enums.SocialPlatform;
import org.ithub.socialintegrationservice.model.SocialAccount;
import org.ithub.socialintegrationservice.service.SocialAccountService;
import org.ithub.socialintegrationservice.util.converter.StringToSocialPlatformConverter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/send/text")
    public ResponseEntity<String> sendMessage(@RequestBody SendMessageRequest request) {
        socialAccountService.sendMessage(request);
        return ResponseEntity.ok("Message sent successfully");
    }

    @PostMapping(value = "/send/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> sendFile(@ModelAttribute SendMessageRequest request) {
        socialAccountService.sendMessage(request, request.getFile());
        return ResponseEntity.ok("Message sent successfully");
    }
}
