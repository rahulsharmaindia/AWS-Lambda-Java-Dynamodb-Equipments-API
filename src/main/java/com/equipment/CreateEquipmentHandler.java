package com.equipment;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class CreateEquipmentHandler implements RequestHandler<Equipment, Equipment> {

    private DynamoDB dynamoDb;
    private String DYNAMODB_TABLE_NAME = "Equipment";
    private Regions REGION = Regions.US_EAST_1;
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public Equipment handleRequest(
            Equipment equipment, Context context) {
        this.initDynamoDbClient();
        LambdaLogger logger = context.getLogger();
        createEquipment(equipment, logger);
        return equipment;
    }

    private PutItemOutcome createEquipment(Equipment equipment, LambdaLogger logger)
            throws ConditionalCheckFailedException {
        logger.log("EQUIPMENT RECIEVED --"+gson.toJson(equipment));
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String startDate = dateFormat.format(equipment.getContract_start_date());
        String endDate = dateFormat.format(equipment.getContract_end_date());
        return this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
                .putItem(
                        new PutItemSpec().withItem(new Item()
                                .withLong("equipment_number", equipment.getEquipment_number())
                                .withString("address", equipment.getAddress())
                                .withString("contract_start_date", startDate)
                                .withString("contract_end_date", endDate)
                                .withString("status", equipment.getStatus())));
    }

    private void initDynamoDbClient() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(REGION).build();
        this.dynamoDb = new DynamoDB(client);
    }
}