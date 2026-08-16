package com.steve0v0.home.service;

import com.steve0v0.home.common.constant.Constants;
import com.steve0v0.home.common.exception.BusinessException;
import com.steve0v0.home.dto.StatusUpdateDTO;
import com.steve0v0.home.entity.Status;
import com.steve0v0.home.mapper.StatusMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusServiceTest {

    @Mock
    private StatusMapper statusMapper;

    @InjectMocks
    private StatusService statusService;

    @Test
    void updateStatusAcceptsExercisingState() {
        Status existing = new Status();
        existing.setId(Constants.STATUS_RECORD_ID);
        existing.setState(Constants.STATUS_STUDYING);
        when(statusMapper.selectById(Constants.STATUS_RECORD_ID)).thenReturn(existing);

        StatusUpdateDTO dto = new StatusUpdateDTO();
        dto.setState(Constants.STATUS_EXERCISING);
        dto.setCurrentTask("打篮球");

        statusService.updateStatus(dto);

        ArgumentCaptor<Status> captor = ArgumentCaptor.forClass(Status.class);
        verify(statusMapper).updateById(captor.capture());
        assertEquals(Constants.STATUS_EXERCISING, captor.getValue().getState());
        assertEquals("打篮球", captor.getValue().getCurrentTask());
    }

    @Test
    void updateStatusRejectsUnknownState() {
        StatusUpdateDTO dto = new StatusUpdateDTO();
        dto.setState("gaming");

        assertThrows(BusinessException.class, () -> statusService.updateStatus(dto));
        verifyNoInteractions(statusMapper);
    }
}
