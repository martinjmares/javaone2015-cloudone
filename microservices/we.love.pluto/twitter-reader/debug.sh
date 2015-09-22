pushd `dirname $0` > /dev/null
SCRIPTPATH=`pwd`
popd > /dev/null

JARPATH="$SCRIPTPATH/target"
if [ ! -d "$JARPATH" ]; then
  echo "Twitter Reader service does not exists. Probably not yet built. Please use \"mvn clean install\""
  exit 1
fi


java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -jar "$JARPATH"/twitter-reader-*.jar --file ~/twitter.txt $@