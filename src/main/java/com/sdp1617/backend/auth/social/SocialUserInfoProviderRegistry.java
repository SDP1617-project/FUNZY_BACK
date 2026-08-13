package com.sdp1617.backend.auth.social;

import com.sdp1617.backend.auth.entity.AuthProvider;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class SocialUserInfoProviderRegistry {

    private final Map<AuthProvider, SocialUserInfoProvider> providers;

    public SocialUserInfoProviderRegistry(List<SocialUserInfoProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(SocialUserInfoProvider::provider, Function.identity()));
    }

    public SocialUserInfoProvider get(AuthProvider provider) {
        SocialUserInfoProvider found = providers.get(provider);
        if (found == null) {
            throw new CustomException(ErrorCode.COMMON_002, "지원하지 않는 소셜 로그인입니다.");
        }
        return found;
    }
}
