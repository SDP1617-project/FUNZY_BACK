package com.sdp1617.backend.auth.social;

import com.sdp1617.backend.auth.entity.AuthProvider;

public interface SocialUserInfoProvider {

    AuthProvider provider();

    SocialUserInfo fetchUserInfo(String token);
}
