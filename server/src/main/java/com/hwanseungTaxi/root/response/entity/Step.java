package com.hwanseungTaxi.root.response.entity;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class Step {

    public String mode;
    public int sectionTime;
    public String route;
    public String routeId;
    public List<String> stationList;

}
