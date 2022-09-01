# form-data-storage-migration-cli

### Overview

Command-line storage migration utility for Low-code Platform that migrates temporary forms data from **Ceph** storage to **Redis**

### Usage

```bash
Example: java -jar form-data-storage-migration-cli.jar \
         --delete-after-migration=<arg> \
	     --delete-invalid-data=<arg> \
	     --s3.config.client.protocol=<arg> \
	     --s3.config.options.pathStyleAccess=<arg> \
	     --storage.backend.ceph.http-endpoint=<arg> \
	     --storage.backend.ceph.access-key=<arg> \
	     --storage.backend.ceph.secret-key=<arg> \
	     --storage.backend.ceph.bucket=<arg> \
	     --storage.backend.redis.password=<arg> \
	     --storage.backend.redis.sentinel.master=<arg> \
	     --storage.backend.redis.sentinel.nodes=<arg>
```
### Args description
#### App args
* `--delete-after-migration` - (Required) whether cli tool should delete migrated from data from source storage(ceph)
* `--delete-invalid-data` - (Required) whether cli tool should delete data with **invalid** key from source storage(ceph)
* `--additional-key-patterns` - (Optional) Additional regex patterns that will be used by key validator for verification of keys then valid keys will be migrated(accepts multiple values separated by ',')

#### Storages config args (Required)
* `--s3.config.client.protocol` - s3 config client protocol
* `--s3.config.options.pathStyleAccess` - s3 config path style access
* `--storage.backend.ceph.http-endpoint` - ceph http endpoint
* `--storage.backend.ceph.access-key` - ceph bucket access key
* `--storage.backend.ceph.secret-key` - ceph bucket secret key
* `--storage.backend.ceph.bucket` - ceph bucket name
* `--storage.backend.redis.password` - redis password
* `--storage.backend.redis.sentinel.master` - master instance name
* `--storage.backend.redis.sentinel.nodes` - nodes hostname

### Test execution

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE.

### License

The registry-regulations-validation-cli is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).