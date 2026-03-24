package com.example.microquest.service;

import com.example.microquest.model.Appeal;
import com.example.microquest.model.AppealStatus;
import com.example.microquest.model.BanRecord;
import com.example.microquest.model.BanTier;
import com.example.microquest.model.UserProfile;
import com.example.microquest.repository.AppealRepository;
import com.example.microquest.repository.BanRecordRepository;
import com.example.microquest.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BanService {

    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");

    private final BanRecordRepository banRecordRepository;
    private final AppealRepository appealRepository;
    private final UserProfileRepository userProfileRepository;
    private final EmailService emailService;

    public BanService(BanRecordRepository banRecordRepository,
                      AppealRepository appealRepository,
                      UserProfileRepository userProfileRepository,
                      EmailService emailService) {
        this.banRecordRepository = banRecordRepository;
        this.appealRepository = appealRepository;
        this.userProfileRepository = userProfileRepository;
        this.emailService = emailService;
    }

    /**
     * Issues a ban with automatic tier escalation:
     *   banCount == 0 → 1-month ban
     *   banCount == 1 → 3-month ban
     *   banCount >= 2 → permanent ban
     */
    public BanRecord issueBan(UserProfile user, String reason) {
        int count = user.getBanCount();
        BanTier tier = count == 0 ? BanTier.ONE_MONTH
                    : count == 1 ? BanTier.THREE_MONTHS
                    : BanTier.PERMANENT;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = switch (tier) {
            case ONE_MONTH     -> now.plusMonths(1);
            case THREE_MONTHS  -> now.plusMonths(3);
            case PERMANENT     -> null;
        };

        BanRecord ban = new BanRecord();
        ban.setUser(user);
        ban.setTier(tier);
        ban.setReason(reason);
        ban.setBannedAt(now);
        ban.setExpiresAt(expiry);
        ban.setAppealStatus(AppealStatus.PENDING);
        banRecordRepository.save(ban);

        user.setBannedUntil(expiry);
        user.setBanCount(count + 1);
        if (tier == BanTier.PERMANENT) {
            user.setActive(false);
        }
        userProfileRepository.save(user);

        // Send email notification (best-effort)
        if (user.getEmail() != null) {
            try {
                if (tier == BanTier.PERMANENT) {
                    emailService.sendPermanentBanEmail(user.getEmail(), user.getDisplayName(), reason);
                } else {
                    String expiryStr = expiry.format(DISPLAY_FMT);
                    emailService.sendTempBanEmail(user.getEmail(), user.getDisplayName(), reason, expiryStr, ban.getId());
                }
            } catch (Exception e) {
                // log only; don't abort the ban
            }
        }

        return ban;
    }

    public void liftBan(UserProfile user) {
        user.setBannedUntil(null);
        user.setActive(true);
        userProfileRepository.save(user);
    }

    public Appeal submitAppeal(Long banRecordId, UserProfile user, String message) {
        BanRecord ban = banRecordRepository.findById(banRecordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ban record not found"));

        if (!ban.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your ban record");
        }

        if (appealRepository.findByBanRecordId(banRecordId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An appeal already exists for this ban");
        }

        Appeal appeal = new Appeal();
        appeal.setBanRecord(ban);
        appeal.setUser(user);
        appeal.setMessage(message);
        appeal.setStatus(AppealStatus.PENDING);
        appeal.setSubmittedAt(LocalDateTime.now());
        appealRepository.save(appeal);

        ban.setAppealStatus(AppealStatus.PENDING);
        banRecordRepository.save(ban);

        return appeal;
    }

    public Appeal reviewAppeal(Long appealId, boolean accept, String adminResponse) {
        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appeal not found"));

        appeal.setStatus(accept ? AppealStatus.ACCEPTED : AppealStatus.REJECTED);
        appeal.setAdminResponse(adminResponse);
        appeal.setReviewedAt(LocalDateTime.now());
        appealRepository.save(appeal);

        appeal.getBanRecord().setAppealStatus(accept ? AppealStatus.ACCEPTED : AppealStatus.REJECTED);
        banRecordRepository.save(appeal.getBanRecord());

        UserProfile user = appeal.getUser();
        if (accept) {
            liftBan(user);
            // Reverse the banCount increment
            if (user.getBanCount() > 0) {
                user.setBanCount(user.getBanCount() - 1);
                userProfileRepository.save(user);
            }
        }

        if (user.getEmail() != null) {
            try {
                emailService.sendAppealDecisionEmail(
                        user.getEmail(), user.getDisplayName(), accept, adminResponse);
            } catch (Exception e) {
                // best-effort
            }
        }

        return appeal;
    }

    @Transactional(readOnly = true)
    public List<BanRecord> getBanHistoryForUser(Long userId) {
        return banRecordRepository.findAllByUserIdOrderByBannedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Appeal> getPendingAppeals() {
        return appealRepository.findAllByStatusOrderBySubmittedAtDesc(AppealStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public Optional<Appeal> getAppealForBan(Long banRecordId) {
        return appealRepository.findByBanRecordId(banRecordId);
    }

    @Transactional(readOnly = true)
    public boolean isCurrentlyBanned(UserProfile user) {
        return !user.isActive()
                || (user.getBannedUntil() != null && user.getBannedUntil().isAfter(LocalDateTime.now()));
    }
}
