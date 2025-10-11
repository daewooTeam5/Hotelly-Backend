package daewoo.team5.hotelreservation.global.configuration;

import daewoo.team5.hotelreservation.infrastructure.file.FileUploader;
import daewoo.team5.hotelreservation.infrastructure.file.LocalFileUploader;
import daewoo.team5.hotelreservation.infrastructure.file.S3FileUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;


@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {
    private final S3Client s3Client;
    @Value("${MODE}")
    private  String MODE;

    @Bean
    public FileUploader getFileUploader() {
        if(MODE.equals("development")){
            return new LocalFileUploader();
        }else{
            return new S3FileUploader(s3Client);
        }
    }
}
