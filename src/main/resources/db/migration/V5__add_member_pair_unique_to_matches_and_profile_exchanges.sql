create unique index uk_matches_active_member_pair on matches (
    (case when status not in ('EXPIRED', 'REJECT_CHECKED') then least(requester_id, responder_id) end),
    (case when status not in ('EXPIRED', 'REJECT_CHECKED') then greatest(requester_id, responder_id) end)
);

create unique index uk_profile_exchanges_member_pair on profile_exchanges (
    (least(requester_id, responder_id)),
    (greatest(requester_id, responder_id))
);
