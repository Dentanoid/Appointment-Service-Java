package org.example4.DatabaseManagement;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example4.DatabaseManagement.Schemas.Appointments;
import org.example4.DatabaseManagement.Schemas.AvailableTimes;
import org.example4.DatabaseManagement.Schemas.CollectionSchema;

import static com.mongodb.client.model.Filters.eq;

import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;

public class PayloadParser {
    public static String getAttributeValue(String payload, String attributeName, CollectionSchema classSchema) {
        Gson gson = new Gson();
        CollectionSchema schemaObject = gson.fromJson(payload, classSchema.getClass());
        return schemaObject.getDocument().get(attributeName).toString();
    }

    // Convert the payload-string to a document that can be stored in the database
    public static Document convertPayloadToDocument(String payload, CollectionSchema classSchema) {
        Gson gson = new Gson();
        CollectionSchema schemaClass = gson.fromJson(payload, classSchema.getClass());
        return schemaClass.getDocument();
    }

    // Get ObjectId of already existing DB-instance that has content identical to the payload
    public static String getObjectId(String payload, CollectionSchema classSchema, MongoCollection<Document> collection) {
        Document payloadDoc = convertPayloadToDocument(payload, classSchema);
        Document objectIdDoc = queryBySchema(collection, payloadDoc);
        return objectIdDoc == null ? "-1" : objectIdDoc.get("_id").toString();
    }

    // Takes payload as input and queries it according to the attributes of a schema. Returns the documents that has identical content as the payload
    public static Document queryBySchema(MongoCollection<Document> collection, Document payloadDocument) {
        Bson schemaQueryConditions = new Document(payloadDocument);
        Document result = collection.find(schemaQueryConditions).first();
        return result;
    }

    public static Document findDocumentById(String objectId, MongoCollection<Document> collection) {
        return collection.find(eq("_id", new ObjectId(objectId))).first();
    }

    public static Document savePayloadDocument(String payload, CollectionSchema collectionSchema, MongoCollection<Document> collection) {
        Document payloadDoc = convertPayloadToDocument(payload, collectionSchema);

        collection.insertOne(payloadDoc);
        return payloadDoc;
    }
}
