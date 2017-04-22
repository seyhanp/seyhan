#!/bin/bash
# Start Stop the Seyhan ERP

### BEGIN INIT INFO
# Provides:          seyhan
# Required-Start:    $network $remote_fs $syslog
# Required-Stop:     $network $remote_fs $syslog
# Should-Start:      network-manager
# Should-Stop:       network-manager
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Seyhan Enterprise Resource Planning or Accounting
# Description: This script will start Seyhan ERP
 #
#  Please change the SEYHAN_USER and SEYHAN_HOME as your needs
#
### END INIT INFO
#
#  seyhan full directory
SEYHAN_HOME=/home/seyhan/seyhan-1.0.18
#

#seyhan username (dont run as root unless you know what you are doing)
SEYHAN_USER=seyhan
#
#
PATH=/bin:/usr/bin:/sbin:/usr/sbin
DESC="seyhan erp"
NAME=seyhanserver
DAEMON=$SEYHAN_HOME/seyhandaemon.sh
#PIDFILE=$SEYHAN_HOME/SEYHANSERVER_PID_FILE.pid
SCRIPTNAME=/etc/init.d/"$NAME"

if [ -r /lib/lsb/init-functions ]; then
    . /lib/lsb/init-functions
else
    exit 1
fi

case $1 in

        start)
	pushd $SEYHAN_HOME
	su -c "$DAEMON start" $SEYHAN_USER
	;;

        stop)
	pushd $SEYHAN_HOME
	su -c "$DAEMON stop" $SEYHAN_USER
	;;

	*)
	echo "Usage: /etc/init.d/$NAME {start|stop}"
	;;
esac
