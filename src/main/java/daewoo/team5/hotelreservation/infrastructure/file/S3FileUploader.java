package daewoo.team5.hotelreservation.infrastructure.file;


import daewoo.team5.hotelreservation.global.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RequiredArgsConstructor
public class S3FileUploader extends FileUploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloudfront.domain}")
    private String cloudFrontDomain;

    @Override
    public UploadResult uploadFile(MultipartFile file, String fileName) {
        String uniqueName = UUID.randomUUID() + "-" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + fileName;
        String key = "uploads/" + uniqueName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 실패","S3에 파일 업로드 중 오류가 발생했습니다.");
        }

        // CloudFront 도메인으로 URL 구성!
        String fileUrl = String.format("%s/%s", cloudFrontDomain, key);
        UploadResult uploadResult = new UploadResult(fileUrl, uniqueName, extractExtension(fileName), toGeneralFileType(file.getContentType()));
        System.out.println("asdfasdf");
        System.out.println(uploadResult);
        return uploadResult;
    }
}
