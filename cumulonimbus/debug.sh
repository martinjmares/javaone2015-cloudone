pushd `dirname $0` > /dev/null
SCRIPTPATH=`pwd`
popd > /dev/null

JARPATH="$SCRIPTPATH/target"
if [ ! -d "$JARPATH" ]; then
  echo "Cumulonimbus does not exists. Probably not yet built. Please use \"mvn clean install\""
  exit 1
fi


java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 -jar "$JARPATH"/cumulonimbus-*.jar $@