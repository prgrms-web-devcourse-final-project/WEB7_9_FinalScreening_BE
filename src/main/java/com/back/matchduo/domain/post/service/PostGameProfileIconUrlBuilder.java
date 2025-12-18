package com.back.matchduo.domain.post.service;

import com.back.matchduo.domain.gameaccount.service.DataDragonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostGameProfileIconUrlBuilder {

    private final DataDragonService dataDragonService;

    private static final String PROFILE_ICON_BASE_URL =
            "https://ddragon.leagueoflegends.com/cdn/%s/img/profileicon/%d.png";

    public String buildProfileIconUrl(Integer profileIconId) {
        if (profileIconId == null) {
            return null;
        }

        try {
            String version = dataDragonService.getLatestVersion();
            return String.format(PROFILE_ICON_BASE_URL, version, profileIconId);
        } catch (Exception e) {
            // 실패 시 null
            return null;
        }
    }
}
