alter table members
    drop column mission_heart_balance,
    drop column purchase_heart_balance;

drop table if exists processed_heart_orders;

delete
from heart_usage_policies
where transaction_type = 'MESSAGE_ACCEPTED';
