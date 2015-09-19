pushd `dirname $0` > /dev/null
SCRIPTPATH=`pwd`
popd > /dev/null

JARPATH="$SCRIPTPATH/target"
if [ ! -d "$JARPATH" ]; then
  echo "Milky Way service does not exists. Probably not yet built.. Please use \"mvn clean install\""
  exit 1
fi


java -jar "$JARPATH"/milky-way-*.jar $@