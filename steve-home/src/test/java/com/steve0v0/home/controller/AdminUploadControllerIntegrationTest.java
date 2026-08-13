package com.steve0v0.home.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AdminUploadControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void markdownUploadRequiresAuthentication() throws Exception {
        MockMultipartFile file = markdownFile("# 未登录");

        mockMvc.perform(multipart("/api/admin/upload/markdown").file(file))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void markdownUploadReturnsContentForAuthenticatedAdmin() throws Exception {
        String markdown = "# 冒烟测试\n\n- 自动导入\n- 自动预览";

        mockMvc.perform(multipart("/api/admin/upload/markdown").file(markdownFile(markdown)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.fileName").value("smoke.md"))
                .andExpect(jsonPath("$.data.content").value(markdown));
    }

    private MockMultipartFile markdownFile(String content) {
        return new MockMultipartFile(
                "file",
                "smoke.md",
                "text/markdown",
                content.getBytes(StandardCharsets.UTF_8));
    }
}
