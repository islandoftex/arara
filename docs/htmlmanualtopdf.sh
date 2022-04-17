#!/usr/bin/env bash

baseurl="https://islandoftex.gitlab.io/"
chapters=("" "introduction" "concepts" "cli" "configuration" "logging" "methods" "rules" "building" "deploying" "yaml" "mvel")
htmlfile="manual.html"
pdffile="manual.pdf"

cat <<EOF > $htmlfile
<!DOCTYPE html>
<html>
<body>
EOF

for chapter in "${chapters[@]}"
do
  wget -O "tmp-$htmlfile" "$baseurl/arara/manual/$chapter"
  cat "tmp-$htmlfile" | pup ':parent-of([class="heading-text"])' | tail -n +2 | head -n -1 >> $htmlfile
done
rm "tmp-$htmlfile"

cat <<EOF >> $htmlfile
</body>
</html>
EOF

weasyprint -u "$baseurl" "$htmlfile" "$pdffile"
