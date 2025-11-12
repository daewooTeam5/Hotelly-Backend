package daewoo.team5.hotelreservation.global.core.filter;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

@Component
@RequiredArgsConstructor
public class HttpRequestEndPointChecker  {

    private final DispatcherServlet servlet;


    public boolean isEndpointExist(HttpServletRequest request) {

        for (HandlerMapping handlerMapping : servlet.getHandlerMappings()) {
            try {
                System.out.println(handlerMapping.getHandler(request).toString());
                HandlerExecutionChain foundHandler = handlerMapping.getHandler(request);
                if (foundHandler != null) {
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}