#!/bin/bash
set -e

filesystem="/dev/sdb"
mountpoint="/storage/test.vo"
user="storm"
userid=1001
group="test.vo"
groupid=1003
repo="https://github.com/enricovianello/storm-quotactl-java.git"
branch="master"

echo "hostname -f => $(hostname -f)"

yum -y install git maven vim-enhanced java-1.8.0-openjdk quota xfsprogs-devel glibc-headers

cat /etc/fstab

fdisk -l

lsblk

echo "Check if $filesystem is mounted ..."
if grep -qs "$filesystem" /proc/mounts; then
  echo "$filesystem is mounted ..."
  echo "umount $filesystem ..."
  umount $filesystem
fi

/usr/sbin/wipefs -f $filesystem
/usr/sbin/mke2fs -F -t ext4 $filesystem

cat /etc/fstab

# users and groups

echo "Add $user user ..."
useradd -u $userid $user || echo "User $user already exists."
echo "Add $group group ..."
groupadd $group -g $groupid || echo "Group $group already exists."
echo "Add $group group to $user groups ..."
usermod -G $group $user
echo "Print $user user info ..."
id $user
echo "Copy settings file to $user .m2 directory ..."
mkdir /home/$user/.m2 || echo "Directory already exists."
chown $user:$group /home/$user/.m2
cp /home/vagrant/sync/files/settings.xml /home/$user/.m2/settings.xml

echo "Create '$mountpoint' directory ..."
mkdir -p /storage/test.vo || echo "Directory already exists."

if grep "$filesystem" /etc/fstab; then
  echo "No need to add rows to /etc/fstab ..."
else
  echo "Add row to /etc/fstab ..."
  echo "$filesystem $mountpoint                       ext4    defaults,grpjquota=aquota.group,jqfmt=vfsv0        1 1" >> /etc/fstab
fi

echo "Print /etc/fstab ..."
cat /etc/fstab
echo "Mount $filesystem on '$mountpoint' directory ..."
mount -t ext4 $filesystem $mountpoint
echo "Sleep 10 ..."
sleep 10
echo "Mount list filtered by $filesystem ..."
mount -l | grep $filesystem
echo "Check mount point permissions ..."
ls -latr $mountpoint
echo "Remount $filesystem ..."
mount -o remount $mountpoint
echo "Check mount point permissions ..."
ls -latr $mountpoint
echo "Change ownership of '$mountpoint' directory ..."
chown $user:$group $mountpoint
echo "Change permissions of '$mountpoint' directory ..."
chmod -R 750 $mountpoint
echo "Check mount point permissions ..."
ls -latr $mountpoint

quotacheck -avugm
quotaon -avug

echo "Setting block hard limit to 1000 ..."
setquota -g $group 0 1000 0 0 $filesystem

echo "Report $filesystem quota ..."
repquota -vsig $mountpoint

if ls /home/$user/storm-quotactl-java; then
  cmd="cd storm-quotactl-java && git pull && mvn test -P includeLocalTests"
else
  cmd="git clone $repo && cd storm-quotactl-java && git checkout $branch && mvn test -P includeLocalTests"
fi

su - "storm" -c "whoami; id; $cmd"

echo "finished"