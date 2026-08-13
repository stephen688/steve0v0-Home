package com.steve0v0.home.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steve0v0.home.common.exception.BusinessException;
import com.steve0v0.home.common.result.ResultCode;
import com.steve0v0.home.dto.AboutProjectCreateDTO;
import com.steve0v0.home.dto.AboutProjectUpdateDTO;
import com.steve0v0.home.entity.AboutProject;
import com.steve0v0.home.mapper.AboutProjectMapper;
import com.steve0v0.home.vo.AboutProjectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 关于页 GitHub 项目服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AboutProjectService {

    private final AboutProjectMapper aboutProjectMapper;

    /**
     * 查询项目列表
     * 按 sort ASC, id DESC 排序
     */
    public List<AboutProjectVO> getProjectList() {
        LambdaQueryWrapper<AboutProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(AboutProject::getSort)
                .orderByDesc(AboutProject::getId);
        return aboutProjectMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询项目详情
     */
    public AboutProjectVO getProjectById(Long id) {
        AboutProject project = aboutProjectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return toVO(project);
    }

    /**
     * 新增项目
     */
    public Long createProject(AboutProjectCreateDTO dto) {
        validateGithubUrl(dto.getGithubUrl());

        AboutProject project = new AboutProject();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setGithubUrl(dto.getGithubUrl());
        project.setTechTags(dto.getTechTags() != null ? new ArrayList<>(dto.getTechTags()) : new ArrayList<>());
        project.setSort(dto.getSort() != null ? dto.getSort() : 0);
        aboutProjectMapper.insert(project);
        log.info("新建 GitHub 项目成功 | id={} | name={}", project.getId(), project.getName());
        return project.getId();
    }

    /**
     * 修改项目
     */
    public void updateProject(Long id, AboutProjectUpdateDTO dto) {
        AboutProject project = aboutProjectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        validateGithubUrl(dto.getGithubUrl());

        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setGithubUrl(dto.getGithubUrl());
        project.setTechTags(dto.getTechTags() != null ? new ArrayList<>(dto.getTechTags()) : new ArrayList<>());
        project.setSort(dto.getSort() != null ? dto.getSort() : 0);
        aboutProjectMapper.updateById(project);
        log.info("修改 GitHub 项目成功 | id={}", id);
    }

    /**
     * 删除项目（物理删除）
     */
    public void deleteProject(Long id) {
        AboutProject project = aboutProjectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        aboutProjectMapper.deleteById(id);
        log.info("删除 GitHub 项目成功 | id={}", id);
    }

    private AboutProjectVO toVO(AboutProject project) {
        AboutProjectVO vo = new AboutProjectVO();
        vo.setId(project.getId());
        vo.setName(project.getName());
        vo.setDescription(project.getDescription());
        vo.setGithubUrl(project.getGithubUrl());
        vo.setTechTags(project.getTechTags() != null ? project.getTechTags() : List.of());
        vo.setSort(project.getSort());
        return vo;
    }

    private void validateGithubUrl(String value) {
        try {
            URI uri = URI.create(value);
            String path = uri.getPath();
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || !"github.com".equalsIgnoreCase(uri.getHost())
                    || path == null
                    || path.endsWith("/")) {
                throw new IllegalArgumentException();
            }

            String[] segments = path.split("/");
            if (segments.length != 3 || segments[1].isBlank() || segments[2].isBlank()) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "GitHub地址必须是有效的 HTTPS 仓库地址");
        }
    }
}
