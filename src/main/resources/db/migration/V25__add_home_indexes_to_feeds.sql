create index idx_feeds_grade_status on feeds (grade, status);
create index idx_feeds_type_status_is_pinned_published_at on feeds (type, status, is_pinned, published_at);
