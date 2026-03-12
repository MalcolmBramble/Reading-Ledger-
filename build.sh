#!/bin/bash
# Combines split files into a single index.html for preview/testing
cd "$(dirname "$0")"

cat > dist.html << 'HTML_START'
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
<title>The Reading Ledger</title>
<link href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,400;0,500;0,600;0,700;1,400;1,500&family=Outfit:wght@300;400;500;600&display=swap" rel="stylesheet">
<style>
HTML_START

cat css/styles.css >> dist.html
echo '</style>' >> dist.html

# Extract body from index.html (between </head> and the first <script>)
sed -n '/<\/head>/,/<script>/p' index.html | head -n -1 >> dist.html

echo '<script>' >> dist.html
echo 'window.onerror=function(m,s,l){const e=document.createElement("div");e.style.cssText="position:fixed;top:0;left:0;right:0;padding:12px;background:#C45B5B;color:#fff;font:12px Outfit;z-index:9999;white-space:pre-wrap";e.textContent="Error: "+m+" ("+s+":"+l+")";document.body.prepend(e)};' >> dist.html

for f in js/data.js js/helpers.js js/shelf.js js/timeline.js js/insights.js js/analytics.js js/detail.js js/app.js; do
  cat "$f" >> dist.html
  echo '' >> dist.html
done

echo '</script></body></html>' >> dist.html
echo "Built dist.html: $(wc -c < dist.html) bytes"
