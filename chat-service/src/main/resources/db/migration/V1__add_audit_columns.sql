ALTER TABLE chat_schema.p_chat_room
    ADD COLUMN created_by uuid,
ADD COLUMN updated_by uuid,
ADD COLUMN deleted_by uuid;

ALTER TABLE chat_schema.p_message
    ADD COLUMN created_by uuid,
ADD COLUMN updated_by uuid,
ADD COLUMN deleted_by uuid;