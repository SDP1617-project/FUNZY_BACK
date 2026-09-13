package com.sdp1617.backend.card.service;

import com.sdp1617.backend.auth.entity.Member;
import com.sdp1617.backend.card.dto.CardBoxType;
import com.sdp1617.backend.card.dto.request.CardCreateRequest;
import com.sdp1617.backend.card.dto.request.CardImagePresignedUrlRequest;
import com.sdp1617.backend.card.dto.request.CardImageUploadCompleteRequest;
import com.sdp1617.backend.card.dto.response.CardCalendarDayResponse;
import com.sdp1617.backend.card.dto.response.CardCalendarResponse;
import com.sdp1617.backend.card.dto.response.CardFolderResponse;
import com.sdp1617.backend.card.dto.response.CardImagePresignedUrlResponse;
import com.sdp1617.backend.card.dto.response.CardListResponse;
import com.sdp1617.backend.card.dto.response.CardResponse;
import com.sdp1617.backend.card.dto.response.CardStorageResponse;
import com.sdp1617.backend.card.dto.response.CursorPageResponse;
import com.sdp1617.backend.card.entity.Card;
import com.sdp1617.backend.card.entity.Envelop;
import com.sdp1617.backend.card.repository.CardRepository;
import com.sdp1617.backend.card.repository.CardRepository.CalendarImageProjection;
import com.sdp1617.backend.card.repository.CardRepository.FolderSummaryProjection;
import com.sdp1617.backend.card.repository.EnvelopRepository;
import com.sdp1617.backend.global.config.properties.S3Properties;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int IMAGE_SIGNATURE_READ_BYTES = 16;
    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final LocalDateTime EMPTY_CREATED_AT = LocalDateTime.MIN;
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final EnvelopRepository envelopRepository;
    private final CardRepository cardRepository;
    private final EntityManager entityManager;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public CardImagePresignedUrlResponse issueImagePresignedUrl(
            Long senderId,
            CardImagePresignedUrlRequest request
    ) {
        validateS3Properties();
        String contentType = normalizeContentType(request.contentType());
        validateImageContentType(contentType);

        String imageKey = createImageKey(senderId, contentType);
        Instant expiresAt = Instant.now()
                .plus(Duration.ofMinutes(s3Properties.presignedUrlExpirationMinutes()));
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(imageKey)
                .contentType(contentType)
                .build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(s3Properties.presignedUrlExpirationMinutes()))
                .putObjectRequest(putObjectRequest)
                .build();
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new CardImagePresignedUrlResponse(
                presignedRequest.url().toString(),
                imageKey,
                createImageUrl(imageKey),
                "PUT",
                expiresAt
        );
    }

    public CursorPageResponse<CardStorageResponse> getCards(
            Long memberId,
            CardBoxType type,
            LocalDate date,
            String keyword,
            String cursor,
            int size
    ) {
        CursorKey cursorKey = parseCursor(cursor);
        int pageSize = normalizePageSize(size);
        List<CardStorageResponse> cards = findCardsByType(
                memberId,
                type,
                dateStartAt(date),
                dateEndAt(date),
                normalizeKeyword(keyword),
                cursorKey,
                PageRequest.of(0, pageSize + 1)
        )
                .stream()
                .map(CardStorageResponse::from)
                .toList();

        return toCursorPage(
                cards,
                pageSize,
                response -> new CursorKey(response.createdAt(), response.cardId())
        );
    }

    public CursorPageResponse<CardFolderResponse> getFolders(
            Long memberId,
            CardBoxType type,
            String cursor,
            int size
    ) {
        CursorKey cursorKey = parseCursor(cursor);
        int pageSize = normalizePageSize(size);
        List<CardFolderResponse> folders = findFolderSummaries(
                memberId,
                type,
                cursorKey,
                PageRequest.of(0, pageSize + 1)
        );

        return toCursorPage(
                folders,
                pageSize,
                response -> new CursorKey(response.latestCardCreatedAt(), response.memberId())
        );
    }

    public CardCalendarResponse getCalendar(Long memberId, CardBoxType type, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startAt = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endAt = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
        Map<LocalDate, List<String>> imageUrlsByDate = findCalendarImages(memberId, type, startAt, endAt)
                .stream()
                .collect(LinkedHashMap::new,
                        (map, image) -> map.computeIfAbsent(
                                image.getCreatedAt().toLocalDate(),
                                key -> new java.util.ArrayList<>()
                        ).add(image.getImageUrl()),
                        LinkedHashMap::putAll);

        List<CardCalendarDayResponse> days = imageUrlsByDate.entrySet()
                .stream()
                .map(entry -> new CardCalendarDayResponse(
                        entry.getKey(),
                        entry.getValue()
                                .stream()
                                .filter(imageUrl -> imageUrl != null && !imageUrl.isBlank())
                                .toList()
                ))
                .toList();

        return new CardCalendarResponse(type, year, month, days);
    }

    public CardStorageResponse getCard(Long memberId, Long cardId) {
        return CardStorageResponse.from(findAccessibleCard(memberId, cardId));
    }

    @Transactional
    public CardListResponse completeImageUpload(
            Long senderId,
            Long cardId,
            CardImageUploadCompleteRequest request
    ) {
        validateImageKeyOwner(senderId, request.imageKey());
        validateUploadedImage(request.imageKey());
        Card card = findWrittenCard(senderId, cardId);
        card.updateImage(request.imageKey(), createImageUrl(request.imageKey()));
        return CardListResponse.from(card);
    }

    @Transactional
    public void deleteWrittenCard(Long senderId, Long cardId) {
        Card card = findWrittenCard(senderId, cardId);
        cardRepository.delete(card);
    }

    @Transactional
    public CardResponse createCard(CardCreateRequest request){
        validateEnvelopCreateFields(request);
        Envelop envelop = envelopRepository.findBySender_IdAndReceiver_Id(request.senderId(), request.receiverId())
                .orElseGet(() -> createEnvelop(request));

        Card card = Card.create(
                envelop,
                request.title(),
                request.category(),
                request.link(),
                request.linkTitle(),
                request.content()
        );
        applyImageIfPresent(request.senderId(), request.imageKey(), card);
        Card savedCard = cardRepository.save(card);

        return CardResponse.from(savedCard, createShareUrl(savedCard.getId()));
    }

    private void applyImageIfPresent(Long senderId, String imageKey, Card card) {
        if (imageKey == null || imageKey.isBlank()) {
            return;
        }
        validateImageKeyOwner(senderId, imageKey);
        validateUploadedImage(imageKey);
        card.updateImage(imageKey, createImageUrl(imageKey));
    }

    private Envelop createEnvelop(CardCreateRequest request) {
        Member sender = entityManager.getReference(Member.class, request.senderId());
        Member receiver = entityManager.getReference(Member.class, request.receiverId());

        Envelop envelop = Envelop.create(sender, receiver, request.designType());
        return envelopRepository.save(envelop);
    }

    private void validateEnvelopCreateFields(CardCreateRequest request) {
        if (request.receiverId() == null || request.designType() == null) {
            throw new CustomException(
                    ErrorCode.COMMON_002,
                    "receiverId와 designType은 봉투 생성 시 필수입니다."
            );
        }
    }

    private Card findWrittenCard(Long senderId, Long cardId) {
        return cardRepository.findByIdAndEnvelop_Sender_Id(cardId, senderId)
                .orElseThrow(() -> new CustomException(ErrorCode.CARD_001));
    }

    private Card findAccessibleCard(Long memberId, Long cardId) {
        return cardRepository.findByIdAndEnvelop_Sender_Id(cardId, memberId)
                .or(() -> cardRepository.findByIdAndEnvelop_Receiver_Id(cardId, memberId))
                .orElseThrow(() -> new CustomException(ErrorCode.CARD_001));
    }

    private List<Card> findCardsByType(
            Long memberId,
            CardBoxType type,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String keyword,
            CursorKey cursorKey,
            Pageable pageable
    ) {
        if (type == CardBoxType.RECEIVED) {
            return cardRepository.findReceivedCards(
                    memberId,
                    startAt,
                    endAt,
                    keyword,
                    cursorCreatedAt(cursorKey),
                    cursorId(cursorKey),
                    EMPTY_CREATED_AT,
                    pageable
            );
        }
        return cardRepository.findSentCards(
                memberId,
                startAt,
                endAt,
                keyword,
                cursorCreatedAt(cursorKey),
                cursorId(cursorKey),
                EMPTY_CREATED_AT,
                pageable
        );
    }

    private List<CardFolderResponse> findFolderSummaries(
            Long memberId,
            CardBoxType type,
            CursorKey cursorKey,
            Pageable pageable
    ) {
        List<FolderSummaryProjection> summaries = type == CardBoxType.RECEIVED
                ? cardRepository.findReceivedFolderSummaries(
                        memberId,
                        cursorCreatedAt(cursorKey),
                        cursorId(cursorKey),
                        EMPTY_CREATED_AT,
                        pageable
                )
                : cardRepository.findSentFolderSummaries(
                        memberId,
                        cursorCreatedAt(cursorKey),
                        cursorId(cursorKey),
                        EMPTY_CREATED_AT,
                        pageable
                );

        return summaries.stream()
                .map(summary -> new CardFolderResponse(
                        summary.getMemberId(),
                        summary.getNickname(),
                        summary.getCardCount(),
                        findLatestFolderImageUrl(memberId, type, summary.getMemberId()),
                        restoreEmptyCreatedAt(summary.getLatestCardCreatedAt())
                ))
                .toList();
    }

    private String findLatestFolderImageUrl(Long memberId, CardBoxType type, Long folderMemberId) {
        List<String> imageUrls = type == CardBoxType.RECEIVED
                ? cardRepository.findLatestReceivedFolderImageUrl(
                        memberId,
                        folderMemberId,
                        EMPTY_CREATED_AT,
                        PageRequest.of(0, 1)
                )
                : cardRepository.findLatestSentFolderImageUrl(
                        memberId,
                        folderMemberId,
                        EMPTY_CREATED_AT,
                        PageRequest.of(0, 1)
                );
        return imageUrls.isEmpty() ? null : imageUrls.get(0);
    }

    private List<CalendarImageProjection> findCalendarImages(
            Long memberId,
            CardBoxType type,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        if (type == CardBoxType.RECEIVED) {
            return cardRepository.findReceivedCalendarImages(memberId, startAt, endAt);
        }
        return cardRepository.findSentCalendarImages(memberId, startAt, endAt);
    }

    private LocalDateTime dateStartAt(LocalDate date) {
        return date == null ? null : date.atStartOfDay();
    }

    private LocalDateTime dateEndAt(LocalDate date) {
        return date == null ? null : date.plusDays(1).atStartOfDay();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private LocalDateTime cursorCreatedAt(CursorKey cursorKey) {
        return cursorKey == null ? null : cursorKey.createdAt();
    }

    private Long cursorId(CursorKey cursorKey) {
        return cursorKey == null ? null : cursorKey.id();
    }

    private LocalDateTime restoreEmptyCreatedAt(LocalDateTime createdAt) {
        return EMPTY_CREATED_AT.equals(createdAt) ? null : createdAt;
    }

    private void validateS3Properties() {
        if (s3Properties.bucket() == null || s3Properties.bucket().isBlank()) {
            throw new CustomException(ErrorCode.COMMON_999, "S3 bucket 설정이 필요합니다.");
        }
        if (s3Properties.staticRegion() == null || s3Properties.staticRegion().isBlank()) {
            throw new CustomException(ErrorCode.COMMON_999, "S3 region 설정이 필요합니다.");
        }
        if (s3Properties.presignedUrlExpirationMinutes() <= 0) {
            throw new CustomException(ErrorCode.COMMON_999, "S3 presigned URL 만료 시간 설정이 올바르지 않습니다.");
        }
    }

    private void validateImageContentType(String contentType) {
        if (!ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new CustomException(ErrorCode.CARD_003);
        }
    }

    private void validateUploadedImage(String imageKey) {
        validateS3Properties();
        HeadObjectResponse objectMetadata = getImageObjectMetadata(imageKey);
        String contentType = normalizeContentType(objectMetadata.contentType());
        validateImageContentType(contentType);
        validateImageSize(objectMetadata.contentLength());
        validateImageSignature(imageKey, contentType);
    }

    private HeadObjectResponse getImageObjectMetadata(String imageKey) {
        try {
            return s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(imageKey)
                    .build());
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                throw new CustomException(ErrorCode.CARD_002);
            }
            throw exception;
        }
    }

    private void validateImageSize(Long contentLength) {
        if (contentLength == null || contentLength <= 0) {
            throw new CustomException(ErrorCode.CARD_003);
        }
        if (contentLength > MAX_IMAGE_SIZE_BYTES) {
            throw new CustomException(ErrorCode.CARD_004);
        }
    }

    private void validateImageSignature(String imageKey, String contentType) {
        byte[] signature = readImageSignature(imageKey);
        boolean valid = switch (contentType) {
            case "image/jpeg" -> isJpeg(signature);
            case "image/png" -> isPng(signature);
            case "image/webp" -> isWebp(signature);
            default -> false;
        };
        if (!valid) {
            throw new CustomException(ErrorCode.CARD_003);
        }
    }

    private byte[] readImageSignature(String imageKey) {
        try (ResponseInputStream<GetObjectResponse> object = s3Client.getObject(GetObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(imageKey)
                .range("bytes=0-" + (IMAGE_SIGNATURE_READ_BYTES - 1))
                .build())) {
            return object.readNBytes(IMAGE_SIGNATURE_READ_BYTES);
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                throw new CustomException(ErrorCode.CARD_002);
            }
            throw exception;
        } catch (IOException exception) {
            throw new CustomException(ErrorCode.CARD_003);
        }
    }

    private boolean isJpeg(byte[] signature) {
        return signature.length >= 3
                && (signature[0] & 0xFF) == 0xFF
                && (signature[1] & 0xFF) == 0xD8
                && (signature[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] signature) {
        int[] pngSignature = {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        if (signature.length < pngSignature.length) {
            return false;
        }
        for (int i = 0; i < pngSignature.length; i++) {
            if ((signature[i] & 0xFF) != pngSignature[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean isWebp(byte[] signature) {
        return signature.length >= 12
                && signature[0] == 'R'
                && signature[1] == 'I'
                && signature[2] == 'F'
                && signature[3] == 'F'
                && signature[8] == 'W'
                && signature[9] == 'E'
                && signature[10] == 'B'
                && signature[11] == 'P';
    }

    private void validateImageKeyOwner(Long senderId, String imageKey) {
        if (!imageKey.startsWith(imageKeyPrefix(senderId))) {
            throw new CustomException(ErrorCode.COMMON_004);
        }
    }

    private String createImageKey(Long senderId, String contentType) {
        return imageKeyPrefix(senderId) + UUID.randomUUID() + extensionFor(contentType);
    }

    private String imageKeyPrefix(Long senderId) {
        return "cards/" + senderId + "/";
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new CustomException(ErrorCode.CARD_003);
        };
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            throw new CustomException(ErrorCode.CARD_003);
        }
        return contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
    }

    private String createImageUrl(String imageKey) {
        if (s3Properties.publicBaseUrl() != null && !s3Properties.publicBaseUrl().isBlank()) {
            return s3Properties.publicBaseUrl().replaceAll("/$", "") + "/" + imageKey;
        }
        return "https://" + s3Properties.bucket() + ".s3." + s3Properties.staticRegion() + ".amazonaws.com/" + imageKey;
    }

    private String createShareUrl(Long cardId) {
        return frontendUrl.replaceAll("/$", "") + "/cards/" + cardId;
    }

    private int normalizePageSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private <T> CursorPageResponse<T> toCursorPage(
            List<T> items,
            int size,
            Function<T, CursorKey> cursorExtractor
    ) {
        boolean hasNext = items.size() > size;
        List<T> pageItems = hasNext ? items.subList(0, size) : items;
        String nextCursor = hasNext ? encodeCursor(cursorExtractor.apply(pageItems.get(pageItems.size() - 1))) : null;
        return new CursorPageResponse<>(pageItems, nextCursor, hasNext);
    }

    private String encodeCursor(CursorKey cursorKey) {
        String plainCursor = normalizeCreatedAt(cursorKey.createdAt()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                + "|" + cursorKey.id();
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(plainCursor.getBytes(StandardCharsets.UTF_8));
    }

    private CursorKey parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String plainCursor = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = plainCursor.split("\\|", 2);
            return new CursorKey(
                    LocalDateTime.parse(parts[0], DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    Long.parseLong(parts[1])
            );
        } catch (RuntimeException exception) {
            throw new CustomException(ErrorCode.COMMON_002, "cursor 값이 올바르지 않습니다.");
        }
    }

    private LocalDateTime normalizeCreatedAt(LocalDateTime createdAt) {
        return createdAt == null ? EMPTY_CREATED_AT : createdAt;
    }

    private record CursorKey(
            LocalDateTime createdAt,
            Long id
    ) {
        private CursorKey {
            createdAt = createdAt == null ? EMPTY_CREATED_AT : createdAt;
        }
    }
}
