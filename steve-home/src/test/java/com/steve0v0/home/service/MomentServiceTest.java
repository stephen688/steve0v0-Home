package com.steve0v0.home.service;

import com.steve0v0.home.dto.MomentCreateDTO;
import com.steve0v0.home.entity.Moment;
import com.steve0v0.home.entity.MomentImage;
import com.steve0v0.home.mapper.MomentImageMapper;
import com.steve0v0.home.mapper.MomentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MomentServiceTest {

    @Mock
    private MomentMapper momentMapper;

    @Mock
    private MomentImageMapper momentImageMapper;

    @Test
    void createMomentPreservesOriginalTimeAndImageOrder() {
        MomentService service = new MomentService(momentMapper, momentImageMapper);
        LocalDateTime originalTime = LocalDateTime.of(2025, 9, 9, 20, 27);
        MomentCreateDTO dto = new MomentCreateDTO();
        dto.setContent("孤独是一种灵魂的享受");
        dto.setMediaType("image");
        dto.setLocation("广州市·小洲村");
        dto.setCreatedAt(originalTime);
        dto.setImages(List.of("/moments/01.jpg", "/moments/02.jpg", "/moments/03.jpg"));

        doAnswer(invocation -> {
            Moment moment = invocation.getArgument(0);
            moment.setId(99L);
            return 1;
        }).when(momentMapper).insert(any(Moment.class));

        assertEquals(99L, service.createMoment(dto));

        ArgumentCaptor<Moment> momentCaptor = ArgumentCaptor.forClass(Moment.class);
        verify(momentMapper).insert(momentCaptor.capture());
        assertEquals(originalTime, momentCaptor.getValue().getCreatedAt());
        assertEquals("广州市·小洲村", momentCaptor.getValue().getLocation());

        ArgumentCaptor<MomentImage> imageCaptor = ArgumentCaptor.forClass(MomentImage.class);
        verify(momentImageMapper, times(3)).insert(imageCaptor.capture());
        List<MomentImage> images = imageCaptor.getAllValues();
        assertEquals(List.of(0, 1, 2), images.stream().map(MomentImage::getSort).toList());
        assertEquals(List.of("/moments/01.jpg", "/moments/02.jpg", "/moments/03.jpg"), images.stream().map(MomentImage::getUrl).toList());
    }
}
