import time

import pytest

from party_up.output.base import DebugOutput
from party_up.universe import Universe


def test_universe_stop():
    dummy_universe = Universe(DebugOutput())

    assert dummy_universe.write_enabled is True

    dummy_universe.stop()

    assert dummy_universe.write_enabled is False


def test_universe_close():
    dummy_universe = Universe(DebugOutput())

    assert dummy_universe.write_enabled is True

    dummy_universe.close()

    assert dummy_universe.write_enabled is False
    assert dummy_universe.output.log.pop() == [-1]


def test_universe_run():
    sleep_seconds = 0.01
    wait_seconds = sleep_seconds + 0.005

    dummy_universe = Universe(DebugOutput(sleep_seconds=sleep_seconds))
    log = dummy_universe.output.log

    assert len(log) == 1

    dummy_universe.state[0] = 64
    dummy_universe.state[2] = 128

    time.sleep(wait_seconds)
    assert len(log) == 2

    dummy_universe.state[0] = 222
    dummy_universe.state[2] = 111

    time.sleep(wait_seconds)
    assert len(log) == 3

    dummy_universe.stop()

    time.sleep(wait_seconds)
    assert len(log) == 3

    assert log[0][:5] == [0, 0, 0, 0, 0]
    assert log[1][:5] == [64, 0, 128, 0, 0]
    assert log[2][:5] == [222, 0, 111, 0, 0]


@pytest.mark.skip(reason="hangs")
def test_universe_start():
    sleep_seconds = 0.01
    wait_seconds = sleep_seconds + 0.005

    dummy_universe = Universe(
        DebugOutput(sleep_seconds=sleep_seconds),
        write_enabled=False,
    )

    log = dummy_universe.output.log

    assert len(log) == 0
    time.sleep(wait_seconds)
    assert len(log) == 0

    dummy_universe.state[0] = 64
    dummy_universe.state[2] = 128

    dummy_universe.write_enabled = True
    dummy_universe.start()

    # print(log[0])

    time.sleep(wait_seconds)
    assert len(log) == 1

    dummy_universe.state[0] = 222
    dummy_universe.state[2] = 111

    time.sleep(wait_seconds)
    assert len(log) == 2

    dummy_universe.close()

    time.sleep(wait_seconds)
    assert len(log) == 3

    # assert log[0][:5] == [64, 0, 128, 0, 0]
    # assert log[1][:5] == [222, 0, 111, 0, 0]
    # assert log[2][:5] == [-1]
