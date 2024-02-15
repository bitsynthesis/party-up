import pytest

from party_up.output.base import *


def test_debug_output_write():
    dummy_output = DebugOutput()

    assert len(dummy_output.log) == 0

    dummy_output.write([11, 12, 13, 14, 15])
    dummy_output.write([21, 22, 23, 24, 25])

    assert len(dummy_output.log) == 2


def test_debug_output_write_roll_log():
    dummy_output = DebugOutput(log_limit=3)

    assert len(dummy_output.log) == 0

    dummy_output.write([1] * 512)
    dummy_output.write([2] * 512)
    dummy_output.write([3] * 512)
    dummy_output.write([4] * 512)
    dummy_output.write([5] * 512)

    assert len(dummy_output.log) == 3

    assert dummy_output.log.pop() == [5] * 512
    assert dummy_output.log.pop() == [4] * 512
    assert dummy_output.log.pop() == [3] * 512

    with pytest.raises(IndexError):
        dummy_output.log.pop()


def test_debug_output_close():
    dummy_output = DebugOutput()

    assert len(dummy_output.log) == 0

    dummy_output.write([1] * 512)
    dummy_output.close()

    assert len(dummy_output.log) == 2

    assert dummy_output.log.pop() == [-1]
    assert dummy_output.log.pop() == [1] * 512
