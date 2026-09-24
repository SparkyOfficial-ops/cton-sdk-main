# Logo Files for CTON-SDK Website

This directory contains several logo files for the CTON-SDK website:

## Files Included

1. `logo.svg` - Main SVG logo with gradient and design elements
2. `logo-simple.svg` - Simplified SVG logo without gradients (fallback)
3. `logo.png` - Current logo file (to be replaced)
4. `logo-new.png` - Placeholder for new PNG logo
5. `favicon.svg` - SVG favicon
6. `favicon.ico` - ICO favicon

## Recommended Process for Creating PNG Versions

To create proper PNG versions of the SVG logos, use ImageMagick or a similar tool:

```bash
# Convert main logo to PNG
magick convert -background none -size 200x200 logo.svg logo.png

# Convert favicon to PNG
magick convert -background none -size 32x32 favicon.svg favicon-32.png
```

## Logo Usage

The website is configured to:
1. Use SVG logos for best quality and scalability
2. Fall back to simplified SVG if needed
3. Use PNG as last resort
4. Support both SVG and ICO favicons

## Design Elements

The logo features:
- Hexagon shape representing blockchain technology
- Blue gradient color scheme (#4fc3f7 to #29b6f6)
- "CTON" text in bold
- "SDK" subtitle
- Abstract node connections in the advanced version