import pytest

from party_up.fixture.base import *
from party_up.output.base import DebugOutput
from party_up.universe import Universe


def test_capability_bad_configuration():
    with pytest.raises(ValueError):
        Capability("foo", channels_to_value=lambda _: 1)

    with pytest.raises(ValueError):
        Capability("foo", value_to_channels=lambda _: 1)

def test_capability_default():
    default_capability = Capability("foo")

    assert default_capability.name == "foo"
    assert default_capability.name_color == "white"

    assert default_capability.value_to_channels(123) == {"foo": 123}
    assert default_capability.channels_to_value({"foo": 42}) == 42
    assert default_capability.value_color(0) == "white"
    assert default_capability.value_color(64) == "white"
    assert default_capability.value_color(255) == "white"


def test_capability_custom():
    custom_capability = Capability(
        "bar",
        "black",
        channels_to_value=lambda c: c["baz"] + 5,
        value_to_channels=lambda v: {"baz": v - 5},
        value_color=lambda v: "red" if v == 0 else "green",
    )

    assert custom_capability.name == "bar"
    assert custom_capability.name_color == "black"

    assert custom_capability.value_to_channels(128) == {"baz": 123}
    assert custom_capability.channels_to_value({"baz": 42}) == 47
    assert custom_capability.value_color(0) == "red"
    assert custom_capability.value_color(64) == "green"
    assert custom_capability.value_color(255) == "green"


# def test_fixture_state():
#     dummy_universe = Universe(DebugOutput())
#     dummy_fixture = Fixture("foo", dummy_universe, 32, ["a", "b", "c"])
#
#     assert dummy_fixture.state == [0, 0, 0]
