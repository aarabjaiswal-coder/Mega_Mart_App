package com.megamart.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "com.megamart.controller")
public class PageExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(PageExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return "error";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleConflict(Model model) {
        model.addAttribute("message", "That value is already in use. Check the SKU and try again.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception exception, Model model) {
        log.error("Page request failed", exception);
        model.addAttribute("message", "We couldn't complete that request. Please try again.");
        return "error";
    }
}
