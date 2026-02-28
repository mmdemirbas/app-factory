#!/bin/bash
set -e
echo "Starting Google Sheets validation test..."

# To run this, you need a valid user token and a spreadsheet ID
# As this needs a real Google Sheet, we will just echo instructions for manual testing
echo "To test the Google Sheets connector end-to-end:"
echo "1. Start the backend: ./gradlew :backend:run"
echo "2. Authorize via Nango in the UI"
echo "3. Input your Spreadsheet ID in the Field Mapping UI"
echo "4. Trigger a sync"
