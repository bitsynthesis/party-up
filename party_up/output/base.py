import abc
import time


class Output(abc.ABC):
    def close(self):
        print("Warning: close not implemented for output")

    def write(self, state: list[int]):
        raise NotImplementedError("OutputDevice must implement write")


class DebugOutput(Output):
    """
    Writes state to internal log.
    """
    def __init__(self, sleep_seconds: int = 0):
        self.sleep_seconds = sleep_seconds
        self.log = []

    def write(self, state: list[int]):
        # TODO roll log to constrain memory usage
        self.log.append(state)
        time.sleep(self.sleep_seconds)
