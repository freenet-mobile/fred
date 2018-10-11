BC_PROVIDER_JAR="bcprov-ext-jdk15on-159.jar"
DOWNLOAD_PATH="${HOME}/.bc"
DOWNLOAD_FULLPATH="${DOWNLOAD_PATH}/${BC_PROVIDER_JAR}"
EXT_FULLPATH="${JAVA_HOME}/jre/lib/ext/${BC_PROVIDER_JAR}"

mkdir -p "${DOWNLOAD_PATH}"

if [ ! -f "${DOWNLOAD_FULLPATH}" ]; then
    # travis-ci is configured to cache ${DOWNLOAD_PATH}
    wget "https://bouncycastle.org/download/${BC_PROVIDER_JAR}" -O "${DOWNLOAD_FULLPATH}";
fi
cp "${DOWNLOAD_FULLPATH}" "${EXT_FULLPATH}"

echo $JAVA_VERSION
ls ${EXT_FULLPATH}

cat /etc/java-7-openjdk/security/java.security|grep security.provider

cp ${HOME}/project/gradle.properties-docker ${HOME}/project/gradle.properties
cat ${HOME}/project/gradle.properties

gradle jar
gradle test
