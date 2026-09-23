"""Regenerates the Play Store / website feature graphic for PadelCompanion.

Rebuilds the 1024x500 banner from its parts rather than patching the old PNG:
the watch face circle overlaps the wordmark, so pasting a new face over the
flattened image would cover the text.

Layout values were measured off the original graphic so the result lines up
with it: icon at (100,170) 160x160, wordmark ink starting at (304,219),
tagline at (301,274), watch circle centred on (784.5,250) with an outer
radius of 168 and an 8px bezel.

Usage:  python resources/make_banner.py <watch-screenshot.png>
"""
import sys
import numpy as np
from PIL import Image, ImageDraw, ImageFont

# Pristine source for the background gradient and app icon. Kept separate
# so repeat runs do not compound on their own output.
ORIGINAL = 'resources/feature-graphic-base.png'
OUT = 'resources/feature-graphic.png'
ROBOTO = ('C:/Program Files/Android/Android Studio/plugins/design-tools/'
          'resources/layoutlib/data/fonts/Roboto-Regular.ttf')

W, H = 1024, 500
ICON_BOX = (100, 170, 160)          # left, top, size
HEADLINE = ('PadelCompanion', 304, 219, 364, (245, 245, 245))
TAGLINE = ('Score tracker for your wrist', 301, 274, 258, (203, 209, 219))
CX, CY, R_OUT, RING_W = 784.5, 250.0, 168, 8
RING = (106, 108, 121)
SS = 4


def fitted_background(src):
    """Reproduces the smooth navy gradient with a per-channel quadratic fit."""
    a = np.asarray(src).astype(float)
    yy, xx = np.mgrid[0:H, 0:W]
    # Keep only areas that are plain background in the original.
    bg = ((xx < 90) | (yy < 60) | (yy > 440) | (xx > 960))
    basis = lambda x, y: np.stack(
        [np.ones_like(x), x, y, x * x, x * y, y * y], axis=-1)
    A = basis(xx[bg] / W, yy[bg] / H)
    full = basis(xx / W, yy / H)
    out = np.zeros((H, W, 3))
    for c in range(3):
        coef, *_ = np.linalg.lstsq(A, a[bg][:, c], rcond=None)
        out[:, :, c] = full @ coef
    return Image.fromarray(np.clip(out, 0, 255).astype('uint8'))


def fit_font(text, target_w):
    """Picks the Roboto size whose rendered ink width matches the original."""
    best, best_err = 10, 1e9
    for size in range(10, 120):
        f = ImageFont.truetype(ROBOTO, size)
        box = f.getbbox(text)
        err = abs((box[2] - box[0]) - target_w)
        if err < best_err:
            best, best_err = size, err
    return ImageFont.truetype(ROBOTO, best)


def draw_text(canvas, spec):
    text, left, top, target_w, colour = spec
    font = fit_font(text, target_w)
    box = font.getbbox(text)
    ImageDraw.Draw(canvas).text((left - box[0], top - box[1]), text,
                                font=font, fill=colour)


def watch(canvas, shot_path):
    inner = int(R_OUT - RING_W / 2) * 2
    face = Image.open(shot_path).convert('RGB').resize((inner, inner), Image.LANCZOS)
    mask = Image.new('L', (inner * SS, inner * SS), 0)
    ImageDraw.Draw(mask).ellipse((0, 0, inner * SS - 1, inner * SS - 1), fill=255)
    mask = mask.resize((inner, inner), Image.LANCZOS)
    canvas.paste(face, (int(CX - inner / 2), int(CY - inner / 2)), mask)

    ring = Image.new('RGBA', (W * SS, H * SS), (0, 0, 0, 0))
    ImageDraw.Draw(ring).ellipse(
        ((CX - R_OUT) * SS, (CY - R_OUT) * SS,
         (CX + R_OUT) * SS, (CY + R_OUT) * SS),
        outline=RING + (255,), width=RING_W * SS)
    ring = ring.resize((W, H), Image.LANCZOS)
    return Image.alpha_composite(canvas.convert('RGBA'), ring).convert('RGB')


def main(shot_path):
    original = Image.open(ORIGINAL).convert('RGB')
    canvas = fitted_background(original)

    left, top, size = ICON_BOX
    canvas.paste(original.crop((left, top, left + size, top + size)), (left, top))

    # The circle overlaps the wordmark, so the watch goes down first and the
    # text is drawn over it, as in the original graphic.
    canvas = watch(canvas, shot_path)
    draw_text(canvas, HEADLINE)
    draw_text(canvas, TAGLINE)
    canvas.save(OUT)
    print('wrote', OUT, canvas.size)


if __name__ == '__main__':
    main(sys.argv[1])
