package com.lec.spring.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {   // 로그아웃 성공직후 호출됨
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        System.out.println("### 로그아웃 성공 : CustomLogoutSuccessHandler 동작 ###");


        // 로그아웃 시간 남기기
        LocalDateTime logoutTime = LocalDateTime.now();
        System.out.println("로그아웃시간: " + logoutTime);

        // 사용시간 (로그인 ~ 로그아웃) 계산해보기
        LocalDateTime loginTime = (LocalDateTime) request.getSession().getAttribute("loginTime");
        if(loginTime != null) {
            long seconds = loginTime.until(logoutTime, ChronoUnit.SECONDS);    // 로그인 타임 ~ 로그아웃 타임 초단위로 계산
            System.out.println("사용시간: " + seconds + "초");
        }
        request.getSession().invalidate();  // session invalidate

        String redirectUrl = "/user/login?logoutHandler";

        // return_url 이 있는 경유 logout 하고 url 로 redirect
        if(request.getParameter("ret_url") != null) {
            redirectUrl = request.getParameter("ret_url");
        }

        response.sendRedirect(redirectUrl);
    }
}
