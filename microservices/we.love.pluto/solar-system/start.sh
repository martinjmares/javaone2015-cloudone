pushd `dirname $0` > /dev/null
SCRIPTPATH=`pwd`
popd > /dev/null

JARPATH="$SCRIPTPATH/target"
if [ ! -d "$JARPATH" ]; then
  echo "SolarSystem service does not exists. Probubly not yet builded. Please use \"mvn clean install\""
  exit 1
fi


java -jar "$JARPATH"/solar-system-*.jar $@