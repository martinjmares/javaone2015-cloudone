pushd `dirname $0` > /dev/null
SCRIPTPATH=`pwd`
popd > /dev/null

JARPATH="$SCRIPTPATH/target"
if [ ! -d "$JARPATH" ]; then
  echo "Info proxy service does not exists. Probably not yet built.. Please use \"mvn clean install\""
  exit 1
fi


java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -jar "$JARPATH"/info-proxy-*.jar $@