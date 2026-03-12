#!/bin/bash
# Combines split files into a single dist.html for preview
cd "$(dirname "$0")"

# Extract head from index.html up to </head>
sed -n '1,/<\/head>/p' index.html | head -n -1 > dist.html
echo '<style>' >> dist.html
cat css/styles.css >> dist.html
echo '</style>' >> dist.html
echo '</head>' >> dist.html

# Extract body from index.html (between </head> and the first <script>)
sed -n '/<\/head>/,/<script>/p' index.html | tail -n +2 | head -n -1 >> dist.html

echo '<script>' >> dist.html
echo 'window.onerror=function(m,s,l){const e=document.createElement("div");e.style.cssText="position:fixed;top:0;left:0;right:0;padding:12px;background:#C45B5B;color:#fff;font:12px Outfit;z-index:9999;white-space:pre-wrap";e.textContent="Error: "+m+" ("+s+":"+l+")";document.body.prepend(e)};' >> dist.html

for f in js/data.js js/helpers.js js/shelf.js js/timeline.js js/analytics.js js/detail.js js/app.js; do
  cat "$f" >> dist.html
  echo '' >> dist.html
done

echo 'if("serviceWorker" in navigator)navigator.serviceWorker.register("sw.js");' >> dist.html
echo '</script></body></html>' >> dist.html
echo "Built dist.html: $(wc -c < dist.html) bytes"
