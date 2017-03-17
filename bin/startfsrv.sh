#!/bin/sh

#
# $Id: mqbroker 587 2012-11-20 03:26:56Z shijia.wxr $
#

if [ -z "$CLOUDMQ_HOME" ] ; then
  ## resolve links - $0 may be a link to maven's home
  PRG="$0"

  # need this for relative symlinks
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
      PRG="$link"
    else
      PRG="`dirname "$PRG"`/$link"
    fi
  done

  saveddir=`pwd`

  CLOUDMQ_HOME=`dirname "$PRG"`/..

  # make it fully qualified
  CLOUDMQ_HOME=`cd "$CLOUDMQ_HOME" && pwd`

  cd "$saveddir"
fi

export CLOUDMQ_HOME

nohup sh ${CLOUDMQ_HOME}/bin/runserver.sh com.alibaba.rocketmq.filtersrv.FiltersrvStartup $@ &
