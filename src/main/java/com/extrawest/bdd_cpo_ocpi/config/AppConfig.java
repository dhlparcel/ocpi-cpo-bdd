//package com.extrawest.bdd_cpo_ocpi.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.fasterxml.jackson.databind.json.JsonMapper;
//import jakarta.inject.Singleton;
//
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//
//public class AppConfig {
//
//    @Singleton
//    public ObjectMapper mapper() {
//        DateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm");
//        return JsonMapper.builder()
//                .findAndAddModules()
//                .defaultDateFormat(df)
//                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
//                .build();
//    }
//}
