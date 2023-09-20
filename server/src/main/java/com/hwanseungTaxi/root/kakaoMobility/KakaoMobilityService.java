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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class KakaoMobilityService {

    @Value("${kakaoMobility.apiKey}")
    private String kakaoRestApiKey;
    private static final String baseUrl = "https://apis-navi.kakaomobility.com/v1/directions?";

    private final ObjectMapper objectMapper;

//    public List<TaxiInfoEntity> getTaxiInfoEntities(MockEntity mockEntity) {
//        List<TaxiInfoEntity> taxiInfoEntities = new ArrayList<>();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);
//        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
//
//        List<Leg> legs = mockEntity.metaData.plan.itineraries.get(0).legs;
//
//        String startX = mockEntity.metaData.requestParameters.startX;
//        String startY = mockEntity.metaData.requestParameters.startY;
//
//        // ParallelStream을 사용하여 병렬처리 진행
//        legs.parallelStream().forEach(leg -> {
//            if (!leg.mode.equals("WALK")) { // 걷기 지점이 아닌 경우에만 택시 거리 진행
//                List<Station> stationList = leg.passStopList.stationList;
//                stationList.parallelStream().forEach(station -> {
//                    StringBuilder sb = new StringBuilder();
//                    sb.append(baseUrl);
//                    sb.append("origin=" + startX + "," + startY);
//                    sb.append("&destination=" + station.lon + "," + station.lat);
//                    sb.append("&priority=" + "TIME");
//                    sb.append("&avoid=roadevent|ferries|toll");
//
//                    String apiUrl = sb.toString();
//                    ResponseEntity<String> responseEntity = new RestTemplate().exchange(apiUrl, HttpMethod.GET, httpEntity, String.class);
//
//                    try {
//                        JsonNode root = objectMapper.readTree(responseEntity.getBody());
//                        JsonNode summary = root.path("routes").path(0).path("summary");
//                        int taxiFare = summary.path("fare").path("taxi").asInt();
//                        int taxiDuration = summary.path("duration").asInt();
//
//                        TaxiInfoEntity taxiInfoEntity = new TaxiInfoEntity(station.stationName, station.lon, station.lat, taxiDuration, taxiFare);
//                        taxiInfoEntities.add(taxiInfoEntity);
//
//                    } catch (Exception e) {
//                        System.out.println("Error occurred while parsing JSON response.");
//                    }
//                });
//            }
//        });
//
//        return taxiInfoEntities;
//    }

    public List<TaxiInfoEntity> getTaxiInfoEntities(MockEntity mockEntity) {
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

                        TaxiInfoEntity taxiInfoEntity = new TaxiInfoEntity(station.stationName, station.lon, station.lat, taxiDuration, taxiFare);
                        taxiInfoEntities.add(taxiInfoEntity);

                    } catch (Exception e) {
                        System.out.println("Error occurred while parsing JSON response.");
                    }
                }
            }
        }
        return taxiInfoEntities;
    }

//    public List<TaxiInfoEntity> getTaxiInfoEntities(MockEntity mockEntity) {
////        List<TaxiInfoEntity> taxiInfoEntities = new LinkedList<>();
//        ConcurrentLinkedQueue<TaxiInfoEntity> taxiInfoEntities = new ConcurrentLinkedQueue<>();
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);
//        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
//
//        List<Leg> legs = mockEntity.metaData.plan.itineraries.get(0).legs;
//
//        String startX = mockEntity.metaData.requestParameters.startX;
//        String startY = mockEntity.metaData.requestParameters.startY;
//
//        int maxThreads = 25; // 최대 스레드 개수 설정
//        ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);
//
//        List<CompletableFuture<Void>> futures = new ArrayList<>();
//
//        for (Leg leg : legs) {
//            if (!leg.mode.equals("WALK")) { // 걷기 지점이 아닌 경우에만 택시 거리 진행
//                List<Station> stationList = leg.passStopList.stationList;
//                for (Station station : stationList) {
//                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                        try {
//                            StringBuilder sb = new StringBuilder();
//                            sb.append(baseUrl);
//                            sb.append("origin=" + startX + "," + startY);
//                            sb.append("&destination=" + station.lon + "," + station.lat);
//                            sb.append("&priority=" + "TIME");
//                            sb.append("&avoid=roadevent|ferries|toll");
//
//                            String apiUrl = sb.toString();
//                            ResponseEntity<String> responseEntity = new RestTemplate().exchange(apiUrl, HttpMethod.GET, httpEntity, String.class);
//
//                            JsonNode root = objectMapper.readTree(responseEntity.getBody());
//                            JsonNode summary = root.path("routes").path(0).path("summary");
//                            int taxiFare = summary.path("fare").path("taxi").asInt();
//                            int taxiDuration = summary.path("duration").asInt();
//
//                            TaxiInfoEntity taxiInfoEntity = new TaxiInfoEntity(station.stationName, station.lon, station.lat, taxiDuration, taxiFare);
//                            taxiInfoEntities.add(taxiInfoEntity);
//
//                        } catch (Exception e) {
//                            System.out.println("Error occurred while parsing JSON response.");
//                        }
//                    }, executorService);
//
//                    futures.add(future);
//                }
//            }
//        }
//
//        // Wait for all CompletableFuture to complete
//        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
//        allOf.join();
//
//        executorService.shutdown();
//
//        return new ArrayList<>(taxiInfoEntities);
//    }

    public List<TaxiInfoEntity> getAdjustTaxiInfoEntities(List<TaxiInfoEntity> taxiInfoEntities, int minFare, int maxFare) {
        List<TaxiInfoEntity> adjustTaxiInfoEntities = new ArrayList<>();
        for(TaxiInfoEntity taxiInfoEntity : taxiInfoEntities) {
            if(minFare <= taxiInfoEntity.getTaxiFare() && taxiInfoEntity.getTaxiFare() <= maxFare) {
                adjustTaxiInfoEntities.add(taxiInfoEntity);
            }
        }
        return adjustTaxiInfoEntities;
    }

}
