#!/bin/sh

until $(curl -o /dev/null -s --head --fail $ELASTICSEARCH_CONTAINER_NAME:9200); do
    echo "Waiting for ES to start..."
    sleep 5
done

echo "Pushing place template to elastic search..."
curl http://$ELASTICSEARCH_CONTAINER_NAME:9200/gazetteer/ -X PUT -H "Content-Type: application/json" -d "@/mappings/place_template.json"

echo ""
echo "Starting connector..."
mongo-connector -m $MONGO_CONTAINER_NAME:27017 \
                -t $ELASTICSEARCH_CONTAINER_NAME:9200 \
                -d elastic_doc_manager

cat /mongo-connector.log # Print log if mongo-connector should crash at some point