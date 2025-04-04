package org.ithub.mediastorageservice.config;

import org.ithub.mediastorageservice.service.MediaStorageService;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class MinioInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final MediaStorageService mediaStorageService;

    public MinioInitializer(MediaStorageService mediaStorageService) {
        this.mediaStorageService = mediaStorageService;
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        mediaStorageService.init();
    }
}