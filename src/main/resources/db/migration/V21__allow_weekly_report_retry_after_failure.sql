-- =========================================================
-- Allow weekly fridge report retry after failed delivery
-- =========================================================

ALTER TABLE weekly_fridge_report_delivery_logs
    DROP CONSTRAINT IF EXISTS uk_weekly_report_delivery;

CREATE UNIQUE INDEX IF NOT EXISTS uk_weekly_report_success_delivery
    ON weekly_fridge_report_delivery_logs(fridge_id, receiver_member_id, report_week)
    WHERE status = 'SUCCESS';
