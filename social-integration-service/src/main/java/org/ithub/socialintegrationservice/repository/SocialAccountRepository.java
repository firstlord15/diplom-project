package org.ithub.socialintegrationservice.repository;

import org.ithub.socialintegrationservice.enums.SocialPlatform;
import org.ithub.socialintegrationservice.model.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    Optional<SocialAccount> findByUserIdAndPlatform(Long userId, SocialPlatform platform);
}

