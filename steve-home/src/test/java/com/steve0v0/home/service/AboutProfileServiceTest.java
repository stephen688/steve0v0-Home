package com.steve0v0.home.service;

import com.steve0v0.home.common.exception.BusinessException;
import com.steve0v0.home.dto.AboutProfileUpdateDTO;
import com.steve0v0.home.entity.AboutProfile;
import com.steve0v0.home.mapper.AboutProfileMapper;
import com.steve0v0.home.vo.AboutProfileVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AboutProfileServiceTest {

    @Mock
    private AboutProfileMapper aboutProfileMapper;

    @InjectMocks
    private AboutProfileService aboutProfileService;

    @Test
    void getProfileReturnsEmptyProfileWhenRecordDoesNotExist() {
        when(aboutProfileMapper.selectById(1L)).thenReturn(null);

        AboutProfileVO result = aboutProfileService.getProfile();

        assertEquals("", result.getName());
        assertEquals("", result.getAvatarUrl());
    }

    @Test
    void updateProfileUsesFixedIdAndUpsert() {
        AboutProfileUpdateDTO dto = new AboutProfileUpdateDTO();
        dto.setName("steve0v0");
        dto.setAvatarUrl("https://example.com/avatar.png");

        aboutProfileService.updateProfile(dto);

        ArgumentCaptor<AboutProfile> captor = ArgumentCaptor.forClass(AboutProfile.class);
        verify(aboutProfileMapper).upsert(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals("steve0v0", captor.getValue().getName());
        assertEquals("https://example.com/avatar.png", captor.getValue().getAvatarUrl());
    }

    @Test
    void updateProfileRejectsNonHttpsAvatar() {
        AboutProfileUpdateDTO dto = new AboutProfileUpdateDTO();
        dto.setName("steve0v0");
        dto.setAvatarUrl("http://example.com/avatar.png");

        assertThrows(BusinessException.class, () -> aboutProfileService.updateProfile(dto));
    }
}
