#!/usr/bin/env python3
import time

from party_up.universe import DmxKingUltraDmxMicro, Fixture, Universe

uni = Universe(output=DmxKingUltraDmxMicro())

min_spot = Fixture(
    name="MinSpot",
    universe=uni,
    address=1,
    channels=[
        "pan",
        "pan-fine",
        "tilt",
        "tilt-fine",
        "vector-speed-pan-tilt",
        "dimmer-strobe",
        "red",
        "green",
        "blue",
        "color-macros",
        "vector-speed-color",
        "movement-macros",
        "gobo",
    ]
)

min_wash = Fixture(
    name="MinWash",
    universe=uni,
    address=15,
    channels=[
        "pan",
        "pan-fine",
        "tilt",
        "tilt-fine",
        "vector-speed-pan-tilt",
        "dimmer-strobe",
        "red",
        "green",
        "blue",
        "color-macros",
        "vector-speed-color",
        "movement-macros",
    ]
)



min_spot.set("pan", 0x50)
min_spot.set("tilt", 0x80)
min_spot.set("dimmer-strobe", 0xFF)
min_spot.set("blue", 0xFF)
min_spot.set("red", 0x80)

min_wash.set("pan", 0x50)
min_wash.set("tilt", 0x80)

try:
    while(True):
        time.sleep(3)
        min_spot.set("blue", 0x00)
        min_spot.set("red", 0xFF)

        time.sleep(3)
        min_spot.set("blue", 0xFF)
        min_spot.set("red", 0x00)

except:
    print("Closing time")
    uni.state = [0] * 512
    time.sleep(0.03)
    uni.close()
