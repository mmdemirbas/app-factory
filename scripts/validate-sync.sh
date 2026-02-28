#!/usr/bin/env bash

# Validation Script for Sync Flow
echo "Validating E2E Sync Flow..."

echo "Waiting for backend to be ready..."
sleep 2

# Check initial sync state
echo "Checking Sync State..."
STATE_RES=$(curl -s "http://localhost:8081/api/sync/state")
echo "State Response: $STATE_RES"

# Trigger a sync
echo "Triggering Sync..."
SYNC_RES=$(curl -s -X POST -H "Content-Type: application/json" -d '{"entityType": "app_settings"}' "http://localhost:8081/api/sync/trigger")
echo "Sync Response: $SYNC_RES"

echo "Sync Validation Complete."
