package com.hwanseungTaxi.root.kakaoMobility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwanseungTaxi.root.mockData.entity.Leg;
import com.hwanseungTaxi.root.mockData.entity.MockEntity;
import com.hwanseungTaxi.root.mockData.entity.Station;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KakaoMobilityService {

    @Value("${kakaoMobility.apiKey}")
    private String kakaoRestApiKey;
    private static final String baseUrl = "https://apis-navi.kakaomobility.com/v1/directions?";

    private final ObjectMapper objectMapper;

    public List<TaxiInfoEntity> getTaxiInfoEntities(MockEntity mockEntity, int maxFare) {
        List<TaxiInfoEntity> taxiInfoEntities = new LinkedList<>();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        List<Leg> legs = mockEntity.metaData.plan.itineraries.get(0).legs;

        String startX = mockEntity.metaData.requestParameters.startX;
        String startY = mockEntity.metaData.requestParameters.startY;

        for(Leg leg : legs) {
            if(!leg.mode.equals("WALK")) { // 걷기 지점이 아닌 경우에만 택시 거리 진행
                List<Station> stationList = leg.passStopList.stationList;
                for(Station station : stationList) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(baseUrl);
                    sb.append("origin="+startX+","+startY);
                    sb.append("&destination="+station.lon+","+station.lat);
                    sb.append("&priority="+"TIME");
                    sb.append("&avoid=roadevent|ferries|toll");

                    String apiUrl = sb.toString();
                    ResponseEntity<String> responseEntity = new RestTemplate().exchange(apiUrl, HttpMethod.GET, httpEntity, String.class);

                    try{
                        JsonNode root = objectMapper.readTree(responseEntity.getBody());
                        JsonNode summary = root.path("routes").path(0).path("summary");
                        int taxiFare = summary.path("fare").path("taxi").asInt();
                        int taxiDuration = summary.path("duration").asInt();
                        if(taxiFare<=maxFare) {
                            TaxiInfoEntity taxiInfoEntity = new TaxiInfoEntity(station.stationName, station.lon, station.lat, taxiDuration, taxiFare);
                            taxiInfoEntities.add(taxiInfoEntity);
                        }
                    } catch (Exception e) {
                        System.out.println("Error occurred while parsing JSON response.");
                    }
                }
            }
        }
        return taxiInfoEntities;
    }


}
