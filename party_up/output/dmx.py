import time

import serial

from party_up.output.base import Output


class DmxKingUltraDmxMicro(Output):
    def __init__(self, port: str = "/dev/ttyUSB0"):
        self.port = serial.Serial(
            port=port,
            baudrate=250000,
            bytesize=serial.EIGHTBITS,
            stopbits=serial.STOPBITS_TWO,
            parity=serial.PARITY_NONE,
        )

    def close(self):
        # let a last frame write, this may not be desired
        time.sleep(0.05)
        self.port.close()

    def write(self, state: list[int]):
        # magic dmxking incantation
        header = [126, 6, 1, 2, 0]
        footer = [231]

        frame = bytearray(header + state + footer)

        self.port.write(frame)
