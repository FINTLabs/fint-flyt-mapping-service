#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TEMPLATE_DIR="$ROOT/kustomize/templates"
DEFAULT_TEMPLATE="$TEMPLATE_DIR/overlay.yaml.tpl"

select_template() {
  local namespace="$1"
  local environment="$2"
  local -a candidates=()

  if [[ -n "$namespace" && -n "$environment" ]]; then
    candidates+=("$TEMPLATE_DIR/overlay-${namespace}-${environment}.yaml.tpl")
  fi

  if [[ -n "$namespace" ]]; then
    candidates+=("$TEMPLATE_DIR/overlay-${namespace}.yaml.tpl")
  fi

  if [[ -n "$environment" ]]; then
    candidates+=("$TEMPLATE_DIR/overlay-${environment}.yaml.tpl")
  fi

  candidates+=("$DEFAULT_TEMPLATE")

  for candidate in "${candidates[@]}"; do
    if [[ -f "$candidate" ]]; then
      printf '%s\n' "$candidate"
      return 0
    fi
  done

  printf 'Unable to find a template for %s/%s\n' "$namespace" "$environment" >&2
  exit 1
}

while IFS= read -r file; do
  rel="${file#"$ROOT/kustomize/overlays/"}"
  dir="$(dirname "$rel")"

  namespace="${dir%%/*}"
  env_name="${dir#*/}"
  if [[ "$env_name" == "$namespace" ]]; then
    env_name=""
  fi

  template="$(select_template "$namespace" "$env_name")"

  export NAMESPACE="$namespace"
  export ORG_ID="${namespace//-/.}"
  export APP_INSTANCE="fint-flyt-mapping-service_${namespace//-/_}"
  export KAFKA_TOPIC="${namespace}.flyt.*"
  export FINT_KAFKA_TOPIC_ORGID="$namespace"

  tmp="$(mktemp)"
  envsubst '$NAMESPACE $ORG_ID $APP_INSTANCE $KAFKA_TOPIC $FINT_KAFKA_TOPIC_ORGID' < "$template" > "$tmp"
  mv "$tmp" "$file"
done < <(find "$ROOT/kustomize/overlays" -name kustomization.yaml -print | sort)
