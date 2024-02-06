from functools import partial

import urwid

from party_up.universe2 import Universe
from party_up.fixture.base import Fixture


palette = [
    ("banner", "black", "light gray"),
    ("streak", "black", "dark red"),
    ("bg", "black", "dark blue"),
    ("selected", "light gray", "black"),
    ("fixture_list_item", "white", "black"),
    ("fixture_list_item_selected", "black", "white"),
]


# https://stackoverflow.com/a/56759094
class SimpleButton(urwid.Text):
    _selectable = True
    signals = ["click"]

    def keypress(self, size, key):
        """
        Send 'click' signal on 'activate' command.
        """
        if self._command_map[key] != urwid.ACTIVATE:
            return key

        self._emit("click")

    def mouse_event(self, size, event, button, x, y, focus):
        """
        Send 'click' signal on button 1 press.
        """
        if button != 1 or not urwid.util.is_mouse_press(event):
            return False

        self._emit("click")
        return True


class ImmediateSelectPile(urwid.Pile):
    def keypress(self, size, key):
        result = super().keypress(size, key)

        if key in {"up", "down"}:
            self.keypress(size, "enter")

        # # TODO does this really belong here?
        # if key == "right":
        #     self._emit("click")

        return result


class Layout:
    def __init__(self):
        self.header = urwid.Text(("banner", "Party Up!"), align="center")
        self.left_column = ImmediateSelectPile([urwid.Button("Nothing...")])
        self.right_column = ImmediateSelectPile([urwid.Button("oh hai")])

        self.columns = urwid.Columns(
            [
                ("weight", 1, urwid.LineBox(self.left_column)),
                ("weight", 3, urwid.LineBox(self.right_column)),
            ]
        )

        self.body = urwid.ListBox(urwid.SimpleListWalker([self.header, self.columns]))


def format_fixture_list_item(fixture: Fixture) -> str:
    return fixture.name


def fixture_list_select(widget: urwid.Button, data: tuple[Fixture, Layout]):
    fixture, layout = data

    layout.columns.contents[1][0].set_title(f"{fixture.name} Channels")

    contents = []
    for i, channel in enumerate(fixture.channels):
        full_address = fixture.address + i
        value = fixture.universe.state[full_address]
        item = urwid.Columns(
            [
                ("pack", urwid.Text(f"{full_address + 1: 3}")),
                urwid.Padding(urwid.Text(channel), left=2),
                ("pack", urwid.Text(str(value))),
            ]
        )
        # item = urwid.Text(f"{full_address} {channel}                {value}")
        options = layout.right_column.options()
        contents.append((item, options))

    layout.right_column.contents = contents


def create_capabilities_list(data: tuple[Fixture, Layout]):
    fixture, layout = data

    # layout.columns.contents[1][0].contents


def create_fixtures_list(fixtures: list[Fixture], layout: Layout):
    layout.columns.contents[0][0].set_title("Fixtures")

    contents = []
    for fixture in fixtures:
        button = SimpleButton(format_fixture_list_item(fixture))
        urwid.connect_signal(button, "click", fixture_list_select, (fixture, layout))
        final = urwid.AttrMap(
            button,
            "fixture_list_item",
            "fixture_list_item_selected",
        )
        options = layout.left_column.options()
        contents.append((final, options))

    layout.left_column.contents = contents
    layout.left_column.set_focus(0)
    layout.left_column.contents[0][0].keypress(1, "enter")


def show_or_exit(universe: Universe, layout: Layout, key):
    if key in ("q", "Q"):
        raise urwid.ExitMainLoop()
    else:
        layout.header.set_text(("banner", repr(key)))


def start_tui(universe: Universe, fixtures: list[Fixture]):
    # txt = urwid.Text(("banner", "Party Up!"), align="center")
    # txt_map = urwid.AttrMap(txt, "streak")
    #
    # fill = urwid.Filler(txt_map, "top")
    # fill_map = urwid.AttrMap(fill, "bg")
    #
    # # START
    # fixture_list_items = [
    #     urwid.Button(
    #         format_fixture_list_item(fixture),
    #         on_press=fixture_list_select,
    #         user_data=fixture
    #     )
    #     for fixture in fixtures
    # ]
    #
    # content = urwid.SimpleListWalker(
    #     [urwid.AttrMap(w, None, "selected") for w in fixture_list_items]
    # )
    #
    # listbox = urwid.ListBox(content)
    # # END

    try:
        layout = Layout()

        create_fixtures_list(fixtures, layout)

        loop = urwid.MainLoop(
            layout.body,
            palette,
            unhandled_input=partial(show_or_exit, universe, layout),
        )

        loop.run()

    except BaseException as err:
        with open("errors.txt", "a") as f:
            f.write(err.message + "\n")
