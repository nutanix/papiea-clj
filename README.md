# Papiea - a Prescriptive and Practical Intent Engine Architecture

<img src="https://upload.wikimedia.org/wikipedia/commons/1/13/Papaya.svg" width="150" height="150">

Papiea, pronounced like the fruit, is an Intent engine based on perscriptions or recipes for handling differences
between intended state and real world state.

Managing system state, especially when state is comprised of multiple seperated individual parts is a complex
task. Papiea allows entites to be in two states: desired and real, named "spec" and "status" respectively. `Providers`
are the mechanism which help Papiea transition an entity from its real state (or its "status") to its desired state (or
its "spec"). Three mechanisms are involved in such process: 1. `Add` - an entity has only `spec` but no `status`. The
entity must be created in the real world which will be reflected in its status 2. `Del` - an entity has only `status`
but no `spec`. The entity must be removed from the real world 3. `Change` - an entity has a both `spec` and `status`,
but they differ. The entity must be changed in the real world.

## Usage

### Set up the enviroment For now the system relies on MongoDB to be
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
Once we have mongo accessible, we can simply start the server

`lein ring server`

### Run the tests

`lein test`

### Packaging and running as standalone jar

```sh
lein do clean, ring uberjar
java -jar target/server.jar
```

### Packaging as war

`lein ring uberwar`

## License

Copyright ©  FIXME
