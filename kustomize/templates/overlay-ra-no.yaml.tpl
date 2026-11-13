apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
namespace: $NAMESPACE

resources:
  - ../../../base

labels:
  - pairs:
      app.kubernetes.io/instance: $APP_INSTANCE
      fintlabs.no/org-id: $ORG_ID

patches:
  - patch: |-
      # ra-no gets the authorization client from 1Password, with other key names.
      - op: replace
        path: "/spec/env/1/valueFrom/secretKeyRef/key"
        value: "fint.flyt.authorization.sso.client-id"
      - op: replace
        path: "/spec/env/2/valueFrom/secretKeyRef/key"
        value: "fint.flyt.authorization.sso.client-secret"
      - op: replace
        path: "/spec/kafka/acls/0/topic"
        value: "$KAFKA_TOPIC"
      - op: replace
        path: "/spec/orgId"
        value: "$ORG_ID"
      - op: add
        path: "/spec/env/-"
        value:
          name: "novari.kafka.topic.orgId"
          value: "$FINT_KAFKA_TOPIC_ORGID"
      - op: add
        path: "/spec/env/-"
        value:
          name: "novari.flyt.mapping-service.intgr-conf-client.base-url"
          value: "$INTGR_CONF_BASE_URL"
    target:
      kind: Application
      name: fint-flyt-mapping-service
