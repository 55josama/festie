-- 스키마 생성
CREATE SCHEMA IF NOT EXISTS user_schema;
CREATE SCHEMA IF NOT EXISTS ai_schema;
CREATE SCHEMA IF NOT EXISTS calendar_schema;
CREATE SCHEMA IF NOT EXISTS chat_schema;
CREATE SCHEMA IF NOT EXISTS community_schema;
CREATE SCHEMA IF NOT EXISTS event_schema;
CREATE SCHEMA IF NOT EXISTS notification_schema;
CREATE SCHEMA IF NOT EXISTS favorite_schema;
CREATE SCHEMA IF NOT EXISTS operation_schema;

-- 권한 부여, .env 의 POSTGRES_USER 값: festie 로 통일
GRANT ALL ON SCHEMA user_schema TO festie;
GRANT ALL ON SCHEMA ai_schema TO festie;
GRANT ALL ON SCHEMA calendar_schema TO festie;
GRANT ALL ON SCHEMA chat_schema TO festie;
GRANT ALL ON SCHEMA community_schema TO festie;
GRANT ALL ON SCHEMA event_schema TO festie;
GRANT ALL ON SCHEMA notification_schema TO festie;
GRANT ALL ON SCHEMA favorite_schema TO festie;
GRANT ALL ON SCHEMA operation_schema TO festie;