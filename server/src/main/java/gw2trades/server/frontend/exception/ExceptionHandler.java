package gw2trades.server.frontend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@ControllerAdvice
public class ExceptionHandler {
    @ResponseStatus(value= HttpStatus.NOT_FOUND)
    @org.springframework.web.bind.annotation.ExceptionHandler(ItemNotFoundException.class)
    public ModelAndView itemNotFoundException() {
        return new ModelAndView("error_404");
    }
}
