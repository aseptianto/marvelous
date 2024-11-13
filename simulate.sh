#!/bin/bash

# Define the base URL
BASE_URL="localhost:9091"

# Define 10 unique valid requests with multiple parameters
VALID_REQUESTS=(
  '{"name": "Spider-Man (Peter Parker)"}'
  '{"nameStartsWith": "Sp", "limit": 5}'
  '{"modifiedSince": "2021-01-01", "offset": 10}'
  '{"comics": [76863, 76871]}'
  '{"series": [6079]}'
  '{"events": [238, 249]}'
  '{"stories": [1195, 1195, 1499], "orderBy": "-name"}'
  '{"orderBy": "modified", "limit": 10}'
  '{"limit": 20, "offset": 5}'
  '{"name": "Iron Man", "limit": 10}'
)

# Define 15 repeated requests (same as the valid ones)
REPEATED_REQUESTS=(
  "${VALID_REQUESTS[0]}"
  "${VALID_REQUESTS[1]}"
  "${VALID_REQUESTS[2]}"
  "${VALID_REQUESTS[3]}"
  "${VALID_REQUESTS[4]}"
  "${VALID_REQUESTS[5]}"
  "${VALID_REQUESTS[6]}"
  "${VALID_REQUESTS[7]}"
  "${VALID_REQUESTS[8]}"
  "${VALID_REQUESTS[9]}"
  "${VALID_REQUESTS[0]}"
  "${VALID_REQUESTS[1]}"
  "${VALID_REQUESTS[2]}"
  "${VALID_REQUESTS[3]}"
  "${VALID_REQUESTS[4]}"
)

# Define 3 invalid requests
INVALID_REQUESTS=(
  '{"limit": 200}'
  '{"orderBy": "invalid"}'
  '{"comics": "invalid"}'
)

# Define 2 repeated invalid requests
REPEATED_INVALID_REQUESTS=(
  "${INVALID_REQUESTS[0]}"
  "${INVALID_REQUESTS[1]}"
)

# Function to make a request
make_request() {
  local data=$1
  echo "Requesting: $data"
  grpcurl -plaintext -d "$data" $BASE_URL com.andrioseptianto.marvelous.MarvelService/GetCharacters
}

# Run the valid requests
for request in "${VALID_REQUESTS[@]}"; do
  make_request "$request"
done

# Run the repeated requests
for request in "${REPEATED_REQUESTS[@]}"; do
  make_request "$request"
done

# Run the invalid requests
for request in "${INVALID_REQUESTS[@]}"; do
  make_request "$request"
done

# Run the repeated invalid requests
for request in "${REPEATED_INVALID_REQUESTS[@]}"; do
  make_request "$request"
done