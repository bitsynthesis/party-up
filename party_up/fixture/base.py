import abc
import enum

from party_up.universe import Universe


class Fixture(abc.ABC):
    channels = 0

    def __init__(self, name: str, universe: Universe, address: int):
        self.name = name
        self.universe = universe

        # dmx devices are indexed starting at 1 in hardware, 0 in software
        self.address = address - 1

    @property
    def state(self) -> list[int]:
        end_channel = self.address + self.channels
        return self.universe.state[self.address : end_channel]

    def get(self, channel: enum.Enum) -> int:
        return self.universe.state[self.address + channel.value]

    def set(self, channel: enum.Enum, value: int):
        if value < 0 or value > 255:
            raise ValueError(f"Invalid value for '{channel.name}': {value}")

        universe_address = self.address + channel.value
        self.universe.state[universe_address] = value
