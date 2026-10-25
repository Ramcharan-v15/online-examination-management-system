#!/usr/bin/env python3
"""Render captured terminal output (verbatim, from real runs) as PNG images.

The text is NOT invented here: the script reads .txt transcripts produced
by actually running the program, wraps long lines, and draws them in a
monospace font on a terminal-like background. If a transcript is missing,
the script fails instead of fabricating one.
"""
import sys
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

BASE = Path(__file__).resolve().parent
FONT = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf", 17)

def wrap(line, width=92):
    while len(line) > width:
        yield line[:width]
        line = line[width:]
    yield line

def render(txt_path, png_path):
    lines = []
    for raw in Path(txt_path).read_text().splitlines():
        lines.extend(wrap(raw.rstrip()))
    # measure
    probe = ImageDraw.Draw(Image.new("RGB", (10, 10)))
    char_w = probe.textlength("M", font=FONT)
    line_h = 24
    max_w = max((probe.textlength(l, font=FONT) for l in lines), default=100)
    img = Image.new("RGB", (int(max_w) + 40, line_h * len(lines) + 40), (18, 18, 22))
    draw = ImageDraw.Draw(img)
    for i, line in enumerate(lines):
        draw.text((20, 20 + i * line_h), line, font=FONT, fill=(220, 220, 210))
    img.save(png_path)
    print(f"{png_path}: {img.size[0]}x{img.size[1]} from {txt_path}")

if __name__ == "__main__":
    render(BASE / "screenshots" / "menu-output.txt", BASE / "docs" / "report" / "assets" / "screenshot-menu.png")
    render(BASE / "screenshots" / "result-output.txt", BASE / "docs" / "report" / "assets" / "screenshot-result.png")
    render(BASE / "screenshots" / "menu-output.txt", BASE / "screenshots" / "screenshot-menu.png")
    render(BASE / "screenshots" / "result-output.txt", BASE / "screenshots" / "screenshot-result.png")
