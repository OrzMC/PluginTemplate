#!/usr/bin/env bash
set -euo pipefail

jar_file="${1:-plugin-paper/build/libs/OrzMCPluginTemplate-0.1.0-SNAPSHOT.jar}"
if [[ ! -f "$jar_file" ]]; then
  echo "Plugin JAR not found: $jar_file" >&2
  exit 1
fi

contents="$(jar tf "$jar_file")"
for required in \
  "plugin.yml" \
  "cn/orzmc/template/paper/TemplatePlugin.class" \
  "cn/orzmc/template/core/lifecycle/ModuleManager.class"; do
  if ! grep -Fqx "$required" <<<"$contents"; then
    echo "Required entry missing from JAR: $required" >&2
    exit 1
  fi
done

for forbidden_prefix in "org/bukkit/" "io/papermc/paper/"; do
  if grep -Fq "$forbidden_prefix" <<<"$contents"; then
    echo "Server-provided API was bundled into the plugin JAR: $forbidden_prefix" >&2
    exit 1
  fi
done

echo "JAR structure is valid: $jar_file"
