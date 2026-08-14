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
import com.sdp1617.backend.card.repository.EnvelopRepository;
import com.sdp1617.backend.global.config.properties.S3Properties;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;
    private static final LocalDateTime EMPTY_CREATED_AT = LocalDateTime.MIN;

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
        validateImageContentType(request.contentType());

        String imageKey = createImageKey(senderId, request.fileName());
        Instant expiresAt = Instant.now()
                .plus(Duration.ofMinutes(s3Properties.presignedUrlExpirationMinutes()));
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(imageKey)
                .contentType(request.contentType())
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

    public List<CardStorageResponse> getCards(
            Long memberId,
            CardBoxType type,
            LocalDate date,
            String keyword
    ) {
        return findCardsByType(memberId, type)
                .stream()
                .filter(card -> matchesDate(card, date))
                .filter(card -> matchesKeyword(card, keyword))
                .sorted(cardCursorComparator())
                .map(CardStorageResponse::from)
                .toList();
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
        List<CardStorageResponse> cards = findCardsByType(memberId, type)
                .stream()
                .filter(card -> matchesDate(card, date))
                .filter(card -> matchesKeyword(card, keyword))
                .sorted(cardCursorComparator())
                .filter(card -> isAfterCursor(card.getCreatedAt(), card.getId(), cursorKey))
                .map(CardStorageResponse::from)
                .toList();

        return toCursorPage(
                cards,
                normalizePageSize(size),
                response -> new CursorKey(response.createdAt(), response.cardId())
        );
    }

    public List<CardFolderResponse> getFolders(Long memberId, CardBoxType type) {
        return buildFolders(memberId, type);
    }

    public CursorPageResponse<CardFolderResponse> getFolders(
            Long memberId,
            CardBoxType type,
            String cursor,
            int size
    ) {
        CursorKey cursorKey = parseCursor(cursor);
        List<CardFolderResponse> folders = buildFolders(memberId, type)
                .stream()
                .filter(folder -> isAfterCursor(folder.latestCardCreatedAt(), folder.memberId(), cursorKey))
                .toList();

        return toCursorPage(
                folders,
                normalizePageSize(size),
                response -> new CursorKey(response.latestCardCreatedAt(), response.memberId())
        );
    }

    private List<CardFolderResponse> buildFolders(Long memberId, CardBoxType type) {
        Function<Card, Member> folderMemberExtractor = type == CardBoxType.SENT
                ? card -> card.getEnvelop().getReceiver()
                : card -> card.getEnvelop().getSender();

        Map<Long, List<Card>> cardsByMember = findCardsByType(memberId, type)
                .stream()
                .collect(LinkedHashMap::new,
                        (map, card) -> map.computeIfAbsent(folderMemberExtractor.apply(card).getId(), key -> new java.util.ArrayList<>()).add(card),
                        LinkedHashMap::putAll);

        return cardsByMember.values()
                .stream()
                .map(cards -> {
                    Card latestCard = cards.stream()
                            .max(Comparator.comparing(Card::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                            .orElseThrow();
                    Member member = folderMemberExtractor.apply(latestCard);
                    return new CardFolderResponse(
                            member.getId(),
                            member.getNickname(),
                            cards.size(),
                            latestCard.getImageUrl(),
                            latestCard.getCreatedAt()
                    );
                })
                .sorted(Comparator
                        .comparing(CardFolderResponse::latestCardCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(CardFolderResponse::memberId, Comparator.reverseOrder()))
                .toList();
    }

    public CardCalendarResponse getCalendar(Long memberId, CardBoxType type, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        Map<LocalDate, List<String>> imageUrlsByDate = findCardsByType(memberId, type)
                .stream()
                .filter(card -> card.getCreatedAt() != null)
                .filter(card -> YearMonth.from(card.getCreatedAt()).equals(yearMonth))
                .collect(LinkedHashMap::new,
                        (map, card) -> map.computeIfAbsent(
                                card.getCreatedAt().toLocalDate(),
                                key -> new java.util.ArrayList<>()
                        ).add(card.getImageUrl()),
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
        validateUploadedImageExists(request.imageKey());
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
        Envelop envelop = envelopRepository.findBySender_Id(request.senderId())
                .orElseGet(() -> createEnvelop(request));

        Card card = Card.create(
                envelop,
                request.title(),
                request.category(),
                request.link(),
                request.linkTitle(),
                request.content()
        );
        Card savedCard = cardRepository.save(card);

        return CardResponse.from(savedCard, createShareUrl(savedCard.getId()));
    }

    private Envelop createEnvelop(CardCreateRequest request) {
        validateEnvelopCreateFields(request);

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

    private List<Card> findCardsByType(Long memberId, CardBoxType type) {
        if (type == CardBoxType.RECEIVED) {
            return cardRepository.findByEnvelop_Receiver_IdOrderByCreatedAtDescIdDesc(memberId);
        }
        return cardRepository.findByEnvelop_Sender_IdOrderByCreatedAtDescIdDesc(memberId);
    }

    private boolean matchesDate(Card card, LocalDate date) {
        return date == null || (card.getCreatedAt() != null && card.getCreatedAt().toLocalDate().equals(date));
    }

    private boolean matchesKeyword(Card card, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        return contains(card.getTitle(), normalizedKeyword)
                || contains(card.getContent(), normalizedKeyword)
                || contains(card.getEnvelop().getSender().getNickname(), normalizedKeyword)
                || contains(card.getEnvelop().getReceiver().getNickname(), normalizedKeyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private void validateS3Properties() {
        if (s3Properties.bucket() == null || s3Properties.bucket().isBlank()) {
            throw new CustomException(ErrorCode.COMMON_999, "S3 bucket 설정이 필요합니다.");
        }
        if (s3Properties.presignedUrlExpirationMinutes() <= 0) {
            throw new CustomException(ErrorCode.COMMON_999, "S3 presigned URL 만료 시간 설정이 올바르지 않습니다.");
        }
    }

    private void validateImageContentType(String contentType) {
        if (!contentType.toLowerCase().startsWith("image/")) {
            throw new CustomException(ErrorCode.COMMON_002, "이미지 파일만 업로드할 수 있습니다.");
        }
    }

    private void validateUploadedImageExists(String imageKey) {
        validateS3Properties();
        try {
            s3Client.headObject(HeadObjectRequest.builder()
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

    private void validateImageKeyOwner(Long senderId, String imageKey) {
        if (!imageKey.startsWith(imageKeyPrefix(senderId))) {
            throw new CustomException(ErrorCode.COMMON_004);
        }
    }

    private String createImageKey(Long senderId, String fileName) {
        return imageKeyPrefix(senderId) + UUID.randomUUID() + extractExtension(fileName);
    }

    private String imageKeyPrefix(Long senderId) {
        return "cards/" + senderId + "/";
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        String extension = fileName.substring(dotIndex).toLowerCase();
        if (!extension.matches("\\.[a-z0-9]{1,10}")) {
            return "";
        }
        return extension;
    }

    private String createImageUrl(String imageKey) {
        if (s3Properties.publicBaseUrl() != null && !s3Properties.publicBaseUrl().isBlank()) {
            return s3Properties.publicBaseUrl().replaceAll("/$", "") + "/" + imageKey;
        }
        return "https://" + s3Properties.bucket() + ".s3." + s3Properties.region() + ".amazonaws.com/" + imageKey;
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

    private boolean isAfterCursor(LocalDateTime createdAt, Long id, CursorKey cursorKey) {
        if (cursorKey == null) {
            return true;
        }
        LocalDateTime normalizedCreatedAt = normalizeCreatedAt(createdAt);
        int createdAtCompare = normalizedCreatedAt.compareTo(cursorKey.createdAt());
        if (createdAtCompare < 0) {
            return true;
        }
        return createdAtCompare == 0 && id < cursorKey.id();
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

    private Comparator<Card> cardCursorComparator() {
        return Comparator
                .comparing(Card::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Card::getId, Comparator.reverseOrder());
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
