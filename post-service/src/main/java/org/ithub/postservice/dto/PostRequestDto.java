package org.ithub.postservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ithub.postservice.enums.PostType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание/обновление поста")
public class PostRequestDto {
    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(min = 3, max = 100, message = "Заголовок должен содержать от 3 до 100 символов")
    @Schema(description = "Заголовок поста", example = "Новый продукт в нашем магазине!")
    private String title;

    @NotBlank(message = "Содержание не может быть пустым")
    @Size(max = 2000, message = "Содержание не может быть более 2000 символов")
    @Schema(description = "Содержание поста", example = "Сегодня мы представляем новый продукт...")
    private String content;

    @NotNull(message = "Тип поста должен быть указан")
    @Schema(description = "Тип поста", example = "TEXT")
    private PostType type;

    @Schema(description = "Теги поста", example = "[\"новинка\", \"техника\"]")
    private Set<String> tags = new HashSet<>();

    @Schema(description = "Идентификаторы медиа-файлов", example = "[1, 2, 3]")
    private List<Long> mediaIds = new ArrayList<>();

    @Schema(description = "Подписи к медиа-файлам", example = "[\"Описание изображения 1\", \"Описание изображения 2\"]")
    private List<String> mediaCaptions = new ArrayList<>();

    @Schema(description = "Идентификаторы аккаунтов социальных сетей для публикации", example = "[1, 2]")
    private List<Long> socialAccountIds = new ArrayList<>();

    @Schema(description = "Дата и время для планируемой публикации", example = "2025-05-01T12:00:00")
    private LocalDateTime scheduledAt;
}