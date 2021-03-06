-- API_GRAD_TRAX.TRAX_UPDATED_PUB_EVENT definition

CREATE TABLE "TRAX_UPDATED_PUB_EVENT"
(   "EVENT_ID" RAW(16),
     "EVENT_PAYLOAD" BLOB,
     "EVENT_STATUS" VARCHAR2(50),
     "EVENT_TYPE" VARCHAR2(100),
     "ACTIVITY_CODE" VARCHAR2(15),
     "SAGA_ID" RAW(16),
     "EVENT_OUTCOME" VARCHAR2(100),
     "REPLY_CHANNEL" VARCHAR2(100),
     "CREATE_USER" VARCHAR2(32),
     "CREATE_DATE" TIMESTAMP (6) DEFAULT CURRENT_TIMESTAMP,
     "UPDATE_USER" VARCHAR2(32),
     "UPDATE_DATE" TIMESTAMP (6) DEFAULT CURRENT_TIMESTAMP
) SEGMENT CREATION IMMEDIATE
 NOCOMPRESS LOGGING TABLESPACE "API_GRAD_DATA"   NO INMEMORY
 LOB ("EVENT_PAYLOAD") STORE AS SECUREFILE (
  TABLESPACE "API_GRAD_BLOB_DATA" ENABLE STORAGE IN ROW CHUNK 8192
  NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES) ;




