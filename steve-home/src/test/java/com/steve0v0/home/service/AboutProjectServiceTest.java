package com.steve0v0.home.service;

import com.steve0v0.home.common.exception.BusinessException;
import com.steve0v0.home.dto.AboutProjectCreateDTO;
import com.steve0v0.home.dto.AboutProjectUpdateDTO;
import com.steve0v0.home.entity.AboutProject;
import com.steve0v0.home.mapper.AboutProjectMapper;
import com.steve0v0.home.vo.AboutProjectVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AboutProjectServiceTest {

    @Mock
    private AboutProjectMapper aboutProjectMapper;

    @InjectMocks
    private AboutProjectService aboutProjectService;

    @Test
    void getProjectListMapsProjectFields() {
        AboutProject project = new AboutProject();
        project.setId(1L);
        project.setName("steve-home");
        project.setGithubUrl("https://github.com/example/steve-home");
        project.setTechTags(List.of("Java", "Spring Boot"));
        project.setSort(0);
        when(aboutProjectMapper.selectList(any())).thenReturn(List.of(project));

        List<AboutProjectVO> result = aboutProjectService.getProjectList();

        assertEquals(1, result.size());
        assertEquals("steve-home", result.get(0).getName());
        assertEquals(List.of("Java", "Spring Boot"), result.get(0).getTechTags());
        verify(aboutProjectMapper).selectList(any());
    }

    @Test
    void createProjectStoresDefaultSortAndTags() {
        AboutProjectCreateDTO dto = new AboutProjectCreateDTO();
        dto.setName("steve-home");
        dto.setGithubUrl("https://github.com/example/steve-home");
        dto.setTechTags(List.of("Java"));

        aboutProjectService.createProject(dto);

        ArgumentCaptor<AboutProject> captor = ArgumentCaptor.forClass(AboutProject.class);
        verify(aboutProjectMapper).insert(captor.capture());
        assertEquals(0, captor.getValue().getSort());
        assertEquals(List.of("Java"), captor.getValue().getTechTags());
    }

    @Test
    void createProjectRejectsNonGithubUrl() {
        AboutProjectCreateDTO dto = new AboutProjectCreateDTO();
        dto.setName("steve-home");
        dto.setGithubUrl("https://gitlab.com/example/steve-home");

        assertThrows(BusinessException.class, () -> aboutProjectService.createProject(dto));
        verify(aboutProjectMapper, never()).insert(any(AboutProject.class));
    }

    @Test
    void updateProjectReturnsNotFoundWhenProjectDoesNotExist() {
        AboutProjectUpdateDTO dto = new AboutProjectUpdateDTO();
        dto.setName("steve-home");
        dto.setGithubUrl("https://github.com/example/steve-home");
        when(aboutProjectMapper.selectById(99L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> aboutProjectService.updateProject(99L, dto));
        verify(aboutProjectMapper, never()).updateById(any(AboutProject.class));
    }

    @Test
    void deleteProjectReturnsNotFoundWhenProjectDoesNotExist() {
        when(aboutProjectMapper.selectById(99L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> aboutProjectService.deleteProject(99L));
        verify(aboutProjectMapper, never()).deleteById(99L);
    }
}
