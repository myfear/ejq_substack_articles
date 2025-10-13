#!/bin/bash

echo "🔄 Testing Load Balancing - Making 10 requests..."
echo "================================================"

for i in {1..10}; do
    echo -n "Request $i: "
    response=$(curl -s -X POST -H "X-User-Id: user$i" -H "Content-Type: application/json" -d "\"Item $i\"" http://localhost:8080/cart)
    instance=$(echo $response | grep -o '"instance":"[^"]*"' | cut -d'"' -f4)
    hostname=$(echo $response | grep -o '"hostname":"[^"]*"' | cut -d'"' -f4)
    echo "Instance: $instance, Hostname: $hostname"
    sleep 0.5
done

echo "================================================"
echo "✅ Load balancing test completed!"
