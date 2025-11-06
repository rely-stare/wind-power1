package com.tc.common.exception;

import com.tc.common.http.DataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * Author jiangzhou
 * Date 2023/10/18
 * Description 统一error处理
 **/
@RestControllerAdvice
public class ErrorExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ErrorExceptionHandler.class);

    @ExceptionHandler({Exception.class})
    public DataResponse handleValidationException(Exception ex) {
        logger.info("Exception:", ex);
        List<String> messages = new ArrayList<>();
        if (ex instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException validationException = (MethodArgumentNotValidException) ex;
            for (ObjectError objectError : validationException.getBindingResult().getAllErrors()) {
                messages.add(objectError.getDefaultMessage());
                String resMessages = String.join("; ", messages);
                return new DataResponse(3000, resMessages, null);
            }
        } else if (ex instanceof BindException) {
            BindException bindException = (BindException) ex;
            for (ObjectError objectError : bindException.getAllErrors()) {
                messages.add(objectError.getDefaultMessage());
                String resMessages = String.join("; ", messages);
                return new DataResponse(3000, resMessages, null);
            }
        } else {
            messages.add("系统错误");
        }

        String resMessages = String.join("; ", messages);
        return new DataResponse(500, resMessages, null);

    }
}
