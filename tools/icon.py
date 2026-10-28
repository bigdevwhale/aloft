"""Writes the launcher icon as vector drawables (and docs/logo.png) from tools/icon.png.

python tools/icon.py

tools/icon.png is drawn by the app's own art code (`./gradlew testDebugUnitTest -Prender --tests '*RenderArt.icon'`,
then copied from app/build/art/icon.png): the sylph over a little island, 32×32. Each run of
same-coloured pixels in a row becomes one rectangle in the vector path.
"""
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
DRAW = ROOT / "app/src/main/res/drawable"
SRC = ROOT / "tools/icon.png"
BG = "#FF2E5CC0"


def runs(img):
    w, h = img.size
    out = {}
    for y in range(h):
        x = 0
        while x < w:
            r, g, b, a = img.getpixel((x, y))
            if a < 128:
                x += 1
                continue
            col = f"#FF{r:02X}{g:02X}{b:02X}"
            start = x
            while x < w and img.getpixel((x, y))[3] >= 128 and "#FF%02X%02X%02X" % img.getpixel((x, y))[:3] == col:
                x += 1
            out.setdefault(col, []).append((start, y, x - start))
    return out


def vector(img, scale, offset, mono=False):
    rs = runs(img)
    if mono:
        rs = {"#FFFFFFFF": [r for v in rs.values() for r in v]}
    return [
        f'    <path android:fillColor="{col}" android:pathData="'
        + " ".join(f"M{offset + x * scale:.2f},{offset + y * scale:.2f}h{w * scale:.2f}v{scale:.2f}h{-w * scale:.2f}z" for x, y, w in r)
        + '" />'
        for col, r in rs.items()
    ]


def main():
    img = Image.open(SRC).convert("RGBA")
    DRAW.mkdir(parents=True, exist_ok=True)
    head = '<?xml version="1.0" encoding="utf-8"?>\n<vector xmlns:android="http://schemas.android.com/apk/res/android"\n    android:width="108dp"\n    android:height="108dp"\n    android:viewportWidth="108"\n    android:viewportHeight="108">\n'
    scale = 66 / img.size[0]
    (DRAW / "ic_launcher_foreground.xml").write_text(head + "\n".join(vector(img, scale, 21)) + "\n</vector>\n", encoding="utf-8", newline="\n")
    small = head.replace("108dp", "24dp").replace('"108"', f'"{img.size[0]}"')
    (DRAW / "ic_stat_isle.xml").write_text(small + "\n".join(vector(img, 1, 0, mono=True)) + "\n</vector>\n", encoding="utf-8", newline="\n")
    (DRAW / "ic_launcher_background.xml").write_text(head + f'    <path android:fillColor="{BG}" android:pathData="M0,0h108v108h-108z" />\n</vector>\n', encoding="utf-8", newline="\n")
    bg = Image.new("RGBA", img.size, tuple(int(BG[i:i + 2], 16) for i in (3, 5, 7)) + (255,))
    bg.alpha_composite(img)
    (ROOT / "docs").mkdir(exist_ok=True)
    bg.resize((512, 512), Image.NEAREST).save(ROOT / "docs/logo.png")


if __name__ == "__main__":
    main()
