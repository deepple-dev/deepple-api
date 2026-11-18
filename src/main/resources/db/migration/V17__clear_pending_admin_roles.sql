UPDATE admins
SET role = NULL
WHERE approval_status = 'PENDING';
