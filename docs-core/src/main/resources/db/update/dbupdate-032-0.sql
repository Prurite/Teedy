-- AI-generated-content
-- Tool: Github Copilot
-- Version: Claude 3.7 Sonnet
-- Usage: I asked the AI to generate a SQL script to update the database schema
--      for a user registration feature and chat messaging system.

create cached table T_USER_REGISTRATION_REQUEST (
    URR_ID_C varchar(36) not null,
    URR_NAME_C varchar(100),
    URR_EMAIL_C varchar(255) not null,
    URR_STATUS_C varchar(20) not null default 'PENDING',
    URR_CREATEDATE_D datetime not null,
    URR_UPDATEDATE_D datetime,
    primary key (URR_ID_C)
);

create index IDX_URR_EMAIL_C on T_USER_REGISTRATION_REQUEST (URR_EMAIL_C);
create index IDX_URR_STATUS_C on T_USER_REGISTRATION_REQUEST (URR_STATUS_C);

create cached table T_CHAT_MESSAGE (
    CHM_ID_C varchar(36) not null,
    CHM_IDSENDER_C varchar(36) not null,
    CHM_IDRECEIVER_C varchar(36) not null,
    CHM_CONTENT_C varchar(4000) not null,
    CHM_CREATEDATE_D datetime not null,
    CHM_READ_B boolean not null,
    CHM_DELETEDATE_D datetime,
    primary key (CHM_ID_C)
);

create index IDX_CHM_IDSENDER_C on T_CHAT_MESSAGE (CHM_IDSENDER_C);
create index IDX_CHM_IDRECEIVER_C on T_CHAT_MESSAGE (CHM_IDRECEIVER_C);
create index IDX_CHM_READ_B on T_CHAT_MESSAGE (CHM_READ_B);

insert into T_CONFIG(CFG_ID_C, CFG_VALUE_C) values('USER_REGISTRATION_ENABLED', 'true');

update T_CONFIG set CFG_VALUE_C = '32' where CFG_ID_C = 'DB_VERSION';