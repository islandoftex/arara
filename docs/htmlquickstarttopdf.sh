#!/usr/bin/env bash

set -e

# check the required tools; we make these available through the `nix develop`
# shell so you may use that to build the manual
required_tools=("htmlq" "sed" "weasyprint")
for tool in "${required_tools[@]}"
do
  if ! [ -x "$(command -v "$tool")" ]; then
    echo "Please make sure $tool is accessible." >&2
    exit 1
  fi
done

# this script fetches the manual content from the remote rendered version at
# arara's website; caveat: chapters have to be manually adjusted
currdir="$PWD"
basedir="$(dirname "$currdir")/website/public/"
baseurl="file://$basedir"

if [ ! -d "$basedir" ]
then
  echo "Please run zola before converting to PDF." >&2
  exit 1
fi

# create a temporary directory to build the manual in; this will be created
# in /tmp, so users of operating systems without that directory must change
# it here
tempdir="/tmp/arara-quickstart"
if [ -d "$tempdir" ]
then
  rm -r "$tempdir"
fi
mkdir -p "$tempdir"
cp ./resources/* "$tempdir/"
cd "$tempdir"

# these files are created in the temporary directory; no need to change
# anything at this point except that the name of the PDF file will be used
# when copying back to the source directory
htmlfile="quickstart.html"
pdffile="arara-quickstart.pdf"

cat <<EOF > $htmlfile
<!DOCTYPE html>
<html lang="en-US">
<head>
  <meta charset="UTF-8">
  <title>arara</title>

  <meta name="keywords" content="tex, latex, context, build tool, automation">
  <meta name="name" content="The cool TeX automation tool">
  <meta name="description" content="arara is a TeX automation tool based on directives and rules.">

  <link href="file://$tempdir/manual.css" rel="stylesheet">
</head>
<body>
<article>
  <h1>A quickstart guide to arara</h1>
EOF

# assemble the manual content by fetching chapters from the webpage;
# while doing so assemble the toc for that chapter as well
cat <<EOF >> $htmlfile
$(cat "$(echo "$baseurl" | cut -c8-)quickstart/index.html" | htmlq 'div.content.text' --remove-nodes 'div.heading-text' \
  | tail -n +3 | head -n -1 \
  | sed -r 's/<(\/?)h5/<\1h6/g' | sed -r 's/<(\/?)h4/<\1h5/g' \
  | sed -r 's/<(\/?)h3/<\1h4/g' | sed -r 's/<(\/?)h2/<\1h3/g' \
  | sed -r 's/<(\/?)h1/<\1h2/g')
EOF

cat <<EOF >> $htmlfile
</body>
</html>
EOF

# convert the created HTML manual to PDF
weasyprint -u "$baseurl" "$htmlfile" "$pdffile"

# change back to the docs directory and copy output PDF there
cd "$currdir"
cp "$tempdir/$pdffile" ./
