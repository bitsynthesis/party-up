import dataclasses
import threading
import time

import serial


class DmxOutput:
    def close(self):
        print("Warning: close not implemented for output")

    def write(self, state: list[int]):
        raise NotImplementedException("OutputDevice must implement write")


class DmxKingUltraDmxMicro(DmxOutput):
    def __init__(self, port: str = "/dev/ttyUSB0"):
        self.port = serial.Serial(
            port=port,
            baudrate=250000,
            bytesize=serial.EIGHTBITS,
            stopbits=serial.STOPBITS_TWO,
            parity=serial.PARITY_NONE
        )

    def close(self):
        # let a last frame write, this may not be desired
        time.sleep(0.03)
        self.port.close()

    def write(self, state: list[int]):
        # magic dmxking incantation
        header = [126, 6, 1, 2, 0]
        footer = [231]

        frame = bytearray(header + state + footer)

        self.port.write(frame)


class Universe:
    def __init__(self, output: DmxOutput, write_enabled: bool = True):
        self.output = output
        self.state = [0] * 512
        self.write_enabled = write_enabled
        self.writer = threading.Thread(target=self.start)
        self.writer.start()

    def start(self):
        print("Starting")

        while(self.write_enabled):
            self.output.write(self.state)

        print("Stopping")

    def stop(self):
        self.write_enabled = False

    def close(self):
        self.stop()
        self.output.close()


class Fixture:
    def __init__(self, name: str, universe: Universe, address: int, channels: list[str]):
        self.name = name
        self.universe = universe

        # dmx devices are indexed starting at 1 in hardware, 0 in software
        self.address = address - 1
        self.channels = channels

    def get(self, channel: str) -> int:
        return self.universe.state[self.address + self.channels.index(channel)]

    def set(self, channel: str, value: int):
        self.universe.state[self.address + self.channels.index(channel)] = value
