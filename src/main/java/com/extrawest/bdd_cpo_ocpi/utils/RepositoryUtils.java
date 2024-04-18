//package com.extrawest.bdd_cpo_ocpi.utils;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//public class RepositoryUtils {
//
////    public static BulkWriteResult importToCollection(
////            CodecRegistryProvider codecRegistryProvider,
////            MongoTemplate template, String jsonArray, String collectionName) {
////        DecoderContext decoderContext = DecoderContext.builder().build();
////        Codec<Document> documentCodec = codecRegistryProvider.getCodecFor(Document.class)
////                .orElseThrow(() -> new RuntimeException(
////                        "Failed to load entities from json file"));
////        List<InsertOneModel<Document>> bulk = BsonArray.parse(jsonArray)
////                .stream()
////                .map(bsonValue -> documentCodec.decode(
////                        bsonValue.asDocument().asBsonReader(), decoderContext))
////                .map(InsertOneModel::new)
////                .collect(toList());
////        MongoCollection<Document> collection = template.getCollection(collectionName);
////        return collection.bulkWrite(bulk);
////    }
////
////    public static DeleteResult remove(MongoTemplate template, Query query, String collection) {
////        return template.remove(query, collection);
////    }
//}
