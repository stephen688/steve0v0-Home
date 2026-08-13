package com.steve0v0.home.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steve0v0.home.common.constant.Constants;
import com.steve0v0.home.common.exception.BusinessException;
import com.steve0v0.home.common.pagination.PageResult;
import com.steve0v0.home.common.result.ResultCode;
import com.steve0v0.home.dto.MomentCreateDTO;
import com.steve0v0.home.entity.Moment;
import com.steve0v0.home.entity.MomentImage;
import com.steve0v0.home.mapper.MomentImageMapper;
import com.steve0v0.home.mapper.MomentMapper;
import com.steve0v0.home.vo.MomentListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 动态服务
 * 处理生活动态的创建、发布、删除、列表查询、详情查询
 * 多表操作使用 @Transactional 保证事务一致性
 * 列表查询使用批量查询避免 N+1 问题
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MomentService {

    private final MomentMapper momentMapper;
    private final MomentImageMapper momentImageMapper;

    /**
     * 将 Moment 实体转换为列表 VO，并关联图片
     */
    private MomentListVO toListVO(Moment moment, Map<Long, List<MomentImage>> imageMap) {
        MomentListVO vo = new MomentListVO();
        vo.setId(moment.getId());
        vo.setContent(moment.getContent());
        vo.setMediaType(moment.getMediaType());
        vo.setMediaUrl(moment.getMediaUrl());
        vo.setCreatedAt(moment.getCreatedAt());
        // 从批量查询的图片 Map 中取出该动态的图片
        List<MomentImage> images = imageMap.getOrDefault(moment.getId(), Collections.emptyList());
        vo.setImages(images.stream().map(MomentImage::getUrl).collect(Collectors.toList()));
        return vo;
    }

 //-------------------------------------------------------------------------------------

    /**
     * 公开动态列表（分页倒序）
     * 使用批量查询图片，避免 N+1 问题：
     * 1. 分页查 moment 列表
     * 2. 收集所有 moment_id
     * 3. 一次批量查图片
     * 4. 在 Java 中按 moment_id 分组组装
     */
    public PageResult<MomentListVO> getMomentList(int page, int size) {
        return getMomentList(page, size, "latest");
    }

    public PageResult<MomentListVO> getMomentList(int page, int size, String order) {
        Page<Moment> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Moment> wrapper = new LambdaQueryWrapper<>();
        if ("earliest".equals(order)) {
            wrapper.orderByAsc(Moment::getCreatedAt)
                   .orderByAsc(Moment::getId);
        } else if ("latest".equals(order)) {
            wrapper.orderByDesc(Moment::getCreatedAt)
                   .orderByDesc(Moment::getId);
        } else {
            throw new BusinessException(ResultCode.BAD_REQUEST, "order 仅支持 latest 或 earliest");
        }

        IPage<Moment> result = momentMapper.selectPage(pageParam, wrapper);
        List<Moment> moments = result.getRecords();

        if (CollectionUtils.isEmpty(moments)) {
            return new PageResult<>(page, size, result.getTotal(), Collections.emptyList());
        }

        // 批量查询图片，避免 N+1
        List<Long> momentIds = moments.stream().map(Moment::getId).collect(Collectors.toList());
        List<MomentImage> allImages = momentImageMapper.selectByMomentIds(momentIds);
        // 按 moment_id 分组
        Map<Long, List<MomentImage>> imageMap = allImages.stream()
                .collect(Collectors.groupingBy(MomentImage::getMomentId));

        //
        List<MomentListVO> list = moments.stream()
                .map(m -> toListVO(m, imageMap))
                .collect(Collectors.toList());
        return new PageResult<>(page, size, result.getTotal(), list);
    }

    /**
     * 管理端动态列表
     * 与公开列表一致，预留后续筛选扩展
     */
    public PageResult<MomentListVO> getAdminMomentList(int page, int size) {
        return getMomentList(page, size);
    }

    /**
     * 新建动态
     * 保存动态 + 批量保存图片，事务保证一致性
     */
    @Transactional
    public Long createMoment(MomentCreateDTO dto) {
        Moment moment = new Moment();
        moment.setContent(dto.getContent());
        // mediaType 默认 text，第一阶段仅允许 text 和 image
        String mediaType = dto.getMediaType() != null ? dto.getMediaType() : Constants.MEDIA_TYPE_TEXT;
        if (!Constants.MEDIA_TYPE_TEXT.equals(mediaType) && !Constants.MEDIA_TYPE_IMAGE.equals(mediaType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "第一阶段仅支持 text 和 image 类型");
        }
        moment.setMediaType(mediaType);

        momentMapper.insert(moment);

        // 如果有图片，批量保存
        if (Constants.MEDIA_TYPE_IMAGE.equals(mediaType) && !CollectionUtils.isEmpty(dto.getImages())) {
            saveImages(moment.getId(), dto.getImages());
        }

        log.info("新建动态成功 | id={}", moment.getId());
        return moment.getId();
    }

    /**
     * 删除动态（物理删除）
     * 外键 ON DELETE CASCADE 自动清理关联图片，但仍加事务保险
     */
    @Transactional
    public void deleteMoment(Long id) {
        Moment moment = momentMapper.selectById(id);
        if (moment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // 先手动删除图片，再删除动态（双保险，即使外键配置有误也不会产生孤儿图片）
        deleteImagesByMomentId(id);
        momentMapper.deleteById(id);
        log.info("删除动态成功 | id={}", id);
    }

    /**
     * 批量保存图片
     */
    private void saveImages(Long momentId, List<String> imageUrls) {
        for (int i = 0; i < imageUrls.size(); i++) {
            MomentImage image = new MomentImage();
            image.setMomentId(momentId);
            image.setUrl(imageUrls.get(i));
            image.setSort(i);
            momentImageMapper.insert(image);
        }
    }

    /**
     * 删除某动态的所有图片
     */
    private void deleteImagesByMomentId(Long momentId) {
        LambdaQueryWrapper<MomentImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MomentImage::getMomentId, momentId);
        momentImageMapper.delete(wrapper);
    }
}
