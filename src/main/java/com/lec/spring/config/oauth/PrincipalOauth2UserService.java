package com.lec.spring.config.oauth;

import com.lec.spring.config.PrincipalDetails;
import com.lec.spring.config.oauth.provider.FacebookUserInfo;
import com.lec.spring.config.oauth.provider.GoogleUserInfo;
import com.lec.spring.config.oauth.provider.NaverUserInfo;
import com.lec.spring.config.oauth.provider.OAuth2UserInfo;
import com.lec.spring.domain.User;
import com.lec.spring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * OAuth2UserService<OAuth2UserRequest, OAuth2User>(I)
 * └─ DefaultOAuth2UserService
 */

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {
    // 인증 후 '후처리'를 작성

    @Autowired
    private UserService userService;    // 회원조회, 가입용

//    @Autowired
//    private PasswordEncoder passwordEncoder;    // 회원 가입시 PW 인코딩

    @Value("${app.oauth2.password}")
    private String oauth2Password;  // OAuth2 회원가입시 기본 PW


    // loadUser
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("OAuth2UserService.loaduser() 호출");

        OAuth2User oAuth2User = super.loadUser(userRequest);    // 사용자 프로필 정보 가쟈오기
        // 어떠한 정보가 넘어오는지
        System.out.println("""
                  ClientRegistration: %s
                  RegistrationId: %s
                  AccessToken: %s
                  OAuth2User Attributes : %s
                """.formatted(
                userRequest.getClientRegistration()  // Client Resgistration
                , userRequest.getClientRegistration().getRegistrationId()
                , userRequest.getAccessToken().getTokenValue()  // access token
                , oAuth2User.getAttributes()    // Map<String, Object>  <= 사용자 프로필 정보
        ));

        // 후처리 : 회원가입
        String provider = userRequest.getClientRegistration().getRegistrationId();    // ex: "google"

        OAuth2UserInfo oAuth2UserInfo = switch (provider.toLowerCase()) {
            case "google" -> new GoogleUserInfo(oAuth2User.getAttributes());
            //todo
            case "facebook" -> new FacebookUserInfo(oAuth2User.getAttributes());
            case "naver" -> new NaverUserInfo(oAuth2User.getAttributes());
            default -> null;
        };

        String providerId = oAuth2UserInfo.getProviderId();
        String username = provider + "_" + providerId;  // ex) "google_xxxxx"
        String password = oauth2Password;
        String email = oAuth2UserInfo.getEmail();
        String name = oAuth2UserInfo.getName();

        // 회원가입 진행하기 전에
        // 이미 가입한 회원인지, 혹은 비가입자인지 체크
        User user = userService.findByUsername(username);
        if (user == null) {  // 신규가입인 경우, 회원 가입 진행
            User newUser = User.builder()
                    .username(username)
                    .name(name)
                    .email(email)
                    .password(password)
                    .provider(provider)
                    .providerId(providerId)
                    .build();

            int cnt = userService.register(newUser);  // 회원가입
            if (cnt > 0) {
                System.out.println("[OAuth2 인증. 회원 가입 성공]");
                user = userService.findByUsername(username);    // 다시 읽어오기. regDate 정보 등등
            } else {
                System.out.println("[OAuth2 인증. 회원 가입 실패]");
            }
        } else {
            System.out.println("[OAuth2 인증. 이미 가입된 회원입니다.]");
        }

        PrincipalDetails principalDetails = new PrincipalDetails(user, oAuth2User.getAttributes());
        principalDetails.setUserService(userService);   // 잊지 말것!!
        return principalDetails;
    }
}
