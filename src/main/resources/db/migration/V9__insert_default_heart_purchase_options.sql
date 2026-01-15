-- Insert default heart purchase options
INSERT INTO heart_purchase_options (created_at, updated_at, deleted_at, amount, name, price, product_id)
VALUES (NOW(6), NOW(6), NULL, 45, '45하트', 9900, 'APP_ITEM_HEART_45'),
       (NOW(6), NOW(6), NULL, 90, '90하트', 15000, 'APP_ITEM_HEART_90'),
       (NOW(6), NOW(6), NULL, 110, '110하트', 22000, 'APP_ITEM_HEART_110'),
       (NOW(6), NOW(6), NULL, 350, '350하트', 59000, 'APP_ITEM_HEART_350'),
       (NOW(6), NOW(6), NULL, 550, '550하트', 88000, 'APP_ITEM_HEART_550');