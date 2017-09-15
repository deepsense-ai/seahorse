from collections import namedtuple
import os.path
import itertools
import re
import time


def detect_comment_start(stripped_line):
    if stripped_line.startswith("/*"):
        return "/*"
    elif stripped_line.startswith("//"):
        return "//"
    else:
        return None


def is_still_block_comment(stripped_line):
    return not stripped_line.endswith("*/")


def is_still_line_comment(stripped_line):
    return stripped_line.startswith("//")


def is_comment_end(stripped_line):
    return stripped_line.endswith("*/")


def detect_comment_block(it):
    start = 0
    block_comment_started = 1
    line_comment_started = 2
    comment_ended = 3

    State = namedtuple("State", ['state_no', 'in_it', 'pre', 'out'])

    it = iter(it)

    def process(state):
        state_no, in_it, pre, out = state

        assert state_no != comment_ended

        try:
            line = next(in_it)
            stripped_line = line.strip()
        except StopIteration:
            return State(comment_ended, in_it, pre, out)

        if state_no == start:
            comment_type = detect_comment_start(stripped_line)
            if comment_type is None:
                return State(start, in_it, pre + [line], out)
            elif comment_type == '/*':
                return State(state_no=block_comment_started,
                             in_it=itertools.chain([line], in_it),
                             pre=pre,
                             out=out)
            else:
                return State(state_no=line_comment_started,
                             in_it=in_it,
                             pre=pre,
                             out=out + [line])
        elif state_no == block_comment_started:
            if is_still_block_comment(stripped_line):
                return State(state_no=state_no,
                             in_it=in_it,
                             pre=pre,
                             out=out + [line])
            else:
                return State(state_no=comment_ended,
                             in_it=in_it,
                             pre=pre,
                             out=out + [line])
        else:
            assert state_no == line_comment_started
            if is_still_line_comment(stripped_line):
                return State(state_no=state_no,
                             in_it=in_it,
                             pre=pre,
                             out=out + [line])
            else:
                return State(state_no=comment_ended,
                             in_it=itertools.chain([line], in_it),
                             pre=pre,
                             out=out)

    st = State(start, it, [], [])
    while st.state_no != comment_ended:
        st = process(st)

    return st


t1 = """

not comment content

/**
some comment

*/
immediately not comment

rest

"""


t2 = """

not comment

//one line comment

not comment 2

"""


t3 = """
not comment

/* this is one line comment */

not comment 2
"""


t4 = """
no comment at all
"""


t5 = """
not comment

//multiline
//comment

not comment 2
"""


def test_t(t):
    st = detect_comment_block(t.splitlines(True))
    assert st.state_no == 3
    out = st.out
    if len(out) > 0:
        first = out[0].strip()
        last = out[-1].strip()
        assert first.startswith("/*") or first.startswith("//"), first
        assert last.endswith("*/") or last.startswith("//"), last
    assert "".join(st.pre + st.out + list(st.in_it)) == t


def is_comment_a_copyright(comment_lines):
    for comment_line in comment_lines:
        copyright_match = re.search("""copyright\s+(?:\(c\)\s*)?(\d{4})?""",
                                    comment_line, re.I)
        if copyright_match:
            if copyright_match.group(1):
                return copyright_match.group(1)
            else:
                return True

    return False


def replace_copyright_with(it, replacement_fun, default_year=None):
    if default_year is None:
        default_year = time.gmtime().tm_year
    _, in_it, pre, out = detect_comment_block(it)

    if out:
        copyright_year = is_comment_a_copyright(out)

        if copyright_year:
            if isinstance(copyright_year, bool):
                year_to_insert = default_year
            else:
                year_to_insert = copyright_year

            return itertools.chain(pre, replacement_fun(year_to_insert), in_it)

        else:
            return itertools.chain(
                replacement_fun(default_year),
                pre,
                out,
                in_it)
    else:
        return itertools.chain(
            replacement_fun(default_year),
            pre,
            in_it)


def apache_license_lines(year):
    return """/**
 * Copyright {year}, deepsense.ai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

""".format(year=year).splitlines(True)


def replace_copyright_in_file_with(
        filename,
        replacement_fun,
        default_year=None):
    with open(filename) as f:
        lines = f.readlines()

    with open(filename, 'w') as f:
        f.writelines(replace_copyright_with(
            lines,
            replacement_fun,
            default_year))


def find_files(pred):
    def under_dir(dir):
        for dirpath, _, filenames in os.walk(dir):
            for filename in filenames:
                full_filename = os.path.join(dirpath, filename)
                if pred(full_filename):
                    yield full_filename
                else:
                    continue
    return under_dir


def find_scala_files_under_dir(dir):
    return find_files(lambda filename: filename.endswith(".scala"))(dir)


def find_js_files_under_dir(dir):
    return find_files(lambda filename: filename.endswith(".js"))(dir)


def replace_copyright_in_js_files_under_dir_with(
        dir,
        replacement_fun,
        default_year=None):
    for filename in find_js_files_under_dir(dir):
        replace_copyright_in_file_with(
            filename,
            replacement_fun,
            default_year)


def replace_copyright_in_scala_files_under_dir_with(
        dir,
        replacement_fun,
        default_year=None):
    for filename in find_scala_files_under_dir(dir):
        replace_copyright_in_file_with(
            filename,
            replacement_fun,
            default_year)
