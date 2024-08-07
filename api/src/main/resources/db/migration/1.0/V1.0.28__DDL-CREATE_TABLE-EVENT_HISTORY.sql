CREATE TABLE "EVENT_HISTORY"
(	"EVENT_HISTORY_ID" RAW(16) DEFAULT SYS_GUID() NOT NULL ENABLE,
     "EVENT_ID" RAW(16) NOT NULL ENABLE,
     "ACKNOWLEDGE_FLAG" VARCHAR2(1) NOT NULL ENABLE,
     "CREATE_USER" VARCHAR2(32) DEFAULT USER NOT NULL ENABLE,
     "CREATE_DATE" DATE DEFAULT SYSTIMESTAMP NOT NULL ENABLE,
     "UPDATE_USER" VARCHAR2(32) DEFAULT USER NOT NULL ENABLE,
     "UPDATE_DATE" DATE DEFAULT SYSTIMESTAMP NOT NULL ENABLE,

     CONSTRAINT "EVENT_HISTORY_ID_PK" PRIMARY KEY ("EVENT_HISTORY_ID")
         USING INDEX TABLESPACE "API_GRAD_IDX"  ENABLE
) SEGMENT CREATION IMMEDIATE
 NOCOMPRESS LOGGING
  TABLESPACE "API_GRAD_DATA"   NO INMEMORY ;