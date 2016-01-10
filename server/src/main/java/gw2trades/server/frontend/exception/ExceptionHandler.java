package gw2trades.server.frontend.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@ControllerAdvice
public class ExceptionHandler {
    private static final Logger LOGGER = LogManager.getLogger(ExceptionHandler.class);

    @ResponseStatus(value= HttpStatus.NOT_FOUND)
    @org.springframework.web.bind.annotation.ExceptionHandler(ItemNotFoundException.class)
    public ModelAndView itemNotFoundException(HttpServletRequest request, Exception e) {
        LOGGER.warn("An item was not found ({})", request.getRequestURI(), e);
        return new ModelAndView("error_404");
    }

    @ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR)
    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ModelAndView anyOtherException(HttpServletRequest request, Exception e) {
        LOGGER.error("Failed to load URL {}", request.getRequestURI(), e);
        return new ModelAndView("error_500");
    }
}
