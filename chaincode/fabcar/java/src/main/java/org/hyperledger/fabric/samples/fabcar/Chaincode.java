/*
 * SPDX-License-Identifier: Apache License 2.0
 */

package org.hyperledger.fabric.samples.fabcar;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

public final class Chaincode extends ChaincodeBase {

    @Override
    public Response init(final ChaincodeStub stub) {
        String fcn = stub.getFunction();
        List<String> params = stub.getParameters();
        System.out.printf("init() %s %s\n", fcn, params.toArray());
        return ChaincodeBase.newSuccessResponse();
    }

    @Override
    public Response invoke(final ChaincodeStub stub) {
        try {
            String fcn = stub.getFunction();
            List<String> params = stub.getParameters();
            System.out.printf("invoke() %s %s\n", fcn, params.toArray());

            switch (fcn) {
                case "queryCar":
                    return queryCar(stub, params);
                case "initLedger":
                    return initLedger(stub, params);
                case "createCar":
                    return createCar(stub, params);
                case "queryAllCars":
                    return queryAllCars(stub, params);
                case "changeCarOwner":
                    return changeCarOwner(stub, params);
                default:
                    String errorMessage = String.format("Transaction %s does not exist", fcn);
                    System.out.println(errorMessage);
                    return newErrorResponse(errorMessage);
            }
        } catch (Throwable e) {
            System.out.println(e.toString());
            return newErrorResponse(e);
        }
    }

    private Response queryCar(final ChaincodeStub stub, final List<String> params) {
        System.out.println("QUERY CAR");
        final int keyParam = 0;
        String key = params.get(keyParam);

        String carState = stub.getStringState(key);

        if (carState.isEmpty()) {
            String errorMessage = String.format("Car %s does not exist", key);
            System.out.println(errorMessage);
            return newErrorResponse(errorMessage);
        }

        Genson genson = new Genson();
        Car car = genson.deserialize(carState, Car.class);

        String successMessage = String.format("Found car %s", key);
        return newSuccessResponse(successMessage, genson.serialize(car).getBytes(UTF_8));
    }

    private Response initLedger(final ChaincodeStub stub, final List<String> params) {
        System.out.println("INIT LEDGER");

        String[] carData = {
                "{ \"make\": \"Toyota\", \"model\": \"Prius\", \"color\": \"blue\", \"owner\": \"Tomoko\" }",
                "{ \"make\": \"Ford\", \"model\": \"Mustang\", \"color\": \"red\", \"owner\": \"Brad\" }",
                "{ \"make\": \"Hyundai\", \"model\": \"Tucson\", \"color\": \"green\", \"owner\": \"Jin Soo\" }",
                "{ \"make\": \"Volkswagen\", \"model\": \"Passat\", \"color\": \"yellow\", \"owner\": \"Max\" }",
                "{ \"make\": \"Tesla\", \"model\": \"S\", \"color\": \"black\", \"owner\": \"Adrian\" }",
                "{ \"make\": \"Peugeot\", \"model\": \"205\", \"color\": \"purple\", \"owner\": \"Michel\" }",
                "{ \"make\": \"Chery\", \"model\": \"S22L\", \"color\": \"white\", \"owner\": \"Aarav\" }",
                "{ \"make\": \"Fiat\", \"model\": \"Punto\", \"color\": \"violet\", \"owner\": \"Pari\" }",
                "{ \"make\": \"Tata\", \"model\": \"nano\", \"color\": \"indigo\", \"owner\": \"Valeria\" }",
                "{ \"make\": \"Holden\", \"model\": \"Barina\", \"color\": \"brown\", \"owner\": \"Shotaro\" }"
        };

        for (int i = 0; i < carData.length; i++) {
            String key = String.format("CAR%03d", i);

            Genson genson = new Genson();
            Car car = genson.deserialize(carData[i], Car.class);
            String carState = genson.serialize(car);
            stub.putStringState(key, carState);
        }

        return newSuccessResponse("Initialized ledger");
    }

    private Response createCar(final ChaincodeStub stub, final List<String> params) {
        System.out.println("CREATE");
        final int keyParam = 0;
        final int makeParam = 1;
        final int modelParam = 2;
        final int colorParam = 3;
        final int ownerParam = 4;
        String key = params.get(keyParam);
        String make = params.get(makeParam);
        String model = params.get(modelParam);
        String color = params.get(colorParam);
        String owner = params.get(ownerParam);

        String carState = stub.getStringState(key);
        if (!carState.isEmpty()) {
            String errorMessage = String.format("Car %s already exists", key);
            System.out.println(errorMessage);
            return newErrorResponse(errorMessage);
        }

        Genson genson = new Genson();
        Car car = new Car(make, model, color, owner);
        carState = genson.serialize(car);
        stub.putStringState(key, carState);

        String successMessage = String.format("Created car %s", key);
        return newSuccessResponse(successMessage, carState.getBytes(UTF_8));
    }

    private Response queryAllCars(final ChaincodeStub stub, final List<String> params) {
        System.out.println("QUERY ALL CARS");
        final String startKey = "CAR0";
        final String endKey = "CAR999";
        List<Car> cars = new ArrayList<Car>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange(startKey, endKey);

        Genson genson = new Genson();
        for (KeyValue result: results) {
            Car car = genson.deserialize(result.getStringValue(), Car.class);
            cars.add(car);
        }

        byte[] responsePayload = genson.serialize(cars.toArray(new Car[cars.size()])).getBytes();

        String successMessage = String.format("Queried cars by range %s-%s", startKey, endKey);
        return newSuccessResponse(successMessage, responsePayload);
    }

    private Response changeCarOwner(final ChaincodeStub stub, final List<String> params) {
        System.out.println("UPDATE");
        final int keyParam = 0;
        final int ownerParam = 1;
        String key = params.get(keyParam);
        String newOwner = params.get(ownerParam);

        String carState = stub.getStringState(key);

        if (carState.isEmpty()) {
            String errorMessage = String.format("Car %s does not exist", key);
            System.out.println(errorMessage);
            return newErrorResponse(errorMessage);
        }

        Genson genson = new Genson();
        Car car = genson.deserialize(carState, Car.class);

        Car newCar = new Car(car.make, car.model, car.color, newOwner);
        String newCarState = genson.serialize(newCar);
        stub.putStringState(key, newCarState);

        String successMessage = String.format("Updated car %s", key);
        return newSuccessResponse(successMessage, genson.serialize(newCar).getBytes(UTF_8));
    }

}
