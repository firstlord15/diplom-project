package org.ithub.mediastorageservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.mediastorageservice.model.MediaFile;
import org.ithub.mediastorageservice.model.MediaVariant;
import org.ithub.mediastorageservice.repository.MediaVariantRepository;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageProcessingService {
    private final MediaStorageService mediaStorageService;
    private final MediaVariantRepository mediaVariantRepository;

    /**
     * Создание миниатюры изображения
     */
    public MediaVariant createThumbnail(MediaFile mediaFile, byte[] imageData, int width, int height) {
        return createResizedVariant(mediaFile, imageData, width, height, "thumbnail");
    }

    /**
     * Создание варианта изображения измененного размера
     */
    public MediaVariant createResizedVariant(MediaFile mediaFile, byte[] imageData, int width, int height, String variantName) {
        try {
            // Читаем оригинальное изображение
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
            BufferedImage originalImage = ImageIO.read(inputStream);

            if (originalImage == null) {
                throw new IllegalArgumentException("Cannot read image data");
            }

            // Создаем уменьшенное изображение с сохранением пропорций
            BufferedImage resizedImage = resizeImage(originalImage, width, height);

            // Сохраняем в формате JPEG
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "JPEG", outputStream);
            byte[] resizedImageData = outputStream.toByteArray();

            // Формируем путь в хранилище
            String storageKey = generateVariantKey(mediaFile.getStorageKey(), variantName);

            // Загружаем в MinIO
            mediaStorageService.uploadBytes(resizedImageData, storageKey, "image/jpeg");

            // Создаем запись о варианте
            MediaVariant variant = new MediaVariant();
            variant.setMediaFile(mediaFile);
            variant.setVariantName(variantName);
            variant.setStorageKey(storageKey);
            variant.setWidth(resizedImage.getWidth());
            variant.setHeight(resizedImage.getHeight());
            variant.setSize((long) resizedImageData.length);

            return mediaVariantRepository.save(variant);
        } catch (IOException e) {
            log.error("Error creating image variant: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create image variant", e);
        }
    }

    /**
     * Оптимизация изображения для социальной сети
     */
    public MediaVariant optimizeForSocialNetwork(MediaFile mediaFile, byte[] imageData, String network) {
        // Разные настройки для разных соцсетей
        if ("instagram".equalsIgnoreCase(network)) {
            // Instagram - квадратное изображение
            return createResizedVariant(mediaFile, imageData, 1080, 1080, "instagram");
        } else if ("vk".equalsIgnoreCase(network)) {
            // ВКонтакте - соотношение сторон 16:9
            return createResizedVariant(mediaFile, imageData, 1280, 720, "vk");
        } else if ("telegram".equalsIgnoreCase(network)) {
            // Telegram - макс. ширина 1280px
            return createResizedVariant(mediaFile, imageData, 1280, 0, "telegram");
        } else {
            // По умолчанию
            return createResizedVariant(mediaFile, imageData, 1200, 630, "social");
        }
    }

    /**
     * Изменение размера изображения с сохранением пропорций
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Если одно из измерений равно 0, вычисляем его с сохранением пропорций
        if (targetWidth == 0 && targetHeight > 0) {
            double ratio = (double) targetHeight / originalHeight;
            targetWidth = (int) (originalWidth * ratio);
        } else if (targetHeight == 0 && targetWidth > 0) {
            double ratio = (double) targetWidth / originalWidth;
            targetHeight = (int) (originalHeight * ratio);
        } else if (targetWidth > 0 && targetHeight > 0) {
            // Если заданы оба измерения, выбираем наименьшее соотношение для сохранения пропорций
            double widthRatio = (double) targetWidth / originalWidth;
            double heightRatio = (double) targetHeight / originalHeight;

            if (widthRatio < heightRatio) {
                targetHeight = (int) (originalHeight * widthRatio);
            } else {
                targetWidth = (int) (originalWidth * heightRatio);
            }
        }

        // Создаем новое изображение
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();

        try {
            // Настройки для лучшего качества
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Рисуем изображение
            g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        } finally {
            g.dispose();
        }

        return resizedImage;
    }

    /**
     * Генерация ключа для варианта изображения
     */
    private String generateVariantKey(String originalKey, String variantName) {
        // Примерная структура: исходный_путь/variants/имя_варианта/имя_файла
        int lastSlashIndex = originalKey.lastIndexOf("/");
        if (lastSlashIndex == -1) {
            return "variants/" + variantName + "/" + originalKey;
        }

        String path = originalKey.substring(0, lastSlashIndex);
        String filename = originalKey.substring(lastSlashIndex + 1);

        return path + "/variants/" + variantName + "/" + filename;
    }
}
