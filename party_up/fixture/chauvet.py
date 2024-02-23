from party_up.fixture.base import Capability, Fixture


class _MinBase(Fixture):
    @property
    def capabilities(self) -> list[Capability]:
        return [
            Capability(name="pan"),
            Capability(name="tilt"),
            Capability(
                name="movement_speed",
                value_to_channels=lambda value: {"vector-speed-pan-tilt": value},
                channels_to_value=lambda chans: chans["vector-speed-pan-tilt"],
            ),
            Capability(
                name="dimmer",
                value_to_channels=self.parse_dimmer,
                channels_to_value=lambda x: 0,  # TODO
            ),
            Capability(
                name="strobe",
                value_to_channels=self.parse_strobe,
                channels_to_value=lambda x: 0,  # TODO
            ),
            Capability(name="red", name_color="#f00"),
            Capability(name="green", name_color="#0f0"),
            Capability(name="blue", name_color="#00f"),
            Capability(
                name="color_speed",
                value_to_channels=lambda value: {"vector-speed-color": value},
                channels_to_value=lambda chans: chans["vector-speed-color"],
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

            adjusted_value = min_value - round((value / 255) * total_values)

        return {"dimmer-strobe": adjusted_value}

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

            adjusted_value = min_value + round((value / 255) * total_values)

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
                value_to_channels=lambda value: {"gobo": value * 13},
                channels_to_value=lambda x: 0,  # TODO
            )
        ]
        return base + spot
