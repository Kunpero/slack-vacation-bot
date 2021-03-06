CREATE USER VACATION
IDENTIFIED BY VACATION
DEFAULT TABLESPACE USERS
TEMPORARY TABLESPACE TEMP;

GRANT CREATE SESSION TO VACATION;
GRANT CREATE TABLE TO VACATION;
GRANT CREATE SEQUENCE TO VACATION;
GRANT CREATE VIEW TO VACATION;

ALTER USER vacation QUOTA UNLIMITED ON USERS;
CREATE TABLE VACATION_INFO (
ID                      DECIMAL(22)              PRIMARY KEY,
USER_ID                 VARCHAR2 (21)            NOT NULL,
TEAM_ID                 VARCHAR2 (21)            NOT NULL,
DATE_FROM               TIMESTAMP                NOT NULL,
DATE_TO                 TIMESTAMP                NOT NULL,
SUBSTITUTION_USER_IDS   VARCHAR2 (512),
COMMENT                 VARCHAR2 (512 CHAR),
IS_STATUS_CHANGED       NUMBER(1,0)              NOT NULL DEFAULT 0,
);
create SEQUENCE SEQ_VACATION_INFO START WITH 1 INCREMENT by 1;

CREATE TABLE VACATION_ADMIN (
USER_ID                 VARCHAR (21)        NOT NULL,
TEAM_ID                 VARCHAR (21)        NOT NULL,
CONSTRAINT VACATION_ADMIN_PK PRIMARY KEY (USER_ID)
);