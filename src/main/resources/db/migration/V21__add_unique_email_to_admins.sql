ALTER TABLE admins
    ADD CONSTRAINT uk_admins_email UNIQUE (email);
