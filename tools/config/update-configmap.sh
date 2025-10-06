###########################################################
#ENV VARS
###########################################################
envValue=$1
APP_NAME=$2
GRAD_NAMESPACE=$3
COMMON_NAMESPACE=$4
BUSINESS_NAMESPACE=$5
SPLUNK_TOKEN=$6
APP_LOG_LEVEL=$7
STUDENT_ADMIN_URL_ROOT=$8
GRAD_SCHOOL_NAMESPACE=${12}

SPLUNK_URL="gww.splunk.educ.gov.bc.ca"
FLB_CONFIG="[SERVICE]
   Flush        1
   Daemon       Off
   Log_Level    info
   HTTP_Server   On
   HTTP_Listen   0.0.0.0
   Parsers_File parsers.conf
[INPUT]
   Name   tail
   Path   /mnt/log/*
   Exclude_Path *.gz,*.zip
   Parser docker
   Mem_Buf_Limit 20MB
   Buffer_Max_Size 1MB
[FILTER]
   Name record_modifier
   Match *
   Record hostname \${HOSTNAME}
[OUTPUT]
   Name   stdout
   Match  absolutely_nothing_bud
   Log_Level    off
[OUTPUT]
   Name  splunk
   Match *
   Host  $SPLUNK_URL
   Port  443
   TLS         On
   TLS.Verify  Off
   Message_Key $APP_NAME
   Splunk_Token $SPLUNK_TOKEN
"
PARSER_CONFIG="
[PARSER]
    Name        docker
    Format      json
"
###########################################################
#Setup for config-maps
###########################################################
echo Creating config map "$APP_NAME"-config-map
oc create -n "$GRAD_NAMESPACE"-"$envValue" configmap "$APP_NAME"-config-map \
  --from-literal=APP_LOG_LEVEL="$APP_LOG_LEVEL" \
  --from-literal=BASELINE_ON_MIGRATE="true" \
  --from-literal=CRON_SCHEDULED_PURGE_OLD_RECORDS="0 0 0 * * *" \
  --from-literal=RECORDS_STALE_IN_DAYS="365" \
  --from-literal=CRON_SCHEDULED_GRAD_TO_TRAX_EVENTS="0 0/5 * * * *" \
  --from-literal=CRON_SCHEDULED_GRAD_TO_TRAX_EVENTS_LOCK_AT_LEAST_FOR="PT1M" \
  --from-literal=CRON_SCHEDULED_GRAD_TO_TRAX_EVENTS_LOCK_AT_MOST_FOR="PT5M" \
  --from-literal=CRON_SCHEDULED_GRAD_TO_TRAX_EVENTS_THRESHOLD="1000" \
  --from-literal=CRON_SCHEDULED_TRAX_TO_GRAD_EVENTS="0/30 * * * * *" \
  --from-literal=CRON_SCHEDULED_TRAX_TO_GRAD_EVENTS_LOCK_AT_LEAST_FOR="29000ms" \
  --from-literal=CRON_SCHEDULED_TRAX_TO_GRAD_EVENTS_LOCK_AT_MOST_FOR="30000ms" \
  --from-literal=CRON_SCHEDULED_TRAX_TO_GRAD_EVENTS_THRESHOLD="500" \
  --from-literal=CRON_SCHEDULED_TRIGGER_TRAX_UPDATES="0/30 * * * * *" \
  --from-literal=CRON_SCHEDULED_TRIGGER_TRAX_UPDATES_LOCK_AT_LEAST_FOR="29000ms" \
  --from-literal=CRON_SCHEDULED_TRIGGER_TRAX_UPDATES_LOCK_AT_MOST_FOR="30000ms" \
  --from-literal=CRON_SCHEDULED_TRIGGER_TRAX_UPDATES_THRESHOLD="200" \
  --from-literal=ENABLE_FLYWAY="true" \
  --from-literal=ENABLE_TRAX_UPDATE="true" \
  --from-literal=KEYCLOAK_TOKEN_URL="https://soam-$envValue.apps.silver.devops.gov.bc.ca/" \
  --from-literal=INSTITUTE_API_URL_ROOT="http://institute-api-master.$COMMON_NAMESPACE-$envValue.svc.cluster.local:8080/" \
  --from-literal=GRAD_SCHOOL_API_URL_ROOT="http://grad-school-api-master.$GRAD_SCHOOL_NAMESPACE-$envValue.svc.cluster.local:8080/" \
  --from-literal=MAX_RETRY_ATTEMPTS="3" \
  --from-literal=SCHOOL_CACHE_EXPIRY_IN_MINS="240" \
  --from-literal=STUDENT_ADMIN_URL_ROOT="$STUDENT_ADMIN_URL_ROOT" \
  --from-literal=MAXIMUM_POOL_SIZE='15' \
  --from-literal=MAX_LIFETIME='300000' \
  --from-literal=ENABLE_COMPRESSION="true" \
  --dry-run=client -o yaml | oc apply -f -

echo Creating config map "$APP_NAME"-flb-sc-config-map
oc create -n "$GRAD_NAMESPACE"-"$envValue" configmap "$APP_NAME"-flb-sc-config-map \
  --from-literal=fluent-bit.conf="$FLB_CONFIG" \
  --from-literal=parsers.conf="$PARSER_CONFIG" \
  --dry-run=client -o yaml | oc apply -f -

# Create/Update secret to access Redis cluster
REDIS_SECRET=$(oc get secret -n "$GRAD_NAMESPACE"-"$envValue" redis-ha -o json | jq --raw-output '.data.REDIS_PASSWORD' | base64 --decode)
oc create secret generic -n "$GRAD_NAMESPACE"-"$envValue" redis-cache-secret \
  --from-literal=REDIS_URL=$9 \
  --from-literal=REDIS_PORT="${10}" \
  --from-literal=REDIS_USER="${11}" \
  --from-literal=REDIS_SECRET="$REDIS_SECRET" \
  --dry-run=client -o yaml | oc apply -f -
