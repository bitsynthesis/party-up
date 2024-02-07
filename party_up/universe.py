import dataclasses
import functools
import threading
import time
import typing

from party_up.output.base import Output


class Universe:
    def __init__(self, output: Output, write_enabled: bool = True):
        self.output = output
        self.state = [0] * 512
        self.write_enabled = write_enabled
        self.writer = threading.Thread(target=self.start)
        self.writer.start()

    def start(self):
        print("Starting")

        while self.write_enabled:
            self.output.write(self.state)

        print("Stopping")

    def stop(self):
        self.write_enabled = False

    def close(self):
        self.stop()
        self.output.close()
