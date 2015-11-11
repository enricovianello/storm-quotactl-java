# Posix quota management - Java API 

This Java API allows user to read the group quota from a Posix filesystem, if quota is enabled on that fs.

[PosixQuotaManager](https://github.com/enricovianello/storm-quotactl-java/blob/master/src/main/java/it/grid/storm/api/filesystem/quota/posix/PosixQuotaManager.java) allows to access the quota information on a Posix filesystem. Quotas allow you to control disk usage by user or by group. Quotas prevent individual users and groups from using a larger portion of a filesystem than they are permitted, or from filling it up altogether.

The quotactl() standard C library is called to manipulate disk quotas.

## How to use

```{java}

import it.grid.storm.api.filesystem.quota.posix.PosixQuotaManager;
import it.grid.storm.api.filesystem.quota.posix.PosixQuotaException;
import it.grid.storm.api.filesystem.quota.posix.PosixQuotaInfo;

...

String BLOCKDEVICE = "/dev/sdb";
int GID = 1003;

PosixQuotaManager pqm = new PosixQuotaManager();
PosixQuotaInfo pqi = null;

try {

  pqi = pqm.getGroupQuota(blockDevice, gid);

} catch (PosixQuotaException pqe) {

  ...

}

System.out.println(pqi.toString());

```
