#!/usr/bin/env python3
import os
import time

from party_up.tui import start_tui
from party_up.universe import (
    DmxKingUltraDmxMicro,
    DummyOutput,
    Fixture,
    MinSpot,
    MinWash,
    Universe,
)

# uni = Universe(output=DmxKingUltraDmxMicro())
uni = Universe(output=DummyOutput())

min_spot = MinSpot(name="MinSpot", universe=uni, address=1)
min_wash = MinWash(name="MinWash", universe=uni, address=15)

min_spot.pan = 100
min_spot.tilt = 60
min_spot.dimmer = 0xFF
min_spot.red = 0xFF

min_wash.pan = 195
min_wash.tilt = 60
min_wash.blue = 0xFF
min_wash.dimmer = 0xFF

# time.sleep(5)

# import pdb; pdb.set_trace()

try:
    start_tui(uni, [min_spot, min_wash])

    # while True:
    #     time.sleep(3)
    #
    #     min_spot.movement_speed(0xFF)
    #     min_wash.movement_speed(0xFF)
    #     min_spot.color_speed(0x80)
    #     min_wash.color_speed(0x80)
    #     min_spot.gobo(0)
    #
    #     min_spot.pan(50)
    #     min_wash.pan(150)
    #
    #     min_spot.green(0xFF)
    #     min_wash.red(0)
    #
    #     time.sleep(9)
    #
    #     min_spot.strobe(0xFF)
    #     min_wash.strobe(0xFF)
    #
    #     time.sleep(3)
    #
    #     min_spot.dimmer(0xFF)
    #     min_wash.dimmer(0xFF)
    #
    #     min_spot.green(0)
    #     min_wash.red(0xFF)
    #
    #     min_spot.pan(100)
    #     min_wash.pan(195)
    #
    #     time.sleep(9)
    #
    #     min_spot.gobo(3)
    #
    #     time.sleep(3)
    #
    #     # min_wash.movement_speed(0)
    #     # min_spot.movement_speed(0)
    #     # min_spot.pan(255)
    #     # min_wash.pan(0)

finally:
    print("Closing time")
    uni.state = [0] * 512
    time.sleep(0.03)
    uni.close()
    os._exit(0)
