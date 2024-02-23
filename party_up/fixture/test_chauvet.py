from party_up.fixture.chauvet import (
    min_adjust_dimmer_value,
    min_adjust_strobe_value,
)


def test_min_adjust_dimmer_value():
    assert min_adjust_dimmer_value(0) == 0
    assert min_adjust_dimmer_value(255) == 255
    assert min_adjust_dimmer_value(254) == 8
    assert min_adjust_dimmer_value(1) == 134
    assert min_adjust_dimmer_value(128) == 71


def test_parse_strobe():
    assert min_adjust_strobe_value(0) == 135
    assert min_adjust_strobe_value(255) == 239
    assert min_adjust_strobe_value(254) == 239
    assert min_adjust_strobe_value(1) == 135
    assert min_adjust_strobe_value(128) == 187
