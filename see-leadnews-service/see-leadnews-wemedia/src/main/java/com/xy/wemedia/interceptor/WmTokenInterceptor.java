package com.xy.wemedia.interceptor;

import com.xy.model.wemedia.pojos.WmUser;
import com.xy.utils.thread.WmThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * @author 杨路恒
 */
@Slf4j
public class WmTokenInterceptor implements HandlerInterceptor {
    /**
     * 得到header中的用户信息，并且存入到当前线程中
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getHeader("userId");

        Optional<String> optional = Optional.ofNullable(userId);
        if (optional.isPresent()) {
            //把用户id存入threadlocal中
            WmUser wmUser = new WmUser();
            wmUser.setId(Integer.valueOf(userId));
            WmThreadLocalUtil.setUser(wmUser);
            log.info("wmTokenFilter设置用户信息到threadlocal中...");
        }

        return true;
    }

    /**
     * 清理线程中的数据
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        log.info("清理threadlocal...");
//        WmThreadLocalUtil.clear();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        WmThreadLocalUtil.clear();
    }
}
