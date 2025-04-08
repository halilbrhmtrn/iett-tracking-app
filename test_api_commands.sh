#!/bin/bash

# Set base URL for API
API_URL="http://localhost:8080/api"

# Terminal colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}==============================================================${NC}"
echo -e "${GREEN}IETT Bus Tracking System API Test Script${NC}"
echo -e "${BLUE}==============================================================${NC}"
echo ""

# Test API Health
echo -e "${BLUE}=== Testing API Health ====${NC}"
echo "Testing API health endpoint:"
curl -s http://localhost:8080/api/health | jq .

# Test Database Health
echo -e "\n\n${BLUE}=== Testing Database Health ====${NC}"
echo "Testing database connection:"
curl -s http://localhost:8080/api/db-health | jq .

# Seed Database
echo -e "\n\n${BLUE}=== Seeding Database ====${NC}"
echo "Populating database with test data:"
curl -s -X POST http://localhost:8080/api/seed | jq .

# Test Garage APIs
echo -e "\n\n${BLUE}=== GARAGE API TESTS ====${NC}"

echo -e "\n${GREEN}1. List all garages (first page, 5 per page):${NC}"
curl -s "http://localhost:8080/api/garages?page=0&size=5" | jq .

echo -e "\n\n${GREEN}2. Get garage by ID (ID 99):${NC}"
curl -s "http://localhost:8080/api/garages/99" | jq .

echo -e "\n\n${GREEN}3. Search garages with term 'iki':${NC}"
curl -s "http://localhost:8080/api/garages/search?term=iki" | jq .

echo -e "\n\n${GREEN}4. Force refresh of garage data from SOAP:${NC}"
curl -s "http://localhost:8080/api/garages/refresh" | jq .

echo -e "\n\n${GREEN}5. Create a new garage:${NC}"
NEW_GARAGE_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" \
  -d '{"garageName":"Test Garage","garageCode":"TST","coordinate":"41.0,29.0"}' \
  http://localhost:8080/api/garages)

echo $NEW_GARAGE_RESPONSE | jq .

# Extract the ID of the newly created garage
NEW_GARAGE_ID=$(echo $NEW_GARAGE_RESPONSE | jq -r '.id')

if [ -z "$NEW_GARAGE_ID" ] || [ "$NEW_GARAGE_ID" == "null" ]; then
    echo -e "${RED}Failed to get ID of the newly created garage. Subsequent operations might fail.${NC}"
    # Set a default ID for subsequent operations
    NEW_GARAGE_ID=99
else
    echo -e "\nNew garage created with ID: $NEW_GARAGE_ID"
fi

echo -e "\n\n${GREEN}6. Update garage with ID $NEW_GARAGE_ID:${NC}"
curl -s -X PUT -H "Content-Type: application/json" \
  -d '{"garageName":"Updated Test Garage","garageCode":"UTS","coordinate":"41.1,29.1"}' \
  "http://localhost:8080/api/garages/$NEW_GARAGE_ID" | jq .

# Test Bus APIs
echo -e "\n\n${BLUE}=== BUS API TESTS ====${NC}"

echo -e "\n${GREEN}1. List all buses (first page, 5 per page):${NC}"
curl -s "http://localhost:8080/api/buses?page=0&size=5" | jq .

# Get ID of first bus for testing
FIRST_BUS_RESPONSE=$(curl -s "http://localhost:8080/api/buses?page=0&size=1")
FIRST_BUS_ID=$(echo $FIRST_BUS_RESPONSE | jq '.[0].licensePlate' | tr -d '"' | md5sum | cut -c1-8)
if [ -z "$FIRST_BUS_ID" ] || [ "$FIRST_BUS_ID" == "null" ]; then
    FIRST_BUS_ID=1
fi

echo -e "\n\n${GREEN}2. Get bus by ID ($FIRST_BUS_ID):${NC}"
curl -s "http://localhost:8080/api/buses/$FIRST_BUS_ID" | jq .

echo -e "\n\n${GREEN}3. Get bus by door number:${NC}"
curl -s "http://localhost:8080/api/buses/door/TEST999" | jq .

echo -e "\n\n${GREEN}4. Search buses with term 'Operator1':${NC}"
curl -s "http://localhost:8080/api/buses/search?term=Operator1" | jq .

echo -e "\n\n${GREEN}5. Force refresh of bus data from SOAP:${NC}"
curl -s "http://localhost:8080/api/buses/refresh" | jq .

echo -e "\n\n${GREEN}6. Create a new bus:${NC}"
NEW_BUS_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" \
  -d '{
    "doorNo": "TEST123",
    "operator": "Test Operator",
    "garage": "TST",
    "latitude": 41.05,
    "longitude": 29.02,
    "speed": 42.5,
    "licensePlate": "34 TEST 999"
  }' \
  http://localhost:8080/api/buses)

echo $NEW_BUS_RESPONSE | jq .

# Extract the newly created bus ID
NEW_BUS_DOOR=$(echo $NEW_BUS_RESPONSE | jq -r '.doorNo')

if [ -z "$NEW_BUS_DOOR" ] || [ "$NEW_BUS_DOOR" == "null" ]; then
    echo -e "${RED}Failed to get doorNo of the newly created bus. Subsequent operations might fail.${NC}"
    NEW_BUS_DOOR="TEST123"
else
    echo -e "\nNew bus created with doorNo: $NEW_BUS_DOOR"
fi

echo -e "\n\n${GREEN}7. Update a bus:${NC}"
curl -s -X PUT -H "Content-Type: application/json" \
  -d '{
    "operator": "Updated Operator",
    "garage": "UTS",
    "latitude": 41.06,
    "longitude": 29.03,
    "speed": 30.0,
    "licensePlate": "34 UPDATED 999"
  }' \
  "http://localhost:8080/api/buses/$FIRST_BUS_ID" | jq .

echo -e "\n\n${GREEN}8. Check nearest garage functionality:${NC}"
curl -s "http://localhost:8080/api/buses?page=0&size=1" | jq '.[0] | {doorNo, nearestGarageCode, nearestGarageName, distanceToNearestGarage}'

echo -e "\n\n${GREEN}9. Delete a bus:${NC}"
DELETE_BUS_RESPONSE=$(curl -s -X DELETE -I "http://localhost:8080/api/buses/$FIRST_BUS_ID")
BUS_HTTP_STATUS=$(echo "$DELETE_BUS_RESPONSE" | grep -i "HTTP/" | awk '{print $2}')

if [ "$BUS_HTTP_STATUS" == "204" ]; then
    echo -e "${GREEN}Bus deleted successfully (Status: $BUS_HTTP_STATUS)${NC}"
else
    echo -e "${RED}Failed to delete bus (Status: $BUS_HTTP_STATUS)${NC}"
    echo "$DELETE_BUS_RESPONSE"
fi

echo -e "\n\n${GREEN}10. Delete a garage:${NC}"
DELETE_GARAGE_RESPONSE=$(curl -s -X DELETE -I "http://localhost:8080/api/garages/$NEW_GARAGE_ID")
GARAGE_HTTP_STATUS=$(echo "$DELETE_GARAGE_RESPONSE" | grep -i "HTTP/" | awk '{print $2}')

if [ "$GARAGE_HTTP_STATUS" == "204" ]; then
    echo -e "${GREEN}Garage deleted successfully (Status: $GARAGE_HTTP_STATUS)${NC}"
else
    echo -e "${RED}Failed to delete garage (Status: $GARAGE_HTTP_STATUS)${NC}"
    echo "$DELETE_GARAGE_RESPONSE"
fi

echo -e "\n\n${BLUE}=== API Documentation ====${NC}"
echo "Swagger UI is available at: http://localhost:8080/swagger-ui/index.html"
echo "OpenAPI spec is available at: http://localhost:8080/v3/api-docs"

echo -e "${BLUE}==============================================================${NC}"
echo -e "${GREEN}Test script completed${NC}"
echo -e "${BLUE}==============================================================${NC}"
