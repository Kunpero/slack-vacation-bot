CREATE TABLE VACATION_INFO (
ID                      DECIMAL(22)              PRIMARY KEY,
USER_ID                 VARCHAR2 (21)            NOT NULL,
DATE_FROM               TIMESTAMP                NOT NULL,
DATE_TO                 TIMESTAMP                NOT NULL,
SUBSTITUTION_USER_IDS   VARCHAR2 (512)
);
create SEQUENCE SEQ_VACATION_INFO START WITH 1 INCREMENT by 1;