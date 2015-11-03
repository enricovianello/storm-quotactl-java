#!/bin/bash
set -e

echo "hostname -f => $(hostname -f)"

yum -y install git vim-enhanced java-1.8.0-openjdk quota xfsprogs-devel glibc-headers

#install maven
yum -y install maven
#echo -e 'export M2_HOME=/usr/local/maven\nexport PATH=${M2_HOME}/bin:${PATH}' > /etc/profile.d/maven.sh
#cat /etc/profile.d/maven.sh
#source /etc/profile.d/maven.sh
echo 'The maven version: ' `mvn -version` ' has been installed.'

cat /etc/fstab

fdisk -l

lsblk

/usr/sbin/wipefs -f /dev/sdb
/usr/sbin/mke2fs -F -t ext4 /dev/sdb

cat /etc/fstab

echo "Create '/storage' directory ..."
mkdir /storage
echo "Create '/storage/test.vo' directory ..."
mkdir /storage/test.vo
echo "Create '/storage/atals' directory ..."
mkdir /storage/atlas
echo "Add storm user ..."
useradd storm
echo "Add test.vo group ..."
groupadd test.vo
echo "Add test.vo group to storm groups ..."
usermod -G test.vo storm
echo "Print storm user info ..."
id storm
echo "Change ownership of '/storage/test.vo' directory ..."
chown storm:test.vo /storage/test.vo
echo "Change permissions of '/storage/test.vo' directory ..."
chmod -R 750 /storage/test.vo

echo "Mount /dev/sdb on '/storage/test.vo' directory ..."
mount -t ext4 /dev/sdb /storage/test.vo
echo "Mount list filtered by /dev/sdb ..."
mount -l | grep /dev/sdb

echo "Add row to /etc/ftsb ..."
echo '/dev/sdb /storage/test.vo                       ext4    defaults,grpjquota=aquota.group,jqfmt=vfsv0        1 1' >> /etc/fstab

echo "Print /etc/fstab ..."
cat /etc/fstab

echo "Remount /dev/sdb ..."
mount -o remount /storage/test.vo

echo "Sleep 10 ..."
sleep 10

quotacheck -avugm
quotaon -avug

echo "Setting block hard limit to 1000 ..."
setquota -g test.vo 0 1000 0 0 /dev/sdb

echo "Report quota ..."
repquota -vsig /storage/test.vo

cmd1="git clone https://github.com/enricovianello/storm-quotactl-java.git"
cmd2="cd storm-quotactl-java"
cmd3="mvn test -P includeLocalTests"

su - "storm" -c "$cmd1; $cmd2; $cmd3;"

echo "finished"