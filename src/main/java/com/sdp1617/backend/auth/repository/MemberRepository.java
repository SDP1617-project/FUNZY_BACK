package com.sdp1617.backend.auth.repository;

import com.sdp1617.backend.auth.entity.AuthProvider;
import com.sdp1617.backend.auth.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByProviderAndProviderId(AuthProvider provider, String providerId);

    @Modifying
    @Query("update Member m set m.failedLoginCount = m.failedLoginCount + 1 where m.id = :id")
    int incrementFailedLoginCount(@Param("id") Long id);
}
