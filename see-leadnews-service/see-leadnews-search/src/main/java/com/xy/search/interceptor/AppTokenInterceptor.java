package com.xy.search.interceptor;

import com.xy.model.user.pojos.ApUser;
import com.xy.model.wemedia.pojos.WmUser;
import com.xy.utils.thread.AppThreadLocalUtil;
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
public class AppTokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getHeader("userId");

        Optional<String> optional = Optional.ofNullable(userId);
        if (optional.isPresent()) {
            //把用户id存入threadlocal中
            ApUser apUser = new ApUser();
            apUser.setId(Integer.valueOf(userId));
            AppThreadLocalUtil.setUser(apUser);
            log.info("wmTokenFilter设置用户信息到threadlocal中...");
        }

        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.info("清理threadlocal...");
        AppThreadLocalUtil.clear();
    }
}
