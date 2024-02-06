#!/usr/bin/env python3
import os
import time

from party_up.tui2 import start_tui
from party_up.universe2 import Universe
from party_up.fixture.base import Fixture
from party_up.fixture.chauvet import MinSpot, MinWash
from party_up.output.dmx import DmxKingUltraDmxMicro, DummyOutput

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

# import pdb; pdb.set_trace()

try:
    start_tui(uni, [min_spot, min_wash])

finally:
    print("Closing time")
    uni.state = [0] * 512
    time.sleep(0.03)
    uni.close()
    os._exit(0)
