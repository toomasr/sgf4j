#!/bin/sh


if [[ "$1" = "-h" ]] || [[ "$1" = "--help" ]]; then
  echo "Usage: './get_release_commands.sh VERSION_TO_RELEASE [NEW_DEV_VERSION]'"
  echo "Prints out commands to run to release sgf4j-parser to Maven Central."
  echo "VERSION_TO_RELEASE must be a number (integer or floating). NEW_DEV_VERSION is optional"
  exit 0
fi

if ! [[ "$1" =~ ^[0-9]*\.?[0-9]\.?[0-9]*$ ]] || [[ "$1z" = "z" ]]; then
  echo "Not a valid version number given, use -h to get usage options"
  exit 1
fi

version=$1

version_after_dot=${version##*.}
version_str_length=${#version}-${#version_after_dot}

incr_last_digit=$((${version_after_dot}+1))

version_without_last_digit=${version: 0:${version_str_length}}
new_version="${version_without_last_digit}${incr_last_digit}-SNAPSHOT"

echo "#!/bin/sh"

echo "set -xe"

if [[ "$2z" = "z" ]]; then
  echo "# No new development version given using ${new_version}"
else
  new_version=$2
  echo "# New development version will be ${new_version}"
fi


echo "# To release sgf4j-parser version ${version} run these commands:"
echo ""
echo "# 1) set release version"
echo "./mvnw versions:set -DnewVersion=${version}"

echo ""
echo "# 2) commit & tag"
echo "git add pom.xml"
echo "git commit -m \"Prepare release sgf4j-parser-${version}\""
echo "git tag sgf4j-parser-${version}"

echo ""
echo "# 3) deploy to central (builds, signs, and deploys)"
echo "./mvnw clean deploy"

echo ""
echo "# 4) Go to https://central.sonatype.com/publishing/deployments"
echo "# And Drop or Publish"

echo ""
echo "# 5) set new development version"
echo "./mvnw versions:set -DnewVersion=${new_version}"
echo "./mvnw versions:commit"
echo "git add pom.xml"
echo "git commit -m \"prepare for next development iteration\""

echo ""
echo "# 6) push to GitHub"
echo "git push"
echo "git push --tags"

echo ""
echo "# 7) clean local repo"
echo "git clean -fd"
