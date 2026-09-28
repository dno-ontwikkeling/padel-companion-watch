#!/usr/bin/env bash
# Small Google Play Developer API client for promote-release.yml.
#
#   play.sh notes <tag>                          release notes from feat/fix commits since the previous tag
#   play.sh upload <bundle.aab> <track> <notes>  upload a bundle and release it on a track
#
# upload needs PLAY_TOKEN, an OAuth access token with the androidpublisher
# scope (google-github-actions/auth provides it keylessly).
set -euo pipefail

PACKAGE=com.dnodevelopment.padelcompanion
API=https://androidpublisher.googleapis.com/androidpublisher/v3/applications/$PACKAGE
UPLOAD_API=https://androidpublisher.googleapis.com/upload/androidpublisher/v3/applications/$PACKAGE

# Prints the response body; on an HTTP error prints Google's message instead
# and fails, so the workflow log shows why the API refused the call.
call() {
  local method=$1 url=$2 response status
  shift 2
  response=$(curl -sS -w '\n%{http_code}' -X "$method" -H "Authorization: Bearer $PLAY_TOKEN" "$@" "$url")
  status=${response##*$'\n'}
  response=${response%$'\n'*}
  if [ "$status" -ge 400 ]; then
    echo "::error::$method ${url#*"$PACKAGE"} returned HTTP $status: $(jq -r '.error.message // .' <<< "$response")" >&2
    exit 1
  fi
  printf '%s' "$response"
}

case "${1:-}" in
  notes)
    previous=$(git describe --tags --abbrev=0 "$2^" 2>/dev/null || true)
    range=${previous:+$previous..}$2
    # Play allows 500 characters per language.
    git log --format=%s "$range" \
      | grep -E '^(feat|fix)(\([^)]*\))?!?: ' \
      | sed -E 's/^[a-z]+(\([^)]*\))?!?: /- /' \
      | head -c 500 || true
    ;;
  upload)
    bundle=$2 track=$3 notes=$4
    edit=$(call POST "$API/edits" -H 'Content-Type: application/json' -d '{}' | jq -r .id)

    # Fail early with the real track names when the configured one does not
    # exist, e.g. when a Wear OS app uses "wear:production" instead.
    tracks=$(call GET "$API/edits/$edit/tracks" | jq -r '.tracks[].track')
    if ! grep -qxF "$track" <<< "$tracks"; then
      echo "::error::Track '$track' not found. Available tracks: $(tr '\n' ' ' <<< "$tracks")"
      exit 1
    fi

    code=$(call POST "$UPLOAD_API/edits/$edit/bundles?uploadType=media" \
      -H 'Content-Type: application/octet-stream' --data-binary "@$bundle" | jq -r .versionCode)

    language=$(call GET "$API/edits/$edit/details" | jq -r .defaultLanguage)
    body=$(jq -n --arg track "$track" --arg code "$code" --arg lang "$language" --arg notes "$notes" '{
      track: $track,
      releases: [{
        versionCodes: [$code],
        status: "completed",
        releaseNotes: (if $notes == "" then [] else [{language: $lang, text: $notes}] end)
      }]
    }')
    call PUT "$API/edits/$edit/tracks/$track" -H 'Content-Type: application/json' -d "$body" > /dev/null
    call POST "$API/edits/$edit:commit" > /dev/null
    echo "versionCode $code released on '$track'"
    ;;
  *)
    echo "usage: play.sh notes|upload ..." >&2
    exit 1
    ;;
esac
