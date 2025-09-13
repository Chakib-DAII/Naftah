#!/usr/bin/env bash

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
			echo "JAVA_HOME not set and cannot find javac to deduce location, please set JAVA_HOME."
			exit 1
		fi
		# readlink(1) is not available as standard on Solaris 10.
		readLink="$(command -v readlink)"
		[ "$(expr "${readLink}" : '\([^ ]*\)')" = "no" ] && {
			echo "JAVA_HOME not set and readlink not available, please set JAVA_HOME."
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
	 Please ensure that your JAVA_HOME points to a valid Java SDK.
	 You are currently pointing to:

	  ${JAVA_HOME}

	 This does not seem to be valid. Please rectify and restart.
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
	echo "Not a directory: NAFTAH_HOME=${NAFTAH_HOME}"
	echo "Please rectify and restart."
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

# Append JVM options to JAVA_OPTS
JAVA_OPTS="$JAVA_OPTS --add-modules=jdk.incubator.vector --add-opens=java.base/java.lang.reflect=ALL-UNNAMED"

IFS=" " read -r -a javaOpts <<< "$JAVA_OPTS"

if [[ "${DEBUG}" == "true" ]]; then
  # Check if "-d" is already present in the arguments
  has_d=false
  for arg in "$@"; do
    if [[ "$arg" == "-d" ]]; then
      has_d=true
      break
    fi
  done

  if [[ "$has_d" == "true" ]]; then
    exec "${JAVA_HOME}/bin/java" "${javaOpts[@]}" -cp "$CLASSPATH" -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5006 -Dfile.encoding=UTF-8 org.daiitech.naftah.Naftah "$@"
  else
    exec "${JAVA_HOME}/bin/java" "${javaOpts[@]}" -cp "$CLASSPATH" -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5006 -Dfile.encoding=UTF-8 org.daiitech.naftah.Naftah "$@" -d
  fi
else
  exec "${JAVA_HOME}/bin/java" "${javaOpts[@]}" -cp "$CLASSPATH" -Dfile.encoding=UTF-8 org.daiitech.naftah.Naftah "$@"
fi