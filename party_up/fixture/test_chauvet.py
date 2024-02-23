from party_up.fixture.chauvet import _MinBase


def test_parse_dimmer():
    assert _MinBase.parse_dimmer(0) == {"dimmer-strobe": 0}
    assert _MinBase.parse_dimmer(255) == {"dimmer-strobe": 255}
    assert _MinBase.parse_dimmer(254) == {"dimmer-strobe": 8}
    assert _MinBase.parse_dimmer(1) == {"dimmer-strobe": 134}
    assert _MinBase.parse_dimmer(128) == {"dimmer-strobe": 71}


def test_parse_strobe():
    assert _MinBase.parse_strobe(0) == {"dimmer-strobe": 135}
    assert _MinBase.parse_strobe(255) == {"dimmer-strobe": 239}
    assert _MinBase.parse_strobe(254) == {"dimmer-strobe": 239}
    assert _MinBase.parse_strobe(1) == {"dimmer-strobe": 135}
    assert _MinBase.parse_strobe(128) == {"dimmer-strobe": 187}
