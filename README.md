# Mini-Greenhouse-Control-Panel
## Overview
This Android app was designed to complement an Arduino-powered greenhouse project. It serves as a vital component for monitoring and controlling the greenhouse environment, ensuring optimal conditions for plant growth. It interfaces with sensors and hardware components to provide real-time data and control capabilities.

# Hardware Components
The project involves the following hardware components:

- Arduino Uno
- DHT sensor (for temperature, humidity, and heat-index)
- Soil moisture sensor
- 5V servo motor (for greenhouse window control)
- 12V water pump (for plant watering)


# Features
## Soil Moisture Monitor
- Real-time display of soil moisture sensor readings.
  <br>
  <img src="/assets/hydration-readings.jpg" width="40%">
- Control panel for the water pump with two modes:
  - Manual: Allows the user to manually activate the water pump for a specified duration.
  - Automatic: Automatically waters the plant when the soil moisture falls below a user-defined threshold.
    <br>
    <img src="/assets/hydration-control-auto.jpg" width="40%">
## Climate Monitor
- Real-time display of temperature, humidity, and heat index readings from the DHT sensor.
  <br>
  <img src="/assets/climate-readings.jpg" width="40%">
- Control panel for the greenhouse window using a servo motor with two modes:
  - Manual: Enables the user to manually open or close the greenhouse window.
    <br>
    <img src="/assets/climate-control-manual.jpg" width="40%">
  - Automatic: Automatically adjusts the window position based on user-configured heat index thresholds to regulate the internal temperature.
    <br>
    <img src="/assets/climate-control-auto.jpg" width="40%">
