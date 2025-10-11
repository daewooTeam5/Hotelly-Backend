package daewoo.team5.hotelreservation.infrastructure.file;

import org.springframework.web.multipart.MultipartFile;

public abstract class FileUploader {
    // 파일을 로컬에 저장하고 결과(경로, 실제 저장 파일명, 확장자, 파일타입)를 반환
    abstract public UploadResult uploadFile(MultipartFile file, String fileName);

    protected String extractExtension(String filename) {
        if (filename == null) return null;
        int idx = filename.lastIndexOf('.');
        if (idx == -1 || idx == filename.length() - 1) return null;
        return filename.substring(idx + 1).toLowerCase();
    }

    protected String removeExtensionSafe(String filename) {
        if (filename == null) return null;
        int idx = filename.lastIndexOf('.');
        if (idx <= 0) return filename; // no dot or hidden file like .env
        return filename.substring(0, idx);
    }

    protected String guessExtensionFromContentType(String contentType) {
        if (contentType == null) return null;
        switch (contentType) {
            case "image/jpeg":
                return "jpg";
            case "image/png":
                return "png";
            case "image/gif":
                return "gif";
            case "image/webp":
                return "webp";
            case "video/mp4":
                return "mp4";
            case "application/pdf":
                return "pdf";
            default:
                return null;
        }
    }

    protected String toGeneralFileType(String contentType) {
        if (contentType == null) return "document";
        if (contentType.startsWith("image/")) return "image";
        if (contentType.startsWith("video/")) return "video";
        return "document";
    }
}
