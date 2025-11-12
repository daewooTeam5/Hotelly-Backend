package daewoo.team5.hotelreservation.infrastructure.mail;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailTemplate {
    public String getVerificationEmailTemplate(String code) {
        return "<!DOCTYPE html>"
                + "<html lang=\"ko\">"
                + "<head>"
                + "  <meta charset=\"UTF-8\">"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "  <title>hotelly 이메일 인증</title>"
                + "</head>"
                + "<body style=\"font-family: 'Apple SD Gothic Neo', 'Noto Sans KR', Arial, sans-serif; margin: 0; padding: 0; background-color: #f9f9f9;\">"
                + "  <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f9f9f9;\">"
                + "    <tr>"
                + "      <td align=\"center\" style=\"padding: 40px 20px;\">"
                + "        <table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); overflow: hidden; max-width: 600px;\">"
                + "          <tr>"
                + "            <td align=\"center\" style=\"padding: 30px 20px; background-color: #1C1C1E;\">" // <-- 다크한 배경
                + "              <h1 style=\"font-size: 32px; font-weight: bold; color: #D4AF37; margin: 0; font-family: 'Arial Black', Gadget, sans-serif;\">hotelly</h1>" // <-- 골드 컬러
                + "            </td>"
                + "          </tr>"
                + "          <tr>"
                + "            <td align=\"left\" style=\"padding: 40px 50px;\">"
                + "              <h2 style=\"font-size: 24px; font-weight: bold; color: #333333; margin: 0 0 20px 0;\">이메일 인증을 완료해주세요.</h2>"
                + "              <p style=\"font-size: 16px; color: #555555; line-height: 1.6;\">"
                + "                hotelly에 가입해 주셔서 감사합니다.<br>"
                + "                아래 6자리 인증 코드를 입력하여 가입을 완료해주세요."
                + "              </p>"
                + "              <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin: 30px 0;\">"
                + "                <tr>"
                + "                  <td align=\"center\" style=\"background-color: #f1f3f5; border-radius: 8px; padding: 25px 20px;\">"
                + "                    <span style=\"font-size: 36px; font-weight: bold; color: #D4AF37; letter-spacing: 8px; font-family: 'Courier New', Courier, monospace;\">" // <-- 골드 컬러
                +                         code
                + "                    </span>"
                + "                  </td>"
                + "                </tr>"
                + "              </table>"
                + "              <p style=\"font-size: 15px; color: #777777; line-height: 1.6;\">"
                + "                이 인증 코드는 <strong>10분</strong>간 유효합니다.<br>"
                + "                본인이 요청하지 않은 인증 메일이라면 이 메일을 무시하셔도 좋습니다."
                + "              </p>"
                + "            </td>"
                + "          </tr>"
                + "          <tr>"
                + "            <td align=\"center\" style=\"padding: 30px 20px; background-color: #f8f9fa; border-top: 1px solid #eeeeee;\">"
                + "              <p style=\"font-size: 12px; color: #999999; margin: 0;\">"
                + "                &copy; 2025 hotelly. All rights reserved."
                + "              </p>"
                + "            </td>"
                + "          </tr>"
                + "        </table>"
                + "      </td>"
                + "    </tr>"
                + "  </table>"
                + "</body>"
                + "</html>";
    }
}
