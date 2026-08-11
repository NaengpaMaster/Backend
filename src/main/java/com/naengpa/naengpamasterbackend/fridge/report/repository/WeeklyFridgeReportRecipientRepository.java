package com.naengpa.naengpamasterbackend.fridge.report.repository;

import com.naengpa.naengpamasterbackend.fridge.entity.Fridge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WeeklyFridgeReportRecipientRepository extends JpaRepository<Fridge, Long> {

    @Query(value = """
            SELECT
                f.fridge_id AS fridgeId,
                f.name AS fridgeName,
                m.member_id AS receiverMemberId,
                m.email AS receiverEmail
            FROM fridges f
            JOIN members m ON m.member_id = f.owner_member_id
            WHERE f.status = 'ACTIVE'
              AND m.status = 'ACTIVE'
              AND m.role = 'USER'
              AND m.deleted_at IS NULL
              AND m.email IS NOT NULL
              AND NOT EXISTS (
                  SELECT 1
                  FROM subscriptions s
                  WHERE s.fridge_id = f.fridge_id
                    AND s.status IN ('TRIALING', 'ACTIVE')
              )
            UNION
            SELECT
                f.fridge_id AS fridgeId,
                f.name AS fridgeName,
                m.member_id AS receiverMemberId,
                m.email AS receiverEmail
            FROM fridges f
            JOIN subscriptions s ON s.fridge_id = f.fridge_id
            JOIN fridge_members fm ON fm.fridge_id = f.fridge_id
            JOIN members m ON m.member_id = fm.member_id
            WHERE f.status = 'ACTIVE'
              AND s.status IN ('TRIALING', 'ACTIVE')
              AND fm.status = 'ACTIVE'
              AND m.status = 'ACTIVE'
              AND m.role = 'USER'
              AND m.deleted_at IS NULL
              AND m.email IS NOT NULL
            """, nativeQuery = true)
    List<WeeklyFridgeReportRecipientProjection> findWeeklyReportRecipients();
}
