package com.sunjoo.auth.domain.service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SendMailServiceImpl implements SendMailService {
    private final RedisService redisService;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromMail;
    private String resetPwUrl;


    @Override
    public String sendResetPwdEmail(String email) {
        String uuid = makeUUID();
        String title = "비밀번호 재설정입니다.";
        String cotent = "SUNJOO" + "<br><br>" + "아래 링크를 클릭하면 비밀번호 재설정 페이지로 이동합니다." + "<br>"
                + "<a href =\"" + resetPwUrl + "/" + uuid + "/a>"
                + "<br><br> 해당 링크는 24시간 동안만 유효합니다.";
        sendMail(email, title, cotent);
        saveUuidAndEmail(uuid, email);
        return uuid;
    }

    public void sendMail(String toMail, String title, String content) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new InternetAddress(fromMail));
            helper.setTo(toMail);
            helper.setSubject(title);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String makeUUID() {
        return UUID.randomUUID().toString();
    }

    // UUID와 email을 redis에 저장
    private void saveUuidAndEmail(String uuid, String email) {
        Duration validTime = Duration.ofHours(24);
        redisService.setValues(uuid, email, validTime);
    }
}
