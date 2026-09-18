package com.sdp1617.backend.auth.migration;

import com.sdp1617.backend.auth.entity.AuthProvider;
import com.sdp1617.backend.auth.entity.Consent;
import com.sdp1617.backend.auth.entity.Member;
import com.sdp1617.backend.auth.entity.SocialConnection;
import com.sdp1617.backend.auth.repository.MemberRepository;
import com.sdp1617.backend.auth.repository.SocialConnectionRepository;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialConnectionBackfillerTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SocialConnectionRepository socialConnectionRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private SocialConnectionBackfiller backfiller;

    @BeforeEach
    void setUp() {
        // 실제 트랜잭션 없이, 전달받은 콜백을 그 자리에서 바로 실행해준다.
        lenient().doAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(new SimpleTransactionStatus());
        }).when(transactionTemplate).execute(any());
    }

    private Member socialMember(Long id, AuthProvider provider, String providerId) {
        Member member = new Member("test" + id + "@sdp1617.com", "닉네임" + id, Consent.requiredOnly(), provider, providerId);
        try {
            Field field = Member.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(member, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return member;
    }

    @Test
    void 아직_연결이_없는_소셜_회원을_백필한다() {
        Member member = socialMember(1L, AuthProvider.KAKAO, "12345");
        when(memberRepository.findByProviderNot(AuthProvider.LOCAL)).thenReturn(List.of(member));
        when(socialConnectionRepository.existsByMember_IdAndProvider(1L, AuthProvider.KAKAO)).thenReturn(false);

        backfiller.run(null);

        ArgumentCaptor<SocialConnection> captor = ArgumentCaptor.forClass(SocialConnection.class);
        verify(socialConnectionRepository).saveAndFlush(captor.capture());
        assertEquals(AuthProvider.KAKAO, captor.getValue().getProvider());
        assertEquals("12345", captor.getValue().getProviderId());
    }

    @Test
    void 이미_연결이_있는_회원은_건너뛴다_멱등성() {
        Member member = socialMember(1L, AuthProvider.KAKAO, "12345");
        when(memberRepository.findByProviderNot(AuthProvider.LOCAL)).thenReturn(List.of(member));
        when(socialConnectionRepository.existsByMember_IdAndProvider(1L, AuthProvider.KAKAO)).thenReturn(true);

        backfiller.run(null);

        verify(socialConnectionRepository, never()).saveAndFlush(any());
    }

    @Test
    void 동시_실행으로_유니크_제약_위반이_나도_다른_회원_백필은_계속된다() {
        Member conflicting = socialMember(1L, AuthProvider.KAKAO, "12345");
        Member next = socialMember(2L, AuthProvider.GOOGLE, "67890");
        when(memberRepository.findByProviderNot(AuthProvider.LOCAL)).thenReturn(List.of(conflicting, next));
        when(socialConnectionRepository.existsByMember_IdAndProvider(1L, AuthProvider.KAKAO)).thenReturn(false);
        when(socialConnectionRepository.existsByMember_IdAndProvider(2L, AuthProvider.GOOGLE)).thenReturn(false);
        when(socialConnectionRepository.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("다른 인스턴스가 먼저 연결함"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        backfiller.run(null); // 예외 없이 끝까지 실행되어야 함

        verify(socialConnectionRepository, org.mockito.Mockito.times(2)).saveAndFlush(any());
    }
}
