package com.steve0v0.home.common.exception;

import com.steve0v0.home.common.result.Result;
import com.steve0v0.home.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {
        log.warn("业务异常: {} | 路径: {}", e.getMessage(), request.getRequestURI());
        HttpStatus status = HttpStatus.resolve(e.getCode());
        response.setStatus(status != null && status.isError()
                ? status.value()
                : HttpStatus.INTERNAL_SERVER_ERROR.value());
        return Result.fail(e.getCode(), e.getMessage());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", errors);
        return Result.fail(ResultCode.BAD_REQUEST, errors);
    }


    /**
     * 请求体格式错误（JSON 解析失败、日期格式错误等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMessageNotReadableException(HttpMessageNotReadableException e,
                                                          HttpServletRequest request) {
        log.warn("请求体格式错误 | 路径: {} | 原因: {}", request.getRequestURI(), e.getMostSpecificCause().getMessage());
        return Result.fail(ResultCode.BAD_REQUEST, "请求体格式错误");
    }


    /**
     * 路径或查询参数类型不匹配（如日期格式错误、非法的数字参数）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型不匹配 | 参数: {} | 期望类型: {}", e.getName(),
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        return Result.fail(ResultCode.BAD_REQUEST, "参数格式错误: " + e.getName());
    }


    /**
     * 缺少必填的查询参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingParameterException(MissingServletRequestParameterException e) {
        log.warn("缺少必填参数 | 参数: {}", e.getParameterName());
        return Result.fail(ResultCode.BAD_REQUEST, "缺少必填参数: " + e.getParameterName());
    }


    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        return Result.fail(ResultCode.UNAUTHORIZED);
    }


    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        return Result.fail(ResultCode.FORBIDDEN);
    }


    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFoundException(Exception e) {
        return Result.fail(ResultCode.NOT_FOUND);
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("未捕获异常 | 路径: {} | 异常: ", request.getRequestURI(), e);
        return Result.fail(ResultCode.INTERNAL_ERROR);
    }
}
