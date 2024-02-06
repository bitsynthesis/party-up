import dataclasses
import functools
import threading
import time
import typing

import serial


class DmxOutput:
    def close(self):
        print("Warning: close not implemented for output")

    def write(self, state: list[int]):
        raise NotImplementedError("OutputDevice must implement write")


class DummyOutput(DmxOutput):
    def write(self, state: list[int]):
        time.sleep(10)


class DmxKingUltraDmxMicro(DmxOutput):
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


class Universe:
    def __init__(self, output: DmxOutput, write_enabled: bool = True):
        self.output = output
        self.state = [0] * 512
        self.write_enabled = write_enabled
        self.writer = threading.Thread(target=self.start)
        self.writer.start()

    def start(self):
        print("Starting")

        while self.write_enabled:
            self.output.write(self.state)

        print("Stopping")

    def stop(self):
        self.write_enabled = False

    def close(self):
        self.stop()
        self.output.close()


@dataclasses.dataclass
class Capability:
    """
    An individual feature of a fixture. Returns a mapping of channels and values
    to apply to the fixture. Channels may be specified by index or name.

    This is the translation layer between the program and the specific
    implementation of channels on the fixture.
    """

    name: str
    name_color: str = "white"
    value: typing.Any = 0
    parse_value: typing.Optional[typing.Callable] = None
    value_color: typing.Callable = lambda value: "white"

    def __post_init__(self):
        if self.parse_value is None:
            self.parse_value = lambda value: {self.name: value}


class Fixture:
    def __init__(
        self, name: str, universe: Universe, address: int, channels: list[str]
    ):
        self.name = name
        self.universe = universe

        # dmx devices are indexed starting at 1 in hardware, 0 in software
        self.address = address - 1
        self.channels = channels

    def get(self, channel: str) -> int:
        return self.universe.state[self.address + self.channels.index(channel)]

    def set(self, channel: str, value: int):
        universe_address = self.address + self.channels.index(channel)
        self.universe.state[universe_address] = value

    @property
    def capabilities(self) -> list[Capability]:
        raise NotImplementedError("Fixture must implement capabilities")

    def get_capability(self, name: str) -> Capability:
        matches = list(filter(lambda c: c.name == name, self.capabilities))

        if len(matches) == 0:
            raise AttributeError(f"No capabilities named '{name}' in '{self}'")

        elif len(matches) > 1:
            raise ValueError(
                (
                    "Capabilities should be unique."
                    f" {len(matches)} named '{name}' in '{self}'"
                )
            )

        return matches[0]

    def set_capability_value(self, name: str, value: typing.Any):
        capability = self.get_capability(name)

        capability.value = value

        channel_values = capability.parse_value(value)
        for channel, channel_value in channel_values.items():
            self.set(channel, channel_value)

    def __setattr__(self, name: str, value: typing.Any):
        try:
            self.set_capability_value(name, value)

        except AttributeError:
            super().__setattr__(name, value)


# class Capability:
#     def __init__(self, name: str, value: typing.Any = 0):
#         self.name = name
#         self.value = value
#
#     @property
#     def name_color(self) -> str:
#         return "white"
#
#     def set_value(self, fixture: Fixture, value: typing.Any):
#         fixture.set(self.value)
#
#     def set_and_store_value(self, fixture: Fixture, value: typing.Any):
#         self.value = value
#         self.set_value(self.value)
#
#     def value_color(self, fixture: Fixture) -> str:
#         return "white"


class _MinBase(Fixture):
    @property
    def capabilities(self) -> list[Capability]:
        return [
            Capability(name="pan"),
            Capability(name="tilt"),
            Capability(
                name="movement_speed",
                parse_value=lambda value: {"vector-speed-pan-tilt": value},
            ),
            Capability(name="dimmer", parse_value=self.parse_dimmer),
            Capability(name="strobe", parse_value=self.parse_strobe),
            Capability(name="red", name_color="#f00"),
            Capability(name="green", name_color="#0f0"),
            Capability(name="blue", name_color="#00f"),
            Capability(
                name="color_speed",
                parse_value=lambda value: {"vector-speed-color": value},
            ),
        ]

    # TODO combine with fine for single with more precision
    # def pan_fine(self, value: int):
    #     self.set("pan-fine", value)

    # TODO combine with fine for single with more precision
    # def tilt_fine(self, value: int):
    #     self.set("tilt-fine", value)

    # def movement_speed(self, value: int):
    #     self.set("vector-speed-pan-tilt", value)

    @staticmethod
    def parse_dimmer(value: int):
        if value == 0:
            adjusted_value = 0
        elif value == 255:
            adjusted_value = 255
        if value != 0 and value != 255:
            max_value = 8
            min_value = 134
            total_values = min_value - max_value

            value = min_value - int((255 / value) * total_values)

        return {"dimmer-strobe": value}

    @staticmethod
    def parse_strobe(value: int):
        if value == 0:
            adjusted_value = 135
        elif value == 255:
            adjusted_value = 239
        else:
            max_value = 239
            min_value = 135
            total_values = max_value - min_value

            adjusted_value = int((255 / value) * total_values)

        return {"dimmer-strobe": adjusted_value}


class MinWash(_MinBase):
    def __init__(self, *args, **kwargs):
        kwargs["channels"] = [
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

        super().__init__(*args, **kwargs)


class MinSpot(_MinBase):
    def __init__(self, *args, **kwargs):
        kwargs["channels"] = [
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

        super().__init__(*args, **kwargs)

    @property
    def capabilities(self) -> list[Capability]:
        base = super().capabilities
        spot = [
            Capability(
                name="gobo",
                parse_value=lambda value: {"gobo": value * 13},
            )
        ]
        return base + spot
