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
          "projects" "methods" "rules" "building" "deploying" "yaml"
          "mvel")

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
    <img src="file://$tempdir/arara-logo-with-text-bottom.svg"
         alt="arara logo" />
  </div>
  <div id="refbox">Reference manual</div>
  <div id="metadata">
    <span>The Island of TeX</span>
    <a href="https://gitlab.com/islandoftex/arara">
      <img src="file://$tempdir/gitlab-icon.svg"
           alt="GitLab" />
    </a>
    <span>Version 7.0.0</span>
  </div>
</article>
<article id="license">
  <h1>License</h1>
  <p>arara is licensed under the
    <a href="https://opensource.org/licenses/BSD-3-Clause">New BSD
    License</a>. It is important to observe that the New BSD License has been
    verified as a GPL-compatible free software license by the
    <a href=http://www.fsf.org/">Free Software Foundation</a>, and has been
    vetted as an open source license by the
    <a href="http://www.opensource.org/">Open Source Initiative</a>.
  </p>
  <blockquote>
    <strong>New BSD License</strong>
    <p>
      Copyright 2012–2022, Island of TeX<br/>
      All rights reserved.
    </p>
    <p>
      Redistribution and use in source and binary forms, with or without
      modification, are permitted provided that the following conditions are
      met:
    </p>
    <ol>
      <li>Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.</li>
      <li>Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
      </li>
      <li>Neither the name of the copyright holder nor the names of its
        contributors may be used to endorse or promote products derived from
        this software without specific prior written permission.</li>
    </ol>
    <p>
      This software is provided by the copyright holders and contributors “as
      is” and any express or implied warranties, including, but not limited to,
      the implied warranties of merchantability and fitness for a particular
      purpose are disclaimed. In no event shall the copyright holder or
      contributors be liable for any direct, indirect, incidental, special,
      exemplary, or consequential damages (including, but not limited to,
      procurement of substitute goods or services; loss of use, data, or
      profits; or business interruption) however caused and on any theory of
      liability, whether in contract, strict liability, or tort (including
      negligence or otherwise) arising in any way out of the use of this
      software, even if advised of the possibility of such damage.
    </p>
  </blockquote>
</article>
EOF

# assemble the manual content by fetching chapters from the webpage;
# while doing so assemble the toc for that chapter as well
toc_content=""
chapter_content=""
for chapter in "${chapters[@]}"
do
  wget -O "tmp-$htmlfile" "$baseurl/arara/manual/$chapter" || continue
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

  # create toc entries by iterating over all headings of the chapter;
  # because we normalized headings above and alredy added the chapter
  # title, this will create the whole chapter toc
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
<h1>Contents</h1>
<ul>
$toc_content
</ul>
</article>
$chapter_content
</body>
</html>
EOF

# convert the created HTML manual to PDF
weasyprint -u "$baseurl" "$htmlfile" "$pdffile"

# change back to the docs directory and copy output PDF there
cd "$currdir"
cp "$tempdir/$pdffile" ./
