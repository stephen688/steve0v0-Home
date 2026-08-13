package com.steve0v0.home.service;

import com.steve0v0.home.common.exception.BusinessException;
import com.steve0v0.home.common.result.ResultCode;
import com.steve0v0.home.vo.MarkdownUploadVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传服务
 * 负责图片上传的安全校验和本地存储
 *
 * 安全措施：
 * 1. 大小校验：≤ 5MB
 * 2. 文件签名校验：读取 magic bytes 判断真实格式
 * 3. 解码校验：ImageIO.read() 尝试解码，防止伪装文件
 * 4. UUID 文件名：阻止路径穿越攻击
 * 5. 按日期分目录存储
 */
@Slf4j
@Service
public class UploadService {

    @Value("${steve.upload.path:./upload}")
    private String uploadPath;

    /** 最大文件大小 5MB */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final List<String> MARKDOWN_EXTENSIONS = List.of("md", "markdown");

    /**
     * 文件签名映射
     * key: 文件头 magic bytes 的前几个字节
     * value: 对应的扩展名
     */
    private static final Map<String, String> FILE_SIGNATURES = new HashMap<>();

    static {
        // JPEG: FF D8 FF
        FILE_SIGNATURES.put("FFD8FF", "jpg");
        // PNG: 89 50 4E 47
        FILE_SIGNATURES.put("89504E47", "png");
        // GIF: 47 49 46 38
        FILE_SIGNATURES.put("47494638", "gif");
    }

    /**
     * 上传图片
     *
     * @param file 前端上传的文件
     * @return 图片访问 URL（如 /upload/2026/08/uuid.png）
     */
    public String uploadImage(MultipartFile file) {
        // 1. 空文件检查
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件不能为空");
        }

        // 2. 大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件大小不能超过5MB");
        }

        // 3. 文件签名校验（不只看扩展名和 MIME）
        String extension = verifyFileSignature(file);

        // 4. 尝试解码，确认是有效图片
        verifyImageDecodable(file);

        // 5. 生成 UUID 文件名，阻止路径穿越
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;

        // 6. 按日期分目录
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String relativePath = datePath + "/" + fileName;

        // 7. 保存文件
        try {
            Path uploadDir = Paths.get(uploadPath).toAbsolutePath().resolve(datePath);
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(fileName);
            file.transferTo(filePath.toFile());
            log.info("图片上传成功 | 原文件名: {} | 存储路径: {}", file.getOriginalFilename(), relativePath);
        } catch (IOException e) {
            log.error("图片保存失败", e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件保存失败");
        }

        // 8. 返回访问 URL
        return "/upload/" + relativePath;
    }

    /**
     * 校验并解析 Markdown 文件。
     * Markdown 正文不落盘，直接以 UTF-8 文本返回给文章编辑器。
     */
    public MarkdownUploadVO parseMarkdown(MultipartFile file) {
        validateCommonFile(file);

        String fileName = sanitizeOriginalFilename(file.getOriginalFilename());
        String extension = getExtension(fileName);
        if (!MARKDOWN_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅支持 .md 或 .markdown 文件");
        }

        try {
            byte[] bytes = file.getBytes();
            String content = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
            if (!content.isEmpty() && content.charAt(0) == '\uFEFF') {
                content = content.substring(1);
            }
            if (content.indexOf('\0') >= 0) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "Markdown 文件包含非法二进制内容");
            }
            if (content.isBlank()) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "Markdown 文件内容不能为空");
            }
            return new MarkdownUploadVO(fileName, content);
        } catch (CharacterCodingException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Markdown 文件必须使用 UTF-8 编码");
        } catch (IOException e) {
            log.error("Markdown 文件读取失败", e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Markdown 文件读取失败");
        }
    }

    private void validateCommonFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件大小不能超过5MB");
        }
    }

    private String sanitizeOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Markdown 文件名不能为空");
        }
        String normalized = originalFilename.replace('\\', '/');
        return normalized.substring(normalized.lastIndexOf('/') + 1);
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * 文件签名校验
     * 读取文件头部 magic bytes，判断真实格式
     * 防止通过修改扩展名伪装文件类型
     */
    private String verifyFileSignature(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            int read = is.read(header);
            if (read < 3) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "文件内容不完整");
            }

            // 将前几个字节转为十六进制字符串
            StringBuilder hexBuilder = new StringBuilder();
            for (int i = 0; i < Math.min(read, 4); i++) {
                hexBuilder.append(String.format("%02X", header[i] & 0xFF));
            }
            String hex = hexBuilder.toString();

            // 逐个匹配已知签名
            for (Map.Entry<String, String> entry : FILE_SIGNATURES.entrySet()) {
                if (hex.startsWith(entry.getKey())) {
                    return entry.getValue();
                }
            }

            throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的图片格式，仅支持 JPG/PNG/GIF");
        } catch (IOException e) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件读取失败");
        }
    }

    /**
     * 解码校验
     * 使用 ImageIO 尝试解码图片，失败则拒绝
     * 防止文件头正确但内容损坏或被伪装的文件
     */
    private void verifyImageDecodable(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            if (ImageIO.read(is) == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "文件不是有效的图片");
            }
        } catch (IOException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "图片解码失败");
        }
    }
}
