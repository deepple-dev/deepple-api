delete b
from blocks b
         join blocks kept
              on kept.blocker_id = b.blocker_id
                  and kept.blocked_id = b.blocked_id
                  and kept.id < b.id;

alter table blocks
    add constraint uk_blocks_blocker_id_blocked_id unique (blocker_id, blocked_id);
