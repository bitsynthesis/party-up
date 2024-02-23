import abc
import dataclasses
import typing

from party_up.universe import Universe


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
    value_to_channels: typing.Optional[
        typing.Callable[[typing.Any], dict[str, int]]
    ] = None
    channels_to_value: typing.Optional[
        typing.Callable[[dict[str, int]], typing.Any]
    ] = None
    value_color: typing.Callable = lambda value: "white"

    def __post_init__(self):
        if self.value_to_channels is None and self.channels_to_value is not None:
            raise ValueError(
                "Must define value_to_channels if channels_to_value is defined"
            )

        if self.channels_to_value is None and self.value_to_channels is not None:
            raise ValueError(
                "Must define channels_to_value if value_to_channels is defined"
            )

        if self.value_to_channels is None:
            self.value_to_channels = lambda value: {self.name: value}

        if self.channels_to_value is None:
            self.channels_to_value = lambda channels: channels[self.name]


class Fixture(abc.ABC):
    def __init__(
        self, name: str, universe: Universe, address: int, channels: list[str]
    ):
        self.name = name
        self.universe = universe

        # dmx devices are indexed starting at 1 in hardware, 0 in software
        self.address = address - 1
        self.channels = channels

    @property
    def state(self) -> dict[str, int]:
        end_channel = self.address + len(self.channels)
        return self.universe.state[self.address : end_channel]

    def get(self, channel: str) -> int:
        return self.universe.state[self.address + self.channels.index(channel)]

    def set(self, channel: str, value: int):
        if value < 0 or value > 255:
            raise ValueError(f"Invalid value for '{channel}': {value}")

        universe_address = self.address + self.channels.index(channel)
        self.universe.state[universe_address] = value

    def get_all(self) -> dict[str, int]:
        return {channel: self.get(channel) for channel in self.channels}

    @property
    def capabilities(self) -> list[Capability]:
        raise NotImplementedError("Fixture must implement capabilities")

    def get_capability(self, name: str) -> Capability:
        matches = list(filter(lambda c: c.name == name, self.capabilities))

        if len(matches) == 0:
            raise AttributeError(f"No capabilities named '{name}' in '{self.name}'")

        elif len(matches) > 1:
            raise ValueError(
                (
                    "Capabilities should be unique."
                    f" {len(matches)} named '{name}' in '{self.name}'"
                )
            )

        return matches[0]

    def get_capability_value(self, name: str) -> int:
        return self.get_capability(name).channels_to_value(self.get_all())

    def set_capability_value(self, name: str, value: typing.Any):
        capability = self.get_capability(name)

        channel_values = capability.value_to_channels(value)
        for channel, channel_value in channel_values.items():
            self.set(channel, channel_value)

    def __setattr__(self, name: str, value: typing.Any):
        try:
            self.set_capability_value(name, value)

        except AttributeError:
            super().__setattr__(name, value)
