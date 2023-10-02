package com.greenhousecontrol;

public class Dht {
    float heatIndex;
    float humidity;
    float temperature;

    public Dht() {
    }

    public Dht(float heatIndex, float humidity, float temperature) {
        this.heatIndex = heatIndex;
        this.humidity = humidity;
        this.temperature = temperature;
    }

    public float getHeatIndex() {
        return heatIndex;
    }

    public void setHeatIndex(float heatIndex) {
        this.heatIndex = heatIndex;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }
}
