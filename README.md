# Papiea - Prescriptive and Practical Intent Engine Architecture

<img src="https://upload.wikimedia.org/wikipedia/commons/1/13/Papaya.svg" width="150" height="150">

Papiea, pronounced like the fruit, is an Intent engine based on perscriptions or recipes for handling differences
between intended state and real world state.

Managing system state, especially when state is comprised of multiple seperated individual parts is a complex
task. Papiea allows entites to be in two states: desired and real, named "spec" and "status" respectively. `Providers`
are the mechanism which help Papiea transition an entity from its real state (or its "status") to its desired state (or
its "spec"). Three mechanisms are involved in such process: 

  1. `Add` - an entity has only `spec` but no `status`. The
entity must be created in the real world which will be reflected in its status
  2. `Del` - an entity has only `status`
but no `spec`. The entity must be removed from the real world 
  3. `Change` - an entity has a both `spec` and `status`,
but they differ. The entity must be changed in the real world.

## Usage

### Set up the enviroment
For now the system relies on MongoDB to be
set up. The simplest way to use it is through `docker`. Directions
taken from [here](https://github.com/mvertes/docker-alpine-mongo):

Set up a new docker instance and run it
```sh
docker run -d --name papiea-mongo -p 27017:27017 -v ~/work/mongodb-docker/data:/data/db mvertes/alpine-mongo
```

Stop it
```sh
docker kill papiea-mongo
```

Access it:
```sh
docker exec -ti papiea-mongo sh      # regular shell
docker exec -ti papiea-mongo mongo   # the mongo shell
```

### Run the application locally
This is a library, so it will be used through an application. Stay tuned for some sample applications!

### Run the tests

`lein test`

# License and copyright

Copyright (C) 2018 Nutanix

The code in the public repositories is licensed under the Apache
license.

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License.  You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied.  See the License for the specific language governing
permissions and limitations under the License.
