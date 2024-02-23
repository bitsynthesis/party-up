import threading

from party_up.output.base import Output


class Universe:
    def __init__(self, output: Output, write_enabled: bool = True):
        self.output = output
        self.state = [0] * 512
        self.write_enabled = write_enabled
        self.writer = threading.Thread(target=self.start)
        self.writer.start()

    # TODO rename and replace with something that actually starts the writer thread
    def start(self):
        while self.write_enabled:
            self.output.write(self.state)

    def stop(self):
        self.write_enabled = False

    def close(self):
        self.stop()
        self.output.close()
