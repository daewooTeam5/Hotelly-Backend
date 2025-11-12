package daewoo.team5.hotelreservation.domain.place.repository;

import daewoo.team5.hotelreservation.domain.file.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByDomainAndDomainFileId(String domain, Long domainFileId);
    List<FileEntity> findByDomainAndDomainFileIdIn(String domain, List<Long> domainFileIds);

    void deleteByDomainAndDomainFileId(String domain, Long domainFileId);
    void deleteByDomainAndDomainFileIdIn(String domain, List<Long> domainFileIds);

    Optional<FileEntity> findFirstByDomainAndDomainFileId(String domain, Long domainFileId);

}