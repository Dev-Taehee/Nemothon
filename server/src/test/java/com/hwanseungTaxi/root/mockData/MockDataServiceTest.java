package com.hwanseungTaxi.root.mockData;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwanseungTaxi.root.mockData.entity.MockEntity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class MockDataServiceTest {

    @Test
    public void getGangnamTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        MockDataService mockDataService = new MockDataService(objectMapper);
        MockEntity entity = mockDataService.getEntity("gangnam");
        String name = entity.metaData.plan.itineraries.get(0).legs.get(0).end.name;
        Assertions.assertThat(name).isEqualTo("초당중학교.삼부르네상스아파트");
    }

    @Test
    public void get330Test() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        MockDataService mockDataService = new MockDataService(objectMapper);
        MockEntity entity = mockDataService.getEntity("330");
        String name = entity.metaData.plan.itineraries.get(0).legs.get(0).end.name;
        Assertions.assertThat(name).isEqualTo("불광");
    }

    @Test
    public void getUijeongbuTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        MockDataService mockDataService = new MockDataService(objectMapper);
        MockEntity entity = mockDataService.getEntity("uijeongbu");
        String name = entity.metaData.plan.itineraries.get(0).legs.get(0).end.name;
        Assertions.assertThat(name).isEqualTo("홍대입구");
    }

}