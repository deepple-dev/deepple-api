UPDATE admins
SET role = 'ADMIN'
WHERE role = 'GENERAL';

UPDATE admins
SET role = 'SUPER_ADMIN'
WHERE role = 'MASTER';
