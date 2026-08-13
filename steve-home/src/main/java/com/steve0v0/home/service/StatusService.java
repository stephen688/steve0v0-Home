package com.steve0v0.home.service;

import com.steve0v0.home.common.constant.Constants;
import com.steve0v0.home.common.exception.BusinessException;
import com.steve0v0.home.common.result.ResultCode;
import com.steve0v0.home.dto.StatusUpdateDTO;
import com.steve0v0.home.entity.Status;
import com.steve0v0.home.mapper.StatusMapper;
import com.steve0v0.home.vo.StatusVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 个人状态服务
 * 单站点模式，固定使用 id=1 的记录
 * 没有记录时返回默认状态
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatusService {

    private final StatusMapper statusMapper;

    private static final Set<String> VALID_STATES = Set.of(
            Constants.STATUS_ONLINE,
            Constants.STATUS_STUDYING,
            Constants.STATUS_BUSY,
            Constants.STATUS_REST
    );

    /**
     * 查询个人状态
     * 没有记录时返回默认状态
     */
    public StatusVO getStatus() {
        Status status = statusMapper.selectById(Constants.STATUS_RECORD_ID);
        StatusVO vo = new StatusVO();
        if (status == null) {
            vo.setState(Constants.STATUS_ONLINE);
            vo.setCurrentTask("");
            vo.setMood("");
        } else {
            vo.setState(status.getState());
            vo.setCurrentTask(status.getCurrentTask() != null ? status.getCurrentTask() : "");
            vo.setMood(status.getMood() != null ? status.getMood() : "");
        }
        return vo;
    }

    /**
     * 修改个人状态
     * 使用 upsert 逻辑，固定操作 id=1 的记录
     */
    public void updateStatus(StatusUpdateDTO dto) {
        if (dto.getState() != null && !VALID_STATES.contains(dto.getState())) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "状态值无效，仅支持：online / studying / busy / rest");
        }

        Status existing = statusMapper.selectById(Constants.STATUS_RECORD_ID);
        if (existing == null) {
            // 首次设置状态，插入新记录
            Status status = new Status();
            status.setId(Constants.STATUS_RECORD_ID);
            status.setState(dto.getState() != null ? dto.getState() : Constants.STATUS_ONLINE);
            status.setCurrentTask(dto.getCurrentTask());
            status.setMood(dto.getMood());
            statusMapper.insert(status);
        } else {
            // 已有记录，只更新传入的字段
            if (dto.getState() != null) {
                existing.setState(dto.getState());
            }
            if (dto.getCurrentTask() != null) {
                existing.setCurrentTask(dto.getCurrentTask());
            }
            if (dto.getMood() != null) {
                existing.setMood(dto.getMood());
            }
            statusMapper.updateById(existing);
        }
        log.info("修改个人状态成功 | state={}", dto.getState());
    }
}
