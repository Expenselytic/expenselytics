#!/usr/bin/env python3
"""Check the hard 74-column limit for hand-written application code."""

from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent.parent
LIMIT = 74
SOURCE_ROOTS = (
    "app/src",
    "core/src",
    "db/src",
    "db/sql",
    "web/src",
    "ui/src",
    "scripts",
)
EXTENSIONS = {".java", ".ts", ".html", ".css", ".sql", ".py"}


def main():
    violations = []
    checked = 0
    for source_root in SOURCE_ROOTS:
        for path in sorted((ROOT / source_root).rglob("*")):
            if not path.is_file() or path.suffix not in EXTENSIONS:
                continue
            checked += 1
            lines = path.read_text(encoding="utf-8").splitlines()
            for number, line in enumerate(lines, start=1):
                width = len(line.expandtabs(4))
                if width > LIMIT:
                    location = f"{path.relative_to(ROOT)}:{number}"
                    violations.append(f"{location}: {width} columns")
    if violations:
        print("Lines exceeding 74 columns:", file=sys.stderr)
        print("\n".join(violations), file=sys.stderr)
        return 1
    print(f"Checked {checked} files: all lines are within 74 columns.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
