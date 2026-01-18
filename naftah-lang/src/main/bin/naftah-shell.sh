#!/usr/bin/env bash

# SPDX-License-Identifier: Apache-2.0
# Copyright © The Naftah Project Authors

# OS specific support (must be 'true' or 'false').
cygwin=false
darwin=false
case "$(uname)" in
	CYGWIN*)
		cygwin=true
		;;

	MINGW*)
		cygwin=true
		;;

	Darwin*)
		darwin=true
		;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched.
if $cygwin ; then
	[ -n "${JAVA_HOME}" ] && JAVA_HOME=$(cygpath --unix "${JAVA_HOME}")
fi

# Attempt to find JAVA_HOME if not already set
if [ -z "${JAVA_HOME}" ]; then
	if ${darwin} ; then
	  # Force UTF-8 locale for Linux systems
    export LC_ALL=en_US.UTF-8
    export LANG=en_US.UTF-8
    export LANGUAGE=en_US.UTF-8

		if [[ -z "${JAVA_HOME}" && -f "/usr/libexec/java_home" ]]; then
			JAVA_HOME=$(/usr/libexec/java_home)
			export JAVA_HOME
		fi
		if [[ -z "${JAVA_HOME}" && -d "/Library/Java/Home" ]]; then
			export JAVA_HOME="/Library/Java/Home"
		fi
		if [[ -z "${JAVA_HOME}" && -d "/System/Library/Frameworks/JavaVM.framework/Home" ]]; then
			export JAVA_HOME="/System/Library/Frameworks/JavaVM.framework/Home"
		fi
	else
		javaExecutable="$(command -v javac)"
		if [[ -z "$javaExecutable" || "$(expr "${javaExecutable}" : '\([^ ]*\)')" = "no" ]]; then
			echo "لم يتم تعيين JAVA_HOME ولا يمكن العثور على javac لتحديد موقعه، يرجى تعيين JAVA_HOME."
			exit 1
		fi
		# readlink(1) is not available as standard on Solaris 10.
		readLink="$(command -v readlink)"
		[ "$(expr "${readLink}" : '\([^ ]*\)')" = "no" ] && {
			echo "لم يتم تعيين JAVA_HOME وأداة readlink غير متاحة، يرجى تعيين JAVA_HOME."
			exit 1
		}
		javaExecutable="$(readlink -f "${javaExecutable}")"
		javaHome="$(dirname "${javaExecutable}")"
		javaHome=$(expr "$javaHome" : '\(.*\)/bin')
		JAVA_HOME="${javaHome}"
		export JAVA_HOME
	fi
fi

# Sanity check that we have java
if [ ! -f "${JAVA_HOME}/bin/java" ]; then
	cat <<-JAVA_HOME_NOT_SET_TXT

	======================================================================================================
	 يرجى التأكد من أن JAVA_HOME يشير إلى نسخة Java SDK صحيحة.
	 أنت حالياً تشير إلى:

	  ${JAVA_HOME}

	 هذا لا يبدو صحيحًا. يرجى تصحيحه وإعادة التشغيل.
	======================================================================================================

	JAVA_HOME_NOT_SET_TXT
	exit 1
fi

# Attempt to find NAFTAH_HOME if not already set
if [ -z "${NAFTAH_HOME}" ]; then
	# Resolve links: $0 may be a link
	PRG="$0"
	# Need this for relative symlinks.
	while [ -h "$PRG" ] ; do
		ls=$(ls -ld "$PRG")
		link=$(expr "$ls" : '.*-> \(.*\)$')
		if expr "$link" : '/.*' > /dev/null; then
			PRG="$link"
		else
			PRG=$(dirname "$PRG")"/$link"
		fi
	done
	SAVED="$(pwd)"
	cd "$(dirname "${PRG}")/../" > /dev/null || exit 1
	NAFTAH_HOME="$(pwd -P)"
	export NAFTAH_HOME
	cd "$SAVED" > /dev/null || exit 1
fi

if [ ! -d "${NAFTAH_HOME}" ]; then
	echo "المسار ليس مجلدًا: NAFTAH_HOME=${NAFTAH_HOME}"
	echo "يرجى تصحيح ذلك وإعادة التشغيل."
	exit 2
fi

[[ "${cygwin}" == "true" ]] && NAFTAHPATH=$(cygpath "${NAFTAH_HOME}") || NAFTAHPATH=$NAFTAH_HOME
CLASSPATH=${NAFTAHPATH}/bin
if [ -d "${NAFTAHPATH}/ext" ]; then
	CLASSPATH=$CLASSPATH:${NAFTAHPATH}/ext
fi
for f in "${NAFTAHPATH}"/lib/*; do
	[[ "${cygwin}" == "true" ]] && LIBFILE=$(cygpath "$f") || LIBFILE=$f
	CLASSPATH=$CLASSPATH:$LIBFILE
done

if $cygwin; then
	NAFTAH_HOME=$(cygpath --path --mixed "$NAFTAH_HOME")
	CLASSPATH=$(cygpath --path --mixed "$CLASSPATH")
fi

# Load all .vmoptions files in NAFTAH_HOME
VM_OPTS=""

if [ -d "$NAFTAH_HOME" ]; then
  for file in "$NAFTAH_HOME"/*.vmoptions; do
    [ -e "$file" ] || continue

    while IFS= read -r line || [ -n "$line" ]; do
      # Trim leading/trailing whitespace
      line="${line#"${line%%[![:space:]]*}"}"
      line="${line%"${line##*[![:space:]]}"}"

      # Skip empty lines and full-line comments
      [[ -z "$line" || "$line" == \#* ]] && continue

      # Strip inline comments
      line="${line%%#*}"

      # Trim again after stripping comments
      line="${line#"${line%%[![:space:]]*}"}"
      line="${line%"${line##*[![:space:]]}"}"

      # Append if not empty
      [[ -n "$line" ]] && VM_OPTS="$VM_OPTS $line"
    done < "$file"
  done
fi

# Append JVM options to JAVA_OPTS
JAVA_OPTS="$JAVA_OPTS $VM_OPTS \
--add-modules=jdk.incubator.vector \
--add-opens=java.base/java.lang=ALL-UNNAMED \
--add-opens=java.base/java.lang.invoke=ALL-UNNAMED \
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
--add-opens=java.base/sun.invoke.util=ALL-UNNAMED \
--add-opens=java.base/java.util=ALL-UNNAMED \
--add-opens=java.base/java.util.concurrent=ALL-UNNAMED \
--add-opens=java.base/java.util.stream=ALL-UNNAMED \
--add-opens=java.base/java.time=ALL-UNNAMED \
--add-opens=java.base/java.io=ALL-UNNAMED \
--add-opens=java.base/java.net=ALL-UNNAMED \
--add-opens=java.base/sun.net=ALL-UNNAMED \
--add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED \
--add-opens=java.base/sun.net.www.protocol.file=ALL-UNNAMED \
--add-opens=java.base/java.util.zip=ALL-UNNAMED \
--add-opens=java.base/java.util.jar=ALL-UNNAMED \
--add-opens=java.base/java.security=ALL-UNNAMED \
--add-opens=java.base/sun.security.util=ALL-UNNAMED \
--add-opens=java.base/sun.security.x509=ALL-UNNAMED \
--add-opens=java.base/java.nio=ALL-UNNAMED \
--add-opens=java.base/java.nio.channels=ALL-UNNAMED \
--add-opens=java.base/java.nio.channels.spi=ALL-UNNAMED \
--add-opens=java.base/java.nio.charset=ALL-UNNAMED \
--add-opens=java.base/java.nio.file=ALL-UNNAMED \
--add-opens=java.base/java.nio.file.spi=ALL-UNNAMED \
--add-opens=java.base/java.nio.file.attribute=ALL-UNNAMED \
--add-opens=java.base/sun.nio=ALL-UNNAMED \
--add-opens=java.base/sun.nio.fs=ALL-UNNAMED \
--add-opens=java.base/sun.nio.cs=ALL-UNNAMED \
--add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
--add-opens=java.base/sun.reflect.annotation=ALL-UNNAMED \
--add-opens=java.base/sun.reflect.misc=ALL-UNNAMED \
--add-opens=java.base/java.lang.ref=ALL-UNNAMED \
--add-opens=java.base/jdk.internal.ref=ALL-UNNAMED \
--add-opens=java.base/jdk.internal.loader=ALL-UNNAMED \
--add-opens=java.base/jdk.internal.module=ALL-UNNAMED \
--add-opens=java.xml/com.sun.org.apache.xerces.internal.parsers=ALL-UNNAMED \
--add-opens=java.xml/com.sun.org.apache.xerces.internal.dom=ALL-UNNAMED \
--add-opens=java.xml/com.sun.org.apache.xerces.internal.jaxp=ALL-UNNAMED \
--add-opens=java.desktop/java.awt=ALL-UNNAMED \
--add-opens=java.desktop/sun.awt=ALL-UNNAMED \
--add-opens=java.desktop/javax.swing=ALL-UNNAMED \
--add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED"


IFS=" " read -r -a javaOpts <<< "$JAVA_OPTS"

if [[ "${DEBUG}" == "true" ]]; then
  # Detect LAN IP dynamically
  LAN_IP=$(hostname -I | awk '{print $1}')
  export JAVA_TOOL_OPTIONS="-Djava.rmi.server.hostname=$LAN_IP"

  # Check if "-d" is already present in the arguments
  has_d=false
  for arg in "$@"; do
    if [[ "$arg" == "-d" ]]; then
      has_d=true
      break
    fi
  done

  if [[ "$has_d" == "true" ]]; then
    exec "${JAVA_HOME}/bin/java" "${javaOpts[@]}" \
         -cp "$CLASSPATH" \
         -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006 \
         -Dfile.encoding=UTF-8 \
         org.daiitech.naftah.Naftah "$@"
  else
    exec "${JAVA_HOME}/bin/java" "${javaOpts[@]}" \
         -cp "$CLASSPATH" \
         -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006 \
         -Dfile.encoding=UTF-8 \
         org.daiitech.naftah.Naftah "$@" -d
  fi
else
  exec "${JAVA_HOME}/bin/java" "${javaOpts[@]}" \
       -cp "$CLASSPATH" \
       -Dfile.encoding=UTF-8 \
       org.daiitech.naftah.Naftah "$@"
fi