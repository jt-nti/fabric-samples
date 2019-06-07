/*
 * SPDX-License-Identifier: Apache License 2.0
 */

package org.hyperledger.fabric.samples.fabcar;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.hyperledger.fabric.shim.Chaincode.Response;
import org.hyperledger.fabric.shim.Chaincode.Response.Status;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public final class ChaincodeTest {

    @Test
    public void testInit() {
        Chaincode cc = new Chaincode();
        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(stub.getFunction()).thenReturn("initFunc");
        when(stub.getParameters()).thenReturn(new ArrayList<String>());

        Response res = cc.init(stub);

        assertThat(res.getStatus()).isEqualTo(Status.SUCCESS);
    }

    @Test
    public void invokeUndefinedTransaction() {
        Chaincode cc = new Chaincode();
        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(stub.getFunction()).thenReturn("TransferAsset");
        when(stub.getParameters()).thenReturn(new ArrayList<String>());

        Response res = cc.invoke(stub);

        assertThat(res.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
        assertThat(res.getMessage()).isEqualTo("Transaction TransferAsset does not exist");
    }

    @Test
    void invokeInitLedgerTransaction() {
        Chaincode cc = new Chaincode();
        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(stub.getFunction()).thenReturn("initLedger");
        when(stub.getParameters()).thenReturn(new ArrayList<String>());
        when(stub.getStringState(anyString())).thenReturn("");

        Response res = cc.invoke(stub);

        assertThat(res.getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(res.getMessage()).isEqualTo("Initialized ledger");
    }

    @ParameterizedTest(name = "Invoke createCar transaction with key {0}. Car already exists: {1}")
    @CsvSource({
          "CAR100, true,  INTERNAL_SERVER_ERROR",
          "CAR100, false, SUCCESS"
    })
    void invokeCreateCarTransaction(final String key, final boolean existingState, final String expectedStatus) {
        Chaincode cc = new Chaincode();
        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(stub.getFunction()).thenReturn("createCar");
        when(stub.getParameters()).thenReturn(Arrays.asList(key, "Nissan", "Leaf", "green", "Elon"));
        when(stub.getStringState(key)).thenReturn(
                existingState ? "{\"color\":\"red\",\"make\":\"Ford\",\"model\":\"Mustang\",\"owner\":\"Brad\"}" : "");

        Response res = cc.invoke(stub);

        String expectedPayload = existingState ? null
                : "{\"color\":\"green\",\"make\":\"Nissan\",\"model\":\"Leaf\",\"owner\":\"Elon\"}";

        assertThat(res.getStatus()).isEqualTo(Status.valueOf(expectedStatus));
        byte[] actualPayloadBytes = res.getPayload();
        String actualPayload = (actualPayloadBytes == null) ? null : new String(res.getPayload(), UTF_8);
        assertThat(actualPayload).isEqualTo(expectedPayload);
    }

//    @ParameterizedTest(name = "Invoke transaction {0} with key {1} and value {2} with an existing state of {3}")
//    @CsvSource({
//            "initLedger, a1,   , {\"value\":\"v1\"}, SUCCESS,               "
//            "createCar, a1, v1, '',                 SUCCESS,               {\"value\":\"v1\"}",
//            "createCar, a1, v2, {\"value\":\"v1\"}, INTERNAL_SERVER_ERROR, ",
//            "queryCar,   a1,   , {\"value\":\"v1\"}, SUCCESS,               {\"value\":\"v1\"}",
//            "queryCar,   a1,   , '',                 INTERNAL_SERVER_ERROR, ",
//            "queryAllCars,   a1,   , '',                 INTERNAL_SERVER_ERROR, ",
//            "changeCarOwner, a1, v2, {\"value\":\"v1\"}, SUCCESS,               {\"value\":\"v2\"}",
//            "changeCarOwner, a1, v2, '',                 INTERNAL_SERVER_ERROR, "
//    })
//    void invokeTransaction(final String transaction, final String key, final String value, final String existingState,
//            final String expectedStatus, final String expectedPayload) {
//        Chaincode cc = new Chaincode();
//        ChaincodeStub stub = mock(ChaincodeStub.class);
//
//        when(stub.getFunction()).thenReturn(transaction);
//        when(stub.getParameters()).thenReturn(Arrays.asList(key, value));
//        when(stub.getStringState(key)).thenReturn(existingState);
//
//        Response res = cc.invoke(stub);
//
//        assertThat(res.getStatus()).isEqualTo(Status.valueOf(expectedStatus));
//        byte[] actualPayloadBytes = res.getPayload();
//        String actualPayload = (actualPayloadBytes == null) ? null : new String(res.getPayload(), UTF_8);
//        assertThat(actualPayload).isEqualTo(expectedPayload);
//    }

    @Test
    public void invokeHandlesExceptions() {
        Chaincode cc = new Chaincode();
        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(stub.getFunction()).thenThrow(new RuntimeException("Bother"));

        Response res = cc.invoke(stub);

        assertThat(res.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
        assertThat(res.getMessage()).isEqualTo("Bother");
    }
}
