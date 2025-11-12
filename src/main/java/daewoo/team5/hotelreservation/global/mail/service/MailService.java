package daewoo.team5.hotelreservation.global.mail.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import daewoo.team5.hotelreservation.domain.payment.entity.ReservationEntity;
import daewoo.team5.hotelreservation.domain.payment.projection.PaymentDetailProjection;
import daewoo.team5.hotelreservation.domain.place.repository.ReservationRepository;
import daewoo.team5.hotelreservation.global.exception.ApiException;
import daewoo.team5.hotelreservation.infrastructure.file.FileUploader;
import daewoo.team5.hotelreservation.infrastructure.file.UploadResult;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
@Deprecated
public class MailService {
    private final JavaMailSender javaMailSender;
    private final ReservationRepository reservationRepository; // ReservationRepository 주입
    private final FileUploader fileUploader; // 파일 업로더 주입
    @Value("${DEPLOY_URL}")
    private String DEPLOY_URL;

    @Async
    public void sendOtpCode(String email, String code) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("호텔 예약 시스템 인증 코드");
            String htmlContent = "<p>안녕하세요,</p>" +
                    "<p>호텔 예약 시스템을 이용해 주셔서 감사합니다.</p>" +
                    "<p>인증 코드는 다음과 같습니다:</p>" +
                    "<h2>" + code + "</h2>" +
                    "<p>이 코드는 10분 동안 유효합니다. 다른 사람이 이 코드를 요청한 경우, 이 이메일을 무시해 주세요.</p>" +
                    "<p>감사합니다.</p>";
            mimeMessageHelper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);
            log.info("메일 전송 성공: {}", email);
        } catch (Exception e) {
            log.error("메일 전송 실패: {}", e.getMessage());
            throw new ApiException(HttpStatus.FAILED_DEPENDENCY, "메일 전송 실패", "메일 전송 중 오류가 발생했습니다. 문제가 지속되면 고객센터로 문의해주세요.");
        }
    }

    @Async
    public void sendReservationConfirmation(String email, PaymentDetailProjection paymentDetail) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            // 업로드된 리소스를 본문에서 참조하므로 MIXED_RELATED 사용
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("[My Hotel] " + paymentDetail.getPlaceName() + " 예약이 완료되었습니다.");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분");

            ReservationEntity reservation = reservationRepository.findById(paymentDetail.getReservationId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "예약 정보를 찾을 수 없습니다.", "이메일 발송 중 예약 정보를 찾지 못했습니다."));

            boolean isMember = reservation.getGuest().getUsers() != null;
            String lastName = reservation.getGuest().getLastName();
            String firstName = reservation.getGuest().getFirstName();
            String fullGuestName = lastName + " " + firstName;

            String reservationDetailUrl;
            if (isMember) {
                reservationDetailUrl = DEPLOY_URL + "/profile/payments/" + paymentDetail.getPaymentId();
            } else {
                reservationDetailUrl = DEPLOY_URL + "/guest/reservation-search" +
                        "?reservationId=" + paymentDetail.getReservationId() +
                        "&lastName=" + URLEncoder.encode(lastName, StandardCharsets.UTF_8) +
                        "&firstName=" + URLEncoder.encode(firstName, StandardCharsets.UTF_8) +
                        "&email=" + email;
            }

            // QR 코드 생성(Base64) -> 바이트 변환 -> MultipartFile 감싸기 -> 업로드
            String qrBase64 = generateQrCodeBase64(paymentDetail.getOrderId());
            byte[] qrBytes = Base64.getDecoder().decode(qrBase64);
            MultipartFile qrFile = new ByteArrayMultipartFile(
                    "qr",
                    "reservation-qr.png",
                    "image/png",
                    qrBytes
            );
            UploadResult uploadResult = fileUploader.uploadFile(qrFile, "reservation-qr-" + paymentDetail.getOrderId());
            String qrUrl = uploadResult.getUrl();

            String htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; background-color: #f7f9fc; margin: 0; padding: 0; }
                .container { max-width: 650px; background: #ffffff; margin: 40px auto; border-radius: 12px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); padding: 30px; }
                .header { text-align: center; border-bottom: 2px solid #007bff; padding-bottom: 15px; }
                .header h1 { color: #007bff; margin: 0; }
                .content p { font-size: 16px; color: #333; line-height: 1.6; }
                .info-section { margin-top: 25px; }
                h3 { border-left: 4px solid #007bff; padding-left: 10px; color: #007bff; }
                ul { list-style: none; padding: 0; margin: 0; }
                li { margin-bottom: 8px; font-size: 15px; }
                li b { color: #555; width: 120px; display: inline-block; }
                .button-container { text-align: center; margin-top: 30px; }
                .button { background-color: #007bff; color: white; padding: 14px 28px; border-radius: 6px; text-decoration: none; font-weight: bold; }
                .qr-container { text-align: center; margin-top: 25px; }
                .qr-container img { width: 150px; height: 150px; }
                .footer { margin-top: 40px; text-align: center; font-size: 12px; color: #888; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>✅ 예약 확정 안내</h1>
                </div>
                <div class="content">
                    <p>안녕하세요, <b>%s</b>님!<br>
                    <b>%s</b> 예약이 성공적으로 완료되었습니다.</p>
                </div>

                <div class="qr-container">
                    <p><b>예약 QR 코드</b></p>
                    <img src="%s" alt="Reservation QR Code" />
                </div>

                <div class="button-container">
                    <a href="%s" class="button">예약 상세 보기</a>
                </div>

                <div class="info-section">
                    <h3>예약 정보</h3>
                    <ul>
                        <li><b>예약 번호:</b> %s</li>
                        <li><b>숙소명:</b> %s</li>
                        <li><b>객실 타입:</b> %s</li>
                        <li><b>체크인:</b> %s %s</li>
                        <li><b>체크아웃:</b> %s</li>
                    </ul>

                    <h3>결제 정보</h3>
                    <ul>
                        <li><b>주문 번호:</b> %s</li>
                        <li><b>결제 금액:</b> <b>%s원</b></li>
                        <li><b>결제 수단:</b> %s</li>
                        <li><b>결제 일시:</b> %s</li>
                    </ul>
                </div>

                <div class="footer">
                    <p>본 메일은 발신전용입니다.<br>문의사항은 고객센터로 연락해주세요.<br><b>My Hotel © 2025</b></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                    fullGuestName,
                    paymentDetail.getPlaceName(),
                    qrUrl,
                    reservationDetailUrl,
                    paymentDetail.getReservationId(),
                    paymentDetail.getPlaceName(),
                    paymentDetail.getRoomType(),
                    paymentDetail.getResevStart(),
                    paymentDetail.getCheckIn(),
                    paymentDetail.getResevEnd(),
                    paymentDetail.getOrderId(),
                    paymentDetail.getFinalAmount().toBigInteger(),
                    paymentDetail.getMethod(),
                    paymentDetail.getTransactionDate().format(formatter)
            );

            // 텍스트/HTML 대체 본문 모두 설정
            String plainContent = (
                    "예약 확정 안내\n" +
                    "고객명: %s\n" +
                    "숙소명: %s\n" +
                    "예약 번호: %s\n" +
                    "객실 타입: %s\n" +
                    "체크인: %s %s\n" +
                    "체크아웃: %s\n" +
                    "주문 번호: %s\n" +
                    "결제 금액: %s원\n" +
                    "결제 수단: %s\n" +
                    "QR URL: %s\n" +
                    "예약 상세: %s\n"
            ).formatted(
                    fullGuestName,
                    paymentDetail.getPlaceName(),
                    paymentDetail.getReservationId(),
                    paymentDetail.getRoomType(),
                    paymentDetail.getResevStart(),
                    paymentDetail.getCheckIn(),
                    paymentDetail.getResevEnd(),
                    paymentDetail.getOrderId(),
                    paymentDetail.getFinalAmount().toBigInteger(),
                    paymentDetail.getMethod(),
                    qrUrl,
                    reservationDetailUrl
            );

            mimeMessageHelper.setText(plainContent, htmlContent);
            javaMailSender.send(mimeMessage);
            log.info("예약 확정 메일 전송 성공: {}", email);
            log.info("qr Url: {}", qrUrl);

        } catch (Exception e) {
            log.error("예약 확정 메일 전송 실패: {}", e.getMessage());
        }
    }

    // ✅ QR 코드 Base64 변환 함수
    private String generateQrCodeBase64(String data) throws IOException, WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        var bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 200, 200);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        ImageIO.write(MatrixToImageWriter.toBufferedImage(bitMatrix), "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(pngData);
    }

    // 내부 헬퍼: 바이트 배열을 MultipartFile로 감싸기
    private static class ByteArrayMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] bytes;

        private ByteArrayMultipartFile(String name, String originalFilename, String contentType, byte[] bytes) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.bytes = bytes != null ? bytes : new byte[0];
        }

        @Override
        public String getName() { return name; }

        @Override
        public String getOriginalFilename() { return originalFilename; }

        @Override
        public String getContentType() { return contentType; }

        @Override
        public boolean isEmpty() { return bytes.length == 0; }

        @Override
        public long getSize() { return bytes.length; }

        @Override
        public byte[] getBytes() { return bytes; }

        @Override
        public InputStream getInputStream() { return new ByteArrayInputStream(bytes); }

        @Override
        public void transferTo(Path dest) throws IOException { Files.write(dest, bytes); }

        @Override
        public void transferTo(File dest) throws IOException {
            Files.write(dest.toPath(), bytes);
        }
    }
}
