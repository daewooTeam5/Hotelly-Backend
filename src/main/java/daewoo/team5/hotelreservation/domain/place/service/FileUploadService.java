package daewoo.team5.hotelreservation.domain.place.service;

import daewoo.team5.hotelreservation.domain.file.entity.FileEntity;
import daewoo.team5.hotelreservation.domain.place.repository.FileRepository;
import daewoo.team5.hotelreservation.domain.users.entity.UsersEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileUploadService {

    private final FileRepository fileRepository;
    private final Path fileStorageLocation;

    // 생성자를 통해 의존성 주입 및 경로 초기화
    @Deprecated
    public FileUploadService(@Value("${file.upload-dir:data}") String uploadDir, FileRepository fileRepository) {
        this.fileRepository = fileRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("파일을 업로드할 디렉토리를 생성할 수 없습니다.", ex);
        }
    }

    @Transactional
    public FileEntity storeProfileImage(MultipartFile multipartFile, UsersEntity user, HttpServletRequest request) {
        // 1. 새 파일의 물리적 저장부터 먼저 수행합니다.
        String originalFileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String savedFileName = UUID.randomUUID().toString() + extension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(savedFileName);
            Files.copy(multipartFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 2. 새 파일의 URL을 만듭니다.
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String fileUrl = baseUrl + "/uploads/" + savedFileName;

            // 3. 기존 File 엔티티를 찾습니다.
            Optional<FileEntity> existingFileOpt = fileRepository.findFirstByDomainAndDomainFileId("profile", user.getId());

            FileEntity fileToSave;
            if (existingFileOpt.isPresent()) {
                // ✅ 기존 File 엔티티가 있으면, 내용만 업데이트합니다. (UPDATE)
                fileToSave = existingFileOpt.get();

                // 기존 물리적 파일 삭제
                try {
                    Files.deleteIfExists(this.fileStorageLocation.resolve(fileToSave.getFilename()));
                } catch (IOException e) {
                    e.printStackTrace(); // 로깅 처리
                }

                // 엔티티 필드 업데이트
                fileToSave.setFilename(savedFileName);
                fileToSave.setUrl(fileUrl);
                fileToSave.setExtension(extension);

            } else {
                // ✅ 기존 File 엔티티가 없으면, 새로 생성합니다. (INSERT)
                fileToSave = FileEntity.builder()
                        .user(user)
                        .filename(savedFileName)
                        .extension(extension)
                        .filetype("image")
                        .domain("profile")
                        .domainFileId(user.getId())
                        .url(fileUrl)
                        .build();
            }

            // 4. 업데이트되거나 새로 생성된 File 엔티티를 DB에 저장합니다.
            return fileRepository.save(fileToSave);

        } catch (IOException ex) {
            throw new RuntimeException(savedFileName + " 파일을 저장할 수 없습니다.", ex);
        }
    }
}