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
    target:
      kind: Application
      name: fint-flyt-mapping-service
