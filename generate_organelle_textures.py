#!/usr/bin/env python3
"""Generate 16x16 pixel art textures for organelle organs."""
from PIL import Image

OUT = "src/main/resources/assets/chestcavity/textures/item"

def img():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))

def px(im, pixels, color):
    for x, y in pixels:
        if 0 <= x < 16 and 0 <= y < 16:
            im.putpixel((x, y), color)

def fill_rect(im, x1, y1, x2, y2, color):
    for x in range(x1, x2 + 1):
        for y in range(y1, y2 + 1):
            if 0 <= x < 16 and 0 <= y < 16:
                im.putpixel((x, y), color)

def oval(cx, cy, rx, ry):
    """Generate pixels for a filled oval."""
    pts = []
    for y in range(-ry, ry + 1):
        for x in range(-rx, rx + 1):
            if (x * x) / (rx * rx + 0.01) + (y * y) / (ry * ry + 0.01) <= 1.0:
                pts.append((cx + x, cy + y))
    return pts

def oval_outline(cx, cy, rx, ry):
    """Generate pixels for an oval outline."""
    pts = []
    for y in range(-ry, ry + 1):
        for x in range(-rx, rx + 1):
            v = (x * x) / (rx * rx + 0.01) + (y * y) / (ry * ry + 0.01)
            if 0.6 <= v <= 1.0:
                pts.append((cx + x, cy + y))
    return pts

def circle_outline(cx, cy, r):
    return oval_outline(cx, cy, r, r)

# --- Mitochondria: bean shape with internal folds ---
def mitochondria():
    im = img()
    # Outer membrane - bean/capsule shape
    outer = (180, 80, 60)
    inner = (220, 120, 80)
    fold = (160, 60, 40)
    matrix = (200, 100, 70)
    # Bean body
    px(im, oval(8, 8, 6, 4), outer)
    px(im, oval(8, 8, 5, 3), matrix)
    # Cristae (internal folds) - horizontal lines
    for y in [6, 8, 10]:
        for x in range(5, 12):
            if (x + y) % 3 != 0:
                im.putpixel((x, y), fold)
    # Highlight
    px(im, [(5, 6), (6, 5)], inner)
    return im

# --- Ribosome: two joined spheres (large + small subunit) ---
def ribosome():
    im = img()
    dark = (120, 90, 160)
    light = (160, 130, 200)
    highlight = (190, 170, 220)
    # Large subunit
    px(im, oval(7, 9, 4, 4), dark)
    px(im, oval(7, 9, 3, 3), light)
    px(im, [(5, 7), (6, 7)], highlight)
    # Small subunit
    px(im, oval(10, 5, 3, 2), dark)
    px(im, oval(10, 5, 2, 1), light)
    px(im, [(9, 4)], highlight)
    return im

# --- Smooth ER: wavy tubes ---
def smooth_er():
    im = img()
    membrane = (100, 160, 180)
    lumen = (140, 200, 220)
    highlight = (180, 230, 240)
    # Wavy parallel tubes
    for row, base_y in enumerate([4, 7, 10, 13]):
        for x in range(3, 14):
            offset = 1 if (x + row) % 4 < 2 else 0
            y = base_y + offset
            if 0 <= y < 16:
                im.putpixel((x, y), membrane)
                if 0 <= y - 1 < 16:
                    im.putpixel((x, y - 1), lumen)
    # Highlight top tube
    px(im, [(4, 3), (5, 3)], highlight)
    return im

# --- Rough ER: wavy tubes with dots (ribosomes) ---
def rough_er():
    im = img()
    membrane = (100, 160, 130)
    lumen = (140, 200, 160)
    ribosome_c = (120, 90, 160)
    for row, base_y in enumerate([4, 8, 12]):
        for x in range(3, 14):
            offset = 1 if (x + row) % 4 < 2 else 0
            y = base_y + offset
            if 0 <= y < 16:
                im.putpixel((x, y), membrane)
                if 0 <= y - 1 < 16:
                    im.putpixel((x, y - 1), lumen)
            # Ribosomes dotted along outside
            if x % 3 == 0 and 0 <= y + 1 < 16:
                im.putpixel((x, y + 1), ribosome_c)
    return im

# --- Golgi: stacked crescents ---
def golgi():
    im = img()
    colors = [
        (180, 150, 60),
        (200, 170, 70),
        (220, 190, 80),
        (200, 170, 70),
        (180, 150, 60),
    ]
    for i, color in enumerate(colors):
        y = 4 + i * 2
        for x in range(4, 13):
            curve = 1 if abs(x - 8) > 3 else 0
            py = y + curve
            if 0 <= py < 16:
                im.putpixel((x, py), color)
    # Vesicles budding off
    px(im, [(13, 5), (14, 7), (13, 9)], (220, 190, 80))
    px(im, [(2, 6), (2, 10)], (180, 150, 60))
    return im

# --- Lysosome: sphere with enzymes inside ---
def lysosome():
    im = img()
    membrane = (80, 160, 80)
    interior = (60, 130, 60)
    enzyme = (200, 220, 80)
    dark = (40, 100, 40)
    px(im, oval(8, 8, 5, 5), membrane)
    px(im, oval(8, 8, 4, 4), interior)
    # Enzyme dots scattered inside
    px(im, [(6, 6), (9, 7), (7, 10), (10, 9), (8, 5), (6, 9)], enzyme)
    # Outline
    px(im, oval_outline(8, 8, 5, 5), dark)
    # Highlight
    px(im, [(5, 5), (6, 5)], (100, 180, 100))
    return im

# --- Nucleus: sphere with chromatin and nucleolus ---
def nucleus():
    im = img()
    membrane = (60, 60, 160)
    interior = (80, 80, 180)
    chromatin = (40, 40, 120)
    nucleolus = (120, 60, 60)
    highlight = (120, 120, 220)
    px(im, oval(8, 8, 6, 6), membrane)
    px(im, oval(8, 8, 5, 5), interior)
    # Chromatin strands
    px(im, [(5, 6), (6, 5), (7, 7), (9, 4), (10, 6), (11, 8), (6, 10), (9, 11), (10, 10)], chromatin)
    # Nucleolus
    px(im, oval(8, 8, 2, 2), nucleolus)
    # Nuclear pores (dots on edge)
    px(im, [(3, 7), (8, 2), (13, 8), (8, 14)], (100, 100, 200))
    # Highlight
    px(im, [(5, 4), (6, 4)], highlight)
    return im

# --- Cell Membrane: curved bilayer cross-section ---
def cell_membrane():
    im = img()
    head = (200, 180, 80)  # phospholipid heads
    tail = (180, 160, 100)  # tails
    protein = (120, 80, 160)  # channel protein
    # Upper leaflet heads
    for x in range(2, 15):
        im.putpixel((x, 5), head)
    # Upper tails
    for x in range(2, 15):
        im.putpixel((x, 6), tail)
        im.putpixel((x, 7), tail)
    # Lower tails
    for x in range(2, 15):
        im.putpixel((x, 8), tail)
        im.putpixel((x, 9), tail)
    # Lower leaflet heads
    for x in range(2, 15):
        im.putpixel((x, 10), head)
    # Channel protein spanning membrane
    for y in range(4, 12):
        im.putpixel((7, y), protein)
        im.putpixel((8, y), protein)
    # Channel opening
    im.putpixel((7, 7), (80, 40, 120))
    im.putpixel((8, 8), (80, 40, 120))
    # Small molecules passing through
    px(im, [(7, 3), (8, 12)], (80, 200, 200))
    return im

# --- Cytoskeleton: crossed filaments ---
def cytoskeleton():
    im = img()
    actin = (200, 100, 100)      # red - thin
    microtubule = (100, 200, 100) # green - thick
    intermediate = (200, 200, 100) # yellow
    # Microtubules (diagonal thick)
    for i in range(12):
        px(im, [(2 + i, 2 + i)], microtubule)
        if 2 + i + 1 < 16:
            px(im, [(2 + i + 1, 2 + i)], microtubule)
    for i in range(12):
        px(im, [(13 - i, 2 + i)], microtubule)
        if 13 - i - 1 >= 0:
            px(im, [(13 - i - 1, 2 + i)], microtubule)
    # Actin filaments (horizontal)
    for x in range(3, 14):
        im.putpixel((x, 7), actin)
        im.putpixel((x, 8), actin)
    # Intermediate filaments (vertical)
    for y in range(3, 14):
        im.putpixel((7, y), intermediate)
        im.putpixel((8, y), intermediate)
    # Junction points brighter
    px(im, [(7, 7), (8, 8), (7, 8), (8, 7)], (255, 255, 200))
    return im

# --- Flagellum: whip-like tail ---
def flagellum():
    im = img()
    base = (180, 120, 80)
    shaft = (160, 100, 60)
    tip = (140, 80, 50)
    # Basal body
    px(im, oval(4, 8, 2, 3), base)
    # Wavy tail extending right
    wave = [(6, 8), (7, 7), (8, 7), (9, 8), (10, 9), (11, 9), (12, 8), (13, 7), (14, 7), (15, 8)]
    px(im, wave, shaft)
    # Slightly thicker near base
    px(im, [(6, 9), (7, 8)], shaft)
    # Tip fades
    px(im, [(14, 7), (15, 8)], tip)
    # Basal body highlight
    px(im, [(3, 7)], (200, 150, 100))
    return im

# --- Cilia: multiple short hairs on a cell surface ---
def cilia():
    im = img()
    surface = (180, 140, 120)
    cilium = (100, 180, 200)
    tip = (140, 210, 230)
    # Cell surface at bottom
    fill_rect(im, 1, 12, 14, 14, surface)
    fill_rect(im, 2, 11, 13, 11, (160, 120, 100))
    # Cilia sprouting up - each slightly different angle
    cilia_bases = [3, 5, 7, 9, 11, 13]
    offsets = [0, -1, 0, 1, 0, -1]
    for i, (bx, off) in enumerate(zip(cilia_bases, offsets)):
        for dy in range(1, 8):
            y = 11 - dy
            x = bx + (off if dy > 3 else 0)
            if 0 <= x < 16 and 0 <= y < 16:
                c = tip if dy > 5 else cilium
                im.putpixel((x, y), c)
    return im

# --- Peroxisome: small sphere with crystalline core ---
def peroxisome():
    im = img()
    membrane = (200, 140, 60)
    interior = (220, 170, 80)
    crystal = (255, 220, 140)
    dark = (160, 100, 40)
    px(im, oval(8, 8, 5, 5), membrane)
    px(im, oval(8, 8, 4, 4), interior)
    # Crystalline core (diamond shape)
    px(im, [(8, 5), (7, 7), (9, 7), (8, 9), (8, 7)], crystal)
    px(im, [(7, 6), (9, 6), (7, 8), (9, 8)], crystal)
    # Outline
    px(im, oval_outline(8, 8, 5, 5), dark)
    px(im, [(5, 5), (6, 5)], (240, 190, 100))
    return im

# --- Chloroplast: oval with thylakoid stacks ---
def chloroplast():
    im = img()
    outer = (40, 140, 40)
    stroma = (60, 170, 60)
    thylakoid = (30, 110, 30)
    granum = (20, 90, 20)
    highlight = (100, 200, 100)
    # Outer membrane
    px(im, oval(8, 8, 7, 4), outer)
    px(im, oval(8, 8, 6, 3), stroma)
    # Thylakoid stacks (grana)
    for gx in [5, 8, 11]:
        for gy in [7, 8, 9]:
            im.putpixel((gx, gy), granum)
            im.putpixel((gx + 1, gy), granum)
    # Connecting thylakoids
    for x in range(5, 13):
        if im.getpixel((x, 8))[3] == 0 or im.getpixel((x, 8)) == stroma:
            im.putpixel((x, 8), thylakoid)
    # Highlight
    px(im, [(4, 5), (5, 5)], highlight)
    return im

# --- Vacuole: large empty sphere with thin membrane ---
def vacuole():
    im = img()
    membrane = (100, 140, 200)
    interior = (160, 200, 240)
    highlight = (200, 230, 255)
    dark = (60, 100, 160)
    # Large circle - thin membrane
    px(im, oval_outline(8, 8, 6, 6), membrane)
    px(im, oval(8, 8, 5, 5), interior)
    # Mostly empty inside - just subtle tone
    px(im, oval(8, 8, 3, 3), (170, 210, 245))
    # Highlight
    px(im, [(4, 4), (5, 4), (4, 5)], highlight)
    # Darker edge
    px(im, [(12, 12), (13, 11), (12, 11)], dark)
    return im


organelles = {
    "mitochondria": mitochondria,
    "ribosome": ribosome,
    "smooth_er": smooth_er,
    "rough_er": rough_er,
    "golgi_apparatus": golgi,
    "lysosome": lysosome,
    "nucleus": nucleus,
    "cell_membrane": cell_membrane,
    "cytoskeleton": cytoskeleton,
    "flagellum": flagellum,
    "cilia": cilia,
    "peroxisome": peroxisome,
    "chloroplast": chloroplast,
    "vacuole": vacuole,
}

for name, fn in organelles.items():
    im = fn()
    path = f"/project/{OUT}/{name}.png"
    im.save(path)
    print(f"  {name}.png")

print(f"\nGenerated {len(organelles)} textures")
