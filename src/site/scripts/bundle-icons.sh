#!/bin/zsh

# Get the first command-line argument (or use a default)
ICON_FILE="${1:-GesticulateFX.icon}"

# Build the full path
ICON_PATH="${PWD}/src/site/deploy/macos/${ICON_FILE}"
echo "Using icon: $ICON_PATH"

OUTPUT_PATH="${PWD}/src/site/deploy/macos"
PLIST_PATH="$OUTPUT_PATH/assetcatalog_generated_info.plist"
DEVELOPMENT_REGION="en" # Change if necessary

# Adapted from https://github.com/electron/packager/pull/1806/files
actool $ICON_PATH --compile $OUTPUT_PATH \
  --output-format human-readable-text --notices --warnings --errors \
  --output-partial-info-plist $PLIST_PATH \
  --app-icon AppIcon --include-all-app-icons \
  --enable-on-demand-resources NO \
  --development-region $DEVELOPMENT_REGION \
  --target-device mac \
  --minimum-deployment-target 15.0 \
  --platform macosx

rm $PLIST_PATH
