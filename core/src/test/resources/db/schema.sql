DROP TABLE IF EXISTS VACATION_INFO;

CREATE TABLE VACATION_INFO (
ID                      BIGINT              PRIMARY KEY,
USER_ID                 VARCHAR (21)        NOT NULL,
TEAM_ID                 VARCHAR (21)        NOT NULL,
DATE_FROM               DATE                NOT NULL,
DATE_TO                 DATE                NOT NULL,
SUBSTITUTION_USER_IDS   VARCHAR (512),
COMMENT                 VARCHAR (512),
IS_STATUS_CHANGED       BOOLEAN             NOT NULL DEFAULT FALSE
);
CREATE SEQUENCE IF NOT EXISTS SEQ_VACATION_INFO START WITH 1 INCREMENT BY 1;

CREATE TABLE VACATION_ADMIN (
USER_ID                 VARCHAR (21)        NOT NULL,
TEAM_ID                 VARCHAR (21)        NOT NULL,
CONSTRAINT VACATION_ADMIN_PK PRIMARY KEY (USER_ID)
);