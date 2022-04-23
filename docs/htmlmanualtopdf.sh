#!/usr/bin/env bash

set -e

# check the required tools; we make these available through the `nix develop`
# shell so you may use that to build the manual
required_tools=("iconv" "pup" "sed" "wget" "weasyprint")
for tool in "${required_tools[@]}"
do
  if ! [ -x "$(command -v "$tool")" ]; then
    echo "Please make sure $tool is accessible." >&2
    exit 1
  fi
done

# this script fetches the manual content from the remote rendered version at
# arara's website; caveat: chapters have to be manually adjusted
baseurl="https://islandoftex.gitlab.io/"
chapters=("introduction" "concepts" "cli" "configuration" "logging"
          "methods" "rules" "building" "deploying" "yaml" "mvel")

# create a temporary directory to build the manual in; this will be created
# in /tmp, so users of operating systems without that directory must change
# it here
currdir="$PWD"
tempdir="/tmp/arara-manual"
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
htmlfile="manual.html"
pdffile="manual.pdf"

cat <<EOF > $htmlfile
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>arara</title>

  <meta name="keywords" content="tex, latex, context, build tool, automation">
  <meta name="name" content="The cool TeX automation tool">
  <meta name="description" content="arara is a TeX automation tool based on directives and rules.">

  <link href="file://$tempdir/manual.css" rel="stylesheet">
</head>
<body>
<article id="cover">
  <div id="coverimg">
    <img src="file://$tempdir/arara-logo-with-text-bottom.png"
         alt="arara logo" />
  </div>
  <div id="refbox">Reference manual</div>
  <div id="metadata">
    <span>The Island of TeX</span>
    <a href="https://gitlab.com/islandoftex/arara">
      <img src="file://$tempdir/gitlab-icon.svg"
           alt="GitLab" />
    </a>
    <span>Version 6.1.7</span>
  </div>
</article>
EOF

toc_content=""
chapter_content=""
for chapter in "${chapters[@]}"
do
  wget -O "tmp-$htmlfile" "$baseurl/arara/manual/$chapter"
  chapter_title="$(cat "tmp-$htmlfile" | pup '[class="heading-text"] text{}' \
                 | sed '/^[[:space:]]*$/d' | sed 's/ *$//g' | sed 's/^ *//g')"
  this_chapter_content="$(cat <<EOF
<article>
<h1 id="chapter-$chapter">$chapter_title</h1>
$(cat "tmp-$htmlfile" | pup --pre ':parent-of([class="heading-text"])' \
  | tail -n +5 | head -n -1 \
  | sed -r 's/<(\/?)h5/<\1h6/g' | sed -r 's/<(\/?)h4/<\1h5/g' \
  | sed -r 's/<(\/?)h3/<\1h4/g' | sed -r 's/<(\/?)h2/<\1h3/g' \
  | sed -r 's/<(\/?)h1/<\1h2/g')
</article>
EOF
)"

  toc_headings="$(echo "$this_chapter_content" \
                | sed -n -r 's/^.*<h([1-6]) id="(.*)">.*$/\1;\2/p')"
  current_level=0
  while IFS=";" read -r heading_level heading_id
  do
    if [ $heading_level -eq $current_level ]
    then
      toc_content+="</li>"
    fi
    # we are assuming well-formed content where a new heading will always
    # be of the next lower level
    if [ $heading_level -gt $current_level ]
    then
      toc_content+="<ul>"
      current_level=$heading_level
    fi
    # the assumption above does not hold for closing so we have to process
    # levels properly at this point
    while [ $current_level -gt $heading_level ]
    do
      toc_content+="</ul></li>"
      ((current_level--))
    done
    # create table of contents entry; the link destination is sufficient,
    # the actual title is inserted by CSS following the link
    toc_content+="<li><a href=\"#$heading_id\"></a>"
  done <<< "$toc_headings"
  toc_content+="</li>"
  while [ $current_level -gt 1 ]
  do
    toc_content+="</ul></li>"
    ((current_level--))
  done
  toc_content+="</ul>"

  # append chapter content to actual content
  chapter_content+="$this_chapter_content"
done
rm "tmp-$htmlfile"

cat <<EOF >> $htmlfile
<article id="contents">
<h1>Table of contents</h1>
<ul>
$toc_content
</ul>
</article>
$chapter_content
</body>
</html>
EOF

weasyprint -u "$baseurl" "$htmlfile" "$pdffile"

cd "$currdir"
cp "$tempdir/$pdffile" ./
