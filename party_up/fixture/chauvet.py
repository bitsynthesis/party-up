import enum

from party_up.fixture.base import Fixture


class MinWashChannels(enum.Enum):
    PAN = 0
    PAN_FINE = 1
    TILT = 2
    TILT_FINE = 3
    VECTOR_SPEED_PAN_TILT = 4
    DIMMER_STROBE = 5
    RED = 6
    GREEN = 7
    BLUE = 8
    COLOR_MACROS = 9
    VECTOR_SPEED_COLOR = 10
    MOVEMENT_MACROS = 11


def min_adjust_dimmer_value(value: int) -> int:
    if value == 0:
        adjusted_value = 0
    elif value == 255:
        adjusted_value = 255
    if value != 0 and value != 255:
        max_value = 8
        min_value = 134
        total_values = min_value - max_value

        adjusted_value = min_value - round((value / 255) * total_values)

    return adjusted_value


def min_adjust_strobe_value(value: int) -> int:
    if value == 0:
        adjusted_value = 135
    elif value == 255:
        adjusted_value = 239
    else:
        max_value = 239
        min_value = 135
        total_values = max_value - min_value

        adjusted_value = min_value + round((value / 255) * total_values)

    return adjusted_value


class MinWash(Fixture):
    channels = len(MinWashChannels)

    @property
    def pan(self) -> int:
        return self.get(MinWashChannels.PAN)

    @pan.setter
    def pan(self, value):
        self.set(MinWashChannels.PAN, value)

    @property
    def tilt(self) -> int:
        return self.get(MinWashChannels.TILT)

    @tilt.setter
    def tilt(self, value):
        self.set(MinWashChannels.TILT, value)

    @property
    def movement_speed(self) -> int:
        return self.get(MinWashChannels.VECTOR_SPEED_PAN_TILT)

    @movement_speed.setter
    def movement_speed(self, value):
        self.set(MinWashChannels.VECTOR_SPEED_PAN_TILT, value)

    @property
    def dimmer(self) -> int:
        return 0  # TODO

    @dimmer.setter
    def dimmer(self, value: int):
        adjusted_value = min_adjust_dimmer_value(value)
        self.set(MinWashChannels.DIMMER_STROBE, adjusted_value)

    @property
    def strobe(self) -> int:
        return 0  # TODO

    @strobe.setter
    def strobe(self, value: int):
        adjusted_value = min_adjust_strobe_value(value)
        self.set(MinWashChannels.DIMMER_STROBE, adjusted_value)

    @property
    def red(self) -> int:
        return self.get(MinWashChannels.RED)

    @red.setter
    def red(self, value: int):
        self.set(MinWashChannels.RED, value)

    @property
    def green(self) -> int:
        return self.get(MinWashChannels.GREEN)

    @green.setter
    def green(self, value: int):
        self.set(MinWashChannels.GREEN, value)

    @property
    def blue(self) -> int:
        return self.get(MinWashChannels.BLUE)

    @blue.setter
    def blue(self, value: int):
        self.set(MinWashChannels.BLUE, value)

    @property
    def color_macros(self) -> int:
        return self.get(MinWashChannels.COLOR_MACROS)

    @color_macros.setter
    def color_macros(self, value: int):
        return self.set(MinWashChannels.COLOR_MACROS, value)

    @property
    def color_speed(self) -> int:
        return self.get(MinWashChannels.VECTOR_SPEED_COLOR)

    @color_speed.setter
    def color_speed(self, value: int):
        return self.set(MinWashChannels.VECTOR_SPEED_COLOR, value)

    @property
    def movement_macros(self) -> int:
        return self.get(MinWashChannels.MOVEMENT_MACROS)

    @movement_macros.setter
    def movement_macros(self, value: int):
        return self.set(MinWashChannels.MOVEMENT_MACROS, value)


class MinSpotChannels(enum.Enum):
    PAN = 0
    PAN_FINE = 1
    TILT = 2
    TILT_FINE = 3
    VECTOR_SPEED_PAN_TILT = 4
    DIMMER_STROBE = 5
    RED = 6
    GREEN = 7
    BLUE = 8
    COLOR_MACROS = 9
    VECTOR_SPEED_COLOR = 10
    MOVEMENT_MACROS = 11
    GOBO = 12


class MinSpot(Fixture):
    channels = len(MinSpotChannels)

    @property
    def pan(self) -> int:
        return self.get(MinSpotChannels.PAN)

    @pan.setter
    def pan(self, value):
        self.set(MinSpotChannels.PAN, value)

    @property
    def tilt(self) -> int:
        return self.get(MinSpotChannels.TILT)

    @tilt.setter
    def tilt(self, value):
        self.set(MinSpotChannels.TILT, value)

    @property
    def movement_speed(self) -> int:
        return self.get(MinSpotChannels.VECTOR_SPEED_PAN_TILT)

    @movement_speed.setter
    def movement_speed(self, value):
        self.set(MinSpotChannels.VECTOR_SPEED_PAN_TILT, value)

    @property
    def dimmer(self) -> int:
        return 0  # TODO

    @dimmer.setter
    def dimmer(self, value: int):
        adjusted_value = min_adjust_dimmer_value(value)
        self.set(MinSpotChannels.DIMMER_STROBE, adjusted_value)

    @property
    def strobe(self) -> int:
        return 0  # TODO

    @strobe.setter
    def strobe(self, value: int):
        adjusted_value = min_adjust_strobe_value(value)
        self.set(MinSpotChannels.DIMMER_STROBE, adjusted_value)

    @property
    def red(self) -> int:
        return self.get(MinSpotChannels.RED)

    @red.setter
    def red(self, value: int):
        self.set(MinSpotChannels.RED, value)

    @property
    def green(self) -> int:
        return self.get(MinSpotChannels.GREEN)

    @green.setter
    def green(self, value: int):
        self.set(MinSpotChannels.GREEN, value)

    @property
    def blue(self) -> int:
        return self.get(MinSpotChannels.BLUE)

    @blue.setter
    def blue(self, value: int):
        self.set(MinSpotChannels.BLUE, value)

    @property
    def color_macros(self) -> int:
        return self.get(MinSpotChannels.COLOR_MACROS)

    @color_macros.setter
    def color_macros(self, value: int):
        return self.set(MinSpotChannels.COLOR_MACROS, value)

    @property
    def color_speed(self) -> int:
        return self.get(MinSpotChannels.VECTOR_SPEED_COLOR)

    @color_speed.setter
    def color_speed(self, value: int):
        return self.set(MinSpotChannels.VECTOR_SPEED_COLOR, value)

    @property
    def movement_macros(self) -> int:
        return self.get(MinSpotChannels.MOVEMENT_MACROS)

    @movement_macros.setter
    def movement_macros(self, value: int):
        return self.set(MinSpotChannels.MOVEMENT_MACROS, value)

    @property
    def gobo(self) -> int:
        return 0  # TODO

    @gobo.setter
    def gobo(self, value: int):
        self.set(MinSpotChannels.GOBO, value * 13)
