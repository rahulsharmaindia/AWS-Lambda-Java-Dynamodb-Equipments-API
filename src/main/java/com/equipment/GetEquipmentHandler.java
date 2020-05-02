package com.equipment;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class GetEquipmentHandler implements RequestStreamHandler {

    private DynamoDB dynamoDb;
    private String DYNAMODB_TABLE_NAME = "Equipment";
    private Regions REGION = Regions.US_EAST_1;
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void handleRequest(
            InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        JSONParser parser = new JSONParser();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        JSONObject responseJson = new JSONObject();
        Item result = null;
        try {
            JSONObject event = (JSONObject) parser.parse(reader);
            JSONObject responseBody = new JSONObject();
            //LambdaLogger logger = context.getLogger();
            if (event.get("pathParameters") != null) {
                initDynamoDbClient();
                JSONObject pps = (JSONObject) event.get("pathParameters");
                if (pps.get("equipmentNumber") != null) {
                    Long id = Long.parseLong((String) pps.get("equipmentNumber"));
                    result = dynamoDb.getTable(DYNAMODB_TABLE_NAME).getItem("equipment_number", id);
                }
                if (result != null) {
                    //logger.log(result.toJSON());
                    Equipment equipment = new Equipment(result.toJSON());
                    responseBody.put("Equipment", equipment);
                    responseJson.put("statusCode", 200);
                } else {
                    responseBody.put("message", "No item found");
                    responseJson.put("statusCode", 404);
                }
            } else if (event.get("multiValueQueryStringParameters") != null) {
                JSONObject qps = (JSONObject) event.get("multiValueQueryStringParameters");
                List<Equipment> equipments = new ArrayList<>();
                if (qps.get("limit") != null) {
                    JSONArray array = (JSONArray) qps.get("limit");
                    int limit = Integer.parseInt(array.get(0).toString());
                    ScanRequest scanRequest = new ScanRequest()
                            .withTableName(DYNAMODB_TABLE_NAME).withLimit(limit);
                    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(REGION).build();
                    ScanResult data = client.scan(scanRequest);
                    Equipment equipment;
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    for (Map<String, AttributeValue> item : data.getItems()) {
                        equipment = new Equipment();
                        equipment.setEquipment_number(Long.parseLong(item.get("equipment_number").getN()));
                        equipment.setAddress(item.get("address").getS());
                        equipment.setStatus(item.get("status").getS());
                        try {
                            equipment.setContract_start_date(dateFormat.parse(item.get("contract_start_date").getS()));
                            equipment.setContract_end_date(dateFormat.parse(item.get("contract_end_date").getS()));
                        } catch (java.text.ParseException e) {
                            e.printStackTrace();
                        }
                        equipments.add(equipment);
                        limit--;
                    }
                    if (equipments.size() > 0) {
                        responseBody.put("Equipments", equipments);
                        responseJson.put("statusCode", 200);
                    } else {
                        responseBody.put("message", "No item found");
                        responseJson.put("statusCode", 404);
                    }

                }
            }
            responseJson.put("body", responseBody.toString());

        } catch (ParseException pex) {
            responseJson.put("statusCode", 400);
            responseJson.put("exception", pex);
        }
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toString());
        writer.close();
    }

    private void initDynamoDbClient() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(REGION).build();
        this.dynamoDb = new DynamoDB(client);
    }
}