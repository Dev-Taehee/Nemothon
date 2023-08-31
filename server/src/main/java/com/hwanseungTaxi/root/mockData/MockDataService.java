package com.hwanseungTaxi.root.mockData;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwanseungTaxi.root.mockData.entity.MockEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class MockDataService {

    private final ObjectMapper objectMapper;

    public MockEntity getEntity(String destination) throws IOException {
        MockEntity entity;

        if(destination.equals("gangnam")) {
            LocalTime currentTime = LocalTime.now();
            LocalTime elevenAM = LocalTime.of(11, 0);
            if(currentTime.isBefore(elevenAM)){
                entity = objectMapper.readValue(new File("C:\\Users\\wth00\\OneDrive\\Desktop\\Nemotone\\HwanseungTaxi\\server\\src\\main\\resources\\YonginToGangnamBefore11.json"), MockEntity.class);
            }else {
                entity = objectMapper.readValue(new File("C:\\Users\\wth00\\OneDrive\\Desktop\\Nemotone\\HwanseungTaxi\\server\\src\\main\\resources\\YonginToGangnamAfter11.json"), MockEntity.class);
            }
        } else if(destination.equals("330")) {
            entity = objectMapper.readValue(new File("C:\\Users\\wth00\\OneDrive\\Desktop\\Nemotone\\HwanseungTaxi\\server\\src\\main\\resources\\SushiTo330SubwayNBus.json"), MockEntity.class);
        } else { // 일치하지 않은 경우에도 의정부 객체를 내보내도록 설정
            entity = objectMapper.readValue(new File("C:\\Users\\wth00\\OneDrive\\Desktop\\Nemotone\\HwanseungTaxi\\server\\src\\main\\resources\\HongdaeToUijeongbuBus.json"), MockEntity.class);
        }

        return entity;
    }

}
