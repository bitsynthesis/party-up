import abc
import collections
import time


class Output(abc.ABC):
    def close(self):
        raise NotImplementedError("OutputDevice must implement close")

    def write(self, state: list[int]):
        raise NotImplementedError("OutputDevice must implement write")


class DebugOutput(Output):
    """
    Writes state to internal log.
    """

    def __init__(self, sleep_seconds: int = 0, log_limit: int = 100):
        self.sleep_seconds = sleep_seconds
        self.log_limit = log_limit
        self.log: collections.deque = collections.deque([])

    def write(self, state: list[int]):
        # roll log to constrain memory usage
        if len(self.log) >= self.log_limit:
            self.log.popleft()

        self.log.append(state.copy())

        time.sleep(self.sleep_seconds)

    def close(self):
        # indicate close message in the log
        self.write([-1])
