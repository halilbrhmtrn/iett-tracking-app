#!/bin/bash
# Test commands for IETT Bus Tracking System API

# Color codes for better readability
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Testing API Health ====${NC}"
echo "Testing API health endpoint:"
curl -s http://localhost:8080/api/health

echo -e "\n\n${BLUE}=== Testing Database Health ====${NC}"
echo "Testing database connection:"
curl -s http://localhost:8080/api/db-health

echo -e "\n\n${BLUE}=== Seeding Database ====${NC}"
echo "Populating database with test data:"
curl -s -X POST http://localhost:8080/api/seed

echo -e "\n\n${BLUE}=== GARAGE API TESTS ====${NC}"

echo -e "\n${GREEN}1. List all garages (first page, 5 per page):${NC}"
curl -s "http://localhost:8080/api/garages?page=0&size=5" | jq .

echo -e "\n\n${GREEN}2. List all garages (second page, 5 per page):${NC}"
curl -s "http://localhost:8080/api/garages?page=1&size=5" | jq .

echo -e "\n\n${GREEN}3. Get garage by ID (ID 1):${NC}"
curl -s "http://localhost:8080/api/garages/1" | jq .

echo -e "\n\n${GREEN}4. Search garages with term 'Ana' (contains Anadolu Garage):${NC}"
curl -s "http://localhost:8080/api/garages/search?term=Ana" | jq .

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
    NEW_GARAGE_ID=11
else
    echo -e "\nNew garage created with ID: $NEW_GARAGE_ID"
fi

echo -e "\n\n${GREEN}6. Get newly created garage (search by name):${NC}"
curl -s "http://localhost:8080/api/garages/search?term=Test" | jq .

echo -e "\n\n${GREEN}7. Update garage with ID $NEW_GARAGE_ID:${NC}"
curl -s -X PUT -H "Content-Type: application/json" \
  -d '{"garageName":"Updated Test Garage","garageCode":"UPD","coordinate":"41.1,29.1"}' \
  "http://localhost:8080/api/garages/$NEW_GARAGE_ID" | jq .

echo -e "\n\n${GREEN}8. Delete garage with ID $NEW_GARAGE_ID:${NC}"
DELETE_RESPONSE=$(curl -s -X DELETE -I "http://localhost:8080/api/garages/$NEW_GARAGE_ID")
HTTP_STATUS=$(echo "$DELETE_RESPONSE" | grep -i "HTTP/" | awk '{print $2}')

if [ "$HTTP_STATUS" == "204" ]; then
    echo -e "${GREEN}Garage deleted successfully (Status: $HTTP_STATUS)${NC}"
else
    echo -e "${RED}Failed to delete garage (Status: $HTTP_STATUS)${NC}"
    echo "$DELETE_RESPONSE"
fi

echo -e "\n\n${BLUE}=== BUS API TESTS ====${NC}"

echo -e "\n${GREEN}1. List all buses (first page, 5 per page):${NC}"
curl -s "http://localhost:8080/api/buses?page=0&size=5" | jq .

echo -e "\n\n${GREEN}2. List all buses (second page, 5 per page):${NC}"
curl -s "http://localhost:8080/api/buses?page=1&size=5" | jq .

echo -e "\n\n${GREEN}3. Get bus by door number (DOOR1):${NC}"
curl -s "http://localhost:8080/api/buses/DOOR1" | jq .

echo -e "\n\n${GREEN}4. Search buses with term 'Operator1':${NC}"
curl -s "http://localhost:8080/api/buses/search?term=Operator1" | jq .

echo -e "\n\n${GREEN}5. Create a new bus:${NC}"
NEW_BUS_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" \
  -d '{
    "doorNo": "DOOR999",
    "operator": "Test Operator",
    "garage": "TST",
    "latitude": 41.05,
    "longitude": 29.02,
    "speed": 42.5,
    "licensePlate": "34 TEST 999"
  }' \
  http://localhost:8080/api/buses)

echo $NEW_BUS_RESPONSE | jq .

echo -e "\n\n${GREEN}6. Get newly created bus:${NC}"
curl -s "http://localhost:8080/api/buses/DOOR999" | jq .

echo -e "\n\n${GREEN}7. Update a bus:${NC}"
curl -s -X PUT -H "Content-Type: application/json" \
  -d '{
    "operator": "Updated Operator",
    "garage": "TST",
    "latitude": 41.06,
    "longitude": 29.03,
    "speed": 30.0,
    "licensePlate": "34 UPDATED 999"
  }' \
  http://localhost:8080/api/buses/DOOR999 | jq .

echo -e "\n\n${GREEN}8. Delete a bus:${NC}"
DELETE_BUS_RESPONSE=$(curl -s -X DELETE -I "http://localhost:8080/api/buses/DOOR999")
BUS_HTTP_STATUS=$(echo "$DELETE_BUS_RESPONSE" | grep -i "HTTP/" | awk '{print $2}')

if [ "$BUS_HTTP_STATUS" == "204" ]; then
    echo -e "${GREEN}Bus deleted successfully (Status: $BUS_HTTP_STATUS)${NC}"
else
    echo -e "${RED}Failed to delete bus (Status: $BUS_HTTP_STATUS)${NC}"
    echo "$DELETE_BUS_RESPONSE"
fi

echo -e "\n\n${BLUE}=== API Documentation ====${NC}"
echo "Swagger UI is available at: http://localhost:8080/swagger-ui.html"
echo "OpenAPI spec is available at: http://localhost:8080/api-docs"
