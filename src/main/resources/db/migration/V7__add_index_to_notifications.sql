CREATE INDEX idx_notifications_receiver_deleted_id
    ON notifications (receiver_id, deleted_at, id);