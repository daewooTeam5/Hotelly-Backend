// daewooteam5/hotelreservation-backend/HotelReservation-Backend-feature-review3/src/main/java/daewoo/team5/hotelreservation/domain/place/controller/FileController.java
package daewoo.team5.hotelreservation.domain.place.controller;

import daewoo.team5.hotelreservation.domain.place.service.FileUploadService;
import daewoo.team5.hotelreservation.global.core.common.ApiResult;
import daewoo.team5.hotelreservation.global.exception.ApiException;
import daewoo.team5.hotelreservation.infrastructure.file.FileUploader;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {
    private final FileUploader getFileUploader;


    // 실제 서비스에서는 @Value 등을 통해 외부 설정 파일에서 경로를 관리하는 것이 좋습니다.
  //  private final String uploadDir = Paths.get(System.getProperty("user.dir"), "uploads").toString();

    /**
     * 주석: 여러 이미지 파일을 서버에 업로드하고, 각 파일에 접근할 수 있는 URL 목록을 반환합니다.
     * @param files 클라이언트에서 전송한 MultipartFile 배열
     * @param request HTTP 요청 객체 (서버의 기본 URL을 동적으로 가져오기 위해 사용)
     * @return 서버에서 접근 가능한 이미지 URL 목록
     */
    @PostMapping("/upload")
    @Deprecated
    public ApiResult<List<String>> uploadImages(@RequestParam("files") MultipartFile[] files, HttpServletRequest request) {
        List<String> result = new ArrayList<>();
        for(MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new ApiException(400,"업로드 실패", "파일이 비어 있습니다.");

            }
            result.add(getFileUploader.uploadFile(file,file.getName()).getUrl());
        }
        return ApiResult.ok(result, "이미지 업로드 성공");
    }
}